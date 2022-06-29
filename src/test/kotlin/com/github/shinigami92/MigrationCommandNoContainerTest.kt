package com.github.shinigami92

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import io.quarkus.test.junit.main.LaunchResult
import io.quarkus.test.junit.main.QuarkusMainLauncher
import io.quarkus.test.junit.main.QuarkusMainTest
import org.junit.jupiter.api.Test

@QuarkusMainTest
class MigrationCommandNoContainerTest {

    @Test
    fun `migration --help`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("--help")

        assertThat(result.exitCode()).isEqualTo(0)
        assertThat(result.output).contains(
            """Usage: migration [-hV] [-D=<localDatacenter>] [-G=<graphName>] [-H=<host>]
                 [-P=<port>] MIGRATION_DIRECTORY
Migrate schema for specified database
      MIGRATION_DIRECTORY   Migration directory
  -H, --host=<host>         Host/IP
                              Default: 172.17.0.2
  -P, --port=<port>         Port
                              Default: 9042
  -D, --dc, --local-datacenter=<localDatacenter>
                            DC
                              Default: dc1
  -G, --graph-name=<graphName>
                            Graph name
                              Default: my_graph
  -h, --help                Show this help message and exit.
  -V, --version             Print version information and exit."""
        )
    }

    @Test
    fun `migration --version`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("--version")

        assertThat(result.exitCode()).isEqualTo(0)
        // TODO @Shinigami92 2022-06-28: Add version to the output
    }

    @Test
    fun `migration`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch()

        assertThat(result.exitCode()).isEqualTo(2)
        assertThat(result.errorOutput).contains("Missing required parameter: 'MIGRATION_DIRECTORY'")
    }

    @Test
    fun `migration unknown`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("unknown")

        assertThat(result.exitCode()).isEqualTo(1)
        assertThat(result.errorOutput).contains("IllegalArgumentException: Migration directory unknown does not exist")
    }

    @Test
    fun `migration 001_schema_vertex_person`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("src/test/resources/migrations/001_schema_vertex_person.groovy")

        assertThat(result.exitCode()).isEqualTo(1)
        assertThat(result.errorOutput).contains("IllegalArgumentException: Migration directory src/test/resources/migrations/001_schema_vertex_person.groovy is not a directory")
    }

    @Test
    fun `migration dir`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("src/test/resources/migrations")

        assertThat(result.exitCode()).isEqualTo(1)
        assertThat(result.errorOutput).contains("Could not reach any contact point, make sure you've provided valid addresses")
    }

    @Test
    fun `migration -P=-1`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("src/test/resources/migrations", "-P=-1")

        assertThat(result.exitCode()).isEqualTo(1)
        assertThat(result.errorOutput).contains("Invalid port -1, must be between 0 and 65535")
    }

    @Test
    fun `migration --port=70000`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("src/test/resources/migrations", "--port=70000")

        assertThat(result.exitCode()).isEqualTo(1)
        assertThat(result.errorOutput).contains("Invalid port 70000, must be between 0 and 65535")
    }

    @Test
    fun `migration -G=my-graph`(launcher: QuarkusMainLauncher) {
        val result: LaunchResult = launcher.launch("src/test/resources/migrations", "-G=my-graph")

        assertThat(result.exitCode()).isEqualTo(1)
        assertThat(result.errorOutput).contains("Graph name must begin with an alpha-numeric character and can only contain alpha-numeric characters and underscores")
    }
}
