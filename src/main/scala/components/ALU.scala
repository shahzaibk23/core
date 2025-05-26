package components

import chisel3._
import chisel3.util._

import common.Component
import ALUOps._

class ALU_IO(dw:Int) extends Bundle
{
    val input01 : UInt  =   Input(UInt(dw.W))
    val input02 : UInt  =   Input(UInt(dw.W))
    val aluCtrl : UInt  =   Input(UInt(5.W))

    val zero    : Bool  =   Output(Bool())      // Daya find out why is exists???
    val result  : UInt  =   Output(UInt(dw.W))
    // val adder_out       =   Output(UInt(dw.W))
    // val cmp_out = Output(Bool())
    // val dw              = Input(UInt(1.W))
}

class ALU extends Component
{
    val dataWidth: Int = config.ISA

    val io = IO(new ALU_IO(dataWidth))

    io.result := MuxLookup(io.aluCtrl, 0.U(dataWidth.W))(Seq(
            ADD   ->  (io.input01 + io.input02),
            SUB   ->  (io.input01 - io.input02),
            AND   ->  (io.input01 & io.input02),
            OR    ->  (io.input01 | io.input02),
            XOR   ->  (io.input01 ^ io.input02),
            SLT   ->  (io.input01 < io.input02),
            SLTU  ->  (io.input01.asSInt < io.input02.asSInt).asUInt,
            SLL   ->  (io.input01 << io.input02(5, 0)),
            SRL   ->  (io.input01 >> io.input02(5, 0)),
            SRA   ->  (io.input01.asSInt >> io.input02(5, 0)).asUInt,
            
            ADDW  ->  ((io.input01(31, 0) + io.input02(31, 0)).asSInt.pad(dataWidth).asUInt),
            SUBW  ->  ((io.input01(31, 0) - io.input02(31, 0)).asSInt.pad(dataWidth).asUInt),
            SLLW  ->  ((io.input01(31, 0) << io.input02(4, 0)).asSInt.pad(dataWidth).asUInt),
            SRLW  ->  ((io.input01(31, 0) >> io.input02(4, 0)).asSInt.pad(dataWidth).asUInt),
            SRAW  ->  ((io.input01(31, 0).asSInt >> io.input02(4, 0)).asSInt.pad(dataWidth).asUInt)
        )
    )

    // Replication of Rocket ALU

    // // ADD, SUB
    // val in02_inv        := Mux(isSub(io.aluCtrl), ~io.input02, io.input02)
    // val in01_xor_in02   := io.input01 ^ in2_inv
    // val in01_and_in02   := io.input01 & in2_inv
    // // io.

    // //SLT, SLTU
    // val slt =
    //     Mux(io.input01(xLen-1) === io.input02(xLen-1), io.adder_out(xLen-1),
    //         Mux(cmpUnsigned(io.aluCtrl), io.input02(xLen-1), io.input01(xLen-1))
    //     )
    // io.cmp_out          := cmpInverted(io.aluCtrl) ^ Mux(cmpEq(io.fn), in1_xor_in2 === 0.U, slt)

    // // SLL, SRL, SRA
    // val (shamt, shin_r) =
    //     if(xLen == 32) (io.input02(4, 0), io.input01)
    //     else 
    //     {
    //         require(xLen == 64)
    //         val shin_hi_32      = Fill(32, isSub(io.aluCtrl) && io.input01(31))
    //         val shin_hi         = Mux(io.dw === DW_64, io.input01(63, 62), shin_hi_32)
    //         val shamt           = Cat(io.input02(5) & (io.dw === DW_64), io.in2(4, 0))
    //         (shamt, Cat(shin_hi, io.input01(31, 0)))
    //     }
    // val shin    = Mux(shiftReverse(io.aluCtrl), Reverse(shin_r), shin_r)
    // val shout_r = (Cat(isSub(io.aluCtrl) & shin(xLen-1), shin).asSInt >> shamt)(xLen-1, 0)
    // val shout_l = Reverse(shout_r)
    // val shout   = Mux(io.aluCtrl === SRA || io.aluCtrl === SRL, shout_r, 0.U) |
    //               Mux(io.aluCtrl === SLL,                       shout_l, 0.U)

    // // CZEQZ, CZNEZ
    // // val in2_not_zero    = io.input02.orR
    // // val cond_out        = Option.when(usingConditionalZero)(
    // //     Mux((io.aluCtrl) === )
    // // ) This logic is for zicond extensions --- will finish later, when implemented

    // // AND, OR, XOR
    // val logic = Mux(io.aluCtrl === XOR || io.aluCtrl === OR,  in1_xor_in2, 0.U) |
    //             Mux(io.aluCtrl === OR  || io.aluCtrl === AND, in1_and_in2, 0.U)

    // // bit-manip also pending
    // val shift_logic         = (isCmp(io.aluCtrl) && slt) | logic | shout
    // val shift_logic_cond    = shift_logic // hard coded, since no zicond

    // val out = MuxLookup(io.aluCtrl, shift_logic_cond)(Seq(
    //     ADD -> io.adder_out,
    //     SUB -> 
    // ))

    io.zero := io.result === 0.U
}