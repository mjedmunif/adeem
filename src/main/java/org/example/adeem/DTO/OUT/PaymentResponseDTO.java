package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.adeem.Enums.PaymentStatus;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long paymentId;
    private BigDecimal amount;
    private String transactionReference;
    private PaymentStatus status;
    private String checkoutUrl; // رابط بوابة الدفع - المستخدم يروح له يدفع
}