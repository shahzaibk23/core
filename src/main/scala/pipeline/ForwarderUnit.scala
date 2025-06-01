package pipeline

import chisel3._
import chisel3.util._

import common.{Component, Utils}

class RegConstruct(dw: Int) extends Bundle
{
    val value:  UInt    =   UInt(dw.W)
    val addr:   UInt    =   UInt(5.W)
}

class ForwarderUnit_IO(dw: Int) extends Bundle
{
    val currentRs1: UInt    =   Input(UInt(dw.W))
    val currentRs2: UInt    =   Input(UInt(dw.W))

    val ID_EX_rd:   RegConstruct    =   Input(new RegConstruct(dw))
    val EX_MEM_rd:  RegConstruct    =   Input(new RegConstruct(dw))

    val finalRs1:   ValidIO[UInt]   =   Valid(UInt(dw.W))
    val finalRs2:   ValidIO[UInt]   =   Valid(UInt(dw.W))
}

class ForwarderUnit extends Component
{
    val dataWidth: Int = config.ISA

    val io = IO(new ForwarderUnit_IO(dataWidth))

    io.finalRs1 := forward(io.currentRs1, io.ID_EX_rd, io.EX_MEM_rd) //0.U.asTypeOf(io.finalRs1)
    io.finalRs2 := forward(io.currentRs2, io.ID_EX_rd, io.EX_MEM_rd) //0.U.asTypeOf(io.finalRs2)

    
    def forward(
        current: UInt,
        ex_rd:   RegConstruct,
        mem_rd:  RegConstruct
    ): ValidIO[UInt] =
    {
        val out = Wire(Valid(UInt(dataWidth.W)))
        out.bits := Mux(
            current === ex_rd.addr && ~Utils.isZero(current),
            ex_rd.value,
            Mux(
                current === mem_rd.addr && ~Utils.isZero(current),
                mem_rd.value,
                0.U
            )
        )
        out.valid := (current === ex_rd.addr || current === mem_rd.addr) && current =/= 0.U
        out
    }

}