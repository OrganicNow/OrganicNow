import React, { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import useMessage from "../component/useMessage";
import { pageSize as defaultPageSize } from "../config_variable";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

function InvoiceManagement() {
  const navigate = useNavigate();
  const { showMessageError, showMessageSave, showMessageConfirmDelete, showMessageAdjust } = useMessage();

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [pageSize, setPageSize] = useState(defaultPageSize);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState("");

  // ✅ สถานะกำลังลบใบแจ้งหนี้ (เพื่อ disable ปุ่ม/โชว์ spinner)
  const [deletingId, setDeletingId] = useState(null);

  // ===== CSV Import States =====
  const [showCsvModal, setShowCsvModal] = useState(false);
  const [csvFile, setCsvFile] = useState(null);
  const [csvUploading, setCsvUploading] = useState(false);
  const [csvResult, setCsvResult] = useState("");

  // ===== Payment Management States =====
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [paymentRecords, setPaymentRecords] = useState([]);
  const [paymentForm, setPaymentForm] = useState({
    paymentAmount: '',
    paymentMethod: 'BANK_TRANSFER',
    paymentDate: new Date().toISOString().slice(0, 16),
    transactionReference: '',
    notes: '',
    recordedBy: 'admin'
  });
  const [paymentMethods, setPaymentMethods] = useState({});
  const [paymentStatuses, setPaymentStatuses] = useState({});
  const [loadingPayments, setLoadingPayments] = useState(false);
  const [savingPayment, setSavingPayment] = useState(false);

  // ===== File Upload States =====
  const [selectedFile, setSelectedFile] = useState(null);
  const [proofType, setProofType] = useState('BANK_SLIP');
  const [proofDescription, setProofDescription] = useState('');
  const [uploadingProof, setUploadingProof] = useState(false);

  // ====== DATA จาก Backend ======
  const [data, setData] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [contracts, setContracts] = useState([]);
  const [tenants, setTenants] = useState([]);
  const [packages, setPackages] = useState([]);

  // สำหรับ dropdown ห้อง (ใช้เฉพาะห้องที่มีผู้เช่าอยู่จริง)
  const roomsByFloor = useMemo(() => {
    if (!rooms || rooms.length === 0) {
      return {};
    }

    const result = {};
    
    rooms.forEach((room, index) => {
      // ใช้ field names ที่ถูกต้องจาก API response จริง
      const floor = room.roomFloor;  // field จริงจาก API
      const roomNumber = room.roomNumber;  // field จริงจาก API
      const status = room.status;  // สถานะห้อง
      
      // ✅ เพิ่มเงื่อนไข: แสดงเฉพาะห้องที่มีผู้เช่าอยู่ (status = 'occupied')
      if (floor !== undefined && floor !== null && 
          roomNumber !== undefined && roomNumber !== null && 
          status === 'occupied') {
        const floorStr = String(floor);
        const roomStr = String(roomNumber);
        if (!result[floorStr]) result[floorStr] = [];
        result[floorStr].push(roomStr);
      }
    });
    
    return result;
  }, [rooms]);



  // helper: LocalDate/LocalDateTime -> YYYY-MM-DD
  const d2str = (v) => {
    if (!v) return "";
    const s = String(v);
    if (s.length >= 10) return s.slice(0, 10);
    try {
      return new Date(s).toISOString().slice(0, 10);
    } catch {
      return s;
    }
  };

  // map backend InvoiceDto -> row ใช้ในตาราง
  const mapDto = (it) => ({
    id: it.id,
    createDate: d2str(it.createDate),
    firstName: it.firstName ?? "",
    lastName: it.lastName ?? "",
    nationalId: it.nationalId ?? "",
    phoneNumber: it.phoneNumber ?? "",
    email: it.email ?? "",
    package: it.packageName ?? "",

    signDate: d2str(it.signDate),
    startDate: d2str(it.startDate),
    endDate: d2str(it.endDate),

    floor: it.floor ?? "",
    room: it.room ?? "",

    amount: Number(it.amount ?? it.netAmount ?? 0),
    rent: Number(it.rent ?? 0),
    water: Number(it.water ?? 0),
    waterUnit: Number(it.waterUnit ?? 0),
    electricity: Number(it.electricity ?? 0),
    electricityUnit: Number(it.electricityUnit ?? 0),

    status: (it.status ?? it.statusText ?? "").trim() || "Unknown",
    payDate: d2str(it.payDate),
    penalty: Number(it.penalty ?? ((it.penaltyTotal ?? 0) > 0 ? 1 : 0)),
    penaltyDate: d2str(it.penaltyAppliedAt),
  });

  useEffect(() => {
    fetchData();
    fetchRooms();
    fetchContracts();
    fetchTenants();
    fetchPackages();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ✅ Refresh ข้อมูลเมื่อ page กลับมา visible (เช่น จาก tenant management)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        // หน้าจอ visible แล้ว - refresh ข้อมูล
        fetchRooms();
        fetchContracts();
        fetchTenants();
        fetchData(); // รวมถึง invoice list ด้วย
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    // Cleanup
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      setErr("");
      const res = await fetch(`${API_BASE}/invoice/list`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json(); // List<InvoiceDto>
      const rows = Array.isArray(json) ? json.map(mapDto) : [];
      setData(rows);
      setTotalRecords(rows.length);
      setTotalPages(Math.max(1, Math.ceil(rows.length / pageSize)));
      setCurrentPage(1);
    } catch (e) {
      setErr("Failed to load invoices.");
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  // ✅ ดึงข้อมูลห้องจาก backend
  const fetchRooms = async () => {
    try {
      const res = await fetch(`${API_BASE}/room/list`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (res.ok) {
        const json = await res.json();
        if (Array.isArray(json) && json.length > 0) {
          setRooms(json);
        } else {
          setRooms([]);
        }
      } else {
        // ใช้ fallback หาก API ล้มเหลว
        setRooms([]);
      }
    } catch (e) {
      console.error("Failed to fetch rooms:", e);
      // ใช้ fallback หาก API ล้มเหลว  
      setRooms([]);
    }
  };

  // ✅ ดึงข้อมูล contract จาก backend
  const fetchContracts = async () => {
    try {
      const res = await fetch(`${API_BASE}/contracts`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (res.ok) {
        const json = await res.json();
        setContracts(Array.isArray(json) ? json : []);
      } else {
        setContracts([]);
      }
    } catch (e) {
      console.error("Failed to fetch contracts:", e);
      setContracts([]);
    }
  };

  // ✅ ดึงข้อมูล tenant จาก backend
  const fetchTenants = async () => {
    try {
      const res = await fetch(`${API_BASE}/tenant/list`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (res.ok) {
        const json = await res.json();
        // tenant/list ส่ง object {results: [...]} ไม่ใช่ array โดยตรง
        const tenantArray = json.results || json;
        setTenants(Array.isArray(tenantArray) ? tenantArray : []);
      } else {
        setTenants([]);
      }
    } catch (e) {
      console.error("Failed to fetch tenants:", e);
      setTenants([]);
    }
  };

  // ✅ ดึงข้อมูล packages จาก backend
  const fetchPackages = async () => {
    try {
      const res = await fetch(`${API_BASE}/packages`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (res.ok) {
        const json = await res.json();
        setPackages(Array.isArray(json) ? json : []);
      } else {
        setPackages([]);
      }
    } catch (e) {
      console.error("Failed to fetch packages:", e);
      setPackages([]);
    }
  };

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  // จะคำนวณเพจใหม่จาก filtered ด้านล่าง
  const [search, setSearch] = useState("");
  const [filters, setFilters] = useState({
    status: "ALL",
    payFrom: "",
    payTo: "",
    floor: "",
    room: "",
    amountMin: "",
    amountMax: "",
  });

  // ===== INVOICE FORM STATE (Modal) =====
  const [invForm, setInvForm] = useState({
    floor: "",
    room: "",
    packageId: "", // แทน contractId
    createDate: new Date().toISOString().slice(0, 10),

    waterUnit: "",
    elecUnit: "",
    waterRate: 30,
    elecRate: 8,

    rent: 0, // จะอัปเดตอัตโนมัติจาก package
    status: "Incomplete",

    waterBill: 0,
    elecBill: 0,
    net: 0,
  });

  const mapStatusToCode = (s) => {
    if (s === "Complete") return 1;
    return 0; // Incomplete => 0
  };

  // สร้างตัวเลือกห้องตามชั้น (ใช้ข้อมูลจาก backend)
  const roomOptions = useMemo(() => {
    if (!invForm.floor || !roomsByFloor[invForm.floor]) return [];
    return roomsByFloor[invForm.floor];
  }, [invForm.floor, roomsByFloor]);

  // Auto-select package when floor and room are selected (เฉพาะห้องที่มีผู้เช่าและ active packages)
  useEffect(() => {
    if (invForm.floor && invForm.room) {
      // ✅ ตรวจสอบว่าห้องที่เลือกมีผู้เช่าอยู่จริง
      const selectedRoom = rooms.find(room => {
        const floorMatch = room.roomFloor === Number(invForm.floor);
        const roomMatch = room.roomNumber === invForm.room;
        return floorMatch && roomMatch;
      });
      
      // ถ้าห้องไม่มีผู้เช่า (status !== 'occupied') ให้รีเซ็ต form
      if (!selectedRoom || selectedRoom.status !== 'occupied') {
        setInvForm((prev) => ({ 
          ...prev, 
          packageId: ""
        }));
        return;
      }
      
      // ✅ ใช้ข้อมูลจาก tenants array ที่มี contract data ครบ
      const tenantData = tenants.find(tenant => {
        const floorMatch = tenant.floor === Number(invForm.floor);
        const roomMatch = tenant.room === invForm.room;
        return floorMatch && roomMatch;
      });
      
      if (tenantData && tenantData.packageId) {
        // ✅ ตรวจสอบว่า package ยัง active อยู่หรือไม่
        const packageData = packages.find(pkg => pkg.id === tenantData.packageId);
        if (packageData && (packageData.is_active === 1 || packageData.is_active === true)) {
          setInvForm((prev) => ({ 
            ...prev, 
            packageId: tenantData.packageId.toString()
          }));
          return;
        }
      }
      
      // Fallback: try to find from rooms (ใช้ field names ที่ถูกต้องจาก API)
      const roomData = rooms.find(room => {
        const floorMatch = room.roomFloor === Number(invForm.floor);
        const roomMatch = room.roomNumber === invForm.room;
        return floorMatch && roomMatch;
      });
      
      if (roomData && roomData.packageId) {
        // ✅ ตรวจสอบว่า package ยัง active อยู่หรือไม่
        const packageData = packages.find(pkg => pkg.id === roomData.packageId);
        if (packageData && (packageData.is_active === 1 || packageData.is_active === true)) {
          setInvForm((prev) => ({ 
            ...prev, 
            packageId: roomData.packageId.toString()
          }));
          return;
        }
      }
      
      setInvForm((prev) => ({ 
        ...prev, 
        packageId: ""
      }));
    } else {
      setInvForm((prev) => ({ 
        ...prev, 
        packageId: ""
      }));
    }
  }, [invForm.floor, invForm.room, rooms, tenants, packages]);

  // ถ้าเปลี่ยนชั้นแล้วห้องเดิมไม่อยู่ในตัวเลือก ให้รีเซ็ตห้อง
  useEffect(() => {
    if (!roomOptions.includes(invForm.room)) {
      setInvForm((prev) => ({ ...prev, room: "", packageId: "" }));
    }
  }, [invForm.floor, roomOptions]); // eslint-disable-line

  // ✅ Update rent when package changes (เฉพาะ active packages)
  useEffect(() => {
    if (invForm.packageId && packages.length > 0) {
      const selectedPackage = packages.find(p => 
        p.id === Number(invForm.packageId) && 
        (p.is_active === 1 || p.is_active === true)
      );
      if (selectedPackage) {
        // ใช้ field 'price' แทน 'rent' ตาม DTO structure
        setInvForm((prev) => ({ ...prev, rent: selectedPackage.price || 0 }));
      } else {
        // ถ้า package ไม่ active แล้ว ให้ reset
        setInvForm((prev) => ({ ...prev, packageId: "", rent: 0 }));
      }
    } else {
      setInvForm((prev) => ({ ...prev, rent: 0 }));
    }
  }, [invForm.packageId, packages]);

  const clearFilters = () =>
    setFilters({
      status: "ALL",
      payFrom: "",
      payTo: "",
      floor: "",
      room: "",
      amountMin: "",
      amountMax: "",
    });

  // คำนวณบิลอัตโนมัติ
  useEffect(() => {
    const wUnit = Number(invForm.waterUnit) || 0;
    const eUnit = Number(invForm.elecUnit) || 0;
    const wRate = Number(invForm.waterRate) || 0;
    const eRate = Number(invForm.elecRate) || 0;
    const rent = Number(invForm.rent) || 0;

    const waterBill = wUnit * wRate;
    const elecBill = eUnit * eRate;
    const net = rent + waterBill + elecBill;

    setInvForm((p) => ({ ...p, waterBill, elecBill, net }));
  }, [invForm.waterUnit, invForm.elecUnit, invForm.waterRate, invForm.elecRate, invForm.rent]);

  // ====== FILTERED VIEW ======
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    let rows = [...data];

    rows = rows.filter((r) => {
      // ✅ ใช้ status จาก backend เท่านั้น: Complete, Incomplete
      if (filters.status !== "ALL" && r.status !== filters.status) return false;
      
      if (filters.payFrom && r.payDate && r.payDate < filters.payFrom) return false;
      if (filters.payTo && r.payDate && r.payDate > filters.payTo) return false;
      if (filters.floor && String(r.floor) !== String(filters.floor)) return false;
      if (filters.room && String(r.room) !== String(filters.room)) return false;
      if (filters.amountMin !== "" && r.amount < Number(filters.amountMin)) return false;
      if (filters.amountMax !== "" && r.amount > Number(filters.amountMax)) return false;
      return true;
    });

    if (q) {
      rows = rows.filter(
        (r) =>
          `${r.firstName} ${r.lastName}`.toLowerCase().includes(q) ||
          String(r.room).includes(q) ||
          String(r.floor).includes(q) ||
          (r.createDate ?? "").includes(q) ||
          (r.status ?? "").toLowerCase().includes(q)
      );
    }

    return rows;
  }, [data, filters, search]);

  // ====== PAGINATION ======
  useEffect(() => {
    const newTotalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
    setTotalPages(newTotalPages);
    setTotalRecords(filtered.length);
    if (currentPage > newTotalPages) setCurrentPage(1);
  }, [filtered, pageSize]); // eslint-disable-line

  const handlePageSizeChange = (size) => {
    const n = Number(size) || defaultPageSize;
    const newTotalPages = Math.max(1, Math.ceil(filtered.length / n));
    setPageSize(n);
    setTotalPages(newTotalPages);
    setCurrentPage(1);
  };

  const pageStart = (currentPage - 1) * pageSize;
  const pageEnd = pageStart + pageSize;
  const pageRows = filtered.slice(pageStart, pageEnd);

  // ====== ACTIONS ======
  const [selectedItems, setSelectedItems] = useState([]);
  const [bulkDownloading, setBulkDownloading] = useState(false);
  const [bulkDeleting, setBulkDeleting] = useState(false);

  // ✅ ดาวน์โหลด PDF ใบแจ้งหนี้
  const handleDownloadPdf = async (invoice) => {
    try {
      setErr("");
      
      // แสดง loading สำหรับ PDF นั้น ๆ (optional)
      // showWarning(`กำลังสร้างไฟล์ PDF สำหรับใบแจ้งหนี้ ${invoice.id}...`);
      
      // ✅ ใช้วิธีสร้าง mock PDF แทน เนื่องจาก backend ยังไม่มี PDF endpoint
      showMessageSave(`กำลังสร้าง PDF สำหรับใบแจ้งหนี้ #${invoice.id}...`);
      
      // Mock PDF content - สร้าง HTML แทน PDF ชั่วคราว
      const printContent = `
        <html>
          <head>
            <title>Invoice ${invoice.id}</title>
            <style>
              body { font-family: Arial, sans-serif; padding: 20px; }
              .header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }
              .invoice-info { margin-bottom: 20px; }
              .invoice-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
              .invoice-table th, .invoice-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }
              .invoice-table th { background-color: #f8f9fa; }
              .total-row { background-color: #e9ecef; font-weight: bold; }
            </style>
          </head>
          <body>
            <div class="header">
              <h1>ใบแจ้งหนี้</h1>
              <h2>Invoice #${invoice.id}</h2>
              <p>วันที่: ${invoice.createDate}</p>
            </div>
            <div class="invoice-info">
              <h3>ข้อมูลลูกค้า</h3>
              <p><strong>ชื่อ:</strong> ${invoice.firstName} ${invoice.lastName}</p>
              <p><strong>ห้อง:</strong> ชั้น ${invoice.floor} ห้อง ${invoice.room}</p>
            </div>
            <table class="invoice-table">
              <thead>
                <tr><th>รายการ</th><th>จำนวนเงิน (บาท)</th></tr>
              </thead>
              <tbody>
                <tr><td>ค่าเช่า</td><td>${invoice.rent?.toLocaleString()}</td></tr>
                <tr><td>ค่าน้ำ</td><td>${invoice.water?.toLocaleString()}</td></tr>
                <tr><td>ค่าไฟ</td><td>${invoice.electricity?.toLocaleString()}</td></tr>
                <tr class="total-row"><td><strong>รวมทั้งสิ้น</strong></td><td><strong>${invoice.amount?.toLocaleString()} บาท</strong></td></tr>
              </tbody>
            </table>
          </body>
        </html>
      `;

      // สร้าง HTML file และ download
      const blob = new Blob([printContent], { type: 'text/html' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
      // ตั้งชื่อไฟล์ตามข้อมูล Invoice
      const fileName = `Invoice_${invoice.id}_${invoice.firstName}_${invoice.lastName}_Room_${invoice.room}.pdf`;
      link.download = fileName;
      
      // Trigger download
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      // Cleanup
      window.URL.revokeObjectURL(url);
      
      showMessageSave();
      
    } catch (error) {
      console.error('PDF Download Error:', error);
      setErr(`ดาวน์โหลด PDF ล้มเหลว: ${error.message}`);
      showMessageError(`ดาวน์โหลด PDF ล้มเหลว: ${error.message}`);
    }
  };

  // ✅ ดาวน์โหลด PDF หลายใบพร้อมกัน
  const handleBulkDownloadPdf = async () => {
    if (selectedItems.length === 0) {
      showMessageError("กรุณาเลือกใบแจ้งหนี้ที่ต้องการดาวน์โหลด");
      return;
    }

    setBulkDownloading(true);
    let successCount = 0;
    let errorCount = 0;

    try {
      showMessageSave(`กำลังดาวน์โหลด PDF ${selectedItems.length} ใบ...`);

      for (const invoiceId of selectedItems) {
        const invoice = pageRows.find(item => item.id === invoiceId);
        if (!invoice) continue;

        try {
          const response = await fetch(`${API_BASE}/invoice/pdf/${invoice.id}`, {
            method: 'GET',
            credentials: 'include',
          });

          if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `Invoice_${invoice.id}_${invoice.firstName}_${invoice.lastName}.pdf`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
            successCount++;
          } else {
            console.error(`Failed to download PDF for invoice ${invoice.id}`);
            errorCount++;
          }
        } catch (error) {
          console.error(`Error downloading PDF for invoice ${invoice.id}:`, error);
          errorCount++;
        }

        // หน่วงเวลาเล็กน้อยเพื่อไม่ให้ request มากเกินไป
        await new Promise(resolve => setTimeout(resolve, 500));
      }

      if (successCount > 0) {
        showMessageSave(`ดาวน์โหลด PDF สำเร็จ ${successCount} ใบ${errorCount > 0 ? `, ไม่สำเร็จ ${errorCount} ใบ` : ''}`);
      } else {
        showMessageError("ไม่สามารถดาวน์โหลด PDF ได้");
      }

      // เคลียร์การเลือก
      setSelectedItems([]);

    } catch (error) {
      console.error('Bulk download error:', error);
      showMessageError("เกิดข้อผิดพลาดในการดาวน์โหลด PDF");
    } finally {
      setBulkDownloading(false);
    }
  };

  // ✅ ลบใบแจ้งหนี้หลายรายการพร้อมกัน
  const handleBulkDelete = async () => {
    if (selectedItems.length === 0) {
      showMessageError("กรุณาเลือกใบแจ้งหนี้ที่ต้องการลบ");
      return;
    }

    const confirmed = await showMessageConfirmDelete(
      `คุณต้องการลบใบแจ้งหนี้ ${selectedItems.length} รายการ ใช่หรือไม่?`,
      "การลบจะไม่สามารถกู้คืนได้"
    );

    if (!confirmed) return;

    setBulkDeleting(true);
    let successCount = 0;
    let errorCount = 0;

    try {
      for (const invoiceId of selectedItems) {
        try {
          const response = await fetch(`${API_BASE}/invoice/delete/${invoiceId}`, {
            method: 'DELETE',
            credentials: 'include',
          });

          if (response.ok) {
            successCount++;
          } else {
            console.error(`Failed to delete invoice ${invoiceId}`);
            errorCount++;
          }
        } catch (error) {
          console.error(`Error deleting invoice ${invoiceId}:`, error);
          errorCount++;
        }
      }

      if (successCount > 0) {
        showMessageSave(`ลบใบแจ้งหนี้สำเร็จ ${successCount} รายการ${errorCount > 0 ? `, ไม่สำเร็จ ${errorCount} รายการ` : ''}`);
        fetchData(); // รีเฟรชข้อมูล
      } else {
        showMessageError("ไม่สามารถลบใบแจ้งหนี้ได้");
      }

      // เคลียร์การเลือก
      setSelectedItems([]);

    } catch (error) {
      console.error('Bulk delete error:', error);
      showMessageError("เกิดข้อผิดพลาดในการลบใบแจ้งหนี้");
    } finally {
      setBulkDeleting(false);
    }
  };

  const handleUpdate = (item) => {
    // Update functionality - placeholder for future implementation
    console.log('Update functionality not yet implemented for:', item);
  };

  // ===== Payment Management Functions =====

  // เปิด Payment Management Modal
  const handlePaymentManagement = async (invoice) => {
    setSelectedInvoice(invoice);
    setShowPaymentModal(true);
    await loadPaymentRecords(invoice.id);
    await loadPaymentMethods();
  };

  // โหลดข้อมูล Payment Records
  const loadPaymentRecords = async (invoiceId) => {
    try {
      setLoadingPayments(true);
      const response = await fetch(`${API_BASE}/api/payments/records/invoice/${invoiceId}`);
      if (response.ok) {
        const data = await response.json();
        setPaymentRecords(data);
      } else {
        console.error('Failed to load payment records');
        setPaymentRecords([]);
      }
    } catch (error) {
      console.error('Error loading payment records:', error);
      setPaymentRecords([]);
    } finally {
      setLoadingPayments(false);
    }
  };

  // โหลดข้อมูล Payment Methods
  const loadPaymentMethods = async () => {
    try {
      const [methodsResponse, statusesResponse] = await Promise.all([
        fetch(`${API_BASE}/api/payments/payment-methods`).catch(() => null),
        fetch(`${API_BASE}/api/payments/payment-statuses`).catch(() => null)
      ]);

      if (methodsResponse?.ok) {
        const methods = await methodsResponse.json();
        setPaymentMethods(methods);
      } else {
        // Fallback payment methods
        setPaymentMethods({
          'CASH': 'เงินสด',
          'BANK_TRANSFER': 'โอนเงิน',
          'PROMPTPAY': 'พร้อมเพย์',
          'CREDIT_CARD': 'บัตรเครดิต'
        });
      }

      if (statusesResponse?.ok) {
        const statuses = await statusesResponse.json();
        setPaymentStatuses(statuses);
      } else {
        // Fallback payment statuses
        setPaymentStatuses({
          'PENDING': 'รอยืนยัน',
          'CONFIRMED': 'ยืนยันแล้ว',
          'REJECTED': 'ปฏิเสธ'
        });
      }
    } catch (error) {
      console.error('Error loading payment methods:', error);
      // Set fallback values when error occurs
      setPaymentMethods({
        'CASH': 'เงินสด',
        'BANK_TRANSFER': 'โอนเงิน',
        'PROMPTPAY': 'พร้อมเพย์',
        'CREDIT_CARD': 'บัตรเครดิต'
      });
      setPaymentStatuses({
        'PENDING': 'รอยืนยัน',
        'CONFIRMED': 'ยืนยันแล้ว',
        'REJECTED': 'ปฏิเสธ'
      });
    }
  };

  // เพิ่มการบันทึกการชำระเงิน
  const handleAddPayment = async (e) => {
    e.preventDefault();
    
    if (!selectedInvoice) {
      showMessageError('ไม่พบข้อมูลใบแจ้งหนี้');
      return;
    }
    
    // Validate form data
    if (!paymentForm.paymentAmount || parseFloat(paymentForm.paymentAmount) <= 0) {
      showMessageError('กรุณาระบุจำนวนเงินที่ถูกต้อง');
      return;
    }
    
    try {
      setSavingPayment(true);
      
      const paymentData = {
        invoiceId: selectedInvoice.id,
        paymentAmount: parseFloat(paymentForm.paymentAmount),
        paymentMethod: paymentForm.paymentMethod,
        paymentDate: new Date(paymentForm.paymentDate).toISOString(),
        transactionReference: paymentForm.transactionReference,
        notes: paymentForm.notes,
        recordedBy: paymentForm.recordedBy || 'admin'
      };

      const response = await fetch(`${API_BASE}/api/payments/records`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(paymentData)
      });

      if (response.ok) {
        showMessageSave();
        
        // รีเซ็ตฟอร์ม
        setPaymentForm({
          paymentAmount: '',
          paymentMethod: 'BANK_TRANSFER',
          paymentDate: new Date().toISOString().slice(0, 16),
          transactionReference: '',
          notes: '',
          recordedBy: 'admin'
        });
        
        // โหลดข้อมูลใหม่
        await loadPaymentRecords(selectedInvoice.id);
        await fetchData(); // อัปเดตตาราง Invoice
        
      } else {
        const errorText = await response.text().catch(() => 'Unknown error');
        throw new Error(errorText || `HTTP ${response.status}`);
      }
      
    } catch (error) {
      console.error('Error adding payment:', error);
      showMessageError(`เพิ่มการบันทึกการชำระเงินล้มเหลว: ${error.message}`);
    } finally {
      setSavingPayment(false);
    }
  };

  // ดาวน์โหลดหลักฐานการชำระเงิน
  const handleViewProof = async (proofId, fileName) => {
    try {
      const response = await fetch(`${API_BASE}/api/payments/proofs/${proofId}/download`, {
        method: 'GET'
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        
        // สร้าง element สำหรับดาวน์โหลด
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName || `หลักฐาน_${proofId}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // ทำความสะอาด URL
        window.URL.revokeObjectURL(url);
        
        showMessageSave();
      } else {
        showMessageError('ไม่สามารถดาวน์โหลดหลักฐานได้');
      }
    } catch (error) {
      console.error('Error downloading proof:', error);
      showMessageError('เกิดข้อผิดพลาดในการดาวน์โหลดหลักฐาน');
    }
  };

  // อัปโหลดหลักฐานการชำระเงิน
  const handleUploadProof = async () => {
    if (!selectedFile) {
      showMessageError('กรุณาเลือกไฟล์');
      return;
    }

    if (!selectedInvoice) {
      showMessageError('ไม่พบข้อมูลใบแจ้งหนี้');
      return;
    }

    // ตรวจสอบขนาดไฟล์ (ไม่เกิน 5MB)
    if (selectedFile.size > 5 * 1024 * 1024) {
      showMessageError('ขนาดไฟล์ไม่ควรเกิน 5MB');
      return;
    }

    // ตรวจสอบประเภทไฟล์
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
    if (!allowedTypes.includes(selectedFile.type)) {
      showMessageError('รองรับเฉพาะไฟล์ JPG, PNG, GIF และ PDF');
      return;
    }

    try {
      setUploadingProof(true);
      
      // ถ้าไม่มี payment records ให้เพิ่มก่อน
      if (!paymentRecords.length) {
        showMessageError('กรุณาเพิ่มการบันทึกการชำระเงินก่อนอัปโหลดหลักฐาน');
        return;
      }

      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('proofType', proofType);
      formData.append('description', proofDescription || 'หลักฐานการชำระเงิน');
      formData.append('uploadedBy', 'admin');

      // ใช้ payment record ล่าสุด
      const latestPaymentId = paymentRecords[0]?.id;
      if (!latestPaymentId) {
        throw new Error('ไม่พบการบันทึกการชำระเงิน');
      }

      const response = await fetch(`${API_BASE}/api/payments/records/${latestPaymentId}/proofs`, {
        method: 'POST',
        body: formData
      });

      if (response.ok) {
        showMessageSave();
        
        // รีเซ็ต form
        setSelectedFile(null);
        setProofType('BANK_SLIP');
        setProofDescription('');
        
        // Clear file input
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput) fileInput.value = '';
        
        // โหลดข้อมูลใหม่
        await loadPaymentRecords(selectedInvoice.id);
        
      } else {
        const errorText = await response.text().catch(() => 'Unknown error');
        console.error('Upload failed:', errorText);
        throw new Error(errorText || `HTTP ${response.status}`);
      }
      
    } catch (error) {
      console.error('Error uploading proof:', error);
      showMessageError(`อัปโหลดหลักฐานล้มเหลว: ${error.message}`);
    } finally {
      setUploadingProof(false);
    }
  };

  // อัปเดตสถานะการชำระ
  const handleUpdatePaymentStatus = async (paymentId, newStatus) => {
    try {
      const response = await fetch(`${API_BASE}/api/payments/records/${paymentId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          paymentStatus: newStatus
        })
      });

      if (response.ok) {
        showMessageSave();
        await loadPaymentRecords(selectedInvoice.id);
        await fetchData(); // อัปเดตตาราง Invoice
      } else {
        throw new Error('Failed to update payment status');
      }
    } catch (error) {
      console.error('Error updating payment status:', error);
      showMessageError(`อัปเดตสถานะการชำระเงินล้มเหลว: ${error.message}`);
    }
  };

  // ลบการบันทึกการชำระเงิน
  const handleDeletePayment = async (paymentId) => {
    const result = await showMessageConfirmDelete('payment record');
    if (!result.isConfirmed) return;
    
    try {
      const response = await fetch(`${API_BASE}/api/payments/records/${paymentId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        showMessageSave();
        await loadPaymentRecords(selectedInvoice.id);
        await fetchData(); // อัปเดตตาราง Invoice
      } else {
        throw new Error('Failed to delete payment record');
      }
    } catch (error) {
      console.error('Error deleting payment:', error);
      showMessageError(`ลบการบันทึกการชำระเงินล้มเหลว: ${error.message}`);
    }
  };

  // ✅ ลบใบแจ้งหนี้ (DELETE /invoice/delete/{id})
  const handleDelete = async (id) => {
    const result = await showMessageConfirmDelete(`ใบแจ้งหนี้ #${id}`);
    if (!result.isConfirmed) return;

    try {
      setDeletingId(id);
      setErr("");

      const res = await fetch(`${API_BASE}/invoice/delete/${id}`, {
        method: "DELETE",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => "");
        throw new Error(msg || `ลบไม่สำเร็จ (HTTP ${res.status})`);
      }

      // ลบสำเร็จ → ตัดแถวออกจาก state
      setData((prev) => prev.filter((x) => x.id !== id));
      showMessageSave();
    } catch (e) {
      console.error(e);
      setErr(e.message || "ลบไม่สำเร็จ");
      showMessageError(`ลบ Invoice ล้มเหลว: ${e.message}`);
    } finally {
      setDeletingId(null);
    }
  };

  const handleViewInvoice = (invoice) => {
    navigate("/InvoiceDetails", {
      state: {
        invoice: invoice,
        invoiceId: invoice.id,
        tenantName: `${invoice.firstName} ${invoice.lastName}`,
      },
    });
  };

  const handleSelectRow = (invoiceId) => {
    console.log('🔍 Selecting invoice:', invoiceId);
    setSelectedItems((prev) => {
      const newSelection = prev.includes(invoiceId) 
        ? prev.filter((i) => i !== invoiceId) 
        : [...prev, invoiceId];
      console.log('🔍 New selection:', newSelection);
      return newSelection;
    });
  };

  const handleSelectAll = () => {
    if (selectedItems.length === pageRows.length) {
      setSelectedItems([]);
    } else {
      setSelectedItems(pageRows.map((item) => item.id));
    }
  };

  const isAllSelected = pageRows.length > 0 && selectedItems.length === pageRows.length;

  // ====== CREATE (POST /invoice/create) ======
  const createInvoice = async () => {
    try {
      setSaving(true);
      setErr("");

      // ตรวจสอบฟิลด์ที่จำเป็น
      if (!invForm.floor || !invForm.room || !invForm.packageId) {
        throw new Error("Please select Floor, Room, and Package");
      }

      // ✅ ตรวจสอบว่า package ที่เลือกยัง active อยู่หรือไม่
      const selectedPackage = packages.find(p => 
        p.id === Number(invForm.packageId) && 
        (p.is_active === 1 || p.is_active === true)
      );
      
      if (!selectedPackage) {
        throw new Error("Selected package is not available or has been deactivated. Please select another package.");
      }

      const body = {
        packageId: Number(invForm.packageId),
        floor: invForm.floor,
        room: invForm.room,
        createDate: invForm.createDate, // YYYY-MM-DD
        rentAmount: Number(invForm.rent || 0),
        waterUnit: Number(invForm.waterUnit || 0),
        waterRate: Number(invForm.waterRate || 0),
        electricityUnit: Number(invForm.elecUnit || 0),
        electricityRate: Number(invForm.elecRate || 0),
        penaltyTotal: 0,
        invoiceStatus: mapStatusToCode(invForm.status),
        // subTotal / netAmount: ให้ backend คำนวณเอง
      };

      const res = await fetch(`${API_BASE}/invoice/create`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const t = await res.text().catch(() => "");
        console.error("❌ Backend error:", t);
        throw new Error(t || `HTTP ${res.status}`);
      }

      const result = await res.json();

      // เพิ่มข้อมูลใหม่เข้า state โดยตรง (optimistic update)
      const newInvoice = {
        id: result.id,
        createDate: invForm.createDate,
        firstName: "New", // placeholder
        lastName: "Invoice", // placeholder  
        floor: result.floor || parseInt(invForm.floor),
        room: result.room || invForm.room,
        rent: result.rent || parseInt(invForm.rent),
        water: result.water || parseInt(invForm.waterUnit) * parseInt(invForm.waterRate),
        electricity: result.electricity || parseInt(invForm.elecUnit) * parseInt(invForm.elecRate),
        amount: result.netAmount || 0,
        status: invForm.status || "Incomplete",
        payDate: null,
        penalty: 0,
        penaltyDate: null
      };
      
      // เพิ่มแถวใหม่เข้าไปในตาราง
      setData(prevData => [newInvoice, ...prevData]);
      
      // รอ backend เซฟข้อมูลเสร็จก่อนค่อย refresh
      await new Promise(resolve => setTimeout(resolve, 500));
      
      await fetchData(); // refresh list เพื่อดูข้อมูลจริงจาก database
      
      showMessageSave();
      return true;
    } catch (e) {
      console.error(e);
      setErr(`Create invoice failed: ${e.message}`);
      showMessageError(`สร้าง Invoice ล้มเหลว: ${e.message}`);
      return false;
    } finally {
      setSaving(false);
    }
  };

  // ===== CSV Import Functions =====
  
  const handleCsvFileChange = (e) => {
    const file = e.target.files[0];
    setCsvFile(file);
    setCsvResult("");
  };

  const handleCsvImport = async () => {
    if (!csvFile) {
      showMessageError("Please select a CSV file first");
      return;
    }

    if (!csvFile.name.toLowerCase().endsWith('.csv')) {
      showMessageError("Please select a valid CSV file");
      return;
    }

    setCsvUploading(true);
    setCsvResult("");

    try {
      const formData = new FormData();
      formData.append('file', csvFile);

      const response = await fetch(`${API_BASE}/invoice/import-csv`, {
        method: 'POST',
        body: formData
      });

      if (response.ok) {
        const result = await response.text();
        setCsvResult(result);
        showMessageSave();
        
        // Refresh the invoice list
        setTimeout(() => {
          fetchData();
        }, 1000);
      } else {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to import CSV');
      }
    } catch (error) {
      console.error('CSV Import Error:', error);
      showMessageError(`Failed to import CSV: ${error.message}`);
      setCsvResult(`Error: ${error.message}`);
    } finally {
      setCsvUploading(false);
    }
  };

  const closeCsvModal = () => {
    setShowCsvModal(false);
    setCsvFile(null);
    setCsvResult("");
  };

  return (
    <Layout title="Invoice Management" icon="bi bi-currency-dollar" notifications={3}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          {/* Main */}
          <div className="col-lg-11 p-4">
            {/* Toolbar Card */}
            <div className="toolbar-wrapper card border-0 bg-white">
              <div className="card-header bg-white border-0 rounded-3">
                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                  {/* Left cluster: Filter / Sort / Search */}
                  <div className="d-flex align-items-center gap-3">
                    <button
                      className="btn btn-link tm-link p-0"
                      data-bs-toggle="offcanvas"
                      data-bs-target="#invoiceFilterCanvas"
                    >
                      <i className="bi bi-filter me-1"></i> Filter
                    </button>

                    <button className="btn btn-link tm-link p-0">
                      <i className="bi bi-arrow-down-up me-1"></i> Sort
                    </button>

                    <div className="input-group tm-search">
                      <span className="input-group-text bg-white border-end-0">
                        <i className="bi bi-search"></i>
                      </span>
                      <input
                        type="text"
                        className="form-control border-start-0"
                        placeholder="Search invoices..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                      />
                    </div>
                  </div>

                  {/* แสดงปุ่มจัดการหลายรายการเมื่อมีการเลือก */}
                  {selectedItems.length > 0 && (
                    <div className="d-flex align-items-center gap-2 me-3">
                      <span className="badge bg-primary">{selectedItems.length} รายการที่เลือก</span>
                      <button
                        type="button"
                        className="btn btn-outline-success btn-sm"
                        onClick={handleBulkDownloadPdf}
                        disabled={bulkDownloading}
                        title={`ดาวน์โหลด PDF ${selectedItems.length} ใบ`}
                      >
                        {bulkDownloading ? (
                          <>
                            <span className="spinner-border spinner-border-sm me-1"></span>
                            ดาวน์โหลด...
                          </>
                        ) : (
                          <>
                            <i className="bi bi-file-earmark-pdf-fill me-1"></i>
                            ดาวน์โหลด PDF ({selectedItems.length})
                          </>
                        )}
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline-danger btn-sm"
                        onClick={handleBulkDelete}
                        disabled={bulkDeleting}
                        title={`ลบ ${selectedItems.length} รายการ`}
                      >
                        {bulkDeleting ? (
                          <>
                            <span className="spinner-border spinner-border-sm me-1"></span>
                            ลบ...
                          </>
                        ) : (
                          <>
                            <i className="bi bi-trash-fill me-1"></i>
                            ลบ ({selectedItems.length})
                          </>
                        )}
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline-secondary btn-sm"
                        onClick={() => setSelectedItems([])}
                        title="ยกเลิกการเลือก"
                      >
                        <i className="bi bi-x-circle me-1"></i>
                        ยกเลิก
                      </button>
                    </div>
                  )}

                  {/* Right cluster: Create / Refresh */}
                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-outline-primary btn-sm"
                      onClick={() => {
                        fetchRooms();
                        fetchContracts();
                        fetchTenants();
                        fetchData();
                      }}
                      title="รีเฟรชข้อมูล"
                    >
                      <i className="bi bi-arrow-clockwise me-1"></i> Refresh
                    </button>
                    
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#createInvoiceModal"
                      disabled={Object.keys(roomsByFloor).length === 0}
                      onClick={() => {
                        // ✅ Refresh packages data เมื่อเปิด modal
                        fetchPackages();
                      }}
                      title={Object.keys(roomsByFloor).length === 0 ? "No occupied rooms available for invoice creation" : "Create new invoice"}
                    >
                      <i className="bi bi-plus-lg me-1"></i> Create Invoice
                    </button>
                    
                    {/* CSV Import Button */}
                    <button
                      type="button"
                      className="btn btn-success"
                      onClick={() => setShowCsvModal(true)}
                      title="Import utility usage from CSV file"
                    >
                      <i className="bi bi-file-earmark-spreadsheet me-1"></i> Import CSV
                    </button>
                    {/* <button className="btn btn-outline-secondary" onClick={fetchData} disabled={loading}>
                      <i className={`bi ${loading ? "bi-arrow-repeat spin" : "bi-arrow-repeat"} me-1`}></i>
                      Refresh
                    </button> */}
                  </div>
                </div>
              </div>
            </div>

            {/* Errors */}
            {err && (
              <div className="alert alert-danger mt-3" role="alert">
                {err}
              </div>
            )}

            {/* Warning when no occupied rooms */}
            {Object.keys(roomsByFloor).length === 0 && !loading && (
              <div className="alert alert-warning mt-3" role="alert">
                <i className="bi bi-exclamation-triangle-fill me-2"></i>
                <strong>No occupied rooms available:</strong> Invoices can only be created for rooms with active tenants. 
                Please ensure there are tenants with active contracts before creating invoices.
              </div>
            )}

            {/* Data Table */}
            <div className="table-wrapper">
              <table className="table text-nowrap">
                <thead>
                  <tr>
                    <th className="text-center header-color" style={{ width: '40px', padding: '8px' }}>
                      <input 
                        type="checkbox" 
                        checked={isAllSelected} 
                        onChange={handleSelectAll}
                        style={{ transform: 'scale(1.1)' }}
                      />
                    </th>
                    <th className="text-center align-middle header-color">Order</th>
                    <th className="text-center align-middle header-color">Create date</th>
                    <th className="text-start align-middle header-color">First Name</th>
                    <th className="text-start align-middle header-color">Floor</th>
                    <th className="text-start align-middle header-color">Room</th>
                    <th className="text-start align-middle header-color">Rent</th>
                    <th className="text-start align-middle header-color">Water</th>
                    <th className="text-start align-middle header-color">Electricity</th>
                    <th className="text-start align-middle header-color">NET</th>
                    <th className="text-start align-middle header-color">Status</th>
                    <th className="text-start align-middle header-color">Pay date</th>
                    <th className="text-start align-middle header-color">Penalty</th>
                    <th className="text-center align-middle header-color">Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan="14" className="text-center">
                        Loading...
                      </td>
                    </tr>
                  ) : pageRows.length > 0 ? (
                    pageRows.map((item, idx) => (
                      <tr key={`${item.id}-${idx}`}>
                        <td className="align-middle text-center" style={{ width: '40px', padding: '8px' }}>
                          <input
                            type="checkbox"
                            checked={selectedItems.includes(item.id)}
                            onChange={() => handleSelectRow(item.id)}
                            style={{ transform: 'scale(1.1)' }}
                          />
                        </td>
                        <td className="align-middle text-center">
                          {(currentPage - 1) * pageSize + idx + 1}
                        </td>
                        <td className="align-middle text-center">{item.createDate}</td>
                        <td className="align-middle text-start">{item.firstName}</td>
                        <td className="align-middle text-start">{item.floor}</td>
                        <td className="align-middle text-start">{item.room}</td>
                        <td className="align-middle text-start">{item.rent.toLocaleString()}</td>
                        <td className="align-middle text-start">{item.water.toLocaleString()}</td>
                        <td className="align-middle text-start">{item.electricity.toLocaleString()}</td>
                        <td className="align-middle text-start ">{item.amount.toLocaleString()}</td>
                        <td className="align-middle text-start">
                          <span
                            className={`badge ${
                              item.status === "Complete"
                                ? "bg-success"
                                : "bg-warning text-dark"
                            }`}
                          >
                            <i className="bi bi-circle-fill me-1"></i>
                            {item.status === "Complete" ? "Complete" : "Incomplete"}
                          </span>
                        </td>
                        <td className="align-middle text-start">{item.payDate}</td>
                        <td className="align-middle text-center">
                          <i
                            className={`bi bi-circle-fill ${
                              item.penalty > 0 ? "text-danger" : "text-secondary"
                            }`}
                          ></i>
                        </td>
                        <td className="align-middle text-center">
                          <button
                            className="btn btn-sm form-Button-Edit me-1"
                            onClick={() => handleViewInvoice(item)}
                            aria-label="View invoice"
                            title="ดูรายละเอียดใบแจ้งหนี้"
                          >
                            <i className="bi bi-eye-fill"></i>
                          </button>
                          <button
                            className="btn btn-sm btn-success me-1"
                            onClick={() => handlePaymentManagement(item)}
                            aria-label="Manage payments"
                            title="จัดการการชำระเงิน"
                          >
                            <i className="bi bi-credit-card-fill"></i>
                          </button>
                          <button
                            className="btn btn-sm form-Button-Edit me-1"
                            onClick={() => handleDownloadPdf(item)}
                            aria-label="Download PDF"
                            title="ดาวน์โหลด PDF ใบแจ้งหนี้"
                          >
                            <i className="bi bi-file-earmark-pdf-fill"></i>
                          </button>
                          <button
                            className="btn btn-sm form-Button-Del me-1"
                            onClick={() => handleDelete(item.id)}  // ✅ ส่ง id
                            aria-label="Delete invoice"
                            title="ลบใบแจ้งหนี้"
                            disabled={deletingId === item.id || loading} // ✅ กันกดซ้ำ
                          >
                            <i className={`bi ${deletingId === item.id ? "bi-arrow-repeat spin" : "bi-trash-fill"}`}></i>
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="14" className="text-center">
                        No invoices found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
              totalRecords={totalRecords}
              onPageSizeChange={handlePageSizeChange}
            />
          </div>
        </div>
      </div>

      {/* ===== Modal: Create Invoice ===== */}
      <Modal
        id="createInvoiceModal"
        title="Invoice add"
        icon="bi bi-receipt-cutoff"
        size="modal-lg"
        scrollable="modal-dialog-scrollable"
      >
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            const ok = await createInvoice();
            if (ok) {
              // ปิด modal + reset แบบลวก ๆ
              const el = document.getElementById("createInvoiceModal");
              const modal = window.bootstrap?.Modal.getOrCreateInstance(el);
              modal?.hide();
              setInvForm((p) => ({
                ...p,
                packageId: "",
                floor: "",
                room: "",
                waterUnit: "",
                elecUnit: "",
                rent: "",
                waterBill: 0,
                elecBill: 0,
                net: 0,
                status: "Incomplete",
                createDate: new Date().toISOString().slice(0, 10),
              }));
            }
          }}
        >
          {/* ===== Room / Package Info ===== */}
          <div className="row g-3 align-items-start">
            <div className="col-md-3">
              <strong>Room / Package</strong>
            </div>

            <div className="col-md-9">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Floor <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <select
                      className="form-select"
                      value={invForm.floor}
                      onChange={(e) => setInvForm((p) => ({ ...p, floor: e.target.value }))}
                      required
                      style={{ backgroundColor: '#fff', color: '#000' }}
                    >
                      <option value="" hidden>
                        Select Floor
                      </option>
                      {Object.keys(roomsByFloor).length === 0 ? (
                        <option value="" disabled style={{ backgroundColor: '#fff', color: '#dc3545' }}>
                          No occupied rooms available - Only occupied rooms can receive invoices
                        </option>
                      ) : (
                        Object.keys(roomsByFloor).sort().map((floor) => (
                          <option key={floor} value={floor} style={{ backgroundColor: '#fff', color: '#000' }}>
                            Floor {floor}
                          </option>
                        ))
                      )}
                    </select>
                  </div>
                </div>

                <div className="col-md-6">
                  <label className="form-label">Room <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <select
                      className="form-select"
                      value={invForm.room}
                      onChange={(e) => setInvForm((p) => ({ ...p, room: e.target.value }))}
                      disabled={!invForm.floor}
                      required
                      style={{ backgroundColor: '#fff', color: '#000' }}
                    >
                      <option value="" hidden>
                        {invForm.floor ? "Select Room" : "Select Floor first"}
                      </option>
                      {!invForm.floor ? (
                        <option value="" disabled style={{ backgroundColor: '#fff', color: '#6c757d' }}>
                          Please select floor first
                        </option>
                      ) : roomOptions.length === 0 ? (
                        <option value="" disabled style={{ backgroundColor: '#fff', color: '#dc3545' }}>
                          No occupied rooms available on this floor
                        </option>
                      ) : (
                        roomOptions.map((rm) => (
                          <option key={rm} value={rm} style={{ backgroundColor: '#fff', color: '#000' }}>
                            Room {rm}
                          </option>
                        ))
                      )}
                    </select>
                  </div>
                </div>

                <div className="col-md-12">
                  <label className="form-label">
                    Package 
                    {/* <span className="text-muted ms-2">
                      ({packages.filter(pkg => pkg.is_active === 1 || pkg.is_active === true).length} active packages available)
                    </span> */}
                  </label>
                  {invForm.packageId && packages.length > 0 ? (
                    <div className="d-flex align-items-center gap-2">
                      <div className="form-control bg-light" style={{ flex: 1 }}>
                        {(() => {
                          const selectedPackage = packages.find(p => 
                            p.id === Number(invForm.packageId) && 
                            (p.is_active === 1 || p.is_active === true)
                          );
                          if (!selectedPackage) {
                            return (
                              <div className="text-danger">
                                <i className="bi bi-exclamation-triangle me-1"></i>
                                Package not available (may be inactive)
                              </div>
                            );
                          }
                          return selectedPackage ? 
                            `${selectedPackage.contract_name || selectedPackage.name || 'Package'} - ฿${selectedPackage.price ? selectedPackage.price.toLocaleString() : 'N/A'}` :
                            'Loading package...';
                        })()}
                      </div>
                      <button 
                        type="button" 
                        className="btn btn-outline-secondary btn-sm"
                        onClick={() => setInvForm(prev => ({ ...prev, packageId: '' }))}
                      >
                        เปลี่ยน
                      </button>
                    </div>
                  ) : (
                    <select
                      className="form-select"
                      value={invForm.packageId}
                      onChange={(e) => setInvForm((p) => ({ ...p, packageId: e.target.value }))}
                      required
                      style={{ backgroundColor: '#fff', color: '#000' }}
                    >
                      <option value="" hidden>
                        {invForm.floor && invForm.room ? "Select Package" : "Select Floor and Room first"}
                      </option>
                      {/* ✅ กรองเฉพาะ packages ที่ active เท่านั้น */}
                      {packages.filter(pkg => pkg.is_active === 1 || pkg.is_active === true).length === 0 ? (
                        <option value="" disabled style={{ backgroundColor: '#fff', color: '#dc3545' }}>
                          No active packages available - Please activate packages first
                        </option>
                      ) : (
                        packages
                          .filter(pkg => pkg.is_active === 1 || pkg.is_active === true)
                          .sort((a, b) => {
                            // เรียงตาม duration จากน้อยไปมาก (3, 6, 9, 12 เดือน)
                            const durationA = a.duration || 0;
                            const durationB = b.duration || 0;
                            return durationA - durationB;
                          })
                          .map((pkg) => (
                            <option key={pkg.id} value={pkg.id} style={{ backgroundColor: '#fff', color: '#000' }}>
                              {pkg.contract_name || pkg.name || `Package ${pkg.id}`} - ฿{pkg.price ? pkg.price.toLocaleString() : 'N/A'}
                              {pkg.duration && ` (${pkg.duration} เดือน)`}
                            </option>
                          ))
                      )}
                    </select>
                  )}
                </div>
              </div>
            </div>
          </div>

          <hr className="my-4" />

          {/* ===== Invoice Information ===== */}
          <div className="row g-3 align-items-start">
            <div className="col-md-3">
              <strong>Invoice Information</strong>
            </div>

            <div className="col-md-9">
              <div className="row g-3">
                {/* แถว 1: Create date + Rent */}
                <div className="col-md-6">
                  <label className="form-label">Create date</label>
                  <input type="date" className="form-control" value={invForm.createDate} disabled />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Rent (from package)</label>
                  <input
                    type="text"
                    className="form-control"
                    value={`฿${invForm.rent.toLocaleString()}`}
                    disabled
                  />
                  <div className="form-text text-muted">
                    {invForm.packageId && packages.find(p => p.id === Number(invForm.packageId))?.name}
                  </div>
                </div>

                {/* แถว 2: Water */}
                <div className="col-md-6">
                  <label className="form-label">Water unit</label>
                  <input
                    type="number"
                    className="form-control"
                    placeholder="Add Water unit"
                    min={0}
                    value={invForm.waterUnit}
                    onChange={(e) => setInvForm((p) => ({ ...p, waterUnit: e.target.value }))}
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Water bill</label>
                  <input type="text" className="form-control" value={invForm.waterBill.toLocaleString()} disabled />
                </div>

                {/* แถว 3: Electricity */}
                <div className="col-md-6">
                  <label className="form-label">Electricity unit</label>
                  <input
                    type="number"
                    className="form-control"
                    placeholder="Add Electricity unit"
                    min={0}
                    value={invForm.elecUnit}
                    onChange={(e) => setInvForm((p) => ({ ...p, elecUnit: e.target.value }))}
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Electricity bill</label>
                  <input type="text" className="form-control" value={invForm.elecBill.toLocaleString()} disabled />
                </div>

                {/* แถว 4: NET + Status */}
                <div className="col-md-6">
                  <label className="form-label">NET</label>
                  <input type="text" className="form-control" value={invForm.net.toLocaleString()} disabled />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Status</label>
                  <select
                    className="form-select"
                    value={invForm.status}
                    onChange={(e) => setInvForm((p) => ({ ...p, status: e.target.value }))}
                  >
                    <option value="Incomplete">Incomplete (ยังไม่ชำระ)</option>
                    <option value="Complete">Complete (ชำระแล้ว)</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          {/* ===== Footer buttons ===== */}
          <div className="d-flex justify-content-center gap-3 pt-4 pb-2">
            <button type="button" className="btn btn-outline-secondary" data-bs-dismiss="modal">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </Modal>

      {/* ===== Filters Offcanvas ===== */}
      <div
        className="offcanvas offcanvas-end"
        tabIndex="-1"
        id="invoiceFilterCanvas"
        aria-labelledby="invoiceFilterCanvasLabel"
      >
        <div className="offcanvas-header">
          <h5 id="invoiceFilterCanvasLabel" className="mb-0">
            <i className="bi bi-filter me-2"></i>Filters
          </h5>
          <button type="button" className="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>

        <div className="offcanvas-body">
          <div className="row g-3">
            <div className="col-12">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={filters.status}
                onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}
              >
                <option value="ALL">All</option>
                <option value="Complete">Complete (ชำระแล้ว)</option>
                <option value="Incomplete">Incomplete (ยังไม่ชำระ)</option>
              </select>
            </div>

            <div className="col-md-6">
              <label className="form-label">Pay date from</label>
              <input
                type="date"
                className="form-control"
                value={filters.payFrom}
                onChange={(e) => setFilters((f) => ({ ...f, payFrom: e.target.value }))}
              />
            </div>
            <div className="col-md-6">
              <label className="form-label">Pay date to</label>
              <input
                type="date"
                className="form-control"
                value={filters.payTo}
                onChange={(e) => setFilters((f) => ({ ...f, payTo: e.target.value }))}
              />
            </div>

            <div className="col-md-6">
              <label className="form-label">Floor</label>
              <input
                type="text"
                className="form-control"
                value={filters.floor}
                onChange={(e) => setFilters((f) => ({ ...f, floor: e.target.value }))}
                placeholder="e.g. 2"
              />
            </div>
            <div className="col-md-6">
              <label className="form-label">Room</label>
              <input
                type="text"
                className="form-control"
                value={filters.room}
                onChange={(e) => setFilters((f) => ({ ...f, room: e.target.value }))}
                placeholder="e.g. 205"
              />
            </div>

            <div className="col-md-6">
              <label className="form-label">Amount min</label>
              <input
                type="number"
                className="form-control"
                value={filters.amountMin}
                onChange={(e) => setFilters((f) => ({ ...f, amountMin: e.target.value }))}
                placeholder="e.g. 4000"
              />
            </div>
            <div className="col-md-6">
              <label className="form-label">Amount max</label>
              <input
                type="number"
                className="form-control"
                value={filters.amountMax}
                onChange={(e) => setFilters((f) => ({ ...f, amountMax: e.target.value }))}
                placeholder="e.g. 6000"
              />
            </div>

            <div className="col-12 d-flex justify-content-between mt-2">
              <button className="btn btn-outline-secondary" onClick={clearFilters}>
                Clear
              </button>
              <button className="btn btn-primary" data-bs-dismiss="offcanvas">
                Apply
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* CSV Import Modal */}
      {showCsvModal && (
        <div className="modal fade show" style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  <i className="bi bi-file-earmark-spreadsheet me-2"></i>
                  Import Utility Usage from CSV
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={closeCsvModal}
                  disabled={csvUploading}
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <h6>CSV Format Requirements:</h6>
                  <div className="alert alert-info">
                    <p className="mb-2"><strong>Required columns (in order):</strong></p>
                    <ol className="mb-2">
                      <li><strong>RoomNumber</strong> - Room number (e.g., "101", "A201")</li>
                      <li><strong>WaterUsage</strong> - Water usage in units (e.g., 25)</li>
                      <li><strong>ElectricityUsage</strong> - Electricity usage in units (e.g., 150)</li>
                      <li><strong>BillingMonth</strong> - Billing month in YYYY-MM format (e.g., "2024-11")</li>
                    </ol>
                    <p className="mb-2"><strong>Optional columns:</strong></p>
                    <ul className="mb-0">
                      <li><strong>WaterRate</strong> - Water rate per unit (default: 20 THB/unit)</li>
                      <li><strong>ElectricityRate</strong> - Electricity rate per unit (default: 8 THB/unit)</li>
                    </ul>
                  </div>
                  
                  <div className="alert alert-warning">
                    <h6><i className="bi bi-exclamation-triangle me-1"></i> Sample CSV Format:</h6>
                    <pre className="mb-0" style={{ fontSize: '0.85em' }}>
{`RoomNumber,WaterUsage,ElectricityUsage,BillingMonth,WaterRate,ElectricityRate
101,25,150,2024-11,20,8
102,30,180,2024-11,20,8
A201,22,140,2024-11,20,8`}
                    </pre>
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="csvFile" className="form-label">
                    <strong>Select CSV File:</strong>
                  </label>
                  <input
                    type="file"
                    className="form-control"
                    id="csvFile"
                    accept=".csv"
                    onChange={handleCsvFileChange}
                    disabled={csvUploading}
                  />
                </div>

                {csvFile && (
                  <div className="alert alert-success">
                    <i className="bi bi-file-check me-1"></i>
                    Selected file: <strong>{csvFile.name}</strong> ({(csvFile.size / 1024).toFixed(2)} KB)
                  </div>
                )}

                {csvResult && (
                  <div className="mb-3">
                    <label className="form-label"><strong>Import Result:</strong></label>
                    <textarea
                      className="form-control"
                      rows="8"
                      value={csvResult}
                      readOnly
                      style={{ fontSize: '0.9em', fontFamily: 'monospace' }}
                    />
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={closeCsvModal}
                  disabled={csvUploading}
                >
                  Close
                </button>
                <button
                  type="button"
                  className="btn btn-success"
                  onClick={handleCsvImport}
                  disabled={!csvFile || csvUploading}
                >
                  {csvUploading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      Importing...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-upload me-1"></i>
                      Import CSV
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ===== Payment Management Modal ===== */}
      {showPaymentModal && selectedInvoice && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-xl">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  <i className="bi bi-credit-card-fill me-2"></i>
                  จัดการการชำระเงิน - Invoice #{selectedInvoice.id}
                </h5>
                <button 
                  type="button" 
                  className="btn-close" 
                  onClick={() => setShowPaymentModal(false)}
                ></button>
              </div>
              
              <div className="modal-body">
                {/* Invoice Summary */}
                <div className="row mb-4">
                  <div className="col-md-6">
                    <div className="card">
                      <div className="card-body">
                        <h6 className="card-title">ข้อมูลใบแจ้งหนี้</h6>
                        <p className="mb-1"><strong>ลูกค้า:</strong> {selectedInvoice.firstName} {selectedInvoice.lastName}</p>
                        <p className="mb-1"><strong>ห้อง:</strong> {selectedInvoice.floor}/{selectedInvoice.room}</p>
                        <p className="mb-1"><strong>ยอดรวม:</strong> <span className="text-primary fw-bold">{selectedInvoice.amount?.toLocaleString()} บาท</span></p>
                        <p className="mb-0">
                          <strong>สถานะ:</strong> 
                          <span className={`badge ms-2 ${selectedInvoice.status === 'Complete' ? 'bg-success' : 'bg-warning text-dark'}`}>
                            {selectedInvoice.status === 'Complete' ? 'ชำระแล้ว' : 'ยังไม่ชำระ'}
                          </span>
                        </p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="col-md-6">
                    <div className="card">
                      <div className="card-body">
                        <h6 className="card-title">สรุปการชำระ</h6>
                        {selectedInvoice.totalPaidAmount !== undefined ? (
                          <>
                            <p className="mb-1"><strong>ชำระแล้ว:</strong> <span className="text-success fw-bold">{selectedInvoice.totalPaidAmount?.toLocaleString()} บาท</span></p>
                            <p className="mb-1"><strong>รอยืนยัน:</strong> <span className="text-warning fw-bold">{selectedInvoice.totalPendingAmount?.toLocaleString()} บาท</span></p>
                            <p className="mb-0"><strong>คงเหลือ:</strong> <span className="text-danger fw-bold">{selectedInvoice.remainingAmount?.toLocaleString()} บาท</span></p>
                          </>
                        ) : (
                          <p className="text-muted">กำลังโหลดข้อมูล...</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Add Payment Form */}
                <div className="card mb-4">
                  <div className="card-header">
                    <h6 className="mb-0"><i className="bi bi-plus-circle me-2"></i>เพิ่มการบันทึกการชำระเงิน</h6>
                  </div>
                  <div className="card-body">
                    <form onSubmit={handleAddPayment}>
                      <div className="row g-3">
                        <div className="col-md-3">
                          <label className="form-label">จำนวนเงิน *</label>
                          <input
                            type="number"
                            className="form-control"
                            value={paymentForm.paymentAmount}
                            onChange={(e) => setPaymentForm(prev => ({ ...prev, paymentAmount: e.target.value }))}
                            step="0.01"
                            min="0"
                            required
                          />
                        </div>
                        
                        <div className="col-md-3">
                          <label className="form-label">วิธีการชำระ *</label>
                          <select
                            className="form-select"
                            value={paymentForm.paymentMethod}
                            onChange={(e) => setPaymentForm(prev => ({ ...prev, paymentMethod: e.target.value }))}
                            required
                          >
                            {Object.entries(paymentMethods).map(([key, value]) => (
                              <option key={key} value={key}>{value}</option>
                            ))}
                          </select>
                        </div>
                        
                        <div className="col-md-3">
                          <label className="form-label">วันที่ชำระ *</label>
                          <input
                            type="datetime-local"
                            className="form-control"
                            value={paymentForm.paymentDate}
                            onChange={(e) => setPaymentForm(prev => ({ ...prev, paymentDate: e.target.value }))}
                            required
                          />
                        </div>
                        
                        <div className="col-md-3">
                          <label className="form-label">เลขที่อ้างอิง</label>
                          <input
                            type="text"
                            className="form-control"
                            value={paymentForm.transactionReference}
                            onChange={(e) => setPaymentForm(prev => ({ ...prev, transactionReference: e.target.value }))}
                            placeholder="เลขที่โอนเงิน"
                          />
                        </div>
                        
                        <div className="col-md-9">
                          <label className="form-label">หมายเหตุ</label>
                          <input
                            type="text"
                            className="form-control"
                            value={paymentForm.notes}
                            onChange={(e) => setPaymentForm(prev => ({ ...prev, notes: e.target.value }))}
                            placeholder="หมายเหตุเพิ่มเติม"
                          />
                        </div>
                        
                        <div className="col-md-3">
                          <label className="form-label">ผู้บันทึก</label>
                          <input
                            type="text"
                            className="form-control"
                            value={paymentForm.recordedBy}
                            onChange={(e) => setPaymentForm(prev => ({ ...prev, recordedBy: e.target.value }))}
                            required
                          />
                        </div>
                        
                        <div className="col-12">
                          <button
                            type="submit"
                            className="btn btn-success"
                            disabled={savingPayment}
                          >
                            {savingPayment ? (
                              <>
                                <span className="spinner-border spinner-border-sm me-2"></span>
                                กำลังบันทึก...
                              </>
                            ) : (
                              <>
                                <i className="bi bi-plus-circle me-2"></i>
                                เพิ่มการบันทึกการชำระ
                              </>
                            )}
                          </button>
                        </div>
                      </div>
                    </form>
                  </div>
                </div>

                {/* File Upload Section for Payment Proofs */}
                <div className="card mb-4">
                  <div className="card-header">
                    <h6 className="mb-0">
                      <i className="bi bi-cloud-upload me-2"></i>
                      อัปโหลดหลักฐานการชำระเงิน (ทางเลือก)
                    </h6>
                  </div>
                  <div className="card-body">
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label">เลือกไฟล์</label>
                        <input
                          type="file"
                          className="form-control"
                          accept="image/*,.pdf"
                          onChange={(e) => {
                            const file = e.target.files[0];
                            if (file) {
                              // Validate file size
                              if (file.size > 5 * 1024 * 1024) {
                                showMessageError('ขนาดไฟล์ไม่ควรเกิน 5MB');
                                e.target.value = '';
                                return;
                              }
                              
                              // Validate file type
                              const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
                              if (!allowedTypes.includes(file.type)) {
                                showMessageError('รองรับเฉพาะไฟล์ JPG, PNG, GIF และ PDF');
                                e.target.value = '';
                                return;
                              }
                              
                              setSelectedFile(file);
                            }
                          }}
                        />
                        <div className="form-text">
                          รองรับ: รูปภาพ (JPG, PNG, GIF), PDF | ขนาดไม่เกิน 5MB
                          {selectedFile && (
                            <div className="mt-1">
                              <span className="badge bg-info">
                                📎 {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
                              </span>
                            </div>
                          )}
                        </div>
                      </div>
                      
                      <div className="col-md-3">
                        <label className="form-label">ประเภทหลักฐาน</label>
                        <select 
                          className="form-select"
                          value={proofType}
                          onChange={(e) => setProofType(e.target.value)}
                        >
                          <option value="BANK_SLIP">สลิปโอนเงิน</option>
                          <option value="RECEIPT">ใบเสร็จ</option>
                          <option value="BANK_STATEMENT">Statement ธนาคาร</option>
                          <option value="OTHER">อื่นๆ</option>
                        </select>
                      </div>

                      <div className="col-md-3">
                        <label className="form-label">&nbsp;</label>
                        <div>
                          <button
                            type="button"
                            className="btn btn-outline-primary"
                            onClick={handleUploadProof}
                            disabled={!selectedFile || uploadingProof}
                          >
                            {uploadingProof ? (
                              <>
                                <span className="spinner-border spinner-border-sm me-2"></span>
                                อัปโหลด...
                              </>
                            ) : (
                              <>
                                <i className="bi bi-upload me-2"></i>
                                อัปโหลด
                              </>
                            )}
                          </button>
                        </div>
                      </div>

                      <div className="col-12">
                        <label className="form-label">รายละเอียดเพิ่มเติม</label>
                        <textarea
                          className="form-control"
                          rows="2"
                          value={proofDescription}
                          onChange={(e) => setProofDescription(e.target.value)}
                          placeholder="รายละเอียดเพิ่มเติมเกี่ยวกับหลักฐาน (ถ้ามี)"
                        ></textarea>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Payment Records List */}
                <div className="card">
                  <div className="card-header">
                    <h6 className="mb-0"><i className="bi bi-list me-2"></i>ประวัติการชำระเงิน</h6>
                  </div>
                  <div className="card-body">
                    {loadingPayments ? (
                      <div className="text-center py-3">
                        <span className="spinner-border spinner-border-sm me-2"></span>
                        กำลังโหลดข้อมูล...
                      </div>
                    ) : paymentRecords.length > 0 ? (
                      <div className="table-responsive">
                        <table className="table table-hover">
                          <thead>
                            <tr>
                              <th>วันที่ชำระ</th>
                              <th>จำนวนเงิน</th>
                              <th>วิธีการชำระ</th>
                              <th>สถานะ</th>
                              <th>เลขที่อ้างอิง</th>
                              <th>หลักฐาน</th>
                              <th>หมายเหตุ</th>
                              <th>การจัดการ</th>
                            </tr>
                          </thead>
                          <tbody>
                            {paymentRecords.map((payment) => (
                              <tr key={payment.id}>
                                <td>{new Date(payment.paymentDate).toLocaleString('th-TH')}</td>
                                <td className="fw-bold text-success">{payment.paymentAmount?.toLocaleString()} บาท</td>
                                <td>{payment.paymentMethodDisplay}</td>
                                <td>
                                  <select
                                    className={`form-select form-select-sm ${
                                      payment.paymentStatus === 'CONFIRMED' ? 'text-success' :
                                      payment.paymentStatus === 'PENDING' ? 'text-warning' : 'text-danger'
                                    }`}
                                    value={payment.paymentStatus}
                                    onChange={(e) => handleUpdatePaymentStatus(payment.id, e.target.value)}
                                  >
                                    {Object.entries(paymentStatuses).map(([key, value]) => (
                                      <option key={key} value={key}>{value}</option>
                                    ))}
                                  </select>
                                </td>
                                <td>{payment.transactionReference}</td>
                                <td>
                                  {payment.paymentProofs && payment.paymentProofs.length > 0 ? (
                                    <div>
                                      <span className="badge bg-info me-1">
                                        <i className="bi bi-paperclip me-1"></i>
                                        {payment.paymentProofs.length} ไฟล์
                                      </span>
                                      {payment.paymentProofs.map((proof, index) => (
                                        <div key={proof.id} className="small d-flex align-items-center gap-2 mt-1">
                                          <span>📎 {proof.fileName}</span>
                                          <span className="badge bg-secondary">{proof.proofTypeDisplay}</span>
                                          <button
                                            className="btn btn-sm btn-outline-success"
                                            onClick={() => handleViewProof(proof.id, proof.fileName)}
                                            title="ดาวน์โหลดหลักฐาน"
                                          >
                                            <i className="bi bi-download"></i>
                                          </button>
                                        </div>
                                      ))}
                                    </div>
                                  ) : (
                                    <span className="text-muted small">ไม่มีหลักฐาน</span>
                                  )}
                                </td>
                                <td>{payment.notes}</td>
                                <td>
                                  <button
                                    className="btn btn-sm btn-outline-danger"
                                    onClick={() => handleDeletePayment(payment.id)}
                                    title="ลบการบันทึก"
                                  >
                                    <i className="bi bi-trash"></i>
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div className="text-center py-3 text-muted">
                        <i className="bi bi-inbox display-6"></i>
                        <p>ยังไม่มีการบันทึกการชำระเงิน</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
              
              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={() => setShowPaymentModal(false)}
                >
                  ปิด
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default InvoiceManagement;
