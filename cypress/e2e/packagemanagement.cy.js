import "cypress-wait-until";

describe("Package Management - E2E Test", () => {
  const API = "http://localhost:8080";

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

  // -----------------------------------------
  // FIXED: Mock API ให้ตรงตาม UI จริง
  // -----------------------------------------
  const mockAPI = win => {
    cy.stub(win, "fetch").callsFake((url, opts={}) => {
      const method = (opts.method || "GET").toUpperCase();

      // ===== contract types =====
      if (url.includes(`${API}/contract-types`)) {
        return Promise.resolve(
          new Response(JSON.stringify([
            { id:1, name:"3 เดือน", months:3 },
            { id:2, name:"6 เดือน", months:6 }
          ]), { status:200 })
        );
      }

      // ===== packages =====
      if (url.includes(`${API}/packages`)) {
        if (method === "GET") {
          // ทำให้ table มีแค่ 2 rows เท่านั้น
          return Promise.resolve(
            new Response(JSON.stringify([
              {
                id:101,
                contractTypeId:1,
                contractType:{ id:1, name:"3 เดือน", months:3 },
                roomSize:0,
                price:5000,
                is_active:1
              },
              {
                id:102,
                contractTypeId:2,
                contractType:{ id:2, name:"6 เดือน", months:6 },
                roomSize:1,
                price:6000,
                is_active:0
              }
            ]), { status:200 })
          );
        }
        if (method === "POST") {
          return Promise.resolve(new Response(JSON.stringify({ ok:true }), { status:200 }));
        }
      }

      // toggle active
      if (url.includes("/toggle")) {
        return Promise.resolve(new Response(JSON.stringify({ ok:true }), { status:200 }));
      }

      return Promise.resolve(new Response(null, { status:404 }));
    });
  };

   beforeEach(() => {
      cy.visit('/packagemanagement');
      cy.contains('Package Management', { timeout: 10000 }).should('be.visible');

      // กำหนดขนาดหน้าจอให้เหมาะสมกับ headless และ head mode
      if (Cypress.browser.name === 'chrome') {
        // Headless browser view
        cy.viewport(1280, 720); // เปลี่ยนขนาดหน้าจอสำหรับ headless
      } else {
        // Head mode
        cy.viewport('macbook-15'); // เปลี่ยนขนาดหน้าจอสำหรับ head mode
      }
    });

  // -----------------------------------------
  // TESTS
  // -----------------------------------------

  describe("1. Structure", () => {
    it("1.1 Should show toolbar", () => {
      cy.contains("Filter").should("exist");
      cy.contains("Sort").should("exist");
      cy.contains("Create Package").should("exist");
    });
});


  describe("2. Create Package Modal", () => {
    beforeEach(() => {
      cy.contains("Create Package").click({ force:true });
      cy.get("#createPackageModal", { timeout:8000 }).should("exist");
    });

    it("2.1 Should show form fields", () => {
      cy.contains("Contract type").should("exist");
      cy.contains("Room Size").should("exist");
      cy.contains("Rent").should("exist");
    });
  });

  describe("3. Toggle Active", () => {
    it("3.1 Should toggle active checkbox", () => {
      cy.get("tbody tr").first().find("input[type='checkbox']").click({ force:true });
    });
  });

  describe("4. Pagination", () => {
    it("4.1 Should display pagination controls", () => {
      cy.get(".pagination").should("exist");
    });
  });

  after(() => {
    cy.get(".topbar-profile").click({ force:true });
    cy.contains("Logout").click({ force:true });
    cy.get(".swal2-confirm").click({ force:true });
    cy.url().should("include", "/login");
  });
 });