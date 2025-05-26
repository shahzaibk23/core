package components

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import svsim.verilator._
import chisel3.simulator._
import scala.util.Random

import configs.BaseConfig
import ALUOps._

class ALU_test extends AnyFreeSpec with Matchers with ChiselSim
{
    implicit val config: BaseConfig = BaseConfig()

    "ALU Randomized Test" in
    {
        simulate(new ALU)
        {
            dut =>
                val array_op = Seq(
                    ADD, ADDW,
                    SUB, SUBW,
                    SLL, //SLLW,
                    SRL, //SRLW,
                    SRA, //SRAW,
                    AND, OR, XOR,
                    SLT, SLTU
                )

                for (i <- 0 to 1000) {
                    val srcA    = BigInt(Random.nextLong() & 0xFFFFFFFFL)
                    val srcB    = BigInt(Random.nextLong() & 0xFFFFFFFFL)
                    val opIndex = Random.nextInt(array_op.length)
                    val aluOp   = array_op(opIndex)

                    val result: BigInt = aluOp match {
                        case ADD    => srcA + srcB 
                        case SUB    => srcA - srcB & ((BigInt(1) << 64) - 1)
                        case AND    => srcA & srcB
                        case OR     => srcA | srcB
                        case XOR    => srcA ^ srcB
                        case SLT    => if (srcA < srcB) BigInt(1) else BigInt(0)
                        case SLTU   => if((srcA >> 0) < (srcB >> 0)) BigInt(1) else BigInt(0)
                        case SLL    => (srcA <<  (srcB & BigInt(0x3F)).toInt) & ((BigInt(1) << 64) - 1)
                        case SRL    => (srcA >>  (srcB & BigInt(0x3F)).toInt) & ((BigInt(1) << 64) - 1)
                        case SRA    => (srcA >>  (srcB & BigInt(0x3F)).toInt) & ((BigInt(1) << 64) - 1)

                        // case SRL    => srcA >>> (srcB & 0x3F)
                        // case SRA    => srcA >>  (srcB & 0x3F)

                        // case ADDW   => ((srcA.toInt + srcB.toInt).toInt).toLong
                        case ADDW =>    BigInt(((srcA.toInt + srcB.toInt).toInt)) & ((BigInt(1) << 64) - 1)
                        case SUBW =>    BigInt(((srcA.toInt - srcB.toInt).toInt)) & ((BigInt(1) << 64) - 1)
                        case SLLW =>    (srcA <<  (srcB & BigInt(0x1F)).toInt) & ((BigInt(1) << 64) - 1)
                        case SRLW =>    (srcA >>  (srcB & BigInt(0x1F)).toInt) & ((BigInt(1) << 64) - 1)
                        // case SUBW   => ((srcA.toInt - srcB.toInt).toInt).toLong
                        // case SLLW   => (srcA.toInt << srcB.toInt & 0x1F).toLong//
                        // case SRLW   => (srcA.toInt >>> srcB.toInt & 0x1F).toLong
                        // case SRAW   => (srcA.toInt >> srcB.toInt & 0x1F).toLong

                        // case _      => 0L
                    }

                    // println(p"result: ${result}")
                    

                    // val expectedV: BigInt = result

                    // // println(s"inputA: ${srcA}")
                    // // println(s"inputB: ${srcB}")

                    // dut.io.input01.poke(srcA.U)
                    // dut.io.input02.poke(srcB.U)
                    // dut.io.aluCtrl.poke(aluOp)
                    // dut.clock.step()
                    // val dutResult = dut.io.result.peek().litValue
                    // println(s"inputA     : $srcA")
                    // println(s"inputB     : $srcB")
                    // println(f"expected   : 0x$expectedV%X")
                    // println(f"dut result : 0x$dutResult%X")
                    // // dut.io.result.expect(expectedV.U)

                    // val result: BigInt = computeExpected(srcA, srcB, aluOp)  // however you compute it
                    val expectedV: BigInt = result

                    println(s"inputA     : $srcA")
                    println(s"inputB     : $srcB")
                    println(f"expected   : 0x$expectedV%X")

                    dut.io.input01.poke(srcA.U)
                    dut.io.input02.poke(srcB.U)
                    dut.io.aluCtrl.poke(aluOp)
                    dut.clock.step()

                    val dutResult = dut.io.result.peek().litValue
                    println(f"dut result   : 0x$dutResult%X")
                    dut.io.result.expect(expectedV.U(64.W))

                }
        }
    }

//     def toUInt(x: Long, width: Int): UInt = {
//   BigInt(x & ((1L << width) - 1)).U(width.W)
// }



}