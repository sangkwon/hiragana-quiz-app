package com.example.hiraganapractice.viewmodel

import com.example.hiraganapractice.model.ALL_HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.HiraganaItem
import com.example.hiraganapractice.model.QuestionType
import com.example.hiraganapractice.model.QuizScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.random.Random

/**
 * generateDistractors는 순수 함수라 Robolectric 없이 테스트한다.
 * 무작위 결과를 시드로 고정해서, 실패하면 항상 같은 방식으로 실패하게 만든다.
 */
class DistractorGeneratorTest {

    private val ji = ALL_HIRAGANA_DATA_SET.first { it.character == "じ" }

    @Test
    fun `じ 문제의 오답 보기에 ji가 다시 나오지 않는다`() {
        // ぢ도 로마자가 "ji"라서, 걸러내지 않으면 보기에 "ji"가 두 개 생긴다.
        repeat(SEEDS) { seed ->
            val distractors = generateDistractors(
                ji, QuestionType.HIRAGANA_TO_ROMAJI, ALL_HIRAGANA_DATA_SET, Random(seed)
            )
            assertFalse("seed=$seed, 보기=$distractors", "ji" in distractors)
        }
    }

    @Test
    fun `ji 문제의 오답 보기에 ぢ가 나오지 않는다`() {
        // 로마자 "ji"를 보고 ぢ를 골라도 발음상 맞으므로, 정답이 두 개가 되면 안 된다.
        repeat(SEEDS) { seed ->
            val distractors = generateDistractors(
                ji, QuestionType.ROMAJI_TO_HIRAGANA, ALL_HIRAGANA_DATA_SET, Random(seed)
            )
            assertFalse("seed=$seed, 보기=$distractors", "ぢ" in distractors)
        }
    }

    @Test
    fun `모든 범위와 글자에서 오답 보기는 3개이고 서로 다르며 정답과 발음이 겹치지 않는다`() {
        for (scope in QuizScope.entries) {
            val dataSet = dataSetFor(scope)
            for (target in dataSet) {
                for (type in QuestionType.entries) {
                    repeat(50) { seed ->
                        val distractors = generateDistractors(target, type, dataSet, Random(seed))
                        val message = "scope=$scope, target=${target.character}, type=$type, seed=$seed, 보기=$distractors"

                        assertEquals(message, 3, distractors.size)
                        assertEquals(message, 3, distractors.toSet().size)
                        assertFalse(message, romajiOf(distractors, type, dataSet).contains(target.romaji))
                    }
                }
            }
        }
    }

    /** 보기 문자열을 로마자로 바꾼다 (ROMAJI_TO_HIRAGANA 보기는 글자이므로 데이터셋에서 찾는다). */
    private fun romajiOf(options: List<String>, type: QuestionType, dataSet: List<HiraganaItem>): List<String> =
        when (type) {
            QuestionType.HIRAGANA_TO_ROMAJI -> options
            QuestionType.ROMAJI_TO_HIRAGANA -> options.map { o -> dataSet.first { it.character == o }.romaji }
        }

    private companion object {
        const val SEEDS = 300
    }
}
