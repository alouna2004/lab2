#Lab 2: SOA/SOAP with Java (Spring-WS)
##Reading the Contract
Question 1 : Identify the XSD file and explain its role
- The XSD file "bank.xsd" is located in lab2/src/main/resources
- The XSD defines the structure of the requests and responses for the SOAP service  It tells the service what fields exist their types and how messages should look like Basically, it is the contract that both client and server follow to communicate correctly

Question 2 : List the request/response elements (names, fields, types)
 1) GetAccount : 
    - Request : GetAccountRequest 
        accountId => string
    - Response : GetAccountResponse
        accountId => string
        owner => string
        balance => decimal
        currency => string
 2) Deposit : 
    - Request : DepositRequest 
        accountId => string
        amount => decimal
    - Response : DepositResponse
        newBalance => decimal
Question 3 : Information from the WSDL
I identified from the WSDL :
    - Namespace: http://example.com/bank 
    - porType : BankPort => contains the operations :
        1) GetAccount : 
            - input : GetAccountRequest
            - output : GetAccountResponse
        2) Deposit:
            - input : DepositRequest
            - output : DepositResponse
    - Endpoint URL: http://localhost:8080/ws 
    -Binding :  SOAP 1.1 with document style 
##Add one feature
Question 1 : the choice of the feature :
I added "CreateAccount" operation that allows creationg a new bank account to extend the SOAP service
Question 2 : Update the Contract 
- Request : 
```xsd
 <xsd:element name="CreateAccountRequest">
   <xsd:complexType>
      <xsd:sequence>
         <xsd:element name="accountId" type="xsd:string"/>
         <xsd:element name="owner" type="xsd:string"/>
         <xsd:element name="balance" type="xsd:decimal"/>
         <xsd:element name="currency" type="xsd:string"/>
      </xsd:sequence>
   </xsd:complexType>
</xsd:element>
```
- Response :
```xsd
<xsd:element name="CreateAccountResponse">
  <xsd:complexType>
    <xsd:sequence>
      <xsd:element name="account" type="tns:AccountType"/>
      <xsd:element name="message" type="xsd:string"/>
    </xsd:sequence>
  </xsd:complexType>
</xsd:element>

```
Question 3 : Server-side Implementation
- Endpoint : in BankEndpoint.java
```java
@PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateAccountRequest")
  @ResponsePayload
  public CreateAccountResponse createAccount(@RequestPayload CreateAccountRequest request) {
    Account acc = bankService.createAccount(request.getAccountId(), request.getOwner(), request.getBalance(), request.getCurrency());
    AccountType dto = new AccountType();
    dto.setAccountId(acc.accountId);
    dto.setOwner(acc.owner);
    dto.setBalance(acc.balance);
    dto.setCurrency(acc.currency);

    CreateAccountResponse resp = new CreateAccountResponse();
    resp.setAccount(dto);
    resp.setMessage("Welcome " + acc.owner + "! Your account has been created with ID: " + acc.accountId + " and balance: " + acc.balance + " " + acc.currency);
    return resp;
  }
```
- service : BankService.java
```java
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
```
Question 4 : Testing with Postman 
1)  Valid Case : 
 - Request : 
 ```xml
 <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:bank="http://example.com/bank">
   <soapenv:Body>
      <bank:CreateAccountRequest>
         <bank:accountId>C400</bank:accountId>
         <bank:owner>Aline Aloulou</bank:owner>
         <bank:balance>500.00</bank:balance>
         <bank:currency>TND</bank:currency>
      </bank:CreateAccountRequest>
   </soapenv:Body>
</soapenv:Envelope>
 ```
 - Response :
 ```xml
 <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
        <ns2:CreateAccountResponse xmlns:ns2="http://example.com/bank">
            <ns2:account>
                <ns2:accountId>C400</ns2:accountId>
                <ns2:owner>Aline Aloulou</ns2:owner>
                <ns2:balance>500.00</ns2:balance>
                <ns2:currency>TND</ns2:currency>
            </ns2:account>
            <ns2:message>Welcome Aline Aloulou! Your account has been created with ID: C400 and balance: 500.00 TND</ns2:message>
        </ns2:CreateAccountResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
2) Invalid Case 1 : Account already exists
 - Request :
 ```xml
 <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:bank="http://example.com/bank">
   <soapenv:Body>
      <bank:CreateAccountRequest>
         <bank:accountId>C400</bank:accountId>
         <bank:owner>Aline Aloulou</bank:owner>
         <bank:balance>500.00</bank:balance>
         <bank:currency>TND</bank:currency>
      </bank:CreateAccountRequest>
   </soapenv:Body>
</soapenv:Envelope>
 ```
 - Response :
 ```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
        <SOAP-ENV:Fault>
            <faultcode>SOAP-ENV:Client</faultcode>
            <faultstring xml:lang="en">Account already exists with accountId: C400</faultstring>
        </SOAP-ENV:Fault>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>

 ```
3) Invalid Case 2 : Invalid Amount
 - Request :
 ```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:bank="http://example.com/bank">
   <soapenv:Body>
      <bank:CreateAccountRequest>
         <bank:accountId>D300</bank:accountId>
         <bank:owner>Mayssam</bank:owner>
         <bank:balance>-10</bank:balance>
         <bank:currency>TND</bank:currency>
      </bank:CreateAccountRequest>
   </soapenv:Body>
</soapenv:Envelope>


 ```
 - Response :
 ```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
        <SOAP-ENV:Fault>
            <faultcode>SOAP-ENV:Client</faultcode>
            <faultstring xml:lang="en">Initial balance must be &gt;= 0</faultstring>
        </SOAP-ENV:Fault>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>

 ```




 
        
