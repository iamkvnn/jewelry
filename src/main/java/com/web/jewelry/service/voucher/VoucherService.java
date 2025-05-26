package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.VoucherApplicabilityRequest;
import com.web.jewelry.dto.request.VoucherRequest;
import com.web.jewelry.dto.response.VoucherResponse;
import com.web.jewelry.enums.EVoucherApplicabilityType;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Voucher;
import com.web.jewelry.model.VoucherApplicability;
import com.web.jewelry.repository.VoucherApplicabilityRepository;
import com.web.jewelry.repository.VoucherRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VoucherService implements IVoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherApplicabilityRepository voucherApplicabilityRepository;
    private final CompositeVoucherValidator compositeVoucherValidator;
    private final INotificationService notificationService;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Page<Voucher> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }

    @Override
    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    @Override
    public Page<Voucher> searchVouchers(String query, Pageable pageable) {
        return voucherRepository.searchByCodeOrName(query, pageable);
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    @Override
    public Page<Voucher> getValidVouchers(Pageable pageable) {
        return voucherRepository.findByValidFromBeforeAndValidToAfter(LocalDateTime.now(), LocalDateTime.now(), pageable);
    }

    @Transactional
    @Override
    public Voucher addVoucher(VoucherRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        validateRequest(request);
        Voucher existingVoucher = voucherRepository.findByCode(request.getCode()).orElse(null);
        if (existingVoucher != null && existingVoucher.getValidTo().isAfter(LocalDateTime.now())) {
            throw new AlreadyExistException("A valid voucher code already exists");
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
        if (voucher.getType() == EVoucherType.FREESHIP) {
            notificationService.sendNotificationToAllCustomer(
                    NotificationRequest.builder()
                            .title("Bạn nhận được 1 voucher mới")
                            .content("Bạn vừa nhận được 1 voucher FREESHIP mới với mã " + voucher.getCode() +
                                    ".\n Giảm tới " + voucher.getApplyLimit() + " cho đơn hàng từ " +  voucher.getMinimumToApply() +
                                    ".\n Mã có hiệu lực từ " + voucher.getValidFrom().format(formatter) + " - " + voucher.getValidTo().format(formatter) +
                                    ".\n Bắt đầu mua sắm nào!")
                            .build());
        }
        Set<Long> cusId = new HashSet<>();
        voucher.getVoucherApplicabilities().stream().filter(
                voucherApplicability -> voucherApplicability.getType() == EVoucherApplicabilityType.CUSTOMER
        ).forEach(voucherApplicability -> cusId.add(voucherApplicability.getApplicableObjectId()));
        if (!cusId.isEmpty()) {
            notificationService.sendNotificationToSpecificCustomer(
                    NotificationRequest.builder()
                            .title("Bạn nhận được 1 voucher mới")
                            .content("Bạn vừa nhận được 1 voucher PROMOTION mới với mã " + voucher.getCode() +
                                    ".\n Giảm tới " + voucher.getApplyLimit() + " cho đơn hàng từ " +  voucher.getMinimumToApply() +
                                    ".\n Mã có hiệu lực từ " + voucher.getValidFrom().format(formatter) + " - " + voucher.getValidTo().format(formatter) +
                                    ".\n Bắt đầu mua sắm nào!")
                            .customerIds(cusId)
                            .build());
        }
        return voucher;
    }

    @Transactional
    @Override
    public Voucher updateVoucher(Long id, VoucherRequest request) {
        validateRequest(request);
        Voucher oldVoucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        Voucher existingVoucher = voucherRepository.findByCode(request.getCode()).orElse(null);
        if (existingVoucher != null && !oldVoucher.getCode().equals(existingVoucher.getCode()) && existingVoucher.getValidTo().isAfter(LocalDateTime.now())) {
            throw new AlreadyExistException("A valid voucher code already exists");
        }
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
        compositeVoucherValidator.isValid(vouchers, request);
        return vouchers;
    }



    @Override
    public VoucherResponse convertToResponse(Voucher voucher) {
        return modelMapper.map(voucher, VoucherResponse.class);
    }

    @Override
    public Page<VoucherResponse> convertToResponse(Page<Voucher> vouchers) {
        return vouchers.map(this::convertToResponse);
    }

    @Override
    public Page<Voucher> getValidVouchersForOrder(OrderRequest request, Pageable pageable) {
        Long customerId = userService.getCurrentUser().getId();
        Long totalProductPrice = request.getTotalProductPrice();
        return voucherRepository.findValidVoucherForOrder(LocalDateTime.now(), totalProductPrice, customerId, pageable);
    }

    @Override
    public void decreaseVoucherQuantity(Long id) {
        Voucher voucher = getVoucherById(id);
        if (voucher.getQuantity() <= 0) {
            throw new BadRequestException("Voucher is out of stock");
        }
        voucher.setQuantity(voucher.getQuantity() - 1);
        voucherRepository.save(voucher);
    }
}
