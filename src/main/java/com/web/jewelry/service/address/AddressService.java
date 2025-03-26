package com.web.jewelry.service.address;

import com.web.jewelry.dto.request.AddressRequest;
import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Address;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.User;
import com.web.jewelry.repository.AddressRepository;
import com.web.jewelry.service.user.IUserService;
import com.web.jewelry.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AddressService implements IAddressService{
    private final AddressRepository addressRepository;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Address addAddress(AddressRequest request) {
        User user = userService.getCurrentUser();
        if(user != null) {
            Customer customer = (Customer) user;
            boolean isDefault = !addressRepository.existsByCustomerId(user.getId());
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
        throw new BadRequestException("User not found");
    }

    @Override
    public Address updateAddress(Long id, AddressRequest request) {
        return addressRepository.findById(id).map(address -> {
            address.setRecipientName(request.getRecipientName());
            address.setRecipientPhone(request.getRecipientPhone());
            address.setProvince(request.getProvince());
            address.setDistrict(request.getDistrict());
            address.setVillage(request.getVillage());
            address.setAddress(request.getAddress());
            return addressRepository.save(address);
        }).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    @Override
    public void setDefaultAddress(Long customerId, Long addressId){
        addressRepository.findAllByCustomerId(customerId, Pageable.unpaged()).forEach(address -> {
            address.setDefault(address.getId().equals(addressId));
            addressRepository.save(address);
        });
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    @Override
    public Page<Address> getCustomerAddresses(Pageable pageable) {
        User user = userService.getCurrentUser();
        if(user != null){
            return addressRepository.findAllByCustomerId(user.getId(), pageable);
        }
        throw new BadRequestException("User not found");
    }

    @Override
    public Address getCustomerDefaultAddress(Long customerId) {
        return addressRepository.findByCustomerIdAndIsDefault(customerId, true).orElseThrow(() -> new ResourceNotFoundException("Default address not found"));
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.findById(id).ifPresentOrElse(address -> {
            if (address.isDefault()) {
                throw new BadRequestException("Cannot delete default address");
            }
            addressRepository.delete(address);
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
