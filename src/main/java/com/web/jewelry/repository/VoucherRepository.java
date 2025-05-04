package com.web.jewelry.repository;

import com.web.jewelry.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    Page<Voucher> findByValidFromBeforeAndValidToAfter(LocalDateTime validFrom, LocalDateTime validTo, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE LOWER(CONCAT(v.code, ' ', v.name)) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Voucher> searchByCodeOrName(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Voucher v JOIN v.orders o WHERE v.code = :code AND o.customerId = :customerId")
    Long countUsedByVoucherCodeAndCustomerId(String code, Long customerId);

    @Query("""
    SELECT v
    FROM Voucher v
    WHERE :now BETWEEN v.validFrom AND v.validTo
      AND (:totalPrice >= v.minimumToApply OR v.minimumToApply IS NULL)
      AND (SELECT COUNT(ov)
           FROM OrderVoucher ov
           WHERE ov.voucher.id = v.id AND ov.customerId = :customerId) < v.limitUsePerCustomer
      AND v.quantity > 0
    """)
    Page<Voucher> findValidVoucherForOrder(LocalDateTime now, Long totalPrice, Long customerId, Pageable pageable);
}
