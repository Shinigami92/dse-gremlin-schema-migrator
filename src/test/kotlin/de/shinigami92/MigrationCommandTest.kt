package de.shinigami92

import io.quarkus.test.junit.main.Launch
import io.quarkus.test.junit.main.LaunchResult
import io.quarkus.test.junit.main.QuarkusMainTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusMainTest
open class MigrationCommandTest {
    @Test
    @Launch()
    fun testDefaultArguments(result: LaunchResult) {
        val message = "Host my-dse, Port 9042, DC dc1, Graph name my_graph"
        val output = result.output
        Assertions.assertTrue(output.contains(message))
    }
}
