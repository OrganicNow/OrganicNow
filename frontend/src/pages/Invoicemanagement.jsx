import React, { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import { useToast } from "../component/Toast.jsx";
import { pageSize as defaultPageSize } from "../config_variable";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

function InvoiceManagement() {
  const navigate = useNavigate();
  const { showSuccess, showError, showWarning } = useToast();

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [pageSize, setPageSize] = useState(defaultPageSize);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState("");

  // ✅ สถานะกำลังลบใบแจ้งหนี้ (เพื่อ disable ปุ่ม/โชว์ spinner)
  const [deletingId, setDeletingId] = useState(null);

  // ====== DATA จาก Backend ======
  const [data, setData] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [contracts, setContracts] = useState([]);
  const [tenants, setTenants] = useState([]);
  const [packages, setPackages] = useState([]);

  // สำหรับ dropdown ห้อง (ใช้ข้อมูลจาก backend เท่านั้น)
  const roomsByFloor = useMemo(() => {
    if (!rooms || rooms.length === 0) {
      console.log("⚠️ No rooms from API");
      return {};
    }

    const result = {};
    console.log("🏗️ Processing rooms from API:", rooms);
    
    rooms.forEach((room, index) => {
      // ใช้ field names ที่ถูกต้องจาก API response จริง
      const floor = room.roomFloor;  // field จริงจาก API
      const roomNumber = room.roomNumber;  // field จริงจาก API
      
      if (index < 3) { // แสดง 3 ตัวแรกเป็นตัวอย่าง
        console.log(`🔍 Room ${index}:`, {
          roomFloor: floor,
          roomNumber: roomNumber,
          roomId: room.roomId
        });
      }
      
      if (floor !== undefined && floor !== null && roomNumber !== undefined && roomNumber !== null) {
        const floorStr = String(floor);
        const roomStr = String(roomNumber);
        if (!result[floorStr]) result[floorStr] = [];
        result[floorStr].push(roomStr);
      }
    });
    
    console.log("📋 Final roomsByFloor result:", result);
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
        console.log("🏠 Rooms from API:", json); // Debug log
        if (Array.isArray(json) && json.length > 0) {
          setRooms(json);
          console.log("✅ Rooms loaded successfully from API");
        } else {
          console.log("⚠️ Empty rooms array from API");
          setRooms([]);
        }
      } else {
        console.log("❌ Room API failed:", res.status);
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
      const res = await fetch(`${API_BASE}/contract/list`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (res.ok) {
        const json = await res.json();
        console.log("📄 Contracts from API:", json); // Debug log
        setContracts(Array.isArray(json) ? json : []);
      } else {
        console.log("❌ Contract API failed:", res.status);
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
        console.log("👥 Tenants from API:", json); // Debug log
        // tenant/list ส่ง object {results: [...]} ไม่ใช่ array โดยตรง
        const tenantArray = json.results || json;
        setTenants(Array.isArray(tenantArray) ? tenantArray : []);
      } else {
        console.log("❌ Tenant API failed:", res.status);
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
        console.log("📦 Packages from API:", json);
        setPackages(Array.isArray(json) ? json : []);
      } else {
        console.log("❌ Package API failed:", res.status);
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

  // Auto-select package when floor and room are selected (เฉพาะ active packages)
  useEffect(() => {
    if (invForm.floor && invForm.room) {
      // First try to find from contracts (ใช้ field names ที่ถูกต้อง)
      const contractData = contracts.find(contract => {
        const floorMatch = contract.floor === Number(invForm.floor);
        const roomMatch = contract.room === invForm.room;
        return floorMatch && roomMatch;
      });
      
      if (contractData && contractData.packageId) {
        // ✅ ตรวจสอบว่า package ยัง active อยู่หรือไม่
        const packageData = packages.find(pkg => pkg.id === contractData.packageId);
        if (packageData && (packageData.is_active === 1 || packageData.is_active === true)) {
          setInvForm((prev) => ({ 
            ...prev, 
            packageId: contractData.packageId.toString()
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
  }, [invForm.floor, invForm.room, rooms, contracts, packages]);

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

  const handleUpdate = (item) => {
    console.log("Update: ", item);
  };

  // ✅ ลบใบแจ้งหนี้ (DELETE /invoice/delete/{id})
  const handleDelete = async (id) => {
    const yes = window.confirm("ต้องการลบใบแจ้งหนี้นี้หรือไม่?");
    if (!yes) return;

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
      showSuccess("🗑️ ลบ Invoice สำเร็จแล้ว!");
    } catch (e) {
      console.error(e);
      setErr(e.message || "ลบไม่สำเร็จ");
      showError(`❌ ลบ Invoice ล้มเหลว: ${e.message}`);
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

  const handleSelectRow = (rowIndex) => {
    setSelectedItems((prev) =>
      prev.includes(rowIndex) ? prev.filter((i) => i !== rowIndex) : [...prev, rowIndex]
    );
  };

  const handleSelectAll = () => {
    if (selectedItems.length === pageRows.length) {
      setSelectedItems([]);
    } else {
      setSelectedItems(pageRows.map((_, idx) => idx));
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

      console.log("🚀 Sending invoice data:", body);
      console.log("📋 Current form state:", invForm);

      const res = await fetch(`${API_BASE}/invoice/create`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      console.log("📡 Response status:", res.status);

      if (!res.ok) {
        const t = await res.text().catch(() => "");
        console.error("❌ Backend error:", t);
        throw new Error(t || `HTTP ${res.status}`);
      }

      const result = await res.json();
      console.log("✅ Backend response:", result);
      console.log("🔍 Response details - floor:", result.floor, "room:", result.room, "rent:", result.rent);

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
      
      showSuccess("🎉 สร้าง Invoice สำเร็จแล้ว!");
      return true;
    } catch (e) {
      console.error(e);
      setErr(`Create invoice failed: ${e.message}`);
      showError(`❌ สร้าง Invoice ล้มเหลว: ${e.message}`);
      return false;
    } finally {
      setSaving(false);
    }
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

                  {/* Right cluster: Create / Refresh */}
                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#createInvoiceModal"
                      onClick={() => {
                        // ✅ Refresh packages data เมื่อเปิด modal
                        fetchPackages();
                        console.log("🔄 Refreshing packages before creating invoice");
                      }}
                    >
                      <i className="bi bi-plus-lg me-1"></i> Create Invoice
                    </button>
                    <button className="btn btn-outline-secondary" onClick={fetchData} disabled={loading}>
                      <i className={`bi ${loading ? "bi-arrow-repeat spin" : "bi-arrow-repeat"} me-1`}></i>
                      Refresh
                    </button>
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

            {/* Data Table */}
            <div className="table-wrapper">
              <table className="table text-nowrap">
                <thead>
                  <tr>
                    {/* <th className="text-center header-color checkbox-cell">
                      <input type="checkbox" checked={isAllSelected} onChange={handleSelectAll} />
                    </th> */}
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
                      <td colSpan="12" className="text-center">
                        Loading...
                      </td>
                    </tr>
                  ) : pageRows.length > 0 ? (
                    pageRows.map((item, idx) => (
                      <tr key={`${item.id}-${idx}`}>
                        {/* <td className="align-middle text-center checkbox-cell">
                          <input
                            type="checkbox"
                            checked={selectedItems.includes(idx)}
                            onChange={() => handleSelectRow(idx)}
                          />
                        </td> */}
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
                        <td className="align-middle text-start ">{(item.rent + item.water + item.electricity).toLocaleString()}</td>
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
                          >
                            <i className="bi bi-eye-fill"></i>
                          </button>
                          <button
                            className="btn btn-sm form-Button-Edit me-1"
                            onClick={() => handleUpdate(item)}
                            aria-label="Download PDF"
                          >
                            <i className="bi bi-file-earmark-pdf-fill"></i>
                          </button>
                          <button
                            className="btn btn-sm form-Button-Del me-1"
                            onClick={() => handleDelete(item.id)}  // ✅ ส่ง id
                            aria-label="Delete invoice"
                            disabled={deletingId === item.id || loading} // ✅ กันกดซ้ำ
                          >
                            <i className={`bi ${deletingId === item.id ? "bi-arrow-repeat spin" : "bi-trash-fill"}`}></i>
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="12" className="text-center">
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
                      {Object.keys(roomsByFloor).sort().map((floor) => (
                        <option key={floor} value={floor} style={{ backgroundColor: '#fff', color: '#000' }}>
                          Floor {floor}
                        </option>
                      ))}
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
                      {roomOptions.map((rm) => (
                        <option key={rm} value={rm} style={{ backgroundColor: '#fff', color: '#000' }}>
                          Room {rm}
                        </option>
                      ))}
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
    </Layout>
  );
}

export default InvoiceManagement;
