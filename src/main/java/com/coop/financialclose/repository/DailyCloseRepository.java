package com.coop.financialclose.repository;

import com.coop.financialclose.domain.entity.DailyClose;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyCloseRepository extends JpaRepository<DailyClose, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM daily_close WHERE close_date = :date", nativeQuery = true)
    int deleteByCloseDate(@Param("date") LocalDate date);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO daily_close (close_date, branch_id, product_id, total_income, total_expense, net_total, tx_count, updated_at)
        SELECT
            :date AS close_date,
            t.branch_id,
            t.product_id,
            COALESCE(SUM(CASE WHEN t.tx_type = 'INGRESO' THEN t.amount ELSE 0 END), 0) AS total_income,
            COALESCE(SUM(CASE WHEN t.tx_type = 'EGRESO' THEN t.amount ELSE 0 END), 0) AS total_expense,
            COALESCE(SUM(CASE WHEN t.tx_type = 'INGRESO' THEN t.amount ELSE -t.amount END), 0) AS net_total,
            COUNT(*) AS tx_count,
            NOW() AS updated_at
        FROM transaction_record t
        WHERE t.tx_date = :date
          AND t.status = 'CONCILIADA'
          AND t.branch_id IS NOT NULL
          AND t.product_id IS NOT NULL
        GROUP BY t.branch_id, t.product_id
        """, nativeQuery = true)
    int insertAggregatedForDate(@Param("date") LocalDate date);

    @Query(value = """
        SELECT
            b.code AS branchCode,
            b.name AS branchName,
            p.code AS productCode,
            p.name AS productName,
            dc.total_income AS totalIncome,
            dc.total_expense AS totalExpense,
            dc.net_total AS netTotal,
            dc.tx_count AS txCount
        FROM daily_close dc
        JOIN branch b ON b.id = dc.branch_id
        JOIN financial_product p ON p.id = dc.product_id
        WHERE dc.close_date = :date
          AND (:branchCode IS NULL OR b.code = :branchCode)
          AND (:productCode IS NULL OR p.code = :productCode)
        ORDER BY b.code, p.code
        """, nativeQuery = true)
    List<CloseProjection> findSummary(@Param("date") LocalDate date,
                                      @Param("branchCode") String branchCode,
                                      @Param("productCode") String productCode);
}
