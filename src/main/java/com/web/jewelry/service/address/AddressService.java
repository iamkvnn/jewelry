package com.web.jewelry.service.address;

import com.web.jewelry.dto.request.AddressRequest;
import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Address;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.User;
import com.web.jewelry.repository.AddressRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class AddressService implements IAddressService{
    private final AddressRepository addressRepository;
    private final IUserService userService;
    private final INotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    public Address addAddress(AddressRequest request) {
        Customer customer = (Customer) userService.getCurrentUser();
        boolean isDefault = !addressRepository.existsByCustomerId(customer.getId());
        notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                .title("Bạn vừa thêm địa chỉ mới")
                .content("Bạn vừa thêm địa chỉ: " + request.getRecipientName() + "\n" +
                        "Số điện thoại: " + request.getRecipientPhone() + "\n" +
                        "Địa chỉ: " + request.getAddress() + "\n" +
                        "Tỉnh/Thành phố: " + request.getProvince() + "\n" +
                        "Quận/Huyện: " + request.getDistrict() + "\n" +
                        "Xã/Phường: " + request.getVillage())
                .customerIds(Set.of(customer.getId()))
                .build());
        return addressRepository.save(Address.builder()
                .customer(customer)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .village(request.getVillage())
                .address(request.getAddress())
                .isDefault(isDefault)
                .build());
    }

    @Override
    public Address updateAddress(Long id, AddressRequest request) {
        Long customerId = userService.getCurrentUser().getId();
        return addressRepository.findByIdAndCustomerId(id, customerId).map(address -> {
            address.setRecipientName(request.getRecipientName());
            address.setRecipientPhone(request.getRecipientPhone());
            address.setProvince(request.getProvince());
            address.setDistrict(request.getDistrict());
            address.setVillage(request.getVillage());
            address.setAddress(request.getAddress());
            return addressRepository.save(address);
        }).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    @Transactional
    @Override
    public void setDefaultAddress(Long addressId){
        Long customerId = userService.getCurrentUser().getId();
        addressRepository.findAllByCustomerId(customerId, Pageable.unpaged()).forEach(address -> {
            if (!address.getCustomer().getId().equals(customerId))
                throw new BadRequestException("Address not found");
            address.setDefault(address.getId().equals(addressId));
            addressRepository.save(address);
        });
    }

    @Override
    public Address getAddressById(Long id) {
        Address address = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        User user = userService.getCurrentUser();
        if (!address.getCustomer().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }
        return address;
    }

    @Override
    public Page<Address> getCustomerAddresses(Pageable pageable) {
        Long customerId = userService.getCurrentUser().getId();
        return addressRepository.findAllByCustomerId(customerId, pageable);
    }

    @Override
    public Address getCustomerDefaultAddress() {
        Long customerId = userService.getCurrentUser().getId();
        return addressRepository.findByCustomerIdAndIsDefault(customerId, true).orElseThrow(() -> new ResourceNotFoundException("Default address not found"));
    }

    @Override
    public void deleteAddress(Long id) {
        Long customerId = userService.getCurrentUser().getId();
        addressRepository.findByIdAndCustomerId(id, customerId).ifPresentOrElse(address -> {
            if (address.isDefault()) {
                throw new BadRequestException("Cannot delete default address");
            }
            addressRepository.delete(address);
            notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                    .title("Bạn vừa xóa một địa chỉ")
                    .content("Bạn vừa xóa địa chỉ: " + address.getRecipientName() + "\n" +
                            "Số điện thoại: " + address.getRecipientPhone() + "\n" +
                            "Địa chỉ: " + address.getAddress() + "\n" +
                            "Tỉnh/Thành phố: " + address.getProvince() + "\n" +
                            "Quận/Huyện: " + address.getDistrict() + "\n" +
                            "Xã/Phường: " + address.getVillage())
                    .customerIds(Set.of(customerId))
                    .build());
        }, () -> {
            throw new ResourceNotFoundException("Address not found");
        });
    }

    @Override
    public AddressResponse convertToResponse(Address address) {
        return modelMapper.map(address, AddressResponse.class);
    }

    @Override
    public Page<AddressResponse> convertToResponse(Page<Address> addresses) {
        return addresses.map(this::convertToResponse);
    }
}
