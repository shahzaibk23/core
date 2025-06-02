package core

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._

import configs.{CoreConfig, SingleCoreWithTracer}

class TopTracer_test extends AnyFreeSpec with Matchers with ChiselSim
{
    def verilatorWithWaves = HasSimulator.simulators
        .verilator(verilatorSettings =
            svsim.verilator.Backend.CompilationSettings(
                timing = Some(Backend.CompilationSettings.Timing.TimingDisabled),
                disabledWarnings = Seq("WIDTHTRUNC")
            )
        )

    implicit val vaerilator = verilatorWithWaves
    implicit val config: CoreConfig = SingleCoreWithTracer()

    "Top under test" in
    {
        simulate(new Top)
        {
            dut =>
                // dut.clock.setTimeout(1000)
                dut.reset.poke(true.B)
                dut.clock.step(5)
                dut.reset.poke(false.B)
                val MAX_SIM_TIME = 100000

                for (cycle <- 0 until MAX_SIM_TIME) {
                    // Assuming io.rvfi is defined as Vec[NRET] or Option[TracerO] — adapt accordingly
                    val rvfi = dut.io.rvfi.get  // Remove `.get` if not Option

                    val valid = rvfi.valid(0).peek().litToBoolean
                    val wmask = rvfi.mem_wmask(0).peek().litValue
                    val addr  = rvfi.mem_addr(0).peek().litValue
                    val data  = rvfi.mem_wdata(0).peek().litValue

                    if (valid && wmask == 15) {
                        if (addr == 0x8004) {
                            println(f"[${cycle}] Signature: 0x$data%08x")
                        } else if (addr == 0x8008 && data == 0xCAFECAFE) {
                            println(f"[${cycle}] ✅ Test PASSED — terminating simulation")
                            
                        }
                    }

                    dut.clock.step()
                }
        }
    }
}