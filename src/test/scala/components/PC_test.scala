package components

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._

import configs.BaseConfig

class PC_test extends AnyFreeSpec with Matchers with ChiselSim
{

    def verilatorWithWaves = HasSimulator.simulators
      .verilator(verilatorSettings =
        svsim.verilator.Backend.CompilationSettings(
          timing = Some(Backend.CompilationSettings.Timing.TimingDisabled),
        )
      )

    implicit val vaerilator = verilatorWithWaves
    implicit val config: BaseConfig = BaseConfig()
    "PC under test" in 
    {
        simulate(new PC)
        {   dut =>
                dut.io.in.poke(0.U)
                dut.io.halt.poke(0.B)
                dut.clock.step(2)
                dut.io.in.poke(8.U)
                dut.clock.step(2)
        }
    }
}