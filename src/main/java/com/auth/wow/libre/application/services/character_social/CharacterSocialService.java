package com.auth.wow.libre.application.services.character_social;

import com.auth.wow.libre.domain.model.*;
import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.domain.model.enums.*;
import com.auth.wow.libre.domain.model.exception.*;
import com.auth.wow.libre.domain.ports.in.account.*;
import com.auth.wow.libre.domain.ports.in.character_service.*;
import com.auth.wow.libre.domain.ports.in.character_social.*;
import com.auth.wow.libre.domain.ports.in.characters.*;
import com.auth.wow.libre.domain.ports.in.comands.*;
import com.auth.wow.libre.domain.ports.out.account.*;
import com.auth.wow.libre.domain.ports.out.character_social.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import jakarta.transaction.*;
import jakarta.xml.bind.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Service
public class CharacterSocialService implements CharacterSocialPort {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CharacterSocialService.class);
    public static final String ERROR_IT_WAS_NOT_POSSIBLE_TO_OBTAIN_THE_REQUESTED_CHARACTER = "It was not possible to obtain the requested character";

    private final CharactersPort charactersPort;
    private final ObtainCharacterSocial obtainCharacterSocial;
    private final ObtainAccountPort obtainAccountPort;
    private final AccountPort accountPort;
    private final CharacterTransactionPort characterTransactionPort;
    private final ExecuteCommandsPort executeCommandsPort;

    public CharacterSocialService(CharactersPort charactersPort, ObtainCharacterSocial obtainCharacterSocial,
                                  ObtainAccountPort obtainAccountPort, AccountPort accountPort,
                                  CharacterTransactionPort characterTransactionPort,
                                  ExecuteCommandsPort executeCommandsPort) {
        this.charactersPort = charactersPort;
        this.obtainCharacterSocial = obtainCharacterSocial;
        this.obtainAccountPort = obtainAccountPort;
        this.accountPort = accountPort;
        this.characterTransactionPort = characterTransactionPort;
        this.executeCommandsPort = executeCommandsPort;
    }


    @Override
    public CharacterSocialDto getFriends(Long characterId, String transactionId) {

        CharacterDetailDto characterDetail = charactersPort.getCharacter(characterId, transactionId);

        if (characterDetail == null) {
            throw new InternalException("It was not possible to obtain the requested character", transactionId);
        }

        CharacterSocialDto characterSocialDto = new CharacterSocialDto();

        List<CharacterSocial> characterSocials = obtainCharacterSocial.getFriends(characterId, transactionId);

        if (Objects.nonNull(characterSocials)) {

            List<CharacterSocialDetail> friends =
                    characterSocials.stream().map(characterSocial ->
                            Optional.of(charactersPort.getCharacter(characterSocial.friend, transactionId))
                                    .map(character -> new CharacterSocialDetail(character, characterSocial))
                                    .orElse(null)).toList();
            characterSocialDto.setFriends(friends);
            characterSocialDto.setTotalQuantity(friends.size());
        }

        return characterSocialDto;
    }

    @Override
    public void deleteFriend(Long characterId, Long accountId, Long friendGuid, Long userId,
                             String transactionId) {

        verifierAccount(accountId, userId, transactionId);

        if (charactersPort.getCharacter(characterId, accountId, transactionId) == null) {
            throw new InternalException(ERROR_IT_WAS_NOT_POSSIBLE_TO_OBTAIN_THE_REQUESTED_CHARACTER, transactionId);
        }

        obtainCharacterSocial.getFriends(characterId, transactionId)
                .stream().filter(characterSocial -> characterSocial.friend.equals(friendGuid))
                .findFirst().orElseThrow(() -> new InternalException("Friend not found", transactionId));

        obtainCharacterSocial.deleteFriend(characterId, friendGuid, transactionId);
    }

    private void verifierAccount(Long accountId, Long userId, String transactionId) {
        final Optional<AccountEntity> account = obtainAccountPort.findByIdAndUserId(accountId, userId);

        if (account.isEmpty()) {
            throw new InternalException("The server where your character is currently located is not available",
                    transactionId);
        }

    }

    @Transactional
    @Override
    public void sendMoney(Long characterId, Long accountId, Long userId, Long friendGuid, Long money,
                          Double costRequest,
                          String transactionId) {
        verifierAccount(accountId, userId, transactionId);

        CharacterDetailDto character = charactersPort.getCharacter(characterId, accountId, transactionId);

        if (character == null) {
            throw new InternalException("It was not possible to obtain the requested character", transactionId);
        }

        final Long goldSend = money * 10000;
        final Double cost = costRequest <= 0 ? TransactionType.SEND_MONEY.getCost() : costRequest;

        if (money <= 0) {
            throw new InternalException("The minimum value to send is 1 gold", transactionId);
        }

        if (character.getMoney() <= goldSend + cost) {
            throw new InternalException("You don't have enough money to send the money", transactionId);
        }

        final CharacterDetailDto friend = charactersPort.getCharacter(friendGuid, transactionId);

        if (friend == null) {
            throw new InternalException("The recipient is not found or is no longer available", transactionId);
        }

        Boolean isOnline = accountPort.isOnline(accountId, transactionId);

        if (Boolean.TRUE.equals(isOnline)) {
            throw new InternalException("To send money to your partner you must be offline.", transactionId);
        }

        try {
            final String command = CommandsCore.sendMoney(friend.getName(), "", "",
                    String.valueOf(goldSend));

            executeCommandsPort.execute(command, transactionId);
            charactersPort.updateMoney(characterId, (long) (character.getMoney() - (goldSend + cost)),
                    transactionId);

            characterTransactionPort.create(CharacterTransactionModel.builder()
                    .amount(cost)
                    .transactionType(TransactionType.SEND_MONEY)
                    .characterId(characterId)
                    .indebtedness(false)
                    .status(false)
                    .transactionDate(LocalDateTime.now())
                    .command(command)
                    .accountId(accountId)
                    .userId(userId)
                    .successful(true)
                    .build(), transactionId);

        } catch (JAXBException e) {
            LOGGER.error("I tried to send money by mail to my colleague but it was not possible. CharacterId{} " +
                            "FriendId{} ",
                    characterId, friendGuid);
            throw new InternalException("It was not possible to send the money", transactionId);
        }


    }

    @Override
    public void sendLevel(Long characterId, Long accountId, Long userId, Long friendGuid, int level, Double costRequest,
                          String transactionId) {
        verifierAccount(accountId, userId, transactionId);

        CharacterDetailDto character = charactersPort.getCharacter(characterId, accountId, transactionId);

        if (character == null) {
            throw new InternalException("It was not possible to obtain the requested character", transactionId);
        }

        final CharacterDetailDto friend = charactersPort.getCharacter(friendGuid, transactionId);

        if (friend == null) {
            throw new InternalException("The recipient is not found or is no longer available", transactionId);
        }

        if (friend.getLevel() >= 80) {
            throw new InternalException("The recipient already has the maximum level allowed", transactionId);
        }

        int levelFriend = friend.getLevel();

        int effectiveLevel = level;

        if (levelFriend + level > 80) {
            effectiveLevel = 80 - levelFriend;
        }

        final Double cost = costRequest <= 0 ? TransactionType.SEND_LEVEL.getCost() * effectiveLevel :
                costRequest * effectiveLevel;

        if (character.getMoney() < cost) {
            throw new InternalException("You don't have enough money to send the money", transactionId);
        }

        Boolean isOnline = accountPort.isOnline(accountId, transactionId);

        if (Boolean.TRUE.equals(isOnline)) {
            throw new GenericErrorException("To send money to your partner you must be offline.", transactionId,
                    9000, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {

            final String command = CommandsCore.sendLevel(friend.getName(), friend.getLevel() + level);
            executeCommandsPort.execute(command, transactionId);

            characterTransactionPort.create(CharacterTransactionModel.builder()
                    .amount(cost)
                    .transactionType(TransactionType.SEND_LEVEL)
                    .characterId(characterId)
                    .indebtedness(false)
                    .status(false)
                    .transactionDate(LocalDateTime.now())
                    .command(command)
                    .accountId(accountId)
                    .userId(userId)
                    .successful(true)
                    .build(), transactionId);

            charactersPort.updateMoney(characterId, (long) (character.getMoney() - cost), transactionId);
        } catch (JAXBException e) {
            LOGGER.error("It was not possible to send the level change to the recipient  characterId{}  friendId{}",
                    characterId, friendGuid);

            throw new InternalException("It was not possible to send the level change to the recipient",
                    transactionId);
        }

    }


}
