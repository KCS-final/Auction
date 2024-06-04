package com.kcs3.auction.controller;

import com.kcs3.auction.dto.MypageListDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.MypageService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/mypage")
public class MyPageController {
    @Autowired
    private MypageService mypageService;

    //User Id 추출
/*
    @GetMapping
    public Long getUserIdFromToken() {
        // 현재 사용자의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자 아이디 추출
        Long userId = Long.valueOf(authentication.getName());

        // 사용자 아이디를 이용하여 마이페이지 정보 조회
       return userId;
    }
*/


    //좋아요 페이지
    @GetMapping("/like")
    public List<MypageListDto> getMyLike(@PageableDefault(size =10)Pageable pageable){

        return mypageService.getLikedItemByUserId(pageable);
    }


    @GetMapping("/item-like")
    public ResponseDto<Boolean> getItemLike(Long itemId){
        return ResponseDto.ok(mypageService.getIsLikedItem(itemId));
    }

    //경매 등록 페이지
    @GetMapping("/auction")
    public List<MypageListDto> getMyAuction(@PageableDefault(size =10)Pageable pageable){

        return mypageService.getMyAuctionByUserId(pageable);

    }

    //입찰 참여 페이지
    @GetMapping("/bid")
    public List<MypageListDto> getMyBid(@PageableDefault(size =10)Pageable pageable){

        return mypageService.getMyBidByUserId(pageable);

    }

    //입찰 완료 페이지
    @GetMapping("/award")
    public List<MypageListDto> getMyAward(@PageableDefault(size =10) Pageable pageable){

        return mypageService.getMyCompleteByUserId(pageable);

    }



}

