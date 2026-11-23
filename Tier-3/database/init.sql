-- DriVIA database (Tier 3). Run this in DataGrip on the drivia database.
-- WARNING: it deletes the existing tables and their data first.

DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS users;

CREATE TABLE users( id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL
);

-- Subtypes of users: same id
CREATE TABLE customer( id INTEGER PRIMARY KEY REFERENCES users (id),
                       phone_number VARCHAR(20)
);

CREATE TABLE employee( id INTEGER PRIMARY KEY REFERENCES users (id),
                       employee_no VARCHAR(20) NOT NULL
);

CREATE TABLE location( id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL,
                       address VARCHAR(200) NOT NULL
);

CREATE TABLE vehicle( id SERIAL PRIMARY KEY, registration_no VARCHAR(20) NOT NULL UNIQUE,
                      model VARCHAR(100) NOT NULL, production_date DATE NOT NULL,
                      horsepower INTEGER NOT NULL CHECK (horsepower > 0), category VARCHAR(50) NOT NULL,
                      gearbox VARCHAR(20) NOT NULL, price_per_day INTEGER NOT NULL CHECK (price_per_day > 0),
                      status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'RENTED', 'UNAVAILABLE', 'REMOVED')),
                      location_id INTEGER NOT NULL REFERENCES location (id)
);

CREATE TABLE reservation( id SERIAL PRIMARY KEY, customer_id INTEGER NOT NULL REFERENCES customer (id),
                          vehicle_id INTEGER NOT NULL REFERENCES vehicle (id), location_id INTEGER NOT NULL REFERENCES location (id),
                          pickup_date DATE NOT NULL, return_date DATE NOT NULL,
                          status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
                          total_price INTEGER NOT NULL CHECK (total_price >= 0),
                          CHECK (return_date > pickup_date)
);

-- ---------- Test data ----------

-- The tables were just created
INSERT INTO location( name, address ) VALUES
                      ('Horsens', 'Banegaardsgade 2, 8700 Horsens'),
                      ('Aarhus',  'Ny Banegaardsgade 1, 8000 Aarhus C');

INSERT INTO vehicle( registration_no, model, production_date, horsepower, category, gearbox, price_per_day, status, location_id ) VALUES
                      ('AB12345', 'Toyota Yaris',  '2021-03-15',  92, 'Small',    'Manual',    350, 'AVAILABLE', 1),
                      ('AB23456', 'VW Golf',       '2022-06-01', 130, 'Compact',  'Manual',    450, 'AVAILABLE', 1),
                      ('AB34567', 'Tesla Model 3', '2023-01-20', 283, 'Electric', 'Automatic', 900, 'AVAILABLE', 1),
                      ('AB45678', 'Skoda Octavia', '2020-09-10', 150, 'Family',   'Automatic', 550, 'AVAILABLE', 1),
                      ('AB56789', 'Ford Transit',  '2019-11-05', 130, 'Van',      'Manual',    700, 'AVAILABLE', 1),
                      ('CD12345', 'Peugeot 208',   '2022-02-14', 100, 'Small',    'Manual',    350, 'AVAILABLE', 2),
                      ('CD23456', 'VW Passat',     '2021-08-30', 150, 'Family',   'Automatic', 600, 'AVAILABLE', 2),
                      ('CD34567', 'Kia EV6',       '2023-05-12', 229, 'Electric', 'Automatic', 850, 'AVAILABLE', 2),
                      ('CD45678', 'BMW 3 Series',  '2022-10-03', 184, 'Premium',  'Automatic', 950, 'AVAILABLE', 2),
                      ('CD56789', 'Renault Clio',  '2020-04-22',  90, 'Small',    'Manual',    300, 'AVAILABLE', 2);

INSERT INTO users( name, email, password ) VALUES
                       ('Emma Employee', 'emma@drivia.dk', '12345'),
                       ('Bob Bobsen',   'anna@mail.dk',   'secret1'),
                       ('Peter Jensen',  'peter@mail.dk',  'secret2');

INSERT INTO employee( id, employee_no ) VALUES
                       (1, 'E-001');

INSERT INTO customer( id, phone_number ) VALUES
                       (2, '12345678'),
                       (3, '87654321');