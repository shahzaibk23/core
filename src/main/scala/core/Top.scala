package core

import chisel3._
import _root_.circt.stage.ChiselStage
import java.io.PrintWriter

import common.{Component, ComponentIO}

import components.{SRamTopUno, SRamTopDuo}
import uncore.{Tracer, TracerO}
import configs.TracerConfig

class Top_IO extends ComponentIO with SignatureDump
{
    val pin: UInt = Output(UInt(dw.W))

    // val rvfi: Option[TracerO] = if (config.hasTracer) Some(new TracerO(config.NRET, dw)) else None
    val rvfi: Option[TracerO] = config match {
        case tc: TracerConfig if config.hasTracer => Some(new TracerO(tc.NRET, dw))
        case _ => None
    }

}

class Top(programFile: Option[String], dataFile: Option[String]) extends Component
{
    val dataWidth: Int = config.ISA

    val io = IO(new Top_IO)

    val IMEM = Module(new SRamTopUno(programFile)).io

    val DMEM = Module(new SRamTopDuo(dataFile)).io

    val CORE = Module(new CoreTop).io

    IMEM.req <> CORE.imemReq
    IMEM.rsp <> CORE.imemRsp

    DMEM.req <> CORE.dmemReq
    DMEM.rsp <> CORE.dmemRsp

    io.pin <> CORE.pin

    CORE.stall := 0.B

    CORE.dccm_we    <> io.dccm_we
    CORE.dccm_addr  <> io.dccm_addr
    CORE.dccm_data  <> io.dccm_data

    if (config.hasTracer)
    {
        val TRACER = Module(new Tracer)
        TRACER.rvfi_i <> CORE.rvfi.get
        io.rvfi.get <> TRACER.rvfi_o
    }
}

object CoreDriver
{
    def main(args: Array[String]): Unit =
    {
        val IMem =  if (args.length > 0) args(0) else "program.hex"
        val DMem =  if (args.length > 1) args(1) else "dmem.hex"
        // val sv = ChiselStage.emitSystemVerilog(
        //     new Top(Some(IMem), Some(DMem)),
        //     firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable"),
        // )
        // val writer = new PrintWriter("Top.sv")
        // writer.write(sv)
        // writer.close()

        ChiselStage.emitSystemVerilogFile(
            new Top(Some(IMem), Some(DMem)),
            firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable"),
            args=args
        )
        
        println("SystemVerilog generated: Top.sv")
    }
}