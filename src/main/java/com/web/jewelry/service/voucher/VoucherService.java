package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.VoucherApplicabilityRequest;
import com.web.jewelry.dto.request.VoucherRequest;
import com.web.jewelry.dto.response.VoucherResponse;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Voucher;
import com.web.jewelry.model.VoucherApplicability;
import com.web.jewelry.repository.VoucherApplicabilityRepository;
import com.web.jewelry.repository.VoucherRepository;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService implements IVoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherApplicabilityRepository voucherApplicabilityRepository;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    @Override
    public List<Voucher> getVoucherByType(EVoucherType type) {
        return voucherRepository.findByType(type);
    }

    @Override
    public List<Voucher> getValidVouchers() {
        return voucherRepository.findByValidFromBeforeAndValidToAfter(LocalDateTime.now(), LocalDateTime.now());
    }

    @Transactional
    @Override
    public Voucher addVoucher(VoucherRequest request) {
        validateRequest(request);
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Voucher code already exists");
        }
        Voucher voucher = voucherRepository.save(Voucher.builder()
                .code(request.getCode())
                .name(request.getName())
                .discountRate(request.getDiscountRate())
                .minimumToApply(request.getMinimumToApply())
                .applyLimit(request.getApplyLimit())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .quantity(request.getQuantity())
                .limitUsePerCustomer(request.getLimitUsePerCustomer())
                .type(request.getType())
                .build());
        voucher.setVoucherApplicabilities(request.getApplicabilities().stream().
                    map(applicability -> voucherApplicabilityRepository.save(VoucherApplicability.builder()
                        .voucher(voucher)
                        .applicableObjectId(applicability.getApplicableObjectId())
                        .type(applicability.getType())
                        .build())
                    ).toList());
        return voucher;
    }

    @Transactional
    @Override
    public Voucher updateVoucher(Long id, VoucherRequest request) {
        validateRequest(request);
        Voucher oldVoucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        updateVoucherDetails(oldVoucher, request);
        Voucher voucher = voucherRepository.save(oldVoucher);
        voucher.setVoucherApplicabilities(updateVoucherApplicability(voucher, request.getApplicabilities()));
        return voucher;
    }

    private void updateVoucherDetails(Voucher voucher, VoucherRequest request) {
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDiscountRate(request.getDiscountRate());
        voucher.setMinimumToApply(request.getMinimumToApply());
        voucher.setApplyLimit(request.getApplyLimit());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidTo(request.getValidTo());
        voucher.setQuantity(request.getQuantity());
        voucher.setLimitUsePerCustomer(request.getLimitUsePerCustomer());
        voucher.setType(request.getType());
    }

    private List<VoucherApplicability> updateVoucherApplicability(Voucher voucher, List<VoucherApplicabilityRequest> requests) {
        voucherApplicabilityRepository.deleteAll(voucher.getVoucherApplicabilities());
        List<VoucherApplicability> newApplicability = requests.stream()
                .map(applicability -> VoucherApplicability.builder()
                        .voucher(voucher)
                        .applicableObjectId(applicability.getApplicableObjectId())
                        .type(applicability.getType())
                        .build())
                .toList();
        return voucherApplicabilityRepository.saveAll(newApplicability);
    }

    private void validateRequest(VoucherRequest request) {
        if (request.getValidFrom().isAfter(request.getValidTo())) {
            throw new BadRequestException("ValidFrom date cannot be after ValidTo date.");
        }
        if (request.getApplicabilities() == null || request.getApplicabilities().isEmpty()) {
            throw new BadRequestException("Voucher applicability must not be empty");
        }
    }

    @Override
    public void deleteVoucher(Long id) {
        Voucher voucher = getVoucherById(id);
        if (voucher.getValidFrom().isBefore(LocalDateTime.now()) && voucher.getValidTo().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot delete a valid voucher");
        }
        voucherRepository.delete(voucher);
    }


    @Override
    public List<Voucher> validateVouchers(OrderRequest request) {
        List<Voucher> vouchers = request.getVoucherCodes().stream()
                .map(this::getVoucherByCode)
                .toList();
        if (vouchers.size() > 2) {
            throw new BadRequestException("An order can have only two vouchers.");
        }
        int freeShipCount = 0;
        int promotionCount = 0;
        for (Voucher voucher : vouchers) {
            if (voucher.getType() == EVoucherType.FREESHIP) {
                freeShipCount++;
            } else if (voucher.getType() == EVoucherType.PROMOTION) {
                promotionCount++;
            }
        }
        if (freeShipCount > 1) {
            throw new BadRequestException("An order can have only one 'FREESHIP' voucher.");
        }
        if (promotionCount > 1) {
            throw new BadRequestException("An order can have only one 'PROMOTION' voucher.");
        }
        vouchers.forEach(voucher -> {
            checkValidDate(voucher);
            if (voucher.getMinimumToApply() != null && request.getTotalProductPrice() < voucher.getMinimumToApply()) {
                throw new BadRequestException("Minimum order value is not met");
            }
            voucher.getVoucherApplicabilities().stream().filter(voucherApplicability -> switch (voucherApplicability.getType()) {
                case PRODUCT -> request.getCartItems().stream().map(
                        cartItem -> cartItem.getProduct().getId()
                ).toList().contains(voucherApplicability.getApplicableObjectId());
                case CATEGORY -> request.getCartItems().stream().map(
                        cartItem -> cartItem.getProduct().getCategory() != null ? cartItem.getProduct().getCategory().getId() : Long.valueOf(-1)
                ).toList().contains(voucherApplicability.getApplicableObjectId());
                case COLLECTION -> request.getCartItems().stream().map(
                        cartItem -> cartItem.getProduct().getCollection() != null ? cartItem.getProduct().getCollection().getId() : Long.valueOf(-1)
                ).toList().contains(voucherApplicability.getApplicableObjectId());
                case CUSTOMER -> userService.getCurrentUser().getId().equals(voucherApplicability.getApplicableObjectId());
            }).findFirst().orElseThrow(() -> new BadRequestException("Invalid voucher applicability"));
        });
        return vouchers;
    }

    private void checkValidDate(Voucher voucher) {
        if (voucher.getValidFrom().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot use this voucher now");
        }
        if (voucher.getValidTo().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Voucher is expired");
        }
    }

    @Override
    public VoucherResponse convertToResponse(Voucher voucher) {
        return modelMapper.map(voucher, VoucherResponse.class);
    }

    @Override
    public List<VoucherResponse> convertToResponse(List<Voucher> vouchers) {
        return vouchers.stream().map(this::convertToResponse).toList();
    }
}
