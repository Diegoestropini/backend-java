package com.coop.financialclose.dto;

import java.time.LocalDate;

public record ProcessCloseResultDTO(LocalDate date, int rowsUpserted, long processingTimeMs) {
}
