package pipeline

import chisel3._
import chisel3.util._

import common.Component

import components.{Control, RegisterFile, ImmediateGen, NPC, BranchResolutionUnit}

class InstructionDecodeStage_IO(dw: Int, mw: Int) extends Bundle
{
    val if_id   =   Input(new IF_ID(mw))
    val id_ex   =   Output(new ID_EX(dw, mw))

    val writeReg: UInt = Input(UInt())  // writeback reg
    val writeEnable: Bool = Input(Bool()) // writeback we
    val writeData:  UInt    =Input(UInt(dw.W))  // writeback data

    val npc: ValidIO[UInt] = Valid(UInt(mw.W))

    val HDU_takeBranch: Bool = Input(Bool())

    // val branchTaken: Bool = Output(Bool())
    // val ctrlBranch: Bool    =   Output(Bool())

    // For HDU
    // val dmemRespValid: Bool = Input(Bool())
}

class InstructionDecodeStage extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new InstructionDecodeStage_IO(dataWidth, imemWidth))

    // val HDU = Module(new HazardDetectionUnit).io
    // HDU.dmemRespValid   :=  io.dmemRespValid
    // HDU.ID_EX_memRead   := 

    val control = Module(new Control).io
    control.in  <> io.if_id.instruction
    control.out <> io.id_ex.control

    val regFile = Module(new RegisterFile).io
    dontTouch(regFile)

    val registerRd  = io.writeReg
    val registerRs1 = io.if_id.instruction(19, 15)
    val registerRs2 = io.if_id.instruction(24, 20)
    dontTouch(registerRs1)
    dontTouch(registerRs2)

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

    val branchUnit  = Module(new BranchResolutionUnit).io
    branchUnit.branch   := control.out.branch
    branchUnit.funct3   := io.if_id.instruction(14, 12)
    branchUnit.rd1      := regFile.readData(0)
    branchUnit.rd2      := regFile.readData(1)
    branchUnit.take_branch := io.HDU_takeBranch //1.B // TODO: connect w. HDU
    io.id_ex.branchTaken      := branchUnit.taken

    val npc = Module(new NPC).io
    npc.branch_taken    :=  branchUnit.taken
    npc.immediate       :=  immediateGen.out
    npc.rd1             :=  regFile.readData(0)
    npc.ctrl_jump       :=  control.out.jump
    npc.pc              := io.if_id.pc

    io.npc  <> npc.npc

    io.id_ex.pc     := io.if_id.pc
    io.id_ex.wr_a   := io.if_id.instruction(11, 7)
    io.id_ex.f7     := io.if_id.instruction(31, 25)
    io.id_ex.f3     := io.if_id.instruction(14, 12)

    io.id_ex.instruction := io.if_id.instruction

}