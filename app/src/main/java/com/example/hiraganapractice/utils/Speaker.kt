package com.example.hiraganapractice.utils

/**
 * 글자를 소리 내어 읽어 주는 기능의 계약.
 * 실제 구현은 Android TextToSpeech를 쓰는 TextToSpeechHelper,
 * 테스트에서는 읽으라고 한 글자만 기록하는 FakeSpeaker를 쓴다.
 */
interface Speaker {
    fun speak(text: String)
    fun shutdown()
}
