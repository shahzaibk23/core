package pipeline

import chisel3._ 
import chisel3.util._

import common.Component
import components.JumpType
import components.JumpType._ 
// TODO: shift to Core
class HazardDetectionUnit_IO extends Bundle
{
    val ID_EX_memRead:  Bool    =   Input(Bool())
    val EX_MEM_memRead: Bool    =   Input(Bool())
    val ID_EX_branch:   Bool    =   Input(Bool())
    val ID_EX_rd:       UInt    =   Input(UInt(5.W))
    val EX_MEM_rd:      UInt    =   Input(UInt(5.W))
    val ID_rs1:         UInt    =   Input(UInt(5.W))
    val ID_rs2:         UInt    =   Input(UInt(5.W))
    val dmemRespValid:  Bool    =   Input(Bool())
    val branchTaken:    Bool    =   Input(Bool())
    val jump:           JumpType.Type    =   Input(JumpType.Type())
    val branch:         Bool    =   Input(Bool())
    
    val IF_regWrite:    Bool    =   Output(Bool())
    val pcWrite:        Bool    =   Output(Bool())
    val ctrlMux:        Bool    =   Output(Bool())
    val IF_ID_flush:    Bool    =   Output(Bool())
    val takeBranch:     Bool    =   Output(Bool())
}

class HazardDetectionUnit extends Component
{
    val io = IO(new HazardDetectionUnit_IO)

    io.ctrlMux      := 1.B
    io.pcWrite      := 1.B
    io.IF_regWrite  := 1.B
    io.takeBranch   := 1.B
    io.IF_ID_flush  := 0.B

    // Load Use Hazard
    when(
        (io.ID_EX_memRead || io.branch)                             &&
        (io.ID_EX_rd === io.ID_rs1 || io.ID_EX_rd === io.ID_rs2)    &&
        ((io.ID_EX_rd =/= 0.U && io.ID_rs1 =/= 0.U)                 ||  // Arghhh why didn't I create a helper funct for isZero!!!
         (io.ID_EX_rd =/= 0.U && io.ID_rs2 =/= 0.U))                &&
        !io.ID_EX_branch
    )
    {
        io.ctrlMux      := 0.B
        io.pcWrite      := 0.B
        io.IF_regWrite  := 0.B
        io.takeBranch   := 0.B
    }

    when(
        io.EX_MEM_memRead               &&
        io.branch                       &&
        (io.EX_MEM_rd === io.ID_rs1     ||
         io.EX_MEM_rd === io.ID_rs2)
    )
    {
        io.ctrlMux      := 0.B
        io.pcWrite      := 0.B
        io.IF_regWrite  := 0.B
        io.takeBranch   := 0.B
    }

    // Branch Hazard
    when(io.branchTaken || io.jump =/= none)
    {
        io.IF_ID_flush  := 1.B
    }
    .otherwise
    {
        io.IF_ID_flush  := 0.B
    }
}