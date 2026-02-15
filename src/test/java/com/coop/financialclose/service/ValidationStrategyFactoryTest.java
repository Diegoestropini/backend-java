package com.coop.financialclose.service;

import com.coop.financialclose.domain.enums.ProductType;
import com.coop.financialclose.service.validation.CreditValidationStrategy;
import com.coop.financialclose.service.validation.InvestmentValidationStrategy;
import com.coop.financialclose.service.validation.SavingsValidationStrategy;
import com.coop.financialclose.service.validation.ValidationStrategyFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidationStrategyFactoryTest {

    @Test
    void shouldResolveEachProductType() {
        ValidationStrategyFactory factory = new ValidationStrategyFactory(List.of(
            new SavingsValidationStrategy(),
            new CreditValidationStrategy(),
            new InvestmentValidationStrategy()
        ));

        assertNotNull(factory.resolve(ProductType.AHORRO));
        assertNotNull(factory.resolve(ProductType.CREDITO));
        assertNotNull(factory.resolve(ProductType.INVERSION));
        assertEquals(ProductType.CREDITO, factory.resolve(ProductType.CREDITO).supports());
    }
}
