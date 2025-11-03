import React, { useMemo, useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import { useToast } from "../component/Toast.jsx";
import { pageSize as defaultPageSize } from "../config_variable";
import * as bootstrap from "bootstrap"; // <-- ใช้ตัวนี้สำหรับควบคุมโมดัลแบบโปรแกรม
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

// ===== map helpers =====
const ISSUE_MAP = {
  air: { cat: 3, label: "Air conditioner" }, // เครื่องใช้/เฟอร์นิเจอร์
  light: { cat: 1, label: "Light" },         // ไฟฟ้า
  wall: { cat: 0, label: "Wall" },           // โครงสร้าง
  plumbing: { cat: 2, label: "Plumbing" },   // ประปา
};

// yyyy-mm-dd -> yyyy-mm-ddTHH:MM:SS
const d2ldt = (d) => (d ? `${d}T00:00:00` : null);

function MaintenanceRequest() {
  const navigate = useNavigate();
  const location = useLocation();
  const { showSuccess, showError } = useToast();

  // ✅ Room data from backend
  const [rooms, setRooms] = useState([]);
  
  // สำหรับ dropdown ห้อง (ใช้ข้อมูลจาก backend + fallback)
  const roomsByFloor = useMemo(() => {
    if (!rooms || rooms.length === 0) {
      // Fallback data
      return {
        "1": ["101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112"],
        "2": ["201", "202", "203", "204", "205", "206", "207", "208", "209", "210", "211", "212"]
      };
    }

    const result = {};
    rooms.forEach(room => {
      const floor = String(room.roomFloor);
      if (!result[floor]) result[floor] = [];
      result[floor].push(String(room.roomNumber));
    });
    return result;
  }, [rooms]);

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
        setRooms([]);
      }
    } catch (e) {
      console.error("Failed to fetch rooms:", e);
      setRooms([]);
    }
  };

  // ---------------- Pagination ----------------
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [pageSize, setPageSize] = useState(defaultPageSize || 12);

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) setCurrentPage(page);
  };
  const handlePageSizeChange = (size) => {
    const n = Number(size) || 12;
    setPageSize(n);
    setCurrentPage(1);
  };

  // ---------------- Table data ----------------
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const fetchData = async () => {
    try {
      setLoading(true);
      setErr("");
      const res = await fetch(`${API_BASE}/maintain/list`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json(); // List<MaintainDto>

      const mapped = (json || []).map((m) => ({
        id: m.id,
        room: m.roomNumber ?? "-",
        floor: (m.roomFloor ?? "").toString(),
        target: m.targetType === 0 ? "Asset" : "Building",
        issue: m.issueTitle ?? "-",
        maintainType: m.maintainType ?? "-", // ✅ ดึงจาก backend
        requestDate: (m.createDate || "").slice(0, 10),
        maintainDate: m.scheduledDate ? m.scheduledDate.slice(0, 10) : "-",
        completeDate: m.finishDate ? m.finishDate.slice(0, 10) : "-",
        state: m.finishDate ? "Complete" : "Not Started",
      }));

      setRows(mapped);
      setTotalRecords(mapped.length);
      setTotalPages(Math.max(1, Math.ceil(mapped.length / pageSize)));
      setCurrentPage(1);
    } catch (e) {
      console.error(e);
      setErr("Failed to load maintenance list.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    fetchRooms(); // ✅ Load rooms for dropdowns
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ------------- Toolbar: selection + search -------------
  const [selected, setSelected] = useState([]);
  const isAllSelected = rows.length > 0 && selected.length === rows.length;

  const [search, setSearch] = useState("");

  const filteredRows = useMemo(() => {
    const kw = search.trim().toLowerCase();
    const list = rows.filter((r) => {
      if (!kw) return true;
      return (
        r.room.toLowerCase().includes(kw) ||
        r.floor.toLowerCase().includes(kw) ||
        r.issue.toLowerCase().includes(kw) ||
        r.target.toLowerCase().includes(kw) ||
        r.state.toLowerCase().includes(kw)
      );
    });
    setTotalRecords(list.length);
    setTotalPages(Math.max(1, Math.ceil(list.length / pageSize)));
    return list;
  }, [rows, search, pageSize]);

  const pageStart = (currentPage - 1) * pageSize;
  const pageRows = filteredRows.slice(pageStart, pageStart + pageSize);

  const toggleRow = (id) =>
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  const toggleAll = () =>
    setSelected((prev) => (prev.length === rows.length ? [] : rows.map((r) => r.id)));

  const removeRow = async (row) => {
    if (!confirm(`Delete request #${row.id}?`)) return;
    try {
      const res = await fetch(`${API_BASE}/maintain/${row.id}`, {
        method: "DELETE",
        credentials: "include",
      });
      if (!res.ok) throw new Error(await res.text());
      await fetchData();
      showSuccess("✅ ลบ Maintenance Request สำเร็จ!");
    } catch (e) {
      showError(`❌ ลบ Maintenance Request ไม่สำเร็จ: ${e.message}`);
    }
  };

  // ---------------- Create Request (modal form) ----------------
  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState({
    floor: "",
    room: "",
    target: "",        // 'asset' | 'building'
    issue: "",         // 'air' | 'light' | 'wall' | 'plumbing'
    requestDate: "",
    maintainDate: "",
    completeDate: "",
    state: "Not Started",
    // UI only
    maintainType: "",
    technician: "",
    phone: "",
  });

  // Handle floor change and reset room selection
  const handleFloorChange = (selectedFloor) => {
    setForm(prev => ({ 
      ...prev, 
      floor: selectedFloor, 
      room: "" // Reset room when floor changes
    }));
  };

  // Get available floors from roomsByFloor
  const availableFloors = Object.keys(roomsByFloor).sort();
  
  // Get available rooms for selected floor
  const availableRooms = form.floor ? (roomsByFloor[form.floor] || []) : [];

  const onFormChange = (e) => {
    const { name, value } = e.target;
    setForm((s) => ({
      ...s,
      [name]: value,
      ...(name === "floor" ? { room: "" } : {}),
      ...(name === "target" ? { issue: "" } : {}), // ✅ Clear issue when target changes
      ...(name === "state" && value !== "Complete" ? { completeDate: "" } : {}),
    }));
  };

  const isFormValid = useMemo(() => {
    const valid = form.room &&
      form.target &&
      form.issue &&
      form.requestDate;
    
    
    return valid;
  }, [form.room, form.target, form.issue, form.requestDate]);

  const resetForm = () =>
    setForm({
      floor: "",
      room: "",
      target: "",
      issue: "",
      requestDate: "",
      maintainDate: "",
      completeDate: "",
      state: "Not Started",
      maintainType: "",
      technician: "",
      phone: "",
    });

  // ปิด modal แบบ programmatic (ไม่พึ่ง window.bootstrap)
  const closeModal = () => {
    const el = document.getElementById("requestModal");
    if (!el) return;
    const inst = bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el);
    el.addEventListener(
      "hidden.bs.modal",
      () => {
        try { inst.dispose(); } catch {}
        document.querySelectorAll(".modal-backdrop").forEach((n) => n.remove());
        document.body.classList.remove("modal-open");
        document.body.style.removeProperty("paddingRight");
        document.body.style.removeProperty("overflow");
      },
      { once: true }
    );
    inst.hide();
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!isFormValid) return;

    try {
      setSaving(true);

      const issueMeta = ISSUE_MAP[form.issue] ?? { cat: 5, label: form.issue };
      const payload = {
        targetType: form.target === "asset" ? 0 : 1,
        roomId: null,
        roomNumber: form.room, // ส่งเลขห้องได้เลย Service จะ resolve ให้
        roomAssetId: null,

        issueCategory: issueMeta.cat,
        issueTitle: issueMeta.label,
        issueDescription: "",

        createDate: d2ldt(form.requestDate),
        scheduledDate: d2ldt(form.maintainDate),
        finishDate: form.state === "Complete" && form.completeDate ? d2ldt(form.completeDate) : null,
        
        // ✅ เพิ่มฟิลด์ใหม่
        maintainType: form.maintainType,
        technicianName: form.technician,
        technicianPhone: form.phone,
      };

      const res = await fetch(`${API_BASE}/maintain/create`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      
      if (!res.ok) {
        const errorText = await res.text();
        console.error("❌ Backend error:", errorText);
        throw new Error(errorText);
      }

      const result = await res.json();
      // ✅ รอ 500ms ก่อน fetch ใหม่เพื่อให้ database commit เสร็จ
      await new Promise(resolve => setTimeout(resolve, 500));
      await fetchData();
      
      resetForm();
      closeModal(); // ปิดโมดัลหลังบันทึกสำเร็จ
      showSuccess("✅ สร้าง Maintenance Request สำเร็จ!");
      
    } catch (e2) {
      console.error("❌ Create failed:", e2);
      showError(`❌ สร้าง Maintenance Request ไม่สำเร็จ: ${e2.message}`);
    } finally {
      setSaving(false);
    }
  };

  // >>> ไปหน้า Details (โหลดตัวจริงในหน้านั้น)
  const viewRow = (row) => {
    navigate("/maintenancedetails", { state: { id: row.id, from: location.pathname } });
  };

  const StateBadge = ({ state }) => {
    const complete = (state || "").toLowerCase() === "complete";
    return (
      <span className={`badge rounded-pill ${complete ? "bg-success" : "bg-secondary-subtle text-secondary"}`}>
        {complete ? "Complete" : "Not Started"}
      </span>
    );
  };

  return (
    <Layout title="Maintenance Request" icon="pi pi-wrench" notifications={0}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          {/* Main */}
          <div className="col-lg-11">
            {/* Toolbar */}
            <div className="toolbar-wrapper card border-0 bg-white">
              <div className="card-header bg-white border-0 rounded-3">
                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                  <div className="d-flex align-items-center gap-3">
                    <button className="btn btn-link tm-link p-0" onClick={fetchData} disabled={loading}>
                      <i className={`bi ${loading ? "bi-arrow-repeat spin" : "bi-arrow-repeat"} me-1`} />
                      Refresh
                    </button>
                    <div className="input-group tm-search">
                      <span className="input-group-text bg-white border-end-0">
                        <i className="bi bi-search" />
                      </span>
                      <input
                        type="text"
                        className="form-control border-start-0"
                        placeholder="Search"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                      />
                    </div>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#requestModal"
                    >
                      <i className="bi bi-plus-lg me-1" /> Create Request
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Error */}
            {err && <div className="alert alert-danger my-3">{err}</div>}

            {/* Table */}
            <div className="table-wrapper card border-0 bg-white shadow-sm overflow-hidden mt-3">
              <div className="card-body p-0">
                <div className="table-responsive">
                  <table className="table text-nowrap align-middle tm-left mb-0">
                    <thead className="header-color">
                      <tr>
                        <th className="text-center align-middle header-color">
                          <input 
                            type="checkbox" 
                            checked={isAllSelected} 
                            onChange={toggleAll}
                            aria-label="Select all"
                          />
                        </th>
                        <th>Order</th>
                        <th>Room</th>
                        <th>Floor</th>
                        <th>Target</th>
                        <th>Issue</th>
                        <th>Maintain Type</th>
                        <th>Request date</th>
                        <th>Maintain date</th>
                        <th>Complete date</th>
                        <th>State</th>
                        <th>Action</th>
                      </tr>
                    </thead>

                    <tbody>
                      {loading ? (
                        <tr><td colSpan="12" className="text-center">Loading...</td></tr>
                      ) : pageRows.length ? (
                        pageRows.map((row, index) => (
                          <tr key={row.id}>
                            <td className="text-center align-middle">
                              <input 
                                type="checkbox" 
                                checked={selected.includes(row.id)} 
                                onChange={() => toggleRow(row.id)}
                                aria-label={`Select row ${row.id}`}
                              />
                            </td>
                            <td>{pageStart + index + 1}</td>
                            <td>{row.room}</td>
                            <td>{row.floor}</td>
                            <td>{row.target}</td>
                            <td>{row.issue}</td>
                            <td>{row.maintainType || "-"}</td>
                            <td>{row.requestDate}</td>
                            <td>{row.maintainDate}</td>
                            <td>{row.completeDate}</td>
                            <td><StateBadge state={row.state} /></td>
                            <td>
                              <button
                                className="btn btn-sm form-Button-Edit me-1"
                                onClick={() => viewRow(row)}
                                title="View / Edit"
                              >
                                <i className="bi bi-eye-fill" />
                              </button>
                              <button
                                className="btn btn-sm form-Button-Del"
                                onClick={() => removeRow(row)}
                                title="Delete"
                              >
                                <i className="bi bi-trash-fill" />
                              </button>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan="12" className="text-center">Data Not Found</td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            {/* Pagination */}
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
              totalRecords={totalRecords}
              onPageSizeChange={handlePageSizeChange}
            />

            {/* Create Request Modal */}
            <Modal
              id="requestModal"
              title="Repair Add"
              icon="pi pi-wrench"
              size="modal-xl"
              scrollable="modal-dialog-scrollable"
            >
              <form onSubmit={handleCreate}>
                <div className="row g-4">
                  {/* Room Information */}
                  <div className="col-12">
                    <h6 className="text-muted mb-2">Room Information</h6>
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label">Floor</label>
                        <select
                          className="form-select"
                          value={form.floor}
                          onChange={(e) => handleFloorChange(e.target.value)}
                        >
                          <option value="">Select Floor</option>
                          {availableFloors.map(floor => (
                            <option key={floor} value={floor}>{floor}</option>
                          ))}
                        </select>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label">Room</label>
                        <select
                          className="form-select"
                          value={form.room}
                          onChange={(e) => setForm(prev => ({ ...prev, room: e.target.value }))}
                          disabled={!form.floor}
                        >
                          <option value="">
                            {form.floor ? "Select Room" : "Select Room"}
                          </option>
                          {availableRooms.map(room => (
                            <option key={room} value={room}>{room}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>

                  {/* Repair Information */}
                  <div className="col-12">
                    <h6 className="text-muted mb-2">Repair Information</h6>
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label">Target</label>
                        <select
                          name="target"
                          className="form-select"
                          value={form.target}
                          onChange={onFormChange}
                        >
                          <option value="">Select Target</option>
                          <option value="asset">Asset</option>
                          <option value="building">Building</option>
                        </select>
                      </div>

                      <div className="col-md-6">
                        <label className="form-label">Issue</label>
                        {form.target === "building" ? (
                          <input
                            type="text"
                            className="form-control"
                            name="issue"
                            value={form.issue}
                            onChange={onFormChange}
                            placeholder="Enter building issue"
                          />
                        ) : (
                          <select
                            name="issue"
                            className="form-select"
                            value={form.issue}
                            onChange={onFormChange}
                          >
                            <option value="">Select Issue</option>
                            <option value="air">Air conditioner</option>
                            <option value="light">Light</option>
                            <option value="wall">Wall</option>
                            <option value="plumbing">Plumbing</option>
                          </select>
                        )}
                      </div>

                      <div className="col-md-6">
                        <label className="form-label">Maintain type</label>
                        <select
                          name="maintainType"
                          className="form-select"
                          value={form.maintainType}
                          onChange={onFormChange}
                        >
                          <option value="">Select Maintain type</option>
                          <option value="fix">Fix</option>
                          <option value="shift">Shift</option>
                          <option value="replace">Replace</option>
                          <option value="maintenance">Maintenance</option>
                        </select>
                      </div>

                      <div className="col-md-6">
                        <label className="form-label">Request date</label>
                        <input
                          type="date"
                          className="form-control"
                          name="requestDate"
                          value={form.requestDate}
                          onChange={onFormChange}
                        />
                      </div>

                      <div className="col-md-6">
                        <label className="form-label">Maintain date</label>
                        <input
                          type="date"
                          className="form-control"
                          name="maintainDate"
                          value={form.maintainDate}
                          onChange={onFormChange}
                        />
                      </div>

                      <div className="col-md-6">
                        <label className="form-label">State</label>
                        <select
                          name="state"
                          className="form-select"
                          value={form.state}
                          onChange={onFormChange}
                        >
                          <option value="Not Started">Not Started</option>
                          <option value="Complete">Complete</option>
                        </select>
                      </div>

                      <div className="col-md-6">
                        <label className="form-label">Complete date</label>
                        <input
                          type="date"
                          className="form-control"
                          name="completeDate"
                          value={form.completeDate}
                          onChange={onFormChange}
                          disabled={form.state !== "Complete"}
                        />
                      </div>
                    </div>
                  </div>

                  {/* ✅ Technician Information */}
                  <div className="col-12">
                    <h6 className="text-muted mb-2">Technician Information</h6>
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label">Technician's name</label>
                        <input
                          type="text"
                          className="form-control"
                          name="technician"
                          value={form.technician}
                          onChange={onFormChange}
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
                          onChange={onFormChange}
                          placeholder="Add Phone Number"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div className="d-flex justify-content-center gap-3 mt-5">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    data-bs-dismiss="modal"      // <-- ให้ Bootstrap ปิด modal เองเมื่อกด Cancel
                    onClick={resetForm}
                  >
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={!isFormValid || saving}>
                    {saving ? "Saving..." : "Save"}
                  </button>
                  {/* {!isFormValid && (
                    <div className="mt-2">
                      <small className="text-danger">
                        Please fill required fields: 
                        {!form.room && " [Room]"}
                        {!form.target && " [Target]"}
                        {!form.issue && " [Issue]"}
                        {!form.requestDate && " [Request Date]"}
                      </small>
                    </div>
                  )} */}
                </div>
              </form>
            </Modal>
          </div>
          {/* /Main */}
        </div>
      </div>
    </Layout>
  );
}

export default MaintenanceRequest;
