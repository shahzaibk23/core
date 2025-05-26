package components

import chisel3._

object ALUOps
{
    // ALU Operation Selects (5 bits to support RV64)
    val INVALID = "b11111".U(5.W)

    // Base operations
    val ADD     = "b00000".U
    val SUB     = "b00001".U
    val AND     = "b00010".U
    val OR      = "b00011".U
    val XOR     = "b00100".U
    val SLT     = "b00101".U
    val SLTU    = "b00110".U
    val SLL     = "b00111".U
    val SRL     = "b01000".U
    val SRA     = "b01001".U

    // RV64 32-bit word operations
    val ADDW    = "b01010".U
    val SUBW    = "b01011".U
    val SLLW    = "b01100".U
    val SRLW    = "b01101".U
    val SRAW    = "b01110".U

    // ALUOp selector values from decoder
    val ALUOP_ADD   = 0.U(2.W)
    val ALUOP_SUB   = 1.U(2.W)
    val ALUOP_FUNC  = 2.U(2.W) // When funct3/funct7 needed

    // def isMulFN(fn: UInt, cmp: UInt) = fn(1,0) === cmp(1,0)
    // def isSub(cmd: UInt) = cmd(3)
    // def isCmp(cmd: UInt) = (cmd >= FN_SLT && cmd <= FN_SGEU)
    // def isMaxMin(cmd: UInt) = (cmd >= FN_MAX && cmd <= FN_MINU)
    // def cmpUnsigned(cmd: UInt) = cmd(1)
    // def cmpInverted(cmd: UInt) = cmd(0)
    // def cmpEq(cmd: UInt) = !cmd(3)
    // def shiftReverse(cmd: UInt) = !cmd.isOneOf(FN_SR, FN_SRA, FN_ROR, FN_BEXT)
    // def bwInvRs2(cmd: UInt) = cmd.isOneOf(FN_ANDN, FN_ORN, FN_XNOR)

    // val DW_64 = true.B // doesn't belong here
    // val usingConditionalZero = false // zicond
}