package com.auth.wow.libre.domain.ports.in.account;

import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.infrastructure.repositories.auth.account.*;

public interface AccountPort {
    Long create(String username, String password, String email, Long userId,
                String expansion, byte[] salt, String transactionId);

    void createLocal(String username, String password, String email, String recaptchaResponse, String ipAddress);

    Long countOnline(String transactionId);

    Boolean isOnline(Long accountId, String transactionId);

    AccountDetailDto account(Long accountId, String transactionId);

    void changePassword(Long accountId, Long userId, String password, byte[] salt, String transactionId);

    AccountsDto accounts(int size, int page, String filter, String transactionId);

    Long count(String transactionId);

    MetricsProjection metrics(String transactionId);

    void updateMail(String username, String updateMail, String transactionId);

}
