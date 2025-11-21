// cypress/e2e/maintenancedetails.cy.js

describe('Maintenance Details Page', () => {
  before(() => {
    cy.visit('/login');

    // Ensure the page URL is correct
    cy.url({ timeout: 15000 }).should('include', '/login');

    // Wait for the username and password fields to be visible
    cy.get('input[type="text"]', { timeout: 15000 }).should('be.visible');
    cy.get('input[type="password"]', { timeout: 15000 }).should('be.visible');

    // Fill in the login details
    cy.get('input[type="text"]').type('superadmin');
    cy.get('input[type="password"]').type('admin123', { log: false });
    cy.get('button[type="submit"]').click();

    // Wait until the dashboard page loads
    cy.url({ timeout: 10000 }).should('include', '/dashboard');
  });


  describe('Navigation from Maintenance Request to Details via Eye Button', () => {
    it('should navigate to maintenance details by clicking eye button in table', () => {
      // 1. ไปที่หน้า maintenance request ก่อน
      cy.visit('/maintenancerequest');
      cy.url().should('include', '/maintenancerequest');

      // 2. รอให้ตารางโหลดและมีข้อมูล
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // 3. คลิกปุ่มรูปตา (👁) ในคอลัมน์ Action ของแถวแรก
      cy.get('table tbody tr').first().within(() => {
        cy.get('button.form-Button-Edit').click();
      });

      // 4. ตรวจสอบว่ามายังหน้า details แล้ว
      cy.url({ timeout: 10000 }).should('include', '/maintenancedetails');

      // 5. ตรวจสอบว่าหน้า details โหลดเสร็จ
      cy.get('.container-fluid', { timeout: 10000 }).should('be.visible');
      cy.contains('Maintenance Details').should('be.visible');

      // 6. ตรวจสอบ breadcrumb
      cy.contains('Maintenance Request').should('be.visible');
    });

    it('should display correct maintenance details after navigation', () => {
      // ไปที่หน้า maintenance request ก่อน
      cy.visit('/maintenancerequest');
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // คลิกปุ่มรูปตาในแถวแรก
      cy.get('table tbody tr').first().within(() => {
        cy.get('button.form-Button-Edit').click();
      });

      // ตรวจสอบหน้า details
      cy.url().should('include', '/maintenancedetails');

      // ตรวจสอบส่วนต่างๆ ของหน้า details
      cy.get('.toolbar-wrapper').should('be.visible');

      // ตรวจสอบการ์ด Room Information
      cy.contains('Room Information').should('be.visible');
      cy.contains('Room:').should('be.visible');
      cy.contains('Floor:').should('be.visible');

      // ตรวจสอบการ์ด Request Information
      cy.contains('Request Information').should('be.visible');
      cy.contains('Target:').should('be.visible');
      cy.contains('Issue:').should('be.visible');
      cy.contains('Request date:').should('be.visible');
      cy.contains('State:').should('be.visible');

      // ตรวจสอบการ์ด Technician Information
      cy.contains('Technician Information').should('be.visible');
    });
  });

  describe('Maintenance Details Page Functionality', () => {
    beforeEach(() => {
      // ก่อนแต่ละ test ให้นำทางไปที่ maintenance details ผ่านปุ่มรูปตา
      cy.visit('/maintenancerequest');
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);
      cy.get('table tbody tr').first().within(() => {
        cy.get('button.form-Button-Edit').click();
      });
      cy.url().should('include', '/maintenancedetails');

      // รอให้หน้า details โหลดเสร็จ
      cy.get('.container-fluid', { timeout: 10000 }).should('be.visible');
    });



    it('should allow editing maintenance information in modal', () => {
      cy.get('button').contains('Edit Request').click();

      // รอให้ modal โหลดเสร็จ
      cy.get('#editMaintainModal', { timeout: 10000 }).should('be.visible');

      // ทำงานภายใน modal
      cy.get('#editMaintainModal').within(() => {
        // แก้ไขข้อมูลช่าง
        cy.get('input[name="technician"]').clear().type('Test Technician Name');
        cy.get('input[name="phone"]').clear().type('0812345678');

        // เปลี่ยนประเภทการซ่อม
        cy.get('select[name="maintainType"]').select('fix');
        cy.get('select[name="maintainType"]').should('have.value', 'fix');

        // เปลี่ยนสถานะ
        cy.get('select[name="state"]').select('In Progress');
        cy.get('select[name="state"]').should('have.value', 'In Progress');

        // ตรวจสอบว่าวันที่ซ่อมบำรุงถูกเติมอัตโนมัติเมื่อเปลี่ยนสถานะเป็น In Progress
        cy.get('input[name="maintainDate"]').should('not.have.value', '');

        // ยกเลิกการเปลี่ยนแปลง
        cy.get('button').contains('Cancel').click();
      });
    });


    it('should navigate back to maintenance request via breadcrumb', () => {
      // คลิกที่ breadcrumb เพื่อกลับไปหน้า maintenance request
      cy.contains('Maintenance Request').click();

      // ตรวจสอบว่ากลับมาที่หน้า maintenance request แล้ว
      cy.url().should('include', '/maintenancerequest');
      cy.get('table').should('be.visible');
    });

    it('should handle image upload section in edit modal', () => {
      cy.get('button').contains('Edit Request').click();

      // รอให้ modal โหลดเสร็จ
      cy.get('#editMaintainModal', { timeout: 10000 }).should('be.visible');

      // ทำงานภายใน modal
      cy.get('#editMaintainModal').within(() => {
        // เลื่อนไปยังส่วนอัพโหลดรูปภาพ
        cy.contains('Work Evidence Photo').scrollIntoView();

        // ตรวจสอบส่วนอัพโหลดรูปภาพ
        cy.contains('Work Evidence Photo').should('be.visible');
        cy.get('input[type="file"]').should('exist');
        cy.contains('Supported formats: JPEG, PNG, GIF. Max size: 5MB').should('be.visible');

        // ตรวจสอบปุ่ม Upload
        cy.get('button').contains('Upload').should('be.visible');

        // ปิด modal
        cy.get('button').contains('Cancel').click();
      });
    });

    it('should display tenant information when available', () => {
      // หาการ์ด Tenant Information โดยเฉพาะ (ใช้ contains ที่เจาะจงกว่า)
      cy.contains('h5', 'Tenant Information').parent('.card-body').within(() => {
        // ตรวจสอบว่ามีข้อมูลผู้เช่าหรือแสดงข้อความว่าไม่มี
        cy.get('p, div').then(($elements) => {
          const hasContent = $elements.text().length > 0;
          if (hasContent) {
            // ถ้ามีข้อมูลผู้เช่า
            cy.contains(/First Name:|Last Name:|National ID:|Phone Number:|Email:/).should('exist');
          } else {
            // ถ้าไม่มีข้อมูลผู้เช่า
            cy.contains('No active tenant').should('be.visible');
          }
        });
      });
    });
  });

  describe('Multiple Navigation Scenarios', () => {
    it('should navigate to details from different rows in the table', () => {
      cy.visit('/maintenancerequest');
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 2);

      // คลิกปุ่มรูปตาในแถวที่ 2
      cy.get('table tbody tr').eq(1).within(() => {
        cy.get('button.form-Button-Edit').click();
      });

      cy.url().should('include', '/maintenancedetails');
      cy.get('.container-fluid').should('be.visible');

      // กลับไปหน้า maintenance request
      cy.contains('Maintenance Request').click();
      cy.url().should('include', '/maintenancerequest');

      // คลิกปุ่มรูปตาในแถวสุดท้าย
      cy.get('table tbody tr').last().within(() => {
        cy.get('button.form-Button-Edit').click();
      });

      cy.url().should('include', '/maintenancedetails');
    });

    it('should maintain browser navigation history', () => {
      cy.visit('/maintenancerequest');
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // คลิกปุ่มรูปตา
      cy.get('table tbody tr').first().within(() => {
        cy.get('button.form-Button-Edit').click();
      });

      cy.url().should('include', '/maintenancedetails');

      // ใช้ browser back button
      cy.go('back');
      cy.url().should('include', '/maintenancerequest');

      // ใช้ browser forward button
      cy.go('forward');
      cy.url().should('include', '/maintenancedetails');
    });
  });

  describe('Error Handling in Navigation', () => {
    it('should handle empty maintenance request table', () => {
      // ถ้ามีสถานการณ์ที่ตารางว่างเปล่า
      cy.visit('/maintenancerequest');

      cy.get('table tbody tr').then(($rows) => {
        if ($rows.length === 0 || $rows.text().includes('Data Not Found')) {
          // ถ้าไม่มีข้อมูล ควรแสดงข้อความว่าไม่มีข้อมูล
          cy.contains('Data Not Found').should('be.visible');
          // และไม่ควรมีปุ่มรูปตาให้คลิก
          cy.get('button.form-Button-Edit').should('not.exist');
        }
      });
    });

    it('should handle maintenance details with invalid ID', () => {
      // พยายามเข้าหน้า details ด้วย ID ที่ไม่มีอยู่โดยตรง
      cy.visit('/maintenancedetails?id=999999');
      cy.url().should('include', '/maintenancedetails');

      // ควรแสดง error message
      cy.get('.alert-danger').should('be.visible');
    });
  });
  after(() => {
          // Ensure the profile dropdown is visible and click it
          cy.get('.topbar-profile').click({ force: true }); // Use force: true to click even if covered

          // Click the logout button
          cy.contains('li', 'Logout').click({ force: true }); // Force click the logout button

          // Handle SweetAlert confirmation
          cy.get('.swal2-confirm').click({ force: true }); // Force click on confirm button of SweetAlert

          // Optionally, confirm the redirection to the login page
          cy.url().should('include', '/login');  // Ensure the URL includes '/login' to confirm successful logout
      });
});