package com.web.jewelry.service.address;

import com.web.jewelry.dto.request.AddressRequest;
import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Address;
import com.web.jewelry.model.Customer;
import com.web.jewelry.repository.AddressRepository;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AddressService implements IAddressService{
    private final AddressRepository addressRepository;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Address addAddress(Long customerId, AddressRequest request) {
        Customer customer = (Customer) userService.getCustomerById(customerId);
        return addressRepository.save(Address.builder()
                .customer(customer)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .village(request.getVillage())
                .address(request.getAddress())
                .isDefault(false)
                .build());
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
    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    @Override
    public Page<Address> getCustomerAddresses(Long customerId, Pageable pageable) {
        return addressRepository.findAllByCustomerId(customerId, pageable);
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.findById(id).ifPresentOrElse(addressRepository::delete, () -> {
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
