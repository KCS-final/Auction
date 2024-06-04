package com.kcs3.auction.dto;

import com.kcs3.auction.entity.AuctionProgressItem;
import lombok.Builder;


@Builder
public record HotItemsDto(
        Long itemId,
        String itemTitle,
        String category,
        String thumbnail,
        int startPrice,
        Integer buyNowPrice

) {

    public static HotItemsDto fromHotEntity(AuctionProgressItem auctionProgressItem) {
        return HotItemsDto.builder()
                .itemId(auctionProgressItem.getItem().getItemId())
                .itemTitle(auctionProgressItem.getItemTitle())
                .category(auctionProgressItem.getItem().getCategory().getCategory())
                .thumbnail(auctionProgressItem.getThumbnail())
                .startPrice(auctionProgressItem.getStartPrice())
                .buyNowPrice(auctionProgressItem.getBuyNowPrice())
                .build();
    }
}
