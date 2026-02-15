package com.coop.financialclose.controller;

import com.coop.financialclose.domain.enums.TransactionStatus;
import com.coop.financialclose.dto.TransactionDTO;
import com.coop.financialclose.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public Page<TransactionDTO> transactions(@RequestParam(required = false) String branchCode,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @RequestParam(required = false) TransactionStatus status,
                                             @PageableDefault(size = 50, sort = "txDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.search(branchCode, date, status, pageable);
    }
}
