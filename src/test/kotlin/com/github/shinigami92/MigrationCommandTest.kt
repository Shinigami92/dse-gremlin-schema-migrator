package com.github.shinigami92

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.datastax.dse.driver.api.core.graph.DseGraph.g
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement
import com.datastax.oss.driver.api.core.CqlSession
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
import java.net.InetSocketAddress
import java.time.Duration

@QuarkusMainTest
@DisabledOnOs(MAC)
class MigrationCommandTest {
    companion object {
        lateinit var container: GenericContainer<*>

        lateinit var host: String
        var port: Int = 0

        lateinit var session: CqlSession

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
            port = container.getMappedPort(9042)

            println("DSE container started on $host:$port")

            session = CqlSession.builder()
                .addContactPoint(InetSocketAddress(host, port))
                .withLocalDatacenter("dc1")
                .build()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            println("Stopping DSE container")

            session.close()

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

        val graphResult = session.execute(FluentGraphStatement.newInstance(g.V().hasLabel("migration").count()).setGraphName("my_graph"))
        assertThat(graphResult.one()?.asLong()).isEqualTo(2L)
    }

    @Test
    fun testProvideAlternativeGraph(launcher: QuarkusMainLauncher) {
        val graphName = "alternative_graph"

        val result: LaunchResult = launcher.launch("--host=$host", "--port=$port", "-D=dc1", "-G=$graphName", "src/test/resources/migrations")

        assertThat(result.output).contains("Host $host, Port $port, DC dc1, Graph name $graphName, Migration directory src/test/resources/migrations")
        assertThat(result.output).contains("Found 2 migration files")

        val graphResult = session.execute(FluentGraphStatement.newInstance(g.V().hasLabel("migration").count()).setGraphName(graphName))
        assertThat(graphResult.one()?.asLong()).isEqualTo(2L)
    }
}
