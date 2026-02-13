-- Alter schedule column in restaurants table to TEXT type to support longer schedule data
ALTER TABLE restaurants MODIFY COLUMN schedule TEXT;
