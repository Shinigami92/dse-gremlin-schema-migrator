import io.quarkus.test.junit.main.Launch
import io.quarkus.test.junit.main.LaunchResult
import io.quarkus.test.junit.main.QuarkusMainTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusMainTest
class MigrationCommandTest {
    @Test
    @Launch()
    fun testDefaultArguments(result: LaunchResult) {
        val expected = "Host my-dse, Port 9042, DC dc1, Graph name my_graph\n"
        val actual = result.output
        Assertions.assertTrue(actual.contains(expected))
    }
}
