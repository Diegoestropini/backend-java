package com.coop.financialclose.repository;

import com.coop.financialclose.domain.entity.TransactionRecord;
import com.coop.financialclose.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    @Query("""
        SELECT t FROM TransactionRecord t
        WHERE (:branchCode IS NULL OR t.rawBranchCode = :branchCode)
          AND (:date IS NULL OR t.txDate = :date)
          AND (:status IS NULL OR t.status = :status)
        """)
    Page<TransactionRecord> search(
        @Param("branchCode") String branchCode,
        @Param("date") LocalDate date,
        @Param("status") TransactionStatus status,
        Pageable pageable
    );
}
