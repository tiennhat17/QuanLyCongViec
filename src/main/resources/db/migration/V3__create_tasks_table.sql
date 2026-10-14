CREATE TABLE tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    description TEXT,
    status VARCHAR(255),
    due_date DATE,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_tasks_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);