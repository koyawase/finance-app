INSERT INTO customer (name, email, phone_number)
VALUES ('Lionel Messi', 'leothegoat@test.com', '0212121211');

INSERT INTO customer (name, email, phone_number)
VALUES ('Cristiano Ronaldo', 'cristiano@test.com', '0222121212');

INSERT INTO account (account_number, balance, account_type, customer_id, status, created_at)
SELECT '0000000001', 15000.00, 'CHECKING', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'leothegoat@test.com';

INSERT INTO account (account_number, balance, account_type, customer_id, status, created_at)
SELECT '0000000002', 5000.00, 'SAVINGS', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'leothegoat@test.com';

INSERT INTO account (account_number, balance, account_type, customer_id, status, created_at)
SELECT '0000000003', 25000.00, 'CHECKING', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'cristiano@test.com';

INSERT INTO account (account_number, balance, account_type, customer_id, status, created_at)
SELECT '0000000004', 100000.00, 'SAVINGS', id, 'ACTIVE', CURRENT_TIMESTAMP
FROM customer WHERE email = 'cristiano@test.com';
