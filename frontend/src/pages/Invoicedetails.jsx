import React, { useMemo, useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import QRCodeGenerator from "../component/QRCodeGenerator";
import useMessage from "../component/useMessage";
import "../assets/css/tenantmanagement.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import * as bootstrap from "bootstrap";

const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

function InvoiceDetails() {
  const navigate = useNavigate();
  const location = useLocation();
  const { invoice, invoiceId, tenantName } = location.state || {};
  const { showMessageError, showMessageSave } = useMessage();

  const todayISO = () => new Date().toISOString().slice(0, 10);

  // ===== Mock (fallback) =====
  const defaultInvoice = {
    id: 1,
    createDate: "2025-01-31",
    firstName: "John",
    lastName: "Doe",
    nationalId: "1-2345-67890-12-3",
    phoneNumber: "012-345-6789",
    email: "JohnDoe@gmail.com",
    package: "1 Year",
    signDate: "2024-12-30",
    startDate: "2024-12-31",
    endDate: "2025-12-31",
    floor: "1",
    room: "101",
    amount: 5356,
    rent: 4000,
    water: 120,
    waterUnit: 4,
    electricity: 1236,
    electricityUnit: 206,
    status: "pending",
    payDate: "",
    penalty: 0,
    penaltyDate: null,
  };

  const initial = invoice || defaultInvoice;
  const displayName = tenantName || `${initial.firstName} ${initial.lastName}`;

  // ===== Rates (demo) =====
  const RATE_WATER_PER_UNIT = 30;
  const RATE_ELEC_PER_UNIT = 6.5;
  const SERVICE_FEE = 0;
  const ROUND_TO = 2;

  // ===== State สำหรับฟอร์มและการแสดงผล =====
  const [invoiceForm, setInvoiceForm] = useState({
    id: initial.id,
    createDate: initial.createDate,
    floor: initial.floor || "",
    room: initial.room || "",
    rent: Number(initial.rent) || 0,
    waterUnit: Number(initial.waterUnit) || 0,
    electricityUnit: Number(initial.electricityUnit) || 0,
    addonAmount: Number(initial.addonAmount) || 0, // 🔥 เพิ่ม addon amount
    // derived
    water: Number(initial.water) || 0,
    electricity: Number(initial.electricity) || 0,
    amount: Number(initial.amount) || 0,
    status: (initial.status || "pending").toLowerCase(),
    penalty: Number(initial.penalty) || 0,
    penaltyDate: initial.penaltyDate || null,
    payDate: initial.payDate || null,
    // Outstanding Balance fields
    previousBalance: Number(initial.previousBalance) || 0,
    paidAmount: Number(initial.paidAmount) || 0,
    outstandingBalance: Number(initial.outstandingBalance) || 0,
    hasOutstandingBalance: Boolean(initial.hasOutstandingBalance),
  });

  // ===== Fetch ข้อมูลล่าสุดจาก API =====
  useEffect(() => {
    const fetchInvoiceData = async () => {
      if (!invoiceId && !initial.id) return;
      
      try {
        const response = await fetch(`${API_BASE}/invoice/${invoiceId || initial.id}`, {
          credentials: "include",
        });
        
        if (response.ok) {
          const apiData = await response.json();
          console.log("🔍 API Invoice Data:", apiData);
          console.log("🔍 Water Unit from API:", apiData.waterUnit);
          console.log("🔍 Electricity Unit from API:", apiData.electricityUnit);
          
            // ✅ คำนวณ NET amount ที่ถูกต้องเหมือน Invoice Management
            const rentAmount = Number(apiData.rent) || 0;
            const waterAmount = Number(apiData.water) || 0;
            const electricityAmount = Number(apiData.electricity) || 0;
            const addonAmount = Number(apiData.addonAmount) || 0; // 🔥 เพิ่ม addon amount
            const penaltyAmount = Number(apiData.penaltyTotal) || 0;
            const correctNetAmount = rentAmount + waterAmount + electricityAmount + addonAmount + penaltyAmount;
            const paidAmount = Number(apiData.paidAmount) || 0;
            
            // ✅ ใช้การคำนวณ Outstanding แบบเดียวกับ Invoice Management (ทบยอดจากเดือนก่อน)
            const correctOutstanding = Number(apiData.outstandingBalance ?? 0) > 0 ? 
              // ถ้า backend มีการทบยอดแล้ว ให้ปรับตามส่วนต่างของ NET ที่ถูกต้อง
              Number(apiData.outstandingBalance) + (correctNetAmount - (apiData.netAmount ?? apiData.amount ?? 0)) :
              // ถ้าไม่มีการทบยอด ใช้ NET ลบยอดที่จ่าย  
              correctNetAmount - paidAmount;
            
            console.log("🔍 Invoice Details - Corrected Calculation:", {
              components: { rent: rentAmount, water: waterAmount, electricity: electricityAmount, addon: addonAmount, penalty: penaltyAmount },
              correctNetAmount,
              paidAmount,
              backendOutstanding: Number(apiData.outstandingBalance),
              correctOutstanding,
              useCumulativeOutstanding: Number(apiData.outstandingBalance ?? 0) > 0,
              difference: correctNetAmount - (apiData.netAmount ?? apiData.amount ?? 0)
            });
          
          const updateData = {
            rent: rentAmount,
            water: waterAmount,
            electricity: electricityAmount,
            addonAmount: addonAmount, // 🔥 เพิ่ม addon amount
            // ใช้ค่าจาก backend ถ้ามี ไม่ใช้ fallback ที่อาจผิด
            waterUnit: apiData.waterUnit !== undefined ? Number(apiData.waterUnit) : initial.waterUnit,
            electricityUnit: apiData.electricityUnit !== undefined ? Number(apiData.electricityUnit) : initial.electricityUnit,
            amount: correctNetAmount, // ✅ ใช้ค่า NET ที่คำนวณถูกต้อง
            penalty: penaltyAmount,
            status: (apiData.invoiceStatus === 1 ? "complete" : 
                    apiData.invoiceStatus === 2 ? "cancelled" : "pending"),
            createDate: apiData.createDate ? d2str(apiData.createDate) : initial.createDate, // 🔥 เพิ่ม createDate
            payDate: apiData.payDate ? d2str(apiData.payDate) : initial.payDate,
            penaltyDate: apiData.penaltyAppliedAt ? d2str(apiData.penaltyAppliedAt) : initial.penaltyDate,
            // Outstanding Balance fields - ใช้ค่าที่คำนวณถูกต้อง ✅
            previousBalance: Number(apiData.previousBalance) || 0,
            paidAmount: paidAmount,
            outstandingBalance: correctOutstanding, // ✅ ใช้ค่าที่คำนวณถูกต้อง
            hasOutstandingBalance: correctOutstanding > 0,
          };
          
          console.log("🔍 Update Data:", updateData);
          
          // อัปเดตฟอร์ม
          setInvoiceForm(prev => ({
            ...prev,
            ...updateData
          }));
        }
      } catch (error) {
        console.error("Failed to fetch invoice data:", error);
      }
    };

    fetchInvoiceData();
  }, [invoiceId, initial.id]);

  // ✅ Cleanup เมื่อ component unmount เพื่อป้องกันปัญหา modal ค้าง
  useEffect(() => {
    return () => {
      // ✅ ทำ cleanup เมื่อออกจากหน้า
      cleanupBackdrops();
    };
  }, []);

  // ===== helpers =====
  const toNumber = (v) => {
    const n = Number(v);
    return Number.isFinite(n) && n >= 0 ? n : 0;
    };
  const round = (v, d = ROUND_TO) =>
    Number(Math.round((v + Number.EPSILON) * 10 ** d) / 10 ** d);

  const parseISO = (s) => (s ? new Date(s + "T00:00:00") : null);

  const diffDays = (fromISO, toISO) => {
    const a = parseISO(fromISO);
    const b = parseISO(toISO);
    if (!a || !b) return null;
    const ms = b.getTime() - a.getTime();
    return Math.floor(ms / (1000 * 60 * 60 * 24));
  };

  const mapStatusToCode = (s) => {
    const v = (s || "").toLowerCase();
    if (v === "complete") return 1;
    if (v === "cancelled") return 2;
    return 0; // pending, overdue, incomplete → 0
  };

  // ✅ ฟังก์ชันเปิด modal พร้อม cleanup ก่อน
  const handleOpenEditModal = () => {
    console.log("🎯 Opening edit modal...");
    
    // ✅ Cleanup ก่อนเปิด modal ใหม่เพื่อป้องกัน conflict
    cleanupBackdrops();
    
    // ✅ รอ cleanup เสร็จแล้วค่อยเปิด modal
    setTimeout(() => {
      const modalElement = document.getElementById("editRequestModal");
      if (modalElement) {
        const modal = new bootstrap.Modal(modalElement, {
          backdrop: 'static', // ป้องกันปิดโดยการคลิกข้างนอก
          keyboard: false // ป้องกันปิดด้วย ESC
        });
        modal.show();
        console.log("✅ Edit modal opened");
      }
    }, 50);
  };

  const d2str = (v) => {
    if (!v) return "";
    const s = String(v);
    return s.length >= 10 ? s.slice(0, 10) : s;
  };

  // ✅ คำนวณ water และ electricity bill อัตโนมัติเมื่อ unit เปลี่ยน
  useEffect(() => {
    // ✅ คำนวณใหม่เสมอเมื่อ units เปลี่ยน (ให้ผู้ใช้ควบคุมได้)
    const waterBill = round(toNumber(invoiceForm.waterUnit) * RATE_WATER_PER_UNIT);
    const elecBill = round(toNumber(invoiceForm.electricityUnit) * RATE_ELEC_PER_UNIT);
    
    const rent = toNumber(invoiceForm.rent);
    const addonAmount = toNumber(invoiceForm.addonAmount); // 🔥 เพิ่ม addon amount
    const penalty = toNumber(invoiceForm.penalty);
    
    const subtotal = round(rent + waterBill + elecBill + addonAmount);
    const net = subtotal + penalty;

    // 🔍 Debug log เพื่อดูการคำนวณใน Invoice Details
    console.log(`🔍 Invoice Details #${invoiceForm.id} - Calculation:`, {
      rent,
      waterUnit: invoiceForm.waterUnit,
      waterBill,
      electricityUnit: invoiceForm.electricityUnit,
      elecBill,
      addonAmount, // 🔥 เพิ่ม addon ใน log
      penalty,
      subtotal,
      finalNet: net,
      rates: { water: RATE_WATER_PER_UNIT, electricity: RATE_ELEC_PER_UNIT },
      source: 'calculated_from_units'
    });

    // ✅ อัพเดทค่าใหม่เสมอเมื่อ units เปลี่ยน (แต่ใช้ setTimeout เพื่อป้องกัน race condition)
    const timeoutId = setTimeout(() => {
      setInvoiceForm((p) => ({
        ...p,
        water: waterBill,
        electricity: elecBill,
        amount: net, // ✅ net รวม addon แล้วจากการคำนวณข้างบน
      }));
    }, 10); // รอ 10ms เพื่อให้ state update เสร็จ

    return () => clearTimeout(timeoutId); // cleanup timeout
  }, [
    invoiceForm.waterUnit,
    invoiceForm.electricityUnit,
    invoiceForm.rent,
    invoiceForm.penalty,
  ]);

  //============= cleanupBackdrops =============//
  const cleanupBackdrops = () => {
    console.log("🧹 Starting modal cleanup...");
    
    // ✅ Force remove all modal backdrops
    const backdrops = document.querySelectorAll(".modal-backdrop, .modal-backdrop.fade, .modal-backdrop.show");
    backdrops.forEach((backdrop, index) => {
      console.log(`Removing backdrop ${index + 1}:`, backdrop);
      backdrop.remove();
    });
    
    // ✅ Force reset body styles
    document.body.classList.remove("modal-open");
    document.body.style.overflow = "";
    document.body.style.paddingRight = "";
    document.body.style.removeProperty("padding-right");
    document.body.style.removeProperty("overflow");
    
    // ✅ Reset html styles
    document.documentElement.style.overflow = "";
    document.documentElement.style.removeProperty("overflow");
    
    // ✅ Force hide any open modals
    const modals = document.querySelectorAll(".modal.show, .modal.fade.show");
    modals.forEach((modal, index) => {
      console.log(`Force hiding modal ${index + 1}:`, modal);
      modal.style.display = "none";
      modal.classList.remove("show");
      modal.setAttribute("aria-hidden", "true");
      modal.removeAttribute("aria-modal");
      modal.removeAttribute("role");
    });
    
    console.log("✅ Modal cleanup completed");
  };

  //============= handleSave (PUT /invoice/update/{id}) =============//
  const handleSave = async (e) => {
    e.preventDefault();
    console.log("🔧 handleSave called, current form:", invoiceForm);

    // ✅ คำนวณค่า bill จาก unit ที่ผู้ใช้ป้อนล่าสุด (เพื่อป้องกัน race condition)
    const currentWaterUnit = Number(invoiceForm.waterUnit) || 0;
    const currentElectricityUnit = Number(invoiceForm.electricityUnit) || 0;
    const currentAddonAmount = Number(invoiceForm.addonAmount) || 0; // 🔥 เพิ่ม addon
    const waterBill = round(currentWaterUnit * RATE_WATER_PER_UNIT);
    const elecBill = round(currentElectricityUnit * RATE_ELEC_PER_UNIT);
    const rent = toNumber(invoiceForm.rent);
    const penalty = toNumber(invoiceForm.penalty);
    
    // ✅ อัปเดต state ก่อน save เพื่อให้ UI แสดงค่าที่ถูกต้อง
    setInvoiceForm(prev => ({
      ...prev,
      water: waterBill,
      electricity: elecBill,
      amount: rent + waterBill + elecBill + currentAddonAmount + penalty
    }));
    
    // แปลงค่าเป็น Integer ตาม DTO backend
    const subTotalInt = Math.round(rent + waterBill + elecBill + currentAddonAmount);
    const penaltyInt = Math.round(penalty);
    const netInt = Math.round(subTotalInt + penalty);

    console.log("💡 Final calculation before save:", {
      waterUnit: currentWaterUnit,
      electricityUnit: currentElectricityUnit,
      waterBill,
      elecBill,
      currentAddonAmount, // 🔥 เพิ่ม addon ใน log
      rent,
      penalty,
      subTotalInt,
      netInt
    });

    const payload = {
      // ✅ ส่งข้อมูล unit ไปด้วยเพื่อให้ backend อัปเดต
      waterUnit: currentWaterUnit,
      electricityUnit: currentElectricityUnit,
      waterRate: RATE_WATER_PER_UNIT,
      electricityRate: RATE_ELEC_PER_UNIT,
      // dueDate: (ไม่มี UI ก็ไม่ส่ง)
      createDate: invoiceForm.createDate ? `${invoiceForm.createDate}T00:00:00` : null, // 🔥 เพิ่ม createDate
      invoiceStatus: mapStatusToCode(invoiceForm.status),
      payDate: invoiceForm.payDate ? `${invoiceForm.payDate}T00:00:00` : null,
      payMethod: null, // ยังไม่มีให้เลือกใน UI นี้ จะเว้นไว้
      subTotal: subTotalInt,
      penaltyTotal: penaltyInt,
      netAmount: netInt,
      penaltyAppliedAt: invoiceForm.penaltyDate
        ? `${invoiceForm.penaltyDate}T00:00:00`
        : null,
      // notes: มีใน DTO แต่ entity ยังไม่มี — ไม่จำเป็นต้องส่ง
    };

    console.log("🚀 Sending payload:", payload);
    console.log("🔍 Current invoiceForm units:", { 
      waterUnit: invoiceForm.waterUnit, 
      electricityUnit: invoiceForm.electricityUnit 
    });

    try {
      const res = await fetch(
        `${API_BASE}/invoice/update/${invoiceId || invoiceForm.id}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify(payload),
        }
      );

      console.log("📡 Response status:", res.status, res.ok);

      if (!res.ok) {
        const t = await res.text().catch(() => "");
        console.error("❌ Response error:", t);
        throw new Error(t || `HTTP ${res.status}`);
      }

      // ใช้ค่าที่ backend คำนวณกลับมา (ถ้าต้องการ)
      const updated = await res.json();
      console.log("✅ Updated data from backend:", updated);

      // อัปเดต invoiceForm หลัง Save สำเร็จ - ใช้ข้อมูลที่คำนวณแล้ว + ข้อมูลจาก backend
      const updatedFormData = {
        id: updated.id ?? invoiceForm.id,
        createDate: d2str(updated.createDate) || invoiceForm.createDate,
        rent: Number(updated.rent ?? rent) || invoiceForm.rent,
        waterUnit: updated.waterUnit !== undefined ? Number(updated.waterUnit) : currentWaterUnit,
        electricityUnit: updated.electricityUnit !== undefined ? Number(updated.electricityUnit) : currentElectricityUnit,
        water: Number(updated.water ?? waterBill) || waterBill,
        electricity: Number(updated.electricity ?? elecBill) || elecBill,
        addonAmount: Number(updated.addonAmount ?? currentAddonAmount) || currentAddonAmount, // 🔥 เพิ่ม addon amount
        amount: netInt, // 🔥 ใช้ค่าที่คำนวณเองแทนค่าจาก backend
        status: (updated.status ?? updated.statusText ?? invoiceForm.status).toLowerCase(),
        penalty: Number(updated.penaltyTotal ?? penalty) || penalty,
        penaltyDate: d2str(updated.penaltyAppliedAt) || invoiceForm.penaltyDate,
        payDate: d2str(updated.payDate) || invoiceForm.payDate,
        // Outstanding Balance fields ถ้ามี
        previousBalance: Number(updated.previousBalance) || invoiceForm.previousBalance,
        paidAmount: Number(updated.paidAmount) || invoiceForm.paidAmount,
        outstandingBalance: Number(updated.outstandingBalance) || invoiceForm.outstandingBalance,
        hasOutstandingBalance: Boolean(updated.hasOutstandingBalance) || invoiceForm.hasOutstandingBalance,
      };

      console.log("✅ Immediate update after save:", updatedFormData);

      setInvoiceForm(prev => ({
        ...prev,
        ...updatedFormData
      }));

      // ✅ Force close modal และ cleanup อย่างสมบูรณ์
      console.log("🔄 Starting modal close process...");
      
      // ✅ ก่อนอื่นให้ cleanup ก่อน
      cleanupBackdrops();
      
      const modalElement = document.getElementById("editRequestModal");
      if (modalElement) {
        console.log("🎯 Found modal element:", modalElement);
        
        try {
          const modalInstance = bootstrap.Modal.getInstance(modalElement);
          if (modalInstance) {
            console.log("🔧 Found modal instance, disposing...");
            modalInstance.dispose();
          }
        } catch (error) {
          console.warn("Warning disposing modal instance:", error);
        }
        
        // ✅ Force hide modal element
        modalElement.style.display = "none";
        modalElement.classList.remove("show");
        modalElement.setAttribute("aria-hidden", "true");
        modalElement.removeAttribute("aria-modal");
        modalElement.removeAttribute("role");
        
        console.log("✅ Modal element force hidden");
      }
      
      // ✅ Final cleanup หลังจากทุกอย่าง
      setTimeout(() => {
        cleanupBackdrops();
        console.log("✅ Final cleanup completed");
      }, 100);

      // แสดงข้อความสำเร็จ
      showMessageSave();
      
    } catch (err) {
      console.error("Save failed:", err);
      showMessageError(`Update failed: ${err.message}`);
    }
  };

  // ===== Status style helper =====
  const statusBadge = useMemo(() => {
    const s = (invoiceForm.status || "").toLowerCase();
    if (s === "complete") return "bg-success";
    if (s === "overdue") return "bg-danger";
    if (s === "pending") return "bg-warning text-dark";
    return "bg-secondary";
  }, [invoiceForm.status]);

  const handleStatusChange = (value) => {
    const v = String(value).toLowerCase();
    setInvoiceForm((p) => ({
      ...p,
      status: v,
      payDate: v === "complete" ? todayISO() : null,
    }));
  };

  return (
    <Layout title="Invoice Details" icon="bi bi-currency-dollar" notifications={3}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          <div className="col-lg-11 p-4">
            {/* Toolbar */}
            <div className="toolbar-wrapper card border-0 bg-white">
              <div className="card-header bg-white border-0 rounded-2">
                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                  <div className="d-flex align-items-center gap-2">
                    <span
                      className="breadcrumb-link text-primary"
                      style={{ cursor: "pointer" }}
                      onClick={() => navigate("/invoicemanagement")}
                    >
                      Invoice Management
                    </span>
                    <span className="text-muted">›</span>
                    <span className="breadcrumb-current">{displayName}</span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={handleOpenEditModal}
                    >
                      <i className="bi bi-pencil me-1"></i> Edit Invoice
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Details */}
            <div className="table-wrapper-detail rounded-0">
              <div className="row g-4">
                {/* Left column */}
                <div className="col-lg-6">
                  <div className="card border-0 shadow-sm mb-3 rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Room Information</h5>
                      <p><span className="label">Floor:</span> <span className="value">{invoiceForm.floor}</span></p>
                      <p><span className="label">Room:</span> <span className="value">{invoiceForm.room}</span></p>
                    </div>
                  </div>

                  <div className="card border-0 shadow-sm rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Tenant Information</h5>
                      <p><span className="label">First Name:</span> <span className="value">{initial.firstName}</span></p>
                      <p><span className="label">Last Name:</span> <span className="value">{initial.lastName}</span></p>
                      <p><span className="label">National ID:</span> <span className="value">{initial.nationalId}</span></p>
                      <p><span className="label">Phone Number:</span> <span className="value">{initial.phoneNumber}</span></p>
                      <p><span className="label">Email:</span> <span className="value">{initial.email}</span></p>
                      <p>
                        <span className="label">Package:</span>{" "}
                        <span className="value">
                          <span className="package-badge badge bg-primary">{initial.package}</span>
                        </span>
                      </p>
                      <p><span className="label">Sign date:</span> <span className="value">{initial.signDate}</span></p>
                      <p><span className="label">Start date:</span> <span className="value">{initial.startDate}</span></p>
                      <p><span className="label">End date:</span> <span className="value">{initial.endDate}</span></p>
                    </div>
                  </div>
                </div>

                {/* Right column */}
                <div className="col-lg-6">
                  <div className="card border-0 shadow-sm mb-3 rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Invoice Information</h5>
                      <div className="row">
                        <div className="col-6">
                          <p><span className="label">Create date:</span> <span className="value">{invoiceForm.createDate}</span></p>
                          <p><span className="label">Water unit:</span> <span className="value">{invoiceForm.waterUnit}</span></p>
                          <p><span className="label">Electricity unit:</span> <span className="value">{invoiceForm.electricityUnit}</span></p>
                          <p><span className="label">Pay date:</span> <span className="value">{invoiceForm.payDate || "-"}</span></p>
                        </div>
                        <div className="col-6">
                          <p><span className="label">Rent:</span> <span className="value">{Math.round(invoiceForm.rent).toLocaleString()}</span></p>
                          <p><span className="label">Water bill:</span> <span className="value">{Math.round(invoiceForm.water).toLocaleString()}</span></p>
                          <p><span className="label">Electricity bill:</span> <span className="value">{Math.round(invoiceForm.electricity).toLocaleString()}</span></p>
                          <p><span className="label">Add-on fee:</span> <span className="value">{Math.round(invoiceForm.addonAmount ?? 0).toLocaleString()}</span></p>
                          <p><span className="label">Penalty:</span> <span className={`value ${invoiceForm.penalty > 0 ? 'text-danger fw-bold' : ''}`}>
                             {Math.round(invoiceForm.penalty).toLocaleString()} THB
                             {invoiceForm.penalty > 0 && <small className="text-muted"> (10%)</small>}
                           </span></p>
                          <p><span className="label">Penalty date:</span> <span className="value">{invoiceForm.penaltyDate || "-"}</span></p>
                          <p><span className="label">NET:</span> <span className="value fw-bold text-primary">{Math.round(invoiceForm.amount).toLocaleString()} THB</span></p>
                        </div>
                      </div>
                      <div className="row mt-2">
                        <div className="col-12">
                          <p>
                            <span className="label">Status:</span>{" "}
                            <span className="value">
                              <span className={`badge ${statusBadge}`}>
                                <i className="bi bi-circle-fill me-1"></i>{invoiceForm.status}
                              </span>
                            </span>
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="card border-0 shadow-sm rounded-2 mt-3">
                    <div className="card-body">
                      <h5 className="card-title">
                        <i className="bi bi-credit-card me-2"></i>
                        Outstanding Balance Information
                      </h5>
                      <div className="row">
                        <div className="col-6">
                          <p><span className="label">Previous Balance:</span> 
                             <span className="value">{invoiceForm.previousBalance.toLocaleString()} THB</span></p>
                          <p><span className="label">Paid Amount:</span> 
                             <span className="value text-success">{Math.round(invoiceForm.paidAmount).toLocaleString()} THB</span></p>
                        </div>
                        <div className="col-6">
                          <p><span className="label">Outstanding Balance:</span> 
                             <span className={`value fw-bold ${invoiceForm.hasOutstandingBalance ? 'text-danger' : 'text-success'}`}>
                               {Math.round(invoiceForm.outstandingBalance).toLocaleString()} THB
                             </span></p>
                          <p><span className="label">Outstanding Status:</span> 
                             <span className="value">
                               {invoiceForm.hasOutstandingBalance ? (
                                 <span className="badge bg-danger">
                                   <i className="bi bi-exclamation-triangle me-1"></i>
                                   Outstanding {Math.round(invoiceForm.outstandingBalance).toLocaleString()} THB
                                 </span>
                               ) : (
                                 <span className="badge bg-success">
                                   <i className="bi bi-check-circle me-1"></i>
                                   Fully Paid
                                 </span>
                               )}
                             </span></p>
                        </div>
                      </div>
                      {invoiceForm.hasOutstandingBalance && (
                        <div className="alert alert-warning mt-2 mb-0">
                          <i className="bi bi-info-circle me-2"></i>
                          <small>
                            This outstanding balance will be included in the next month's invoice. If not paid within the deadline, a 10% penalty will apply.
                          </small>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Payment Information */}
                  <div className="card border-0 shadow-sm rounded-2 mt-3">
                    <div className="card-body">
                      <h5 className="card-title">
                        <i className="bi bi-bank me-2"></i>
                        Bank Transfer Information
                      </h5>
                      <div className="row">
                        <div className="col-md-6">
                          <h6 className="text-primary mb-3">
                            <i className="bi bi-building me-1"></i>
                            Bangkok Bank
                          </h6>
                          <p><span className="label">Account Name:</span> <span className="value">OrganicNow Property Management</span></p>
                          <p><span className="label">Account Number:</span> <span className="value fw-bold">123-4-56789-0</span></p>
                          <p><span className="label">Branch:</span> <span className="value">Central Plaza Branch</span></p>
                          <p><span className="label">SWIFT Code:</span> <span className="value">BKKBTHBK</span></p>
                        </div>
                        <div className="col-md-6">
                          <div className="text-center">
                            <h6 className="text-primary mb-3">
                              <i className="bi bi-qr-code me-1"></i>
                              QR Code Payment
                            </h6>
                            <div className="qr-code-container p-3 border rounded-3 bg-light d-flex justify-content-center">
                              <QRCodeGenerator 
                                value={`https://promptpay.io/0123456789/${invoiceForm.amount}.00`}
                                size={150}
                                className="qr-code-payment"
                                errorMessage="QR Code unavailable"
                              />
                            </div>
                            <small className="text-muted d-block mt-2">
                              <strong>Amount:</strong> {Math.round(invoiceForm.amount).toLocaleString()} THB<br />
                              Scan with PromptPay compatible banking apps
                            </small>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* PromptPay Information */}
                  <div className="card border-0 shadow-sm rounded-2 mt-3">
                    <div className="card-body">
                      <h5 className="card-title">
                        <i className="bi bi-phone me-2"></i>
                        PromptPay Information
                      </h5>
                      <div className="row">
                        <div className="col-md-6">
                          <p><span className="label">PromptPay ID:</span> <span className="value fw-bold">0123456789</span></p>
                          <p><span className="label">Account Name:</span> <span className="value">OrganicNow Property Management</span></p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                {/* /Right */}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ===== Modal Edit ===== */}
      <Modal
        id="editRequestModal"
        title="Edit Invoice"
        icon="bi bi-pencil"
        size="modal-lg"
        scrollable="modal-dialog-scrollable"
      >
        <form onSubmit={handleSave}>
          {/* Room */}
          <div className="row g-3 align-items-start">
            <div className="col-md-3"><strong>Room Information</strong></div>
            <div className="col-md-9">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Floor</label>
                  <select
                    className="form-select"
                    value={invoiceForm.floor}
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, floor: e.target.value }))}
                    disabled
                    title="Floor is locked in edit modal"
                  >
                    <option value="" hidden>Select Floor</option>
                    <option>1</option><option>2</option><option>3</option>
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label">Room</label>
                  <select
                    className="form-select"
                    value={invoiceForm.room}
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, room: e.target.value }))}
                    disabled
                    title="Room is locked in edit modal"
                  >
                    <option value="" hidden>Select Room</option>
                    <option>101</option><option>205</option><option>301</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <hr className="my-4" />

          {/* Invoice */}
          <div className="row g-3 align-items-start">
            <div className="col-md-3"><strong>Invoice Information</strong></div>
            <div className="col-md-9">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Create date</label>
                  <input 
                    type="date" 
                    className="form-control" 
                    value={invoiceForm.createDate} 
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, createDate: e.target.value }))}
                    title="แก้ไขวันที่สร้างใบแจ้งหนี้เพื่อทดสอบระบบ Outstanding Balance"
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Rent (from package)</label>
                  <input
                    type="text"
                    className="form-control"
                    value={`฿${invoiceForm.rent.toLocaleString()}`}
                    disabled
                    title="Rent is fixed based on package"
                  />
                  
                </div>

                <div className="col-md-6">
                  <label className="form-label">Water unit</label>
                  <input
                    type="number"
                    min={0}
                    className="form-control"
                    value={invoiceForm.waterUnit}
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, waterUnit: toNumber(e.target.value) }))}
                  />
                  <div className="form-text">
                    Rate: {RATE_WATER_PER_UNIT.toLocaleString()} / unit
                  </div>
                </div>
                <div className="col-md-6">
                  <label className="form-label">Water bill</label>
                  <input type="text" className="form-control" value={invoiceForm.water.toLocaleString()} disabled />
                </div>

                <div className="col-md-6">
                  <label className="form-label">Electricity unit</label>
                  <input
                    type="number"
                    min={0}
                    className="form-control"
                    value={invoiceForm.electricityUnit}
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, electricityUnit: toNumber(e.target.value) }))}
                  />
                  <div className="form-text">
                    Rate: {RATE_ELEC_PER_UNIT.toLocaleString()} / unit
                  </div>
                </div>
                <div className="col-md-6">
                  <label className="form-label">Electricity bill</label>
                  <input type="text" className="form-control" value={invoiceForm.electricity.toLocaleString()} disabled />
                </div>

                <div className="col-md-6">
                  <label className="form-label">NET</label>
                  <input type="text" className="form-control" value={Math.round(invoiceForm.amount).toLocaleString()} disabled />
                </div>

                <div className="col-md-6">
                  <label className="form-label">Status</label>
                  <select
                    className="form-select"
                    value={invoiceForm.status} // 'complete' | 'pending' | 'overdue'
                    onChange={(e) => handleStatusChange(e.target.value)}
                  >
                    <option value="complete">Complete</option>
                    <option value="pending">Pending</option>
                    {/* <option value="overdue">Overdue</option> */}
                  </select>
                </div>

                <div className="col-md-6">
                  <label className="form-label">Pay date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={invoiceForm.payDate || ""}
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, payDate: e.target.value || null }))}
                    title="แก้ไขวันที่ชำระเพื่อทดสอบ penalty system"
                  />
                </div>
              </div>
            </div>
          </div>

          <hr className="my-4" />

          {/* Outstanding Balance Information */}
          <div className="row g-3 align-items-start">
            <div className="col-md-3"><strong>Outstanding Balance Information</strong></div>
            <div className="col-md-9">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Previous Balance</label>
                  <input
                    type="text"
                    className="form-control"
                    value={`${invoiceForm.previousBalance.toLocaleString()} THB`}
                    disabled
                    title="Previous month's outstanding balance (read-only)"
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Paid Amount</label>
                  <input
                    type="text"
                    className="form-control text-success"
                    value={`${Math.round(invoiceForm.paidAmount).toLocaleString()} THB`}
                    disabled
                    title="Amount already paid (read-only)"
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Outstanding Balance</label>
                  <input
                    type="text"
                    className={`form-control fw-bold ${invoiceForm.hasOutstandingBalance ? 'text-danger' : 'text-success'}`}
                    value={`${invoiceForm.outstandingBalance.toLocaleString()} THB`}
                    disabled
                    title="Remaining outstanding amount (read-only)"
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Outstanding Status</label>
                  <div className="form-control d-flex align-items-center" style={{ minHeight: '38px' }}>
                    {invoiceForm.hasOutstandingBalance ? (
                      <span className="badge bg-danger">
                        <i className="bi bi-exclamation-triangle me-1"></i>
                        Outstanding
                      </span>
                    ) : (
                      <span className="badge bg-success">
                        <i className="bi bi-check-circle me-1"></i>
                        No Outstanding
                      </span>
                    )}
                  </div>
                </div>
              </div>
              {/* {invoiceForm.hasOutstandingBalance && (
                <div className="alert alert-info mt-3 mb-0">
                  <i className="bi bi-info-circle me-2"></i>
                  <small>
                    <strong>Note:</strong> Outstanding balance information is automatically calculated by the system and cannot be edited on this page. 
                    To manage payments, please use the "Payment Management" function on the Invoice Management page.
                  </small>
                </div>
              )} */}
            </div>
          </div>

          <hr className="my-4" />

          {/* Penalty Information */}
          <div className="row g-3 align-items-start">
            <div className="col-md-3"><strong>Penalty Information</strong></div>
            <div className="col-md-9">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Penalty (auto, 10% of NET)</label>
                  <input
                    type="text"
                    className="form-control"
                    value={invoiceForm.penalty.toLocaleString()}
                    disabled
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Penalty date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={invoiceForm.penaltyDate || ""}
                    onChange={(e) => setInvoiceForm((p) => ({ ...p, penaltyDate: e.target.value || null }))}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="d-flex justify-content-center gap-3 pt-4 pb-2">
            <button type="button" className="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" className="btn btn-primary">Save</button>
          </div>
        </form>
      </Modal>
    </Layout>
  );
}

export default InvoiceDetails;
