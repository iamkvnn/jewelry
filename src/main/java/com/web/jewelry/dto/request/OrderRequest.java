package com.web.jewelry.dto.request;

import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.dto.response.CartItemResponse;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class OrderRequest {
    private String id;
    private Set<CartItemResponse> cartItems;
    private Long totalProductPrice;
    private AddressResponse shippingAddress;
    private EShippingMethod shippingMethod;
    private Long shippingFee;
    private Long totalPrice;
    private List<String> voucherCodes;
    private Long freeShipDiscount;
    private Long promotionDiscount;
    private EPaymentMethod paymentMethod;
    private String note;
}
