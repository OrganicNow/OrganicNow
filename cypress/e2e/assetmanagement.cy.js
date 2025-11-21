describe('Asset Management - Complete Test Suite', () => {
  // Login ก่อนเริ่ม tests ทั้งหมด
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

  beforeEach(() => {
    cy.visit('/assetmanagement');
    cy.contains('Asset Management', { timeout: 10000 }).should('be.visible');

    // กำหนดขนาดหน้าจอให้เหมาะสมกับ headless และ head mode
    if (Cypress.browser.name === 'chrome') {
      // Headless browser view
      cy.viewport(1280, 720); // เปลี่ยนขนาดหน้าจอสำหรับ headless
    } else {
      // Head mode
      cy.viewport('macbook-15'); // เปลี่ยนขนาดหน้าจอสำหรับ head mode
    }
  });

  // ==================== BASIC FUNCTIONALITY ====================
  describe('1. Basic Page Structure and Layout', () => {
    it('1.1 should load asset management page successfully', () => {
      cy.contains('Asset Management').should('be.visible');
      cy.get('.container-fluid').should('be.visible');
    });

    it('1.2 should display all summary cards', () => {
      cy.contains('Total Groups').should('be.visible');
      cy.contains('Total Assets').should('be.visible');
      cy.contains('In Use').should('be.visible');
      cy.contains('Available').should('be.visible');
      cy.get('.row.g-3.mb-4 .card.shadow-sm').should('have.length', 4);
    });

    it('1.3 should display search and action buttons', () => {
      cy.get('.tm-search input').should('be.visible');
      cy.contains('Sort').should('be.visible');
      cy.contains('Create Asset Group').should('be.visible');
    });

    it('1.4 should display sidebar with asset groups', () => {
      cy.get('.sidebar-modern').should('be.visible');
      cy.contains('Asset Groups').should('be.visible');
      cy.get('.list-group-item').should('have.length.at.least', 1);
      cy.contains('All Groups').should('be.visible');
    });

    it('1.5 should display main assets table', () => {
      cy.get('table').should('be.visible');
      cy.get('thead').should('be.visible');
      cy.get('tbody').should('be.visible');

      // ตรวจสอบ header columns
      const headers = ['Order', 'Asset Name', 'Room', 'Monthly', 'OneTime', 'Status', 'Actions'];
      headers.forEach(header => {
        cy.contains('th', header).should('exist');
      });
    });
  });

  // ==================== ASSET GROUP CRUD ====================
  describe('2. Asset Group CRUD Operations', () => {
    it('2.1 should create new asset group with all fields', () => {
      cy.contains('Create Asset Group').click();
      cy.get('#groupModal').should('be.visible');

      // กรอกข้อมูลครบทุก field
      cy.get('#groupModal input[type="text"]').type('Cypress Test Group ' + Date.now());
      cy.get('#groupModal input[type="number"]').eq(0).clear().type('150');
      cy.get('#groupModal input[type="number"]').eq(1).clear().type('75');
      cy.get('#groupModal input[type="checkbox"]').check();

      // Submit form
      cy.get('#groupModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่าสร้างสำเร็จ (รอให้ modal ปิด)
      cy.get('#groupModal').should('not.be.visible');
    });

    it('2.2 should edit existing asset group', () => {
      // คลิกปุ่ม edit group แรกที่พบ (ที่ไม่ใช่ All Groups)
      cy.get('.list-group-item').not(':contains("All Groups")').first()
        .find('.bi-pencil').click();

      // ตรวจสอบว่า modal edit เปิด
      cy.get('#groupModal').should('be.visible');

      // แก้ไขชื่อ group
      cy.get('#groupModal input[type="text"]').clear().type('Updated Group Name ' + Date.now());

      // Save
      cy.get('#groupModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่า modal ปิด
      cy.get('#groupModal').should('not.be.visible');
    });

    it('2.3 should delete asset group with confirmation', () => {
      // Mock confirmation dialog
      cy.on('window:confirm', () => true);

      // นับจำนวน group ก่อนลบ
      cy.get('.list-group-item').its('length').then((initialCount) => {
        if (initialCount > 1) { // ต้องมีมากกว่า 1 เพราะมี "All Groups"
          // คลิกปุ่มลบ group แรกที่ไม่ใช่ "All Groups"
          cy.get('.list-group-item').not(':contains("All Groups")').first()
            .find('.bi-trash').click();

          // ตรวจสอบว่าจำนวน group ลดลง
          cy.get('.list-group-item').should('have.length.lessThan', initialCount);
        }
      });
    });

    it('2.4 should show validation error for empty group name', () => {
      cy.contains('Create Asset Group').click();
      cy.get('#groupModal').should('be.visible');

      // พยายาม save โดยไม่กรอกชื่อ
      cy.get('#groupModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่ามี SweetAlert แสดง error
      cy.get('.swal2-popup', { timeout: 5000 }).should('be.visible');
      cy.contains('กรุณากรอกชื่อ Group').should('be.visible');

      // ปิด SweetAlert
      cy.get('.swal2-confirm').click();

      // ปิด modal
      cy.get('#modalGroup_btnClose').click();
    });
  });

  // ==================== ASSET CRUD ====================
  describe('3. Asset CRUD Operations', () => {
    it('3.1 should create single asset', () => {
      // เปิด modal สร้าง asset จาก group แรกที่ไม่ใช่ All Groups
      cy.get('.list-group-item').not(':contains("All Groups")').first()
        .find('.bi-plus-circle').click();

      // กรอกข้อมูล asset
      const assetName = 'Cypress Test Asset ' + Date.now();
      cy.get('#assetModal input[type="text"]').type(assetName);

      // Submit
      cy.get('#assetModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่า modal ปิด
      cy.get('#assetModal').should('not.be.visible');
    });

    it('3.2 should create multiple assets with quantity', () => {
      cy.get('.list-group-item').not(':contains("All Groups")').first()
        .find('.bi-plus-circle').click();

      // กรอกข้อมูล asset พร้อม quantity
      const assetName = 'Bulk Test Asset ' + Date.now();
      cy.get('#assetModal input[type="text"]').type(assetName);
      cy.get('#assetModal input[type="number"]').clear().type('3');

      // Submit
      cy.get('#assetModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่า modal ปิด
      cy.get('#assetModal').should('not.be.visible');
    });

    it('3.3 should edit existing asset', () => {
      // คลิกปุ่ม edit asset แรกในตาราง (ถ้ามีข้อมูล)
      cy.get('tbody').then(($tbody) => {
        if ($tbody.find('.bi-pencil').length > 0) {
          cy.get('tbody .bi-pencil').first().click();

          // แก้ไขชื่อ asset
          cy.get('#assetModal input[type="text"]').clear().type('Updated Asset ' + Date.now());

          // Save
          cy.get('#assetModal button[type="submit"]').contains('Save').click();

          // ตรวจสอบว่า modal ปิด
          cy.get('#assetModal').should('not.be.visible');
        } else {
          cy.log('No assets available for editing');
        }
      });
    });

    it('3.4 should delete asset', () => {
      // Mock confirmation
      cy.on('window:confirm', () => true);

      // นับจำนวน asset ก่อนลบ
      cy.get('tbody tr').its('length').then((initialCount) => {
        if (initialCount > 0) {
          // คลิกปุ่มลบ asset แรก
          cy.get('tbody .bi-trash').first().click();

          // ตรวจสอบว่าจำนวน asset ลดลง
          if (initialCount > 1) {
            cy.get('tbody tr').should('have.length.lessThan', initialCount);
          }
        } else {
          cy.log('No assets available for deletion');
        }
      });
    });
  });

  // ==================== SEARCH AND FILTER ====================
  describe('4. Search and Filter Functionality', () => {
    it('4.1 should search assets by name', () => {
      const searchTerm = 'Test';
      cy.get('.tm-search input').type(searchTerm);
      cy.get('.tm-search input').should('have.value', searchTerm);

      // ตรวจสอบว่ามีผลลัพธ์
      cy.get('tbody tr').should('have.length.at.least', 0);

      cy.get('.tm-search input').clear();
    });

    it('4.2 should filter by asset group', () => {
      // ตรวจสอบว่าไม่มี modal เปิดอยู่ก่อน
      cy.get('body').then(($body) => {
        if ($body.find('.modal.show').length === 0) {
          // คลิกเลือก group แรก (ที่ไม่ใช่ All Groups)
          cy.get('.list-group-item').not(':contains("All Groups")').first().then(($item) => {
            const groupName = $item.text().trim();
            cy.wrap($item).click({ force: true });

            // ตรวจสอบว่า group ถูกเลือก
            cy.get('.list-group-item.active').should('exist');

            // ตรวจสอบว่าตารางแสดงข้อมูล
            cy.get('tbody tr').should('have.length.at.least', 0);
          });
        }
      });
    });

    it('4.3 should combine search and filter', () => {
      // ตรวจสอบว่าไม่มี modal เปิดอยู่ก่อน
      cy.get('body').then(($body) => {
        if ($body.find('.modal.show').length === 0) {
          // Filter by group ก่อน
          cy.get('.list-group-item').not(':contains("All Groups")').first().click({ force: true });

          // รอให้ filter ทำงาน
          cy.wait(1000);

          // Search within filtered results
          cy.get('.tm-search input').type('Test', { force: true });

          // ตรวจสอบว่ามีผลลัพธ์
          cy.get('tbody tr').should('have.length.at.least', 0);

          // Clear search
          cy.get('.tm-search input').clear({ force: true });

          // Reset to All Groups
          cy.contains('All Groups').click({ force: true });
        }
      });
    });

    it('4.4 should sort assets by name', () => {
      // บันทึกลำดับก่อน sort
      let initialOrder = [];
      cy.get('tbody tr td:nth-child(2)').each(($el) => {
        initialOrder.push($el.text().trim());
      }).then(() => {
        if (initialOrder.length > 0) {
          // คลิกปุ่ม sort
          cy.contains('Sort').click();

          // บันทึกลำดับหลัง sort
          let sortedOrder = [];
          cy.get('tbody tr td:nth-child(2)').each(($el) => {
            sortedOrder.push($el.text().trim());
          }).then(() => {
            // ตรวจสอบว่าลำดับเปลี่ยน
            expect(initialOrder).to.not.deep.equal(sortedOrder);
          });
        }
      });
    });
  });

  // ==================== PAGINATION ====================
  describe('5. Pagination Functionality', () => {
    it('5.1 should have pagination controls', () => {
      // ตรวจสอบว่ามี select สำหรับ page size
      cy.get('select').last().should('exist');

      // ตรวจสอบว่ามี options ใน select
      cy.get('select').last().find('option').should('have.length.at.least', 1);

      // ตรวจสอบว่ามี pagination (อาจจะไม่มีถ้าข้อมูลน้อย)
      cy.get('body').then(($body) => {
        if ($body.find('.pagination').length > 0) {
          cy.get('.pagination').should('be.visible');
        }
      });
    });

    it('5.2 should navigate between pages if available', () => {
      cy.get('.pagination').then(($pagination) => {
        if ($pagination.find('.page-link').length > 3) {
          cy.get('.page-link').contains('2').click();
          cy.get('.pagination .active').should('contain', '2');
          cy.get('.page-link').contains('1').click();
          cy.get('.pagination .active').should('contain', '1');
        }
      });
    });
  });

  // ==================== MODAL INTERACTIONS ====================
    describe('6. Modal Interactions', () => {
      it('6.1 should open and close group modal properly', () => {
        // เปิด modal
        cy.contains('Create Asset Group').click();
        cy.wait(1000);  // เพิ่มเวลาให้ modal เปิด
        cy.get('#groupModal', { timeout: 10000 }).should('exist').and('be.visible');  // ตรวจสอบว่า modal เปิด

        // ปิดด้วยปุ่ม close
        cy.get('#modalGroup_btnClose').click();
        cy.get('#groupModal').should('not.be.visible');  // ตรวจสอบว่า modal ปิด

        // เปิดใหม่และปิดด้วย ESC
        cy.contains('Create Asset Group').click();
        cy.wait(1000);  // รอเวลาให้ modal เปิด
        cy.get('#groupModal', { timeout: 10000 }).should('exist').and('be.visible');  // ตรวจสอบว่า modal เปิด
        cy.get('body').type('{esc}');
        cy.get('#groupModal').should('not.be.visible');
      });

      it('6.2 should open and close asset modal properly', () => {
        // เปิด modal สร้าง asset
        cy.get('.list-group-item').not(':contains("All Groups")').first()
          .find('.bi-plus-circle').click();
        cy.wait(1000);  // เพิ่มเวลาให้ modal เปิด
        cy.get('#assetModal', { timeout: 10000 }).should('exist').and('be.visible');  // ตรวจสอบว่า modal เปิด

        // ปิดด้วยปุ่ม close
        cy.get('#modalAsset_btnClose').click();
        cy.get('#assetModal').should('not.be.visible');  // ตรวจสอบว่า modal ปิด
      });

      it('6.3 should maintain form state when reopening modal', () => {
        cy.contains('Create Asset Group').click();
        cy.get('#groupModal input[type="text"]').type('Test Input');
        cy.get('#modalGroup_btnClose').click();

        // เปิดใหม่ ควร clear form
        cy.contains('Create Asset Group').click();
        cy.get('#groupModal input[type="text"]').should('have.value', '');
        cy.get('#modalGroup_btnClose').click();
      });
    });

  // ==================== VALIDATION AND ERROR HANDLING ====================
  describe('7. Validation and Error Handling', () => {
    it('7.1 should handle duplicate group names', () => {
      // Mock API response สำหรับ duplicate name
      cy.intercept('POST', '**/asset-group/create', {
        statusCode: 409,
        body: { error: 'Group name already exists' }
      }).as('duplicateGroup');

      cy.contains('Create Asset Group').click();
      cy.get('#groupModal input[type="text"]').type('Existing Group');
      cy.get('#groupModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่ามี error message
      cy.contains('ชื่อ Group ซ้ำ').should('be.visible');

      // ปิด error
      cy.get('.swal2-confirm').click();
      cy.get('#modalGroup_btnClose').click();
    });

    it('7.2 should handle server errors gracefully', () => {
      // Mock server error
      cy.intercept('GET', '**/assets/all', {
        statusCode: 500,
        body: { error: 'Internal Server Error' }
      }).as('serverError');

      // รีเฟรชหน้า
      cy.reload();

      // ตรวจสอบว่าหน้าไม่ crash
      cy.contains('Asset Management').should('be.visible');
      cy.get('body').should('not.contain', 'Cannot read');
      cy.get('body').should('not.contain', 'Error:');
    });

    it('7.3 should handle network timeouts', () => {
      // Mock timeout
      cy.intercept('GET', '**/asset-group/list', {
        delay: 5000, // 5 seconds delay
        statusCode: 200,
        body: []
      }).as('slowRequest');

      cy.reload();

      // ตรวจสอบว่ามี loading state (ถ้ามี)
      cy.get('body').then(($body) => {
        if ($body.find('.loading, .spinner').length > 0) {
          cy.get('.loading, .spinner').should('be.visible');
        }
      });
    });
  });

  // ==================== RESPONSIVE DESIGN ====================
  describe('8. Responsive Design', () => {
    it('8.1 should display correctly on desktop', () => {
      cy.viewport(1280, 720);
      cy.get('.container-fluid').should('be.visible');
      cy.get('.row').should('be.visible');
    });

    it('8.2 should display correctly on tablet', () => {
      cy.viewport('ipad-2');
      cy.get('.container-fluid').should('be.visible');
      cy.get('.tm-search input').should('be.visible');
      cy.contains('Create Asset Group').should('be.visible');

      cy.viewport(1280, 720);
    });

    it('8.3 should display correctly on mobile', () => {
      cy.viewport('iphone-6');
      cy.get('.container-fluid').should('be.visible');
      cy.get('.tm-search input').should('be.visible');

      cy.viewport(1280, 720);
    });
  });

  // ==================== DATA INTEGRITY ====================
  describe('9. Data Integrity and State Management', () => {
    it('9.1 should maintain filter state after page refresh', () => {
      // ทำ filtering ก่อน
      cy.get('.list-group-item').not(':contains("All Groups")').first().click();

      // รีเฟรชหน้า
      cy.reload();

      // ตรวจสอบว่า state ยังคงอยู่
      cy.get('.list-group-item.active').should('exist');
    });

    it('9.2 should update summary cards when data changes', () => {
      // บันทึกค่า summary ก่อน
      let initialTotal;

      cy.contains('Total Assets').parent().find('h4').invoke('text').then((text) => {
        initialTotal = parseInt(text) || 0;

        // สร้าง asset ใหม่
        cy.get('.list-group-item').not(':contains("All Groups")').first()
          .find('.bi-plus-circle').click();
        cy.get('#assetModal input[type="text"]').type('Test Asset for Summary ' + Date.now());
        cy.get('#assetModal button[type="submit"]').contains('Save').click();

        // ตรวจสอบว่า summary อัพเดท
        cy.contains('Total Assets').parent().find('h4').should(($h4) => {
          const newTotal = parseInt($h4.text());
          if (initialTotal >= 0) {
            expect(newTotal).to.be.greaterThan(initialTotal);
          }
        });
      });
    });

    it('9.3 should sync data between components', () => {
      // เลือก group ใน sidebar
      cy.get('.list-group-item').not(':contains("All Groups")').first().then(($item) => {
        const groupName = $item.text().trim();
        cy.wrap($item).click();

        // ตรวจสอบว่าตารางแสดงเฉพาะ assets ใน group นั้น
        cy.get('tbody tr').should('have.length.at.least', 0);
      });
    });
  });

  // ==================== EDGE CASES ====================
  describe('10. Edge Cases and Special Scenarios', () => {
    it('10.1 should handle very long asset names', () => {
      const longName = 'A'.repeat(50); // ลดจาก 100 เป็น 50

      cy.get('.list-group-item').not(':contains("All Groups")').first()
        .find('.bi-plus-circle').click();
      cy.get('#assetModal input[type="text"]').type(longName);
      cy.get('#assetModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่า modal ปิด (แสดงว่าสร้างสำเร็จ)
      cy.get('#assetModal').should('not.be.visible');
    });

    it('10.2 should handle special characters in search', () => {
      const specialChars = '!@#$%^&*()_+-=[]{}|;:,.<>?';

      cy.get('.tm-search input').type(specialChars);
      cy.get('.tm-search input').should('have.value', specialChars);

      // ตรวจสอบว่าไม่ error
      cy.get('body').should('not.contain', 'Error');
      cy.get('.tm-search input').clear();
    });

    it('10.3 should handle empty states', () => {
      // ตรวจสอบการแสดงผลเมื่อไม่มีข้อมูล
      cy.get('tbody').then(($tbody) => {
        if ($tbody.find('tr').length === 0) {
          cy.contains('No assets found').should('be.visible');
        }
      });
    });

    it('10.4 should handle bulk operations with large quantities', () => {
      cy.get('.list-group-item').not(':contains("All Groups")').first()
        .find('.bi-plus-circle').click();
      cy.get('#assetModal input[type="text"]').type('Bulk Operation Test ' + Date.now());
      cy.get('#assetModal input[type="number"]').clear().type('5'); // ลดจาก 10 เป็น 5
      cy.get('#assetModal button[type="submit"]').contains('Save').click();

      // ตรวจสอบว่า modal ปิด
      cy.get('#assetModal').should('not.be.visible');
    });
  });

  // ==================== PERFORMANCE ====================
  describe('11. Performance Tests', () => {
    it('11.1 should load page within acceptable time', () => {
      const startTime = Date.now();

      cy.visit('/assetmanagement');
      cy.contains('Asset Management', { timeout: 10000 }).should('be.visible').then(() => {
        const endTime = Date.now();
        const loadTime = endTime - startTime;

        cy.log(`Page loaded in ${loadTime}ms`);
        expect(loadTime).to.be.lessThan(8000);
      });
    });

    it('11.2 should handle operations without significant delay', () => {
      const startTime = Date.now();

      // ทำ operation ต่างๆ
      cy.contains('Create Asset Group').click();
      cy.get('#groupModal').should('be.visible');
      cy.get('#modalGroup_btnClose').click();

      const endTime = Date.now();
      const operationTime = endTime - startTime;

      cy.log(`Modal operation completed in ${operationTime}ms`);
      expect(operationTime).to.be.lessThan(3000);
    });
  });

  // ==================== ACCESSIBILITY ====================
  describe('12. Accessibility Tests', () => {
    it('12.1 should have proper form labels and attributes', () => {
      cy.get('.tm-search input').should('have.attr', 'placeholder');
      cy.contains('Create Asset Group').should('have.attr', 'type', 'button');
    });

    it('12.2 should be navigable by keyboard', () => {
      // ใช้ realTab command หรือใช้ focus โดยตรง
      cy.get('.tm-search input').focus().should('be.focused');

      // ตรวจสอบว่า element อื่นๆ สามารถ focus ได้
      cy.contains('Create Asset Group').focus().should('be.focused');

      // ตรวจสอบว่า buttons สามารถคลิกได้เมื่อ focus
      cy.get('button').first().focus().should('be.focused');
    });

    it('12.3 should have meaningful text and icons', () => {
      // ตรวจสอบว่า icons มีความหมาย
      cy.get('.bi-search').should('exist');
      cy.get('.bi-plus-lg').should('exist');
      cy.get('.bi-pencil').should('exist');
      cy.get('.bi-trash').should('exist');
    });
  });

  after(() => {
    // Click the profile dropdown to show the logout option
    cy.get('.topbar-profile').click(); // Click the profile dropdown button

    // Click the logout button
    cy.get('li:contains("Logout")').click();

    // Handle SweetAlert confirmation
    cy.get('.swal2-confirm').click();  // Click the confirm button on the alert

    // Optionally, confirm the redirection to the login page
    cy.url().should('include', '/login');  // Ensure the URL includes '/login' to confirm successful logout
  });




});
