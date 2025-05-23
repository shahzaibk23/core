package components

import chisel3._

import common.Component

class RF_IO(dw:Int) extends Bundle
{
    val readAddress:    Vec[UInt]   =   Input(Vec(2, UInt(5.W)))
    val writeEnable:    Bool        =   Input(Bool())
    val writeAddress:   UInt        =   Input(UInt(5.W))
    val writeData:      UInt        =   Input(UInt(dw.W))

    val readData:       Vec[UInt]   =   Output(Vec(2, UInt(dw.W)))
}

// TODO: backwards compatibility for rv32

class RegisterFile extends Component
{
    val dataWidth: Int = config.ISA

    val io      = IO(new RF_IO(dataWidth))

    val reg     = RegInit(VecInit(Seq.fill(32)(0.U(dataWidth.W))))

    when(io.writeEnable && io.writeAddress =/= 0.U)
    {
        reg(io.writeAddress) := io.writeData
    }

    io.readData := io.readAddress.map(addr => 
        Mux(addr === 0.U, 0.U, reg(addr))
    )
}
