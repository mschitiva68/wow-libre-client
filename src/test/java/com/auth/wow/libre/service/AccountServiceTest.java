package com.auth.wow.libre.service;

import com.auth.wow.libre.application.services.account.*;
import com.auth.wow.libre.domain.model.*;
import com.auth.wow.libre.domain.model.exception.*;
import com.auth.wow.libre.domain.ports.in.wow_libre.*;
import com.auth.wow.libre.domain.ports.out.account.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
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


}
