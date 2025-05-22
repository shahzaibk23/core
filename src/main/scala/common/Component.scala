package common

import chisel3._
import configs.BaseConfig

abstract class Component(implicit val config:BaseConfig) extends Module