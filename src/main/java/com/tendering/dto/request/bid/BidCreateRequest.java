package com.tendering.dto.request.bid;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidCreateRequest {

    @NotNull(message = "İhale ID'si boş olamaz")
    private UUID auctionPublicId;

    @NotNull(message = "Teklif miktarı belirtilmelidir")
    @Positive(message = "Teklif miktarı pozitif olmalıdır")
    private BigDecimal amount;
}
