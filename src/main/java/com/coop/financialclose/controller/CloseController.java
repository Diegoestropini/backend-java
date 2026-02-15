package com.coop.financialclose.controller;

import com.coop.financialclose.dto.CloseSummaryDTO;
import com.coop.financialclose.dto.ProcessCloseResultDTO;
import com.coop.financialclose.service.CloseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class CloseController {

    private final CloseService closeService;

    public CloseController(CloseService closeService) {
        this.closeService = closeService;
    }

    @GetMapping("/close-summary")
    public CloseSummaryDTO summary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam(required = false) String branchCode,
                                   @RequestParam(required = false) String productCode) {
        return closeService.summary(date, branchCode, productCode);
    }

    @PostMapping("/process-close")
    public ProcessCloseResultDTO processClose(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return closeService.processClose(date);
    }
}
