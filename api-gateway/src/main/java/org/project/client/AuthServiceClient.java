package org.project.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.VerifyTokenRequest;
import org.project.dto.response.VerifyTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {
    private final WebClient.Builder webClient;

    public Mono<VerifyTokenResponse> verifyToken(String token) {
        VerifyTokenRequest request = VerifyTokenRequest.builder()
                .token(token)
                .build();

        return webClient.build()
                .post()
                .uri("lb://auth-service/api/auth/verify-token")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VerifyTokenResponse.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(e -> log.error("Lỗi khi xác thực token", e))
                .onErrorResume(e -> Mono.just(
                        VerifyTokenResponse.invalid("Không thể xác thực token")
                ));
    }
}
