package com.coop.financialclose.service;

import com.coop.financialclose.dto.CloseItemDTO;
import com.coop.financialclose.dto.CloseSummaryDTO;
import com.coop.financialclose.repository.CloseProjection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class CloseSummaryBuilder {

    public CloseSummaryDTO build(LocalDate date, List<CloseProjection> projections) {
        List<CloseItemDTO> items = projections.stream()
            .map(p -> new CloseItemDTO(
                p.getBranchCode(),
                p.getBranchName(),
                p.getProductCode(),
                p.getProductName(),
                p.getTotalIncome(),
                p.getTotalExpense(),
                p.getNetTotal(),
                p.getTxCount()))
            .toList();

        BigDecimal grandIncome = items.stream().map(CloseItemDTO::totalIncome).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandExpense = items.stream().map(CloseItemDTO::totalExpense).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandNet = items.stream().map(CloseItemDTO::netTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long grandTxCount = items.stream().mapToLong(CloseItemDTO::txCount).sum();

        return new CloseSummaryDTO(date, items, grandIncome, grandExpense, grandNet, grandTxCount);
    }
}
