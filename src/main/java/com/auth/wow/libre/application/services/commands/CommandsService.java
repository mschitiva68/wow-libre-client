package com.auth.wow.libre.application.services.commands;

import com.auth.wow.libre.domain.model.*;
import com.auth.wow.libre.domain.ports.in.comands.*;
import com.auth.wow.libre.domain.ports.in.wow_libre.*;
import com.auth.wow.libre.infrastructure.client.*;
import com.auth.wow.libre.infrastructure.util.*;
import jakarta.xml.bind.*;
import org.springframework.stereotype.*;

import javax.crypto.*;

@Service
public class CommandsService implements ExecuteCommandsPort {
    private final CoreClient coreClient;
    private final WowLibrePort wowLibrePort;

    public CommandsService(CoreClient coreClient, WowLibrePort wowLibrePort) {
        this.coreClient = coreClient;
        this.wowLibrePort = wowLibrePort;
    }

    @Override
    public void execute(String messageEncrypt, byte[] salt, String transactionId) throws Exception {

        final String jwt = wowLibrePort.getJwt(transactionId);
        final ServerModel apiSecret = wowLibrePort.getApiSecret(jwt, transactionId);

        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiSecret.keyPassword(), salt);
        final String decryptedCommand = EncryptionUtil.decrypt(messageEncrypt, derivedKey);

        coreClient.executeCommand(decryptedCommand);
    }

    @Override
    public void execute(String command, String transactionId) throws JAXBException {
        coreClient.executeCommand(command);
    }




}
