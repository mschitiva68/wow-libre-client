package com.auth.wow.libre.infrastructure.client;

import com.auth.wow.libre.infrastructure.client.dto.*;
import com.auth.wow.libre.infrastructure.conf.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.client.*;
import org.springframework.web.util.*;

import java.util.*;

@Component
public class GoogleClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleClient.class);

    private final RestTemplate restTemplate;
    private final Configurations configurations;

    public GoogleClient(RestTemplate restTemplate, Configurations configurations) {
        this.restTemplate = restTemplate;
        this.configurations = configurations;
    }

    public VerifyCaptchaResponse verifyRecaptcha(VerifyCaptchaRequest request) {
        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", configurations.getApiSecret());
        params.add("response", request.getResponse());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        String url = UriComponentsBuilder.fromHttpUrl(String.format("%s/recaptcha/api/siteverify", "https://www" +
                        ".google.com"))
                .toUriString();

        try {
            ResponseEntity<VerifyCaptchaResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    VerifyCaptchaResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return Objects.requireNonNull(response.getBody());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("[GoogleClient] [verifyRecaptcha] Client/Server Error: {}. The request failed with a client " +
                            "or server error. " +
                            "HTTP Status: {}, Response Body: {}",
                    e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Transaction failed due to client or server error");
        } catch (Exception e) {
            LOGGER.error("[GoogleClient] [verifyRecaptcha] Unexpected Error: {}. An unexpected error occurred during " +
                            "the transaction with ID: {}.",
                    e.getMessage(), "", e);
            throw new RuntimeException("Unexpected transaction failure");
        }

        throw new RuntimeException("Unexpected transaction failure");
    }
}
