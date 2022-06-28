package com.github.shinigami92

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement
import com.datastax.oss.driver.api.core.CqlSession
import java.net.InetSocketAddress
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MigrationService {

    fun run(
        host: String,
        port: Int,
        localDatacenter: String,
        graphName: String,
    ) {
        println("Host $host, Port $port, DC $localDatacenter, Graph name $graphName")

        println("Connecting to $host:$port")
        val session =
            CqlSession.builder()
                .addContactPoint(InetSocketAddress(host, port))
                .withLocalDatacenter(localDatacenter)
                .build()

        println("Connected to $host:$port")

        ensureSystemGraph(session, graphName)
    }

    // loadMigrations
    // lock
    // unlock
    // getRunMigrations
    // getMigrationsToRun
    // checkOrder
    // runMigrations

    fun ensureSystemGraph(session: CqlSession, graphName: String) {
        session.execute(
            ScriptGraphStatement.builder("system.graph(graphName).ifNotExists().create()")
                .setQueryParam("graphName", graphName)
                .setSystemQuery(true)
                .build()
        )

        println("Graph $graphName created")
    }
}
