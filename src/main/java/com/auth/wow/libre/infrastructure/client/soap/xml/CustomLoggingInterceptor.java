package com.auth.wow.libre.infrastructure.client.soap.xml;

import org.slf4j.*;
import org.springframework.ws.client.support.interceptor.*;
import org.springframework.ws.context.*;

import java.io.*;
import java.nio.charset.*;

public class CustomLoggingInterceptor implements ClientInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLoggingInterceptor.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        // Log de la solicitud SOAP
        LOGGER.info("Request XML:");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            messageContext.getRequest().writeTo(os);
            LOGGER.info(os.toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Error writing request: " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println(); // Salto de línea para claridad
        return true; // Continuar con la ejecución normal
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        LOGGER.info("Response XML:");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            messageContext.getResponse().writeTo(outputStream);
            LOGGER.info(outputStream.toString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Error writing response: " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        // Log de fallos SOAP, si es necesario
        return true; // Continuar con la ejecución normal
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) {
        // Lógica adicional después de completar la solicitud
    }
}
