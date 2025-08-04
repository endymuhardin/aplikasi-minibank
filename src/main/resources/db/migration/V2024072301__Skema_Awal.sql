CREATE TABLE customer (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mobile_phone VARCHAR(255) NOT NULL UNIQUE,
    birth_place VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL
);

CREATE TABLE account (
    id UUID PRIMARY KEY,
    id_customer UUID NOT NULL,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    account_type VARCHAR(50) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_account_customer FOREIGN KEY (id_customer) REFERENCES customer(id)
);

CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    id_account UUID NOT NULL,
    transaction_time TIMESTAMP NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_transaction_account FOREIGN KEY (id_account) REFERENCES account(id)
);