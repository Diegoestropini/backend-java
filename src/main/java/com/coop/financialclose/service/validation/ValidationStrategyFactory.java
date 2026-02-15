package com.coop.financialclose.service.validation;

import com.coop.financialclose.domain.enums.ProductType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidationStrategyFactory {

    private final Map<ProductType, TransactionValidationStrategy> strategies = new EnumMap<>(ProductType.class);

    public ValidationStrategyFactory(List<TransactionValidationStrategy> strategyList) {
        for (TransactionValidationStrategy strategy : strategyList) {
            strategies.put(strategy.supports(), strategy);
        }
    }

    public TransactionValidationStrategy resolve(ProductType type) {
        return strategies.get(type);
    }
}
