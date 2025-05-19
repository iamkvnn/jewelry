package com.web.jewelry.service.order;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.model.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderContext {
    private OrderRequest orderRequest;
    private Cart cart;
    private Order order;
    private List<Voucher> vouchers;
    private List<OrderItem> orderItems;
}
