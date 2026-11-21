# KUIT 8기 Server · 9주차 미션

## 서블릿과 Spring MVC — 프론트 컨트롤러

8주차에 직접 만든 Custom Tomcat 위에 **프론트 컨트롤러**를 만들고, 같은 기능을 **Spring Boot**로 다시 만들어 보는 미션입니다. 직접 만든 구조가 Spring MVC에서 각각 무엇으로 바뀌는지 확인하는 것이 목표입니다.

- [9주차 워크북](https://app.notion.com/p/41f0adbca4ca818a9389fecf9779f99f)
- [9주차 미션 상세 요구사항](https://app.notion.com/p/41f0adbca4ca818dba70ed01c1e9cfd6)
- 마감일과 영상은 디스코드 공지를 확인해 주세요.

## 배경

8주차 2단계에서 `HttpRequest`와 `HttpResponse`를 분리해 `RequestHandler`가 많이 가벼워졌습니다. 하지만 `RequestHandler`에는 아직 문제가 남아 있습니다.

- URL마다 `if`로 분기하고, 분기마다의 처리까지 직접 합니다.
- 기능이 하나 늘 때마다 `RequestHandler`를 고쳐야 합니다.
- 저장소 같은 객체를 각 처리 코드가 직접 가져옵니다.

이번 주에는 이 구조를 **모든 요청을 한 입구에서 받아 알맞은 컨트롤러에 넘기는 구조**로 바꾸고, Spring MVC가 같은 일을 어떻게 하는지 확인합니다.

## 시작하기

이번 주는 시작 코드가 없습니다. **각자 8주차에 구현한 코드**에서 출발합니다.

1. 이 저장소를 본인의 GitHub 계정으로 Fork하고 clone합니다.

```bash
git clone https://github.com/YOUR_GITHUB_ID/KUIT8_Server_week9.git
cd KUIT8_Server_week9
git switch -c YOUR_NICKNAME
```

2. 본인의 **8주차 브랜치**에서 아래 파일과 폴더를 이 저장소의 **루트**로 복사합니다. `.gitignore`는 복사하지 않습니다.

```text
src/  webapp/  build.gradle  settings.gradle  gradle/  gradlew  gradlew.bat
```

3. 복사한 상태 그대로, **아무것도 고치지 않고** 첫 커밋을 합니다.

```bash
git add .
git commit -m "chore: 8주차 코드 이전"
```

이 커밋을 분리해야 PR에서 9주차에 바꾼 부분만 리뷰할 수 있습니다. PR 본문에 이 커밋의 해시를 적어 주세요.

4. 8주차 기능이 그대로 동작하는지 확인합니다.

```bash
./gradlew test
./gradlew run
```

> 8주차 2단계(`HttpRequest`·`HttpResponse` 분리)를 마치지 못했다면, 그 부분을 먼저 마친 뒤 9주차를 시작합니다.

### 실행 환경

- JDK 21 (8주차와 동일)
- 1단계: 저장소 루트에서 `./gradlew run` → http://localhost:8080
- 2단계: `spring-mvc` 폴더에서 `./gradlew bootRun` → http://localhost:8080
- Windows PowerShell에서는 `./gradlew.bat run`, `./gradlew.bat bootRun`, `./gradlew.bat test`를 사용합니다.
- 1단계 서버와 2단계 서버는 같은 8080 포트를 쓰므로 **동시에 실행하지 않습니다.**

### 완성 후 저장소 구조

```text
KUIT8_Server_week9/
├── src/            # 1단계: 8주차 코드 + 프론트 컨트롤러
├── webapp/         # 1단계: 실습 화면
├── build.gradle    # 1단계 빌드 설정
├── ...
└── spring-mvc/     # 2단계: Spring Initializr로 만든 Spring Boot 프로젝트
```

## 1단계 — 프론트 컨트롤러 만들기

- [ ] URL별 처리를 `Controller` 인터페이스를 구현한 클래스로 분리하기
- [ ] (HTTP 메서드, 경로)를 키로 하는 Map으로 `RequestMapper`를 만들고 **if/else·switch로 URL을 분기하지 않기**
- [ ] 매핑에 있는 요청 → 컨트롤러, 매핑에 없는 정적 파일 → 파일 응답, 둘 다 아님 → 404
- [ ] `RequestMapper`와 컨트롤러를 `WebServer.main()`에서 한 번만 만들고, 저장소는 생성자로 주입하기
- [ ] `RequestMapper` 테스트 작성하기

### 요청 경로와 기대 동작

8주차 동작은 그대로 유지하고, 매핑에 없는 요청에 대한 404만 새로 추가됩니다.

| 요청 | 기대 동작 |
| --- | --- |
| `GET /`, `GET /index.html` | 메인 HTML 반환 |
| `POST /user/signup` | 회원가입 후 `/index.html`로 302 이동 |
| `POST /user/login` | 성공: `Set-Cookie: logined=true; Path=/` + `/index.html`로 302 이동 / 실패: `/user/login_failed.html`로 302 이동 |
| `GET /user/userList`, `GET /user/list.html` | 로그인 상태이면 목록 화면, 아니면 `/user/login.html`로 302 이동 |
| 그 밖의 정적 파일 | 파일 반환 (CSS는 `Content-Type: text/css`) |
| 위 어디에도 해당하지 않는 요청 | **404 Not Found** |

`/user/list.html`은 정적 파일이지만 로그인 검사가 필요합니다. 정적 파일 처리로 빠지지 않도록 매핑에 직접 등록하세요.

## 2단계 — Spring Boot로 옮기기

- [ ] Spring Initializr로 `spring-mvc` 프로젝트를 만들고 html·css를 `src/main/resources/static`으로 옮기기
- [ ] `MemoryUserRepository`를 스프링 빈으로 등록하고 `getInstance()` 제거하기
- [ ] `POST /user/signup` 회원가입
- [ ] `POST /user/login` 로그인과 쿠키
- [ ] `GET /api/users` 사용자 목록 API (쿠키가 없으면 401)
- [ ] (선택) `GET /api/users/{userId}` 회원 한 명 조회

### 프로젝트 생성

[Spring Initializr](https://start.spring.io)에서 아래처럼 설정하고 **GENERATE**를 누릅니다. 받은 zip을 이 저장소 루트에 풀어 `spring-mvc/` 폴더가 생기게 합니다.

| 항목 | 값 |
| --- | --- |
| Project | Gradle - Groovy |
| Language | Java |
| Spring Boot | 기본 선택된 버전 그대로 |
| Group | `kuit8th` |
| Artifact | `spring-mvc` |
| Package name | `kuit8th.springmvc` |
| Packaging | Jar |
| Java | **21** |
| Dependencies | **Spring Web** 하나만 |

- Spring Data JPA 같은 DB 의존성을 미리 넣으면 DB 설정이 없어서 서버가 실행되지 않습니다. DB는 10주차에서 추가합니다.
- IntelliJ에서는 `spring-mvc` 폴더를 별도 프로젝트로 엽니다.
- 이번 주는 Lombok 없이 생성자를 직접 작성합니다.
- 서블릿 패키지는 `javax.servlet`이 아니라 `jakarta.servlet`입니다. 블로그 예제를 참고할 때 import를 확인하세요.
- `user/list.html`은 `static`으로 **옮기지 않습니다.** `static`의 파일은 컨트롤러를 거치지 않아 로그인 검사를 할 수 없습니다.

### 동작 확인

```bash
curl -i http://localhost:8080/api/users
curl -i --cookie "logined=true" http://localhost:8080/api/users
```

첫 요청은 401, 두 번째 요청은 가입한 회원 이름 목록이 JSON으로 와야 합니다. 메뉴의 `/user/userList` 링크는 2단계에서 처리하지 않아도 되며, 404가 나오는 것이 정상입니다.

## 미션 범위

- 사용자 저장소는 8주차와 같은 메모리 저장소입니다. 서버를 재시작하면 가입 정보가 사라집니다.
- `logined=true` 쿠키는 요청·응답 학습용이며 실제 서비스의 인증 방식이 아닙니다. 인증은 12주차에서 다룹니다.
- 2단계 응답은 회원 이름 목록(`List<String>`)으로 충분합니다. 응답 전용 객체(DTO)는 11주차에서 다룹니다.
- 2단계 테스트 코드는 선택입니다. 대신 curl 결과를 PR에 첨부합니다.

## 테스트

```bash
./gradlew test
```

1단계에서 최소한 아래 네 가지를 테스트합니다.

- 매핑에 있는 요청(`POST /user/login`) → 해당 컨트롤러가 선택된다
- 매핑에 없는 정적 파일(`GET /css/styles.css`) → 파일이 응답된다
- 어디에도 없는 경로 → 404가 응답된다
- 쿠키 없이 `GET /user/list.html` → `/user/login.html`로 302가 응답된다

8주차에 제공된 테스트와 본인이 작성한 8주차 테스트도 계속 통과해야 합니다. 요청 예제 파일이 필요하면 `src/test/resources`에 두고 클래스패스로 읽습니다. 본인 컴퓨터의 절대 경로를 테스트에 넣지 마세요.

## 제출 방법

1. 본인 Fork 저장소의 이름·닉네임 브랜치에서 진행합니다. 별도의 이름 폴더는 만들지 않습니다.
2. 기능과 테스트를 확인한 뒤 commit하고 본인 브랜치에 push합니다.
3. **base 저장소: `Konkuk-KUIT/KUIT8_Server_week9`, base 브랜치: `main`**으로 PR을 생성합니다.
4. head 저장소는 본인의 Fork, compare 브랜치는 작업한 이름·닉네임 브랜치로 선택합니다.
5. 제공된 PR 템플릿에 8주차 코드 이전 커밋 해시, 완료한 요구사항, 동작 확인 결과, 1단계 → 2단계 대응표, 추가한 테스트, 실험 결과, 트러블 슈팅을 작성합니다.

## 검토 기준

- if/else·switch 없이 Map으로 요청과 컨트롤러를 연결했는지
- 매핑 → 정적 파일 → 404 순서와 `/user/list.html` 로그인 검사가 지켜지는지
- 객체 생성과 연결이 한 곳에 모여 있고, 의존성을 생성자로 주입받는지
- 공유되는 컨트롤러 객체에 요청마다 달라지는 값을 저장하지 않는지
- Spring에서 직접 만든 싱글톤 없이 빈과 생성자 주입을 사용했는지
- 1단계에서 만든 구조가 Spring MVC의 무엇에 대응하는지 설명할 수 있는지
