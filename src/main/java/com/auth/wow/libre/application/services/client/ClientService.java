package com.auth.wow.libre.application.services.client;

import com.auth.wow.libre.domain.model.*;
import com.auth.wow.libre.domain.model.exception.*;
import com.auth.wow.libre.domain.ports.in.client.*;
import com.auth.wow.libre.domain.ports.in.wow_libre.*;
import com.auth.wow.libre.domain.ports.out.client.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import com.auth.wow.libre.infrastructure.util.*;
import org.slf4j.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import javax.crypto.*;
import java.util.*;

@Service
public class ClientService implements ClientPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientService.class);
    private static final String ROL_DEFAULT = "CLIENT";
    private final ObtainClient obtainClient;
    private final SaveClient saveClient;
    private final PasswordEncoder passwordEncoder;
    private final WowLibrePort wowLibrePort;

    public ClientService(ObtainClient obtainClient, SaveClient saveClient,
                         PasswordEncoder passwordEncoder, WowLibrePort wowLibrePort) {
        this.obtainClient = obtainClient;
        this.saveClient = saveClient;
        this.passwordEncoder = passwordEncoder;
        this.wowLibrePort = wowLibrePort;
    }

    @Override
    public void create(String username, String password, byte[] salt, String transactionId) {
        LOGGER.info("Free wow client creation request Date: {}", new Date());

        Optional<ClientEntity> findUsername = obtainClient.getClientStatusIsTrue();

        if (findUsername.isPresent()) {
            LOGGER.error("There is already a client registered in this integration, please contact support");
            throw new InternalException("It is not possible to create more than one client", transactionId);
        }

        try {
            final String jwt = wowLibrePort.login(transactionId).jwt;
            final ServerModel apiSecret = wowLibrePort.getApiSecret(jwt, transactionId);
            SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiSecret.keyPassword(), salt);
            final String decryptPassword = EncryptionUtil.decrypt(password, derivedKey);
            final String passwordEncode = passwordEncoder.encode(decryptPassword);

            ClientEntity client = new ClientEntity();
            client.setUsername(username);
            client.setRol(ROL_DEFAULT);
            client.setStatus(true);
            client.setPassword(passwordEncode);
            saveClient.save(client);
        } catch (Exception e) {
            LOGGER.error("[ClientService][create] Could not create client {} {} {}", e.getLocalizedMessage(),
                    e.getCause(), e.getMessage());
            throw new InternalException("Could not create client", transactionId);
        }
    }
}
