package com.example.hiraganapractice.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HiraganaDataTest {

    private val sets = mapOf(
        "BASIC" to BASIC_HIRAGANA_DATA_SET,
        "DAKUTEN" to DAKUTEN_HIRAGANA_DATA_SET,
        "YOON_SOKUON" to YOON_AND_SOKUON_DATA_SET,
        "KATAKANA" to KATAKANA_DATA_SET,
    )

    @Test
    fun `all data sets are non-empty and have no blank fields`() {
        for ((name, set) in sets) {
            assertTrue("$name is empty", set.isNotEmpty())
            set.forEach {
                assertTrue("$name blank character: $it", it.character.isNotBlank())
                assertTrue("$name blank romaji: $it", it.romaji.isNotBlank())
            }
        }
    }

    @Test
    fun `characters are unique within each data set`() {
        for ((name, set) in sets) {
            val dups = set.groupBy { it.character }.filterValues { it.size > 1 }.keys
            assertTrue("$name duplicate characters: $dups", dups.isEmpty())
        }
    }

    @Test
    fun `all hiragana set is the concatenation of its parts`() {
        assertEquals(
            BASIC_HIRAGANA_DATA_SET + DAKUTEN_HIRAGANA_DATA_SET + YOON_AND_SOKUON_DATA_SET,
            ALL_HIRAGANA_DATA_SET
        )
    }

    @Test
    fun `getHiraganaText maps romaji to character and passes through hiragana`() {
        assertEquals("あ", getHiraganaText("a"))
        assertEquals("か", getHiraganaText("KA"))
        assertEquals("し", getHiraganaText("し"))
    }

    @Test
    fun `getHiraganaText returns unknown text unchanged`() {
        assertEquals("zzz", getHiraganaText("zzz"))
    }
}
