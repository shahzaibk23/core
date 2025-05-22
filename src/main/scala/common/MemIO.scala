package common

import chisel3._

/*
 * This abstract class provides a template for other protocols to implement the transaction wires.
 * This is used as a template for e.g when the core wants to communicate with the memory or with the peripheral registers.
 * It will set these signals up in order to talk to the Host adapter of the relevant bus protocol
 */

class MemRequestIO(dw:Int) extends Bundle
{
    val addrRequest:    UInt    = Input(UInt(dw.W))
    val dataRequest:    UInt    = Input(UInt(dw.W))
    val activeByteLane: UInt    = Input(UInt((dw/8).W))
    val isWrite:        Bool    = Input(Bool())
}

class MemResponseIO(dw:Int) extends Bundle
{
    val dataResponse:   UInt    = Input(UInt(dw.W))
    val error:          Bool    = Input(Bool())
}