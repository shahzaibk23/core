package core

import chisel3._
import chisel3.util._

import common.Component

import components.{SRamTopUno, SRamTopDuo}

class Top_IO(dw:Int) extends Bundle
{
    val pin: UInt = Output(UInt(dw.W))
}

class Top extends Component
{
    val dataWidth: Int = config.ISA

    val io = IO(new Top_IO(dataWidth))

    val IMEM = Module(new SRamTopUno(Some("/Users/shahzaibkashif/core/instrx.hex"))).io

    val DMEM = Module(new SRamTopDuo(None)).io

    val CORE = Module(new CoreTop).io

    IMEM.req <> CORE.imemReq
    IMEM.rsp <> CORE.imemRsp

    DMEM.req <> CORE.dmemReq
    DMEM.rsp <> CORE.dmemRsp

    io.pin <> CORE.pin

    CORE.stall := 0.B
}