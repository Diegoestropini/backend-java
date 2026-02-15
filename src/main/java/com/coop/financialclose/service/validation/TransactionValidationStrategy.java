package com.coop.financialclose.service.validation;

import com.coop.financialclose.domain.enums.ProductType;
import com.coop.financialclose.dto.UploadLineDTO;

public interface TransactionValidationStrategy {
    ProductType supports();

    ValidationResult validate(UploadLineDTO line);
}
