package components

import chisel3._
import chisel3.util.log2Ceil

import common.Component
import configs.BaseConfig

class PC_IO(pc_width:Int) extends Bundle
{
    val in      = Input(UInt(pc_width.W))
    val halt    = Input(Bool())
    val pc      = Output(UInt(pc_width.W))
    val pc4     = Output(UInt(pc_width.W))
}

class PC extends Component
{

    val pc_width = log2Ceil(config.ImemSize)

    val io = IO(new PC_IO(pc_width))

    val pc_reg = RegInit((-4).S(pc_width.W).asUInt)

    pc_reg      := io.in
    io.pc       := pc_reg
    io.pc4      := Mux(io.halt, pc_reg, pc_reg + 4.U)
    
}