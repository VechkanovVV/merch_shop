CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    coins INT DEFAULT 1000
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price INT NOT NULL
);

CREATE TABLE purchase (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    product_id BIGINT REFERENCES product(id),
    quantity INT DEFAULT 1,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE authorities (
    username VARCHAR(255) NOT NULL,
    authority VARCHAR(255) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username)
);

CREATE INDEX idx_authorities_username ON authorities(username);