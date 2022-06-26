package de.shinigami92

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MigrationService {

    fun runMigration(
            host: String,
            port: Int,
            localDatacenter: String,
            graphName: String,
    ): Unit {
        System.out.printf(
                "Host %s, Port %d, DC %s, Graph name %s\n",
                host,
                port,
                localDatacenter,
                graphName
        )
    }
}
