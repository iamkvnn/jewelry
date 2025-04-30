package com.web.jewelry.repository;

import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    boolean existsByCode(String code);
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByType(EVoucherType type);
    List<Voucher> findByValidFromBeforeAndValidToAfter(LocalDateTime validFrom, LocalDateTime validTo);

    @Query("SELECT v FROM Voucher v WHERE LOWER(CONCAT(v.code, ' ', v.name)) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Voucher> searchByCodeOrName(@Param("query") String query);

}
