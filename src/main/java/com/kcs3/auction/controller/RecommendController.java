package com.kcs3.auction.controller;


import com.kcs3.auction.dto.EmbeddingRequest;
import com.kcs3.auction.dto.NormalResponse;
import com.kcs3.auction.dto.RecommendDto;
import com.kcs3.auction.service.ItemService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/no-auth/auction")
public class RecommendController {

    private final ItemService itemService;
    private WebClient webClient;

    // @Value 어노테이션을 사용하여 application.properties에서 값을 주입
    @Value("${flask.url}")
    private String flaskUrl;

    // @PostConstruct를 사용하여 WebClient 초기화
    @PostConstruct
    private void initWebClient() {
        this.webClient = WebClient.create(flaskUrl);
    }

    // 물품 상세 목록 가져오기


    // 리액트에서 파이썬으로 임베딩 정보 전달
    @PostMapping("/Recommendation")
    public ResponseEntity<NormalResponse> postRecommendation(@RequestBody EmbeddingRequest embeddingRequest) {
        try {
            Mono<ResponseEntity<String>> response = webClient.post()
                    .uri("/api/Recommend")
                    .bodyValue(embeddingRequest)
                    .retrieve()
                    .toEntity(String.class);

            ResponseEntity<String> result = response.block();
            if (result != null && result.getStatusCode().is2xxSuccessful()) {
                String responseBody = result.getBody(); // 파이썬 서버로부터 받은 실제 데이터
                // responseBody를 파싱하여 클라이언트에 반환할 데이터 구성
                String message = "임베딩 추천 정보를 성공적으로 전달했습니다.";
                return ResponseEntity.ok(new NormalResponse("success", message, responseBody)); // 실제 데이터 포함하여 반환
            } else {
                String message = "임베딩 추천 정보 전달에 실패했습니다.";
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NormalResponse("fail", message));
            }
        } catch (Exception e) {
            String message = "임베딩 추천 정보 전달 중 오류가 발생했습니다.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NormalResponse("fail", message));
        }
    }

    // 플라스크에서 받은 아이템 list dto로 작성
    @PostMapping("/Recommendation/makeDto")
    public ResponseEntity<NormalResponse> makeDtoFromEmbedding(@RequestBody List<Long> itemIds) {
        try {
            List<RecommendDto> itemDetails = itemService.getItemsByIds(itemIds);
            String message = "DTO 생성을 성공적으로 완료했습니다.";
            return ResponseEntity.ok(new NormalResponse("success", message, itemDetails));
        } catch (Exception e) {
            String message = "DTO 생성 중 오류가 발생했습니다.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NormalResponse("fail", message));
        }
    }
}
