package com.auth.wow.libre.application.services.wow_libre;

import com.auth.wow.libre.domain.model.*;
import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.domain.model.exception.*;
import com.auth.wow.libre.domain.ports.in.wow_libre.*;
import com.auth.wow.libre.domain.ports.out.client.*;
import com.auth.wow.libre.infrastructure.client.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class WowLibreService implements WowLibrePort {
    private static final Logger LOGGER = LoggerFactory.getLogger(WowLibreService.class);

    private final WowLibreClient wowLibreClient;
    private final ObtainClient obtainClient;
    private final SaveClient saveClient;

    public WowLibreService(WowLibreClient wowLibreClient, ObtainClient obtainClient, SaveClient saveClient) {
        this.wowLibreClient = wowLibreClient;
        this.obtainClient = obtainClient;
        this.saveClient = saveClient;
    }

    @Override
    public String getJwt(String transactionId) {

        Optional<ClientEntity> clientJwt = obtainClient.getClientStatusIsTrue();

        if (clientJwt.isEmpty()) {
            LOGGER.error("[WowLibreService][getJwt] The system has not been configured correctly to use the " +
                    "registration system with Wow Libre {}", transactionId);
            throw new InternalException("The system has not been configured correctly to register users.",
                    transactionId);
        }

        ClientEntity client = clientJwt.get();

        if (client.getExpirationDate() != null && client.getExpirationDate().after(new Date())) {
            return client.getJwt();
        }

        LoginResponseDto jwtDto = login(transactionId);

        if (jwtDto == null) {
            LOGGER.error("[WowLibreService] [getJwt]  The system could not do a successful authentication with " +
                    "free wow so it was not possible to carry out the transaction. {}", transactionId);
            throw new InternalException("It was not possible to authenticate with the free wow central, please " +
                    "contact support", transactionId);
        }

        client.setJwt(jwtDto.jwt);
        client.setRefreshToken(jwtDto.refreshToken);
        client.setExpirationDate(jwtDto.expirationDate);
        saveClient.save(client);

        return jwtDto.jwt;
    }

    @Override
    public LoginResponseDto login(String transactionId) {

        LoginResponseDto jwtDto = wowLibreClient.login(transactionId);

        if (jwtDto == null) {
            LOGGER.error("[WowLibreService] [apiSecret]  The system could not do a successful authentication with " +
                    "free" +
                    " wow so it was not possible to" +
                    " carry out the transaction. {}", transactionId);
            throw new InternalException("It was not possible to authenticate with the free wow central, please " +
                    "contact support", transactionId);
        }

        return jwtDto;
    }

    @Override
    public ServerModel getApiSecret(String jwt, String transactionId) {
        ServerDto serverDto = wowLibreClient.getApiSecret(jwt, transactionId);

        if (serverDto == null || serverDto.getApiSecret() == null) {
            LOGGER.error("[WowLibreService][apiSecret] An error occurred when obtaining the secret api with the free " +
                    "wow central " +
                    "base {}", transactionId);
            throw new InternalException("An error occurred when obtaining the secret api with the free wow central " +
                    "base", transactionId);
        }

        return new ServerModel(serverDto.getApiSecret());
    }
}
