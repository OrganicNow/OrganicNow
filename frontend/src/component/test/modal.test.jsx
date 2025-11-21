// src/component/test/Modal.test.jsx
import React from "react";
import { render } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import Modal from "../Modal";

describe("Modal component", () => {
  it("ควร render modal ด้วย default size, ปุ่ม close ปกติ และแสดง title + icon + children", () => {
    const { container } = render(
      <Modal id="myModal" title="My Modal" icon="pi pi-user">
        <p>Content here</p>
      </Modal>
    );

    // div หลักของ modal
    const modal = container.querySelector("#myModal");
    expect(modal).not.toBeNull();
    expect(modal.className).toContain("modal");
    expect(modal.className).toContain("fade");

    // modal-dialog ควรมี default size = modal-xl
    const dialog = modal.querySelector(".modal-dialog");
    expect(dialog).not.toBeNull();
    expect(dialog.className).toContain("modal-xl");

    // ปุ่ม close (ไม่มี prop back)
    const closeBtn = modal.querySelector(".modal-header .btn-close");
    expect(closeBtn).not.toBeNull();
    expect(closeBtn.getAttribute("data-bs-dismiss")).toBe("modal");
    expect(closeBtn.getAttribute("id")).toBe("myModal_btnClose");
    // ไม่ควรมี data-bs-target
    expect(closeBtn.getAttribute("data-bs-target")).toBeNull();

    // header icon + title
    const header = modal.querySelector(".card-header");
    expect(header).not.toBeNull();
    expect(header.textContent).toContain("My Modal");

    const icon = header.querySelector("i");
    expect(icon).not.toBeNull();
    expect(icon.className).toContain("pi");
    expect(icon.className).toContain("pi-user");
    expect(icon.className).toContain("me-1");

    // children อยู่ใน card-body
    const body = modal.querySelector(".card-body");
    expect(body).not.toBeNull();
    expect(body.textContent).toContain("Content here");
  });

  it("ควรใช้ size และ scrollable ที่ส่งเข้ามา และใช้ปุ่ม back แทน close เมื่อมี prop back", () => {
    const { container } = render(
      <Modal
        id="editModal"
        title="Edit Item"
        icon="pi pi-pencil"
        size="modal-lg"
        scrollable="modal-dialog-scrollable"
        back="previousModal"
      >
        <span>Edit content</span>
      </Modal>
    );

    const modal = container.querySelector("#editModal");
    expect(modal).not.toBeNull();

    const dialog = modal.querySelector(".modal-dialog");
    expect(dialog).not.toBeNull();
    // ควรมี class จาก size และ scrollable
    expect(dialog.className).toContain("modal-lg");
    expect(dialog.className).toContain("modal-dialog-scrollable");

    // ปุ่ม header ตอนนี้เป็นปุ่ม back (toggle modal อื่น)
    const backBtn = modal.querySelector(".modal-header .btn-close");
    expect(backBtn).not.toBeNull();
    expect(backBtn.getAttribute("data-bs-toggle")).toBe("modal");
    expect(backBtn.getAttribute("data-bs-target")).toBe("#previousModal");
    // ไม่ควรมี data-bs-dismiss
    expect(backBtn.getAttribute("data-bs-dismiss")).toBeNull();
    // ไม่ควรมี id ปิดเองแบบ close ปกติ
    expect(backBtn.getAttribute("id")).toBeNull();

    // ตรวจ title อีกครั้ง
    const header = modal.querySelector(".card-header");
    expect(header.textContent).toContain("Edit Item");
  });
});
