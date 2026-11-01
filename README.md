# Hell (NeoForge 1.21.1) — 1단계: 기반

## 들어 있는 것
- 지옥 차원 `hell:hell` (오버월드 지형 + 빨간 잔디 바이옴 `hell:inverted_hell`, 주황 하늘)
- 지옥 포탈 블록 `hell:hell_portal` (네더 포탈 구조)
- 흑요석 틀(내부 2x3 ~ 21x21) 안쪽에서 **메아리 수정**을 우클릭하면 포탈이 열림
  - 포탈 내부 맨 아래 칸이 **Y 0 이하**일 때만 열림
  - 메아리 수정 1개 소모 (크리에이티브는 소모 없음)
- 포탈 진입 시 반대편 차원(오버월드 <-> 지옥)의 같은 X/Z로 이동, 근처 포탈이 없으면 자동 생성

## 아직 없는 것 (다음 단계)
- 낮/밤에 따라 바뀌는 하늘(아침 붉은/주황, 밤 하양)
- 물 대신 용암, 반전 동물, 마을/전초기지, 나머지 바이옴과 몹, 아이템

## 실행
1. JDK 21 + IntelliJ IDEA 설치
2. 이 폴더를 IntelliJ로 열기 (Gradle 프로젝트로 인식)
3. `gradlew`/`gradle/wrapper/gradle-wrapper.jar`가 없으면
   - Gradle 8.10+ 설치 후 폴더에서 `gradle wrapper` 실행, 또는
   - NeoForge MDK(1.21.1)에서 `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`를 복사
4. `./gradlew runClient` 로 게임 실행, `./gradlew build` 로 jar 빌드 (`build/libs/`)

## 테스트 방법
1. 크리에이티브로 Y -30 근처까지 내려가 흑요석 틀(4x5)을 만든다.
2. `/give @s minecraft:echo_shard` 후 틀 안쪽에서 우클릭.
3. 포탈에 들어가면 지옥으로 이동. 바로 가보려면 `/execute in hell:hell run tp @s ~ 100 ~` 도 가능.
