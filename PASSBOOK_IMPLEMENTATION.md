# Passbook Printing Implementation

## Overview

This implementation provides a comprehensive passbook printing functionality for the Minibank Islamic Banking application. The feature allows bank tellers and customer service representatives to print customer account statements in a traditional passbook format.

## Features Implemented

### 1. Account Selection
- **URL**: `/passbook/select-account`
- **Purpose**: Select an active account for passbook printing
- **Features**:
  - Search by account number or account name
  - Shows only active accounts
  - Displays account details (balance, customer info, product type)
  - Preview and Print action buttons

### 2. Passbook Preview
- **URL**: `/passbook/preview/{accountId}`
- **Purpose**: Preview passbook content before printing
- **Features**:
  - Shows recent 10 transactions
  - Account information summary
  - Date filter options
  - Print button to proceed to full printing

### 3. Passbook Printing
- **URL**: `/passbook/print/{accountId}`
- **Purpose**: Generate print-ready passbook document
- **Features**:
  - Professional bank letterhead with logo
  - Complete transaction history with pagination
  - Date range filtering support
  - Print-optimized CSS for A4 paper
  - Browser print compatibility (Chrome & Firefox)

## Technical Implementation

### Controller Layer
- **`PassbookController`**: Main controller handling all passbook operations
- Follows Spring MVC patterns from technical practices guide
- Constructor injection for dependencies
- Proper error handling and validation

### Template Structure
```
src/main/resources/templates/passbook/
├── select-account.html    # Account selection page
├── preview.html          # Preview page with date filters
└── print.html           # Print-ready document
```

### Configuration
Added to `application.properties`:
```properties
# Bank Configuration for Passbook Printing
minibank.bank.name=Minibank Islamic Banking
minibank.bank.address=Jl. Raya Jakarta No. 123, Jakarta 12345, Indonesia
minibank.logo.path=/images/bank-logo.svg
```

### Repository Extensions
Extended `TransactionRepository` with new methods:
- `findByAccount(Account account, Pageable pageable)`
- `findByAccountOrderByTransactionDateAsc(Account account)`
- `findByAccountAndTransactionDateBetween(...)`
- `findByAccountAndTransactionDateGreaterThanEqual(...)`
- `findByAccountAndTransactionDateLessThan(...)`

## Bank Logo Configuration

### Current Implementation
- **Location**: `/images/bank-logo.svg`
- **Type**: SVG (scalable vector graphics)
- **Size**: 80x80 pixels optimized for print
- **Design**: Professional bank building icon with "MINIBANK" text

### Customization
To replace with your bank's logo:

1. **Replace the SVG file**:
   ```bash
   # Replace with your logo
   cp your-bank-logo.svg src/main/resources/static/images/bank-logo.svg
   ```

2. **Or update configuration**:
   ```properties
   # Use PNG/JPG format
   minibank.logo.path=/images/your-logo.png
   ```

3. **Update bank information**:
   ```properties
   minibank.bank.name=Your Bank Name
   minibank.bank.address=Your Bank Address
   ```

## Print Compatibility

### Supported Browsers
- **Chrome**: Full compatibility with print preview
- **Firefox**: Full compatibility with print preview  
- **Safari**: Basic compatibility
- **Edge**: Full compatibility

### Print Settings Recommendations
1. **Paper Size**: A4
2. **Margins**: 0.5 inch (default)
3. **Headers/Footers**: Disabled (recommended)
4. **Background Graphics**: Enabled

### CSS Print Features
- **@media print**: Optimized styles for printing
- **Page breaks**: Controlled page breaking for long transaction lists
- **Font optimization**: Courier New for banking documents
- **Print-only elements**: Bank compliance statements
- **Screen-only elements**: Print buttons and navigation

## Navigation Integration

Added to main sidebar navigation:
```html
<!-- Passbook Printing - Teller and CS -->
<a id="passbook-link" 
   sec:authorize="hasAnyAuthority('ACCOUNT_VIEW', 'TRANSACTION_VIEW')" 
   th:href="@{/passbook/select-account}">
    <svg>...</svg>
    Passbook
</a>
```

## Security and Permissions

### Required Authorities
- **`ACCOUNT_VIEW`**: View account information
- **`TRANSACTION_VIEW`**: View transaction history

### Access Control
- Only users with appropriate permissions can access passbook features
- Only active accounts are available for printing
- Read-only operations - no data modification

## Usage Instructions

### For Bank Staff

1. **Access Passbook Feature**:
   - Login to the system
   - Click "Passbook" in the sidebar navigation

2. **Select Account**:
   - Search for customer account by number or name
   - Click "Preview" to see recent transactions
   - Click "Print" for immediate printing

3. **Apply Date Filters** (Optional):
   - In preview page, set "From Date" and "To Date"
   - Click "Apply Filter & Print"

4. **Print Document**:
   - Review the document in print view
   - Click "Print Passbook" button
   - Use browser's print dialog
   - Recommended: Disable headers/footers

### Print Quality Tips

1. **Browser Settings**:
   - Use Chrome or Firefox for best results
   - Disable "Print headers and footers"
   - Enable "Background graphics"

2. **Paper Recommendations**:
   - A4 white paper (80gsm or higher)
   - Laser printer for crisp text
   - Inkjet acceptable for internal use

## Transaction Display Format

### Transaction Table Columns
1. **Date**: DD/MM/YYYY format with time
2. **Description**: Transaction description + reference number
3. **Transaction No.**: System-generated transaction number
4. **Type**: Transaction type + channel
5. **Debit**: Withdrawal amounts (red)
6. **Credit**: Deposit amounts (green)
7. **Balance**: Running balance after transaction

### Balance Calculation
- Displays running balance after each transaction
- Color-coded amounts (green for credit, red for debit)
- IDR currency formatting with thousand separators

## Error Handling

### Common Scenarios
1. **Account Not Found**: Redirects to selection with error message
2. **Inactive Account**: Cannot print passbook for non-active accounts
3. **Invalid Date Format**: Validates date inputs with error feedback
4. **No Transactions**: Shows appropriate empty state message

### Logging
- Transaction access logged for audit purposes
- Print operations logged with user information
- Error conditions logged for troubleshooting

## Future Enhancements

### Potential Improvements
1. **PDF Generation**: Server-side PDF generation option
2. **Email Delivery**: Send passbook via email
3. **Digital Signatures**: Electronic signature support
4. **Multi-language**: Support for Bahasa Indonesia
5. **Batch Printing**: Multiple accounts in one operation
6. **Custom Templates**: Bank-specific passbook designs

### Technical Debt
1. **Performance**: Optimize for accounts with large transaction history
2. **Caching**: Cache frequently accessed account data
3. **Pagination**: Implement pagination for very long transaction lists

## Testing

### Manual Testing Checklist
- [ ] Account selection works with search
- [ ] Preview shows correct data
- [ ] Date filtering works properly
- [ ] Print view displays correctly
- [ ] Browser print function works
- [ ] Error handling works
- [ ] Navigation links work
- [ ] Permissions enforced correctly

### Browser Testing
- [ ] Chrome print compatibility
- [ ] Firefox print compatibility
- [ ] Print preview accuracy
- [ ] Page break handling
- [ ] Logo display correctly

## Support

### Common Issues
1. **Logo not displaying**: Check file path and permissions
2. **Print format issues**: Verify browser print settings
3. **Access denied**: Check user permissions
4. **Slow loading**: Consider transaction volume

### Configuration Verification
```bash
# Verify logo file exists
ls -la src/main/resources/static/images/bank-logo.svg

# Check application properties
grep minibank src/main/resources/application.properties
```

This implementation follows the established patterns in the codebase and provides a robust, print-ready passbook solution compatible with modern browsers.