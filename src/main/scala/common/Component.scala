package common

import chisel3._
import configs.CoreConfig

abstract class Component(implicit val config:CoreConfig) extends Module
abstract class ComponentIO(implicit val config: CoreConfig) extends Bundle
{
    val dw:     Int = config.XLEN
    val mw:     Int = config.ImemSize
}