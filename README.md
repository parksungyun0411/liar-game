# Liar Game — Java 소켓 멀티플레이어

> 최대 8명이 동시 접속하는 TCP 소켓 기반 멀티플레이어 라이어 게임.
> 한 명(라이어)만 제시어를 모른 채 시작해, 대화로 라이어를 찾아내면 시민 승 — 지목된 라이어가 제시어를 맞히면 라이어 승.

![라이어 게임 타이틀 화면](./mainimage진진진짜.png)

## 핵심 요약

- **기간**: 2024.03 ~ 2024.05 (3-1 네트워크 프로그래밍 수업 팀 프로젝트)
- **팀**: 3인 (김호정 · 양승원 · 박성윤)
- **본인(박성윤) 담당**: 서버 측 멀티스레드 처리 전담 + 게임 로직 설계
  — `LiarServer.java` / `OneClientModul` / `GameManager.java`
- **기술**: Java, Swing, `Socket` / `ServerSocket`, `Thread` / `Vector`, Gradle
- **기획 자료**: [제안서 발표자료](./docs/proposal.pptx)

## 게임 흐름

1. 서버 관리자가 포트를 지정해 서버를 열고, 플레이어들이 닉네임·IP·포트로 입장 (최대 8명, 닉네임 중복 차단)
2. 게임 시작 시 주제 풀(`주제.txt`, 181개)에서 랜덤 제시어 선정, 플레이어 중 1명을 라이어로 무작위 지정 — 라이어에게만 제시어를 숨김
3. 무작위 순서로 1인당 10초씩 제시어를 한마디로 설명 (본인 차례에만 채팅 잠금 해제)
4. 20초 투표 (전원 투표 완료 시 조기 마감) → 최다 득표자를 라이어로 지목
5. 지목이 맞으면 라이어에게 10초 내 제시어 추리 기회 — 맞히면 라이어 승, 틀리면 시민 승. 지목이 틀리면 라이어 승

## 아키텍처

```
            LoginUi.main() ── 단일 진입점
           ┌──────┴──────┐
   "서버 생성하기"       "서버 입장하기"
           │                  │
      ServerUi (Swing)   ClientUi (Swing)
           │                  │
      LiarServer          Client (Runnable, 수신 스레드)
      ├ serverThread:         │
      │  accept 루프          │ TCP (DataInput/OutputStream,
      │  → 접속마다           │      writeUTF 텍스트 프로토콜)
      │  OneClientModul ◄─────┘
      │  스레드 생성 (최대 8)
      └ gameThread: 시작 버튼 → GameManager 구동
```

- **`LiarServer`**: `ServerSocket` accept 루프를 `serverThread`로 돌리고, 접속마다 `OneClientModul` 스레드를 생성해 `Vector`에 보관. 게임 시작 시 별도 `gameThread`에서 `GameManager`를 구동해 accept 루프와 게임 진행이 서로 블로킹되지 않도록 분리. 인원 상한(8명)·게임 중 입장 차단·강퇴 처리
- **`OneClientModul`**: 클라이언트 1명당 1 스레드로 수신 대기. 메시지 접두사(`liarTopic` = 라이어의 제시어 추리, `cVote` = 투표, `gm` = 게임 명령, 그 외 = 채팅)로 분기하고 전체 브로드캐스트. 전원 투표 완료 시 `gameThread.interrupt()`로 대기 조기 해제
- **`GameManager`**: 주제 로딩 → 라이어 무작위 선정 → 발언 순서 셔플과 채팅 잠금/해제 → 투표 집계(`Collections.frequency`) → 승패 판정. 진행 상황은 `gm` 접두사 명령으로 클라이언트 UI(투표 다이얼로그, 결과 화면)를 원격 제어
- **`ClientUi` / `ServerUi` / `LoginUi` / `VoteDialog` / `Result`**: Swing GUI. 커스텀 `RoundedButton`, `ImagePanel`(배경 이미지 패널) 사용

## 실행 방법

JDK 필요 (Gradle 8.4 wrapper 포함). 리소스(이미지, `주제.txt`)를 작업 디렉토리에서 읽으므로 **반드시 리포 루트에서 실행**해야 한다.

```bash
# 빌드
./gradlew build

# 실행 (macOS/Linux — Windows는 클래스패스 구분자를 ; 로)
java -cp "build/classes/java/main:." LoginUi
```

- 실행 후 "서버 생성하기"로 한 명이 서버를 열고, 나머지는 "서버 입장하기"로 접속
- `주제.txt`는 EUC-KR 인코딩인데 코드가 플랫폼 기본 문자셋으로 읽는다 — 한국어 Windows 기준으로 개발되어, 기본 문자셋이 UTF-8인 환경에서는 제시어가 깨질 수 있음
- 일부 이미지 리소스(`pBack.png`, `buddy.jpg`)는 리포에 남아있지 않아 해당 패널이 비어 보일 수 있음

## 팀 구성 · 역할

3인 팀 프로젝트 — 김호정 · 양승원 · 박성윤.
원본 저장소는 [@kimhoojung/Network_11](https://github.com/kimhoojung/Network_11) (final branch)이며, 본 리포는 포트폴리오 목적으로 복제한 것입니다. 개별 커밋 작성자 정보는 그대로 보존되어 있습니다.

**본인(박성윤) 담당** — 서버 측 멀티스레드 처리 + 게임 로직:

- `LiarServer.java`: accept 루프 스레드, 접속 인원 상한·게임 중 입장 차단, 게임 스레드 분리, 강퇴
- `OneClientModul`: 클라이언트 1명당 1 스레드 수신, 접두사 기반 메시지 프로토콜 분기, 입퇴장·브로드캐스트, 닉네임 중복 검사
- `GameManager.java`: 주제 풀 로딩, 라이어 선정, 발언 순서·채팅 잠금 제어, 투표 집계, 승패 판정

클라이언트 측 구현과 Swing UI·리소스 제작은 팀원들과 분담했습니다 (세부 분담은 기획 단계 자료인 `docs/proposal.pptx` 참고).

## 배운 점 / 한계

- **TCP 소켓 + 멀티스레드 직접 경험**: 스레드 생성·동기화·자원 정리를 프레임워크 없이 손으로 다뤄봄. 동시 접속 환경에서 강퇴·브로드캐스트 타이밍 버그를 디버깅한 것이 가장 기억에 남는 부분
- **알게 된 한계**:
  - 텍스트 접두사 기반 프로토콜은 확장에 취약 → 다시 한다면 JSON 등 구조화된 메시지 + 명령 enum
  - 동기화가 `Vector`에 의존 → 게임 진행 중 입퇴장 시 순회-수정 경합 위험
  - 네트워크 스레드에서 Swing 컴포넌트를 직접 갱신 → `SwingUtilities.invokeLater`로 EDT에 위임했어야 함
  - 리소스를 작업 디렉토리 상대 경로로 로딩 → JAR 패키징 시 깨짐. classpath 리소스로 옮겨야 함
