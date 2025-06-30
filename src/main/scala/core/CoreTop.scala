package core

import chisel3._
import chisel3.util._

import common.{Component, ComponentIO, MemRequestIO, MemResponseIO}

import pipeline.{InstructionFetchStage, InstructionDecodeStage, ExecuteStage, MemoryStage, IF_ID, ID_EX, EX_MEM, MEM_WB, HazardDetectionUnit, ForwarderUnit}

import uncore.{Tracer, TracerI, delays}

import components.MemToReg._

trait SignatureDump extends ComponentIO
{
    val dccm_we = Output(Bool())
    val dccm_addr = Output(UInt(mw.W))
    val dccm_data = Output(UInt(dw.W))
}

class CoreTop_IO extends ComponentIO with SignatureDump
{
    val stall:  Bool   =   Input(Bool())

    val pin:    UInt   =   Output(UInt(dw.W))

    val imemReq: DecoupledIO[MemRequestIO]  =   Decoupled(new MemRequestIO(32, mw))
    val imemRsp: DecoupledIO[MemResponseIO] =   Flipped(Decoupled(new MemResponseIO(32)))

    val dmemReq: DecoupledIO[MemRequestIO]  =   Decoupled(new MemRequestIO(dw, mw))
    val dmemRsp: DecoupledIO[MemResponseIO] =   Flipped(Decoupled(new MemResponseIO(dw)))

    val rvfi: Option[TracerI]   = if (config.hasTracer) Some(Flipped(new TracerI)) else None

    // val done: Bool = Output(Bool())
}

class CoreTop extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new CoreTop_IO)

    val IF_ID_pipe  =   Reg(new IF_ID(imemWidth))
    val ID_EX_pipe  =   Reg(new ID_EX(dataWidth, imemWidth))
    val EX_MEM_pipe =   Reg(new EX_MEM(dataWidth, imemWidth))
    val MEM_WB_pipe =   Reg(new MEM_WB(dataWidth, imemWidth))

    val HDU = Module(new HazardDetectionUnit).io

    val IF_stage = Module(new InstructionFetchStage).io
    val ID_stage = Module(new InstructionDecodeStage).io
    val EX_stage = Module(new ExecuteStage).io
    val MEM_stage = Module(new MemoryStage).io
    /******** Instruction Fetch Stage ********/

    io.imemReq                  <> IF_stage.core_out.imemReq
    IF_stage.core_out.imemRsp   <> io.imemRsp

    when(HDU.IF_regWrite)
    {IF_ID_pipe <> IF_stage.if_id}

    // IF_ID_pipe <> Mux(HDU.IF_regWrite, IF_stage.if_id, 0.U.asTypeOf(new IF_ID(imemWidth)))

    when(HDU.IF_ID_flush)
    {IF_ID_pipe.instruction := 0.U}

    /******** Instruction Decode Stage ********/

    ID_stage.if_id  <>  IF_ID_pipe  //IF_stage.if_id
    
    IF_stage.npc <> ID_stage.npc

    ID_stage.HDU_takeBranch := HDU.takeBranch

    HDU.ID_EX_memRead   :=  ID_EX_pipe.control.memRead
    HDU.EX_MEM_memRead  :=  EX_MEM_pipe.control.memRead
    HDU.ID_EX_branch    :=  ID_EX_pipe.control.branch
    HDU.ID_EX_rd        :=  ID_EX_pipe.wr_a
    HDU.EX_MEM_rd       :=  ID_EX_pipe.wr_a
    HDU.ID_rs1          :=  IF_ID_pipe.instruction(19, 15)
    HDU.ID_rs2          :=  IF_ID_pipe.instruction(24, 20)
    HDU.dmemRespValid   :=  io.dmemRsp.valid
    HDU.branchTaken     :=  ID_stage.id_ex.branchTaken
    HDU.jump            :=  ID_stage.id_ex.control.jump
    HDU.branch          :=  ID_stage.id_ex.control.branch

    dontTouch(HDU.pcWrite)

    ID_EX_pipe <> ID_stage.id_ex
    
    ID_EX_pipe.control.memWrite := Mux(HDU.ctrlMux && ID_stage.id_ex.instruction =/= "h13".U, ID_stage.id_ex.control.memWrite, 0.B)
    ID_EX_pipe.control.regWrite := Mux(HDU.ctrlMux && ID_stage.id_ex.instruction =/= "h13".U, ID_stage.id_ex.control.regWrite, 0.B)

    var currentRs1 = ID_stage.id_ex.instruction(19, 15)
    var currentRs2 = ID_stage.id_ex.instruction(24, 20)

    val ForwarderUnit = Module(new ForwarderUnit).io // TODO: may need to shift it to exe stage, to avoid critical path
    ForwarderUnit.currentRs1 := currentRs1
    ForwarderUnit.currentRs2 := currentRs2
    ForwarderUnit.ID_EX_rd.addr   := ID_EX_pipe.wr_a
    ForwarderUnit.EX_MEM_rd.addr  := EX_MEM_pipe.wr_a
    ForwarderUnit.ID_EX_rd.value  := EX_stage.ex_mem.alu_result
    ForwarderUnit.EX_MEM_rd.value := EX_MEM_pipe.alu_result

    ID_EX_pipe.rd1 := Mux(ForwarderUnit.finalRs1.valid, ForwarderUnit.finalRs1.bits, ID_stage.id_ex.rd1)
    ID_EX_pipe.rd2 := Mux(ForwarderUnit.finalRs2.valid, ForwarderUnit.finalRs2.bits, ID_stage.id_ex.rd2)


    // dummies -- to be del when WB comes
    // ID_stage.writeReg := 0.U
    // ID_stage.writeEnable := 0.B
    // ID_stage.writeData := 0.U

    /******** Execute Stage ********/

    EX_stage.id_ex  <> ID_EX_pipe //ID_stage.id_ex

    EX_MEM_pipe     <> EX_stage.ex_mem

    /******** Execute Stage ********/

    MEM_stage.ex_mem <> EX_MEM_pipe //EX_stage.ex_mem

    MEM_stage.core_out.dccmReq <> io.dmemReq
    MEM_stage.core_out.dccmRsp <> io.dmemRsp

    MEM_WB_pipe <>  MEM_stage.mem_wb

    ID_stage.writeReg := MEM_WB_pipe.wr_a
    ID_stage.writeEnable := MEM_WB_pipe.control.regWrite
    ID_stage.writeData := MuxLookup(MEM_WB_pipe.control.memToReg, 0.U)(Seq(
        alu    -> MEM_WB_pipe.alu_result,
        load   -> MEM_WB_pipe.mem_readData,
        pc    ->  MEM_WB_pipe.pc
    ))



    io.pin := MEM_WB_pipe.alu_result

    /******** RVFI Pins ********/

    io.dccm_we := MEM_stage.core_out.dccmReq.valid && MEM_stage.core_out.dccmReq.bits.isWrite
    io.dccm_addr := MEM_stage.core_out.unfilteredDccmReq_address
    io.dccm_data := MEM_stage.core_out.dccmReq.bits.dataRequest

    if(config.hasTracer)
    {
        io.rvfi.get.bool := (MEM_WB_pipe.instruction =/= 0.U) //&& !clock.asBool
        io.rvfi.get.uint2 := 3.U
        io.rvfi.get.uint4 := delays(1, MEM_stage.core_out.dccmReq.bits.activeByteLane)

        var rd_addr_vec = Vector(
            ID_EX_pipe.rd1,
            ID_EX_pipe.rd2,
            ID_stage.id_ex.wr_a
        )
        Vector(3, 3, 0).zipWithIndex.foreach(
            r => io.rvfi.get.uint5(r._2) := delays(r._1, rd_addr_vec(r._2))
        )

        Vector(
            MEM_WB_pipe.instruction,
            delays(2, ID_EX_pipe.rd1),
            delays(1, ID_EX_pipe.rd2),
            ID_stage.writeData,
            MEM_WB_pipe.pc,
            delays(4, Mux(ID_stage.npc.valid, ID_stage.npc.bits, ID_stage.id_ex.pc + 4.U)),
            Mux(
                delays(1, MEM_stage.core_out.dccmReq.valid).asBool,
                delays(1, EX_MEM_pipe.alu_result),
                0.U
            ),
            Mux(
                delays(1, EX_MEM_pipe.control.memRead).asBool,
                MEM_WB_pipe.wr_a,
                0.U
            ),
            Mux(
                delays(1, EX_MEM_pipe.control.memWrite).asBool,
                delays(1, MEM_stage.core_out.dccmReq.bits.dataRequest),
                0.U
            )
        ).zipWithIndex.foreach(
            r => io.rvfi.get.uint32(r._2) := r._1
        )
    }

}