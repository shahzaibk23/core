package pipeline

import common.Component
import components.{PC}

class InstructionFetchStage extends Component
{
    val io = IO(new IF_ID)

    // val PC = Module(new PC).io


}