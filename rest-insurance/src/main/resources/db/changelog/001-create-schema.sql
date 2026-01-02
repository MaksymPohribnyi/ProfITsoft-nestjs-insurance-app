--liquibase formatted sql

--changeset maksymus:001-20251207
CREATE TABLE clients (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX idx_client_email ON clients(email);
-- rollback DROP TABLE clients;

--changeset maksymus:002-20251207
CREATE TABLE insurance_policies (
    id UUID PRIMARY KEY,
    policy_number VARCHAR(255) NOT NULL UNIQUE,
    policy_type VARCHAR(255),
    start_date DATE,
    end_date DATE,
    client_id UUID NOT NULL,
    CONSTRAINT fk_policy_client FOREIGN KEY (client_id) REFERENCES clients(id)
);
CREATE INDEX idx_policy_number ON insurance_policies(policy_number);
CREATE INDEX idx_policy_type ON insurance_policies(policy_type);
CREATE INDEX idx_client_id ON insurance_policies(client_id);
-- rollback DROP TABLE insurance_policies;

--changeset maksymus:003-20251207
CREATE TABLE policy_covered_risks (
    policy_id UUID NOT NULL,
    covered_risks VARCHAR(255),
    CONSTRAINT fk_risks_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies(id)
);
-- rollback DROP TABLE policy_covered_risks;
