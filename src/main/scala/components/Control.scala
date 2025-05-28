package components

import chisel3._
import chisel3.util._

import common.Component
import InstructionTypes._ 

object InstructionTypes
{
    val RType   = BitPat("b?????????????????????????0110011")
    val IType   = BitPat("b?????????????????????????0010011")
    val Load    = BitPat("b?????????????????????????0000011")
    val Store   = BitPat("b?????????????????????????0100011")
    val SBType  = BitPat("b?????????????????????????1100011")
    val LUI     = BitPat("b?????????????????????????0110111")
    val AUIPC   = BitPat("b?????????????????????????0010111")
    val JAL     = BitPat("b?????????????????????????1101111")
    val JALR    = BitPat("b?????????????????????????1100111")
    val CSR     = BitPat("b?????????????????????????1110011")   // UNUSED for now, re-check this later
}

object JumpType extends ChiselEnum
{
    val none, jal, jalr = Value
}

object  MemToReg extends ChiselEnum
{
    val alu, load, pc   = Value
}

object AluSrc1 extends ChiselEnum
{
    val rs1, pc, zero   = Value
}

trait ControlSignals_ALU extends Bundle
{
    val aluSrc:     Bool                = Bool()
    val aluOp:      UInt                = UInt(2.W)
    val aluSrc1:    AluSrc1.Type        = AluSrc1.Type()
}

class ControlSignals_MEM_WB extends Bundle
{
    val memToReg:   MemToReg.Type       = MemToReg.Type()
    val regWrite:   Bool                = Bool()
}

class ControlSignals_EX_MEM extends ControlSignals_MEM_WB
{
    val memRead:    Bool                = Bool()
    val memWrite:   Bool                = Bool()
    val branch:     Bool                = Bool()
}

class ControlSignals extends ControlSignals_EX_MEM with ControlSignals_ALU{
    val jump:       JumpType.Type       = JumpType.Type()
}

class Control_IO extends Bundle{
    val in:         UInt            = Input(UInt(32.W))
    val out:        ControlSignals  = Output(new ControlSignals)
}

class Control extends Component
{
    val io = IO(new Control_IO)

    val defaultSignals   = Wire(new ControlSignals)
    defaultSignals      := 0.U.asTypeOf(new ControlSignals)

    val controlTable     = Array(
        RType      -> ControlSignalsLit(
            aluSrc      = 1.B,
            memToReg    = MemToReg.alu,
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.rs1         
        ),
        IType      -> ControlSignalsLit(
            aluSrc      = 0.B,
            memToReg    = MemToReg.alu,
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.rs1   
        ),
        InstructionTypes.Load       -> ControlSignalsLit(
            aluSrc      = 0.B,
            memToReg    = MemToReg.load,
            regWrite    = 1.B,
            memRead     = 1.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.rs1        
        ),
        Store      -> ControlSignalsLit(
            aluSrc      = 0.B,
            memToReg    = MemToReg.alu,
            regWrite    = 0.B,
            memRead     = 0.B,
            memWrite    = 1.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.rs1     
        ),
        SBType      -> ControlSignalsLit(
            aluSrc      = 0.B,
            memToReg    = MemToReg.alu,
            regWrite    = 0.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 1.B,
            jump        = JumpType.none,
            aluOp       = "b01".U,
            aluSrc1     = AluSrc1.rs1        
        ),
        LUI         -> ControlSignalsLit(
            aluSrc      = 1.B,
            memToReg    = MemToReg.alu,
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.zero        
        ),
        AUIPC      -> ControlSignalsLit(
            aluSrc      = 1.B,
            memToReg    = MemToReg.alu,
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.pc        
        ),
        JAL        -> ControlSignalsLit(
            aluSrc      = 0.B,
            memToReg    = MemToReg.alu,         // TODO: lookback and verify if its correcty
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.jal,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.rs1         
        ),
        JALR       -> ControlSignalsLit(
            aluSrc      = 1.B,
            memToReg    = MemToReg.pc,
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.jalr,
            aluOp       = "b00".U,
            aluSrc1     = AluSrc1.rs1        
        ),
        CSR        -> ControlSignalsLit(
            aluSrc      = 1.B,
            memToReg    = MemToReg.alu,
            regWrite    = 1.B,
            memRead     = 0.B,
            memWrite    = 0.B,
            branch      = 0.B,
            jump        = JumpType.none,
            aluOp       = "b10".U,
            aluSrc1     = AluSrc1.rs1       
        )
    )

    val decoded = ListLookup(io.in, defaultSignals.litAsList, controlTable.map { case (k, v) => k -> v.litAsList })

    io.out     := ControlSignals.fromList(decoded)

    def ControlSignalsLit
    (
        aluSrc:     Bool,
        memToReg:   MemToReg.Type,
        regWrite:   Bool,
        memRead:    Bool,
        memWrite:   Bool,
        branch:     Bool,
        jump:       JumpType.Type,
        aluOp:      UInt,
        aluSrc1:    AluSrc1.Type
    ): ControlSignals = 
    {
        val sig       = Wire(new ControlSignals)
        sig.aluSrc   := aluSrc
        sig.memToReg := memToReg
        sig.regWrite := regWrite
        sig.memRead  := memRead
        sig.memWrite := memWrite
        sig.branch   := branch
        sig.jump     := jump
        sig.aluOp    := aluOp
        sig.aluSrc1  := aluSrc1
        sig
    }

    // Convert Bundle to List and back
    implicit class ControlSignalsOps(val cs: ControlSignals) 
    {
        def litAsList: List[UInt] = List(
            cs.aluSrc,
            cs.memToReg.asUInt,
            cs.regWrite,
            cs.memRead,
            cs.memWrite,
            cs.branch,
            cs.jump.asUInt,
            cs.aluOp,
            cs.aluSrc1.asUInt
        )
    }

    object ControlSignals
    {
        def fromList(l: List[UInt]): ControlSignals =
        {
            val sig       = Wire(new ControlSignals)
            sig.aluSrc   := l(0).asBool
            sig.memToReg := MemToReg(l(1))
            sig.regWrite := l(2).asBool
            sig.memRead  := l(3).asBool
            sig.memWrite := l(4).asBool
            sig.branch   := l(5).asBool
            sig.jump     := JumpType(l(6))
            sig.aluOp    := l(7)
            sig.aluSrc1  := AluSrc1(l(8))
            sig
        }
    }
}
// HMMM!! I know the structure is quite confusing
// It is to me as well, the moment i am done coding.
// Will do some commenting and documentation.... someday
// SOMEDAY! Not Today, Not tomrrow!! BUT, SOME DAYY