package object core
{
  // Default implicit BaseConfig for all components
  implicit val defaultConfig: configs.TracerConfig = configs.SingleCoreWithTracer() // Adjust ImemSize as needed
}