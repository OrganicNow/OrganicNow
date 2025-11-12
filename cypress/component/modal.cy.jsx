import * as React from "react";
import * as ReactDOM from "react-dom/client";
globalThis.React = React;

import Modal from "../../frontend/src/component/modal.jsx";

describe("Modal Component", () => {
  let rootEl;

  beforeEach(() => {
    // ✅ สร้าง root และ inject CSS ให้ modal แสดงจริง
    rootEl = document.createElement("div");
    rootEl.id = "test-root";
    document.body.innerHTML = "";
    document.body.appendChild(rootEl);

    const style = document.createElement("style");
    style.innerHTML = `
      .modal.fade { display: block !important; opacity: 1 !important; }
      .modal-backdrop { display: none !important; }
    `;
    document.head.appendChild(style);
  });

  afterEach(() => {
    document.body.innerHTML = "";
  });

  function renderModal(element) {
    // ✅ รอให้ React render เสร็จก่อนถึงจะเริ่ม cy.get
    return new Promise((resolve) => {
      ReactDOM.createRoot(rootEl).render(element);
      requestAnimationFrame(() => resolve());
    });
  }

  it("renders correctly", () => {
    return renderModal(
      React.createElement(
        Modal,
        { id: "myModal", title: "Test Modal", icon: "pi pi-user" },
        React.createElement("p", null, "Hello World")
      )
    ).then(() => {
      cy.get("#myModal").should("exist").and("be.visible");
      cy.get(".card-header").should("contain.text", "Test Modal");
      cy.get(".pi-user").should("exist");
      cy.get(".card-body").should("contain.text", "Hello World");
    });
  });

  it("renders scrollable large modal with custom classes", () => {
    return renderModal(
      React.createElement(
        Modal,
        {
          id: "scrollModal",
          title: "Scroll Test",
          size: "modal-lg",
          scrollable: "modal-dialog-scrollable",
        },
        React.createElement("p", null, "Scroll body")
      )
    ).then(() => {
      cy.get(".modal-dialog")
        .should("have.class", "modal-lg")
        .and("have.class", "modal-dialog-scrollable");
    });
  });

  it("renders close button when no back prop", () => {
    return renderModal(
      React.createElement(Modal, { id: "closeModal", title: "Close Test" })
    ).then(() => {
      cy.get("#closeModal_btnClose")
        .should("exist")
        .and("have.attr", "data-bs-dismiss", "modal");
    });
  });

  it("renders back button when back prop is provided", () => {
    return renderModal(
      React.createElement(
        Modal,
        { id: "backModal", title: "Back Test", back: "previousModal" },
        React.createElement("p", null, "Back button test")
      )
    ).then(() => {
      cy.get(".btn-close")
        .should("have.attr", "data-bs-target", "#previousModal")
        .and("have.attr", "data-bs-toggle", "modal");
    });
  });

  it("renders children properly", () => {
    return renderModal(
      React.createElement(
        Modal,
        { id: "childModal", title: "Child Test" },
        React.createElement(
          "div",
          { className: "child-div" },
          React.createElement("p", null, "Inside content")
        )
      )
    ).then(() => {
      cy.get(".child-div").should("exist").and("contain.text", "Inside content");
    });
  });
});
