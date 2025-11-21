import "cypress-wait-until";

describe("Room Detail - Complete E2E Test", () => {
  const API = "http://localhost:8080";
  const ROOM_ID = 1;

  // ------------------------------------------
  // LOGIN
  // ------------------------------------------
  before(() => {
    cy.visit("/login");

    cy.get('input[type="text"]').type("superadmin");
    cy.get('input[type="password"]').type("admin123", { log: false });
    cy.get('button[type="submit"]').click();

    cy.url().should("include", "/dashboard");
  });

  // ------------------------------------------
  // STUB ALL API BEFORE ENTER PAGE
  // ------------------------------------------
  beforeEach(() => {
    // Room detail
    cy.intercept("GET", `${API}/room/${ROOM_ID}/detail`, {
      statusCode: 200,
      body: {
        roomId: ROOM_ID,
        roomFloor: 1,
        roomNumber: "101",
        roomSize: "Studio",
        status: "available",
        firstName: "John",
        lastName: "Doe",
        phoneNumber: "0900000000",
        email: "john@example.com",
        contractName: "6-month",
        signDate: "2025-01-01T00:00:00",
        startDate: "2025-01-02T00:00:00",
        endDate: "2025-07-01T00:00:00",
        assets: [
          { assetId: 10, assetName: "Air Conditioner", checked: true },
        ],
        requests: [
          {
            id: 99,
            issueTitle: "Water leak",
            scheduledDate: "2025-02-01T00:00:00",
            finishDate: "2025-02-05T00:00:00",
          },
        ],
      },
    }).as("detail");

    // All assets
    cy.intercept("GET", `${API}/assets/all`, {
      statusCode: 200,
      body: {
        result: [
          { assetId: 10, assetName: "Air Conditioner", assetGroupId: 1 },
          { assetId: 11, assetName: "Bed", assetGroupId: 2 },
          { assetId: 12, assetName: "Table", assetGroupId: 1 },
        ],
      },
    }).as("assetsAll");

    // Groups
    cy.intercept("GET", `${API}/asset-group/list`, {
      statusCode: 200,
      body: [
        { id: 1, name: "Electrical" },
        { id: 2, name: "Furniture" },
      ],
    }).as("groups");

    // Events
    cy.intercept("GET", `${API}/room/${ROOM_ID}/events`, {
      statusCode: 200,
      body: [
        {
          eventId: 1,
          eventType: "added",
          assetName: "Air Conditioner",
          reasonType: "addon",
          createdAt: "2025-02-03T12:00:00",
        },
      ],
    }).as("events");

    // Room list (used assets mapping)
    cy.intercept("GET", `${API}/room`, {
      statusCode: 200,
      body: [
        {
          roomId: 99,
          roomNumber: "999",
          assets: [{ assetId: 11 }],
        },
      ],
    }).as("roomList");

    cy.visit(`/roomdetail/${ROOM_ID}`);

    cy.wait("@detail");
    cy.contains("Room Detail").should("exist");
  });

  // ------------------------------------------
  // 1. Page Structure
  // ------------------------------------------
  describe("1. Structure", () => {
    it("1.1 Should show breadcrumb + Edit Room button", () => {
      cy.contains("Room Management").should("exist");
      cy.contains("101").should("exist");
      cy.contains("Edit Room").should("be.visible");
    });

    it("1.2 Should show room information", () => {
      cy.contains("Floor:").should("exist");
      cy.contains("Room Size:").should("exist");
      cy.contains("Available").should("exist");
    });
  });

  // ------------------------------------------
  // 2. Tabs Test
  // ------------------------------------------
  describe("2. Tabs", () => {
    it("2.1 Assets tab should show items", () => {
      cy.get("#assets").should("exist");
      cy.contains("Air Conditioner").should("exist");
    });

    it("2.2 Request History tab should work", () => {
      cy.get("#requests-tab").click();
      cy.contains("Water leak").should("exist");
    });

    it("2.3 Asset History tab should work", () => {
      cy.get("#history-tab").click();
      cy.contains("added").should("exist");
      cy.contains("Air Conditioner").should("exist");
    });
  });

  // ------------------------------------------
  // 3. Edit Room Modal
  // ------------------------------------------
  describe("3. Edit Modal", () => {
    beforeEach(() => {
      cy.contains("Edit Room").click();
      cy.get("#editRoomModal .form-select")
        .filter(':contains("All Groups")')
        .select("1");

    });

    it("3.1 Fields visible", () => {
      cy.contains("label", "Floor").should("exist");
      cy.contains("label", "Room Size").should("exist");
      cy.contains("label", "Select Assets for this Room").should("exist");
    });



    it("3.2 Should check/uncheck assets", () => {
      cy.get("#asset-12").check({ force: true });
      cy.get("#asset-12").should("be.checked");
    });

    it("3.3 Should open reason modal after clicking Save", () => {
      cy.contains("Save").click();
      cy.contains("Select Reason for Update").should("exist");
    });
  });

  // ------------------------------------------
  // 4. Save with Reason
  // ------------------------------------------
  describe("4. Save Operation", () => {
    it("4.1 Should send PUT requests when Confirm", () => {
      cy.contains("Edit Room").click();
      cy.contains("Save").click();

      cy.intercept("PUT", `${API}/room/${ROOM_ID}/assets/event`, {
        statusCode: 200,
        body: { message: "ok" },
      }).as("saveEvent");

      cy.intercept("PUT", `${API}/room/${ROOM_ID}`, {
        statusCode: 200,
        body: { message: "ok" },
      }).as("saveRoom");

      cy.contains("Confirm").click();

      cy.wait("@saveEvent");
      cy.wait("@saveRoom");
    });
  });

  // ------------------------------------------
  // 5. Logout
  // ------------------------------------------
  after(() => {
    cy.get(".topbar-profile").click({ force: true });
    cy.contains("Logout").click({ force: true });
    cy.get(".swal2-confirm").click({ force: true });

    cy.url().should("include", "/login");
  });
});
