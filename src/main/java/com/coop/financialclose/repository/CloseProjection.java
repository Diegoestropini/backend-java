package com.coop.financialclose.repository;

import java.math.BigDecimal;

public interface CloseProjection {
    String getBranchCode();

    String getBranchName();

    String getProductCode();

    String getProductName();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();

    BigDecimal getNetTotal();

    Long getTxCount();
}
