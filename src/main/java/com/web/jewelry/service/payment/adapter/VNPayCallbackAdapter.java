package com.web.jewelry.service.payment.adapter;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayCallbackAdapter implements Map<String, String> {
    private final Map<String, String> fields;

    public VNPayCallbackAdapter(HttpServletRequest request) {
        fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName;
            String fieldValue;
            fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return fields.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return fields.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return fields.get(key);
    }

    @Override
    public String put(String key, String value) {
        return fields.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return fields.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        fields.putAll(m);
    }

    @Override
    public void clear() {
        fields.clear();
    }

    @Override
    public Set<String> keySet() {
        return fields.keySet();
    }

    @Override
    public Collection<String> values() {
        return fields.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return fields.entrySet();
    }
}
