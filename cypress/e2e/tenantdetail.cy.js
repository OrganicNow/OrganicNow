describe("Tenant Detail - E2E Test", () => {
  const contractId = "12345"; // mock contract ID for testing

  before(() => {
    // Login before starting tests
    cy.visit('/login');

    // Ensure the login page is visible
    cy.url({ timeout: 15000 }).should('include', '/login');
    cy.get('input[type="text"]', { timeout: 15000 }).should('be.visible');
    cy.get('input[type="password"]', { timeout: 15000 }).should('be.visible');

    // Fill login details
    cy.get('input[type="text"]').type('superadmin');
    cy.get('input[type="password"]').type('admin123', { log: false });
    cy.get('button[type="submit"]').click();

    // Ensure navigation to the dashboard
    cy.url({ timeout: 10000 }).should('include', '/dashboard');
  });

  beforeEach(() => {
    // Intercepting the API request to fetch tenant details
    cy.intercept("GET", `/tenant/${contractId}`, {
      statusCode: 200,
      body: {
        tenantId: 1,
        firstName: "John",
        lastName: "Doe",
        email: "john@example.com",
        phoneNumber: "0812345678",
        nationalId: "1234567890123",
        room: "101",
        floor: 1,
        contractName: "3 เดือน",
        rentAmountSnapshot: "5000",
        signDate: "2024-01-01T00:00:00",
        startDate: "2024-01-01",
        endDate: "2024-12-31",
        deposit: "10000",
      },
    }).as("getTenantDetail");

    // Visit the Tenant Detail page
    cy.visit(`/tenantdetail/${contractId}`);

    // Wait for the tenant details to be loaded
    cy.wait('@getTenantDetail', { timeout: 15000 });
  });

  // ==================== Basic Page Structure ====================
  describe('Basic Page Structure and Layout', () => {
    it('should load tenant details page successfully', () => {
      // ตรวจสอบว่าโหลดหน้าได้สำเร็จ
      cy.url().should('include', '/tenantdetail');
      cy.get('body').should('be.visible');
    });

    it('should display basic tenant information', () => {
      // ตรวจสอบข้อมูลผู้เช่าพื้นฐานที่แน่นอนว่าต้องมี
      cy.contains('John', { timeout: 10000 }).should('be.visible');
      cy.contains('Doe', { timeout: 10000 }).should('be.visible');
    });

    it('should display contact information', () => {
      // ตรวจสอบข้อมูลการติดต่อ
      cy.contains('0812345678').should('be.visible');
      cy.contains('john@example.com').should('be.visible');
    });

    it('should display room information', () => {
      // ตรวจสอบข้อมูลห้อง
      cy.contains('1').should('be.visible');
      cy.contains('101').should('be.visible');
    });
  });

  // ==================== Page Elements ====================
  describe('Page Elements', () => {
    it('should have visible page content', () => {
      cy.get('body').should('be.visible');
      cy.get('div').should('exist');
    });

    it('should have navigation elements', () => {
      cy.get('a').should('exist');
      cy.get('button').should('exist');
    });
  });

  // ==================== Responsive Design ====================
  describe('Responsive Design', () => {
    it('should display correctly on mobile viewport', () => {
      cy.viewport('iphone-6');
      cy.get('body').should('be.visible');
    });

    it('should display correctly on tablet viewport', () => {
      cy.viewport('ipad-2');
      cy.get('body').should('be.visible');
    });

    it('should display correctly on desktop viewport', () => {
      cy.viewport(1280, 720);
      cy.get('body').should('be.visible');
    });
  });

  // ==================== Navigation ====================
  describe('Navigation', () => {
    it('should allow browser back navigation', () => {
      cy.go('back');
      cy.url().should('include', '/dashboard');
    });

    it('should allow browser forward navigation', () => {
      cy.go('back');
      cy.go('forward');
      cy.url().should('include', '/tenantdetail');
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