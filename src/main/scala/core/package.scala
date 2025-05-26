package object core
{
  // Default implicit BaseConfig for all components
  implicit val defaultConfig: configs.BaseConfig = configs.BaseConfig(ImemSize = 1024) // Adjust ImemSize as needed
}