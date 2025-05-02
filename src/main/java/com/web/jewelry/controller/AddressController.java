package com.web.jewelry.controller;

import com.web.jewelry.dto.request.AddressRequest;
import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.model.Address;
import com.web.jewelry.service.address.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/addresses")
public class AddressController {
    private final IAddressService addressService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAddressById(@PathVariable Long id) {
        Address address = addressService.getAddressById(id);
        AddressResponse addressResponse = addressService.convertToResponse(address);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponse));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<ApiResponse> getAddresses(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Address> addresses = addressService.getCustomerAddresses(PageRequest.of(page - 1, size));
        Page<AddressResponse> addressResponses = addressService.convertToResponse(addresses);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponses));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addAddress(@RequestBody AddressRequest request) {
        Address address = addressService.addAddress(request);
        AddressResponse addressResponse = addressService.convertToResponse(address);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponse));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateAddress(@PathVariable Long id, @RequestBody AddressRequest request) {
        Address address = addressService.updateAddress(id, request);
        AddressResponse addressResponse = addressService.convertToResponse(address);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponse));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/setDefault/{addressId}")
    public ResponseEntity<ApiResponse> setDefaultAddress(@PathVariable Long addressId) {
        addressService.setDefaultAddress(addressId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
