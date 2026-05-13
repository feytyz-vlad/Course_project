package ua.com.kisit.course_project.Service;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Service
public class WayForPayService {

    private final String merchantAccount = "test_merch_n1";
    private final String merchantSecretKey = "flk3409refn54t54t*FNJRET";
    private final String merchantDomainName = "www.market.ua";

    public String getMerchantAccount() {
        return merchantAccount;
    }

    public String getMerchantDomainName() {
        return merchantDomainName;
    }

    public String generateSignature(String orderReference, long orderDate, String amount, String currency, List<String> productNames, List<String> productCounts, List<String> productPrices) {
        List<String> parts = new ArrayList<>();
        parts.add(merchantAccount);
        parts.add(merchantDomainName);
        parts.add(orderReference);
        parts.add(String.valueOf(orderDate));
        parts.add(amount);
        parts.add(currency);
        
        parts.addAll(productNames);
        parts.addAll(productCounts);
        parts.addAll(productPrices);

        String data = String.join(";", parts);
        System.out.println("WayForPay Signature Data String: [" + data + "]");
        String signature = hmacMd5(data, merchantSecretKey);
        System.out.println("WayForPay Generated Signature: [" + signature + "]");
        return signature;
    }

    private String hmacMd5(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacMD5");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacMD5");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC-MD5 signature", e);
        }
    }
}
