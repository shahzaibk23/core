package components

import chisel3._
import chisel3.util._

import common.Component
import components.JumpType._

class NPC_IO(dw: Int, mw: Int) extends Bundle
{
    val branch_taken    : Bool          = Input(Bool())
    val immediate       : UInt          = Input(UInt(dw.W))
    val rd1             : UInt          = Input(UInt(dw.W))
    val ctrl_jump       : JumpType.Type = Input(JumpType.Type())
    val pc              : UInt          = Input(UInt(mw.W))

    val npc             : ValidIO[UInt] = Valid(UInt(mw.W))
}

class NPC extends Component
{
    val dataWidth: Int = config.ISA
    val imemWidth: Int = log2Ceil(config.ImemSize)

    val io = IO(new NPC_IO(dataWidth, imemWidth))

    io.npc.valid := io.branch_taken | io.ctrl_jump === jal | io.ctrl_jump === jalr
    io.npc.bits  := MuxCase(io.branch_taken, Seq(
        (io.branch_taken)       -> (io.pc + io.immediate),
        (io.ctrl_jump === jal)  -> (io.pc + io.immediate),
        (io.ctrl_jump === jalr) -> (io.rd1 + io.immediate)
    ))
}