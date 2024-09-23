package com.sparta.outsourcing.aop;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtility {

    // 기본 로그 레벨을 설정할 수 있는 필드 (INFO, DEBUG, ERROR, WARN, TRACE)
    private static LogLevel currentLogLevel = LogLevel.INFO;

    // 로그 레벨을 동적으로 설정
    public static void setLogLevel(LogLevel newLevel) {
        currentLogLevel = newLevel;
        log(currentLogLevel, "로그 레벨이 변경되었습니다: " + currentLogLevel);
    }

    // 로그 레벨별로 로그 출력
    public static void log(LogLevel level, String message) {
        if (level.ordinal() >= currentLogLevel.ordinal()) {
            switch (level) {
                case DEBUG -> log.debug(message);
                case ERROR -> log.error(message);
                case WARN -> log.warn(message);
                case TRACE -> log.trace(message);
                default -> log.info(message);
            }
        }
    }

    public static void log(LogLevel level, String message, Throwable throwable) {
        if (level.ordinal() >= currentLogLevel.ordinal()) {
            switch (level) {
                case DEBUG -> log.debug(message, throwable);
                case ERROR -> log.error(message, throwable);
                case WARN -> log.warn(message, throwable);
                case TRACE -> log.trace(message, throwable);
                default -> log.info(message, throwable);
            }
        }
    }
}
