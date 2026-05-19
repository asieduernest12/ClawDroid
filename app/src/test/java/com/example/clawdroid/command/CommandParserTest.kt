package com.example.clawdroid.command

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class CommandParserTest {

    private val registry = CommandRegistry.defaultCommands()

    @Test
    fun parse_clear_returnsSuccess() {
        val result = CommandParser.parse("/clear", registry)
        assertTrue(result is CommandParseResult.Success)
        assertEquals("clear", (result as CommandParseResult.Success).command.name)
    }

    @Test
    fun parse_modelWithArg_returnsSuccessWithArgs() {
        val result = CommandParser.parse("/model gpt-4", registry)
        assertTrue(result is CommandParseResult.Success)
        val success = result as CommandParseResult.Success
        assertEquals("model", success.command.name)
        assertEquals(listOf("gpt-4"), success.args)
    }

    @Test
    fun parse_sessionNew_returnsSuccess() {
        val result = CommandParser.parse("/session new", registry)
        assertTrue(result is CommandParseResult.Success)
        val success = result as CommandParseResult.Success
        assertEquals("session", success.command.name)
        assertEquals(listOf("new"), success.args)
    }

    @Test
    fun parse_unknownCommand_returnsUnknown() {
        val result = CommandParser.parse("/foobar", registry)
        assertTrue(result is CommandParseResult.Unknown)
    }

    @Test
    fun parse_clearWithExtraArgs_returnsSuccess() {
        val result = CommandParser.parse("/clear extra args here", registry)
        assertTrue(result is CommandParseResult.Success)
        val success = result as CommandParseResult.Success
        assertEquals(listOf("extra", "args", "here"), success.args)
    }

    @Test
    fun parse_noSlash_returnsUnknown() {
        val result = CommandParser.parse("not a command", registry)
        assertTrue(result is CommandParseResult.Unknown)
    }

    @Test
    fun parse_modelWithoutArgs_returnsMissingArgs() {
        val result = CommandParser.parse("/model", registry)
        assertTrue(result is CommandParseResult.MissingArgs)
    }
}
