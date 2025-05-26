package com.web.jewelry.config;

import com.web.jewelry.dto.request.MomoPaymentRequest;
import lombok.Getter;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Component
public class MomoPaymentConfig {

    @Value("${MoMo.secret}")
    private String secretKey;
    @Value("${MoMo.url}")
    private String momoURL;
    @Value("${MoMo.partnerCode}")
    private String partnerCode;
    @Value("${MoMo.accessKey}")
    private String accessKey;

    public String generateSignature(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
    public String sendToMomo(MomoPaymentRequest request){
        String jsonRequest = "{"
                + "\"partnerCode\":\"" + request.getPartnerCode() + "\","
                + "\"accessKey\":\"" + request.getAccessKey() + "\","
                + "\"requestId\":\"" + request.getRequestId() + "\","
                + "\"amount\":\"" + request.getAmount() + "\","
                + "\"orderId\":\"" + request.getOrderId() + "\","
                + "\"orderInfo\":\"" + request.getOrderInfo() + "\","
                + "\"redirectUrl\":\"" + request.getReturnUrl() + "\","
                + "\"ipnUrl\":\"" + request.getNotifyUrl() + "\","
                + "\"extraData\":\"" + request.getExtraData() + "\","
                + "\"requestType\":\"" + request.getRequestType().getValue() + "\","
                + "\"signature\":\"" + request.getSignature() + "\""
                + "}";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonRequest);
        Request momoRequest = new Request.Builder()
                .url(momoURL)
                .post(body)
                .build();

        try (Response response = client.newCall(momoRequest).execute()) {
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public MomoPaymentRequest createPaymentRequest(String orderId, String amount, String orderInfo,
                                                          String returnUrl, String notifyUrl, String extraData, ERequestType requestType) throws NoSuchAlgorithmException, InvalidKeyException {
        String requestId = "REQ" + orderId;
        String requestRawData = "accessKey=" + accessKey + "&" +
                "amount=" + amount + "&" +
                "extraData=" + extraData + "&" +
                "ipnUrl=" + notifyUrl + "&" +
                "orderId=" + orderId + "&" +
                "orderInfo=" + orderInfo + "&" +
                "partnerCode=" + partnerCode + "&" +
                "redirectUrl=" + returnUrl + "&" +
                "requestId=" + requestId + "&" +
                "requestType=" + requestType.getValue();
        String signature = generateSignature(requestRawData, secretKey);
        return new MomoPaymentRequest(partnerCode, accessKey, requestId, amount, orderId, orderInfo, returnUrl,
                notifyUrl, extraData, requestType, signature);
    }

    public boolean isValidSignature(Map<String, String> response) {
        try {
            String signatureFromMoMo = response.get("signature");
            response.remove("signature");
            // Sắp xếp key theo thứ tự alphabet
            StringBuilder rawData = new StringBuilder();
            rawData.append("accessKey").append("=")
                    .append(accessKey)
                    .append("&");
            response.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Sắp xếp key theo thứ tự alphabet
                    .forEach(entry -> {
                        String key = entry.getKey();
                        String value = entry.getValue() != null ? entry.getValue().trim() : "";
                        rawData.append(key).append("=")
                                .append(value)
                                .append("&");
                    });

            if (!rawData.isEmpty()) {
                rawData.setLength(rawData.length() - 1);
            }
            // Tạo signature mới
            String generatedSignature = generateSignature(rawData.toString(), secretKey);
            return generatedSignature.equals(signatureFromMoMo);
        } catch (Exception e) {
            return false;
        }
    }

    @Getter
    public enum ERequestType {
        PAY_WITH_ATM("payWithATM");
        private final String value;
        ERequestType(String value) {
            this.value = value;
        }
    }
}
