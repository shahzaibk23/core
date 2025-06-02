package configs

trait TracerConfig extends CoreConfig
{
    val NRET:   Int = 1
    override val hasTracer: Boolean = true
}
