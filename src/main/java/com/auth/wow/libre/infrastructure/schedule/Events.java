package com.auth.wow.libre.infrastructure.schedule;

import com.auth.wow.libre.domain.model.enums.*;
import com.auth.wow.libre.domain.ports.in.characters.*;
import com.auth.wow.libre.domain.ports.in.comands.*;
import com.auth.wow.libre.infrastructure.conf.*;
import com.auth.wow.libre.infrastructure.entities.characters.*;
import jakarta.xml.bind.*;
import org.slf4j.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Component
public class Events {
    private static final Logger LOGGER = LoggerFactory.getLogger(Events.class);
    private final CharactersPort charactersPort;
    private final Configurations configurations;
    private final ExecuteCommandsPort executeCommandsPort;

    public Events(CharactersPort charactersPort, Configurations configurations,
                  ExecuteCommandsPort executeCommandsPort) {
        this.charactersPort = charactersPort;
        this.configurations = configurations;
        this.executeCommandsPort = executeCommandsPort;
    }

    @Scheduled(cron = "0 */40 * * * *")
    public void eventTwinks() {

        if (configurations.isEventTwink()) {
            String transactionId = "";
            List<CharactersEntity> characters = charactersPort.getCharactersIsLevelMax(20, "EVENTS");

            if (characters.isEmpty()) {
                return;
            }

            characters.forEach(character -> {
                try {

                    final String command = CommandsCore.sendLevel(character.getName(), 19);
                    executeCommandsPort.execute(command, transactionId);
                } catch (JAXBException e) {
                    LOGGER.error("EVENT INVALID LVL");
                }
            });
        }


    }


}

