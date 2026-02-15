package com.coop.financialclose.dto;

import com.coop.financialclose.domain.enums.TransactionStatus;
import com.coop.financialclose.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(Long id,
                             LocalDate txDate,
                             String branchCode,
                             String productCode,
                             BigDecimal amount,
                             TransactionType txType,
                             TransactionStatus status,
                             String rejectionReason) {
}
