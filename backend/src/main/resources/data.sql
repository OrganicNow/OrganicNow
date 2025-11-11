-- ========================
-- Admin (ข้อมูลเริ่มต้น)
-- ========================
INSERT INTO admin (admin_username, admin_password, admin_role) VALUES
                                                                   ('alex', 'admin123', 0),
                                                                   ('superadmin', 'admin123', 1)
    ON CONFLICT (admin_username) DO NOTHING;

-- ========================
-- Room (2 ชั้น × 12 ห้อง)
-- ========================
INSERT INTO room (room_floor, room_number, room_size) VALUES
                                                          -- ชั้น 1
                                                          (1, '101', 0), (1, '102', 0), (1, '103', 0), (1, '104', 0),
                                                          (1, '105', 1), (1, '106', 1), (1, '107', 1), (1, '108', 1),
                                                          (1, '109', 2), (1, '110', 2), (1, '111', 2), (1, '112', 2),
                                                          -- ชั้น 2
                                                          (2, '201', 0), (2, '202', 0), (2, '203', 0), (2, '204', 0),
                                                          (2, '205', 1), (2, '206', 1), (2, '207', 1), (2, '208', 1),
                                                          (2, '209', 2), (2, '210', 2), (2, '211', 2), (2, '212', 2)
    ON CONFLICT (room_number) DO NOTHING;

-- ========================
-- Tenant
-- ========================
INSERT INTO tenant (first_name, last_name, phone_number, email, national_id) VALUES
                                                                                 ('Somchai', 'Sukjai', '0812345678', 'somchai@example.com', '1111111111111'),
                                                                                 ('Suda',   'Thongdee', '0898765432', 'suda@example.com',   '2222222222222'),
                                                                                 ('Anan',   'Meechai',  '0861122334', 'anan@example.com',   '3333333333333')
    ON CONFLICT (national_id) DO NOTHING;

-- ========================
-- Contract Type
-- ========================
INSERT INTO contract_type (contract_name, duration) VALUES
                                                        ('3 เดือน', 3),
                                                        ('6 เดือน', 6),
                                                        ('9 เดือน', 9),
                                                        ('1 ปี', 12)
    ON CONFLICT (contract_type_id) DO NOTHING;

-- ========================
-- Package Plan
-- ========================
INSERT INTO package_plan (contract_type_id, price, is_active, room_size) VALUES
                                                                             (1,  7000.00, 1, 0),
                                                                             (2, 6500.00, 1, 0),
                                                                             (3, 6000.00, 1, 0),
                                                                             (4, 5500.00, 1, 0),
                                                                             (1,  9000.00, 1, 1),
                                                                             (2, 8500.00, 1, 1),
                                                                             (3, 8000.00, 1, 1),
                                                                             (4, 7500.00, 1, 1),
                                                                             (1, 12000.00, 1, 2),
                                                                             (2, 11000.00, 1, 2),
                                                                             (3, 10000.00, 1, 2),
                                                                             (4,  9000.00, 1, 2)
    ON CONFLICT (package_id) DO NOTHING;

-- ========================
-- Contract
-- ========================
INSERT INTO contract (room_id, tenant_id, package_id, sign_date, start_date, end_date, status, deposit, rent_amount_snapshot) VALUES
                                                                                                                                  (1, 1, 1, '2025-09-01', '2025-10-01', '2025-12-31', 1, 5000.00,  8000.00),
                                                                                                                                  (2, 2, 2, '2025-09-05', '2025-10-01', '2026-03-31', 1, 5000.00, 15000.00),
                                                                                                                                  (3, 3, 3, '2025-09-10', '2025-10-01', '2026-06-30', 1, 5000.00, 21000.00)
    ON CONFLICT (contract_id) DO NOTHING;

-- ========================
-- Invoice (แค่ 3 บิลสำหรับทดสอบ Outstanding Balance)
-- ========================
INSERT INTO invoice (contract_id, create_date, due_date, invoice_status, pay_date, pay_method, sub_total, penalty_total, net_amount, penalty_applied_at,
<<<<<<< HEAD
                    requested_floor, requested_room, requested_rent, requested_water, requested_water_unit, requested_electricity, requested_electricity_unit) VALUES
=======
                     requested_floor, requested_room, requested_rent, requested_water, requested_water_unit, requested_electricity, requested_electricity_unit) VALUES
>>>>>>> f88e7a40f80460f3b336a41bbe20336a38657894
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

-- ========================
-- Asset Group (5 กลุ่ม + ฟิลด์ใหม่)
-- ========================
INSERT INTO asset_group (asset_group_name, monthly_addon_fee, one_time_damage_fee, free_replacement) VALUES
                                                                                                         ('wardrobe', 0, 200, true),
                                                                                                         ('table', 0, 150, true),
                                                                                                         ('chair', 0, 100, true),
                                                                                                         ('bulb', 0, 0, true),
                                                                                                         ('bed', 300, 250, false)
    ON CONFLICT (asset_group_name) DO NOTHING;

-- ========================
-- Generate Assets
-- ========================
INSERT INTO asset (asset_group_id, asset_name, status)
SELECT (SELECT asset_group_id FROM asset_group WHERE asset_group_name='bed'),
       'bed-' || LPAD(gs::text, 3, '0'), 'available'
FROM generate_series(1, 35) AS gs
    ON CONFLICT DO NOTHING;

INSERT INTO asset (asset_group_id, asset_name, status)
SELECT (SELECT asset_group_id FROM asset_group WHERE asset_group_name='wardrobe'),
       'wardrobe-' || LPAD(gs::text, 3, '0'), 'available'
FROM generate_series(1, 35) AS gs
    ON CONFLICT DO NOTHING;

INSERT INTO asset (asset_group_id, asset_name, status)
SELECT (SELECT asset_group_id FROM asset_group WHERE asset_group_name='chair'),
       'chair-' || LPAD(gs::text, 3, '0'), 'available'
FROM generate_series(1, 35) AS gs
    ON CONFLICT DO NOTHING;

INSERT INTO asset (asset_group_id, asset_name, status)
SELECT (SELECT asset_group_id FROM asset_group WHERE asset_group_name='table'),
       'table-' || LPAD(gs::text, 3, '0'), 'available'
FROM generate_series(1, 35) AS gs
    ON CONFLICT DO NOTHING;

INSERT INTO asset (asset_group_id, asset_name, status)
SELECT (SELECT asset_group_id FROM asset_group WHERE asset_group_name='bulb'),
       'bulb-' || LPAD(gs::text, 3, '0'), 'available'
FROM generate_series(1, 50) AS gs
    ON CONFLICT DO NOTHING;

-- ========================
-- Assign Asset to Each Room
-- ========================
DELETE FROM room_asset;

WITH asset_sets AS (
    SELECT
        a.asset_id,
        ag.asset_group_name,
        ROW_NUMBER() OVER (PARTITION BY ag.asset_group_name ORDER BY a.asset_id) AS rn
    FROM asset a
             JOIN asset_group ag ON ag.asset_group_id = a.asset_group_id
),
     room_sets AS (
         SELECT
             room_id,
             ROW_NUMBER() OVER (ORDER BY room_id) AS rn
         FROM room
     )
INSERT INTO room_asset (room_id, asset_id)
SELECT r.room_id, a.asset_id
FROM room_sets r
         JOIN asset_sets a
              ON (
                     (a.asset_group_name IN ('bed','table','chair','wardrobe') AND a.rn = ((r.rn - 1) % 35) + 1)
                  OR (a.asset_group_name = 'bulb' AND a.rn = ((r.rn - 1) % 50) + 1)
    )
ON CONFLICT DO NOTHING;

-- ========================
-- Fix Asset Status
-- ========================
UPDATE asset
SET status = 'in_use'
WHERE asset_id IN (SELECT asset_id FROM room_asset)
  AND status <> 'deleted';

UPDATE asset
SET status = 'available'
WHERE asset_id NOT IN (SELECT asset_id FROM room_asset)
  AND status <> 'deleted';

-- ========================
-- Maintain
-- ========================
INSERT INTO maintain (target_type, room_id, issue_category, issue_title, issue_description, create_date, scheduled_date, finish_date) VALUES
                                                                                                                                          (0, 1, 1, 'Air conditioner - Fix', 'แอร์ไม่เย็น มีเสียงดัง', '2025-03-11', '2025-03-14 09:00:00', NULL),
                                                                                                                                          (1, 2, 0, 'Wall - Fix', 'ผนังร้าวเล็กน้อย', '2025-02-28', '2025-02-28 10:00:00', '2025-02-28 16:00:00'),
                                                                                                                                          (0, 15, 1, 'Light - Shift', 'ย้ายตำแหน่งโคมไฟ', '2025-02-28', '2025-02-28 13:00:00', '2025-02-28 15:00:00')
    ON CONFLICT DO NOTHING;

-- ========================
-- Maintenance Schedule
-- ========================
INSERT INTO maintenance_schedule
(schedule_scope, asset_group_id, cycle_month, last_done_date, next_due_date, notify_before_date, schedule_title, schedule_description)
VALUES
    (0, 1, 6, '2025-01-01', '2025-07-01', 7, 'ตรวจแอร์', 'ตรวจเช็คและทำความสะอาดแอร์'),
    (1, 2, 12, '2025-01-10', '2026-01-10', 14, 'ตรวจสภาพห้อง', 'ตรวจสอบรอยร้าว พื้น เพดาน'),
    (0, 3, 3, '2025-02-01', '2025-05-01', 3, 'ตรวจหลอดไฟ', 'ตรวจสอบและเปลี่ยนหลอดไฟ')
    ON CONFLICT DO NOTHING;

-- ========================
-- Notification Skip
-- ========================
CREATE TABLE IF NOT EXISTS maintenance_notification_skip (
                                                             skip_id BIGSERIAL PRIMARY KEY,
                                                             schedule_id BIGINT NOT NULL,
                                                             due_date DATE NOT NULL,
                                                             skipped_by_user_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_mns_schedule FOREIGN KEY (schedule_id) REFERENCES maintenance_schedule(schedule_id) ON DELETE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_mns_schedule_due
    ON maintenance_notification_skip (schedule_id, due_date);

-- ========================
-- Enable fuzzy search extension
-- ========================
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_tenant_name_trgm
    ON tenant USING gin ((first_name || ' ' || last_name) gin_trgm_ops);