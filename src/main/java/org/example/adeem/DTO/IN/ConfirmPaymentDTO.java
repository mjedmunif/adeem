package org.example.adeem.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmPaymentDTO {

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;
}