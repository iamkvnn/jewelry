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
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/addresses")
public class AddressController {
    private final IAddressService addressService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAddressById(@PathVariable Long id) {
        Address address = addressService.getAddressById(id);
        AddressResponse addressResponse = addressService.convertToResponse(address);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponse));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getAddresses(@PathVariable Long customerId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Address> addresses = addressService.getCustomerAddresses(customerId, PageRequest.of(page - 1, size));
        Page<AddressResponse> addressResponses = addressService.convertToResponse(addresses);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponses));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addAddress(@RequestParam Long customerId, @RequestBody AddressRequest request) {
        Address address = addressService.addAddress(customerId, request);
        AddressResponse addressResponse = addressService.convertToResponse(address);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponse));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateAddress(@PathVariable Long id, @RequestBody AddressRequest request) {
        Address address = addressService.updateAddress(id, request);
        AddressResponse addressResponse = addressService.convertToResponse(address);
        return ResponseEntity.ok(new ApiResponse("200", "Success", addressResponse));
    }

    @PutMapping("/setDefault/{customerId}/{addressId}")
    public ResponseEntity<ApiResponse> setDefaultAddress(@PathVariable Long customerId, @PathVariable Long addressId) {
        addressService.setDefaultAddress(customerId, addressId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
