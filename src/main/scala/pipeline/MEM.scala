package pipeline

import chisel3._
import chisel3.util._

import common.{Component, MemRequestIO, MemResponseIO, Utils}
import components.ControlSignals_MEM_WB

class MemoryStage_CoreTop_IO(dw: Int, mw: Int) extends Bundle
{
    val dccmReq:    DecoupledIO[MemRequestIO]   =   Decoupled(new MemRequestIO(dw, mw))
    val dccmRsp:    DecoupledIO[MemResponseIO]  =   Flipped(Decoupled(new MemResponseIO(dw)))
}

class MemoryStage_IO(dw: Int, mw: Int) extends Bundle
{
    val ex_mem  =   Input(new EX_MEM(dw, mw))
    val mem_wb  =   Output(new MEM_WB(dw, mw))

    val core_out = new MemoryStage_CoreTop_IO(dw, mw)

    val stall   = Bool()
}

class MemoryStage extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new MemoryStage_IO(dataWidth, imemWidth))

    val rdata = Wire(UInt(dataWidth.W))

    io.core_out.dccmRsp.ready   := 1.B

    io.core_out.dccmReq.bits.activeByteLane := "b1111".U // TODO: make all variants: half word, word, double word
    io.core_out.dccmReq.bits.dataRequest    := io.ex_mem.write_data
    io.core_out.dccmReq.bits.addrRequest    := io.ex_mem.alu_result(imemWidth-1, 0) >> 2
    io.core_out.dccmReq.bits.isWrite        := io.ex_mem.control.memWrite
    io.core_out.dccmReq.valid               := Mux(io.ex_mem.control.memWrite | io.ex_mem.control.memRead, 1.B, 0.B)

    io.stall := (io.ex_mem.control.memWrite || io.ex_mem.control.memRead) && !io.core_out.dccmRsp.valid

    rdata := Mux(io.core_out.dccmRsp.valid, io.core_out.dccmRsp.bits.dataResponse, DontCare)

    io.mem_wb.mem_readData  := rdata
    io.mem_wb.instruction   := io.ex_mem.instruction
    io.mem_wb.alu_result    := io.ex_mem.alu_result
    io.mem_wb.branch        := 0.B //TODO: gen
    io.mem_wb.wr_a          := io.ex_mem.wr_a
    io.mem_wb.pc            := io.ex_mem.pc
    //io.mem_wb.control       := io.ex_mem.control.asTypeOf(new ControlSignals_MEM_WB)
    Utils.copyCommonFields(io.ex_mem.control, io.mem_wb.control)
}