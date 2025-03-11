# JWT를 활용한 Stateless Spring Security 예제

## 프로젝트 소개
이 프로젝트는 Spring Security와 JWT(JSON Web Token)를 활용한 Stateless 인증 구현 예제입니다. 기존 세션 기반 인증 방식 대신 JWT를 사용하여 서버가 상태를 유지하지 않는(Stateless) 방식으로 인증을 구현했습니다.

## 기술 스택
- Java 17
- Spring Boot 3.3.4

## 프로젝트 구조
```
src
├── main
│   ├── java
│   │   └── org.example.statelessspringsecurity
│   │       ├── config
│   │       │   ├── JwtAuthenticationFilter.java     # JWT 토큰 검증 및 인증 처리 필터
│   │       │   ├── JwtAuthenticationToken.java      # JWT 인증 토큰 객체
│   │       │   ├── JwtUtil.java                     # JWT 생성 및 검증 유틸
│   │       │   └── SecurityConfig.java              # Spring Security 설정
│   │       ├── controller                           # 컨트롤러
│   │       ├── dto                                  # 데이터 전송 객체
│   │       ├── entity                               # JPA 엔티티
│   │       ├── enums                                # 열거형
│   │       ├── repository                           # JPA 리포지토리
│   │       ├── service                              # 비즈니스 로직
│   │       └── StatelessSpringSecurityApplication.java
│   └── resources
│       └── application.yml                          # 애플리케이션 설정
└── test                                             # 테스트 코드
```

## 주요 기능
1. **Stateless 인증**: 서버가 사용자 인증 상태를 저장하지 않고 JWT 토큰으로 인증을 처리합니다.
2. **Spring Security 필터 체인 커스터마이징**: 세션 관리, 폼 로그인, 기본 인증 등 불필요한 기능을 비활성화하였습니다.
3. **JWT 토큰 기반 인증**: Authorization 헤더의 Bearer 토큰으로 인증을 처리합니다.
4. **역할(Role) 기반 권한 부여**: 사용자 역할에 따라 API 접근 권한이 다르게 설정됩니다.

## 인증 흐름
1. 회원가입: `/auth/signup` 엔드포인트를 통해 사용자 등록
2. 로그인: `/auth/signin` 엔드포인트를 통해 로그인 후 JWT 토큰 발급 (응답 헤더의 Authorization에 포함)
3. 인증: 발급받은 JWT 토큰을 요청 헤더의 Authorization에 Bearer 형식으로 추가하여 API 호출

## API 엔드포인트
- `POST /auth/signup`: 회원가입
- `POST /auth/signin`: 로그인 및 JWT 토큰 발급
- `GET /open`: 인증 없이 접근 가능한 API
- `GET /test`: ADMIN 권한을 가진 사용자만 접근 가능한 API

## 설정 방법
1. `application.yml` 파일에 JWT 시크릿 키 설정:
```yaml
jwt:
  secret:
    key: [시크릿 키]  # 최소 256비트 이상의 Base64 인코딩된 시크릿 키
```

## 테스트 코드

이 프로젝트에서는 Spring Security를 사용한 세 가지 서로 다른 테스트 방식을 구현했습니다.

### 1. 기본 인증 토큰 주입 방식 (TestControllerTest)
각 테스트 메서드마다 `JwtAuthenticationToken`을 생성하여 인증 객체를 직접 주입하는 방식입니다.

```java
@Test
public void 권한이_ADMIN일_경우_200() throws Exception {
    AuthUser authUser = new AuthUser(1L, "admin@example.com", UserRole.ROLE_ADMIN);
    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
    
    mockMvc.perform(get("/test")
                    .with(authentication(authenticationToken)))
            .andExpect(status().isOk());
}
```

이 방식은 각 테스트 케이스마다 인증 객체를 생성해야 하므로 코드 중복이 발생할 수 있지만, 테스트마다 다른 인증 정보가 필요한 경우 유연하게 설정할 수 있습니다.

### 2. 사전 설정된 토큰 재사용 방식 (TestControllerWithSetUpTokenTest)
`@BeforeEach`를 사용하여 테스트 실행 전에 인증 토큰을 미리 생성하고 재사용하는 방식입니다.

```java
@BeforeEach
public void setUp() {
    AuthUser adminUser = new AuthUser(1L, "admin@example.com", UserRole.ROLE_ADMIN);
    adminAuthenticationToken = new JwtAuthenticationToken(adminUser);
    
    AuthUser normalUser = new AuthUser(2L, "user@example.com", UserRole.ROLE_USER);
    userAuthenticationToken = new JwtAuthenticationToken(normalUser);
}

@Test
public void 권한이_ADMIN일_경우_200() throws Exception {
    mockMvc.perform(get("/test")
                    .with(authentication(adminAuthenticationToken)))
            .andExpect(status().isOk());
}
```

이 방식은 여러 테스트에서 동일한 인증 정보를 재사용할 수 있어 코드 중복을 줄일 수 있습니다.

### 3. 커스텀 어노테이션 방식 (TestControllerWithMockAuthUserTest)
`@WithMockAuthUser`와 같은 커스텀 어노테이션을 생성하여 테스트 메서드에 직접 인증 정보를 설정하는 방식입니다.

```java
@Test
@WithMockAuthUser(userId = 1L, email = "admin@example.com", role = UserRole.ROLE_ADMIN)
public void 권한이_ADMIN일_경우_200() throws Exception {
    mockMvc.perform(get("/test"))
            .andExpect(status().isOk());
}
```

이 방식은 가장 간결하고 가독성이 높으며, 테스트 메서드에 직접 인증 정보를 명시하므로 테스트 의도를 쉽게 파악할 수 있습니다. 커스텀 어노테이션은 다음과 같이 구현됩니다:

```java
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestSecurityContextFactory.class)
public @interface WithMockAuthUser {
    long userId();
    String email();
    UserRole role();
}
```

그리고 `TestSecurityContextFactory`는 다음과 같이 구현됩니다:

```java
public class TestSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthUser authUser = new AuthUser(customUser.userId(), customUser.email(), customUser.role());
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(authUser);
        context.setAuthentication(authentication);
        return context;
    }
}
```

### 통합 테스트 (AuthIntegrationTest)
위 세 가지 테스트 방식 외에도, 실제 회원가입과 로그인 프로세스를 통해 JWT 토큰을 발급받고 이를 사용하여 엔드포인트를 호출하는 통합 테스트도 구현되어 있습니다.

```java
@Test
public void 회원가입과_로그인_후_ADMIN_인가를_통과하고_유저_정보를_확인한다() throws Exception {
    // 1. 회원가입
    SignupRequest signupRequest = new SignupRequest(adminEmail, UserRole.Authority.ADMIN);
    mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest))
                    .with(csrf()))
            .andExpect(status().isOk());

    // 2. 로그인
    SigninRequest signinRequest = new SigninRequest(adminEmail);
    MvcResult mvcResult = mockMvc.perform(post("/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinRequest))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    String bearerToken = mvcResult.getResponse().getHeader("Authorization");

    // 3. 발급받은 토큰으로 API 호출
    mockMvc.perform(get("/test")
                    .header("Authorization", bearerToken))
            .andExpect(status().isOk());
}
```

이 통합 테스트는 실제 애플리케이션 동작 흐름을 그대로 테스트하므로 전체 인증 프로세스의 동작을 검증하는 데 유용합니다.

## 라이센스
이 프로젝트는 LICENSE 파일에 명시된 라이센스 조건에 따라 배포됩니다. 
