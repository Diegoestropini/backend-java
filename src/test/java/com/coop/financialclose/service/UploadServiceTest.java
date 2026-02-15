package com.coop.financialclose.service;

import com.coop.financialclose.domain.entity.Branch;
import com.coop.financialclose.domain.entity.FinancialProduct;
import com.coop.financialclose.domain.enums.ProductType;
import com.coop.financialclose.dto.UploadResultDTO;
import com.coop.financialclose.repository.BranchRepository;
import com.coop.financialclose.repository.FinancialProductRepository;
import com.coop.financialclose.service.batch.UploadService;
import com.coop.financialclose.service.validation.TransactionValidationStrategy;
import com.coop.financialclose.service.validation.ValidationResult;
import com.coop.financialclose.service.validation.ValidationStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private BranchRepository branchRepository;
    @Mock
    private FinancialProductRepository financialProductRepository;
    @Mock
    private ValidationStrategyFactory validationStrategyFactory;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UploadService uploadService;

    @Test
    void shouldRejectMissingBranchAndContinueBatch() throws IOException {
        Branch branch = new Branch();
        branch.setCode("SUC001");
        setId(branch, 1L);

        FinancialProduct product = new FinancialProduct();
        product.setCode("PROD001");
        product.setType(ProductType.AHORRO);
        setId(product, 2L);

        when(branchRepository.findAll()).thenReturn(List.of(branch));
        when(financialProductRepository.findAll()).thenReturn(List.of(product));
        TransactionValidationStrategy strategy = new TransactionValidationStrategy() {
            @Override
            public ProductType supports() { return ProductType.AHORRO; }
            @Override
            public ValidationResult validate(com.coop.financialclose.dto.UploadLineDTO line) { return ValidationResult.ok(); }
        };
        when(validationStrategyFactory.resolve(ProductType.AHORRO)).thenReturn(strategy);
        doReturn(new int[][]{{1}}).when(jdbcTemplate).batchUpdate(anyString(), any(List.class), anyInt(), any());

        String csv = "fecha,codigoSucursal,codigoProducto,monto,tipo\n" +
            "2026-02-14,SUC001,PROD001,100.50,INGRESO\n" +
            "2026-02-14,SUC404,PROD001,50.00,EGRESO\n";
        MockMultipartFile file = new MockMultipartFile("file", "tx.csv", "text/csv", csv.getBytes());

        UploadResultDTO result = uploadService.process(file);

        assertEquals(2, result.processedRows());
        assertEquals(1, result.acceptedRows());
        assertEquals(1, result.rejectedRows());
        assertEquals(1L, result.errorsByReason().get("BRANCH_NOT_FOUND"));
    }

    @Test
    void shouldRejectInvalidAmountAndType() throws IOException {
        Branch branch = new Branch();
        branch.setCode("SUC001");
        setId(branch, 1L);

        FinancialProduct product = new FinancialProduct();
        product.setCode("PROD001");
        product.setType(ProductType.AHORRO);
        setId(product, 2L);

        when(branchRepository.findAll()).thenReturn(List.of(branch));
        when(financialProductRepository.findAll()).thenReturn(List.of(product));
        doReturn(new int[][]{{1}}).when(jdbcTemplate).batchUpdate(anyString(), any(List.class), anyInt(), any());

        String csv = "2026-02-14,SUC001,PROD001,-10,INGRESO\n" +
            "2026-02-14,SUC001,PROD001,10,INVALIDO\n";
        MockMultipartFile file = new MockMultipartFile("file", "tx.csv", "text/csv", csv.getBytes());

        UploadResultDTO result = uploadService.process(file);

        assertEquals(0, result.acceptedRows());
        assertEquals(2, result.rejectedRows());
        assertEquals(1L, result.errorsByReason().get("INVALID_AMOUNT"));
        assertEquals(1L, result.errorsByReason().get("INVALID_TX_TYPE"));
    }

    private static void setId(Object target, Long id) {
        try {
            var field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
