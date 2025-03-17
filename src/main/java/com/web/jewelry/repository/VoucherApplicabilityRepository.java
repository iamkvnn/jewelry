package com.web.jewelry.repository;

import com.web.jewelry.enums.EVoucherApplicabilityType;
import com.web.jewelry.model.VoucherApplicability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherApplicabilityRepository extends JpaRepository<VoucherApplicability, Long> {
    List<VoucherApplicability> findByVoucherId(Long voucherId);
    List<VoucherApplicability> findByApplicableObjectIdAndType(Long objectId, EVoucherApplicabilityType applicabilityType);
}
