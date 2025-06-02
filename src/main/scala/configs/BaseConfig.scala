package configs

trait CoreConfig
{
    val ISA     : Int = 64
    val ImemSize: Int = 1024 // 1KB -- in bytes
    val XLEN:     Int = ISA

    val hasTracer: Boolean = false
}

//TODO: at some point if needed convert this to a trait//