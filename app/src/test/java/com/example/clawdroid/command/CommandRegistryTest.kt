package com.example.clawdroid.command

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class CommandRegistryTest {

    @Test
    fun defaultCommands_containsAllSix() {
        val registry = CommandRegistry.defaultCommands()
        assertEquals(6, registry.getAll().size)
    }

    @Test
    fun find_byExactName_returnsCommand() {
        val registry = CommandRegistry.defaultCommands()
        val cmd = registry.find("clear")
        assertNotNull(cmd)
        assertEquals("clear", cmd?.name)
    }

    @Test
    fun find_withLeadingSlash_returnsCommand() {
        val registry = CommandRegistry.defaultCommands()
        val cmd = registry.find("/clear")
        assertNotNull(cmd)
        assertEquals("clear", cmd?.name)
    }

    @Test
    fun find_byAlias_returnsCommand() {
        val registry = CommandRegistry.defaultCommands()
        val cmd = registry.find("h")
        assertNotNull(cmd)
        assertEquals("help", cmd?.name)
    }

    @Test
    fun find_unknownCommand_returnsNull() {
        val registry = CommandRegistry.defaultCommands()
        assertNull(registry.find("foobar"))
    }

    @Test
    fun search_prefixMatch_returnsCommands() {
        val registry = CommandRegistry.defaultCommands()
        val results = registry.search("mo")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.name.startsWith("mo", ignoreCase = true) || it.aliases.any { a -> a.startsWith("mo", ignoreCase = true) } })
    }

    @Test
    fun search_noMatch_returnsEmpty() {
        val registry = CommandRegistry.defaultCommands()
        assertTrue(registry.search("zzzzzz").isEmpty())
    }
}
