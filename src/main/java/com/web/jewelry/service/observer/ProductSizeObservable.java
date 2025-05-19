package com.web.jewelry.service.observer;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductSizeObservable {
    private final List<ProductSizeListener> listeners = new ArrayList<>();

    public void addObserver(ProductSizeListener listener) {
        listeners.add(listener);
    }

    public void removeObserver(ProductSizeListener listener) {
        listeners.remove(listener);
    }

    public void notifyObservers(Long productSizeId) {
        for (ProductSizeListener listener : listeners) {
            listener.onProductSizeDeleted(productSizeId);
        }
    }
}
