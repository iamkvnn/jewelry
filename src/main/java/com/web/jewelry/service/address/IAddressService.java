package com.web.jewelry.service.address;

import com.web.jewelry.dto.request.AddressRequest;
import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAddressService {
    Address addAddress(Long userId, AddressRequest request);
    Address updateAddress(Long id, AddressRequest request);
    Address getAddressById(Long id);
    Address getCustomerDefaultAddress(Long customerId);
    Page<Address> getCustomerAddresses(Long customerId, Pageable pageable);
    void setDefaultAddress(Long customerId, Long addressId);
    void deleteAddress(Long id);
    AddressResponse convertToResponse(Address address);
    Page<AddressResponse> convertToResponse(Page<Address> addresses);
}
