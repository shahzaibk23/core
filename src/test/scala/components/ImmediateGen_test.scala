package components

import chisel3._ 
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._

import configs.BaseConfig

class ImmediateGen_test extends AnyFreeSpec with Matchers with ChiselSim
{
    implicit val config: BaseConfig = BaseConfig()

    "generate correct I-type immediate" in {
        simulate(new ImmediateGen) { c =>
            c.io.instruction.poke("h300001B".U)
            c.clock.step()
            c.io.out.expect(48.U)
        }
    }

    "generate correct S-type immediate" in {
        simulate(new ImmediateGen) { c =>
            c.io.instruction.poke("h6003023".U)
            c.clock.step()
            c.io.out.expect(96.U)
        }
    }

    "generate correct B-type immediate" in {
        simulate(new ImmediateGen) { c =>
            c.io.instruction.poke("h6005063".U)
            c.clock.step()
            c.io.out.expect(96.U)
        }
    }

    // "generate correct U-type immediate" in {
    //     simulate(new ImmediateGen) { c =>
    //         val imm = 0x000AB000
    //         val inst = imm | 0x37 // LUI with upper 20 bits of imm
    //         c.io.instruction.poke(inst.U)
    //         c.clock.step()
    //         c.io.out.expect(imm.U)
    //     }
    // }

    "generate correct J-type immediate" in {
        simulate(new ImmediateGen) { c =>
            c.io.instruction.poke("h600006F".U)
            c.clock.step()
            c.io.out.expect(96.U)
        }
    }
}