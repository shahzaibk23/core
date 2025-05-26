package components

import chisel3._
import chisel3.util._

import common.Component
import ALUOps._ 

class ALUControl_IO extends Bundle
{
    val aluOp:  UInt    = Input(UInt(2.W))              // Main ALUOp (from decoder)
    val f7:     UInt    = Input(UInt(7.W))              // funct7
    val f3:     UInt    = Input(UInt(3.W))              // funct3
    val isWord: Bool    = Input(Bool())                 // true for 32-bit ops (e.g. ADDW, SUBW)
    val out:    UInt    = Output(UInt(5.W))  // extended to 5 bits for more operations
}

class ALUControl extends Component
{
    val io = IO(new ALUControl_IO)

    io.out := INVALID

    // Maps for funct3 + funct7 combinations
    val funcMap: Map[(UInt, UInt, Bool), UInt] = 
        Map[(UInt, UInt, Bool), UInt](
            // (f3, f7, isWord) -> ALU_OP
            ("b000".U, "b0000000".U, 0.B)   -> ADD,
            ("b000".U, "b0100000".U, 0.B)   -> SUB,
            ("b000".U, "b0000000".U, 1.B )  -> ADDW,
            ("b000".U, "b0100000".U, 1.B )  -> SUBW,

            ("b001".U, "b0000000".U, 0.B)   -> SLL,
            ("b001".U, "b0000000".U, 1.B)   -> SLLW,

            ("b101".U, "b0000000".U, 0.B)   -> SRL,
            ("b101".U, "b0100000".U, 0.B)   -> SRA,
            ("b101".U, "b0000000".U, 1.B)   -> SRLW,
            ("b101".U, "b0100000".U, 1.B)   -> SRAW,

            ("b010".U, "b0000000".U, 0.B)   -> SLT,
            ("b011".U, "b0000000".U, 0.B)   -> SLTU,
            ("b100".U, "b0000000".U, 0.B)   -> XOR,
            ("b110".U, "b0000000".U, 0.B)   -> OR,
            ("b111".U, "b0000000".U, 0.B)   -> AND
        )

    when(io.aluOp === ALUOP_ADD)
    {
        io.out  :=  Mux(io.isWord, ADDW, ADD)
    }
    .elsewhen(io.aluOp === ALUOP_SUB)
    {
        io.out  :=  Mux(io.isWord, SUBW, SUB)
    }
    .elsewhen(io.aluOp === ALUOP_FUNC)
    {
        // io.out  :=  funcMap.collectFirst{
        //     case ((f3, f7, word), aluOut)
        //         when(f3 === io.f3 && (f7 === io.f7 || f7 === "b0000000".U) && word === io.isWord)
        //         {

        //         }
                    
        // }.getOrElse(INVALID)

        io.out := INVALID
            for (((f3Key, f7Key, isWordKey), aluOut) <- funcMap) {
                when(io.aluOp === ALUOP_FUNC && io.f3 === f3Key && io.f7 === f7Key && io.isWord === isWordKey) {
                    io.out := aluOut
                }
            }
    }
}