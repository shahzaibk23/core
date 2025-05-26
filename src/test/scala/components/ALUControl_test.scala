package components

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._

import configs.BaseConfig
import ALUOps._

class ALUControl_test extends AnyFreeSpec with Matchers with ChiselSim
{
    implicit val config: BaseConfig = BaseConfig()

    "produce correct ALU outputs for basic ALUOp cases" in {
        simulate(new ALUControl) { c =>

        // Test ADD vs ADDW
        c.io.aluOp.poke(ALUOP_ADD)
        c.io.isWord.poke(false.B)
        c.io.f3.poke(0.U)
        c.io.f7.poke(0.U)
        c.clock.step()
        c.io.out.expect(ADD)

        c.io.isWord.poke(true.B)
        c.clock.step()
        c.io.out.expect(ADDW)

        // Test SUB vs SUBW
        c.io.aluOp.poke(ALUOP_SUB)
        c.io.isWord.poke(false.B)
        c.io.f7.poke("b0100000".U)
        c.clock.step()
        c.io.out.expect(SUB)

        c.io.isWord.poke(true.B)
        c.clock.step()
        c.io.out.expect(SUBW)
        }
    }

    "produce correct ALU outputs for FUNC ALUOp cases" in {
        simulate(new ALUControl) { c =>
        // ADD
        c.io.aluOp.poke(ALUOP_FUNC)
        c.io.f3.poke("b000".U)
        c.io.f7.poke("b0000000".U)
        c.io.isWord.poke(false.B)
        c.clock.step()
        c.io.out.expect(ADD)

        // SLLW
        c.io.f3.poke("b001".U)
        c.io.isWord.poke(true.B)
        c.clock.step()
        c.io.out.expect(SLLW)

        // XOR
        c.io.f3.poke("b100".U)
        c.io.isWord.poke(false.B)
        c.clock.step()
        c.io.out.expect(XOR)// XOR
        c.io.f3.poke("b100".U)
        c.io.isWord.poke(false.B)
        c.clock.step()
        c.io.out.expect(XOR)

        // SUBW
        c.io.f3.poke("b000".U)
        c.io.f7.poke("b0100000".U)
        c.io.isWord.poke(true.B)
        c.clock.step()
        c.io.out.expect(SUBW)

        // SRA
        c.io.f3.poke("b101".U)
        c.io.f7.poke("b0100000".U)
        c.io.isWord.poke(false.B)
        c.clock.step()
        c.io.out.expect(SRA)

        // SRAW
        c.io.isWord.poke(true.B)
        c.clock.step()
        c.io.out.expect(SRAW)

        // AND
        c.io.f3.poke("b111".U)
        c.io.f7.poke("b0000000".U)
        c.io.isWord.poke(false.B)
        c.clock.step()
        c.io.out.expect(AND)
        }
    }
}