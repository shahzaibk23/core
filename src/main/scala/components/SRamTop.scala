package components

import scala.io.Source
import java.io.PrintWriter

import chisel3._ 
import chisel3.util._

import common.{Component, MemRequestIO, MemResponseIO}

class SRamTop_IO(dw:Int) extends Bundle
{
    val req: DecoupledIO[MemRequestIO]   = Flipped(Decoupled(new MemRequestIO(dw)))
    val rsp: DecoupledIO[MemResponseIO]  = Decoupled(new MemResponseIO(dw))
}

class SRamTop(programFile: Option[String]) extends Component
{
    val dataWidth = config.ISA

    val io = IO(new SRamTop_IO(dataWidth))

    val validReg = RegInit(0.B)
    io.rsp.valid := validReg
    io.req.ready := 1.B          // ASSUMPTION: core always ready to accept req from mem

    val rData    = Wire(UInt(dataWidth.W))

    /* Preprocessing programFile to segregate to both SRAMs */
    // TODO: Shift to a separate util func
    // TODO: Configure for 32/64 both
    val fileInput = programFile match 
    {
        case Some(fileName) => Source.fromFile(fileName).getLines()
        case None => Iterator.empty
    }
    // val fileInput = Source.fromFile(programFile).getLines()
    val fileOut0  = new PrintWriter("sram_0.hex")
    val fileOut1  = new PrintWriter("sram_1.hex")

    for (line <- fileInput) {
        val clean = line.trim.toLowerCase
        require(clean.length == 16, s"Expected 16 hex chars per line, got: $line")

        fileOut0.println(clean.slice(8, 16)) // lower 4 bytes (last 8 hex chars)
        fileOut1.println(clean.slice(0, 8))  // upper 4 bytes (first 8 hex chars)
    }

    fileOut0.close()
    fileOut1.close()


    val sram_0    = Module(new sram_top(Some("sram_0.hex")))
    val sram_1    = Module(new sram_top(Some("sram_1.hex")))

    val clk       = WireInit(clock)
    val rst       = Wire(Bool())
    rst          := reset

    val commonIO = Wire(new SRAM_IO)
    commonIO.clk_i   := clk
    commonIO.rst_i   := rst
    commonIO.csb_i   := 1.B
    commonIO.we_i    := DontCare
    commonIO.wmask_i := DontCare
    commonIO.addr_i  := DontCare
    commonIO.wdata_i := DontCare

    sram_0.io <> commonIO
    sram_1.io <> commonIO

    rData := DontCare

    when(io.req.valid)
    {
        validReg := 1.B

        val wdataLo = io.req.bits.dataRequest(31,  0)
        val wdataHi = io.req.bits.dataRequest(63, 32)

        val wmaskLo = io.req.bits.activeByteLane(3,0)
        val wmaskHi = io.req.bits.activeByteLane(7,4)

        when(io.req.bits.isWrite)
        {
            sram_0.io.csb_i    := false.B
            sram_0.io.we_i     := false.B
            sram_0.io.addr_i   := io.req.bits.addrRequest
            sram_0.io.wdata_i  := wdataLo
            sram_0.io.wmask_i  := wmaskLo

            sram_1.io.csb_i    := false.B
            sram_1.io.we_i     := false.B
            sram_1.io.addr_i   := io.req.bits.addrRequest
            sram_1.io.wdata_i  := wdataHi
            sram_1.io.wmask_i  := wmaskHi

            rData := DontCare
        }
        .otherwise
        {
            sram_0.io.csb_i    := false.B
            sram_0.io.we_i     := true.B
            sram_0.io.addr_i   := io.req.bits.addrRequest

            sram_1.io.csb_i    := false.B
            sram_1.io.we_i     := true.B
            sram_1.io.addr_i   := io.req.bits.addrRequest

            // Concatenate both halves of read data
            rData := Cat(sram_1.io.rdata_o, sram_0.io.rdata_o)
        }
    }

    io.rsp.bits.dataResponse := rData
    io.rsp.bits.error        := 0.B         // ASSUMPTION: Never a error will come from mem
    // TODO: Error handeling if a error comes from mem rsp
}

class SRAM_IO extends Bundle
{
    val clk_i = Input(Clock())
    val rst_i = Input(Reset())
    val csb_i = Input(Bool())
    val we_i = Input(Bool())
    val wmask_i = Input(UInt(4.W))
    val addr_i = Input(UInt(13.W))
    val wdata_i = Input(UInt(32.W))
    val rdata_o = Output(UInt(32.W))
}

class sram_top(programFile:Option[String] ) extends BlackBox(
    Map("IFILE_IN" -> {if (programFile.isDefined) programFile.get else ""})
) with HasBlackBoxResource
{
    val io = IO(new SRAM_IO)
    addResource("/sram_top.v")
    addResource("/sram.v")
}