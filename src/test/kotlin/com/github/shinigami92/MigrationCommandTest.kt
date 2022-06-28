package com.github.shinigami92

import assertk.assertThat
import assertk.assertions.contains
import io.quarkus.test.junit.main.LaunchResult
import io.quarkus.test.junit.main.QuarkusMainLauncher
import io.quarkus.test.junit.main.QuarkusMainTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@QuarkusMainTest
class MigrationCommandTest {
    companion object {
        lateinit var container: GenericContainer<*>

        var host: String? = null
        var port: String? = null

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            println("Starting DSE test resource")

            container =
                GenericContainer(DockerImageName.parse("datastax/dse-server:6.8.24"))
                    .withCommand("-s -g")
                    .withStartupTimeout(Duration.ofMinutes(5))
                    .withEnv("DS_LICENSE", "accept")
                    .withExposedPorts(9042)

            println("Starting DSE container")

            container.start()

            host = container.host
            port = container.getMappedPort(9042).toString()

            println("DSE container started on $host:$port")
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            println("Stopping DSE container")

            container.stop()
        }
    }

    @Test
    fun testDefaultArguments(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch(host, port)

        val message1 = "Host $host, Port $port, DC dc1, Graph name my_graph"
        val message2 = "No migration folder specified"
        val output = result.output
        assertThat(output).contains(message1)
        assertThat(output).contains(message2)
    }

    @Test
    fun testProvideMigrationFolder(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch(host, port, "dc1", "my_graph", "src/test/resources/migrations")

        val message1 = "Host $host, Port $port, DC dc1, Graph name my_graph, Migration folder src/test/resources/migrations"
        val message2 = "Found 2 migration files"
        val output = result.output
        assertThat(output).contains(message1)
        assertThat(output).contains(message2)
    }
}
