// cypress/e2e/roommanagement.cy.js
import "cypress-wait-until";

describe("Room Management - Complete Test Suite", () => {
    // Login ก่อนเริ่ม tests ทั้งหมด
    before(() => {
        cy.visit('/login');

        // Ensure the page URL is correct
        cy.url({ timeout: 15000 }).should('include', '/login');

        // Wait for the username and password fields to be visible
        cy.get('input[type="text"]', { timeout: 15000 }).should('be.visible');
        cy.get('input[type="password"]', { timeout: 15000 }).should('be.visible');

        // Fill in the login details using env variables
        cy.get('input[type="text"]').type(Cypress.env('adminUsername'));
        cy.get('input[type="password"]').type(Cypress.env('adminPassword'), { log: false });
        cy.get('button[type="submit"]').click();

        // Wait until the dashboard page loads
        cy.url({ timeout: 10000 }).should('include', '/dashboard');
    });

    // รีเฟรชหน้าทุกครั้งก่อนทดสอบ
    beforeEach(() => {
        cy.visit('/roommanagement');
        cy.contains('Room Management', { timeout: 10000 }).should('be.visible');
    });

    // ==================== BASIC PAGE STRUCTURE AND LAYOUT ====================
    describe('1. Basic Page Structure and Layout', () => {
        it('1.1 should load room management page successfully', () => {
            cy.contains('Room Management').should('be.visible');
            cy.get('.container-fluid').should('be.visible');
        });

        it('1.2 should display add room button', () => {
            cy.contains('Add Room').should('be.visible');
        });

        it('1.3 should display filter and sort buttons', () => {
            cy.contains('Filter').should('be.visible');
            cy.contains('Sort').should('be.visible');
        });

        it('1.4 should display search input', () => {
            cy.get('input[placeholder="Search"]').should('be.visible');
        });
    });

    // ==================== TABLE AND DATA DISPLAY ====================
    describe('2. Table and Data Display', () => {
        it('2.1 should display room table', () => {
            cy.get('table').should('be.visible');
            cy.get('table tbody tr').should('have.length.at.least', 1);
        });

        it('2.2 should display room information in table', () => {
            cy.get('table tbody tr').first().within(() => {
                cy.contains('td', /101|205/).should('exist');
                cy.get('button').should('have.length.at.least', 1);
            });
        });

        it('2.3 should display action buttons for each room', () => {
            cy.get('table tbody tr').first().within(() => {
                cy.get('button[aria-label="Delete"]').should('exist');
                cy.get('button.form-Button-Edit').should('exist');
            });
        });
    });

    // ==================== SEARCH FUNCTIONALITY ====================
    describe('3. Search Functionality', () => {
        it('3.1 should search rooms by room number', () => {
            cy.get('input[placeholder="Search"]').type('101');
            cy.get('table tbody tr').should('have.length', 1);
            cy.contains('101').should('be.visible');
        });

        it('3.2 should clear search and show all rooms', () => {
            cy.get('input[placeholder="Search"]').type('101');
            cy.get('table tbody tr').should('have.length', 1);

            cy.get('input[placeholder="Search"]').clear();
            cy.get('table tbody tr').should('have.length.at.least', 2);
        });
    });

    // ==================== FILTER FUNCTIONALITY ====================
    describe('4. Filter Functionality', () => {
        it('4.1 should open filter modal', () => {
            cy.contains('Filter').click();
            cy.get('#roomFilterCanvas').should('be.visible');
        });

        it('4.2 should display filter options', () => {
            cy.contains('Filter').click();
            cy.get('#roomFilterCanvas').within(() => {
                cy.get('select').should('exist');
                cy.contains('Apply').should('be.visible');
            });
        });


    });

    // ==================== SORT FUNCTIONALITY ====================
    describe('5. Sort Functionality', () => {
        it('5.1 should sort rooms', () => {
            cy.contains('Sort').click();
            cy.get('tbody tr').first().should('exist');
        });

        it('5.2 should change sort order when clicked multiple times', () => {
            cy.contains('Sort').click();
            const firstRoomFirstClick = cy.get('tbody tr').first().invoke('text');

            cy.contains('Sort').click();
            const firstRoomSecondClick = cy.get('tbody tr').first().invoke('text');

            // ตรวจสอบว่าข้อมูลเปลี่ยนเมื่อคลิก sort ซ้ำ
            firstRoomFirstClick.should('not.equal', firstRoomSecondClick);
        });
    });

    // ==================== ADD ROOM MODAL ====================
    describe('6. Add Room Modal', () => {
        it('6.1 should open add room modal', () => {
            cy.contains('Add Room').click();
            cy.get('#addRoomModal').should('be.visible');
        });

        it('6.2 should display form fields in modal', () => {
            cy.contains('Add Room').click();
            cy.get('#addRoomModal', { timeout: 5000 }).within(() => {
                cy.contains('Room Number').should('be.visible');
                cy.contains('Floor').should('be.visible');
                cy.contains('Room Size').should('be.visible');
                cy.get('input[type="text"]').should('exist');
                cy.get('select').should('exist');
            });
        });

        it('6.3 should display asset selection', () => {
            cy.contains('Add Room').click();
            cy.get('#addRoomModal', { timeout: 5000 }).within(() => {
                cy.contains('Select Assets for this Room').should('be.visible');
                cy.get('.form-check-input').should('have.length.at.least', 1);
            });
        });
    });

    // ==================== ACTION BUTTONS ====================
    describe('7. Action Buttons', () => {
        it('7.1 should navigate to room detail page when clicking edit', () => {
            cy.get('table tbody tr').first().find('button.form-Button-Edit').click();
            cy.url().should('include', '/roomdetail/');
        });

        it('7.2 should open delete confirmation when clicking delete', () => {
            cy.get('table tbody tr').first().find('button[aria-label="Delete"]').click();
            cy.get('.swal2-popup').should('be.visible');
        });
    });

    // ==================== RESPONSIVE DESIGN ====================
    describe('8. Responsive Design', () => {
        it('8.1 should display correctly on desktop', () => {
            cy.viewport(1280, 720);
            cy.get('.container-fluid').should('be.visible');
        });

        it('8.2 should display correctly on tablet', () => {
            cy.viewport('ipad-2');
            cy.get('.container-fluid').should('be.visible');
            cy.viewport(1280, 720);
        });

        it('8.3 should display correctly on mobile', () => {
            cy.viewport('iphone-6');
            cy.get('.container-fluid').should('be.visible');
            cy.viewport(1280, 720);
        });
    });



    // Logout หลังจาก tests ทั้งหมด
    after(() => {
        // Ensure the profile dropdown is visible and click it
        cy.get('.topbar-profile').click({ force: true });

        // Click the logout button
        cy.contains('li', 'Logout').click({ force: true });

        // Handle SweetAlert confirmation
        cy.get('.swal2-confirm').click({ force: true });

        // Confirm the redirection to the login page
        cy.url().should('include', '/login');
    });
});