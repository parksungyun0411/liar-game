# Liar Game — Java 네트워크 멀티플레이어

> 8명까지 동시 접속 가능한 TCP 소켓 기반 실시간 멀티플레이어 라이어 게임. Java + Swing으로 클라이언트·서버 GUI를 직접 구현했습니다.

## 📌 핵심 요약
- **기간**: 2024.03 ~ 2024.05 (네트워크 프로그래밍 팀 프로젝트)
- **팀**: 3인 (김호정 · 양승원 · 본인)
- **본인 담당**: 서버 측 멀티스레드 처리 전담 + 게임 로직 설계
  → `LiarServer.java` (TCP accept 루프, 클라이언트별 스레드 생성, 게임 스레드 분리), `OneClientModul` (클라이언트 1명당 1 스레드 I/O), `GameManager.java` (라운드 진행, 주제·라이어 선정, 발언 순서·투표·결과 판정)
- **기술 키워드**: Java, Swing GUI, TCP Socket, Multi-Threading, Gradle
- **기획 문서**: [제안서 발표자료](./docs/proposal.pptx)

## 🎯 무엇을 만들었나
보드게임 "라이어 게임"의 온라인 멀티플레이어 버전. 한 명만 "주제어"를 모르는 상태로 시작해서, 대화를 통해 라이어를 찾아내거나, 라이어가 들키지 않고 주제어를 맞히면 이기는 게임.

**게임 흐름**:
1. 서버 관리자가 게임 시작 (포트 지정, 최대 8명 수용)
2. 181개 주제 풀에서 랜덤 주제 선정, 한 명을 라이어로 무작위 지정
3. 플레이어들이 순서대로 10초씩 주제어를 한마디로 설명
4. 전원 투표 후 최다 득표자가 라이어로 지목
5. 라이어가 맞으면 → 라이어에게 10초 내 주제어 추리 기회
6. 라이어가 주제어를 맞히면 라이어 승, 아니면 시민 승

## 🏗 아키텍처

```
┌─────────────┐         TCP (port 3000)          ┌─────────────┐
│  ClientUi   │ ◄──────────────────────────────► │ ServerUi    │
│  (Swing)    │   DataInputStream / Output       │ (Swing)     │
└──────┬──────┘                                  └──────┬──────┘
       │                                                │
       ▼                                                ▼
   Client.java                                     LiarServer.java
   (Runnable + ActionListener)                     (Thread, max 8 OCMs)
                                                        │
                                                        ▼
                                                  GameManager
                                                  (round logic,
                                                   voting, result)
```

- **`LiarServer`**: `ServerSocket`으로 클라이언트 accept → 각 접속마다 `OneClientModul` 스레드 생성. 게임 시작 시 별도 `gameThread`로 `GameManager` 구동
- **`OneClientModul`**: 클라이언트 1명당 1 스레드. 메시지 프로토콜 (`liarTopic`, `cVote`, `gm` 접두사 등)로 채팅·투표·게임 상태 분기
- **`GameManager`**: 라운드 진행, 주제·라이어 선정, 발언 순서 잠금/해제, 투표 집계, 결과 판정
- **`ClientUi` / `ServerUi` / `LoginUi` / `VoteDialog` / `Result`**: Swing 기반 GUI. 커스텀 컴포넌트 (`RoundedButton`, `ImagePanel`)로 시각 효과

## 🔧 기술 스택
- **언어**: Java
- **GUI**: Swing (`JFrame`, `JDialog`, 커스텀 `JPanel`)
- **네트워크**: `java.net.ServerSocket` / `Socket`, `DataInputStream` / `DataOutputStream`
- **동시성**: `Thread`, `Vector` (스레드 안전 리스트), 멀티스레드 브로드캐스트
- **빌드**: Gradle (Kotlin DSL, `build.gradle.kts`)

## 💡 기술적 의사결정
- **WebSocket 대신 raw TCP**: 네트워크 프로그래밍 수업 맥락 — 소켓·스트림·프로토콜을 직접 다루는 학습 목적. 메시지 접두사 기반 간단한 텍스트 프로토콜로 채팅과 게임 명령을 한 채널에서 처리.
- **각 클라이언트 1 스레드 모델**: 8명이 상한이라 스레드 풀 없이 처리 가능. 인원 늘리려면 NIO 또는 Netty 전환 필요.
- **GameManager를 별도 스레드로 분리**: accept 루프(`serverThread`)와 게임 로직(`gameThread`)이 서로 블로킹되지 않도록 분리.

## 📊 배운 점 / 한계
- **TCP 소켓 + 멀티스레드 직접 경험**: 동시성·동기화·자원 정리를 처음 손으로 다뤄봄
- **알게 된 한계**:
  - 메시지 프로토콜이 텍스트 접두사 기반이라 확장성 약함 → 다시 한다면 JSON + protocol enum
  - `synchronized` 부족 (`Vector` 의존) → 동시 입퇴장에서 ConcurrentModificationException 위험
  - GUI 스레드와 네트워크 스레드 분리가 모호 → 실무라면 `SwingUtilities.invokeLater` 적극 사용
  - 이미지·주제 파일을 작업 디렉토리에서 직접 읽음 → JAR 패키징 시 깨짐. classpath 로딩 필요

## 🚀 실행 방법

```bash
# Gradle로 빌드
./gradlew build

# 서버 실행 (한 사람)
java -cp build/classes/java/main LiarServer
# → 포트 입력 후 "시작" 버튼

# 클라이언트 실행 (각자)
java -cp build/classes/java/main Client
# → 서버 IP, 포트, 닉네임 입력
```

> ⚠️ `주제.txt`, 이미지 파일들이 작업 디렉토리에 있어야 정상 동작.

## 👥 팀 구성

3인 팀 협업 프로젝트 — 김호정, 양승원, 박성윤 (본인).
원본 레포지토리는 [@kimhoojung/Network_11](https://github.com/kimhoojung/Network_11) (final branch). 본 레포는 동일 커밋 이력을 포트폴리오 목적으로 본 계정에 복제한 것이며, **개별 커밋 작성자 정보는 그대로 보존되어 있습니다**.

자세한 기획·역할 분담은 [`docs/proposal.pptx`](./docs/proposal.pptx) 참고.

### 본인 담당 영역 (3인 팀 중 1인 담당)
- **서버 인프라**: `LiarServer.java` — `ServerSocket` 기반 accept 루프, 접속 인원 상한 처리(최대 8명), 게임 진행을 별도 `gameThread`로 분리해 accept 블로킹 방지
- **클라이언트별 스레드 모듈**: `OneClientModul` — 클라이언트 1명당 1 스레드, 메시지 프로토콜 분기(`liarTopic`/`cVote`/`gm` 접두사), 입퇴장·강퇴·브로드캐스트 처리
- **게임 로직 설계**: `GameManager.java` — 주제 풀(181개) 로딩, 라이어 무작위 선정, 발언 순서 셔플·잠금·해제, 투표 집계, 라이어 승/패 판정 분기

> 위 코드들은 면접에서 라인 단위로 설명 가능.
