// src/component/test/pagination.test.jsx
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import Pagination from "../Pagination";

describe("Pagination component", () => {
  // helper สร้าง component พร้อม default props
  const setup = (overrideProps = {}) => {
    const onPageChange = vi.fn();
    const onPageSizeChange = vi.fn();

    const props = {
      currentPage: 1,
      totalPages: 5,
      totalRecords: 42,
      onPageChange,
      onPageSizeChange,
      ...overrideProps,
    };

    const utils = render(<Pagination {...props} />);

    return { ...utils, props, onPageChange, onPageSizeChange };
  };

  it("ควรแสดงข้อความ Showing X to Y of Z results ตาม currentPage และ pageSize ปัจจุบัน", () => {
    const { container } = setup({
      currentPage: 2,
      totalPages: 5,
      totalRecords: 42,
    });

    // อ่านค่า page size ปัจจุบันจาก select
    const select = container.querySelector("select");
    const pageSize = Number(select.value);

    // คำนวณ expected start/end
    const expectedStart = Math.min((2 - 1) * pageSize + 1, 42);
    const expectedEnd = Math.min(2 * pageSize, 42);

    const infoEls = screen.getAllByText(/Showing/i);
    expect(infoEls.length).toBeGreaterThanOrEqual(1);

    const infoText = infoEls[0].textContent;
    expect(infoText).toContain(String(expectedStart));
    expect(infoText).toContain(String(expectedEnd));
    expect(infoText).toContain(String(42));
  });

  it("เมื่อ totalRecords = 0 ควรแสดงข้อความ No results found", () => {
    setup({
      currentPage: 1,
      totalPages: 1,
      totalRecords: 0,
    });

    const noResultEls = screen.getAllByText("No results found");
    expect(noResultEls.length).toBeGreaterThanOrEqual(1);
  });

  it("เมื่อคลิกปุ่ม next/prev ควรเรียก onPageChange ด้วยหมายเลขหน้าที่ถูกต้อง และไม่ออกนอกช่วง", () => {
    // currentPage = 1
    const { onPageChange, container, rerender, props } = setup({
      currentPage: 1,
      totalPages: 3,
      totalRecords: 30,
    });

    const buttons = container.querySelectorAll("button.page-link");
    // โครง: [prev, page1, page2, page3, next]
    const prevBtn = buttons[0];
    const nextBtn = buttons[buttons.length - 1];

    // กด prev ที่หน้า 1 ไม่ควรเรียก onPageChange
    fireEvent.click(prevBtn);
    expect(onPageChange).not.toHaveBeenCalled();

    // กด next จากหน้า 1 → ควรไปหน้า 2
    fireEvent.click(nextBtn);
    expect(onPageChange).toHaveBeenCalledTimes(1);
    expect(onPageChange).toHaveBeenCalledWith(2);

    // จำลองไปหน้า 3 แล้วลองกด next อีกที
    onPageChange.mockClear();
    rerender(
      <Pagination
        {...props}
        currentPage={3}
        totalPages={3}
        onPageChange={onPageChange}
      />
    );

    const buttons2 = container.querySelectorAll("button.page-link");
    const prevBtn2 = buttons2[0];
    const nextBtn2 = buttons2[buttons2.length - 1];

    // กด next หน้า 3 (สุดท้าย) ไม่ควรยิง onPageChange
    fireEvent.click(nextBtn2);
    expect(onPageChange).not.toHaveBeenCalled();

    // กด prev หน้า 3 → ควรไปหน้า 2
    fireEvent.click(prevBtn2);
    expect(onPageChange).toHaveBeenCalledTimes(1);
    expect(onPageChange).toHaveBeenCalledWith(2);
  });

  it("เมื่อคลิกหมายเลขหน้า ควรเรียก onPageChange ด้วยหน้าที่เลือก", () => {
    const { onPageChange, container } = setup({
      currentPage: 2,
      totalPages: 5,
      totalRecords: 50,
    });

    // ใช้ container ของเทสต์นี้เอง ไม่ใช้ screen (กันชนกับเทสต์อื่น)
    const pageButtons = Array.from(
      container.querySelectorAll("button.page-link")
    );
    const page4Button = pageButtons.find((btn) => btn.textContent === "4");

    expect(page4Button).not.toBeUndefined();

    fireEvent.click(page4Button);

    expect(onPageChange).toHaveBeenCalledTimes(1);
    expect(onPageChange).toHaveBeenCalledWith(4);
  });

  it("เมื่อเปลี่ยน page size จาก dropdown ควรเรียก onPageSizeChange และอัปเดตข้อความ Showing ...", () => {
    const { container, onPageSizeChange } = setup({
      currentPage: 1,
      totalPages: 10,
      totalRecords: 100,
    });

    const select = container.querySelector("select");
    expect(select).not.toBeNull();

    // เปลี่ยนเป็น 20
    fireEvent.change(select, { target: { value: "20" } });
    expect(onPageSizeChange).toHaveBeenCalledTimes(1);
    expect(onPageSizeChange).toHaveBeenCalledWith(20);

    // ใช้ container อย่างเดียวเพื่อไม่ให้ปนกับ DOM จากเทสต์อื่น
    const infoSpan = container.querySelector(".text-center .text-muted.small");
    expect(infoSpan).not.toBeNull();

    const infoText = infoSpan.textContent || "";

    expect(infoText).toContain("Showing");
    expect(infoText).toContain("of 100");
    expect(infoText).toContain("20"); // คาดว่ามี 20 อยู่ในข้อความ (1 to 20)
  });

  it("เมื่อเลือก Custom แล้วกรอกค่าและกด Apply ควรเรียก onPageSizeChange ด้วยค่าที่กรอก", () => {
    const { container, onPageSizeChange } = setup({
      currentPage: 1,
      totalPages: 10,
      totalRecords: 100,
    });

    const select = container.querySelector("select");
    expect(select).not.toBeNull();

    // เลือก "Custom"
    fireEvent.change(select, { target: { value: "custom" } });

    // ควรแสดง input สำหรับ custom page size
    const input = container.querySelector('input[type="number"]');
    expect(input).not.toBeNull();

    // ใส่ค่า 7 แล้วกดปุ่ม Apply (title="Apply")
    fireEvent.change(input, { target: { value: "7" } });

    const applyBtn = screen
      .getAllByRole("button")
      .find((btn) => btn.getAttribute("title") === "Apply");

    expect(applyBtn).not.toBeUndefined();

    fireEvent.click(applyBtn);

    expect(onPageSizeChange).toHaveBeenCalledTimes(1);
    expect(onPageSizeChange).toHaveBeenCalledWith(7);
  });

  it("เมื่อมีจำนวนหน้ามาก ควรแสดงปุ่มหน้าแรก/สุดท้าย และ ... (ellipsis)", () => {
    const { container } = setup({
      currentPage: 10,
      totalPages: 20,
      totalRecords: 400,
    });

    // ควรมีปุ่ม '1' และ '20' ภายใน container นี้
    const buttons = Array.from(
      container.querySelectorAll("button.page-link")
    );
    const page1Btn = buttons.find((btn) => btn.textContent === "1");
    const page20Btn = buttons.find((btn) => btn.textContent === "20");

    expect(page1Btn).not.toBeUndefined();
    expect(page20Btn).not.toBeUndefined();

    // มี ... อย่างน้อย 1 จุด (อาจมี 2 จุด prev, next)
    const ellipsis = Array.from(container.querySelectorAll("span.page-link"))
      .filter((el) => el.textContent === "...");
    expect(ellipsis.length).toBeGreaterThanOrEqual(1);
  });
});
