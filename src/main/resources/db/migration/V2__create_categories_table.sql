CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    user_id BIGINT NOT NULL,
    created_at DATETIME(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);