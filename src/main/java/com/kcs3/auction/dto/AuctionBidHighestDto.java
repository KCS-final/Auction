package com.kcs3.auction.dto;

import lombok.Builder;

@Builder
public record AuctionBidHighestDto(
        Long auctionProgressItemId,
        Long userId,
        String maxPersonNickName,
        Integer maxPrice
) {
}