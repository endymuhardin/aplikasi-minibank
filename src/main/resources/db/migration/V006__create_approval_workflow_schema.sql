-- Create approval workflow schema
-- Add approval_status columns to customers and accounts tables
-- Create approval_requests table for tracking approval workflow

-- Add approval_status to customers table
ALTER TABLE customers
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'APPROVED'
CHECK (approval_status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED'));

-- Add approval_status to accounts table
ALTER TABLE accounts
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'APPROVED'
CHECK (approval_status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED'));

-- Create approval_requests table
CREATE TABLE approval_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_type VARCHAR(50) NOT NULL CHECK (request_type IN ('CUSTOMER_CREATION', 'ACCOUNT_OPENING')),
    entity_type VARCHAR(20) NOT NULL CHECK (entity_type IN ('CUSTOMER', 'ACCOUNT')),
    entity_id UUID NOT NULL,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    requested_by VARCHAR(100) NOT NULL,
    request_notes TEXT,
    requested_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(100),
    review_notes TEXT,
    reviewed_date TIMESTAMP,
    rejection_reason TEXT,
    branch_id UUID REFERENCES branches(id),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient queries
CREATE INDEX idx_approval_requests_status ON approval_requests(approval_status);
CREATE INDEX idx_approval_requests_entity ON approval_requests(entity_type, entity_id);
CREATE INDEX idx_approval_requests_branch ON approval_requests(branch_id);
CREATE INDEX idx_approval_requests_requested_by ON approval_requests(requested_by);
CREATE INDEX idx_approval_requests_request_type ON approval_requests(request_type);
CREATE INDEX idx_approval_requests_requested_date ON approval_requests(requested_date);

-- Create composite indexes for common queries
CREATE INDEX idx_approval_requests_branch_status ON approval_requests(branch_id, approval_status);
CREATE INDEX idx_approval_requests_status_date ON approval_requests(approval_status, requested_date DESC);

-- Add comments for documentation
COMMENT ON TABLE approval_requests IS 'Tracks approval workflow for customer and account creation';
COMMENT ON COLUMN approval_requests.request_type IS 'Type of approval request: CUSTOMER_CREATION or ACCOUNT_OPENING';
COMMENT ON COLUMN approval_requests.entity_type IS 'Entity being approved: CUSTOMER or ACCOUNT';
COMMENT ON COLUMN approval_requests.entity_id IS 'UUID of the customer or account being approved';
COMMENT ON COLUMN approval_requests.approval_status IS 'Current status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN customers.approval_status IS 'Approval status of customer: PENDING_APPROVAL, APPROVED, or REJECTED';
COMMENT ON COLUMN accounts.approval_status IS 'Approval status of account: PENDING_APPROVAL, APPROVED, or REJECTED';
