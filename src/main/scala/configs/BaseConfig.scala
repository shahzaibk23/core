package configs

case class BaseConfig
(
    val ISA     : Int = 64,
    val ImemSize: Int = 1024 // 1KB -- in bytes
)