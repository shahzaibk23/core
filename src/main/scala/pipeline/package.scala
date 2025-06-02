package object pipeline
{
  // Default implicit BaseConfig for all components
  implicit val defaultConfig: configs.BaseConfig = configs.BaseConfig() // Adjust ImemSize as needed
}