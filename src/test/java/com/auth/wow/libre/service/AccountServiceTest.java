package com.auth.wow.libre.service;

import com.auth.wow.libre.application.services.account.*;
import com.auth.wow.libre.domain.model.*;
import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.domain.model.exception.*;
import com.auth.wow.libre.domain.ports.in.account_banned.*;
import com.auth.wow.libre.domain.ports.in.wow_libre.*;
import com.auth.wow.libre.domain.ports.out.account.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import com.auth.wow.libre.infrastructure.repositories.auth.account.*;
import com.auth.wow.libre.infrastructure.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import javax.crypto.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {


    @Mock
    private WowLibrePort wowLibrePort;

    @Mock
    private ObtainAccountPort obtainAccountPort;

    @Mock
    private SaveAccountPort saveAccountPort;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountBannedPort accountBannedPort;


    private String username;
    private String email;
    private Long userId;
    private String expansion;
    private String transactionId;
    private String apiKeySecret;

    @BeforeEach
    void setUp() {
        username = "testUser";
        email = "test@example.com";
        userId = 1L;
        expansion = "2";
        transactionId = "transaction123";
        apiKeySecret = "keyPassword";
    }

    @Test
    void testCreateAccountSuccessfully() throws Exception {
        byte[] saltPassword = KeyDerivationUtil.generateSalt();
        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiKeySecret, saltPassword);
        String passwordEncrypt = EncryptionUtil.encrypt("newPassword", derivedKey);

        // Arrange
        when(wowLibrePort.getJwt(transactionId)).thenReturn("jwtToken");
        when(wowLibrePort.getApiSecret("jwtToken", transactionId)).thenReturn(new ServerModel("keyPassword"));
        when(obtainAccountPort.findByUsername(username)).thenReturn(Optional.empty());
        when(saveAccountPort.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity account = invocation.getArgument(0);
            account.setId(1L); // Simulate the ID generation
            return account;
        });

        // Act
        Long accountId = accountService.create(username, passwordEncrypt, email, userId, expansion, saltPassword,
                transactionId);

        // Assert
        assertNotNull(accountId);
        assertEquals(1L, accountId);
        verify(saveAccountPort, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void testCreateAccountWithExistingUsername() throws Exception {
        byte[] saltPassword = KeyDerivationUtil.generateSalt();
        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiKeySecret, saltPassword);
        String passwordEncrypt = EncryptionUtil.encrypt("newPassword", derivedKey);


        // Arrange
        when(wowLibrePort.getJwt(transactionId)).thenReturn("jwtToken");
        when(wowLibrePort.getApiSecret("jwtToken", transactionId)).thenReturn(new ServerModel("keyPassword"));
        when(obtainAccountPort.findByUsername(username)).thenReturn(Optional.of(new AccountEntity()));

        // Act & Assert
        InternalException exception = assertThrows(InternalException.class, () -> accountService.create(username,
                passwordEncrypt, email, userId, expansion, saltPassword, transactionId));

        assertEquals("The username is not available", exception.getMessage());
        verify(saveAccountPort, never()).save(any(AccountEntity.class));
    }

    @Test
    void testCreateAccountWithEncryptionFailure() throws Exception {

        byte[] saltPassword = KeyDerivationUtil.generateSalt();
        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiKeySecret, saltPassword);
        String passwordEncrypt = EncryptionUtil.encrypt("newPassword", derivedKey);

        // Arrange
        when(wowLibrePort.getJwt(transactionId)).thenReturn("jwtToken");
        when(wowLibrePort.getApiSecret("jwtToken", transactionId)).thenReturn(new ServerModel("keyPassword"));

        // Act & Assert
        InternalException exception = assertThrows(InternalException.class, () -> accountService.create(username,
                passwordEncrypt + "s", email, userId, expansion, saltPassword,
                transactionId));

        assertEquals("It was not possible to create the client, please try later and contact support",
                exception.getMessage());
    }

    @Test
    void testCountOnlineUsers() {
        // Arrange
        when(obtainAccountPort.countOnline(transactionId)).thenReturn(22L);
        // Act
        Long usersOnline = accountService.countOnline(transactionId);
        // Assert
        assertEquals(22, usersOnline);
    }

    @Test
    void testOnlineByUser() {
        Long accountId = 222L;
        // Arrange
        when(obtainAccountPort.findById(accountId)).thenReturn(Optional.of(new AccountEntity()));
        // Act
        Boolean online = accountService.isOnline(accountId, "");
        // Assert
        assertEquals(false, online);
    }

    @Test
    void testUpdateMailSuccessfully() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(username);
        accountEntity.setEmail(email);

        when(obtainAccountPort.findByUsername(username)).thenReturn(Optional.of(accountEntity));

        assertDoesNotThrow(() -> accountService.updateMail(username, "new@example.com", transactionId));
        verify(saveAccountPort, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void testUpdateMailUserNotFound() {
        when(obtainAccountPort.findByUsername(username)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () ->
                accountService.updateMail(username, "new@example.com", transactionId));

        assertEquals("Cannot find a user with that username", exception.getMessage());
    }

    @Test
    void testCountSuccessfully() {
        when(obtainAccountPort.count()).thenReturn(100L);

        Long count = accountService.count(transactionId);
        assertEquals(100L, count);
    }

    @Test
    void testMetricsSuccessfully() {
        MetricsProjection metricsProjection = mock(MetricsProjection.class);
        when(obtainAccountPort.metrics(transactionId)).thenReturn(metricsProjection);

        MetricsProjection result = accountService.metrics(transactionId);
        assertNotNull(result);
        assertEquals(metricsProjection, result);
    }

    @Test
    void testAccountMuteSuccessfully() {
        Long accountId = 1L;
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountId);
        accountEntity.setUsername(username);
        accountEntity.setEmail(email);
        accountEntity.setMuteTime(200L);
        accountEntity.setMuteBy("GM");
        accountEntity.setMuteReason("Banned");

        when(obtainAccountPort.findById(accountId)).thenReturn(Optional.of(accountEntity));
        when(accountBannedPort.getAccountBanned(accountId)).thenReturn(null);

        AccountDetailDto result = accountService.account(accountId, transactionId);

        assertNotNull(result);
        assertEquals(accountId, result.id());
        assertEquals(username, result.username());
        assertEquals("Banned", result.muteReason());
        assertEquals("GM", result.muteBy());
        assertTrue(result.mute());

        verify(obtainAccountPort, times(1)).findById(accountId);
    }

    @Test
    void testAccountSuccessfully() {
        Long accountId = 1L;
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountId);
        accountEntity.setUsername(username);
        accountEntity.setEmail(email);
        accountEntity.setMuteTime(null);
        when(obtainAccountPort.findById(accountId)).thenReturn(Optional.of(accountEntity));
        when(accountBannedPort.getAccountBanned(accountId)).thenReturn(null);

        AccountDetailDto result = accountService.account(accountId, transactionId);

        assertNotNull(result);
        assertEquals(accountId, result.id());
        assertEquals(username, result.username());
        verify(obtainAccountPort, times(1)).findById(accountId);
    }

    @Test
    void testAccountNotFound() {
        Long accountId = 1L;

        when(obtainAccountPort.findById(accountId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                accountService.account(accountId, transactionId));

        assertEquals("There is no associated account or it is not available.", exception.getMessage());
    }


    @Test
    void testAccountsSuccessfully() {
        AccountEntity accountMuted = new AccountEntity();
        accountMuted.setMuteTime(200L);
        AccountEntity accountNotMuted = new AccountEntity();
        accountNotMuted.setMuteTime(0L);
        AccountEntity accountMuteTimeNull = new AccountEntity();
        accountMuteTimeNull.setMuteTime(null);

        List<AccountEntity> accountEntities = List.of(
                accountNotMuted,
                accountMuted,
                accountMuteTimeNull
        );

        when(obtainAccountPort.findByAll(10, 0, "filter")).thenReturn(accountEntities);
        when(obtainAccountPort.count()).thenReturn(3L);

        AccountsDto result = accountService.accounts(10, 0, "filter", transactionId);

        assertNotNull(result);
        assertEquals(3, result.getAccounts().size());
        assertEquals(3L, result.getSize());
        verify(obtainAccountPort, times(1)).findByAll(10, 0, "filter");
        verify(obtainAccountPort, times(1)).count();
    }

    @Test
    void testChangePasswordSuccessfully() throws Exception {
        Long accountId = 1L;
        Long userId = 10L;
        byte[] saltPassword = KeyDerivationUtil.generateSalt();
        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiKeySecret, saltPassword);
        String encryptedPassword = EncryptionUtil.encrypt("newPassword", derivedKey);

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountId);
        accountEntity.setUsername(username);

        when(obtainAccountPort.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(accountEntity));
        when(wowLibrePort.getJwt(transactionId)).thenReturn("jwtToken");
        when(wowLibrePort.getApiSecret("jwtToken", transactionId)).thenReturn(new ServerModel("keyPassword"));

        accountService.changePassword(accountId, userId, encryptedPassword, saltPassword, transactionId);

        verify(saveAccountPort, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void testChangePasswordAccountNotFound() {
        Long accountId = 1L;
        Long userId = 10L;
        byte[] saltPassword = new byte[16];

        when(obtainAccountPort.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () ->
                accountService.changePassword(accountId, userId, "password", saltPassword, transactionId));

        assertEquals("The server where your character is currently located is not available", exception.getMessage());
    }

    @Test
    void testChangePasswordEncryptionFailure() throws Exception {
        Long accountId = 1L;
        Long userId = 10L;
        byte[] saltPassword = KeyDerivationUtil.generateSalt();
        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiKeySecret, saltPassword);
        String encryptedPassword = EncryptionUtil.encrypt("newPassword", derivedKey);

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountId);
        accountEntity.setUsername(username);

        when(obtainAccountPort.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(accountEntity));
        when(wowLibrePort.getJwt(transactionId)).thenReturn("jwtToken");
        when(wowLibrePort.getApiSecret("jwtToken", transactionId)).thenReturn(new ServerModel("keyPassword"));

        doThrow(new InternalException("Encryption error",""))
                .when(saveAccountPort).save(any(AccountEntity.class));

        InternalException exception = assertThrows(InternalException.class, () ->
                accountService.changePassword(accountId, userId, encryptedPassword, saltPassword, transactionId));

        assertEquals("Could not update password", exception.getMessage());
    }

    @Test
    void testChangePasswordGeneralFailure() throws Exception {
        Long accountId = 1L;
        Long userId = 10L;
        byte[] saltPassword = KeyDerivationUtil.generateSalt();
        SecretKey derivedKey = KeyDerivationUtil.deriveKeyFromPassword(apiKeySecret, saltPassword);
        String encryptedPassword = EncryptionUtil.encrypt("newPassword", derivedKey);

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountId);
        accountEntity.setUsername(username);

        when(obtainAccountPort.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(accountEntity));
        when(wowLibrePort.getJwt(transactionId)).thenReturn("jwtToken");
        when(wowLibrePort.getApiSecret("jwtToken", transactionId)).thenReturn(new ServerModel("keyPassword"));

        doThrow(new RuntimeException("Unexpected error"))
                .when(saveAccountPort).save(any(AccountEntity.class));

        InternalException exception = assertThrows(InternalException.class, () ->
                accountService.changePassword(accountId, userId, encryptedPassword, saltPassword, transactionId));

        assertEquals("Could not update password", exception.getMessage());
    }

}
