
# S1lk Pay test task

An application for fund transfer in microservice architecture on Java/Spring boot. DBMS is H2 in-memory database.

The application  consist of 3 microservices(AuthService, AccountService, ExternalPaymentService)

AuthService is responsible for authentication and authorization using JWT. Also, it is responsible for JWT validation and providing user data for AccountService as per the valid JWT.

AccountService is responsible fund management like showing the account balance, fund transfer anc fund acceptance.

ExternalPaymentService integrates to AccountService to top up the account balance.



# Flow diagram of microservices
![](../../Desktop/diagram.png)



# AuthService API port 8080

### Login

```http
  POST /api/auth/login
```

#### Request. Provide username and password in JSON format.
```json
{
    "username": "Your username",
    "password": "Your password"
}
```
#### Response. Returns user info and JWT for access.
```json
{
    "user": {
        "id": "ID",
        "username": "username"
    },
    "token": "JWT"
}
```
### Register

```http
  POST /api/auth/register
```
#### Request. Provide username and password in JSON format.
```json
{
    "username": "Your username",
    "password": "Your password"
}
```
#### Response. Returns user success message in JSON format.
```json
{
    "message": "User has been registered successfully."
}
```
### Provide user data as per JWT in request header sent by other microservices

```http
  GET /api/auth/validate
```
#### Returns current authorized user's ID
```json
ID
```
-------------------------------------------------------------------
# AccountService API port 8081

### Create an account

```http
  POST /api/account
```
#### Request. Provide username and password in JSON format.
```json
{
    "accountNumber": "KZ1",
    "amount": "5000.0"
}
```
#### Returns create account in JSON format.
```json
{
    "id": 1,
    "accountNumber": "KZ1",
    "balance": "5000.0",
    "clientId": "1"
}
```
### Get account balance

```http
  GET /api/account/{accountNumber}/balance
```
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `accountNumber` | `string` | **Required**. Account number (ex. KZ1) |

#### Returns account balance

```json
5000.0
```
### Top up account

```http
  POST /api/account/top-up
```
#### Request. Provide account data in JSON format.
```json
{
    "accountNumber": "KZ1",
    "amount": "5000.0"
}
```
### Fund transfer

```http
  POST /api/account/transfer
```

```json
{
    "fromAccountNumber": "KZ1",
    "toAccountNumber",: "KZ2",
    "amount": "5000.0"
}
```
-------------------------------------------------------------------
## ExternalPaymentService API port 8082

### Payment to top up account balance

```http
  POST /api/external-payment-system/pay
```
#### Provide payment information in JSON format.
```json
{
    "accountNumber": "KZ1",
    "amount": "5000.0"
}
```
