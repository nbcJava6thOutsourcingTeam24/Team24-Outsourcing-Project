package com.sparta.outsourcing.aop;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@Profile("test")
@RestController
@RequestMapping("/log-level")
public class LogLevelController {

    @PostMapping("/set")
    public ResponseEntity<String> setLogLevel(@RequestParam LogLevel level) {
        LogUtility.setLogLevel(level);
        return ResponseEntity.ok("로그 레벨이 " + level + "로 설되었습니다.");
    }
}
