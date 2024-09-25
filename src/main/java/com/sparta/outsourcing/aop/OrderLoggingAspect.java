package com.sparta.outsourcing.aop;

import com.sparta.outsourcing.domain.order.dto.request.OrderRequestDto;
import com.sparta.outsourcing.domain.order.dto.response.OrderResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@Aspect
@Component
public class OrderLoggingAspect {

    private static final String BEARER_PREFIX = "Bearer "; // JWT 토큰 접두사
    private static final String SECRET_KEY = System.getenv("SPRING_DATASOURCE_JWT"); // JWT 시크릿 키
    private static final int MAX_STACK_TRACE_LINES = 5; // 출력할 스택 트레이스 최대 라인 수 제어

    @Around("execution(* com.sparta.outsourcing.domain.order.service.service.OrderService.*(..))")
    public Object logExecutionWithToken(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        String methodName = joinPoint.getSignature().toShortString();
        String functionality = extractFunctionalityName(methodName);

        // JWT 토큰 추출 및 검증
        HttpServletRequest request = getRequest();
        String token = extractToken(request);
        Claims claims = parseJwtToken(token);

        // 유저 정보 추출
        Long userId = extractUserId(claims);
        String userRole = extractUserRole(claims);

        String actorInfo = String.format("유저 ID: %d, 권한: %s", userId, userRole);

        // 가게 ID 추출
        String storeId = extractStoreId(joinPoint.getArgs());

        LogUtility.log(LogLevel.INFO, String.format("[요청 처리 시작] 메서드: %s, 기능: '%s', 요청자: %s, 가게 ID: %s, 시작 시간: %s",
                methodName, functionality, actorInfo, storeId, startTime));

        // 비즈니스 로직 실행
        Object result = proceedWithExecution(joinPoint, functionality, actorInfo, startTime, methodName, storeId);

        // 종료 시간 및 성공 로그 남기기
        LocalDateTime endTime = LocalDateTime.now();
        LogUtility.log(LogLevel.INFO, String.format("[요청 처리 완료] 메서드: %s, 기능: '%s', 요청자: %s, 가게 ID: %s, 종료 시간: %s, 실행 시간: %sms",
                methodName, functionality, actorInfo, storeId, endTime, ChronoUnit.MILLIS.between(startTime, endTime)));

        return result;
    }

    // 주문 생성 후 주문 ID 로그
    @AfterReturning(pointcut = "execution(* com.sparta.outsourcing.domain.order.service.service.OrderService.createOrder(..))", returning = "result")
    public void logAfterOrderCreation(JoinPoint joinPoint, Object result) {
        OrderResponseDto orderResponse = (OrderResponseDto) result;
        OrderRequestDto orderRequest = (OrderRequestDto) joinPoint.getArgs()[0];

        Claims claims = parseJwtToken(extractToken(getRequest()));
        Long userId = extractUserId(claims);
        String userRole = extractUserRole(claims);

        LogUtility.log(LogLevel.INFO, String.format("[주문 생성 완료] 메서드: OrderService.createOrder(..), 기능: '주문 생성', 요청자: 유저 ID: %d, 권한: %s, 가게 ID: %d, 주문 ID: %d, 생성 시간: %s",
                userId, userRole, orderRequest.getStoreId(), orderResponse.getId(), LocalDateTime.now()));
    }

    // 주문 상태 변경 후 로그
    @AfterReturning(pointcut = "execution(* com.sparta.outsourcing.domain.order.service.service.OrderService.updateOrderStatus(..))", returning = "result")
    public void logAfterOrderStatusUpdate(JoinPoint joinPoint, Object result) {
        OrderResponseDto orderResponse = (OrderResponseDto) result;
        Long orderId = (Long) joinPoint.getArgs()[0];

        Claims claims = parseJwtToken(extractToken(getRequest()));
        Long userId = extractUserId(claims);
        String userRole = extractUserRole(claims);

        LogUtility.log(LogLevel.INFO, String.format("[주문 상태 변경 완료] 메서드: OrderService.updateOrderStatus(..), 기능: '주문 상태 변경', 요청자: 유저 ID: %d, 권한: %s, 주문 ID: %d, 상태: %s, 변경 시간: %s",
                userId, userRole, orderId, orderResponse.getStatus(), LocalDateTime.now()));
    }

    // 주문 조회 후 로그
    @AfterReturning(pointcut = "execution(* com.sparta.outsourcing.domain.order.service.service.OrderService.getOrderForUser(..)) || execution(* com.sparta.outsourcing.domain.order.service.service.OrderService.getOrderByOwner(..))", returning = "result")
    public void logAfterOrderRetrieval(JoinPoint joinPoint, Object result) {
        Long orderId = (Long) joinPoint.getArgs()[0]; // 첫 번째 인자로 주문 ID 추출

        // JWT 토큰에서 유저 정보 추출
        Claims claims = parseJwtToken(extractToken(getRequest()));
        Long userId = extractUserId(claims);
        String userRole = extractUserRole(claims);

        // 메서드명이 getOrderForUser 또는 getOrderByOwner에 따라 다르게 로그 처리
        String methodName = joinPoint.getSignature().getName();
        String featureName = methodName.equals("getOrderForUser") ? "고객 주문 조회" : "사장님 주문 조회";

        LogUtility.log(LogLevel.INFO, String.format("[주문 조회 완료] 메서드: %s, 기능: '%s', 요청자: 유저 ID: %d, 권한: %s, 주문 ID: %d, 조회 시간: %s",
                methodName, featureName, userId, userRole, orderId, LocalDateTime.now()));
    }


    // 비즈니스 로직 실행 및 에러 핸들링
    private Object proceedWithExecution(ProceedingJoinPoint joinPoint, String functionality, String actorInfo, LocalDateTime startTime, String methodName, String storeId) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            logErrorWithControlledStackTrace(e, functionality, actorInfo, startTime, methodName, storeId);
            throw e;
        }
    }

    // 에러 로그 출력 및 스택 트레이스 제어
    private void logErrorWithControlledStackTrace(Exception exception, String functionality, String actorInfo, LocalDateTime startTime, String methodName, String storeId) {
        LogUtility.log(LogLevel.ERROR, String.format("[요청 실패] 메서드: %s, 기능: '%s', 요청자: %s, 가게 ID: %s, 시작 시간: %s, 에러 발생 시간: %s",
                methodName, functionality, actorInfo, storeId, startTime, LocalDateTime.now()));

        LogUtility.log(LogLevel.ERROR, "Order AOP 에러 메시지: " + exception.getMessage());

        StackTraceElement[] stackTrace = exception.getStackTrace();

        // 스택 트레이스 제어 - 최대 5줄까지만 출력
        int maxStackTraceLines = Math.min(stackTrace.length, MAX_STACK_TRACE_LINES);
        LogUtility.log(LogLevel.ERROR, String.format("스택 트레이스 (현재 스택 제어 최대 %d줄):", maxStackTraceLines));

        for (int i = 0; i < maxStackTraceLines; i++) {
            LogUtility.log(LogLevel.ERROR, "at " + stackTrace[i]);
        }
    }

    // SQL 예외 처리 로그
    @AfterThrowing(pointcut = "execution(* com.sparta.outsourcing.domain.order.repository.*.*(..))", throwing = "exception")
    public void logSqlException(JoinPoint joinPoint, DataAccessException exception) {
        LogUtility.log(LogLevel.ERROR, String.format("[Order SQL 예외 발생] 메서드: %s, 시간: %s, 예외 메시지: %s",
                joinPoint.getSignature().toShortString(), LocalDateTime.now(), exception.getMessage()), exception);
    }

    // JWT 토큰에서 유저 ID 추출
    private Long extractUserId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            LogUtility.log(LogLevel.ERROR, "JWT 토큰에서 유저 ID를 추출할 수 없습니다. Claims: " + claims);
            throw new IllegalArgumentException("유효한 유저 ID가 없습니다.");
        }
    }

    // JWT 토큰에서 유저 권한 추출
    private String extractUserRole(Claims claims) {
        try {
            return claims.get("role", String.class);
        } catch (Exception e) {
            LogUtility.log(LogLevel.ERROR, "JWT 토큰에서 유저 권한을 추출할 수 없습니다. Claims: " + claims);
            throw new IllegalArgumentException("유효한 유저 권한이 없습니다.");
        }
    }

    // HttpServletRequest 객체 추출
    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    // JWT 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        } else {
            throw new IllegalArgumentException("유효한 JWT 토큰이 없습니다.");
        }
    }

    // JWT 토큰 파싱
    private Claims parseJwtToken(String token) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT 토큰 파싱 중 오류 발생: " + e.getMessage());
        }
    }

    // 기능 이름 추출
    private String extractFunctionalityName(String methodName) {
        if (methodName.contains("createOrder")) {
            return "주문 생성";
        } else if (methodName.contains("updateOrderStatus")) {
            return "주문 상태 변경";
        } else if (methodName.contains("getOrderById")) {
            return "주문 조회";
        }
        return "알 수 없는 기능";
    }

    // 가게 ID 추출
    private String extractStoreId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof OrderRequestDto) {
                return String.valueOf(((OrderRequestDto) arg).getStoreId());
            }
        }
        return "NULL"; // 기본값 처리
    }
}
