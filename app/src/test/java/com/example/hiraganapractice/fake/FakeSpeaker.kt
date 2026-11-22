package com.example.hiraganapractice.fake

import com.example.hiraganapractice.utils.Speaker

/**
 * 실제로 소리를 내지 않고, "무엇을 읽으라고 했는지"만 기록하는 가짜 Speaker.
 * 이렇게 기록을 남기는 Fake를 흔히 Spy라고도 부른다.
 */
class FakeSpeaker : Speaker {
    val spokenTexts = mutableListOf<String>()
    var isShutdown = false
        private set

    override fun speak(text: String) {
        spokenTexts += text
    }

    override fun shutdown() {
        isShutdown = true
    }
}
