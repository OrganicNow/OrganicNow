import * as React from "react";
import * as ReactDOM from "react-dom/client";
globalThis.React = React;

let NotificationBell;
let NotificationProvider;

// ✅ mock provider context แทนการ stub
function MockNotificationProvider({ children }) {
  const mockValue = {
    notifications: [
      {
        id: 1,
        title: "System Maintenance",
        message: "Maintenance scheduled",
        type: "MAINTENANCE_SCHEDULE_CREATED",
        isRead: false,
        createdAt: new Date().toISOString(),
      },
    ],
    unreadCount: 1,
    loading: false,
    refreshNotifications: cy.stub().as("refreshNotifications"),
    markAsRead: cy.stub().as("markAsRead"),
    markAllAsRead: cy.stub().as("markAllAsRead"),
    deleteNotification: cy.stub().as("deleteNotification"),
  };

  const context = React.createContext(mockValue);
  const useNotifications = () => React.useContext(context);

  return React.createElement(
    context.Provider,
    { value: mockValue },
    children
  );
}

describe("🔔 NotificationBell (Context Wrapper Test)", () => {
  let rootEl;

  before(async () => {
    // ✅ import แบบ vite-ignore เพื่อหลบ dynamic analysis
    const bellModule = await import("../../frontend/src/component/NotificationBell.jsx" /* @vite-ignore */);
    NotificationBell = bellModule.default;
  });

  beforeEach(() => {
    document.body.innerHTML = "";
    rootEl = document.createElement("div");
    rootEl.id = "root";
    document.body.appendChild(rootEl);
  });

  afterEach(() => {
    document.body.innerHTML = "";
  });

  function renderComponent() {
    return new Promise((resolve) => {
      ReactDOM.createRoot(rootEl).render(
        React.createElement(MockNotificationProvider, null,
          React.createElement(NotificationBell)
        )
      );
      requestAnimationFrame(() => resolve());
    });
  }

  it("renders bell and badge", () => {
    return renderComponent().then(() => {
      cy.get(".notification-bell").should("exist");
      cy.get(".p-badge").should("contain.text", "1");
    });
  });

  it("shows overlay when clicked", () => {
    return renderComponent().then(() => {
      cy.get(".notification-bell").click();
      cy.get(".notification-panel").should("exist");
      cy.contains("Notifications").should("exist");
    });
  });
});
