package de.shinigami92

import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "migration", mixinStandardHelpOptions = true)
class MigrationCommand : Runnable {

    @Parameters(paramLabel = "<host>", defaultValue = "my-dse", description = ["Host/IP"])
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

    override fun run() {
        System.out.printf(
                "Host %s, Port %d, DC %s, Graph name %s\n",
                host,
                port,
                localDatacenter,
                graphName
        )
    }
}
