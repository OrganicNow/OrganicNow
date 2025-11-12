CREATE TABLE IF NOT EXISTS room (
                                    room_id SERIAL PRIMARY KEY,
                                    room_number VARCHAR(30) NOT NULL UNIQUE,
                                    room_floor INT NOT NULL,
                                    status VARCHAR(20) DEFAULT 'available'
);

-- Create asset_group table (required for maintenance_schedule)
CREATE TABLE IF NOT EXISTS asset_group (
                                           asset_group_id SERIAL PRIMARY KEY,
                                           asset_group_name VARCHAR(100) NOT NULL UNIQUE
);

-- Create maintenance_schedule table
CREATE TABLE IF NOT EXISTS maintenance_schedule (
                                                    schedule_id SERIAL PRIMARY KEY,
                                                    schedule_scope INTEGER NOT NULL CHECK (schedule_scope IN (0, 1)),
                                                    asset_group_id BIGINT,
                                                    cycle_month INTEGER NOT NULL CHECK (cycle_month >= 1),
                                                    last_done_date TIMESTAMP,
                                                    next_due_date TIMESTAMP,
                                                    notify_before_date INTEGER,
                                                    schedule_title VARCHAR(200) NOT NULL,
                                                    schedule_description TEXT,
                                                    FOREIGN KEY (asset_group_id) REFERENCES asset_group(asset_group_id) ON DELETE SET NULL
);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
                                             notification_id SERIAL PRIMARY KEY,
                                             title VARCHAR(200) NOT NULL,
                                             message TEXT,
                                             type VARCHAR(50),
                                             is_read BOOLEAN NOT NULL DEFAULT FALSE,
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             read_at TIMESTAMP,
                                             maintenance_schedule_id BIGINT,
                                             FOREIGN KEY (maintenance_schedule_id) REFERENCES maintenance_schedule(schedule_id) ON DELETE SET NULL
);

-- Create maintain table
CREATE TABLE IF NOT EXISTS maintain (
    maintain_id SERIAL PRIMARY KEY,
    target_type INTEGER NOT NULL CHECK (target_type IN (0, 1)),
    room_id BIGINT NOT NULL,
    room_asset_id BIGINT,
    issue_category INTEGER NOT NULL CHECK (issue_category >= 0 AND issue_category <= 5),
    issue_title VARCHAR(200) NOT NULL,
    issue_description TEXT,
    create_date TIMESTAMP NOT NULL,
    scheduled_date TIMESTAMP,
    finish_date TIMESTAMP,
    maintain_type VARCHAR(50),
    technician_name VARCHAR(100),
    technician_phone VARCHAR(20),
    work_image_url VARCHAR(500),
    FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE
);

TRUNCATE TABLE room RESTART IDENTITY;

-- Insert sample asset groups
INSERT INTO asset_group (asset_group_name) VALUES
                                               ('Asset'),
                                               ('Building'),
                                               ('Electrical'),
                                               ('Plumbing'),
                                               ('HVAC');

DO $$
    DECLARE
        f INT;
        r INT;
    BEGIN
        FOR f IN 1..2 LOOP
                FOR r IN 1..12 LOOP
                        INSERT INTO room (room_floor, room_number, status)
                        VALUES (f, CONCAT(f, LPAD(r::text, 2, '0')), 'available');
                    END LOOP;
            END LOOP;
    END$$;


INSERT INTO tenant (first_name, last_name, phone_number, email, national_id)
VALUES
    ('Somchai', 'Sukjai', '0812345678', 'somchai@example.com', '1111111111111'),
    ('Suda', 'Thongdee', '0898765432', 'suda@example.com', '2222222222222'),
    ('Anan', 'Meechai', '0861122334', 'anan@example.com', '3333333333333');


INSERT INTO contract_type (contract_name, duration)
VALUES
    ('สัญญา 3 เดือน', 3),
    ('สัญญา 6 เดือน', 6),
    ('สัญญา 9 เดือน', 9),
    ('สัญญา 1 ปี', 12);


INSERT INTO package_plan (contract_type_id, price, is_active, room_size)
VALUES
    (1, 8000.00, 1, 1),   -- 3 เดือน
    (2, 15000.00, 1, 1),  -- 6 เดือน
    (3, 21000.00, 1, 1),  -- 9 เดือน
    (4, 28000.00, 1, 1);  -- 1 ปี


INSERT INTO contract
(room_id, tenant_id, package_id, sign_date, start_date, end_date, status, deposit, rent_amount_snapshot)
VALUES
-- Somchai -> ห้อง 101 -> package 3 เดือน
(1, 1, 1, '2025-01-01', '2025-02-01', '2025-04-30', 1, 5000.00, 8000.00),

-- Suda -> ห้อง 102 -> package 6 เดือน
(2, 2, 2, '2025-01-05', '2025-02-01', '2025-07-31', 1, 5000.00, 15000.00),

-- Anan -> ห้อง 103 -> package 9 เดือน
(3, 3, 3, '2025-01-10', '2025-02-01', '2025-10-31', 1, 5000.00, 21000.00);

INSERT INTO invoice (contract_id, create_date, due_date, invoice_status, pay_date, pay_method, sub_total, penalty_total, net_amount, penalty_applied_at,
                     requested_floor, requested_room, requested_rent, requested_water, requested_water_unit, requested_electricity, requested_electricity_unit) VALUES
-- บิล 1: ตุลาคม 2025 - มี penalty เพราะค้างจ่าย (ยังไม่จ่าย)
(1, '2025-10-08', '2025-11-08', 0, NULL, NULL, 7000, 700, 7700, '2025-11-08',
 1, '101', 7000, 30, 1, 7, 1),
-- บิล 2: พฤศจิกายน 2025 - ใหม่ ยังไม่จ่าย (ไม่มี penalty ยัง)
(1, '2025-11-09', '2025-12-09', 0, NULL, NULL, 7000, 0, 7000, NULL,
 1, '101', 7000, 30, 1, 8, 1),
-- บิล 3: ธันวาคม 2025 - ในอนาคต สำหรับทดสอบ (ยังไม่ถึงเวลา)
(1, '2025-12-01', '2026-01-01', 0, NULL, NULL, 7000, 0, 7000, NULL,
 1, '101', 7000, 30, 1, 6, 1)
    ON CONFLICT (invoice_id) DO NOTHING;
