package de.shinigami92

import javax.enterprise.inject.Default
import javax.inject.Inject
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "migration", mixinStandardHelpOptions = true)
class MigrationCommand : Runnable {

    @Parameters(paramLabel = "<host>", defaultValue = "172.17.0.2", description = ["Host/IP"])
    var host: String? = null

    @Parameters(paramLabel = "<port>", defaultValue = "9042", description = ["Port"])
    var port: Int? = null

    @Parameters(paramLabel = "<local-datacenter>", defaultValue = "dc1", description = ["DC"])
    var localDatacenter: String? = null

    @Parameters(
            paramLabel = "<graph-name>",
            defaultValue = "my_graph",
            description = ["Graph name"]
    )
    var graphName: String? = null

    @Inject @field:Default lateinit var service: MigrationService

    override fun run(): Unit {
        service.runMigration(host!!, port!!, localDatacenter!!, graphName!!)
    }
}
