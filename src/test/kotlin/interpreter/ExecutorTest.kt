package interpreter

import org.junit.jupiter.api.Test
import java.io.CharArrayWriter
import kotlin.test.assertEquals

class ExecutorTest {
    private lateinit var charArrayWriter : CharArrayWriter
    private lateinit var toTest : Executor
}