package kr.ac.kopo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 활성화. (M-10 탈퇴 7일 경과 회원 자동 삭제 등 주기 작업)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
