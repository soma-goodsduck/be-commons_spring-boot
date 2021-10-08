package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.AccountDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String bank;
    private String accountNumber;

    public Account(AccountDto accountDto) {
        this.name = accountDto.getName();
        this.bank = accountDto.getBank();
        this.accountNumber = accountDto.getAccountNumber();
    }
}
