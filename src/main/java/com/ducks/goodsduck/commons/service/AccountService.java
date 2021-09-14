package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.model.dto.AccountDto;
import com.ducks.goodsduck.commons.model.dto.AddressDto;
import com.ducks.goodsduck.commons.model.entity.Account;
import com.ducks.goodsduck.commons.model.entity.Address;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.AccountRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final MessageSource messageSource;

    public AccountDto getAccount(Long userId) {
        Account account = accountRepository.findByUserId(userId);
        if(account == null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"Account"}, null));
        }
        return new AccountDto(account);
    }

    public Boolean registerAccount(Long userId, AccountDto accountDto) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoResultException("Not find user in AccountService.registerAccount"));

            Account account = new Account(accountDto);
            account.setUser(user);
            accountRepository.save(account);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean editAccount(Long userId, AccountDto accountDto) {
        try {
            Account account = accountRepository.findByUserId(userId);

            if(account == null) {
                throw new NoResultException("Not find user in AccountService.editAccount");
            }

            account.setName(accountDto.getName());
            account.setBank(account.getBank());
            account.setAccountNumber(accountDto.getAccountNumber());

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
