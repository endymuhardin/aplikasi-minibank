-- Create passbooks table for tracking passbook printing state
CREATE TABLE passbooks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_accounts UUID NOT NULL UNIQUE,
    passbook_number VARCHAR(50) UNIQUE NOT NULL,

    -- Printing state tracking
    current_page INTEGER NOT NULL DEFAULT 1,
    last_printed_line INTEGER NOT NULL DEFAULT 0,
    lines_per_page INTEGER NOT NULL DEFAULT 20,

    -- Last printed transaction tracking
    id_last_printed_transaction UUID,
    last_print_date TIMESTAMP,

    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FULL', 'LOST', 'REPLACED', 'CLOSED')),

    -- Audit fields
    issued_date DATE DEFAULT CURRENT_DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),

    -- Foreign key constraints
    CONSTRAINT fk_passbooks_accounts FOREIGN KEY (id_accounts) REFERENCES accounts(id),
    CONSTRAINT fk_passbooks_last_transaction FOREIGN KEY (id_last_printed_transaction) REFERENCES transactions(id),

    -- Business rules
    CONSTRAINT chk_current_page_positive CHECK (current_page > 0),
    CONSTRAINT chk_last_printed_line_valid CHECK (last_printed_line >= 0 AND last_printed_line <= lines_per_page),
    CONSTRAINT chk_lines_per_page_valid CHECK (lines_per_page > 0 AND lines_per_page <= 30)
);

-- Create passbook_print_history table for audit trail
CREATE TABLE passbook_print_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_passbooks UUID NOT NULL,

    -- Print session details
    print_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    page_number INTEGER NOT NULL,
    start_line INTEGER NOT NULL,
    end_line INTEGER NOT NULL,
    transactions_printed INTEGER NOT NULL DEFAULT 0,

    -- First and last transaction in this print session
    id_first_transaction UUID,
    id_last_transaction UUID,

    -- Printer info (optional)
    printer_name VARCHAR(100),
    printer_port VARCHAR(50),

    -- Status
    status VARCHAR(20) DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS', 'FAILED', 'PARTIAL')),
    error_message TEXT,

    -- Audit
    printed_by VARCHAR(100),

    -- Foreign key constraints
    CONSTRAINT fk_print_history_passbooks FOREIGN KEY (id_passbooks) REFERENCES passbooks(id),
    CONSTRAINT fk_print_history_first_tx FOREIGN KEY (id_first_transaction) REFERENCES transactions(id),
    CONSTRAINT fk_print_history_last_tx FOREIGN KEY (id_last_transaction) REFERENCES transactions(id)
);

-- Create indexes for performance
CREATE INDEX idx_passbooks_account ON passbooks(id_accounts);
CREATE INDEX idx_passbooks_passbook_number ON passbooks(passbook_number);
CREATE INDEX idx_passbooks_status ON passbooks(status);
CREATE INDEX idx_passbooks_last_transaction ON passbooks(id_last_printed_transaction);

CREATE INDEX idx_print_history_passbook ON passbook_print_history(id_passbooks);
CREATE INDEX idx_print_history_print_date ON passbook_print_history(print_date);
CREATE INDEX idx_print_history_status ON passbook_print_history(status);

-- Insert sequence for passbook numbers
INSERT INTO sequence_numbers (sequence_name, last_number, prefix)
VALUES ('PASSBOOK', 0, 'PB')
ON CONFLICT (sequence_name) DO NOTHING;
