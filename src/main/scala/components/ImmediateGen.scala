package components

import chisel3._
import chisel3.util._ 

import common.Component
import OpcodeTypes._

object OpcodeTypes
{
    val IType   = Seq(
        "b0000011".U,       // Load
        "b0001111".U,       // FENCE
        "b0010011".U,       // ALU-imm
        "b1100111".U,       // JALR
        "b1110011".U        // SYSTEM
    )

    val SType   = Seq(
        "b0100011".U        // Store
    )

    val BType  = Seq(
        "b1100011".U        // Branch
    )

    val UType   = Seq(
        "b0110111".U,       // LUI
        "b0010111".U        // AUIPC
    )

    val JType  = Seq(
        "b1101111".U        // JAL
    )
}

class ImmediateGen_IO(dw:Int) extends Bundle
{
    val instruction:    UInt    = Input(UInt(32.W))
    val out:            UInt    = Output(UInt(dw.W)) 
}

class ImmediateGen extends Component
{

    val dataWidth: Int = config.ISA

    val io = IO(new ImmediateGen_IO(dataWidth)) 

    val opcode: UInt    = io.instruction(6,0)
    val instr:  UInt    = io.instruction

    val immI:   UInt    = instr(31,20)
    val immS:   UInt    = Cat(instr(31, 25), instr(11, 7))
    val immSB:  UInt    = Cat(instr(31), instr(7), instr(30, 25), instr(11, 8))
    val immU:   UInt    = instr(31, 12)
    val immUJ:  UInt    = Cat(instr(31), instr(19, 12), instr(20), instr(30, 21))

    def signExtend(value: UInt, fromBits: Int): UInt =
            Cat(Fill(64 - fromBits, value(fromBits - 1)), value)
        

    io.out :=
        MuxCase(signExtend(immUJ ## 0.U(1.W), 21), Seq(
            (opcode isOneOf IType) -> signExtend(immI, 12),
            (opcode isOneOf SType) -> signExtend(immS, 12),
            (opcode isOneOf BType) -> signExtend(immSB ## 0.U(1.W), 13),
            (opcode isOneOf UType) -> Cat(immU, 0.U(12.W)),
            (opcode isOneOf JType) -> signExtend(immUJ ## 0.U(1.W), 21)
        ))

    
    implicit class UIntOps(u: UInt) {
        def isOneOf(options: Seq[UInt]): Bool = options.map(_ === u).reduce(_ || _)
    }
}