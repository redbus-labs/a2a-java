-- Table Schema for A2A Task Store
-- Author: Sandeep Belgavi
-- Date: 2026-02-17

CREATE TABLE IF NOT EXISTS a2a_tasks (
    task_id VARCHAR(255) PRIMARY KEY,
    task_data TEXT NOT NULL
);
