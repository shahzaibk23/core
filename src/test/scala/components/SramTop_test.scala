package components

import chisel3._ 
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._

import configs.BaseConfig

class SRamTop_test extends AnyFreeSpec with Matchers with ChiselSim
{
    def verilatorWithWaves = HasSimulator.simulators
      .verilator(verilatorSettings =
        svsim.verilator.Backend.CompilationSettings(
          timing = Some(Backend.CompilationSettings.Timing.TimingDisabled),
          disabledWarnings = Seq("WIDTHTRUNC")
        )
      )

    implicit val vaerilator = verilatorWithWaves
    implicit val config: BaseConfig  = BaseConfig()
    "SRamTop under test" in
    {
        val settings = Backend.CompilationSettings(
            timing = Some(Backend.CompilationSettings.Timing.TimingDisabled),
        )
        simulate(new SRamTopDuo(Some("/Users/shahzaibkashif/core/instrx.hex")))
        {
            dut =>
                dut.io.req.valid.poke(1.B)
                dut.io.req.bits.dataRequest.poke(0.U)
                dut.io.req.bits.addrRequest.poke(1.U)
                dut.io.req.bits.activeByteLane.poke("b1111".U)
                dut.io.req.bits.isWrite.poke(0.B)
                dut.clock.step(3)
                dut.io.req.bits.addrRequest.poke(0.U)
                dut.clock.step(3)
                dut.io.req.bits.addrRequest.poke(2.U)
                dut.clock.step(3)
                
        }
    }
}