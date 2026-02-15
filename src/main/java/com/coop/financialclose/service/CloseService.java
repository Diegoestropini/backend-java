package com.coop.financialclose.service;

import com.coop.financialclose.dto.CloseSummaryDTO;
import com.coop.financialclose.dto.ProcessCloseResultDTO;
import com.coop.financialclose.repository.CloseProjection;
import com.coop.financialclose.repository.DailyCloseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CloseService {

    private final DailyCloseRepository dailyCloseRepository;
    private final CloseSummaryBuilder closeSummaryBuilder;

    public CloseService(DailyCloseRepository dailyCloseRepository, CloseSummaryBuilder closeSummaryBuilder) {
        this.dailyCloseRepository = dailyCloseRepository;
        this.closeSummaryBuilder = closeSummaryBuilder;
    }

    @Transactional
    public ProcessCloseResultDTO processClose(LocalDate date) {
        long start = System.currentTimeMillis();
        dailyCloseRepository.deleteByCloseDate(date);
        int rows = dailyCloseRepository.insertAggregatedForDate(date);
        long elapsed = System.currentTimeMillis() - start;
        return new ProcessCloseResultDTO(date, rows, elapsed);
    }

    @Transactional(readOnly = true)
    public CloseSummaryDTO summary(LocalDate date, String branchCode, String productCode) {
        List<CloseProjection> projections = dailyCloseRepository.findSummary(date, branchCode, productCode);
        return closeSummaryBuilder.build(date, projections);
    }
}
