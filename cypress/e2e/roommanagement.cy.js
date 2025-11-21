// cypress/e2e/roommanagement.cy.js
import "cypress-wait-until";

describe("Room Management - Complete E2E Test", () => {
  const API = "http://localhost:8080";

  // ===============================
  // LOGIN ก่อนเริ่มทั้งหมด
  // ===============================
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


  // ===============================
  // Mock Fetch ก่อนเข้าเพจทุกครั้ง
  // ===============================
  beforeEach(() => {
    // Mock API ก่อนโหลดหน้า
    cy.intercept("GET", `${API}/room/list`, {
      statusCode: 200,
      body: [
        {
          id: 1,
          roomNumber: "101",
          roomFloor: 1,
          roomSize: "Studio",
          status: "available",
          requests: [{ finishDate: null }],
        },
        {
          id: 2,
          roomNumber: "205",
          roomFloor: 2,
          roomSize: "Deluxe",
          status: "occupied",
          requests: [{ finishDate: "2025-03-01" }],
        },
      ],
    }).as("roomList");

    cy.intercept("GET", `${API}/assets/available`, {
      statusCode: 200,
      body: {
        result: [
          { id: 10, assetName: "Air Conditioner" },
          { id: 11, assetName: "Bed" },
        ],
      },
    }).as("assetsAvailable");

    cy.visit("/roommanagement");

    // รอให้ table load เสร็จ
    cy.wait("@roomList");
    cy.wait("@assetsAvailable");

    cy.contains("Room Management").should("be.visible");
  });


  // ======================================================
  // 1. โครงสร้างหน้า
  // ======================================================
  describe("1. Page Structure", () => {
    it("1.1 Should load table", () => {
      cy.get("table").should("be.visible");
      cy.get("table tbody tr").should("have.length", 2);
    });

    it("1.2 Toolbar elements visible", () => {
      cy.contains("button", "Filter").should("be.visible");
      cy.contains("button", "Sort").should("be.visible");
      cy.get('input[placeholder="Search"]').should("be.visible");
      cy.contains("button", "Add Room").should("be.visible");
    });
  });

  // ======================================================
  // 2. ฟิลเตอร์ + ค้นหา
  // ======================================================
  describe("2. Search & Filters", () => {
    it("2.1 Search rooms", () => {
      cy.get('input[placeholder="Search"]').type("101");
      cy.get("table tbody tr").should("have.length", 1);
      cy.contains("101");
    });

    it("2.2 Clear search", () => {
      cy.get('input[placeholder="Search"]').type("101");
      cy.get("table tbody tr").should("have.length", 1);

      cy.get('input[placeholder="Search"]').clear();
      cy.get("table tbody tr").should("have.length", 2);
    });

    it("2.3 Filter by Floor", () => {
      cy.contains("Filter").click();
      cy.get("#roomFilterCanvas").should("be.visible");

      cy.get("#roomFilterCanvas select")
        .first()
        .select("1");

      cy.contains("Apply").click();
      cy.get("table tbody tr").should("have.length", 1);
      cy.contains("101");
    });
  });

  // ======================================================
  // 3. Sorting
  // ======================================================
  describe("3. Sorting", () => {
    it("3.1 Should sort ascending/descending", () => {
      cy.contains("Sort").click();
      cy.get("tbody tr").first().contains("205");

      cy.contains("Sort").click();
      cy.get("tbody tr").first().contains("101");
    });
  });

  // ======================================================
  // 4. Add New Room Modal
  // ======================================================
  describe("4. Add Room Modal", () => {
    beforeEach(() => {
      cy.contains("Add Room").click();
      cy.get("#addRoomModal").should("be.visible");
    });

    it("4.1 Modal fields visible", () => {
      cy.contains("label", "Room Number").should("be.visible");
      cy.contains("label", "Floor").should("be.visible");
      cy.contains("label", "Room Size").should("be.visible");
      cy.contains("label", "Select Assets for this Room").should("be.visible");

      cy.get('input[type="text"]').should("exist");
      cy.get("select").should("exist");
    });

    it("4.2 Should check available assets", () => {
      cy.contains("Air Conditioner").should("exist");
      cy.contains("Bed").should("exist");
    });

    it("4.3 Should select assets", () => {
      cy.get("#asset-10").check();
      cy.get("#asset-11").check();

      cy.get("#asset-10").should("be.checked");
      cy.get("#asset-11").should("be.checked");
    });
  });

  // ======================================================
  // 5. Action Buttons
  // ======================================================
  describe("5. Action Buttons", () => {
    it("5.1 Should navigate to room detail page", () => {
      cy.get("table tbody tr")
        .first()
        .find("button.form-Button-Edit")
        .click();

      cy.location("pathname").should("include", "/roomdetail/");
    });

    it("5.2 Should click delete button", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").resolves(new Response(null, { status: 200 }));
      });

      cy.get("table tbody tr")
        .first()
        .find("button[aria-label='Delete']")
        .click();

      // SweetAlert pop-up
      cy.get(".swal2-confirm", { timeout: 8000 }).click({ force: true });
    });
  });

  // ======================================================
  // 6. Pagination
  // ======================================================
  describe("6. Pagination", () => {
    it("6.1 Should show pagination controls", () => {
      cy.get(".pagination").should("exist");
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
