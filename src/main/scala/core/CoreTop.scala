package core

import chisel3._
import chisel3.util._

import common.{Component, MemRequestIO, MemResponseIO}

import pipeline.{InstructionFetchStage, InstructionDecodeStage, ExecuteStage, MemoryStage}

import components.MemToReg._

class CoreTop_IO(dw:Int, mw:Int) extends Bundle
{
    val stall:  Bool   =   Input(Bool())

    val pin:    UInt   =   Output(UInt(dw.W))

    val imemReq: DecoupledIO[MemRequestIO]  =   Decoupled(new MemRequestIO(32, mw))
    val imemRsp: DecoupledIO[MemResponseIO] =   Flipped(Decoupled(new MemResponseIO(32)))

    val dmemReq: DecoupledIO[MemRequestIO]  =   Decoupled(new MemRequestIO(dw, mw))
    val dmemRsp: DecoupledIO[MemResponseIO] =   Flipped(Decoupled(new MemResponseIO(dw)))
}

class CoreTop extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new CoreTop_IO(dataWidth, imemWidth))

    /******** Instruction Fetch Stage ********/

    val IF_stage = Module(new InstructionFetchStage).io

    io.imemReq                  <> IF_stage.core_out.imemReq
    IF_stage.core_out.imemRsp   <> io.imemRsp

    /******** Instruction Fetch Stage ********/

    val ID_stage = Module(new InstructionDecodeStage).io

    ID_stage.if_id  <>  IF_stage.if_id

    // dummies -- to be del when WB comes
    // ID_stage.writeReg := 0.U
    // ID_stage.writeEnable := 0.B
    // ID_stage.writeData := 0.U

    /******** Execute Stage ********/

    val EX_stage = Module(new ExecuteStage).io

    EX_stage.id_ex  <> ID_stage.id_ex

    /******** Execute Stage ********/

    val MEM_stage = Module(new MemoryStage).io

    MEM_stage.ex_mem <> EX_stage.ex_mem

    MEM_stage.core_out.dccmReq <> io.dmemReq
    MEM_stage.core_out.dccmRsp <> io.dmemRsp

    ID_stage.writeReg := MEM_stage.mem_wb.wr_a
    ID_stage.writeEnable := MEM_stage.mem_wb.control.regWrite
    ID_stage.writeData := MuxLookup(MEM_stage.mem_wb.control.memToReg, 0.U)(Seq(
        alu    ->  MEM_stage.mem_wb.alu_result,
        load   ->  MEM_stage.mem_wb.mem_readData,
        pc    ->  MEM_stage.mem_wb.pc
    ))


    io.pin := MEM_stage.mem_wb.alu_result
}