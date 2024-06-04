package com.kcs3.auction.service;


import com.kcs3.auction.document.ItemDocument;
import com.kcs3.auction.dto.HotItemListDto;
import com.kcs3.auction.dto.HotItemsDto;
import com.kcs3.auction.dto.ProgressItemListDto;
import com.kcs3.auction.dto.ProgressItemsDto;
import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.repository.AuctionInfoRepository;
import com.kcs3.auction.repository.ItemRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class ItemListService {

    private final ItemRepository itemRepository;
    private final AuctionInfoRepository auctionInfoRepository;


    private final RedisTemplate<String, HotItemsDto> redisTemplate;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public ProgressItemListDto getSearchItems(String keyword) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("itemTitle")
                                .query(keyword)
                        )
                )
                .build();

        SearchHits<ItemDocument> searchHits = elasticsearchOperations.search(query, ItemDocument.class);

        List<ItemDocument> items = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .toList();

        return null;
    }

    /**
     * 경매진행중인 아이템 목록 조회 - 서비스 로직
     * 경매완료된 아이템 목록 조회 - 서비스 로직
     * 모든 아이템 목록 조회 - 서비스 로직
     */

    public ProgressItemListDto getProgressItems(String category, Integer method, String region, String status, Pageable pageable) {
        List<ProgressItemsDto> itemtemDtoList = new ArrayList<>();

        if ("progress".equals(status)) {
            Slice<AuctionProgressItem> progressItems = itemRepository.findByProgressItemWithLocationAndMethodAndRegion(category, method, region, pageable);
            for (AuctionProgressItem progressItem : progressItems) {
                ProgressItemsDto progressItemsDto = ProgressItemsDto.fromProgressEntity(progressItem);
                itemtemDtoList.add(progressItemsDto);
            }
        } else if ("completion".equals(status)) {
            Slice<AuctionCompleteItem> completionItems = itemRepository.findByCompleteItemWithLocationAndMethodAndRegion(category, method, region, pageable);
            for (AuctionCompleteItem completionItem : completionItems) {
                ProgressItemsDto progressItemsDto = ProgressItemsDto.fromCompletionEntity(completionItem);
                itemtemDtoList.add(progressItemsDto);
            }
        } else {
            Slice<AuctionProgressItem> allItems = itemRepository.findByProgressItemWithLocationAndMethodAndRegion(category, method, region, pageable);
            for (AuctionProgressItem progressItems : allItems) {
                ProgressItemsDto progressItemsDto = ProgressItemsDto.fromProgressEntity(progressItems);
                itemtemDtoList.add(progressItemsDto);
            }

            Slice<AuctionCompleteItem> completionItems = itemRepository.findByCompleteItemWithLocationAndMethodAndRegion(category, method, region, pageable);
            for (AuctionCompleteItem completionItem : completionItems) {
                ProgressItemsDto progressItemsDto = ProgressItemsDto.fromCompletionEntity(completionItem);
                itemtemDtoList.add(progressItemsDto);
            }




        } //else

        return ProgressItemListDto.builder()
                .progressItemListDto(itemtemDtoList)
                .build();
    }



    /**
     * Hot 아이템 목록 Redis 조회 서비스 로직
     */
    public HotItemListDto getHotItems(){

        List<HotItemsDto> hotItemsDtos = new ArrayList<>();

        for (int i=1;i<=10;i++) {
            hotItemsDtos.add( redisTemplate.opsForValue().get("hot_item:"+i));
        }

        return HotItemListDto.builder()
                .hotItemListDtos(hotItemsDtos)
                .build();
    }

    /**
     *  New 아이템 목록 Redis 조회 서비스 로직
     */
    public HotItemListDto getNewItems(){

        List<HotItemsDto> newItemsDtos = new ArrayList<>();

        for (int i=1;i<=10;i++) {
            newItemsDtos.add( redisTemplate.opsForValue().get("new_item:"+i));
        }

        return HotItemListDto.builder()
                .hotItemListDtos(newItemsDtos)
                .build();
    }



    /**
     * 핫아이템 Redis 저장 서비스 로직
     */
    public void saveHotItems() {

        // 최근 인기 아이템의 itemId 리스트 조회
        Pageable pageable = PageRequest.of(0, 10);
        List<Long> hotItemIdList = auctionInfoRepository.findTop10ItemIds(pageable);
        List<AuctionProgressItem> hotItemList = new ArrayList<>();


        for (Long itemId : hotItemIdList) {
            AuctionProgressItem hotItem = itemRepository.findByHotItemList(itemId);
            hotItemList.add(hotItem);
        }



        // 조회된 AuctionProgressItem을 HotItemsDto로 변환
        List<HotItemsDto> hotItemsDtos = hotItemList
                .stream()
                .map(HotItemsDto::fromHotEntity)
                .collect(Collectors.toList());


        int i = 1;
        for (HotItemsDto hotItemsDto : hotItemsDtos) {
            redisTemplate.opsForValue().set("hot_item:" + i, hotItemsDto);
            i++;
        }


    }



    /**
     * 신규 아이템 Redis 저장 서비스 로직
     */
    public void saveNewItems() {

        // 신규 아이템의 itemId 리스트 조회
        Pageable pageable = PageRequest.of(0, 10);
        List<Long> newItemIdList = auctionInfoRepository.findNew10ItemIds(pageable);
        List<AuctionProgressItem> newItemList = new ArrayList<>();


        for (Long itemId : newItemIdList) {
            AuctionProgressItem newItem = itemRepository.findByHotItemList(itemId);
            newItemList.add(newItem);
        }

        // 조회된 AuctionProgressItem을 NewItemsDto로 변환
        List<HotItemsDto> hotItemsDtos = newItemList
                .stream()
                .map(HotItemsDto::fromHotEntity)
                .collect(Collectors.toList());


        int i = 1;
        for (HotItemsDto hotItemsDto : hotItemsDtos) {
            redisTemplate.opsForValue().set("new_item:" + i, hotItemsDto);
            i++;
        }



    }


}

