package com.coop.financialclose.service;

import com.coop.financialclose.domain.entity.TransactionRecord;
import com.coop.financialclose.dto.TransactionDTO;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDTO toDto(TransactionRecord transaction) {
        return new TransactionDTO(
            transaction.getId(),
            transaction.getTxDate(),
            transaction.getRawBranchCode(),
            transaction.getRawProductCode(),
            transaction.getAmount(),
            transaction.getTxType(),
            transaction.getStatus(),
            transaction.getRejectionReason());
    }
}
