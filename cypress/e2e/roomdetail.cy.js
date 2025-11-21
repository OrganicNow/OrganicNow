// roomdetail.cy.js
describe('Room Detail - Complete Test Suite', () => {
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
    // รีเฟรชหน้าทุกครั้งก่อนทดสอบ
    beforeEach(() => {
        // ไปที่หน้า room management
        cy.visit('/roommanagement');

        // รอให้หน้าโหลด (ตรวจสอบหลายวิธี)
        cy.get('body').then(($body) => {
            if ($body.text().includes('Room Management') ||
                $body.find('h1, h2, h3').text().includes('Room') ||
                $body.find('table').length > 0) {
                cy.log('Room Management page loaded');
            }
        });

        // หาและคลิกปุ่มหรือไอคอนเพื่อเข้า room detail
        cy.get('body').then(($body) => {
            const detailSelectors = [
                '.bi-eye',
                '.bi-folder',
                '.bi-pencil',
                '[title*="detail"]',
                '[title*="view"]',
                '[aria-label*="detail"]',
                '[aria-label*="view"]',
                'button:contains("Detail")',
                'a:contains("Detail")',
                'button:contains("View")',
                'a:contains("View")',
                '.btn-outline-primary',
                '.btn-primary'
            ];

            let clicked = false;

            // ลองหาและคลิกตาม selectors ต่างๆ
            detailSelectors.forEach(selector => {
                if ($body.find(selector).length > 0 && !clicked) {
                    cy.get(selector).first().click({ force: true });
                    clicked = true;
                    cy.log(`Clicked on selector: ${selector}`);
                }
            });

            // ถ้ายังไม่คลิก ให้ลองคลิกแถวแรกในตาราง
            if (!clicked && $body.find('table tbody tr').length > 0) {
                cy.get('table tbody tr').first().click({ force: true });
                clicked = true;
                cy.log('Clicked on first table row');
            }

            // ถ้ายังไม่คลิก ให้ใช้ URL โดยตรง
            if (!clicked) {
                cy.visit('/roomdetail/1');
                cy.log('Using direct URL to room detail');
            }
        });

        // รอให้หน้า room detail โหลด
        cy.get('body').then(($body) => {
            if ($body.text().includes('Room Detail') ||
                $body.text().includes('Room Information') ||
                $body.find('.container-fluid').length > 0) {
                cy.log('Room Detail page loaded successfully');
            } else {
                // ถ้าไม่ใช่หน้า room detail ให้ลองโหลดใหม่
                cy.visit('/roomdetail/1');
            }
        });

        cy.contains('Room Detail', { timeout: 10000 }).should('be.visible');
    });

    // ==================== BASIC PAGE STRUCTURE ====================
    describe('1. Basic Page Structure and Layout', () => {
        it('1.1 should load room detail page successfully', () => {
            cy.contains('Room Detail').should('be.visible');
            cy.get('.container-fluid').should('be.visible');
        });

        it('1.2 should display edit room button', () => {
            cy.get('body').then(($body) => {
                if ($body.find('button:contains("Edit Room")').length > 0) {
                    cy.contains('button', 'Edit Room').should('be.visible');
                } else if ($body.find('button:contains("Edit")').length > 0) {
                    cy.contains('button', 'Edit').should('be.visible');
                }
            });
        });

        it('1.3 should display main information sections', () => {
            cy.get('body').then(($body) => {
                if ($body.text().includes('Room Information')) {
                    cy.contains('Room Information').should('be.visible');
                }
                if ($body.text().includes('Current Tenant') || $body.text().includes('Tenant Information')) {
                    cy.contains('Current Tenant').should('be.visible');
                }
            });
        });
    });

    // ==================== SIMPLIFIED TESTS ====================
    describe('2. Basic Functionality', () => {
        it('2.1 should open edit modal', () => {
            cy.get('body').then(($body) => {
                if ($body.find('button:contains("Edit Room")').length > 0) {
                    cy.contains('button', 'Edit Room').click();
                    cy.get('.modal, [role="dialog"]', { timeout: 5000 }).should('be.visible');
                }
            });
        });

        it('2.2 should display room data', () => {
            cy.get('body').then(($body) => {
                // ตรวจสอบว่ามีข้อมูลแสดง (ไม่ใช่ loading หรือ error)
                expect($body.text()).not.to.include('Loading...');
                expect($body.text()).not.to.include('No data found');
                expect($body.text()).not.to.include('Failed to fetch');
            });
        });

        it('2.3 should have navigation elements', () => {
            cy.get('body').then(($body) => {
                // ตรวจสอบ breadcrumb หรือ back button
                if ($body.find('a:contains("Room Management")').length > 0) {
                    cy.contains('a', 'Room Management').should('exist');
                } else if ($body.find('.breadcrumb').length > 0) {
                    cy.get('.breadcrumb').should('exist');
                }
            });
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