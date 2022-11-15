import org.testcontainers.containers.GenericContainer

internal class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
