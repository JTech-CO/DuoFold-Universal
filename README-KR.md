# DuoFold Universal - 1.0 Stable 준비

현재 버전은 **1.0.0-dev.1**이며 정식 버전 검증 완료를 의미하지 않습니다.
기존 2.0.0 표시는 프로토타입 개정 번호였습니다.

## 구현 구조

- `duofold-core`: Compose 의존성이 없는 입력 계약, 보정, 기하, 두 패널 회전, FoldRenderState.
- `duofold-compose`: 공통 AGSL/SkSL 셰이더, Compose modifier, Android/데스크톱 입력 어댑터.
- `composeApp`: 공통 데모 UI.
- `androidApp`, `desktopApp`: 플랫폼별 실행 호스트.

중앙 접힘은 양쪽의 독립된 부호 있는 회전각을 사용합니다. `FoldPanels.fromOpening(120f)`는
120도 개방각을 두 패널에 분배합니다. 각 픽셀을 패널에 역투영하고, 겹치는 경우 가까운 면을
선택하며, 투명도를 보존합니다. 하드웨어 힌지 개방각은 모션 센서처럼 영점 보정하지 않습니다.
센서와 코루틴 수집은 화면의 시작/정지 수명주기에 연결했습니다.

이는 강체 패널의 기하 모델이며 스프링/질량 기반 동역학이나 실측 광학 모델은 아닙니다.
정확히 닫힌 상태는 화면 투영의 특이점 때문에 렌더링에서 패널당 89도로 제한합니다.

## 빌드

JDK 21, Kotlin 2.4.20, Compose 1.12.0, AGP 9.3.1, Gradle Wrapper 9.5.0을 사용합니다.
최신 Compose 의존성 때문에 compileSdk는 37이며, 데모의 targetSdk는 요청한 36입니다.
Android SDK `platforms;android-37.0`, Build Tools 36.0.0이 필요합니다. 렌더러 최소 API는 33입니다.

```powershell
./gradlew.bat :androidApp:assembleDebug :androidApp:lintDebug
./gradlew.bat :desktopApp:run
./gradlew.bat :duofold-core:desktopTest :duofold-compose:desktopTest
./scripts/test-windows.ps1
./gradlew.bat :duofold-core:checkKotlinAbi :duofold-compose:checkKotlinAbi
```

Windows 한글 경로에서 Gradle 테스트 워커가 클래스를 찾지 못하면 `test-windows.ps1`로
동일한 컴파일 결과를 JUnit에서 직접 실행할 수 있습니다. JAVA_HOME과 ANDROID_HOME은
사용하는 JDK/SDK 설치 경로로 설정합니다.

SDK 사용 예제와 로컬 Maven 스테이징 명령은 [영문 README](README.md)에 있습니다.
SDK 패키지명은 기존 `com.example.duofold`를 유지하며, Maven 그룹 `dev.duofold`는
로컬 스테이징용 임시 값입니다. Stable 전에 소유권과 API를 확정해야 합니다.

## 남은 출시 조건

[로컬 검증 결과](validation/LOCAL-RESULTS.md)와 [실기기 검증 절차](validation/README.md)를 확인하세요.
Galaxy 일반폰·Tab·Z Fold·Z Flip·Windows·macOS의 실측 결과, 성능 및 시각 검증이 모두 필요합니다.
macOS는 JVM 데스크톱 경로이며 Kotlin/Native 렌더러가 아닙니다.
힌지 센서만으로 실제 접힘 선 위치·방향·가림 영역을 알 수 없어 WindowManager 연결과
회전·DeX·외부 화면 검증이 남아 있습니다. CI 파일은 추가했지만 원격 CI 실행 결과는 없습니다.

기존 자료에 상류 프로젝트 라이선스 미확인 기록이 있어 [라이선스 검토](docs/LICENSING.md)가
필요합니다. 임의의 오픈소스 라이선스를 부여하거나 Stable로 표시하지 않았습니다.
로컬 검증 당시에는 Git 연결 전이었습니다. 현재 개발 변경은
[JTech-CO/DuoFold-Universal](https://github.com/JTech-CO/DuoFold-Universal)에서 관리합니다.
