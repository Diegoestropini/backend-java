package com.coop.financialclose.dto;

import java.math.BigDecimal;

public record CloseItemDTO(String branchCode,
                           String branchName,
                           String productCode,
                           String productName,
                           BigDecimal totalIncome,
                           BigDecimal totalExpense,
                           BigDecimal netTotal,
                           Long txCount) {
}
