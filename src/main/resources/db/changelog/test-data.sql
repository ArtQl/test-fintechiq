--liquibase formatted sql

--changeset artq:2 runOnChange:true
-- Test data for stop factor tests

-- Test case 1: Ogada Isaac Abraham Samuel vs ISAAC ABRAHAM SAMUEL OGADA
INSERT INTO request_content (loan_request_id, content, created_at)
VALUES ('0190e7b2-14a8-72e4-8528-89a8cd91d430',
        '{
          "loanRequestID": "0190e7b2-14a8-72e4-8528-89a8cd91d430",
          "regPerson": {
            "firstName": "Ogada",
            "middleName": "Isaac Abraham",
            "lastName": "Samuel"
          },
          "creditBureau": {
            "account_info": [
              {
                "account_number": "25470392059420191215",
                "account_status": "Closed",
                "current_balance": "0.00000",
                "date_opened": "2019-12-15",
                "days_in_arrears": 0,
                "delinquency_code": "003",
                "highest_days_in_arrears": 51,
                "is_your_account": false,
                "last_payment_amount": "0.00000",
                "last_payment_date": null,
                "loaded_at": "2020-04-16",
                "original_amount": "608.10000",
                "overdue_balance": "0.00000",
                "overdue_date": null,
                "product_type_id": 7
              },
              {
                "account_number": "25470392059420200414",
                "account_status": "Closed",
                "current_balance": "0.00000",
                "date_opened": "2020-04-14",
                "days_in_arrears": 0,
                "delinquency_code": "003",
                "highest_days_in_arrears": 0,
                "is_your_account": false,
                "last_payment_amount": "644.10000",
                "last_payment_date": "2020-09-30",
                "loaded_at": "2020-09-30",
                "original_amount": "644.10000",
                "overdue_balance": "0.00000",
                "overdue_date": null,
                "product_type_id": 7
              }
            ]
          },
          "verified_name": {
            "first_name": "ISAAC",
            "other_name": "ABRAHAM SAMUEL",
            "surname": "OGADA"
          }
        }', CURRENT_TIMESTAMP);

INSERT INTO reg_person (loan_request_id, first_name, middle_name, last_name)
VALUES ('0190e7b2-14a8-72e4-8528-89a8cd91d430', 'Ogada', 'Isaac Abraham',
        'Samuel');

INSERT INTO verified_name (loan_request_id, first_name, other_name, surname)
VALUES ('0190e7b2-14a8-72e4-8528-89a8cd91d430', 'ISAAC', 'ABRAHAM SAMUEL',
        'OGADA');

-- Test case 2: Solomon Awich vs SOLOMON RAORE AWICH
INSERT INTO request_content (loan_request_id, content, created_at)
VALUES ('0190e8e4-cf7b-72a5-a647-cd87e14f6715',
        '{
          "loanRequestID": "0190e8e4-cf7b-72a5-a647-cd87e14f6715",
          "regPerson": {
            "firstName": "Solomon",
            "lastName": "Awich"
          },
          "creditBureau": {
            "account_info": [
              {
                "account_number": "25470392059420201109",
                "account_status": "Closed",
                "current_balance": "0.00000",
                "date_opened": "2020-11-09",
                "days_in_arrears": 0,
                "delinquency_code": "003",
                "highest_days_in_arrears": 0,
                "is_your_account": false,
                "last_payment_amount": "0.00000",
                "last_payment_date": null,
                "loaded_at": "2022-02-10",
                "original_amount": "494.00000",
                "overdue_balance": "0.00000",
                "overdue_date": null,
                "product_type_id": 7
              }
            ]
          },
          "verified_name": {
            "first_name": "SOLOMON",
            "other_name": "RAORE",
            "surname": "AWICH"
          }
        }', CURRENT_TIMESTAMP);

INSERT INTO reg_person (loan_request_id, first_name, last_name)
VALUES ('0190e8e4-cf7b-72a5-a647-cd87e14f6715', 'Solomon', 'Awich');

INSERT INTO verified_name (loan_request_id, first_name, other_name, surname)
VALUES ('0190e8e4-cf7b-72a5-a647-cd87e14f6715', 'SOLOMON', 'RAORE', 'AWICH');

-- Test case 3: FABIAN OTIENO vs TERESA WAMUYU THERI
INSERT INTO request_content (loan_request_id, content, created_at)
VALUES ('0190e7b7-4868-73e9-8087-f2c70ea12b61',
        '{
          "loanRequestID": "0190e7b7-4868-73e9-8087-f2c70ea12b61",
          "regPerson": {
            "firstName": "FABIAN",
            "lastName": "OTIENO"
          },
          "creditBureau": {
            "account_info": [],
            "verified_name": {
              "first_name": "TERESA",
              "other_name": "WAMUYU",
              "surname": "THERI"
            }
          }
        }', CURRENT_TIMESTAMP);

INSERT INTO reg_person (loan_request_id, first_name, last_name)
VALUES ('0190e7b7-4868-73e9-8087-f2c70ea12b61', 'FABIAN', 'OTIENO');

INSERT INTO verified_name (loan_request_id, first_name, other_name, surname)
VALUES ('0190e7b7-4868-73e9-8087-f2c70ea12b61', 'TERESA', 'WAMUYU', 'THERI');
