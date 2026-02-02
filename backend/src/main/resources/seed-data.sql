-- Database Seed Script for Issue Tracker
-- This is a reference SQL script. The actual seeding is done automatically by DatabaseSeeder.java
-- You can use this script if you need to manually seed the database

-- Note: UUIDs and timestamps will be different when using this script
-- The DatabaseSeeder.java class is the recommended way to seed data

-- Instructions:
-- 1. Connect to your PostgreSQL database
-- 2. Run this script to populate initial data
-- 3. Password for both users is 'password123' (hashed with BCrypt)

-- Clear existing data (optional - be careful in production!)
-- TRUNCATE TABLE comments, activity_logs, issues, project_members, projects, users CASCADE;

-- Insert Users
-- Note: Password is BCrypt hash of 'password123'
-- You'll need to generate fresh BCrypt hashes if using this script directly
INSERT INTO users (id, email, password_hash, full_name, created_at, updated_at) VALUES
  (gen_random_uuid(), 'admin1@issue-tracker.com', '$2a$10$XQZ9qhvqvqyPrKPQvlvB8uyKUz8bPZCqZn9YnfLvYqB6WgBfKH3Nu', 'Admin User1', NOW(), NOW()),
  (gen_random_uuid(), 'admin2@issue-tracker.com', '$2a$10$XQZ9qhvqvqyPrKPQvlvB8uyKUz8bPZCqZn9YnfLvYqB6WgBfKH3Nu', 'Admin User2', NOW(), NOW());

-- Note: The actual seeding is handled by the Java DatabaseSeeder class
-- which includes:
-- - 2 Users (admin1@issue-tracker.com and admin2@issue-tracker.com)
-- - 3 Projects (Issue Tracker Application, E-Commerce Platform, Mobile Banking App)
-- - 12 Issues with various statuses and priorities
-- - 20+ Comments on the issues
-- - Project member relationships

-- To disable automatic seeding, you can modify the DatabaseSeeder.java class
-- or set a property to skip seeding

-- For development, it's recommended to use the automatic seeding via DatabaseSeeder.java
-- This ensures consistency and makes it easy to reset the database
