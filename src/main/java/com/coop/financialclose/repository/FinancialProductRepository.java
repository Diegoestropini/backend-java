package com.coop.financialclose.repository;

import com.coop.financialclose.domain.entity.FinancialProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialProductRepository extends JpaRepository<FinancialProduct, Long> {
    Optional<FinancialProduct> findByCode(String code);
}
