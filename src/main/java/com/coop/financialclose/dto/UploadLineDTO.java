package com.coop.financialclose.dto;

import java.time.LocalDate;

public record UploadLineDTO(LocalDate date, String branchCode, String productCode, String amount, String txType) {
}
