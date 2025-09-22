package com.tendering.dto.request.auction;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateRequest {

    @NotBlank(message = "İhale başlığı boş olamaz")
    private String title;

    private String description;

    @NotNull(message = "İhale tipi belirtilmelidir")
    private String type; // PRODUCT, LIST, LIVESTREAM

    @NotNull(message = "Başlangıç fiyatı belirtilmelidir")
    @Positive(message = "Başlangıç fiyatı pozitif olmalıdır")
    private BigDecimal startPrice;

    private BigDecimal reservePrice;

    @Positive(message = "Minimum artış miktarı pozitif olmalıdır")
    private BigDecimal minBidIncrement;

    @NotNull(message = "Başlangıç zamanı belirtilmelidir")
    @Future(message = "Başlangıç zamanı gelecekte olmalıdır")
    private LocalDateTime startTime;

    @NotNull(message = "Bitiş zamanı belirtilmelidir")
    @Future(message = "Bitiş zamanı gelecekte olmalıdır")
    private LocalDateTime endTime;

    private String thumbnailUrl;

    private Boolean isFeatured = false;

    private Boolean isLive = false;

    private String streamUrl;

    private Boolean chatEnabled = true;
}
