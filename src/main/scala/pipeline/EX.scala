package pipeline

import chisel3._
import chisel3.util._

import common.{Component, Utils}

import components.{ALU, ALUControl, ControlSignals_EX_MEM}
import components.AluSrc1._

class ExecuteStage_IO(dw: Int, mw: Int) extends Bundle
{
    val id_ex   =   Input(new ID_EX(dw, mw))
    val ex_mem  =   Output(new EX_MEM(dw, mw))
}

class ExecuteStage extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new ExecuteStage_IO(dataWidth, imemWidth))

    val ALU         = Module(new ALU).io
    val ALUControl  = Module(new ALUControl).io

    // TODO: Make isword gen
    ALUControl.aluOp    := io.id_ex.control.aluOp
    ALUControl.f7       := io.id_ex.f7
    ALUControl.f3       := io.id_ex.f3
    ALUControl.isWord   := 0.B // hardcode a.t.m

    ALU.input01 := MuxLookup(io.id_ex.control.aluSrc1.asUInt, 0.U) (Seq(
        rs1.asUInt     ->  io.id_ex.rd1,
        pc.asUInt     ->  io.id_ex.pc,
        zero.asUInt   ->  0.U 
    ))
    ALU.input02 := Mux(io.id_ex.control.aluSrc, io.id_ex.rd2, io.id_ex.imm)
    ALU.aluCtrl := ALUControl.out

    //io.ex_mem.control <> io.id_ex.control.asTypeOf(new ControlSignals_EX_MEM) // TODO: make more generic

    Utils.copyCommonFields(io.id_ex.control, io.ex_mem.control)

    io.ex_mem.alu_result    :=  ALU.result
    io.ex_mem.write_data    :=  io.id_ex.rd2
    io.ex_mem.wr_a          :=  io.id_ex.wr_a
    io.ex_mem.instruction   :=  io.id_ex.instruction
    io.ex_mem.pc            :=  io.id_ex.pc
}