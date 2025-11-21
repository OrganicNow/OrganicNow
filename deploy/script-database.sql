CREATE TABLE room (
                      room_id BIGSERIAL PRIMARY KEY,
                      room_number VARCHAR(30) NOT NULL,
                      room_floor INTEGER NOT NULL,
                      room_size INTEGER NOT NULL DEFAULT 0,
                      CONSTRAINT uk_room_room_number UNIQUE (room_number)
);

CREATE TABLE tenant (
                        tenant_id BIGSERIAL PRIMARY KEY,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        phone_number VARCHAR(50),
                        email VARCHAR(255),
                        national_id VARCHAR(50) NOT NULL,
                        CONSTRAINT uk_tenant_national_id UNIQUE (national_id)
);

CREATE TABLE contract_type (
                               contract_type_id BIGSERIAL PRIMARY KEY,
                               contract_name VARCHAR(100) NOT NULL,
                               duration INTEGER
);

CREATE TABLE asset_group (
                             asset_group_id BIGSERIAL PRIMARY KEY,
                             asset_group_name VARCHAR(100) NOT NULL,
                             monthly_addon_fee NUMERIC(10,2) DEFAULT 0.00,
                             one_time_damage_fee NUMERIC(10,2) DEFAULT 0.00,
                             free_replacement BOOLEAN DEFAULT TRUE,
                             updated_at TIMESTAMP,
                             CONSTRAINT uk_asset_group_name UNIQUE (asset_group_name)
);

CREATE TABLE fee (
                     fee_id BIGSERIAL PRIMARY KEY,
                     fee_name VARCHAR(120) NOT NULL,
                     unit_fee INTEGER NOT NULL,
                     CONSTRAINT uk_fee_name UNIQUE (fee_name)
);

CREATE TABLE package_plan (
                              package_id BIGSERIAL PRIMARY KEY,
                              contract_type_id BIGINT NOT NULL,
                              price NUMERIC(12,2),
                              is_active INTEGER NOT NULL,
                              room_size INTEGER NOT NULL,
                              CONSTRAINT fk_package_plan_contract_type
                                  FOREIGN KEY (contract_type_id)
                                      REFERENCES contract_type(contract_type_id)
);

CREATE TABLE asset (
                       asset_id BIGSERIAL PRIMARY KEY,
                       asset_group_id BIGINT NOT NULL,
                       asset_name VARCHAR(120) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       CONSTRAINT fk_asset_asset_group
                           FOREIGN KEY (asset_group_id)
                               REFERENCES asset_group(asset_group_id),
                       CONSTRAINT uk_asset_group_asset_name
                           UNIQUE (asset_group_id, asset_name)
);

CREATE TABLE contract (
                          contract_id BIGSERIAL PRIMARY KEY,
                          room_id BIGINT NOT NULL,
                          tenant_id BIGINT NOT NULL,
                          package_id BIGINT NOT NULL,
                          sign_date TIMESTAMP,
                          start_date TIMESTAMP,
                          end_date TIMESTAMP,
                          status INTEGER NOT NULL,
                          deposit NUMERIC(12,2),
                          rent_amount_snapshot NUMERIC(12,2),
                          CONSTRAINT fk_contract_room
                              FOREIGN KEY (room_id)
                                  REFERENCES room(room_id),
                          CONSTRAINT fk_contract_tenant
                              FOREIGN KEY (tenant_id)
                                  REFERENCES tenant(tenant_id),
                          CONSTRAINT fk_contract_package
                              FOREIGN KEY (package_id)
                                  REFERENCES package_plan(package_id)
);

CREATE TABLE contract_file (
                               id BIGSERIAL PRIMARY KEY,
                               contract_id BIGINT NOT NULL UNIQUE,
                               signed_pdf BYTEA,
                               uploaded_at TIMESTAMP,
                               CONSTRAINT fk_contract_file_contract
                                   FOREIGN KEY (contract_id)
                                       REFERENCES contract(contract_id)
                                       ON DELETE CASCADE
);

CREATE TABLE invoice (
                         invoice_id BIGSERIAL PRIMARY KEY,
                         contract_id BIGINT NOT NULL,
                         create_date TIMESTAMP NOT NULL,
                         due_date TIMESTAMP NOT NULL,
                         invoice_status INTEGER NOT NULL,
                         pay_date TIMESTAMP,
                         pay_method INTEGER,
                         sub_total INTEGER NOT NULL,
                         penalty_total INTEGER NOT NULL,
                         net_amount INTEGER NOT NULL,
                         previous_balance INTEGER,
                         paid_amount INTEGER,
                         remaining_balance INTEGER,
                         penalty_applied_at TIMESTAMP,
                         package_id BIGINT,
                         requested_floor INTEGER,
                         requested_room VARCHAR(255),
                         requested_rent INTEGER,
                         requested_water INTEGER,
                         requested_water_unit INTEGER,
                         requested_electricity INTEGER,
                         requested_electricity_unit INTEGER,
                         CONSTRAINT fk_invoice_contract
                             FOREIGN KEY (contract_id)
                                 REFERENCES contract(contract_id)
);

CREATE TABLE invoice_item (
                              invoice_detail_id BIGSERIAL PRIMARY KEY,
                              fee_id BIGINT NOT NULL,
                              invoice_id BIGINT NOT NULL,
                              total_fee INTEGER NOT NULL,
                              CONSTRAINT fk_invoice_item_fee
                                  FOREIGN KEY (fee_id)
                                      REFERENCES fee(fee_id),
                              CONSTRAINT fk_invoice_item_invoice
                                  FOREIGN KEY (invoice_id)
                                      REFERENCES invoice(invoice_id),
                              CONSTRAINT uk_invoice_item_invoice_fee
                                  UNIQUE (invoice_id, fee_id)
);

CREATE TABLE payment_records (
                                 id BIGSERIAL PRIMARY KEY,
                                 invoice_id BIGINT NOT NULL,
                                 payment_amount NUMERIC(10,2) NOT NULL,
                                 payment_method VARCHAR(50) NOT NULL,
                                 payment_status VARCHAR(50) NOT NULL,
                                 payment_date TIMESTAMP NOT NULL,
                                 transaction_reference VARCHAR(255),
                                 notes VARCHAR(1000),
                                 recorded_by VARCHAR(100),
                                 created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMP,
                                 CONSTRAINT fk_payment_records_invoice
                                     FOREIGN KEY (invoice_id)
                                         REFERENCES invoice(invoice_id)
);

CREATE TABLE payment_proofs (
                                id BIGSERIAL PRIMARY KEY,
                                payment_record_id BIGINT NOT NULL,
                                file_name VARCHAR(255) NOT NULL,
                                file_path VARCHAR(500) NOT NULL,
                                file_size BIGINT,
                                content_type VARCHAR(100),
                                proof_type VARCHAR(50) NOT NULL,
                                description VARCHAR(500),
                                uploaded_by VARCHAR(100),
                                uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                CONSTRAINT fk_payment_proofs_payment_record
                                    FOREIGN KEY (payment_record_id)
                                        REFERENCES payment_records(id)
);

CREATE TABLE room_asset (
                            room_asset_id BIGSERIAL PRIMARY KEY,
                            asset_id BIGINT NOT NULL,
                            room_id BIGINT NOT NULL,
                            CONSTRAINT fk_room_asset_asset
                                FOREIGN KEY (asset_id)
                                    REFERENCES asset(asset_id),
                            CONSTRAINT fk_room_asset_room
                                FOREIGN KEY (room_id)
                                    REFERENCES room(room_id),
                            CONSTRAINT uk_room_asset_unique
                                UNIQUE (room_id, asset_id)
);

CREATE TABLE asset_event (
                             event_id BIGSERIAL PRIMARY KEY,
                             room_id BIGINT NOT NULL,
                             asset_id BIGINT NOT NULL,
                             event_type VARCHAR(20) NOT NULL,
                             reason_type VARCHAR(20),
                             note TEXT,
                             created_at TIMESTAMP,
                             CONSTRAINT fk_asset_event_room
                                 FOREIGN KEY (room_id)
                                     REFERENCES room(room_id),
                             CONSTRAINT fk_asset_event_asset
                                 FOREIGN KEY (asset_id)
                                     REFERENCES asset(asset_id)
);

CREATE TABLE maintain (
                          maintain_id BIGSERIAL PRIMARY KEY,
                          target_type INTEGER NOT NULL,
                          room_id BIGINT NOT NULL,
                          room_asset_id BIGINT,
                          issue_category INTEGER NOT NULL,
                          issue_title VARCHAR(200) NOT NULL,
                          issue_description TEXT,
                          create_date TIMESTAMP NOT NULL,
                          scheduled_date TIMESTAMP,
                          finish_date TIMESTAMP,
                          maintain_type VARCHAR(50),
                          technician_name VARCHAR(100),
                          technician_phone VARCHAR(20),
                          work_image_url VARCHAR(500),
                          CONSTRAINT fk_maintain_room
                              FOREIGN KEY (room_id)
                                  REFERENCES room(room_id),
                          CONSTRAINT fk_maintain_room_asset
                              FOREIGN KEY (room_asset_id)
                                  REFERENCES room_asset(room_asset_id)
);

CREATE TABLE maintenance_schedule (
                                      schedule_id BIGSERIAL PRIMARY KEY,
                                      schedule_scope INTEGER NOT NULL,
                                      asset_group_id BIGINT,
                                      cycle_month INTEGER NOT NULL,
                                      last_done_date TIMESTAMP,
                                      next_due_date TIMESTAMP,
                                      notify_before_date INTEGER,
                                      schedule_title VARCHAR(200) NOT NULL,
                                      schedule_description TEXT,
                                      CONSTRAINT fk_schedule_asset_group
                                          FOREIGN KEY (asset_group_id)
                                              REFERENCES asset_group(asset_group_id)
);

CREATE TABLE maintenance_notification_skip (
                                               skip_id BIGSERIAL PRIMARY KEY,
                                               schedule_id BIGINT NOT NULL,
                                               due_date DATE NOT NULL,
                                               skipped_by_user_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                               CONSTRAINT ux_mns_schedule_due
                                                   UNIQUE (schedule_id, due_date)
);

CREATE TABLE admin (
                       admin_id BIGSERIAL PRIMARY KEY,
                       admin_username VARCHAR(60) NOT NULL,
                       admin_password VARCHAR(255) NOT NULL,
                       admin_role INTEGER NOT NULL DEFAULT 0,
                       CONSTRAINT uk_admin_username UNIQUE (admin_username)
);

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
-- Maintain (3 รายการตัวอย่างที่ตรงกับระบบใหม่)
-- ========================
INSERT INTO maintain (target_type, room_id, issue_category, issue_title, issue_description, create_date, scheduled_date, finish_date, maintain_type, technician_name, technician_phone) VALUES
-- รายการ 1: Asset - bed-001 ในห้อง 101 (Somchai) - กำลังซ่อม
(0, 1, 0, 'bed-001', 'เตียงหักขา ต้องซ่อมด่วน', '2025-11-10 09:00:00', '2025-11-14 14:00:00', NULL, 'fix', 'ช่างโจ', '0891234567'),

-- รายการ 2: Building - ห้อง 102 (Suda) - เสร็จแล้ว  
(1, 2, 0, 'Wall crack repair', 'ผนังห้องน้ำมีรอยร้าว ซ่อมเสร็จแล้ว', '2025-11-08 10:30:00', '2025-11-12 09:00:00', '2025-11-12 16:30:00', 'fix', 'ช่างดำ', '0892345678'),

-- รายการ 3: Asset - chair-001 ในห้อง 103 (Anan) - รอซ่อม
(0, 3, 0, 'chair-001', 'เก้าอี้หัก ต้องเปลี่ยนใหม่', '2025-11-12 14:15:00', NULL, NULL, 'replace', NULL, NULL)
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
