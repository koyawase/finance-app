INSERT INTO customer (name, email, phone_number)
VALUES ('Lionel Messi', 'leothegoat@test.com', '0212121211');

INSERT INTO customer (name, email, phone_number)
VALUES ('Cristiano Ronaldo', 'cristiano@test.com', '0222121212');

INSERT INTO account (account_number, account_name, balance, account_type, customer_id, status, created_at)
SELECT '0000000001', 'Everyday spending', 15000.00, 'CHECKING', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'leothegoat@test.com';

INSERT INTO account (account_number, account_name, balance, account_type, customer_id, status, created_at)
SELECT '0000000002', 'Holiday savings', 5000.00, 'SAVINGS', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'leothegoat@test.com';

INSERT INTO account (account_number, account_name, balance, account_type, customer_id, status, created_at)
SELECT '0000000003', 'Daily expenses', 25000.00, 'CHECKING', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'cristiano@test.com';

INSERT INTO account (account_number, account_name, balance, account_type, customer_id, status, created_at)
SELECT '0000000004', 'Future savings', 100000.00, 'SAVINGS', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'cristiano@test.com';

-- Opening credits start the history before the mock transfers below.
INSERT INTO transaction (account_id, type, amount, balance_after, reference, created_at)
SELECT id, 'CREDIT', 16000.00, 16000.00, 'Opening balance', TIMESTAMP WITH TIME ZONE '2026-10-12 09:00:00+00'
FROM account WHERE account_number = '0000000001';

INSERT INTO transaction (account_id, type, amount, balance_after, reference, created_at)
SELECT id, 'CREDIT', 5000.00, 5000.00, 'Opening balance', TIMESTAMP WITH TIME ZONE '2026-10-12 09:00:00+00'
FROM account WHERE account_number = '0000000002';

INSERT INTO transaction (account_id, type, amount, balance_after, reference, created_at)
SELECT id, 'CREDIT', 24000.00, 24000.00, 'Opening balance', TIMESTAMP WITH TIME ZONE '2026-10-12 09:00:00+00'
FROM account WHERE account_number = '0000000003';

INSERT INTO transaction (account_id, type, amount, balance_after, reference, created_at)
SELECT id, 'CREDIT', 100000.00, 100000.00, 'Opening balance', TIMESTAMP WITH TIME ZONE '2026-10-12 09:00:00+00'
FROM account WHERE account_number = '0000000004';

-- Transfer 1: Messi checking -> Ronaldo checking.
INSERT INTO transfer (from_account_id, to_account_id, amount, reference, created_at)
SELECT sender.id, recipient.id, 750.00, 'Shared expenses', TIMESTAMP WITH TIME ZONE '2026-10-12 10:00:00+00'
FROM account sender CROSS JOIN account recipient
WHERE sender.account_number = '0000000001' AND recipient.account_number = '0000000003';

INSERT INTO transaction (account_id, transfer_id, type, amount, balance_after, reference, created_at)
SELECT from_account_id, id, 'DEBIT', amount, 15250.00, reference, created_at
FROM transfer WHERE reference = 'Shared expenses';

INSERT INTO transaction (account_id, transfer_id, type, amount, balance_after, reference, created_at)
SELECT to_account_id, id, 'CREDIT', amount, 24750.00, reference, created_at
FROM transfer WHERE reference = 'Shared expenses';

-- Transfer 2: Messi checking -> Messi savings.
INSERT INTO transfer (from_account_id, to_account_id, amount, reference, created_at)
SELECT sender.id, recipient.id, 250.00, 'Savings top up', TIMESTAMP WITH TIME ZONE '2026-10-12 11:00:00+00'
FROM account sender CROSS JOIN account recipient
WHERE sender.account_number = '0000000001' AND recipient.account_number = '0000000002';

INSERT INTO transaction (account_id, transfer_id, type, amount, balance_after, reference, created_at)
SELECT from_account_id, id, 'DEBIT', amount, 15000.00, reference, created_at
FROM transfer WHERE reference = 'Savings top up';

INSERT INTO transaction (account_id, transfer_id, type, amount, balance_after, reference, created_at)
SELECT to_account_id, id, 'CREDIT', amount, 5250.00, reference, created_at
FROM transfer WHERE reference = 'Savings top up';

-- Transfer 3: Messi savings -> Ronaldo checking.
INSERT INTO transfer (from_account_id, to_account_id, amount, reference, created_at)
SELECT sender.id, recipient.id, 250.00, 'Match tickets', TIMESTAMP WITH TIME ZONE '2026-10-12 12:00:00+00'
FROM account sender CROSS JOIN account recipient
WHERE sender.account_number = '0000000002' AND recipient.account_number = '0000000003';

INSERT INTO transaction (account_id, transfer_id, type, amount, balance_after, reference, created_at)
SELECT from_account_id, id, 'DEBIT', amount, 5000.00, reference, created_at
FROM transfer WHERE reference = 'Match tickets';

INSERT INTO transaction (account_id, transfer_id, type, amount, balance_after, reference, created_at)
SELECT to_account_id, id, 'CREDIT', amount, 25000.00, reference, created_at
FROM transfer WHERE reference = 'Match tickets';
