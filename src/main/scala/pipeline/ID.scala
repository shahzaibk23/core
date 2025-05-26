package pipeline

import chisel3._
import chisel3.util._

import common.Component

import components.{Control, RegisterFile, ImmediateGen}

class InstructionDecodeStage_IO(dw: Int, mw: Int) extends Bundle
{
    val if_id   =   Input(new IF_ID(mw))
    val id_ex   =   Output(new ID_EX(dw, mw))

    val writeReg: UInt = Input(UInt())  // writeback reg
    val writeEnable: Bool = Input(Bool()) // writeback we
    val writeData:  UInt    =Input(UInt(dw.W))  // writeback data
}

class InstructionDecodeStage extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new InstructionDecodeStage_IO(dataWidth, imemWidth))

    val control = Module(new Control).io
    control.in  <> io.if_id.instruction
    control.out <> io.id_ex.control

    val regFile = Module(new RegisterFile).io
    dontTouch(regFile)

    val registerRd  = io.writeReg
    val registerRs1 = io.if_id.instruction(19, 15)
    val registerRs2 = io.if_id.instruction(24, 20)

    regFile.readAddress(0) := registerRs1
    regFile.readAddress(1) := registerRs2
    regFile.writeEnable    := io.writeEnable
    regFile.writeAddress   := registerRd
    regFile.writeData      := io.writeData

    io.id_ex.rd1  := regFile.readData(0)
    io.id_ex.rd2  := regFile.readData(1)

    val immediateGen = Module(new ImmediateGen).io
    immediateGen.instruction := io.if_id.instruction
    io.id_ex.imm := immediateGen.out

    io.id_ex.pc     := io.if_id.pc
    io.id_ex.wr_a   := io.if_id.instruction(11, 7)
    io.id_ex.f7     := io.if_id.instruction(31, 25)
    io.id_ex.f3     := io.if_id.instruction(14, 12)

    io.id_ex.instruction := io.if_id.instruction

}