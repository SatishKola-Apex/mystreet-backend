package com.mystreet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class OrderRequestDto {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    @NotEmpty(message = "Order items cannot be empty & must contain at least one item")
    @Valid
    private List<OrderItemsRequestDto> orderItems;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<OrderItemsRequestDto> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemsRequestDto> orderItems) {
        this.orderItems = orderItems;
    }
}
