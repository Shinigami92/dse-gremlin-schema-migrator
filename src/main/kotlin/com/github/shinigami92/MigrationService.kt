package com.github.shinigami92

import com.datastax.dse.driver.api.core.graph.DseGraph.g
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement
import com.datastax.dse.driver.api.core.graph.GraphResultSet
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement
import com.datastax.oss.driver.api.core.CqlSession
import org.apache.commons.io.IOUtils
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.math.BigInteger
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MigrationService {

    fun run(
        migrationDirectory: File,
        host: String,
        port: Int,
        localDatacenter: String,
        graphName: String
    ) {
        validateMigrationDirectory(migrationDirectory)
        validatePort(port)
        validateGraphName(graphName)

        println("Host $host, Port $port, DC $localDatacenter, Graph name $graphName, Migration directory $migrationDirectory")

        println("Connecting to $host:$port")
        val session = CqlSession.builder()
            .addContactPoint(InetSocketAddress(host, port))
            .withLocalDatacenter(localDatacenter)
            .build()

        println("Connected to $host:$port")

        ensureSystemGraph(session, graphName)

        createVertexMigration(session, graphName)

        val migrationFiles = getMigrationFiles(migrationDirectory)

        println("Found ${migrationFiles.size} migration files")

        for (i in migrationFiles.indices) {
            val file = migrationFiles[i]
            val filename = file.name
            val step = i + 1

            println("--- Migration $filename ---")

            var migrationStatement = try {
                IOUtils.toString(FileInputStream(file), StandardCharsets.UTF_8)
            } catch (e: IOException) {
                throw UncheckedIOException("Failed to read migration file $filename", e)
            }

            val checksum = generateChecksum(migrationStatement)
            // println("checksum: $checksum");

            val checksumFromDB = getChecksumFromDB(session, graphName, step)

            if (checksumFromDB != null) {
                println("Compare checksum: file:$checksum, db:$checksumFromDB")
                if (checksum == checksumFromDB) {
                    println("Skipping $filename: already executed")
                    println("--- Migration $filename skipped successfully ---")
                    continue
                } else {
                    println("$filename, checksum differs. Aborting migration.")
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
                addMigrationVertex(session, graphName, step, checksum)
            }

            println("--- Migration $filename executed successfully ---")
        }

        session.close()
    }

    private fun generateChecksum(migrationStatement: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(migrationStatement.toByteArray())
        val digest = md.digest()
        return String.format("%032x", BigInteger(1, digest))
    }

    private fun validateMigrationDirectory(migrationDirectory: File) {
        if (!migrationDirectory.exists()) {
            throw IllegalArgumentException("Migration directory $migrationDirectory does not exist")
        }

        if (!migrationDirectory.isDirectory) {
            throw IllegalArgumentException("Migration directory $migrationDirectory is not a directory")
        }
    }

    private fun validatePort(port: Int) {
        if (port < 0 || port > 65535) {
            throw IllegalArgumentException("Invalid port $port, must be between 0 and 65535")
        }
    }

    private fun validateGraphName(graphName: String) {
        if (!Regex("^[a-zA-Z0-9]\\w*$").matches(graphName)) {
            throw IllegalArgumentException("Graph name must begin with an alpha-numeric character and can only contain alpha-numeric characters and underscores")
        }
    }

    private fun ensureSystemGraph(session: CqlSession, graphName: String) {
        session.execute(
            ScriptGraphStatement
                .builder("system.graph(graphName).ifNotExists().create()")
                .setQueryParam("graphName", graphName)
                .setSystemQuery(true)
                .build()
        )

        println("Graph $graphName created")
    }

    private fun createVertexMigration(
        session: CqlSession,
        graphName: String
    ): GraphResultSet {
        return session.execute(
            ScriptGraphStatement.newInstance(
                "schema" +
                    ".vertexLabel('migration')" +
                    ".ifNotExists()" +
                    ".partitionBy('step', Int)" +
                    ".property('checksum', Text)" +
                    ".property('executedAt', Timestamp)" +
                    ".create();"
            ).setGraphName(graphName)
        )
    }

    private fun getMigrationFiles(directory: File): List<File> {
        return directory.listFiles({ file -> file.name.endsWith(".groovy") })
            .sortedBy { file -> file.name }
            .toList()
    }

    private fun addMigrationVertex(
        session: CqlSession,
        graphName: String,
        step: Int,
        checksum: String
    ): GraphResultSet {
        return session.execute(
            FluentGraphStatement.newInstance(
                g.addV("migration")
                    .property("step", step)
                    .property("checksum", checksum)
                    .property("executedAt", Instant.now())
            ).setGraphName(graphName)
        )
    }

    private fun getChecksumFromDB(
        session: CqlSession,
        graphName: String,
        step: Int
    ): String? {
        val traversal: GraphTraversal<Vertex, String> = g.V()
            .has("migration", "step", step)
            .values("checksum")
        val result = session.execute(
            FluentGraphStatement.newInstance(traversal).setGraphName(graphName)
        )
        return result.one()?.asString()
    }
}
