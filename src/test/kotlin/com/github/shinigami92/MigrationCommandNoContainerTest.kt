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
                 [-P=<port>] <migration folder>
Migrate schema for specified database
      <migration folder>   Migration folder
  -H, --host=<host>        Host/IP
                             Default: 172.17.0.2
  -P, --port=<port>        Port
                             Default: 9042
  -D, --dc, --local-datacenter=<localDatacenter>
                           DC
                             Default: dc1
  -G, --graph-name=<graphName>
                           Graph name
                             Default: my_graph
  -h, --help               Show this help message and exit.
  -V, --version            Print version information and exit."""
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
        assertThat(result.errorOutput).contains("Missing required parameter: '<migration folder>'")
    }
}
