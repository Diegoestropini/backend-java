package com.coop.financialclose.service.validation;

import com.coop.financialclose.domain.enums.ProductType;
import com.coop.financialclose.dto.UploadLineDTO;
import org.springframework.stereotype.Component;

@Component
public class SavingsValidationStrategy implements TransactionValidationStrategy {
    @Override
    public ProductType supports() {
        return ProductType.AHORRO;
    }

    @Override
    public ValidationResult validate(UploadLineDTO line) {
        return ValidationResult.ok();
    }
}
