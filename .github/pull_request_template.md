## 1. 8주차 코드 이전 커밋

<!-- `chore: 8주차 코드 이전` 커밋의 해시를 적어 주세요. 리뷰어는 이 커밋 이후의 변경만 확인합니다. -->

## 2. 완료한 요구사항

**1단계: 프론트 컨트롤러**
- [ ] URL별 처리를 Controller로 분리
- [ ] RequestMapper로 if/switch 분기 제거
- [ ] 매핑에 없는 요청 처리 (정적 파일 / 404)
- [ ] 객체 조립을 `WebServer.main()` 한 곳에서
- [ ] RequestMapper 테스트 작성

**2단계: Spring Boot**
- [ ] 프로젝트 생성과 정적 파일 옮기기
- [ ] 저장소를 빈으로 등록
- [ ] 회원가입
- [ ] 로그인
- [ ] 사용자 목록 API
- [ ] (선택) 회원 한 명 조회

## 3. 동작 확인 결과

<!-- 1단계: 브라우저·curl로 확인한 내용, `./gradlew test` 결과 -->
<!-- 2단계: 아래 명령의 결과를 붙여 주세요.
curl -i http://localhost:8080/api/users
curl -i --cookie "logined=true" http://localhost:8080/api/users
-->

## 4. 1단계 → 2단계 대응표

| 1단계에서 직접 만든 것 | Spring에서는? |
| --- | --- |
| `WebServer` (소켓·스레드 풀) | |
| `RequestHandler` + `RequestMapper` | |
| `Controller` 인터페이스와 구현체 | |
| `StaticFileController` | |
| `main`에서 객체를 만들고 연결하는 코드 | |
| `HttpResponse`의 redirect | |

## 5. 추가한 테스트

<!-- 어떤 경우를 테스트했는지 적어 주세요. -->

## 6. `@RestController` redirect 실험

<!-- `@RestController`에서 "redirect:/index.html"을 반환했을 때 어떤 응답이 왔고, 왜 그런지 적어 주세요. -->

## 7. 트러블 슈팅 · 고민한 점

<!-- 문제 → 원인 → 해결 순서로 적어 주세요. -->
