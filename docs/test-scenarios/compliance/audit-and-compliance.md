# Test Scenarios: Audit and Compliance

## Overview
Dokumen ini berisi skenario test untuk fitur audit and compliance dalam aplikasi minibank Islam. Audit dan compliance mencakup regulatory reporting, audit trail, transaction monitoring, AML (Anti-Money Laundering), KYC (Know Your Customer), dan Shariah compliance monitoring.

## Implementation Status
**✅ Implemented**: Basic audit fields (created_by, created_date, updated_by, updated_date)
**🔄 Partially Implemented**: RBAC system dengan user permissions, transaction logging
**❌ Not Implemented**: Complete audit trail system, regulatory reporting, AML monitoring

## Preconditions
- Database PostgreSQL berjalan dengan audit triggers
- Aplikasi Spring Boot aktif dengan proper logging configuration
- User sudah login dengan appropriate audit permissions
- Audit trail tables configured dan functional
- Regulatory compliance framework setup
- Shariah governance framework active

## Regulatory Framework

### Indonesian Banking Regulations
- Bank Indonesia (BI) regulations
- Otoritas Jasa Keuangan (OJK) requirements
- Islamic banking specific regulations (POJK)
- Anti-Money Laundering (AML) compliance
- Know Your Customer (KYC) requirements

### Audit Requirements
Based on existing entity audit fields:
```java
// Base audit fields dalam entities
@CreatedDate
private LocalDateTime createdDate;

@LastModifiedDate  
private LocalDateTime updatedDate;

@CreatedBy
private String createdBy;

@LastModifiedBy
private String updatedBy;
```

## Test Cases

### TC-AC-001: Transaction Audit Trail Verification
**Deskripsi**: Comprehensive audit trail untuk all financial transactions

**Test Data**:
- Account: ACC0000001
- Transaction Type: DEPOSIT
- Amount: 1000000.00
- User: teller1
- Date: 2024-08-15 10:30:00
- IP Address: 192.168.1.100
- Terminal ID: TERMINAL-001

**Steps**:
1. Login sebagai teller1 dari terminal specific
2. Process deposit transaction: 1,000,000.00
3. Verify transaction completion
4. Check audit trail generation
5. Verify audit data completeness
6. Test audit trail query functionality

**Expected Result**:
- **Transaction Record**:
  - Transaction ID: Unique UUID
  - Transaction Number: Auto-generated (TXN0000xxx)
  - Amount: 1,000,000.00
  - Created by: teller1
  - Created date: 2024-08-15 10:30:00
- **Audit Trail Components**:
  - User identification: teller1
  - Session tracking: Session ID recorded
  - IP address logging: 192.168.1.100
  - Terminal identification: TERMINAL-001
  - Timestamp precision: Millisecond accuracy
  - Action performed: TRANSACTION_DEPOSIT
- **Data Integrity**:
  - Before/after balance recorded
  - Account state changes logged
  - Immutable audit records
  - Cryptographic hash untuk integrity
- **Compliance Fields**:
  - Regulatory reference numbers
  - Anti-fraud check results
  - AML screening status
  - Business day validation

### TC-AC-002: User Activity Monitoring
**Deskripsi**: Comprehensive user activity tracking dan anomaly detection

**Test Data**:
- User: cs1 (Customer Service)
- Activities: Multiple customer lookups, account openings
- Time Period: 8 hours working session
- Expected Pattern: Normal customer service activities
- Anomaly: Multiple failed login attempts

**Steps**:
1. Monitor normal user activities (cs1):
   - Customer searches: 50 queries
   - Account openings: 10 accounts
   - Transaction processing: 25 transactions
2. Simulate suspicious activities:
   - Rapid multiple customer data access
   - Unusual time activity (outside business hours)
   - Failed permission attempts
3. Generate user activity report
4. Test anomaly detection triggers
5. Verify alert generation

**Expected Result**:
- **Normal Activity Tracking**:
  - All actions logged dengan timestamp
  - Customer interactions recorded
  - Transaction processing tracked
  - Login/logout sessions maintained
- **Anomaly Detection**:
  - Rapid data access flagged
  - Unusual time patterns detected
  - Failed permission attempts recorded
  - Geographic location anomalies noted
- **Alert Generation**:
  - Real-time anomaly alerts
  - Security team notifications
  - Manager escalation triggers
  - Automated account locking (if severe)
- **Reporting**:
  - Daily activity summaries
  - Weekly behavior analysis
  - Monthly compliance reports
  - Annual audit trail exports

### TC-AC-003: KYC (Know Your Customer) Compliance
**Deskripsi**: Complete KYC compliance tracking dan verification

**Test Data**:
- Customer: New customer registration
- Customer Type: Personal
- Required Documents: KTP, NPWP, proof of income
- Risk Classification: Low/Medium/High
- PEP Status: Non-PEP (Politically Exposed Person)
- Sanctions Screening: Required

**Steps**:
1. Initiate new customer KYC process
2. Collect required documents:
   - KTP (Identity Card): 3271234567890123
   - NPWP (Tax ID): 12.345.678.9-123.456
   - Proof of income: Salary certificate
   - Proof of address: Utility bill
3. Perform identity verification:
   - Document authenticity check
   - Biometric verification (if available)
   - Address verification
4. Conduct risk assessment:
   - Source of funds verification
   - Business relationship purpose
   - Expected transaction patterns
5. Perform sanctions screening:
   - Name matching against sanctions lists
   - PEP database checking
   - Adverse media screening
6. Complete KYC classification
7. Generate KYC compliance certificate

**Expected Result**:
- **Document Verification**:
  - KTP verified authentic
  - NPWP cross-checked dengan tax authority
  - Income documents validated
  - Address verification completed
- **Risk Assessment**:
  - Customer risk score calculated
  - Risk factors identified
  - Risk mitigation measures applied
  - Ongoing monitoring requirements set
- **Sanctions Screening**:
  - No matches found dalam sanctions lists
  - PEP status confirmed negative
  - Adverse media screening clear
  - Screening results documented
- **Compliance Certification**:
  - KYC status: APPROVED
  - Risk classification: LOW
  - Review date scheduled: Annual
  - Compliance officer approval recorded
- **Audit Trail**:
  - Complete KYC process logged
  - Document verification results stored
  - Risk assessment reasoning documented
  - Screening results archived

### TC-AC-004: AML (Anti-Money Laundering) Transaction Monitoring
**Deskripsi**: Real-time AML monitoring dan suspicious transaction detection

**Test Data**:
- Account: ACC0000001 (High-value account)
- Monitoring Period: 30 days
- Transaction Patterns: Various amounts dan frequencies
- Suspicious Indicators: Cash transactions > 100M, unusual patterns
- CTR Threshold: 500,000,000 (500 million IDR)

**Steps**:
1. Setup AML monitoring rules:
   - Cash transaction threshold: 100,000,000
   - Daily accumulation limit: 500,000,000
   - Velocity triggers: 10+ transactions/hour
   - Cross-border activity monitoring
2. Process normal transactions:
   - Regular deposits: 5,000,000 - 50,000,000
   - Regular withdrawals: 10,000,000 - 30,000,000
   - Normal frequency: 2-5 transactions/day
3. Simulate suspicious activities:
   - Large cash deposit: 150,000,000
   - Multiple cash deposits: 10 x 50,000,000 (same day)
   - Round number transactions: 100,000,000
   - Rapid succession: 15 transactions dalam 30 minutes
4. Verify AML alert generation
5. Test STR (Suspicious Transaction Report) creation
6. Verify CTR (Currency Transaction Report) triggers

**Expected Result**:
- **Normal Transaction Processing**:
  - All transactions processed normally
  - AML scoring within acceptable range
  - No alerts generated
  - Regular monitoring continue
- **Suspicious Activity Detection**:
  - Large cash deposit flagged
  - Multiple deposits pattern detected
  - Round amount transactions noted
  - Velocity trigger activated
- **Alert Management**:
  - Real-time alerts generated
  - Compliance team notifications sent
  - Transaction temporarily held (if required)
  - Investigation case opened
- **Regulatory Reporting**:
  - STR prepared untuk submission
  - CTR generated for large transactions
  - Documentation complete
  - PPATK (Indonesian FIU) reporting ready
- **Case Management**:
  - Investigation case ID assigned
  - Evidence collection initiated
  - Timeline tracking established
  - Resolution tracking implemented

### TC-AC-005: Shariah Compliance Audit
**Deskripsi**: Comprehensive Shariah compliance monitoring dan reporting

**Test Data**:
- Islamic Banking Products: All MUDHARABAH, WADIAH, MUSHARAKAH
- Monitoring Period: Monthly Shariah compliance review
- Shariah Board: Regular review meetings
- Compliance Violations: Track non-compliant activities

**Steps**:
1. Setup Shariah compliance monitoring:
   - Product compliance rules
   - Transaction compliance checks
   - Customer activity screening
   - Investment compliance monitoring
2. Monitor Islamic banking activities:
   - Mudharabah profit sharing calculations
   - Wadiah account operations
   - Musharakah partnership management
   - Asset-based financing (Ijarah)
3. Check compliance violations:
   - Interest-based calculations (prohibited)
   - Non-halal business funding
   - Guaranteed return products
   - Speculation activities (maysir)
4. Generate Shariah compliance reports
5. Schedule Shariah board review
6. Track corrective actions

**Expected Result**:
- **Product Compliance**:
  - All Islamic products Shariah-compliant
  - Profit-loss sharing authentic
  - Asset ownership clear
  - No interest-based elements
- **Transaction Compliance**:
  - All transactions follow Shariah principles
  - No prohibited activities funded
  - Proper documentation maintained
  - Shariah advisor approvals recorded
- **Compliance Reporting**:
  - Monthly compliance dashboard
  - Violation tracking report
  - Corrective action status
  - Shariah board meeting minutes
- **Audit Trail**:
  - Complete Shariah review process
  - Board member approvals
  - Compliance certification
  - Annual Shariah audit report

### TC-AC-006: Regulatory Reporting Generation
**Deskripsi**: Automated generation of regulatory reports untuk various authorities

**Test Data**:
- Reporting Period: Monthly/Quarterly/Annual
- Target Authorities: BI, OJK, PPATK
- Report Types: Balance sheet, P&L, CTR, STR, LBU
- Data Sources: All transaction data, customer data, account data

**Steps**:
1. Configure reporting parameters:
   - Reporting periods (monthly, quarterly, annual)
   - Regulatory authority requirements
   - Data aggregation rules
   - Report format specifications
2. Generate regulatory reports:
   - LBU (Laporan Berkala Usaha) untuk OJK
   - CTR (Currency Transaction Report) untuk PPATK
   - STR (Suspicious Transaction Report) untuk PPATK
   - Islamic banking specific reports
3. Validate report accuracy:
   - Data reconciliation checks
   - Cross-reference verification
   - Mathematical accuracy validation
   - Compliance rule verification
4. Submit reports electronically:
   - Digital signatures applied
   - Encryption protocols used
   - Submission confirmation received
   - Archive copies maintained
5. Track submission status
6. Handle regulatory feedback

**Expected Result**:
- **Report Generation**:
  - All reports generated accurately
  - Data reconciliation 100% match
  - Format compliance verified
  - Submission deadlines met
- **Data Quality**:
  - No data discrepancies found
  - Complete transaction coverage
  - Accurate customer information
  - Proper classification applied
- **Submission Process**:
  - Electronic submission successful
  - Digital signatures valid
  - Confirmation receipts received
  - Archive copies secured
- **Compliance Tracking**:
  - Submission log maintained
  - Regulatory feedback tracked
  - Corrective actions implemented
  - Compliance status updated

### TC-AC-007: Data Privacy dan Protection Compliance
**Deskripsi**: Data privacy compliance volgens Indonesian data protection regulations

**Test Data**:
- Customer Data: Personal information, financial data
- Access Controls: Role-based permissions
- Data Retention: Regulatory retention periods
- Data Masking: Sensitive data protection

**Steps**:
1. Verify data access controls:
   - Role-based access restrictions
   - Need-to-know principle enforcement
   - Manager approval requirements
   - Customer consent verification
2. Test data masking/encryption:
   - PII (Personally Identifiable Information) masking
   - Financial data encryption
   - Account number partial masking
   - Password hashing verification
3. Validate data retention policies:
   - Automatic data archival
   - Retention period compliance
   - Secure data destruction
   - Legal hold procedures
4. Test customer data rights:
   - Data access requests
   - Data correction procedures
   - Data deletion requests (right to be forgotten)
   - Data portability requirements
5. Audit data sharing:
   - Third-party data sharing controls
   - Consent management
   - Data sharing audit trails
   - Cross-border transfer restrictions

**Expected Result**:
- **Access Control Verification**:
  - Role-based access working correctly
  - Unauthorized access prevented
  - Manager approvals enforced
  - Customer consent tracked
- **Data Protection**:
  - Sensitive data properly masked
  - Encryption standards applied
  - Data integrity maintained
  - Security breach prevention
- **Retention Compliance**:
  - Data retention policies enforced
  - Automatic archival functioning
  - Secure destruction verified
  - Legal hold procedures active
- **Customer Rights**:
  - Data access requests processed
  - Correction procedures working
  - Deletion requests handled
  - Portability features available

### TC-AC-008: Fraud Detection dan Prevention
**Deskripsi**: Real-time fraud detection dan prevention mechanisms

**Test Data**:
- Account: Multiple customer accounts
- Fraud Scenarios: Card skimming, account takeover, transaction fraud
- Detection Rules: Velocity, geography, behavior analysis
- Prevention Measures: Account blocking, transaction limits

**Steps**:
1. Setup fraud detection rules:
   - Transaction velocity limits
   - Geographic anomaly detection
   - Behavioral pattern analysis
   - Device fingerprinting
2. Simulate fraud scenarios:
   - Multiple rapid transactions
   - Geographic impossibility (transactions dari different cities)
   - Unusual transaction patterns
   - Large amount transactions outside normal behavior
3. Test fraud detection triggers:
   - Real-time scoring mechanisms
   - Alert generation systems
   - Automatic blocking procedures
   - Investigation workflow initiation
4. Verify prevention measures:
   - Transaction blocking effectiveness
   - Account freezing procedures
   - Customer notification systems
   - Recovery procedures
5. Test false positive handling:
   - Legitimate transaction processing
   - Customer appeal procedures
   - Manual override capabilities
   - Learning algorithm adjustments

**Expected Result**:
- **Fraud Detection**:
  - Real-time scoring accurate
  - Suspicious patterns identified
  - Alerts generated immediately
  - Investigation cases opened
- **Prevention Measures**:
  - Fraudulent transactions blocked
  - Accounts frozen when necessary
  - Customer notifications sent
  - Security teams alerted
- **False Positive Management**:
  - Legitimate transactions processed
  - Appeal procedures functional
  - Manual overrides available
  - System learning improved
- **Investigation Support**:
  - Complete evidence collection
  - Timeline reconstruction
  - Communication trails preserved
  - Law enforcement cooperation

### TC-AC-009: Business Continuity dan Disaster Recovery Audit
**Deskripsi**: Business continuity planning dan disaster recovery testing

**Test Data**:
- Critical Systems: Core banking, customer database, transaction processing
- Recovery Objectives: RTO (Recovery Time Objective), RPO (Recovery Point Objective)
- Backup Systems: Hot standby, cold backup, cloud backup
- Test Scenarios: System failure, natural disaster, cyber attack

**Steps**:
1. Document business continuity plans:
   - Critical system identification
   - Recovery priority matrix
   - Communication procedures
   - Resource allocation plans
2. Test disaster recovery procedures:
   - System failover testing
   - Data backup verification
   - Communication system testing
   - Staff notification procedures
3. Verify recovery capabilities:
   - System restoration timing
   - Data integrity verification
   - Service availability testing
   - Customer access restoration
4. Test backup systems:
   - Hot standby activation
   - Cold backup restoration
   - Cloud backup accessibility
   - Cross-site replication
5. Conduct business impact analysis:
   - Service disruption assessment
   - Financial impact calculation
   - Customer impact analysis
   - Regulatory reporting requirements

**Expected Result**:
- **Continuity Planning**:
  - Comprehensive plans documented
  - Regular plan updates maintained
  - Staff training completed
  - Vendor agreements secured
- **Recovery Testing**:
  - RTO targets met consistently
  - RPO objectives achieved
  - System restoration successful
  - Data integrity verified
- **Backup Verification**:
  - All backup systems functional
  - Data recovery successful
  - Cross-site replication working
  - Cloud backup accessible
- **Impact Assessment**:
  - Business impact minimized
  - Customer service maintained
  - Regulatory requirements met
  - Financial losses controlled

### TC-AC-010: Security Audit dan Penetration Testing
**Deskripsi**: Comprehensive security audit dan vulnerability assessment

**Test Data**:
- Application Security: Web application, API endpoints, database
- Infrastructure Security: Networks, servers, firewalls
- Access Controls: Authentication, authorization, session management
- Data Security: Encryption, data masking, secure transmission

**Steps**:
1. Conduct security vulnerability assessment:
   - Application scanning (OWASP top 10)
   - Network vulnerability scanning
   - Database security assessment
   - Configuration review
2. Perform penetration testing:
   - External penetration testing
   - Internal network testing
   - Social engineering assessment
   - Physical security testing
3. Test security controls:
   - Authentication mechanisms
   - Authorization effectiveness
   - Session management security
   - Input validation procedures
4. Verify data protection:
   - Encryption implementation
   - Data transmission security
   - Storage encryption verification
   - Key management procedures
5. Review security policies:
   - Security policy compliance
   - Incident response procedures
   - Security awareness training
   - Vendor security assessments

**Expected Result**:
- **Vulnerability Assessment**:
  - Critical vulnerabilities identified
  - Risk ratings assigned
  - Remediation recommendations provided
  - Timeline for fixes established
- **Penetration Testing**:
  - Security controls tested
  - Exploit scenarios documented
  - Evidence collected
  - Recommendations prioritized
- **Control Effectiveness**:
  - Security controls functioning
  - Authentication mechanisms secure
  - Authorization properly enforced
  - Session management robust
- **Compliance Verification**:
  - Security policies followed
  - Regulatory requirements met
  - Industry standards compliance
  - Best practices implementation

## Performance Test Cases

### TC-AC-P001: Audit Log Performance
**Deskripsi**: Test audit logging performance under high transaction load

**Test Scenario**:
- Transaction Volume: 10,000 transactions per hour
- Concurrent Users: 100 users
- Audit Log Generation: Real-time
- Database Performance: Monitoring required

**Expected Result**:
- Audit logging < 10ms overhead per transaction
- Database performance maintained
- No audit log loss
- Query performance acceptable

### TC-AC-P002: Compliance Report Generation Performance
**Deskripsi**: Test regulatory report generation performance

**Test Scenario**:
- Data Volume: 1 million transactions
- Report Types: Multiple regulatory reports
- Generation Time: Business hour processing
- System Resources: Monitor CPU/memory usage

**Expected Result**:
- Report generation < 30 minutes
- System remains responsive
- Report accuracy maintained
- Resource usage optimized

## Integration Test Cases

### TC-AC-I001: End-to-End Compliance Workflow
**Deskripsi**: Complete compliance workflow dari transaction to regulatory reporting

**Steps**:
1. Process various transactions
2. Generate audit trails
3. Monitor AML triggers
4. Create compliance reports
5. Submit regulatory filings
6. Handle regulatory feedback

**Expected Result**:
- Complete workflow functioning
- All compliance requirements met
- Audit trails complete
- Regulatory submissions successful

### TC-AC-I002: External System Integration
**Deskripsi**: Integration dengan external compliance dan regulatory systems

**Steps**:
1. Connect to PPATK reporting system
2. Interface dengan BI reporting portal
3. Integrate dengan OJK submission system
4. Connect to sanctions screening services
5. Interface dengan credit bureau systems

**Expected Result**:
- All external connections functional
- Data transmission secure
- Authentication successful
- Report submissions acknowledged

## Database Validation

### Audit Trail Data Integrity
```sql
-- Check audit trail completeness
SELECT 
  table_name,
  COUNT(*) as total_records,
  COUNT(created_by) as records_with_creator,
  COUNT(created_date) as records_with_date
FROM information_schema.tables t
JOIN audit_trail a ON t.table_name = a.table_name
WHERE t.table_schema = 'public'
GROUP BY table_name;

-- Verify transaction audit trail
SELECT 
  t.transaction_number,
  t.created_by,
  t.created_date,
  at.action_type,
  at.old_values,
  at.new_values
FROM transactions t
LEFT JOIN audit_trail at ON t.id = at.record_id
WHERE t.transaction_date >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY t.created_date DESC;

-- Check user activity logging
SELECT 
  ua.user_id,
  ua.action_type,
  ua.target_table,
  ua.target_id,
  ua.ip_address,
  ua.session_id,
  ua.created_date
FROM user_activities ua
WHERE ua.created_date >= CURRENT_DATE - INTERVAL '1 day'
ORDER BY ua.created_date DESC;

-- Validate compliance status
SELECT 
  c.customer_number,
  c.kyc_status,
  c.kyc_completion_date,
  c.risk_classification,
  c.last_review_date,
  CASE 
    WHEN c.last_review_date < CURRENT_DATE - INTERVAL '1 year' 
    THEN 'REVIEW_REQUIRED' 
    ELSE 'CURRENT' 
  END as compliance_status
FROM customers c
WHERE c.kyc_status IS NOT NULL;
```

## API Test Examples

### REST API Calls
```bash
# Get Audit Trail
curl -X GET "http://localhost:8080/api/audit/transactions/{transactionId}" \
  -H "Authorization: Bearer <token>"

# Generate Compliance Report
curl -X POST http://localhost:8080/api/compliance/reports \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "reportType": "CTR",
    "startDate": "2024-08-01",
    "endDate": "2024-08-31",
    "authority": "PPATK"
  }'

# Check AML Status
curl -X GET "http://localhost:8080/api/aml/status/{customerId}" \
  -H "Authorization: Bearer <token>"

# Submit STR Report
curl -X POST http://localhost:8080/api/aml/str \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "transactionIds": ["uuid1", "uuid2"],
    "suspiciousActivity": "Multiple large cash deposits",
    "investigationNotes": "Customer behavior analysis",
    "reportingOfficer": "compliance1"
  }'

# Get User Activity Log
curl -X GET "http://localhost:8080/api/audit/user-activities?userId={userId}&startDate=2024-08-01" \
  -H "Authorization: Bearer <token>"
```

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup audit trail test data
DELETE FROM audit_trail 
WHERE created_by LIKE 'test%' 
OR record_id IN (
  SELECT id FROM transactions WHERE description LIKE '%test%'
);

-- Cleanup user activity logs
DELETE FROM user_activities 
WHERE user_id IN (
  SELECT id FROM users WHERE username LIKE 'test%'
);

-- Cleanup compliance test records
DELETE FROM kyc_records 
WHERE customer_id IN (
  SELECT id FROM customers WHERE customer_number LIKE 'TEST%'
);

-- Cleanup AML test records
DELETE FROM aml_alerts 
WHERE customer_id IN (
  SELECT id FROM customers WHERE customer_number LIKE 'TEST%'
);

-- Reset compliance status untuk test customers
UPDATE customers 
SET kyc_status = NULL,
    risk_classification = NULL,
    last_review_date = NULL
WHERE customer_number LIKE 'TEST%';
```

## Regulatory Compliance Matrix

### Indonesian Banking Regulations:
1. **Bank Indonesia (BI) Regulations**:
   - Minimum capital requirements
   - Liquidity ratio compliance
   - Risk management frameworks
   - Business continuity planning

2. **OJK (Financial Services Authority)**:
   - Consumer protection requirements
   - Market conduct regulations
   - Financial product compliance
   - Corporate governance standards

3. **PPATK (Financial Intelligence Unit)**:
   - Anti-money laundering (AML)
   - Counter-terrorism financing (CTF)
   - Suspicious transaction reporting
   - Customer due diligence

4. **Islamic Banking Specific (POJK)**:
   - Shariah compliance requirements
   - Islamic product regulations
   - Shariah governance framework
   - Profit-loss sharing compliance

This comprehensive audit and compliance test scenario ensures complete coverage of regulatory requirements, audit trails, monitoring capabilities, dan compliance verification untuk Indonesian Islamic banking operations.