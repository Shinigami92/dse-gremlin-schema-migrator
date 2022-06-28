package com.github.shinigami92

import com.datastax.dse.driver.api.core.graph.DseGraph.g
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement
import com.datastax.dse.driver.api.core.graph.GraphResultSet
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement
import com.datastax.oss.driver.api.core.CqlSession
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MigrationService {

    fun run(
        host: String,
        port: Int,
        localDatacenter: String,
        graphName: String
    ) {
        println("Host $host, Port $port, DC $localDatacenter, Graph name $graphName")

        println("Connecting to $host:$port")
        val session = CqlSession.builder()
            .addContactPoint(InetSocketAddress(host, port))
            .withLocalDatacenter(localDatacenter)
            .build()

        println("Connected to $host:$port")

        ensureSystemGraph(session, graphName)

        val folder = File("src/main/resources/migrations")

        createVertexMigration(session, graphName)

        val migrationFiles = getMigrationFiles(folder)

        println("Found ${migrationFiles.size} migration files")

        for (i in migrationFiles.indices) {
            val file = migrationFiles.get(i)
            val filename = file.getName()
            val step = i + 1

            println("--- Migration $filename ---")

            var migrationStatement = try {
                IOUtils.toString(FileInputStream(file), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
                System.exit(1)
                return
            }

            val md5Hex = DigestUtils.md5Hex(migrationStatement)
            // println("md5Hex: $md5Hex");

            val md5HexFromDB = getMd5HexFromDB(session, graphName, step)

            if (md5HexFromDB.isNullOrEmpty()) {
                println("Compare md5: file:$md5Hex, db:$md5HexFromDB")
                if (md5Hex == md5HexFromDB) {
                    println("Skipping $filename: already executed")
                    println("--- Migration $filename skipped successfully ---")
                    continue
                } else {
                    println("$filename, md5Hex differs. Aborting migration.")
                    break
                }
            } else {
                println("Execute migration $filename")
                session.execute(
                    ScriptGraphStatement
                        .newInstance(migrationStatement)
                        .setGraphName(graphName)
                )

                println("Insert migration vertex $filename")
                addMigrationVertex(session, graphName, step, md5Hex)
            }

            println("--- Migration $filename executed successfully ---")
        }
    }

    fun ensureSystemGraph(session: CqlSession, graphName: String) {
        session.execute(
            ScriptGraphStatement
                .builder("system.graph(graphName).ifNotExists().create()")
                .setQueryParam("graphName", graphName)
                .setSystemQuery(true)
                .build()
        )

        println("Graph $graphName created")
    }

    fun createVertexMigration(
        session: CqlSession,
        graphName: String
    ): GraphResultSet {
        return session.execute(
            ScriptGraphStatement.newInstance(
                "schema" +
                    ".vertexLabel('migration')" +
                    ".ifNotExists()" +
                    ".partitionBy('step', Int)" +
                    ".property('md5hex', Text)" +
                    ".property('executedAt', Timestamp)" +
                    ".create();"
            ).setGraphName(graphName)
        )
    }

    fun getMigrationFiles(folder: File): List<File> {
        return folder.listFiles({ file -> file.name.endsWith(".groovy") })
            .sortedBy { file -> file.name }
            .toList()
    }

    fun addMigrationVertex(
        session: CqlSession,
        graphName: String,
        step: Int,
        md5Hex: String
    ): GraphResultSet {
        return session.execute(
            FluentGraphStatement.newInstance(
                g.addV("migration")
                    .property("step", step)
                    .property("md5hex", md5Hex)
                    .property("executedAt", Instant.now())
            ).setGraphName(graphName)
        )
    }

    fun getMd5HexFromDB(
        session: CqlSession,
        graphName: String,
        step: Int
    ): String? {
        val traversal: GraphTraversal<Vertex, String> = g.V()
            .has("migration", "step", step)
            .values("md5Hex")
        val result = session.execute(
            FluentGraphStatement.newInstance(traversal).setGraphName(graphName)
        )
        return result.one()?.asString()
    }
}
