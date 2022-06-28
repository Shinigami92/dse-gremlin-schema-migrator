package com.github.shinigami92

import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import javax.enterprise.inject.Default
import javax.inject.Inject

@Command(
    name = "migration",
    description = ["Migrate schema for specified database"],
    mixinStandardHelpOptions = true,
    showDefaultValues = true,
    sortOptions = false
)
class MigrationCommand : Runnable {

    @Parameters(
        index = "0",
        paramLabel = "MIGRATION_DIRECTORY",
        description = ["Migration directory"]
    )
    private lateinit var migrationDirectory: File

    @Option(
        names = ["-H", "--host"],
        description = ["Host/IP"]
    )
    private var host: String = "172.17.0.2"

    @Option(
        names = ["-P", "--port"],
        description = ["Port"]
    )
    private var port: Int = 9042

    @Option(
        names = ["-D", "--dc", "--local-datacenter"],
        description = ["DC"]
    )
    private var localDatacenter: String = "dc1"

    @Option(
        names = ["-G", "--graph-name"],
        description = ["Graph name"]
    )
    private var graphName: String = "my_graph"

    @Inject @field:Default
    internal lateinit var service: MigrationService

    override fun run() {
        service.run(
            migrationDirectory,
            host,
            port,
            localDatacenter,
            graphName
        )
    }
}
