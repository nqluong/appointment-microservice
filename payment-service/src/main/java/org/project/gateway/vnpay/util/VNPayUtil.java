package org.project.gateway.vnpay.util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class VNPayUtil {
    public String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public String hashAllFields(Map<String, String> fields, String hashSecret) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(hashSecret, sb.toString());
    }

    public String buildPaymentUrl(String baseUrl, Map<String, String> params, String hashSecret) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            return baseUrl + "?" + query.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error building payment URL", e);
        }
    }

    public String createQuerySecureHash(String requestId, String version, String command,
                                        String tmnCode, String txnRef, String transactionDate,
                                        String createDate, String ipAddr, String orderInfo,
                                        String hashSecret) {
        StringBuilder data = new StringBuilder();
        data.append(requestId).append("|")
                .append(version).append("|")
                .append(command).append("|")
                .append(tmnCode).append("|")
                .append(txnRef).append("|")
                .append(transactionDate).append("|")
                .append(createDate).append("|")
                .append(ipAddr).append("|")
                .append(orderInfo);

        return hmacSHA512(hashSecret, data.toString());
    }

    public String createRefundSecureHash(String requestId, String version, String command,
                                         String tmnCode, String transactionType, String txnRef,
                                         String amount, String transactionNo, String transactionDate, String createBy,
                                         String ipAddr, String createDate, String orderInfo,
                                         String hashSecret) {
        StringBuilder data = new StringBuilder();
        data.append(requestId).append("|")
                .append(version).append("|")
                .append(command).append("|")
                .append(tmnCode).append("|")
                .append(transactionType).append("|")
                .append(txnRef).append("|")
                .append(amount).append("|")
                .append(transactionNo).append("|")
                .append(transactionDate).append("|")
                .append(createBy).append("|")
                .append(createDate).append("|")
                .append(ipAddr).append("|")
                .append(orderInfo);

        return hmacSHA512(hashSecret, data.toString());
    }

    public String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(calendar.getTime());
    }

    public String generateRequestId() {
        return getCurrentDateTime();
    }

    public String extractTransactionDate(String transactionId) {
        try {
            String timestampStr = transactionId.substring(3, 16);
            long timestamp = Long.parseLong(timestampStr);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            cal.setTimeInMillis(timestamp);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            return formatter.format(cal.getTime());
        } catch (Exception e) {
            return getCurrentDateTime();
        }
    }


    public Map<String, String> parseResponseParams(String responseBody) {
        Map<String, String> params = new HashMap<>();
        if (responseBody != null && !responseBody.isEmpty()) {
            String[] pairs = responseBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    public boolean verifyCallback(Map<String, String> params, String hashSecret) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            return false;
        }

        Map<String, String> fieldsToVerify = new HashMap<>(params);
        fieldsToVerify.remove("vnp_SecureHashType");
        fieldsToVerify.remove("vnp_SecureHash");

        Map<String, String> encodedFields = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldsToVerify.entrySet()) {
            try {
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
                encodedFields.put(entry.getKey(), encodedValue);
            } catch (Exception e) {
                encodedFields.put(entry.getKey(), entry.getValue());
            }
        }

        String calculatedHash = hashAllFields(encodedFields, hashSecret);
        return calculatedHash.equals(receivedHash);
    }
}
