package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountDto {

    private String name;
    private String bank;
    private String accountNumber;

    public AccountDto(Account account) {
        this.name = account.getName();
        this.bank = account.getBank();
        this.accountNumber = account.getAccountNumber();
    }
}
