package com.coop.financialclose.dto;

import java.util.Map;

public record UploadResultDTO(long processedRows,
                              long acceptedRows,
                              long rejectedRows,
                              Map<String, Long> errorsByReason,
                              long processingTimeMs) {
}
