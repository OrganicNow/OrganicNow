// src/pages/MaintenanceSchedule.jsx
import React, { useMemo, useState, useEffect } from "react";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import { pageSize as defaultPageSize } from "../config_variable";
import { useNotifications } from "../contexts/NotificationContext";
import { useToast } from "../contexts/ToastContext";
import * as bootstrap from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "../assets/css/fullcalendar.css";

import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import listPlugin from "@fullcalendar/list";

// ===== API base =====
const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

// ===== Helpers =====
const addMonthsISO = (isoDate, months) => {
    if (!isoDate) return "";
    const [y, m, d] = isoDate.split("-").map(Number);
    const dt = new Date(y, m - 1, d);
    dt.setMonth(dt.getMonth() + Number(months || 0));
    const yyyy = dt.getFullYear();
    const mm = String(dt.getMonth() + 1).padStart(2, "0");
    const dd = String(dt.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
};

// Convert date to LocalDateTime format for backend
const d2ldt = (d) => (d ? `${d}T00:00:00` : null);

// ล้างซาก Backdrop + class บน body (เผื่อ modal/offcanvas ค้าง)
const cleanupBackdrops = () => {
    // ลบทุก backdrop ที่ค้าง
    document.querySelectorAll(".modal-backdrop, .offcanvas-backdrop").forEach(el => el.remove());
    // เคลียร์ class/inline style ที่ Bootstrap ใส่ให้ body
    document.body.classList.remove("modal-open");
    document.body.style.removeProperty("padding-right");
};

// ===== Endpoints =====
const SCHEDULE_API = {
    LIST: `${API_BASE}/schedules`,                 // GET -> { result, assetGroupDropdown }
    CREATE: `${API_BASE}/schedules`,               // POST
    DELETE: (id) => `${API_BASE}/schedules/${id}`, // DELETE
};

// ===== Mapping: API -> แถวบนตาราง =====
function fromApi(item) {
    const rawScope = item.scheduleScope ?? item.scope;
    const scope = rawScope === 0 ? "Asset" : "Building";

    const lastDate = item.lastDoneDate ? String(item.lastDoneDate).slice(0, 10) : "";
    const cycle = Number(item.cycleMonth ?? 0);
    const nextDate = item.nextDueDate
        ? String(item.nextDueDate).slice(0, 10)
        : lastDate && cycle
            ? addMonthsISO(lastDate, cycle)
            : "";

    const title = item.scheduleTitle ?? "-";
    const description = item.scheduleDescription ?? item.description ?? "";

    return {
        id: item.id,
        scope, // "Asset" | "Building"
        title, // ใช้ในคอลัมน์ Title
        description,
        cycle,
        notify: Number(item.notifyBeforeDate ?? 0),
        lastDate,
        nextDate,
        assetGroupId: item.assetGroupId ?? null,
        assetGroupName: item.assetGroupName ?? null,
    };
}

// ===== Mapping: ฟอร์ม -> payload (8 ฟิลด์ตามสเปค) =====
function toCreatePayload(f) {
    const scopeNum  = Number(f.scope);      // "0"/"1" -> 0/1
    const cycleNum  = Number(f.cycle);
    const notifyNum = Number(f.notify);

    const lastISO = f.lastDate || "";
    const nextISO = lastISO && cycleNum ? addMonthsISO(lastISO, cycleNum) : "";

    return {
        scheduleScope: scopeNum,                                         // 0=Asset, 1=Building
        assetGroupId: scopeNum === 0 ? (Number(f.assetGroupId) || null) : null,
        cycleMonth: cycleNum,
        lastDoneDate: d2ldt(lastISO),                                    // "YYYY-MM-DDT00:00:00"
        nextDueDate: nextISO ? d2ldt(nextISO) : null,                    // "YYYY-MM-DDT00:00:00"
        notifyBeforeDate: notifyNum,
        scheduleTitle: (f.title || "").trim(),
        scheduleDescription: f.description ?? "",
    };
}

function MaintenanceSchedule() {
    // --------- DATA (จาก backend) ----------
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // notification context สำหรับ refresh
    const { refreshNotifications } = useNotifications();
    const { showMaintenanceCreated } = useToast();

    // assetGroupDropdown (จาก /schedules)
    const [assetOptions, setAssetOptions] = useState([]);
    const [assetLoading] = useState(false);
    const [assetError] = useState(null);

    // ===== โหลดตารางจาก /schedules (GET) =====
    const loadSchedules = async () => {
        try {
            setLoading(true);
            setError("");
            const res = await fetch(SCHEDULE_API.LIST, { credentials: "include" });
            if (!res.ok) throw new Error(await res.text());

            const json = await res.json();
            const list = Array.isArray(json?.result) ? json.result : [];
            const rows = list.map(fromApi);
            setSchedules(rows);

            if (Array.isArray(json?.assetGroupDropdown)) {
                const opts = json.assetGroupDropdown.map((x) => ({
                    id: x.id ?? x.groupId ?? x.code ?? String(Math.random()),
                    name: x.name ?? x.groupName ?? x.displayName ?? "Unnamed",
                }));
                setAssetOptions(opts);
            }
        } catch (e) {
            console.error(e);
            setError("โหลดตาราง Maintenance Schedule ไม่สำเร็จ");
            setSchedules([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSchedules();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // --------- TABLE CONTROLS ----------
    const [search, setSearch] = useState("");
    const [sortAsc, setSortAsc] = useState(true); // sort ตาม lastDate

    const [filters, setFilters] = useState({
        scope: "ALL",
        cycleMin: "",
        cycleMax: "",
        notifyMin: "",
        notifyMax: "",
        dateFrom: "", // lastDate from
        dateTo: "",   // lastDate to
        nextFrom: "",
        nextTo: "",
        assetGroupId: "",
    });

    // ===== สร้างรายการ (POST /schedules) =====
    const [saving, setSaving] = useState(false);
    const [newSch, setNewSch] = useState({
        scope: "",          // "0" | "1"
        assetGroupId: "",   // เมื่อ scope=0 (Asset)
        cycle: "",          // cycleMonth
        lastDate: new Date().toISOString().slice(0, 10),
        notify: "",         // notifyBeforeDate
        title: "",          // scheduleTitle
        description: "",    // scheduleDescription
    });

    // ====== VIEW MODAL (รายละเอียดอีเวนต์) ======
    const [viewEvent, setViewEvent] = useState(null);

    const openViewModal = (data) => {
        setViewEvent(data);
        const el = document.getElementById("viewScheduleModal");
        if (el) (bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el)).show();
    };

    const closeViewModal = () => {
        const el = document.getElementById("viewScheduleModal");
        if (el) (bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el)).hide();
        cleanupBackdrops();
    };

    const handleDeleteFromView = async () => {
        if (!viewEvent?.id) return;
        await deleteRow(viewEvent.id);
        closeViewModal();
        setViewEvent(null);
    };

    useEffect(() => {
        // ทุกครั้งที่ scope เปลี่ยน รีค่า assetGroupId
        setNewSch((prev) => ({
            ...prev,
            assetGroupId: "",
        }));
    }, [newSch.scope]);

    const validateNewSch = () => {
        if (newSch.scope === "") return "กรุณาเลือก Scope";
        if (newSch.scope === "0" && !newSch.assetGroupId) return "กรุณาเลือก Asset Group";
        if (!newSch.title.trim()) return "กรุณากรอก Title";
        if (!newSch.cycle || Number(newSch.cycle) < 1) return "Cycle ต้อง ≥ 1";
        if (newSch.notify === "" || Number(newSch.notify) < 0) return "Notify ต้อง ≥ 0";
        if (!newSch.lastDate) return "กรุณาเลือก Last date";
        return null;
    };

    const addSchedule = async () => {
        const payload = toCreatePayload(newSch);
        const res = await fetch(SCHEDULE_API.CREATE, {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!res.ok) {
            const msg = await res.text().catch(() => "");
            throw new Error(`HTTP ${res.status} ${msg || ""}`.trim());
        }
        
        const newSchedule = await res.json();
        await loadSchedules();
        
        // 🎯 แสดง toast เมื่อสร้าง schedule สำเร็จ
        showMaintenanceCreated({
            scheduleTitle: newSch.title
        });
        
        // 🔔 Refresh notifications หลังจากสร้าง schedule ใหม่
        setTimeout(() => {
            refreshNotifications();
        }, 1000); // รอ 1 วินาทีให้ backend สร้าง notification เสร็จก่อน
    };

    const clearFilters = () =>
        setFilters({
            scope: "ALL",
            cycleMin: "",
            cycleMax: "",
            notifyMin: "",
            notifyMax: "",
            dateFrom: "",
            dateTo: "",
            nextFrom: "",
            nextTo: "",
            assetGroupId: "",
        });

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        let rows = [...schedules];

        rows = rows.filter((r) => {
            if (filters.scope !== "ALL" && r.scope !== filters.scope) return false;
            if (filters.assetGroupId) {
                if (String(r.assetGroupId || "") !== String(filters.assetGroupId)) return false;
            }
            if (filters.cycleMin !== "" && r.cycle < Number(filters.cycleMin)) return false;
            if (filters.cycleMax !== "" && r.cycle > Number(filters.cycleMax)) return false;
            if (filters.notifyMin !== "" && r.notify < Number(filters.notifyMin)) return false;
            if (filters.notifyMax !== "" && r.notify > Number(filters.notifyMax)) return false;
            if (filters.dateFrom && r.lastDate && r.lastDate < filters.dateFrom) return false;
            if (filters.dateTo && r.lastDate && r.lastDate > filters.dateTo) return false;
            if (filters.nextFrom && r.nextDate && r.nextDate < filters.nextFrom) return false;
            if (filters.nextTo && r.nextDate && r.nextDate > filters.nextTo) return false;
            return true;
        });

        if (q) {
            rows = rows.filter(
                (r) =>
                    r.scope.toLowerCase().includes(q) ||
                    r.title.toLowerCase().includes(q) ||
                    (r.description || "").toLowerCase().includes(q) ||
                    String(r.cycle).includes(q) ||
                    String(r.notify).includes(q) ||
                    r.lastDate.includes(q) ||
                    r.nextDate.includes(q)
            );
        }

        rows.sort((a, b) =>
            sortAsc ? a.lastDate.localeCompare(b.lastDate) : b.lastDate.localeCompare(a.lastDate)
        );
        return rows;
    }, [schedules, filters, search, sortAsc]);

    const calendarEvents = useMemo(() => {
        return filtered
            .filter(r => r.nextDate || r.lastDate)
            .map(r => {
                const date = r.nextDate || r.lastDate;
                const isAsset = r.scope === "Asset";
                return {
                    id: String(r.id),
                    title: `${r.title}${r.assetGroupName ? " · " + r.assetGroupName : ""}`,
                    start: date,
                    allDay: true,
                    extendedProps: {
                        scope: r.scope,
                        lastDate: r.lastDate,
                        nextDate: r.nextDate,
                        description: r.description,
                        assetGroupName: r.assetGroupName,
                    },
                    backgroundColor: isAsset ? "#02BEA3" : undefined,
                    borderColor: isAsset ? "#02BEA3" : undefined,
                };
            });
    }, [filtered]);

    // --------- PAGINATION ----------
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(defaultPageSize || 10);
    const totalRecords = filtered.length;
    const totalPages = Math.max(1, Math.ceil(totalRecords / pageSize));

    useEffect(() => {
        setCurrentPage(1);
    }, [search, sortAsc, pageSize, filters]);

    const pageRows = useMemo(() => {
        const start = (currentPage - 1) * pageSize;
        return filtered.slice(start, start + pageSize);
    }, [filtered, currentPage, pageSize]);

    // ===== ลบรายการ =====
    const deleteRow = async (rowId) => {
        if (!confirm("ลบรายการนี้ใช่หรือไม่?")) return;
        try {
            const res = await fetch(SCHEDULE_API.DELETE(rowId), {
                method: "DELETE",
                credentials: "include",
            });
            if (!res.ok) throw new Error(await res.text());
            await loadSchedules();
        } catch (e) {
            console.error(e);
            alert("ลบไม่สำเร็จ");
        }
    };

    const hasAnyFilter =
        filters.scope !== "ALL" ||
        filters.cycleMin !== "" ||
        filters.cycleMax !== "" ||
        filters.notifyMin !== "" ||
        filters.notifyMax !== "" ||
        !!filters.dateFrom ||
        !!filters.dateTo ||
        !!filters.nextFrom ||
        !!filters.nextTo ||
        !!filters.assetGroupId;

    const filterSummary = [];
    if (filters.scope !== "ALL") filterSummary.push(`Scope: ${filters.scope}`);
    if (filters.assetGroupId) {
        const found = assetOptions.find((a) => String(a.id) === String(filters.assetGroupId));
        filterSummary.push(`Group: ${found?.name || filters.assetGroupId}`);
    }
    if (filters.cycleMin !== "") filterSummary.push(`Cycle ≥ ${filters.cycleMin}`);
    if (filters.cycleMax !== "") filterSummary.push(`Cycle ≤ ${filters.cycleMax}`);
    if (filters.notifyMin !== "") filterSummary.push(`Notify ≥ ${filters.notifyMin}`);
    if (filters.notifyMax !== "") filterSummary.push(`Notify ≤ ${filters.notifyMax}`);
    if (filters.dateFrom) filterSummary.push(`Last ≥ ${filters.dateFrom}`);
    if (filters.dateTo) filterSummary.push(`Last ≤ ${filters.dateTo}`);
    if (filters.nextFrom) filterSummary.push(`Next ≥ ${filters.nextFrom}`);
    if (filters.nextTo) filterSummary.push(`Next ≤ ${filters.nextTo}`);

    return (
        <Layout title="Maintenance Schedule" icon="bi bi-alarm" notifications={0}>
            <div className="container-fluid">
                <div className="row min-vh-100">
                    <div className="col-lg-11 p-4">
                        {/* Toolbar */}
                        <div className="toolbar-wrapper card border-0 bg-white">
                            <div className="card-header bg-white border-0 rounded-3">
                                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                                    <div className="d-flex align-items-center gap-3">
                                        <button
                                            className="btn btn-link tm-link p-0"
                                            data-bs-toggle="offcanvas"
                                            data-bs-target="#scheduleFilterCanvas"
                                        >
                                            <i className="bi bi-filter me-1"></i> Filter
                                            {hasAnyFilter && <span className="badge bg-primary ms-2">●</span>}
                                        </button>

                                        <button
                                            className="btn btn-link tm-link p-0"
                                            onClick={() => setSortAsc((s) => !s)}
                                        >
                                            <i className="bi bi-arrow-down-up me-1"></i>
                                            Sort
                                        </button>

                                        <div className="input-group tm-search">
                      <span className="input-group-text bg-white border-end-0">
                        <i className="bi bi-search"></i>
                      </span>
                                            <input
                                                type="text"
                                                className="form-control border-start-0"
                                                placeholder="Search schedule"
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
                                            data-bs-target="#createScheduleModal"
                                        >
                                            <i className="bi bi-plus-lg me-1"></i> Create Schedule
                                        </button>
                                    </div>
                                </div>

                                <div className={`collapse ${hasAnyFilter ? "show" : ""}`}>
                                    <div className="pt-2 d-flex flex-wrap gap-2">
                                        {filterSummary.map((txt, idx) => (
                                            <span key={idx} className="badge bg-light text-dark border">
                        {txt}
                      </span>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Calendar */}
                        <div className="table-wrapper mt-3">
                            {error && <div className="alert alert-danger">{error}</div>}
                            <div className="mt-3">
                                {error && <div className="alert alert-danger">{error}</div>}
                                <FullCalendar
                                    className="custom-calendar"
                                    plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin, listPlugin]}
                                    initialView="dayGridMonth"
                                    headerToolbar={{
                                        left: "prev,next today",
                                        center: "title",
                                        right: "dayGridMonth,listWeek",
                                    }}
                                    titleFormat={{ year: "numeric", month: "long" }}
                                    locale={"en"}           // ✅ ต้องใส่ใน { }
                                    firstDay={1}
                                    height="auto"
                                    dayMaxEventRows={3}
                                    eventOrder="start,-duration,allDay,title"
                                    events={calendarEvents}
                                    eventContent={(arg) => {
                                        // ✅ ปรับ style ของ event ตาม design ใหม่
                                        const { title } = arg.event;
                                        return {
                                            html: `
        <div class="fc-event-card">
          <div class="fc-event-title">${title}</div>
        </div>
      `,
                                        };
                                    }}
                                    dateClick={(arg) => {
                                        setNewSch((p) => ({ ...p, lastDate: arg.dateStr }));
                                        const modalEl = document.getElementById("createScheduleModal");
                                        if (modalEl) {
                                            (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).show();
                                        }
                                    }}
                                    eventClick={(info) => {
                                        const ev = info.event;
                                        const xp = ev.extendedProps || {};
                                        openViewModal({
                                            id: ev.id,
                                            title: ev.title,
                                            scope: xp.scope || "-",
                                            lastDate: xp.lastDate || "-",
                                            nextDate: xp.nextDate || "-",
                                            assetGroupName: xp.assetGroupName || "-",
                                            description: xp.description || "",
                                        });
                                    }}
                                    eventMouseEnter={(info) => {
                                        const xp = info.event.extendedProps || {};
                                        const tip =
                                            (xp.description ? xp.description + "\n" : "") +
                                            (xp.nextDate ? "Next: " + xp.nextDate : "");
                                        if (tip) info.el.setAttribute("title", tip);
                                    }}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* View Schedule Modal */}
            <Modal id="viewScheduleModal" title="Schedule Detail" icon="bi bi-calendar-event" size="modal-md">
                {viewEvent ? (
                    <div className="row g-3">
                        <div className="col-12">
                            <div className="d-flex align-items-center justify-content-between">
                                <h5 className="mb-0">{viewEvent.title}</h5>
                                <span
                                    className={`badge ${viewEvent.scope === "Asset" ? "bg-success" : "bg-primary"}`}
                                    title="Scope"
                                >
            {viewEvent.scope}
          </span>
                            </div>
                            {viewEvent.assetGroupName && viewEvent.assetGroupName !== "-" && (
                                <div className="text-muted mt-1">Group: {viewEvent.assetGroupName}</div>
                            )}
                        </div>

                        <div className="col-md-6">
                            <label className="form-label text-muted mb-0">Last date</label>
                            <div className="fw-semibold">{viewEvent.lastDate}</div>
                        </div>
                        <div className="col-md-6">
                            <label className="form-label text-muted mb-0">Next date</label>
                            <div className="fw-semibold">{viewEvent.nextDate}</div>
                        </div>

                        <div className="col-12">
                            <label className="form-label text-muted mb-0">Description</label>
                            <div className="border rounded p-2 bg-light" style={{ minHeight: 60 }}>
                                {viewEvent.description || <span className="text-muted">-</span>}
                            </div>
                        </div>

                        <div className="col-12 d-flex justify-content-between pt-2">
                            <button type="button" className="btn btn-outline-secondary" onClick={closeViewModal}>
                                Close
                            </button>
                            <button type="button" className="btn btn-danger" onClick={handleDeleteFromView}>
                                <i className="bi bi-trash me-1" /> Delete
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="text-center text-muted py-4">Loading...</div>
                )}
            </Modal>

            {/* Create Schedule Modal */}
            <Modal id="createScheduleModal" title="Create Schedule" icon="bi bi-alarm" size="modal-lg">
                <form
                    onSubmit={async (e) => {
                        e.preventDefault();
                        const err = validateNewSch();
                        if (err) { alert(err); return; }
                        try {
                            setSaving(true);
                            await addSchedule(); // POST ไป backend + reload ตาราง

                            const modalEl = document.getElementById("createScheduleModal");
                            if (modalEl) {
                                const inst = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                                inst.hide();
                            }
                            cleanupBackdrops();

                            setNewSch({
                                scope: "",
                                assetGroupId: "",
                                cycle: "",
                                lastDate: new Date().toISOString().slice(0, 10),
                                notify: "",
                                title: "",
                                description: "",
                            });
                        } catch (e2) {
                            console.error(e2);
                            alert("บันทึกไม่สำเร็จ");
                        } finally {
                            setSaving(false);
                        }
                    }}
                >

                <div className="row g-3">
                        {/* 1) scheduleScope */}
                        <div className="col-md-6">
                            <label className="form-label">Scope</label>
                            <select
                                className="form-select"
                                value={newSch.scope}
                                onChange={(e) => setNewSch((p) => ({ ...p, scope: e.target.value }))}
                                required
                            >
                                <option value="">Select Scope</option>
                                <option value="0">Asset</option>
                                <option value="1">Building</option>
                            </select>
                        </div>

                        {/* 2) assetGroupId (เฉพาะเมื่อ scope=0) */}
                        <div className="col-md-6">
                            <label className="form-label">Asset Group</label>
                            <select
                                className="form-select"
                                value={newSch.assetGroupId}
                                onChange={(e) =>
                                    setNewSch((p) => ({ ...p, assetGroupId: e.target.value }))
                                }
                                disabled={newSch.scope !== "0"}
                                required={newSch.scope === "0"}
                            >
                                <option value="">
                                    {newSch.scope === "0"
                                        ? "Select Asset Group"
                                        : " "}
                                </option>
                                {assetOptions.map((a) => (
                                    <option key={a.id} value={a.id}>
                                        {a.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* 3) cycleMonth */}
                        <div className="col-md-4">
                            <label className="form-label">Cycle (months)</label>
                            <input
                                type="number"
                                className="form-control"
                                placeholder="e.g. 6"
                                value={newSch.cycle}
                                min={1}
                                onChange={(e) => setNewSch((p) => ({ ...p, cycle: Number(e.target.value) }))}
                                required
                            />
                        </div>

                        {/* 4) lastDoneDate */}
                        <div className="col-md-4">
                            <label className="form-label">Last date</label>
                            <input
                                type="date"
                                className="form-control"
                                value={newSch.lastDate}
                                onChange={(e) => setNewSch((p) => ({ ...p, lastDate: e.target.value }))}
                                required
                            />
                        </div>

                        {/* 5) nextDueDate (auto + disabled) */}
                        <div className="col-md-4">
                            <label className="form-label">Next date (auto)</label>
                            <input
                                type="date"
                                className="form-control"
                                value={
                                    newSch.lastDate && newSch.cycle
                                        ? addMonthsISO(newSch.lastDate, newSch.cycle)
                                        : ""
                                }
                                disabled
                                readOnly
                            />
                        </div>

                        {/* 6) notifyBeforeDate */}
                        <div className="col-md-4">
                            <label className="form-label">Notify (days)</label>
                            <input
                                type="number"
                                className="form-control"
                                placeholder="e.g. 7"
                                value={newSch.notify}
                                min={0}
                                onChange={(e) => setNewSch((p) => ({ ...p, notify: Number(e.target.value) }))}
                                required
                            />
                        </div>

                        {/* 7) scheduleTitle */}
                        <div className="col-md-8">
                            <label className="form-label">Title</label>
                            <input
                                type="text"
                                className="form-control"
                                placeholder="หัวข้อ เช่น ตรวจแอร์ / ตรวจสภาพห้อง"
                                value={newSch.title}
                                onChange={(e) => setNewSch((p) => ({ ...p, title: e.target.value }))}
                                required
                            />
                        </div>

                        {/* 8) scheduleDescription */}
                        <div className="col-md-12">
                            <label className="form-label">Description</label>
                            <textarea
                                className="form-control"
                                rows={3}
                                placeholder="รายละเอียดงานตรวจ/ซ่อม"
                                value={newSch.description}
                                onChange={(e) => setNewSch((p) => ({ ...p, description: e.target.value }))}
                            />
                        </div>

                        <div className="col-12 d-flex justify-content-center gap-3 pt-3 pb-3">
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                data-bs-dismiss="modal"
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
                    <span
                        className="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true"
                    ></span>
                                        Saving...
                                    </>
                                ) : (
                                    "Save"
                                )}
                            </button>
                        </div>
                    </div>
                </form>
            </Modal>

            {/* Filters Offcanvas */}
            <div
                className="offcanvas offcanvas-end"
                tabIndex="-1"
                id="scheduleFilterCanvas"
                aria-labelledby="scheduleFilterCanvasLabel"
            >
                <div className="offcanvas-header">
                    <h5 id="scheduleFilterCanvasLabel" className="mb-0">
                        <i className="bi bi-filter me-2"></i>Filters
                    </h5>
                    <button type="button" className="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
                </div>

                <div className="offcanvas-body">
                    <div className="row g-3">
                        <div className="col-12">
                            <label className="form-label">Scope</label>
                            <select
                                className="form-select"
                                value={filters.scope}
                                onChange={(e) => setFilters((f) => ({ ...f, scope: e.target.value }))}
                            >
                                <option value="ALL">All</option>
                                <option value="Asset">Asset</option>
                                <option value="Building">Building</option>
                            </select>
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Asset Group</label>
                            <select
                                className="form-select"
                                value={filters.assetGroupId}
                                onChange={(e) => setFilters((f) => ({ ...f, assetGroupId: e.target.value }))}
                            >
                                <option value="">All</option>
                                {assetOptions.map((a) => (
                                    <option key={a.id} value={a.id}>{a.name}</option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Cycle min</label>
                            <input
                                type="number"
                                className="form-control"
                                value={filters.cycleMin}
                                onChange={(e) => setFilters((f) => ({ ...f, cycleMin: e.target.value }))}
                                placeholder="e.g. 3"
                            />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Cycle max</label>
                            <input
                                type="number"
                                className="form-control"
                                value={filters.cycleMax}
                                onChange={(e) => setFilters((f) => ({ ...f, cycleMax: e.target.value }))}
                                placeholder="e.g. 12"
                            />
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Notify min</label>
                            <input
                                type="number"
                                className="form-control"
                                value={filters.notifyMin}
                                onChange={(e) => setFilters((f) => ({ ...f, notifyMin: e.target.value }))}
                                placeholder="e.g. 3"
                            />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Notify max</label>
                            <input
                                type="number"
                                className="form-control"
                                value={filters.notifyMax}
                                onChange={(e) => setFilters((f) => ({ ...f, notifyMax: e.target.value }))}
                                placeholder="e.g. 14"
                            />
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Last date from</label>
                            <input
                                type="date"
                                className="form-control"
                                value={filters.dateFrom}
                                onChange={(e) => setFilters((f) => ({ ...f, dateFrom: e.target.value }))}
                            />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Last date to</label>
                            <input
                                type="date"
                                className="form-control"
                                value={filters.dateTo}
                                onChange={(e) => setFilters((f) => ({ ...f, dateTo: e.target.value }))}
                            />
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Next date from</label>
                            <input
                                type="date"
                                className="form-control"
                                value={filters.nextFrom}
                                onChange={(e) => setFilters((f) => ({ ...f, nextFrom: e.target.value }))}
                            />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Next date to</label>
                            <input
                                type="date"
                                className="form-control"
                                value={filters.nextTo}
                                onChange={(e) => setFilters((f) => ({ ...f, nextTo: e.target.value }))}
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

export default MaintenanceSchedule;
