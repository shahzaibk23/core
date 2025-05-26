package components

import chisel3._
import chisel3.util._

import common.{Component, MemRequestIO, MemResponseIO}

class InstructionFetch_IO(dw:Int, mw:Int) extends Bundle
{
    val address     : UInt  = Input(UInt(mw.W))
    val stall       : Bool  = Input(Bool())

    val instruction : UInt  = Output(UInt(32.W))

    val coreInstrReq    : DecoupledIO[MemRequestIO]  = Decoupled(new MemRequestIO(dw, mw))
    val coreInstrRsp    : DecoupledIO[MemResponseIO] = Flipped(Decoupled(new MemResponseIO(dw)))
}

class InstructionFetch extends Component
{
    val dataWidth: Int = config.ISA
    val mem_width: Int = log2Ceil(config.ImemSize)

    val io = IO(new InstructionFetch_IO(dataWidth, mem_width))

    // val rst = Wire(Bool())// Question: What is the point of this rst???
    io.coreInstrRsp.ready  := 1.B

    io.coreInstrReq.bits.activeByteLane := "b1111".U
    io.coreInstrReq.bits.isWrite        := 0.B
    io.coreInstrReq.bits.dataRequest    := DontCare

    io.coreInstrReq.bits.addrRequest    := io.address >> 2
    io.coreInstrReq.valid               := Mux(io.stall, 0.B, 1.B)

    io.instruction  := Mux(io.coreInstrRsp.valid, io.coreInstrRsp.bits.dataResponse, DontCare)
}