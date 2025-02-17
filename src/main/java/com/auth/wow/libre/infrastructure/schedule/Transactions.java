package com.auth.wow.libre.infrastructure.schedule;

import com.auth.wow.libre.domain.model.enums.*;
import com.auth.wow.libre.domain.ports.in.character_service.*;
import com.auth.wow.libre.domain.ports.in.characters.*;
import com.auth.wow.libre.domain.ports.in.comands.*;
import com.auth.wow.libre.infrastructure.entities.characters.*;
import jakarta.xml.bind.*;
import org.slf4j.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Component
public class Transactions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transactions.class);
    private final CharactersPort charactersPort;

    private final CharacterTransactionPort characterTransactionPort;
    private final ExecuteCommandsPort executeCommandsPort;

    public Transactions(CharactersPort charactersPort, CharacterTransactionPort characterTransactionPort,
                        ExecuteCommandsPort executeCommandsPort) {
        this.charactersPort = charactersPort;
        this.characterTransactionPort = characterTransactionPort;
        this.executeCommandsPort = executeCommandsPort;
    }

    @Scheduled(cron = " 1 */40  * * * *")
    @Transactional
    public void sendCreditLoans() {
        String transactionId = "";
        List<CharacterTransactionEntity> characterTransaction =
                characterTransactionPort.findByTransactionType(TransactionType.SEND_ITEMS, transactionId);

        if (characterTransaction.isEmpty()) {
            return;
        }

        characterTransaction.forEach(transaction -> {
            try {

                if (transaction.getIndebtedness()) {
                    double cost = transaction.getAmount() * (10000);

                    List<CharactersEntity> characters =
                            charactersPort.getCharactersAvailableMoney(transaction.getAccountId(), cost, transactionId);

                    if (characters.isEmpty()) {
                        return;
                    }

                    for (CharactersEntity character : characters) {
                        if (cost <= 0) break; // Si el costo ya fue cubierto, salimos del ciclo

                        double characterMoney = character.getMoney();

                        double deduction = Math.min(characterMoney, cost);
                        charactersPort.updateMoney(character.getGuid(), (long) (characterMoney - deduction),
                                transactionId);
                        cost -= deduction;
                    }
                }
                executeCommandsPort.execute(transaction.getCommand(), transactionId);
                transaction.setStatus(false);
                characterTransactionPort.update(transaction, transactionId);
            } catch (JAXBException e) {
                LOGGER.error("Send invalid items");
            }
        });

    }

    @Scheduled(cron = " * */30 * * * *")
    @Transactional
    public void sendAnnouncement() {
        String transactionId = "";

        Optional<CharacterTransactionEntity> characterTransaction =
                characterTransactionPort.findByTransactionType(TransactionType.ANNOUNCEMENT, "")
                        .stream().findAny();

        if (characterTransaction.isEmpty()) {
            return;
        }
        CharacterTransactionEntity transaction = characterTransaction.get();

        try {
            executeCommandsPort.execute(transaction.getCommand(), transactionId);
            transaction.setStatus(false);
            characterTransactionPort.update(transaction, transactionId);
        } catch (JAXBException e) {
            LOGGER.error("Send invalid items");
        }
    }

}

