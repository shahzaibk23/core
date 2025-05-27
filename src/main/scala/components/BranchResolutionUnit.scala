package components

import chisel3._
import chisel3.util._

import common.Component

class BranchResolutionUnit_IO(dw: Int) extends Bundle
{
    val branch:     Bool = Input(Bool())
    val funct3:     UInt = Input(UInt(3.W))
    val rd1:        UInt = Input(UInt(dw.W))
    val rd2:        UInt = Input(UInt(dw.W))
    val take_branch:Bool = Input(Bool())

    val taken:      Bool = Output(Bool())
}

class BranchResolutionUnit extends Component
{
    val dataWidth: Int = config.ISA

    val io = IO(new BranchResolutionUnit_IO(dataWidth))

    io.taken := DontCare
    val check: Bool = Wire(Bool())
    check := MuxLookup(io.funct3, 0.U)(Seq(
        0.U ->  (io.rd1 === io.rd2),                // BEQ
        1.U ->  (io.rd1 =/= io.rd2),                // BNE
        4.U ->  (io.rd1.asSInt < io.rd2.asSInt),    // BLT
        5.U ->  (io.rd1.asSInt >= io.rd2.asSInt),   // BGE
        6.U ->  (io.rd1 < io.rd2),                  // BLTU
        7.U ->  (io.rd1 >= io.rd2)                  // BGEU
    ))

    io.taken := check & io.branch & io.take_branch
}