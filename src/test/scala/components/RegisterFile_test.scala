package components

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._ 
import chisel3.simulator._

import configs.BaseConfig

class RegisterFile_test extends AnyFreeSpec with Matchers with ChiselSim
{
    implicit val config: BaseConfig = BaseConfig()
    
    val dw = config.ISA

    "write and read registers correctly" in
    {
        simulate(new RegisterFile)
        {
            dut =>
                def writeReg(addr: Int, data:BigInt): Unit = {
                    println(s"Writing ${data} to reg ${addr}")
                    dut.io.writeEnable.poke(1.B)
                    dut.io.writeAddress.poke(addr.U)
                    dut.io.writeData.poke(data.U)
                    dut.clock.step()
                    dut.io.writeEnable.poke(0.B)
                }

                def readRegs(addr1:Int, addr2:Int): (BigInt, BigInt) = {
                    println(s"Reading from ${addr1} and ${addr2}")
                    dut.io.readAddress(0).poke(addr1.U)
                    dut.io.readAddress(1).poke(addr2.U)
                    dut.clock.step()
                    (
                        dut.io.readData(0).peek().litValue,
                        dut.io.readData(1).peek().litValue
                    )
                }

                // Test: Write and Read from Register 01
                writeReg(1, 0x12345678ABCDEFL)
                val (rd1, _) = readRegs(1, 0)
                assert(rd1 == 0x12345678ABCDEFL, s"Expected 0x12345678ABCDEFL, got ${rd1.toString(16)}")

                // Test: Write and Read from Register 31
                writeReg(31, 0xDEADBEEFCAFEL)
                val (_, rd31) = readRegs(0, 31)
                assert(rd31 == 0xDEADBEEFCAFEL, s"Expected 0xDEADBEEFCAFEL, got ${rd31.toString(16)}")

                // Test: x0 is always zero
                writeReg(0, 0x0FFFFFFFFFFFFFFFL)
                val (rd0, _) = readRegs(0, 1)
                assert(rd0 == 0, s"Expected 0xFFFFFFFFFFFFFFFFL, got ${rd0.toString(16)}")

                // Test: Multiple Write and Reads
                for (i <- 2 to 31){
                    writeReg(i, i*42)
                }
                for (i <- 2 to 31 by 2){
                    val (a,b) = readRegs(i, i+1)
                    assert(a == i*42, s"Unexpected value in reg $i")
                    assert(b == (i+1)*42, s"Unexpected value in reg ${i+1}")
                }

        }
    }
}