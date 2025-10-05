package org.project.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.client.AuthServiceClient;
import org.project.dto.response.ErrorResponse;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify",
            "/api/payments/vnpay/callback",
            "/api/payments/vnpay/return"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        // Bỏ qua xác thực cho public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        //Get token from authorization header
        String token = extractToken(exchange.getRequest());
        if (token == null) {
            return unauthorized(exchange.getResponse(), "Thiếu token xác thực");
        }

        // Verify token
        // Gọi tới auth-service
        return authServiceClient.verifyToken(token)
                .flatMap(response -> {
                    if (response.isValid()) {
                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .header("X-User-Id", response.getUserId())
                                .header("X-Username", response.getUsername())
                                .header("X-Email", response.getEmail())
                                .header("X-Roles", String.join(",", response.getRoles()))
                                .build();

                        return chain.filter(exchange.mutate().request(request).build());
                    }
                    return unauthorized(exchange.getResponse(), response.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(error);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        String authHeader = headers.get(0);
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

}
