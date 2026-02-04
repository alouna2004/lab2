package com.example.bank.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class BankService {

  public static class Account {
    public final String accountId;
    public final String owner;
    public BigDecimal balance;
    public final String currency;

    public Account(String accountId, String owner, BigDecimal balance, String currency) {
      this.accountId = accountId;
      this.owner = owner;
      this.balance = balance;
      this.currency = currency;
    }
  }

  private final Map<String, Account> db = new ConcurrentHashMap<>();

  public BankService() {
    db.put("A100", new Account("A100", "Alice", new BigDecimal("150.00"), "TND"));
    db.put("B200", new Account("B200", "Bob",   new BigDecimal("80.50"),  "TND"));
  }

  public Account getAccount(String accountId) {
    return db.get(accountId);
  }

  public BigDecimal deposit(String accountId, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidAmountException("Amount must be > 0");
    }
    Account acc = db.get(accountId);
    if (acc == null) {
      throw new UnknownAccountException("Unknown accountId: " + accountId);
    }
    acc.balance = acc.balance.add(amount);
    return acc.balance;
  }
  public Account createAccount(String accountId, String owner, BigDecimal initialBalance, String currency) {
    if (db.containsKey(accountId)) {
      throw new AccountAlreadyExistsException("Account already exists with accountId: " + accountId);
    }
    if(initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidAmountException("Initial balance must be >= 0");
    }
    Account acc = new Account(accountId, owner, initialBalance, currency);
    db.put(accountId, acc);
    return acc;
  }
}
