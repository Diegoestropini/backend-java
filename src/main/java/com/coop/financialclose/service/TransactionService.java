package com.coop.financialclose.service;

import com.coop.financialclose.domain.enums.TransactionStatus;
import com.coop.financialclose.dto.TransactionDTO;
import com.coop.financialclose.repository.TransactionRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TransactionService {

    private final TransactionRecordRepository transactionRecordRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRecordRepository transactionRecordRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.transactionMapper = transactionMapper;
    }

    public Page<TransactionDTO> search(String branchCode, LocalDate date, TransactionStatus status, Pageable pageable) {
        return transactionRecordRepository.search(branchCode, date, status, pageable)
            .map(transactionMapper::toDto);
    }
}
