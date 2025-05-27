package pipeline

import chisel3._
import chisel3.util._

import common.{Component, MemRequestIO, MemResponseIO}
import components.{PC, InstructionFetch}

class InstructionFetchStage_Ins(pc_width: Int) extends Bundle
{
    val pc  :   UInt    =   UInt(pc_width.W)
}

class InstructionFetchStage_CoreTop_Out(dw:Int, mw:Int) extends Bundle
{
    val imemReq:    DecoupledIO[MemRequestIO]   =   Decoupled(new MemRequestIO(dw, mw))
    val imemRsp:    DecoupledIO[MemResponseIO]  =   Flipped(Decoupled(new MemResponseIO(dw)))
}

class InstructionFetchStage_IO(data_width: Int, pc_width: Int) extends Bundle
{
    // val in  =   Input(new InstructionFetchStage_Ins(pc_width))
    val if_id =   Output(new IF_ID(pc_width))
    val core_out =   new InstructionFetchStage_CoreTop_Out(data_width, pc_width)

    val npc: Valid[UInt] = Flipped(Valid(UInt(pc_width.W)))
}

class InstructionFetchStage extends Component
{
    val dataWidth: Int  = 32
    val pc_width: Int   = log2Ceil(config.ImemSize)

    val io = IO(new InstructionFetchStage_IO(dataWidth, pc_width))

    val PC = Module(new PC).io
    val IF = Module(new InstructionFetch).io

    PC.halt := 0.B // not wired a.t.m
    PC.in   := Mux(io.npc.valid, io.npc.bits, PC.pc4)

    IF.address := PC.in.asUInt
    IF.stall   := 0.B

    io.core_out.imemReq <> IF.coreInstrReq
    IF.coreInstrRsp     <> io.core_out.imemRsp

    io.if_id.instruction  := IF.instruction
    io.if_id.pc           := PC.pc


}