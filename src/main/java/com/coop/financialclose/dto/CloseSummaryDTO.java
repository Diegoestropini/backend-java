package com.coop.financialclose.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CloseSummaryDTO(LocalDate date,
                              List<CloseItemDTO> totals,
                              BigDecimal grandIncome,
                              BigDecimal grandExpense,
                              BigDecimal grandNet,
                              Long grandTxCount) {
}
