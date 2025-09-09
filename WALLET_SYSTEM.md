# Wallet System Documentation

## Overview

This document describes the enhanced wallet system implementation for the tendering/auction platform, which includes payment methods, escrow functionality, and secure transaction processing.

## Core Components

### 1. Payment Methods

#### Bank Accounts (`BankAccount`)
- IBAN-based bank account management
- Account holder name validation
- Bank name and branch code support
- Verification workflow for withdrawal security
- Default account selection

#### Credit Cards (`CreditCard`)
- Secure masked card number storage
- Automatic brand detection (VISA, Mastercard, AMEX, etc.)
- Expiry date validation
- Payment gateway tokenization support
- PCI-compliant data handling

### 2. Enhanced Wallet System

#### Wallet (`Wallet`)
- Available balance for spending
- Hold balance for escrow operations  
- Pending balance for processing transactions
- Total deposited/withdrawn tracking
- Wallet locking capability for security

#### Transactions (`Transaction`)
- Enhanced transaction types:
  - `DEPOSIT` - Money deposits
  - `WITHDRAWAL` - Money withdrawals
  - `BID_PAYMENT` - Auction bid payments
  - `EARNINGS` - Seller earnings
  - `ESCROW_HOLD` - Funds held in escrow
  - `ESCROW_RELEASE` - Funds released from escrow
  - `COMMISSION_FEE` - Platform commission
- Complete audit trail with previous/current balances
- Escrow linking for transaction traceability

### 3. Escrow System

#### Escrow (`Escrow`)
- Secure fund holding during auction completion
- Automatic commission calculation
- Multiple escrow statuses:
  - `PENDING` - Escrow created but funds not held
  - `HELD` - Funds successfully held
  - `RELEASED` - Funds released to seller
  - `REFUNDED` - Funds returned to buyer
  - `DISPUTED` - Manual intervention required
  - `CANCELLED` - Escrow cancelled

## API Endpoints

### Payment Methods

#### Bank Accounts
```
POST   /api/payment/bank-accounts              # Create bank account
GET    /api/payment/bank-accounts              # List user's bank accounts
GET    /api/payment/bank-accounts/{id}         # Get bank account details
PUT    /api/payment/bank-accounts/{id}/set-default  # Set default account
DELETE /api/payment/bank-accounts/{id}         # Delete bank account
PUT    /api/payment/bank-accounts/{id}/verify  # Verify account (admin)
```

#### Credit Cards
```
POST   /api/payment/credit-cards               # Add credit card
GET    /api/payment/credit-cards               # List user's credit cards
GET    /api/payment/credit-cards/{id}          # Get credit card details
PUT    /api/payment/credit-cards/{id}/set-default   # Set default card
DELETE /api/payment/credit-cards/{id}          # Delete credit card
PUT    /api/payment/credit-cards/{id}/verify   # Verify card (admin)
```

### Escrow Management

```
GET    /api/escrow/my-escrows                  # List user's escrows
GET    /api/escrow/{id}                        # Get escrow details
POST   /api/escrow/create                      # Create escrow (admin)
PUT    /api/escrow/{id}/release                # Release escrow (admin)
PUT    /api/escrow/{id}/refund                 # Refund escrow (admin)
```

### Auction Management

```
POST   /api/auction-management/{id}/complete-with-escrow  # Complete with escrow
POST   /api/auction-management/{id}/complete              # Complete without escrow
POST   /api/auction-management/{id}/cancel                # Cancel auction
GET    /api/auction-management/{id}/winning-bid           # Get winning bid
```

### Enhanced Wallet Operations

Existing wallet endpoints now support payment method validation:

```
POST   /api/wallet/deposit                     # Deposit with payment method
POST   /api/wallet/withdraw                    # Withdraw to bank account
GET    /api/wallet                             # Get wallet info
GET    /api/wallet/transactions                # Transaction history
```

## Workflow Examples

### 1. User Adds Payment Method

```
1. User calls POST /api/payment/bank-accounts with IBAN details
2. System validates IBAN format and creates account record
3. Bank account marked as unverified initially
4. Admin verifies account via PUT /api/payment/bank-accounts/{id}/verify
5. User can now use account for withdrawals
```

### 2. Auction Completion with Escrow

```
1. Auction ends with winning bid
2. Admin calls POST /api/auction-management/{auctionId}/complete-with-escrow
3. System:
   - Validates winning bid and buyer's wallet balance
   - Creates escrow record
   - Moves funds from buyer's balance to hold balance
   - Creates ESCROW_HOLD transaction
   - Sets escrow status to HELD
4. After delivery confirmation, admin calls PUT /api/escrow/{id}/release
5. System:
   - Calculates commission (e.g., 5%)
   - Transfers net amount to seller's wallet
   - Creates EARNINGS transaction for seller
   - Creates COMMISSION_FEE transaction
   - Sets escrow status to RELEASED
```

### 3. Money Deposit with Credit Card

```
1. User adds credit card via POST /api/payment/credit-cards
2. System masks card number and detects brand
3. Payment gateway verifies card and returns token
4. User calls POST /api/wallet/deposit with:
   - amount: "100.00"
   - paymentMethod: "CREDIT_CARD"
   - paymentMethodId: "{creditCardId}"
5. System validates card ownership and processes deposit
6. Creates DEPOSIT transaction and updates wallet balance
```

## Security Features

### Authentication & Authorization
- JWT-based authentication for all endpoints
- User can only access their own payment methods and wallets
- Admin-only endpoints for verification and escrow management

### Payment Security
- Credit card numbers are masked (only last 4 digits visible)
- Payment gateway tokenization for secure card storage
- IBAN validation for bank accounts
- Payment method ownership verification

### Financial Security
- All monetary operations are atomic with transaction rollback
- Complete audit trail for all financial operations
- Escrow system prevents fraud in auction payments
- Wallet locking capability for security incidents

### Data Protection
- Sensitive financial data properly encrypted
- PCI compliance considerations for card data
- Audit logs for all payment method changes

## Database Schema

### Key Relationships
```
User 1:1 Wallet
User 1:N BankAccount
User 1:N CreditCard
Wallet 1:N Transaction
Auction 1:1 Escrow (optional)
Bid 1:1 Escrow (as winning bid)
```

### Important Indexes
```sql
-- For performance optimization
CREATE INDEX idx_wallet_user_id ON wallets(user_id);
CREATE INDEX idx_transaction_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX idx_transaction_escrow_id ON wallet_transactions(escrow_id);
CREATE INDEX idx_escrow_auction_id ON escrows(auction_id);
CREATE INDEX idx_bank_account_user_id ON bank_accounts(user_id);
CREATE INDEX idx_credit_card_user_id ON credit_cards(user_id);
```

## Configuration

### Commission Rates
Default commission rate is 5% but can be configured per auction:
```java
// In auction completion
escrowService.createEscrowForAuction(auctionId, bidId, new BigDecimal("0.05")); // 5%
```

### Auto-release Escrow
Escrows are automatically set to release after 30 days but can be manually controlled:
```java
escrow.setAutoReleaseDate(LocalDateTime.now().plusDays(30));
```

### Payment Method Limits
- Users can have multiple bank accounts and credit cards
- Only one default payment method per type
- Payment methods must be verified for withdrawals

## Error Handling

### Common Error Scenarios
1. **Insufficient Funds**: When escrow amount exceeds wallet balance
2. **Invalid Payment Method**: When payment method doesn't belong to user
3. **Escrow Already Exists**: When trying to create duplicate escrow for auction
4. **Unverified Payment Method**: When trying to withdraw to unverified account
5. **Invalid Auction State**: When auction is not in correct state for completion

### Error Response Format
```json
{
  "success": false,
  "message": "Detailed error message",
  "data": null
}
```

## Testing

Run the escrow service tests:
```bash
mvn test -Dtest=EscrowServiceTest
```

## Future Enhancements

1. **Payment Gateway Integration**: Real payment processing with iyzico, Stripe, etc.
2. **Scheduled Escrow Release**: Automatic escrow release after timeout
3. **Multi-currency Support**: Support for different currencies
4. **Recurring Payments**: Subscription-based payments
5. **Mobile Payment Integration**: Apple Pay, Google Pay support
6. **Advanced Fraud Detection**: AI-based fraud prevention
7. **Crypto Payments**: Cryptocurrency payment support