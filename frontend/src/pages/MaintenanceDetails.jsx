// src/pages/MaintenanceDetails.jsx
import React, { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import useMessage from "../component/useMessage";
import * as bootstrap from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import { apiPath } from "../config_variable";

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
      const res = await fetch(`${apiPath}/maintain/${maintainId}`, {
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
      
      const res = await fetch(`${apiPath}/tenant/list`, {
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
  const [uploadingImage, setUploadingImage] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const [workImageUrl, setWorkImageUrl] = useState("");
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

  // ✅ Assets for room (dynamic loading)
  const [assets, setAssets] = useState([]);
  const [loadingAssets, setLoadingAssets] = useState(false);

  const fetchAssets = async (roomId) => {
    if (!roomId) {
      console.log("❌ fetchAssets (Details): No roomId provided");
      setAssets([]);
      return;
    }
    
    try {
      console.log("🔍 fetchAssets (Details): Fetching assets for roomId:", roomId);
      setLoadingAssets(true);
      const res = await fetch(`${apiPath}/assets/${roomId}`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      console.log("📡 fetchAssets (Details): Response status:", res.status);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json(); // ApiResponse<List<AssetDto>>
      console.log("✅ fetchAssets (Details): Full response:", json);
      console.log("✅ fetchAssets (Details): Response.result:", json.result);
      console.log("✅ fetchAssets (Details): Assets array length:", (json.result || []).length);
      setAssets(json.result || []); // ✅ ใช้ json.result แทน json.data
    } catch (e) {
      console.error("❌ fetchAssets (Details): Failed to fetch assets:", e);
      setAssets([]);
    } finally {
      setLoadingAssets(false);
    }
  };

  // ✅ Issue options for Asset target (ตอนนี้เป็น hardcoded แต่จะเปลี่ยนเป็น dynamic)
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

  // ✅ ฟังก์ชันจัดการอัพโหลดรูปภาพ
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // ตรวจสอบประเภทไฟล์
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      showMessageError("กรุณาเลือกไฟล์รูปภาพ (JPEG, PNG, GIF)");
      return;
    }

    // ตรวจสอบขนาดไฟล์ (ไม่เกิน 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      showMessageError("ขนาดไฟล์ต้องไม่เกิน 5MB");
      return;
    }

    setSelectedImage(file);
  };

  const uploadImage = async () => {
    if (!selectedImage || !maintainId) {
      showMessageError("กรุณาเลือกรูปภาพก่อน");
      return;
    }

    try {
      setUploadingImage(true);
      
      const formData = new FormData();
      formData.append('file', selectedImage);
      
      const res = await fetch(`${apiPath}/maintain/${maintainId}/work-image`, {
        method: 'POST',
        credentials: 'include',
        body: formData,
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || `HTTP ${res.status}`);
      }
      
      const result = await res.json();
      
      // อัพเดต state ด้วย URL ที่ได้
      setWorkImageUrl(result.url);
      setSelectedImage(null);
      showMessageSave("อัพโหลดรูปภาพสำเร็จ");
      
    } catch (e) {
      console.error("Upload error:", e);
      showMessageError(`อัพโหลดล้มเหลว: ${e.message}`);
    } finally {
      setUploadingImage(false);
    }
  };

  const removeImage = () => {
    setSelectedImage(null);
    setWorkImageUrl("");
  };

  // ✅ ฟังก์ชัน cleanup modal backdrop
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

  const handleOpenEditModal = () => {
    console.log("🎯 Opening edit modal...");
    
    // ✅ Cleanup ก่อนเปิด modal ใหม่เพื่อป้องกัน conflict
    cleanupBackdrops();
    
    // ✅ รีเซ็ต form เมื่อเปิด modal
    if (data) {
      const targetType = data.targetType === 0 ? "asset" : "building";
      setForm({
        target: targetType,
        issueTitle: data.issueTitle ?? "",
        issueCategory: data.issueCategory ?? 0,
        issueDescription: data.issueDescription ?? "",
        requestDate: toDate(data.createDate) || "",
        maintainDate: toDate(data.scheduledDate) || "",
        completeDate: toDate(data.finishDate) || "",
        maintainType: data.maintainType ?? "fix",
        technician: data.technicianName ?? "",
        phone: data.technicianPhone ?? "",
        state: data.finishDate ? "Complete" : (data.scheduledDate ? "In Progress" : "Not Started"),
      });
      
      // ✅ Fetch assets if target is "asset"
      if (targetType === "asset" && data.roomId) {
        console.log("🎯 Modal opened with asset target, fetching assets for roomId:", data.roomId);
        fetchAssets(data.roomId);
      }
    }
    
    // ✅ รอ cleanup เสร็จแล้วค่อยเปิด modal
    setTimeout(() => {
      const modalElement = document.getElementById("editMaintainModal");
      if (modalElement) {
        const modal = new bootstrap.Modal(modalElement, {
          backdrop: 'static',
          keyboard: false
        });
        modal.show();
        console.log("✅ Edit modal opened");
      }
    }, 50);
  };

  useEffect(() => {
    cleanupBackdrops(); // ✅ Cleanup เมื่อ component mount
    if (!data) return;
    
    // ✅ Set form data
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
    
    // ✅ Set work image URL
    if (data.workImageUrl) {
      setWorkImageUrl(data.workImageUrl);
    }
  }, [data]);

  // ✅ Cleanup เมื่อ component unmount
  useEffect(() => {
    return () => {
      cleanupBackdrops();
    };
  }, []);

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
        // ✅ Fetch assets when target changes to "asset"
        if (value === "asset" && data?.roomId) {
          console.log("🎯 Target changed to asset, fetching assets for roomId:", data.roomId);
          fetchAssets(data.roomId);
        } else if (value === "building") {
          console.log("🏢 Target changed to building, clearing assets");
          setAssets([]);
        }
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
        workImageUrl: workImageUrl, // ✅ ส่ง URL รูปภาพ
      };

      const res = await fetch(`${apiPath}/maintain/update/${maintainId}`, {
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

      // ✅ ปิด modal อย่างสมบูรณ์พร้อม cleanup
      const modalElement = document.getElementById("editMaintainModal");
      if (modalElement) {
        const modalInstance = bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
        
        // ✅ ทำ cleanup ทันทีหลังปิด modal
        setTimeout(() => {
          cleanupBackdrops();
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
      const res = await fetch(`${apiPath}/maintain/${maintainId}`, {
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

  // ✅ PDF Download function
  const handleDownloadPdf = async () => {
    if (!maintainId) {
      showMessageError("ไม่พบข้อมูลงานซ่อมบำรุง");
      return;
    }
    
    try {
      const res = await fetch(`${apiPath}/maintain/${maintainId}/report-pdf`, {
        method: "GET",
        credentials: "include",
      });
      
      if (!res.ok) throw new Error("ไม่สามารถสร้างรายงาน PDF ได้");
      
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `maintenance-report-${maintainId}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (e) {
      showMessageError(`Download failed: ${e.message}`);
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

      const res = await fetch(`${apiPath}/maintain/update/${maintainId}`, {
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

      const res = await fetch(`${apiPath}/maintain/update/${maintainId}`, {
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
    <Layout title="Maintenance Details" icon="bi bi-wrench" notifications={0}>
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
                      onClick={handleOpenEditModal}
                      disabled={!data}
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
                          {workImageUrl && (
                            <div>
                              <span className="label">Work Evidence:</span>
                              <div className="mt-2">
                                <img
                                  src={`${apiPath}${workImageUrl}`}
                                  alt="Work Evidence"
                                  className="img-thumbnail"
                                  style={{ maxWidth: "200px", maxHeight: "150px", cursor: "pointer" }}
                                  onClick={() => {
                                    // เปิดรูปในแท็บใหม่
                                    window.open(`${apiPath}${workImageUrl}`, '_blank');
                                  }}
                                />
                                <div className="small text-muted mt-1">
                                  <i className="bi bi-camera me-1"></i>
                                  Click on picture for watch full picture
                                </div>
                              </div>
                            </div>
                          )}
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
                        name="issueTitle"
                        value={form.issueTitle}
                        onChange={onChange}
                        disabled={loadingAssets}
                        required
                      >
                        <option value="">
                          {loadingAssets 
                            ? "Loading assets..." 
                            : assets.length === 0 
                              ? "No assets in this room"
                              : "Select asset"
                          }
                        </option>
                        {assets.map((asset) => (
                          <option key={asset.assetId} value={asset.assetName}>
                            {asset.assetName} ({asset.assetGroupName})
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
                  
                  <div className="col-md-12">
                    <label className="form-label">Work Evidence Photo</label>
                    <div className="border rounded p-3">
                      {/* Current Image Display */}
                      {workImageUrl && (
                        <div className="mb-3">
                          <label className="small text-muted">Current Image:</label>
                          <div className="position-relative d-inline-block">
                            <img
                              src={`${apiPath}${workImageUrl}`}
                              alt="Current Work Evidence"
                              className="img-thumbnail me-2"
                              style={{ maxWidth: "120px", maxHeight: "90px" }}
                            />
                            <button
                              type="button"
                              className="btn btn-danger btn-sm position-absolute top-0 end-0"
                              style={{ transform: "translate(50%, -50%)" }}
                              onClick={removeImage}
                              title="Remove image"
                            >
                              <i className="bi bi-x"></i>
                            </button>
                          </div>
                        </div>
                      )}
                      
                      {/* File Input */}
                      <div className="input-group">
                        <input
                          type="file"
                          className="form-control"
                          accept="image/*"
                          onChange={handleImageChange}
                          disabled={uploadingImage}
                        />
                        <button
                          type="button"
                          className="btn btn-outline-primary"
                          onClick={uploadImage}
                          disabled={!selectedImage || uploadingImage || !maintainId}
                        >
                          {uploadingImage ? (
                            <>
                              <span className="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                              Uploading...
                            </>
                          ) : (
                            <>
                              <i className="bi bi-cloud-upload me-1"></i>
                              Upload
                            </>
                          )}
                        </button>
                      </div>
                      <div className="form-text">
                        <i className="bi bi-info-circle me-1"></i>
                        Supported formats: JPEG, PNG, GIF. Max size: 5MB
                      </div>
                    </div>
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
