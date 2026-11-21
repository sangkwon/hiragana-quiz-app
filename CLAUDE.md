# HiraganaPractice

히라가나/가타카나 퀴즈 안드로이드 앱 (Kotlin, Jetpack Compose, 단일 `app` 모듈).
구조: `QuizViewModel` (상태/로직) ← `QuizStatsRepository` (SharedPreferences) / `TextToSpeechHelper`.

## 빌드·검증 명령

Gradle은 Android Studio의 JDK를 사용한다. 명령 앞에 항상 `JAVA_HOME`을 지정할 것.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug   # 빌드
./gradlew test            # JVM 단위 테스트 (Robolectric 포함, 에뮬레이터 불필요, 수 초)
./gradlew connectedDebugAndroidTest   # Compose UI 테스트 (에뮬레이터/기기 필요, 약 4~5분)
```

UI 테스트용 에뮬레이터 부팅 (헤드리스):

```bash
~/Library/Android/sdk/emulator/emulator -avd Pixel_3a_API_34_extension_level_7_arm64-v8a -no-window -no-audio -no-boot-anim &
~/Library/Android/sdk/platform-tools/adb wait-for-device
```

특정 UI 테스트만 실행: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.hiraganapractice.QuizFlowTest#메서드명`
(여러 개를 쉼표로 넘기면 1개만 실행되는 경우가 있으니 하나씩 실행할 것)

## 작업 규칙

- 코드를 수정한 뒤에는 반드시 `./gradlew test`를 실행해 통과를 확인한다. 화면/흐름(UI)을 건드렸다면 `connectedDebugAndroidTest`도 실행한다.
- 로직(ViewModel, Repository, 데이터셋)을 바꾸면 해당 테스트도 함께 추가/수정한다.
- 테스트가 실패하면 테스트를 약하게 고치기 전에, 코드 변경이 의도한 동작 변화인지 먼저 판단하고 보고한다.
- 테스트는 `app/src/test/java/com/example/hiraganapractice/` 아래에 둔다.
- UI 테스트는 `app/src/androidTest/.../QuizFlowTest.kt`. 화면 텍스트(한국어)와 `testTag`(`question_prompt`, `option_0..3`)로 요소를 찾는다. 문구를 바꾸면 테스트도 함께 수정한다.
- 퀴즈 정답은 무작위라, UI 테스트는 `BASIC_HIRAGANA_DATA_SET`에서 정답 보기를 계산해 누른다.
