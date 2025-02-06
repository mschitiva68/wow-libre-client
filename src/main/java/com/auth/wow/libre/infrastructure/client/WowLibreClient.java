package com.auth.wow.libre.infrastructure.client;

import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.domain.model.exception.*;
import com.auth.wow.libre.domain.model.shared.*;
import com.auth.wow.libre.infrastructure.conf.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

import java.util.*;

import static com.auth.wow.libre.domain.model.constant.Constants.*;

@Component
public class WowLibreClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(WowLibreClient.class);

    private final RestTemplate restTemplate;
    private final Configurations configurations;

    public WowLibreClient(RestTemplate restTemplate, Configurations configurations) {
        this.restTemplate = restTemplate;
        this.configurations = configurations;
    }


    public LoginResponseDto login(String transactionId) {
        HttpHeaders headers = new HttpHeaders();

        LoginDto request = new LoginDto();
        request.setPassword(configurations.getLoginPassword());
        request.setUsername(configurations.getLoginUsername());

        headers.set(HEADER_TRANSACTION_ID, transactionId);
        HttpEntity<LoginDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<GenericResponse<LoginResponseDto>> response = restTemplate.exchange(String.format("%s",
                            configurations.getPathLoginWowLibre()),
                    HttpMethod.POST, entity,
                    new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode().is2xxSuccessful()) {
                return Objects.requireNonNull(response.getBody()).getData();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("Client/Server Error: {}. The request failed with a client or server error. " +
                            "HTTP Status: {}, Response Body: {}",
                    e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalException("Transaction failed due to client or server error", transactionId);
        } catch (Exception e) {
            LOGGER.error("Unexpected Error: {}. An unexpected error occurred during the transaction with ID: {}.",
                    e.getMessage(), transactionId, e);
            throw new InternalException("Unexpected transaction failure", transactionId);
        }

        throw new InternalException("Unexpected transaction failure", transactionId);

    }

    public ServerDto getApiSecret(String jwt, String transactionId) {
        HttpHeaders headers = new HttpHeaders();

        headers.set(HEADER_TRANSACTION_ID, transactionId);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GenericResponse<ServerDto>> response = restTemplate.exchange(String.format("%s?api_key=%s",
                            configurations.getPathServerWowLibre(), configurations.getServerApiKey()),
                    HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode().is2xxSuccessful()) {
                return Objects.requireNonNull(response.getBody()).getData();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("[WowLibreClient] [getApiSecret] Client/Server Error: {}. The request failed with a client or " +
                            "server error. " +
                            "HTTP Status: {}, Response Body: {}",
                    e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalException("Transaction failed due to client or server error", transactionId);
        } catch (Exception e) {
            LOGGER.error("[WowLibreClient] [getApiSecret] Unexpected Error: {}. An unexpected error occurred during the" +
                            " transaction with ID: {}.",
                    e.getMessage(), transactionId, e);
            throw new InternalException("Unexpected transaction failure", transactionId);
        }

        throw new InternalException("Unexpected transaction failure", transactionId);

    }

}
