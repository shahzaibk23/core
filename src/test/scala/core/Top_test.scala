package core

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._

import configs.BaseConfig

class Top_test extends AnyFreeSpec with Matchers with ChiselSim
{
    def verilatorWithWaves = HasSimulator.simulators
        .verilator(verilatorSettings =
            svsim.verilator.Backend.CompilationSettings(
                timing = Some(Backend.CompilationSettings.Timing.TimingDisabled),
                disabledWarnings = Seq("WIDTHTRUNC")
            )
        )

    implicit val vaerilator = verilatorWithWaves
    implicit val config: BaseConfig = BaseConfig()

    "Top under test" in
    {
        simulate(new Top)
        {
            dut =>
                dut.clock.step(50)
        }
    }
}