package com.example.bank.service;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.CLIENT)
public class AccountAlreadyExistsException extends RuntimeException {
  public AccountAlreadyExistsException(String message) {
    super(message);
  }
}
