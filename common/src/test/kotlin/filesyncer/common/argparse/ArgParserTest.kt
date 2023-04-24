package filesyncer.common.argparse

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ArgParserTest {

    @Test
    fun testParser() {
        var parser = ArgParser()
        parser.addArg("apos", ArgType.STRING, false)
        parser.addArg("bpos", ArgType.STRING, false)
        parser.addArg("aopt", ArgType.STRING, true, "hahaha")
        parser.addArg("bopt", ArgType.INT, true, 50)

        val args = parser.parse(arrayOf("aaa", "-a", "kelvin", "-b", "5050", "aaa"))
        Assertions.assertEquals("aaa", args["apos"])
        Assertions.assertEquals("aaa", args["bpos"])
        Assertions.assertEquals("kelvin", args["aopt"])
        Assertions.assertEquals(5050, args["bopt"])
    }
}
