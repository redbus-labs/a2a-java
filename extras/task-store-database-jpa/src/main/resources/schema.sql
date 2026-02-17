-- Table Schema for A2A Task Store (Java Implementation)
-- Matches io.a2a.extras.taskstore.database.jpa.JpaTask
-- Author: Sandeep Belgavi
-- Date: 2026-02-17

CREATE TABLE IF NOT EXISTS a2a_tasks (
    task_id VARCHAR(255) PRIMARY KEY,
    context_id VARCHAR(255),
    state VARCHAR(50),
    status_timestamp TIMESTAMP,
    finalized_at TIMESTAMP,
    task_data TEXT NOT NULL
);

-- Optional: Create index on context_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_a2a_tasks_context_id ON a2a_tasks(context_id);
