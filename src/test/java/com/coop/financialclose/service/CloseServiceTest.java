package com.coop.financialclose.service;

import com.coop.financialclose.dto.CloseSummaryDTO;
import com.coop.financialclose.dto.ProcessCloseResultDTO;
import com.coop.financialclose.repository.CloseProjection;
import com.coop.financialclose.repository.DailyCloseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseServiceTest {

    @Mock
    private DailyCloseRepository dailyCloseRepository;

    private CloseService closeService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        closeService = new CloseService(dailyCloseRepository, new CloseSummaryBuilder());
    }

    @Test
    void shouldProcessCloseAndReturnRowsUpserted() {
        when(dailyCloseRepository.insertAggregatedForDate(any(LocalDate.class))).thenReturn(3);

        ProcessCloseResultDTO result = closeService.processClose(LocalDate.of(2026, 2, 15));

        assertEquals(3, result.rowsUpserted());
    }

    @Test
    void shouldBuildSummaryTotals() {
        CloseProjection projection = new CloseProjection() {
            @Override
            public String getBranchCode() { return "SUC001"; }
            @Override
            public String getBranchName() { return "Centro"; }
            @Override
            public String getProductCode() { return "PROD001"; }
            @Override
            public String getProductName() { return "Ahorro"; }
            @Override
            public BigDecimal getTotalIncome() { return new BigDecimal("100.00"); }
            @Override
            public BigDecimal getTotalExpense() { return new BigDecimal("40.00"); }
            @Override
            public BigDecimal getNetTotal() { return new BigDecimal("60.00"); }
            @Override
            public Long getTxCount() { return 2L; }
        };

        when(dailyCloseRepository.findSummary(any(LocalDate.class), any(), any())).thenReturn(List.of(projection));

        CloseSummaryDTO summary = closeService.summary(LocalDate.of(2026, 2, 15), null, null);

        assertEquals(new BigDecimal("100.00"), summary.grandIncome());
        assertEquals(new BigDecimal("40.00"), summary.grandExpense());
        assertEquals(new BigDecimal("60.00"), summary.grandNet());
        assertEquals(2L, summary.grandTxCount());
        assertEquals(1, summary.totals().size());
    }
}
