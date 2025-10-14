package org.project.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.exception.ExternalServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class BaseServiceClient {

    protected final RestTemplate restTemplate;

    protected abstract String getServiceName();

    @Retryable(
            value = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected <T> T get(String url, Class<T> responseType) {
        try {

            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExternalServiceException(
                        String.format("%s trả về status code: %s", getServiceName(), response.getStatusCode())
                );
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException(
                    String.format("%s đang gặp sự cố. Vui lòng thử lại sau.", getServiceName())
            );
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException(
                    String.format("Không thể kết nối đến %s", getServiceName())
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi %s", getServiceName())
            );
        }
        return null;
    }

    @Retryable(
            value = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        try {

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    responseType
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExternalServiceException(
                        String.format("%s trả về status code: %s", getServiceName(), response.getStatusCode())
                );
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException(
                    String.format("%s đang gặp sự cố. Vui lòng thử lại sau.", getServiceName())
            );
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException(
                    String.format("Không thể kết nối đến %s", getServiceName())
            );
        } catch (Exception e) {
            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi %s", getServiceName())
            );
        }
        return null;
    }


    @Retryable(
            value = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected <T, R> R post(String url, T body, Class<R> responseType) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<T> entity = new HttpEntity<>(body, headers);

            ResponseEntity<R> response = restTemplate.postForEntity(url, entity, responseType);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExternalServiceException(
                        String.format("%s trả về status code: %s", getServiceName(), response.getStatusCode())
                );
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException(
                    String.format("%s đang gặp sự cố. Vui lòng thử lại sau.", getServiceName())
            );
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException(
                    String.format("Không thể kết nối đến %s", getServiceName())
            );
        } catch (Exception e) {

            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi %s", getServiceName())
            );
        }
        return null;
    }

    @Retryable(
            value = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected <T, R> R put(String url, T body, Class<R> responseType) {
        try {
            log.debug("Calling {} - PUT: {}", getServiceName(), url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<T> entity = new HttpEntity<>(body, headers);

            ResponseEntity<R> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, responseType);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExternalServiceException(
                        String.format("%s trả về status code: %s", getServiceName(), response.getStatusCode())
                );
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("Server error from {}: {}", getServiceName(), e.getMessage());
            throw new ExternalServiceException(
                    String.format("%s đang gặp sự cố. Vui lòng thử lại sau.", getServiceName())
            );
        } catch (Exception e) {
            log.error("Unexpected error calling {}: {}", getServiceName(), e.getMessage(), e);
            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi %s", getServiceName())
            );
        }
        return null;
    }

    @Retryable(
            value = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected void delete(String url) {
        try {
            log.debug("Calling {} - DELETE: {}", getServiceName(), url);
            restTemplate.delete(url);

        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("Server error from {}: {}", getServiceName(), e.getMessage());
            throw new ExternalServiceException(
                    String.format("%s đang gặp sự cố. Vui lòng thử lại sau.", getServiceName())
            );
        } catch (Exception e) {
            log.error("Unexpected error calling {}: {}", getServiceName(), e.getMessage(), e);
            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi %s", getServiceName())
            );
        }
    }

    private void handleHttpClientError(HttpClientErrorException e) {
        log.error("Client error from {}: {} - {}", getServiceName(), e.getStatusCode(), e.getMessage());

        HttpStatusCode statusCode = e.getStatusCode();
        if (statusCode.equals(NOT_FOUND)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        } else if (statusCode.equals(BAD_REQUEST)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        } else if (statusCode.equals(FORBIDDEN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        } else if (statusCode.equals(UNAUTHORIZED)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        throw new ExternalServiceException(
                String.format("Lỗi từ %s: %s", getServiceName(), e.getMessage())
        );
    }
}
