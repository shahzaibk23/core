package core

import chisel3._
import chisel3.util._

import common.{Component, ComponentIO}

import components.{SRamTopUno, SRamTopDuo}
import uncore.{Tracer, TracerO}
import configs.TracerConfig

class Top_IO extends ComponentIO
{
    val pin: UInt = Output(UInt(dw.W))

    // val rvfi: Option[TracerO] = if (config.hasTracer) Some(new TracerO(config.NRET, dw)) else None
    val rvfi: Option[TracerO] = config match {
        case tc: TracerConfig if config.hasTracer => Some(new TracerO(tc.NRET, dw))
        case _ => None
    }

}

class Top extends Component
{
    val dataWidth: Int = config.ISA

    val io = IO(new Top_IO)

    val IMEM = Module(new SRamTopUno(Some("/Users/shahzaibkashif/core/instrx.hex"))).io

    val DMEM = Module(new SRamTopDuo(None)).io

    val CORE = Module(new CoreTop).io

    IMEM.req <> CORE.imemReq
    IMEM.rsp <> CORE.imemRsp

    DMEM.req <> CORE.dmemReq
    DMEM.rsp <> CORE.dmemRsp

    io.pin <> CORE.pin

    CORE.stall := 0.B

    if (config.hasTracer)
    {
        val TRACER = Module(new Tracer)
        TRACER.rvfi_i <> CORE.rvfi.get
        io.rvfi.get <> TRACER.rvfi_o
    }
}