package com.coop.financialclose.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@ConditionalOnProperty(name = "app.close.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class CloseScheduler {

    private final CloseService closeService;

    public CloseScheduler(CloseService closeService) {
        this.closeService = closeService;
    }

    @Scheduled(cron = "${app.close.cron:0 15 23 * * *}")
    public void runDailyClose() {
        closeService.processClose(LocalDate.now());
    }
}
