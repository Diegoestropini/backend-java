package com.coop.financialclose.service.batch;

import com.coop.financialclose.domain.entity.Branch;
import com.coop.financialclose.domain.entity.FinancialProduct;
import com.coop.financialclose.domain.enums.ProductType;
import com.coop.financialclose.domain.enums.TransactionStatus;
import com.coop.financialclose.domain.enums.TransactionType;
import com.coop.financialclose.dto.UploadResultDTO;
import com.coop.financialclose.repository.BranchRepository;
import com.coop.financialclose.repository.FinancialProductRepository;
import com.coop.financialclose.service.validation.TransactionValidationStrategy;
import com.coop.financialclose.service.validation.ValidationResult;
import com.coop.financialclose.service.validation.ValidationStrategyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class UploadService {

    private static final int BATCH_SIZE = 1000;

    private final BranchRepository branchRepository;
    private final FinancialProductRepository productRepository;
    private final ValidationStrategyFactory validationStrategyFactory;
    private final JdbcTemplate jdbcTemplate;

    public UploadService(BranchRepository branchRepository,
                         FinancialProductRepository productRepository,
                         ValidationStrategyFactory validationStrategyFactory,
                         JdbcTemplate jdbcTemplate) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.validationStrategyFactory = validationStrategyFactory;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public UploadResultDTO process(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();

        Map<String, Branch> branchesByCode = new HashMap<>();
        for (Branch branch : branchRepository.findAll()) {
            branchesByCode.put(branch.getCode(), branch);
        }

        Map<String, FinancialProduct> productsByCode = new HashMap<>();
        for (FinancialProduct product : productRepository.findAll()) {
            productsByCode.put(product.getCode(), product);
        }

        long processed = 0;
        long accepted = 0;
        long rejected = 0;
        Map<String, Long> errorsByReason = new HashMap<>();

        List<InsertRow> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (lineNumber == 1 && looksLikeHeader(cols)) {
                    continue;
                }

                processed++;
                ParseResult parsed = parse(cols);
                FinancialProduct product = productsByCode.get(parsed.productCode);
                Branch branch = branchesByCode.get(parsed.branchCode);

                TransactionStatus status = TransactionStatus.CONCILIADA;
                String rejectionReason = null;

                if (!parsed.validDate || !parsed.validAmount) {
                    status = TransactionStatus.RECHAZADA;
                    rejectionReason = "PARSE_ERROR";
                } else if (branch == null) {
                    status = TransactionStatus.RECHAZADA;
                    rejectionReason = "BRANCH_NOT_FOUND";
                } else if (product == null) {
                    status = TransactionStatus.RECHAZADA;
                    rejectionReason = "PRODUCT_NOT_FOUND";
                } else if (parsed.amount.compareTo(BigDecimal.ZERO) <= 0) {
                    status = TransactionStatus.RECHAZADA;
                    rejectionReason = "INVALID_AMOUNT";
                } else if (!parsed.validTxType) {
                    status = TransactionStatus.RECHAZADA;
                    rejectionReason = "INVALID_TX_TYPE";
                } else {
                    TransactionValidationStrategy strategy = validationStrategyFactory.resolve(product.getType());
                    if (strategy != null) {
                        ValidationResult result = strategy.validate(parsed.toUploadLineDTO());
                        if (!result.valid()) {
                            status = TransactionStatus.RECHAZADA;
                            rejectionReason = result.reason();
                        }
                    }
                }

                if (status == TransactionStatus.CONCILIADA) {
                    accepted++;
                } else {
                    rejected++;
                    errorsByReason.merge(rejectionReason, 1L, Long::sum);
                }

                batch.add(new InsertRow(
                    parsed.date,
                    branch == null ? null : branch.getId(),
                    product == null ? null : product.getId(),
                    parsed.branchCode,
                    parsed.productCode,
                    parsed.amount,
                    parsed.txType,
                    status,
                    rejectionReason,
                    LocalDateTime.now()
                ));

                if (batch.size() == BATCH_SIZE) {
                    flush(batch);
                }
            }
        }

        flush(batch);

        long elapsed = System.currentTimeMillis() - start;
        return new UploadResultDTO(processed, accepted, rejected, errorsByReason, elapsed);
    }

    private boolean looksLikeHeader(String[] cols) {
        if (cols.length < 5) {
            return false;
        }
        String first = cols[0].trim().toLowerCase(Locale.ROOT);
        return first.equals("fecha") || first.equals("date");
    }

    private ParseResult parse(String[] cols) {
        String dateRaw = cols.length > 0 ? cols[0].trim() : "";
        String branchCode = cols.length > 1 ? cols[1].trim() : "";
        String productCode = cols.length > 2 ? cols[2].trim() : "";
        String amountRaw = cols.length > 3 ? cols[3].trim() : "";
        String typeRaw = cols.length > 4 ? cols[4].trim() : "";

        LocalDate date;
        boolean validDate = true;
        try {
            date = LocalDate.parse(dateRaw);
        } catch (Exception ex) {
            date = LocalDate.now();
            validDate = false;
        }

        BigDecimal amount;
        boolean validAmount = true;
        try {
            amount = new BigDecimal(amountRaw);
        } catch (Exception ex) {
            amount = BigDecimal.ZERO;
            validAmount = false;
        }

        TransactionType txType;
        boolean validTxType = true;
        try {
            txType = TransactionType.valueOf(typeRaw.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            txType = TransactionType.INGRESO;
            validTxType = false;
        }

        return new ParseResult(date, branchCode, productCode, amount, txType, validDate, validAmount, validTxType);
    }

    private void flush(List<InsertRow> batch) {
        if (batch.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
            INSERT INTO transaction_record
            (tx_date, branch_id, product_id, raw_branch_code, raw_product_code, amount, tx_type, status, rejection_reason, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, batch, batch.size(), (PreparedStatement ps, InsertRow row) -> {
            ps.setDate(1, Date.valueOf(row.date));
            if (row.branchId == null) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, row.branchId);
            }
            if (row.productId == null) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, row.productId);
            }
            ps.setString(4, row.rawBranchCode);
            ps.setString(5, row.rawProductCode);
            ps.setBigDecimal(6, row.amount);
            ps.setString(7, row.txType.name());
            ps.setString(8, row.status.name());
            ps.setString(9, row.rejectionReason);
            ps.setTimestamp(10, Timestamp.valueOf(row.createdAt));
        });

        batch.clear();
    }

    private record InsertRow(LocalDate date,
                             Long branchId,
                             Long productId,
                             String rawBranchCode,
                             String rawProductCode,
                             BigDecimal amount,
                             TransactionType txType,
                             TransactionStatus status,
                             String rejectionReason,
                             LocalDateTime createdAt) {
    }

    private record ParseResult(LocalDate date,
                               String branchCode,
                               String productCode,
                               BigDecimal amount,
                               TransactionType txType,
                               boolean validDate,
                               boolean validAmount,
                               boolean validTxType) {
        com.coop.financialclose.dto.UploadLineDTO toUploadLineDTO() {
            return new com.coop.financialclose.dto.UploadLineDTO(date, branchCode, productCode, amount.toPlainString(), txType.name());
        }
    }
}
