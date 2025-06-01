package pipeline

import chisel3._

import components.{ControlSignals, ControlSignals_EX_MEM, ControlSignals_MEM_WB}

class IF_ID(pc_width: Int) extends Bundle
{
    val instruction : UInt  =   UInt(32.W)
    val pc          : UInt  =   UInt(pc_width.W)
}

class ID_EX(dw: Int, mw: Int) extends Bundle
{
    val pc              : UInt  =   UInt(mw.W)
    val rd1             : UInt  =   UInt(dw.W)
    val rd2             : UInt  =   UInt(dw.W)
    val imm             : UInt  =   UInt(dw.W)
    val wr_a            : UInt  =   UInt(5.W)
    val f7              : UInt  =   UInt(7.W)
    val f3              : UInt  =   UInt(3.W)
    val instruction     : UInt  =   UInt(32.W)
    val branchTaken     : Bool  =   Bool()
    val control                 = new ControlSignals
}

class EX_MEM(dw: Int, mw: Int) extends Bundle
{
    val alu_result      : UInt  =   UInt(dw.W)
    val write_data      : UInt  =   UInt(dw.W)
    val wr_a            : UInt  =   UInt(5.W)
    val instruction     : UInt  =   UInt(32.W)
    val pc              : UInt  =   UInt(mw.W)
    // branch
    val control                 = new ControlSignals_EX_MEM
}

class MEM_WB(dw: Int, mw: Int) extends Bundle
{
    val mem_readData    : UInt  =   UInt(dw.W)
    val instruction     : UInt  =   UInt(32.W)
    val alu_result      : UInt  =   UInt(dw.W)
    val branch          : UInt  =   UInt(mw.W)
    val wr_a            : UInt  =   UInt(5.W)
    val pc              : UInt  =   UInt(mw.W)
    val control                 = new ControlSignals_MEM_WB
}