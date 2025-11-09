// src/pages/MaintenanceDetails.jsx
import React, { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import useMessage from "../component/useMessage";
import * as bootstrap from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

// ตั้งค่า API
const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

// helper: ดึง yyyy-mm-dd จาก LocalDateTime
const toDate = (s) => (s ? s.slice(0, 10) : "");
// helper: แปลง yyyy-mm-dd -> yyyy-mm-ddTHH:mm:ss
const toLdt = (d) => (d ? `${d}T00:00:00` : null);

function MaintenanceDetails() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { showMessageError, showMessageSave, showMessageConfirmDelete } = useMessage();

  // รองรับรับ id ได้ทั้งจาก state และ query (?id=1)
  const idFromState = location.state?.id;
  const idFromQuery = searchParams.get("id");
  const maintainId = idFromState ?? (idFromQuery ? Number(idFromQuery) : null);

  // โหลดข้อมูล
  const [data, setData] = useState(null);
  const [tenantData, setTenantData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const fetchOne = async () => {
    if (!maintainId) {
      setErr("Missing maintenance id");
      return;
    }
    try {
      setLoading(true);
      setErr("");
      const res = await fetch(`${API_BASE}/maintain/${maintainId}`, {
        credentials: "include",
      });
      if (!res.ok) throw new Error(await res.text());
      const json = await res.json();
      setData(json);
      
      // ✅ ดึงข้อมูล tenant จากห้อง
      if (json.roomId) {
        await fetchTenantFromRoom(json.roomId);
      }
    } catch (e) {
      console.error(e);
      setErr("Failed to load maintenance.");
    } finally {
      setLoading(false);
    }
  };

  // ✅ ฟังก์ชันดึงข้อมูล tenant จาก contract
  const fetchTenantFromRoom = async (roomId) => {
    try {
      console.log("🔍 Fetching tenant for roomId:", roomId);
      
      const res = await fetch(`${API_BASE}/tenant/list`, {
        credentials: "include",
      });
      if (res.ok) {
        const json = await res.json();
        console.log("📋 Tenant API response:", json);
        
        const tenantList = json.results || json;
        console.log("👥 Tenant list:", tenantList);
        
        if (Array.isArray(tenantList)) {
          console.log("🔎 Looking for tenant with roomId:", roomId);
          tenantList.forEach((tenant, index) => {
            console.log(`Tenant ${index}:`, {
              roomId: tenant.roomId,
              status: tenant.status,
              firstName: tenant.firstName,
              lastName: tenant.lastName
            });
          });
          
          // หา tenant ที่อยู่ในห้องนี้ (รวมทั้งที่หมดอายุแล้ว)
          console.log("🔍 Searching for roomId:", roomId, "type:", typeof roomId);
          
          const tenant = tenantList.find(t => {
            const roomIdMatch = Number(t.roomId) === Number(roomId);
            // เปลี่ยนจาก status=1 เป็น status>=0 เพื่อรวม tenant ที่หมดอายุ
            const statusMatch = Number(t.status) >= 0;
            console.log(`Checking tenant: roomId=${t.roomId} vs ${roomId} (match: ${roomIdMatch}), status=${t.status} (match: ${statusMatch})`);
            return roomIdMatch && statusMatch;
          });
          
          console.log("🎯 Found tenant:", tenant);
          setTenantData(tenant || null);
        } else {
          console.log("❌ Tenant list is not an array");
          setTenantData(null);
        }
      } else {
        console.log("❌ Tenant API failed:", res.status);
        setTenantData(null);
      }
    } catch (e) {
      console.error("❌ Failed to fetch tenant data:", e);
      setTenantData(null);
    }
  };

  useEffect(() => {
    fetchOne();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maintainId]);

  // ✅ Enhanced status badge with more states
  const statusInfo = useMemo(() => {
    if (!data) return { badge: "bg-secondary", text: "Loading", icon: "bi-hourglass" };
    
    const hasScheduled = !!data.scheduledDate;
    const isComplete = !!data.finishDate;
    
    if (isComplete) {
      return { 
        badge: "bg-success", 
        text: "Complete", 
        icon: "bi-check-circle-fill" 
      };
    } else if (hasScheduled) {
      return { 
        badge: "bg-warning", 
        text: "In Progress", 
        icon: "bi-gear-fill" 
      };
    } else {
      return { 
        badge: "bg-secondary-subtle text-secondary", 
        text: "Not Started", 
        icon: "bi-circle" 
      };
    }
  }, [data]);

  // ------- ฟอร์มใน Modal (สไตล์เดิม) -------
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    target: "asset", // "asset" or "building"
    issueTitle: "",
    issueCategory: 0,
    issueDescription: "",
    requestDate: "",
    maintainDate: "",
    completeDate: "",
    maintainType: "", // ✅ เพิ่มฟิลด์ maintain type
    technician: "",   // ✅ เพิ่มฟิลด์ช่าง
    phone: "",        // ✅ เพิ่มฟิลด์เบอร์โทรช่าง
    state: "Not Started", // ✅ เพิ่ม state field
  });

  // ✅ Issue options for Asset target
  const assetIssueOptions = [
    { value: 0, label: "แอร์" },
    { value: 1, label: "ไฟ" },
    { value: 2, label: "ประปา" },
    { value: 3, label: "ไฟฟ้า" },
    { value: 4, label: "อื่นๆ" }
  ];

  // ✅ Maintain type options
  const maintainTypeOptions = [
    { value: "fix", label: "Fix" },
    { value: "shift", label: "Shift" },
    { value: "replace", label: "Replace" },
    { value: "maintenance", label: "Maintenance" }
  ];

  useEffect(() => {
    if (!data) return;
    setForm({
      target: data.targetType === 0 ? "asset" : "building",
      issueTitle: data.issueTitle ?? "",
      issueCategory: data.issueCategory ?? 0,
      issueDescription: data.issueDescription ?? "",
      requestDate: toDate(data.createDate) || "",
      maintainDate: toDate(data.scheduledDate) || "",
      completeDate: toDate(data.finishDate) || "",
      maintainType: data.maintainType || "", // ✅ ดึงจาก backend
      technician: data.technicianName || "",   // ✅ ดึงจาก backend  
      phone: data.technicianPhone || "",        // ✅ ดึงจาก backend
      state: data.finishDate ? "Complete" : (data.scheduledDate ? "In Progress" : "Not Started"), // ✅ กำหนด state ตาม data
    });
  }, [data]);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((s) => {
      // ✅ Convert issueCategory to Integer
      const processedValue = name === "issueCategory" ? parseInt(value, 10) || 0 : value;
      const newForm = { ...s, [name]: processedValue };
      
      // ✅ Reset issue fields when target changes
      if (name === "target") {
        newForm.issueTitle = "";
        newForm.issueCategory = 0;
      }
      
      // ✅ Auto-set completeDate when state changes to Complete
      if (name === "state") {
        if (value === "Complete") {
          // ถ้ายังไม่มี completeDate ให้ใส่วันนี้
          if (!newForm.completeDate) {
            newForm.completeDate = new Date().toISOString().slice(0, 10);
          }
        } else if (value === "Not Started") {
          // ถ้าเป็น Not Started ให้ลบ completeDate และ maintainDate
          newForm.completeDate = "";
          newForm.maintainDate = "";
        } else if (value === "In Progress") {
          // ถ้าเป็น In Progress ให้ลบ completeDate แต่เก็บ maintainDate
          newForm.completeDate = "";
          if (!newForm.maintainDate) {
            newForm.maintainDate = new Date().toISOString().slice(0, 10);
          }
        }
      }
      
      return newForm;
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    
    // ✅ ป้องกัน double submit
    if (saving) return;
    
    try {
      setSaving(true);

      // ✅ Check for status changes to show appropriate messages (ใช้ form.state เป็นหลัก)
      const previousStatus = data?.finishDate ? "Complete" : (data?.scheduledDate ? "In Progress" : "Not Started");
      const newStatus = form.state; // ✅ ใช้ state จาก form โดยตรง
      const statusChanged = previousStatus !== newStatus;

      const payload = {
        targetType: form.target === "asset" ? 0 : 1,
        issueTitle: form.issueTitle,
        issueCategory: form.target === "asset" ? form.issueCategory : 0, // ✅ Building ใช้ 0, Asset ใช้จาก dropdown
        issueDescription: form.issueDescription,
        scheduledDate: form.state !== "Not Started" && form.maintainDate ? toLdt(form.maintainDate) : null, // ✅ ส่ง scheduledDate ตาม state
        finishDate: form.state === "Complete" && form.completeDate ? toLdt(form.completeDate) : null, // ✅ ส่ง finishDate ตาม state
        // ✅ เพิ่มฟิลด์ใหม่
        maintainType: form.maintainType,
        technicianName: form.technician,
        technicianPhone: form.phone,
      };

      const res = await fetch(`${API_BASE}/maintain/update/${maintainId}`, {
        method: "PUT",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(await res.text());
      
      await fetchOne();

      // ✅ Enhanced success notifications based on changes
      if (statusChanged) {
        if (newStatus === "Complete") {
          showMessageSave();
        } else if (newStatus === "In Progress" && previousStatus === "Not Started") {
          showMessageSave();
        } else if (newStatus === "Not Started" && previousStatus === "Complete") {
          showMessageSave();
        } else {
          showMessageSave();
        }
      } else {
        showMessageSave();
      }

      // ✅ ปิด modal อย่างสมบูรณ์
      const modalElement = document.getElementById("editMaintainModal");
      if (modalElement) {
        const modalInstance = bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
        
        // ✅ Force ลบ modal backdrop และ class
        setTimeout(() => {
          // ลบ backdrop
          const backdrop = document.querySelector('.modal-backdrop');
          if (backdrop) {
            backdrop.remove();
          }
          
          // ลบ class จาก body
          document.body.classList.remove('modal-open');
          document.body.style.overflow = '';
          document.body.style.paddingRight = '';
          
          // รีเซ็ต modal
          modalElement.classList.remove('show');
          modalElement.style.display = 'none';
          modalElement.setAttribute('aria-hidden', 'true');
          modalElement.removeAttribute('aria-modal');
          modalElement.removeAttribute('role');
        }, 150);
      }
      
      // ✅ รีโหลดข้อมูลหลังอัปเดตเสร็จ
      setTimeout(() => {
        fetchOne();
      }, 200);
      
    } catch (e2) {
      console.error("❌ Update error:", e2);
      showMessageError(`Update failed: ${e2.message}`);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    const result = await showMessageConfirmDelete(`maintenance #${maintainId}`);
    if (!result.isConfirmed) return;
    
    try {
      const res = await fetch(`${API_BASE}/maintain/${maintainId}`, {
        method: "DELETE",
        credentials: "include",
      });
      if (!res.ok) throw new Error(await res.text());
      
      showMessageSave();
      navigate("/maintenancerequest");
    } catch (e) {
      showMessageError(`Delete failed: ${e.message}`);
    }
  };

  // ✅ Quick action handlers for status changes
  const handleMarkComplete = async () => {
    const today = new Date().toISOString().slice(0, 10);
    
    try {
      setSaving(true);

      const payload = {
        targetType: data.targetType, // Keep existing target type
        issueTitle: form.issueTitle || data.issueTitle,
        issueCategory: data.issueCategory,
        issueDescription: form.issueDescription || data.issueDescription,
        scheduledDate: toLdt(form.maintainDate || toDate(data.scheduledDate)),
        finishDate: toLdt(today), // Set completion date to today
      };

      const res = await fetch(`${API_BASE}/maintain/update/${maintainId}`, {
        method: "PUT",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(await res.text());
      
      await fetchOne(); // Refresh data
      showMessageSave();
      
    } catch (e2) {
      showMessageError(`Mark complete failed: ${e2.message}`);
    } finally {
      setSaving(false);
    }
  };

  const handleStartWork = async () => {
    const today = new Date().toISOString().slice(0, 10);
    
    try {
      setSaving(true);

      const payload = {
        targetType: data.targetType, // Keep existing target type
        issueTitle: form.issueTitle || data.issueTitle,
        issueCategory: data.issueCategory,
        issueDescription: form.issueDescription || data.issueDescription,
        scheduledDate: toLdt(today), // Set maintain date to today
        finishDate: null, // Clear completion to make it "In Progress"
      };

      const res = await fetch(`${API_BASE}/maintain/update/${maintainId}`, {
        method: "PUT",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(await res.text());
      
      await fetchOne(); // Refresh data
      showMessageSave();
      
    } catch (e2) {
      showMessageError(`Start work failed: ${e2.message}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout title="Maintenance Request" icon="bi bi-wrench" notifications={0}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          <div className="col-lg-11 p-4">
            {/* Toolbar (เหมือนหน้าเดิม/InvoiceDetails) */}
            <div className="toolbar-wrapper card border-0 bg-white">
              <div className="card-header bg-white border-0 rounded-2">
                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                  <div className="d-flex align-items-center gap-2">
                    <span
                      className="breadcrumb-link text-primary"
                      style={{ cursor: "pointer" }}
                      onClick={() => navigate("/maintenancerequest")}
                    >
                      Maintenance Request
                    </span>
                    <span className="text-muted">›</span>
                    <span className="breadcrumb-current">
                      {data ? `#${data.id} - ${data.roomNumber || "-"}` : "-"}
                    </span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    {/* ✅ Smart action buttons based on status */}
                    {data && !data.finishDate && !data.scheduledDate && (
                      <button
                        type="button"
                        className="btn btn-success btn-sm"
                        onClick={handleStartWork}
                        disabled={saving}
                        title="Start maintenance work"
                      >
                        <i className={saving ? "bi bi-hourglass-split me-1" : "bi bi-play-fill me-1"}></i> 
                        {saving ? "Starting..." : "Start Work"}
                      </button>
                    )}
                    {data && data.scheduledDate && !data.finishDate && (
                      <button
                        type="button"
                        className="btn btn-warning btn-sm"
                        onClick={handleMarkComplete}
                        disabled={saving}
                        title="Mark as complete"
                      >
                        <i className={saving ? "bi bi-hourglass-split me-1" : "bi bi-check-circle me-1"}></i>
                        {saving ? "Completing..." : "Mark Complete"}
                      </button>
                    )}
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#editMaintainModal"
                      disabled={!data}
                      onClick={() => {
                        // ✅ รีเซ็ต form เมื่อเปิด modal
                        if (data) {
                          setForm({
                            target: data.targetType === 0 ? "asset" : "building",
                            issueTitle: data.issueTitle ?? "",
                            issueCategory: data.issueCategory ?? 0,
                            issueDescription: data.issueDescription ?? "",
                            requestDate: toDate(data.createDate) || "",
                            maintainDate: toDate(data.scheduledDate) || "",
                            completeDate: toDate(data.finishDate) || "",
                            maintainType: data.maintainType ?? "fix",
                            technician: data.technicianName ?? "",
                            phone: data.technicianPhone ?? "",
                            state: data.finishDate ? "Complete" : (data.scheduledDate ? "In Progress" : "Not Started"), // ✅ กำหนด state
                          });
                        }
                      }}
                    >
                      <i className="bi bi-pencil me-1"></i> Edit Request
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {err && <div className="alert alert-danger mt-3">{err}</div>}

            {/* Details (เลย์เอาต์ 2 คอลัมน์ + การ์ดเหมือนเดิม) */}
            <div className="table-wrapper-detail rounded-0 mt-3">
              <div className="row g-4">
                {/* Left column */}
                <div className="col-lg-6">
                  <div className="card border-0 shadow-sm mb-3 rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Room Information</h5>
                      {loading || !data ? (
                        <div>Loading...</div>
                      ) : (
                        <>
                          <p>
                            <span className="label">Room:</span>{" "}
                            <span className="value">{data.roomNumber || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Floor:</span>{" "}
                            <span className="value">{data.roomFloor ?? "-"}</span>
                          </p>
                          {/* <p>
                            <span className="label">Target:</span>{" "}
                            <span className="value">
                              <span className={`badge ${data.targetType === 0 ? 'bg-info' : 'bg-primary'}`}>
                                <i className={`bi ${data.targetType === 0 ? 'bi-gear' : 'bi-building'} me-1`}></i>
                                {data.targetType === 0 ? "Asset" : "Building"}
                              </span>
                            </span>
                          </p> */}
                        </>
                      )}
                    </div>
                  </div>

                  <div className="card border-0 shadow-sm rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Tenant Information</h5>
                      {loading ? (
                        <div>Loading tenant info...</div>
                      ) : tenantData ? (
                        <>
                          <p>
                            <span className="label">First Name:</span>{" "}
                            <span className="value">{tenantData.firstName || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Last Name:</span>{" "}
                            <span className="value">{tenantData.lastName || "-"}</span>
                          </p>
                          <p>
                            <span className="label">National ID:</span>{" "}
                            <span className="value">{tenantData.nationalId || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Phone Number:</span>{" "}
                            <span className="value">{tenantData.phoneNumber || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Email:</span>{" "}
                            <span className="value">{tenantData.email || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Package:</span>{" "}
                            <span className="value">
                              <span className="badge bg-primary">
                                <i className="bi bi-box me-1"></i>
                                {tenantData.contractName || "Standard Package"}
                              </span>
                            </span>
                          </p>
                          <div className="row">
                            <div className="col-6">
                              <p>
                                <span className="label">Sign date:</span>{" "}
                                <span className="value">{toDate(tenantData.signDate) || "-"}</span>
                              </p>
                              <p>
                                <span className="label">End date:</span>{" "}
                                <span className="value">{toDate(tenantData.endDate) || "-"}</span>
                              </p>
                            </div>
                            <div className="col-6">
                              <p>
                                <span className="label">Start date:</span>{" "}
                                <span className="value">{toDate(tenantData.startDate) || "-"}</span>
                              </p>
                            </div>
                          </div>
                        </>
                      ) : (
                        <div className="text-muted">
                          <i className="bi bi-info-circle me-2"></i>
                          No active tenant found for this room
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Right column */}
                <div className="col-lg-6">
                  <div className="card border-0 shadow-sm mb-3 rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Request Information</h5>
                      {loading || !data ? (
                        <div>Loading...</div>
                      ) : (
                        <>
                          <p>
                            <span className="label">Target:</span>{" "}
                            <span className="value">
                              <span className={`badge ${data.targetType === 0 ? 'bg-info' : 'bg-primary'}`}>
                                <i className={`bi ${data.targetType === 0 ? 'bi-gear' : 'bi-building'} me-1`}></i>
                                {data.targetType === 0 ? "Asset" : "Building"}
                              </span>
                            </span>
                          </p>
                          <p>
                            <span className="label">Issue:</span>{" "}
                            <span className="value">{data.issueTitle || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Maintain type:</span>{" "}
                            <span className="value">
                              {data.maintainType ? (
                                <span className="badge bg-warning text-dark">
                                  <i className="bi bi-circle-fill me-1"></i>
                                  {data.maintainType}
                                </span>
                              ) : (
                                <span className="text-muted">-</span>
                              )}
                            </span>
                          </p>
                          <p>
                            <span className="label">Request date:</span>{" "}
                            <span className="value">{toDate(data.createDate) || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Maintain date:</span>{" "}
                            <span className="value">{toDate(data.scheduledDate) || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Complete date:</span>{" "}
                            <span className="value">{toDate(data.finishDate) || "-"}</span>
                          </p>
                          <p>
                            <span className="label">State:</span>{" "}
                            <span className="value">
                              <span className={`badge ${statusInfo.badge}`}>
                                <i className={`${statusInfo.icon} me-1`}></i>
                                {statusInfo.text}
                              </span>
                            </span>
                          </p>
                        </>
                      )}
                    </div>
                  </div>

                  <div className="card border-0 shadow-sm rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Technician Information</h5>
                      {loading || !data ? (
                        <div>Loading...</div>
                      ) : (
                        <>
                          <p>
                            <span className="label">Technician's name:</span>{" "}
                            <span className="value">{data.technicianName || "-"}</span>
                          </p>
                          <p>
                            <span className="label">Phone Number:</span>{" "}
                            <span className="value">{data.technicianPhone || "-"}</span>
                          </p>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ===== Modal Edit (คงรูปแบบเดียวกับหน้าที่คุณใช้) ===== */}
      <Modal
        id="editMaintainModal"
        title="Edit Request"
        icon="bi bi-pencil"
        size="modal-lg"
        scrollable="modal-dialog-scrollable"
      >
        {!data ? (
          <div className="p-3">Loading...</div>
        ) : (
          <form onSubmit={handleSave}>
            {/* Room Information */}
            <div className="row g-3 align-items-start">
              <div className="col-md-3"><strong>Room Information</strong></div>
              <div className="col-md-9">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Floor</label>
                    <input type="text" className="form-control" value={data.roomFloor ?? ""} disabled />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Room</label>
                    <input type="text" className="form-control" value={data.roomNumber || ""} disabled />
                  </div>
                </div>
              </div>
            </div>

            <hr className="my-4" />

            {/* Repair Information */}
            <div className="row g-3 align-items-start">
              <div className="col-md-3"><strong>Repair Information</strong></div>
              <div className="col-md-9">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Target</label>
                    <select
                      className="form-select"
                      name="target"
                      value={form.target}
                      onChange={onChange}
                      required
                    >
                      <option value="asset">Asset</option>
                      <option value="building">Building</option>
                    </select>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Issue</label>
                    {form.target === "asset" ? (
                      <select
                        className="form-select"
                        name="issueCategory"
                        value={form.issueCategory}
                        onChange={onChange}
                        required
                      >
                        <option value={0}>เลือกประเภทปัญหา</option>
                        {assetIssueOptions.map(option => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <input
                        type="text"
                        className="form-control"
                        name="issueTitle"
                        value={form.issueTitle}
                        onChange={onChange}
                        placeholder="ระบุปัญหาของอาคาร"
                        required
                      />
                    )}
                  </div>
                  
                  <div className="col-md-6">
                    <label className="form-label">Maintain type</label>
                    <select
                      className="form-select"
                      name="maintainType"
                      value={form.maintainType}
                      onChange={onChange}
                      required
                    >
                      <option value="">เลือกประเภทการซ่อม</option>
                      {maintainTypeOptions.map(option => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  
                  <div className="col-md-6">
                    <label className="form-label">Request date</label>
                    <input type="date" className="form-control" value={form.requestDate} disabled />
                  </div>
                  
                  <div className="col-md-6">
                    <label className="form-label">Maintain date</label>
                    <input
                      type="date"
                      className="form-control"
                      name="maintainDate"
                      value={form.maintainDate}
                      onChange={onChange}
                    />
                  </div>
                  
                  <div className="col-md-6">
                    <label className="form-label">State</label>
                    <select
                      className="form-select"
                      name="state"
                      value={form.state}
                      onChange={onChange}
                    >
                      <option value="Not Started">Not Started</option>
                      <option value="In Progress">In Progress</option>
                      <option value="Complete">Complete</option>
                    </select>
                  </div>
                  
                  <div className="col-md-12">
                    <label className="form-label">Complete date</label>
                    <input
                      type="date"
                      className="form-control"
                      name="completeDate"
                      value={form.completeDate}
                      onChange={onChange}
                      disabled={form.state !== "Complete"}
                    />
                  </div>
                </div>
              </div>
            </div>

            <hr className="my-4" />

            {/* Technician Information */}
            <div className="row g-3 align-items-start">
              <div className="col-md-3"><strong>Technician Information</strong></div>
              <div className="col-md-9">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Technician's name</label>
                    <input
                      type="text"
                      className="form-control"
                      name="technician"
                      value={form.technician}
                      onChange={onChange}
                      placeholder="Add Technician's name"
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Phone Number</label>
                    <input
                      type="text"
                      className="form-control"
                      name="phone"
                      value={form.phone}
                      onChange={onChange}
                      placeholder="Add Phone Number"
                    />
                  </div>
                </div>
              </div>
            </div>

            <hr className="my-4" />

            {/* Footer */}
            <div className="d-flex justify-content-center gap-3 pt-4 pb-2">
              <button 
                type="button" 
                className="btn btn-outline-secondary" 
                data-bs-dismiss="modal"
                disabled={saving}
              >
                Cancel
              </button>
              <button 
                type="submit" 
                className="btn btn-primary" 
                disabled={saving}
              >
                {saving ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Saving...
                  </>
                ) : (
                  "Save"
                )}
              </button>
            </div>
          </form>
        )}
      </Modal>
    </Layout>
  );
}

export default MaintenanceDetails;
