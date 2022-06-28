package com.github.shinigami92

import assertk.assertThat
import assertk.assertions.contains
import io.quarkus.test.junit.main.LaunchResult
import io.quarkus.test.junit.main.QuarkusMainLauncher
import io.quarkus.test.junit.main.QuarkusMainTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.MAC
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@QuarkusMainTest
@DisabledOnOs(MAC)
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
        val result: LaunchResult = launcher.launch("--host=$host", "--port=$port")

        assertThat(result.errorOutput).contains("Missing required parameter: 'MIGRATION_DIRECTORY'")
    }

    @Test
    fun testProvideMigrationDirectory(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("--host=$host", "--port=$port", "-D=dc1", "-G=my_graph", "src/test/resources/migrations")

        assertThat(result.output).contains("Host $host, Port $port, DC dc1, Graph name my_graph, Migration directory src/test/resources/migrations")
        assertThat(result.output).contains("Found 2 migration files")
    }
}
