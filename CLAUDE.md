# HiraganaPractice

히라가나/가타카나 퀴즈 안드로이드 앱 (Kotlin, Jetpack Compose, 단일 `app` 모듈).
구조: `QuizViewModel` (상태/로직) ← `QuizStatsRepository` (SharedPreferences) / `TextToSpeechHelper`.

## 빌드·검증 명령

Gradle은 Android Studio의 JDK를 사용한다. 명령 앞에 항상 `JAVA_HOME`을 지정할 것.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug   # 빌드
./gradlew test            # JVM 단위 테스트 (Robolectric 포함, 에뮬레이터 불필요)
```

## 작업 규칙

- 코드를 수정한 뒤에는 반드시 `./gradlew test`를 실행해 통과를 확인한다.
- 로직(ViewModel, Repository, 데이터셋)을 바꾸면 해당 테스트도 함께 추가/수정한다.
- 테스트가 실패하면 테스트를 약하게 고치기 전에, 코드 변경이 의도한 동작 변화인지 먼저 판단하고 보고한다.
- 테스트는 `app/src/test/java/com/example/hiraganapractice/` 아래에 둔다.
