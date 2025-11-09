// src/pages/MaintenanceSchedule.jsx
import React, { useMemo, useState, useEffect, useRef } from "react";
import Layout from "../component/layout";
import Modal from "../component/modal";
import { pageSize as defaultPageSize } from "../config_variable";
import { useNotifications } from "../contexts/NotificationContext";
import useMessage from "../component/useMessage";
import * as bootstrap from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import { useLocation } from "react-router-dom";

import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import listPlugin from "@fullcalendar/list";

import "../assets/css/fullcalendar.css";

// ===== API base =====
const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

// ===== Date Helpers (dd/mm/yyyy <-> ISO yyyy-MM-dd) =====
const pad2 = (n) => String(n).padStart(2, "0");

const toDmy = (dateObj) => {
    if (!dateObj || isNaN(dateObj)) return "";
    return `${pad2(dateObj.getDate())}/${pad2(dateObj.getMonth() + 1)}/${dateObj.getFullYear()}`;
};

const todayDmy = () => toDmy(new Date());

const isoToDmy = (iso) => {
    if (!iso) return "";
    const [y, m, d] = iso.split("-").map(Number);
    if (!y || !m || !d) return "";
    return `${pad2(d)}/${pad2(m)}/${y}`;
};

const dmyToIso = (dmy) => {
    if (!dmy) return "";
    const parts = dmy.split(/[\/\-.]/).map((s) => s.trim());
    if (parts.length < 3) return "";
    const [dd, mm, yyyy] = parts;
    if (!dd || !mm || !yyyy) return "";
    return `${yyyy}-${pad2(mm)}-${pad2(dd)}`;
};

// เพิ่มเดือนให้ "ISO" แล้วคืน "ISO"
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

// เพิ่มเดือนให้ "dd/mm/yyyy" แล้วคืน "dd/mm/yyyy"
const addMonthsDMY = (dmy, months) => {
    const iso = dmyToIso(dmy);
    if (!iso) return "";
    const nextIso = addMonthsISO(iso, months);
    return isoToDmy(nextIso);
};

// Convert to LocalDateTime (ISO) for backend
const d2ldt = (isoDate) => (isoDate ? `${isoDate}T00:00:00` : null);

// ล้างซาก Backdrop + class บน body (เผื่อ modal/offcanvas ค้าง)
const cleanupBackdrops = () => {
    document.querySelectorAll(".modal-backdrop, .offcanvas-backdrop").forEach((el) => el.remove());
    document.body.classList.remove("modal-open");
    document.body.style.removeProperty("padding-right");
};

// ===== Endpoints =====
const SCHEDULE_API = {
    LIST: `${API_BASE}/schedules`, // GET -> { result, assetGroupDropdown }
    CREATE: `${API_BASE}/schedules`, // POST
    DELETE: (id) => `${API_BASE}/schedules/${id}`, // DELETE
};

// ===== Mapping: API -> แถวบนตาราง (เก็บเป็น dd/mm/yyyy) =====
function fromApi(item) {
    const rawScope = item.scheduleScope ?? item.scope;
    const scope = rawScope === 0 ? "Asset" : "Building";

    const lastIso = item.lastDoneDate ? String(item.lastDoneDate).slice(0, 10) : "";
    const cycle = Number(item.cycleMonth ?? 0);
    const backendNextIso = item.nextDueDate ? String(item.nextDueDate).slice(0, 10) : "";

    // คำนวณ next ถ้า backend ไม่ให้มา
    const nextIso = backendNextIso || (lastIso && cycle ? addMonthsISO(lastIso, cycle) : "");

    const title = item.scheduleTitle ?? "-";
    const description = item.scheduleDescription ?? item.description ?? "";

    return {
        id: item.id,
        scope, // "Asset" | "Building"
        title,
        description,
        cycle,
        notify: Number(item.notifyBeforeDate ?? 0),
        lastDate: isoToDmy(lastIso), // << dd/mm/yyyy
        nextDate: isoToDmy(nextIso), // << dd/mm/yyyy
        assetGroupId: item.assetGroupId ?? null,
        assetGroupName: item.assetGroupName ?? null,
    };
}

// ===== Mapping: ฟอร์ม -> payload (ส่งเป็น ISO ให้ backend) =====
function toCreatePayload(f) {
    const scopeNum = Number(f.scope); // "0"/"1" -> 0/1
    const cycleNum = Number(f.cycle);
    const notifyNum = Number(f.notify);

    const lastIso = dmyToIso(f.lastDate || ""); // << แปลงจาก dd/mm/yyyy เป็น ISO
    const nextIso = lastIso && cycleNum ? addMonthsISO(lastIso, cycleNum) : "";

    return {
        scheduleScope: scopeNum, // 0=Asset, 1=Building
        assetGroupId: scopeNum === 0 ? (Number(f.assetGroupId) || null) : null,
        cycleMonth: cycleNum,
        lastDoneDate: d2ldt(lastIso), // "YYYY-MM-DDT00:00:00"
        nextDueDate: nextIso ? d2ldt(nextIso) : null, // "YYYY-MM-DDT00:00:00"
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
    const { showMessageSave, showMessageError, showMessageConfirmDelete } = useMessage();

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
        dateFrom: "", // dd/mm/yyyy
        dateTo: "", // dd/mm/yyyy
        nextFrom: "", // dd/mm/yyyy
        nextTo: "", // dd/mm/yyyy
        assetGroupId: "",
    });

    // ===== สร้างรายการ (POST /schedules) =====
    const [saving, setSaving] = useState(false);
    const [newSch, setNewSch] = useState({
        scope: "", // "0" | "1"
        assetGroupId: "", // เมื่อ scope=0 (Asset)
        cycle: "", // cycleMonth
        lastDate: todayDmy(), // << dd/mm/yyyy
        notify: "", // notifyBeforeDate
        title: "", // scheduleTitle
        description: "", // scheduleDescription
    });

    // ====== VIEW MODAL (รายละเอียดอีเวนต์) ======
    const [viewEvent, setViewEvent] = useState(null);

    const openViewModal = (data) => {
        setViewEvent(data); // data ต้องมี scheduleId
        const el = document.getElementById("viewScheduleModal");
        if (el) (bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el)).show();
    };

    const closeViewModal = () => {
        const el = document.getElementById("viewScheduleModal");
        if (el) (bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el)).hide();
        cleanupBackdrops();
    };

    const handleDeleteFromView = async () => {
        if (!viewEvent?.scheduleId) return;
        await deleteRow(viewEvent.scheduleId); // ลบด้วย scheduleId (ทั้งซีรีส์)
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
        // ตรวจรูปแบบ dd/mm/yyyy ง่าย ๆ
        if (!/^\d{2}\/\d{2}\/\d{4}$/.test(newSch.lastDate)) return "รูปแบบวันที่ต้องเป็น dd/mm/yyyy";
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

        await res.json();
        await loadSchedules();

        // 🎯 toast
        showMaintenanceCreated({ scheduleTitle: newSch.title });

        // 🔔 refresh notifications
        setTimeout(() => {
            refreshNotifications();
        }, 1000);
    };

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

            // เปรียบเทียบช่วงวันที่ ด้วยการแปลง dd/mm/yyyy -> ISO แล้วเปรียบเทียบ string
            const lastIso = dmyToIso(r.lastDate);
            const nextIso = dmyToIso(r.nextDate);
            const fromIso = dmyToIso(filters.dateFrom);
            const toIso = dmyToIso(filters.dateTo);
            const nFromIso = dmyToIso(filters.nextFrom);
            const nToIso = dmyToIso(filters.nextTo);

            if (filters.dateFrom && lastIso && lastIso < fromIso) return false;
            if (filters.dateTo && lastIso && lastIso > toIso) return false;
            if (filters.nextFrom && nextIso && nextIso < nFromIso) return false;
            if (filters.nextTo && nextIso && nextIso > nToIso) return false;

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
                    (r.lastDate || "").includes(q) ||
                    (r.nextDate || "").includes(q)
            );
        }

        // sort โดยใช้ ISO เพื่อให้ถูกต้องตามลำดับเวลา
        rows.sort((a, b) => {
            const ai = dmyToIso(a.lastDate) || "";
            const bi = dmyToIso(b.lastDate) || "";
            return sortAsc ? ai.localeCompare(bi) : bi.localeCompare(ai);
        });

        return rows;
    }, [schedules, filters, search, sortAsc]);

    // ===== สร้างอีเวนต์เฉพาะช่วงเวลาที่ FullCalendar ขอ (ไม่จำกัดปี) =====
    function buildEventsInRange(viewStart, viewEnd, rows) {
        const evs = [];

        // helper: เพิ่มเดือนแบบคืน Date ใหม่
        const addMonthsDate = (date, months) => {
            const d = new Date(date);
            d.setMonth(d.getMonth() + months);
            return d;
        };

        for (const r of rows) {
            if (!r.lastDate) continue;

            const cycle = Number(r.cycle || 0);
            const baseIso = dmyToIso(r.lastDate);
            if (!baseIso) continue;

            const isAsset = r.scope === "Asset";
            const title = r.title + (r.assetGroupName ? ` · ${r.assetGroupName}` : "");

            // 🎨 สีคงที่ต่อชุด (เหมือนเดิม)
            const baseColor = isAsset
                ? `hsl(${(r.id * 47 % 60) + 0}, 80%, 65%)`    // โทนร้อน 0–60°
                : `hsl(${(r.id * 67 % 100) + 200}, 80%, 65%)`; // โทนเย็น 200–300°

            // คำนวณ occurrence ตัวแรกที่อยู่ใน (หรือก่อน)ช่วงที่ขอ
            const firstDate = new Date(baseIso);

            if (cycle <= 0) {
                // ไม่มี cycle → แสดงแค่ครั้งเดียวถ้าอยู่ในช่วง
                if (firstDate <= viewEnd && firstDate >= viewStart) {
                    const nextDmy = ""; // ไม่มี next
                    evs.push({
                        id: `${r.id}-single`,
                        title,
                        start: baseIso,
                        allDay: true,
                        extendedProps: {
                            kind: "single",
                            scheduleId: r.id,
                            scope: r.scope,
                            occurrenceDate: r.lastDate, // dmy
                            nextDate: nextDmy,
                            description: r.description,
                            assetGroupName: r.assetGroupName,
                            cycle: r.cycle,
                        },
                        backgroundColor: baseColor,
                        borderColor: baseColor,
                    });
                }
                continue;
            }

            // มี cycle (เดือน)
            // กระโดดข้ามให้ถึง occurrence แรกที่ไม่ก่อน viewStart
            let cur = new Date(firstDate);

            if (cur < viewStart) {
                // คำนวณจำนวนเดือนต่าง (approx) เพื่อกระโดดทีเดียว
                const monthsDiff =
                    (viewStart.getFullYear() - cur.getFullYear()) * 12 +
                    (viewStart.getMonth() - cur.getMonth());

                // หา step ที่คร่อม viewStart
                const step = Math.max(0, Math.floor(monthsDiff / cycle));
                cur = addMonthsDate(cur, step * cycle);

                // ถ้ายังน้อยกว่า viewStart ให้ขยับอีกหนึ่งรอบ
                while (cur < viewStart) {
                    cur = addMonthsDate(cur, cycle);
                }
            }

            // ไล่สร้าง occurrence ไปจนพ้นช่วง viewEnd
            while (cur <= viewEnd) {
                const yyyy = cur.getFullYear();
                const mm = pad2(cur.getMonth() + 1);
                const dd = pad2(cur.getDate());
                const occIso = `${yyyy}-${mm}-${dd}`;
                const occDmy = isoToDmy(occIso);
                const occNextDmy = addMonthsDMY(occDmy, cycle);

                evs.push({
                    id: `${r.id}-${yyyy}${mm}${dd}`,
                    title,
                    start: occIso,
                    allDay: true,
                    extendedProps: {
                        kind: occIso === baseIso ? "last" : "recurring",
                        scheduleId: r.id,
                        scope: r.scope,
                        occurrenceDate: occDmy,
                        nextDate: occNextDmy,
                        description: r.description,
                        assetGroupName: r.assetGroupName,
                        cycle: r.cycle,
                    },
                    backgroundColor: baseColor,
                    borderColor: baseColor,
                });

                cur = addMonthsDate(cur, cycle);
            }
        }

        return evs;
    }

    // ===== ลบรายการ =====
    const deleteRow = async (rowId) => {
        const result = await showMessageConfirmDelete(`รายการ #${rowId}`);
        if (!result.isConfirmed) return;
        
        try {
            const res = await fetch(SCHEDULE_API.DELETE(rowId), {
                method: "DELETE",
                credentials: "include",
            });
            if (!res.ok) throw new Error(await res.text());
            await loadSchedules();
            showMessageSave();
        } catch (e) {
            console.error(e);
            showMessageError("ลบไม่สำเร็จ");
        }
    };

    const calendarRef = useRef(null);

    // === Handle deep-link from NotificationBell (?scheduleId=...&due=YYYY-MM-DD) ===
    const location = useLocation();

    useEffect(() => {
        // ต้องมีทั้ง scheduleId และ due (รูปแบบ YYYY-MM-DD)
        const params = new URLSearchParams(location.search || "");
        const sid = params.get("scheduleId");
        const dueIso = params.get("due"); // e.g. 2025-11-09

        if (!sid || !dueIso || !schedules?.length) return;

        const row = schedules.find((r) => String(r.id) === String(sid));
        if (!row) return;

        // เลื่อนไปยังวันที่ due
        const cal = calendarRef.current?.getApi();
        const dueDateObj = new Date(dueIso);
        if (cal && !isNaN(dueDateObj)) {
            cal.gotoDate(dueDateObj);
        }

        // เตรียมข้อมูลเปิดโมดัล เหมือน eventClick
        const occDmy = isoToDmy(dueIso);                          // dd/mm/yyyy
        const nextDmy = row.cycle ? addMonthsDMY(occDmy, row.cycle) : "";

        openViewModal({
            scheduleId: row.id,
            eventId: `${row.id}-${dueIso.replaceAll("-", "")}`,
            title: row.title + (row.assetGroupName ? ` · ${row.assetGroupName}` : ""),
            scope: row.scope || "-",
            lastDate: occDmy || "-",
            nextDate: nextDmy || "-",
            assetGroupName: row.assetGroupName || "-",
            description: row.description || "",
            cycle: row.cycle ?? "-",
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.search, schedules]);

    return (
        <Layout title="Maintenance Schedule" icon="bi bi-alarm" notifications={0}>
            <div className="container-fluid">
                <div className="row min-vh-100">
                    <div className="col-lg-11">
                        {/* Toolbar */}
                        <div className="toolbar-wrapper card border-0 bg-white mb-3">
                            <div className="card-header bg-white border-0 rounded-3">
                                <div className="d-flex justify-content-between align-items-center p-3 flex-wrap gap-2">
                                    {/* ปุ่มควบคุมเดือน */}
                                    <div className="d-flex align-items-center gap-2">
                                        <input
                                            type="month"
                                            className="form-control form-control-sm"
                                            onChange={(e) => {
                                                const date = new Date(e.target.value + "-01");
                                                if (!isNaN(date)) calendarRef.current?.getApi().gotoDate(date);
                                            }}
                                        />
                                    </div>

                                    {/* ปุ่มสร้าง */}
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
                        </div>

                        <div className="table-wrapper mt-3 p-4">
                            {error && <div className="alert alert-danger">{error}</div>}
                            {/* Calendar */}
                            <div className="custom-calendar mt-3">
                                <FullCalendar
                                    ref={calendarRef}
                                    plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin, listPlugin]}
                                    initialView="dayGridMonth"
                                    headerToolbar={{
                                        left: "prev,next today",
                                        center: "title",
                                        right: "dayGridMonth,listYear",
                                    }}
                                    titleFormat={{ year: "numeric", month: "long" }}
                                    locale={"en"}
                                    firstDay={1}
                                    height="auto"
                                    dayMaxEventRows={3}
                                    eventOrder="start,-duration,allDay,title"
                                    // 👇 สร้างอีเวนต์เฉพาะช่วงที่ FullCalendar ขอ (อนันต์ตามการเลื่อน)
                                    events={(fetchInfo, successCallback) => {
                                        const start = fetchInfo.start; // Date
                                        const end = fetchInfo.end;     // Date (exclusive บาง view)
                                        const evs = buildEventsInRange(start, end, filtered);
                                        successCallback(evs);
                                    }}
                                    eventContent={(arg) => {
                                        const { title, start } = arg.event;
                                        const dayIndex = start ? new Date(start).getDay() : 0; // 0=Sun..6=Sat
                                        const dayNames = ["sun", "mon", "tue", "wed", "thu", "fri", "sat"];
                                        const dayClass = `fc-event-card fc-event-${dayNames[dayIndex]}`;
                                        return {
                                            html: `
        <div class="${dayClass}">
          <div class="fc-event-title">${title}</div>
        </div>
      `,
                                        };
                                    }}
                                    dateClick={(arg) => {
                                        setNewSch((p) => ({ ...p, lastDate: isoToDmy(arg.dateStr) }));
                                        const modalEl = document.getElementById("createScheduleModal");
                                        if (modalEl) {
                                            (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).show();
                                        }
                                    }}
                                    eventClick={(info) => {
                                        const ev = info.event;
                                        const xp = ev.extendedProps || {};
                                        openViewModal({
                                            scheduleId: xp.scheduleId,
                                            eventId: ev.id,
                                            title: ev.title,
                                            scope: xp.scope || "-",
                                            lastDate: xp.occurrenceDate || "-", // dmy
                                            nextDate: xp.nextDate || "-",       // dmy
                                            assetGroupName: xp.assetGroupName || "-",
                                            description: xp.description || "",
                                            cycle: xp.cycle ?? "-",
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
            <Modal id="viewScheduleModal" title="Schedule Detail" icon="bi bi-calendar-event" size="modal-lg">
                {viewEvent ? (
                    <form>
                        <div className="row g-3">
                            {/* Scope */}
                            <div className="col-md-6">
                                <label className="form-label">Scope</label>
                                <input type="text" className="form-control" value={viewEvent.scope || "-"} disabled />
                            </div>

                            {/* Asset Group */}
                            <div className="col-md-6">
                                <label className="form-label">Asset Group</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={viewEvent.assetGroupName && viewEvent.assetGroupName !== "-" ? viewEvent.assetGroupName : "-"}
                                    disabled
                                />
                            </div>

                            {/* Last date */}
                            <div className="col-md-6">
                                <label className="form-label">Last date</label>
                                <input type="text" className="form-control" value={viewEvent.lastDate || "-"} disabled />
                            </div>

                            {/* Next date */}
                            <div className="col-md-6">
                                <label className="form-label">Next date</label>
                                <input type="text" className="form-control" value={viewEvent.nextDate || "-"} disabled />
                            </div>

                            {/* Cycle */}
                            <div className="col-md-6">
                                <label className="form-label">Cycle</label>
                                <input type="text" className="form-control" value={viewEvent.cycle
                                    ? `every ${viewEvent.cycle} month${viewEvent.cycle > 1 ? "s" : ""}`
                                    : "-"} disabled />
                            </div>

                            {/* Title */}
                            <div className="col-md-12">
                                <label className="form-label">Title</label>
                                <input type="text" className="form-control" value={viewEvent.title || "-"} disabled />
                            </div>

                            {/* Description */}
                            <div className="col-md-12">
                                <label className="form-label">Description</label>
                                <textarea className="form-control" rows={3} value={viewEvent.description || "-"} disabled />
                            </div>

                            {/* Footer Buttons */}
                            <div className="col-12 d-flex justify-content-between pt-3">
                                <button type="button" className="btn btn-outline-secondary" onClick={closeViewModal}>
                                    Close
                                </button>
                                <button type="button" className="btn btn-danger" onClick={handleDeleteFromView}>
                                    <i className="bi bi-trash me-1" /> Delete
                                </button>
                            </div>
                        </div>
                    </form>
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
                        if (err) {
                            alert(err);
                            return;
                        }
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
                                lastDate: todayDmy(),
                                notify: "",
                                title: "",
                                description: "",
                            });
                        } catch (e2) {
                            console.error(e2);
                            showMessageError("บันทึกไม่สำเร็จ");
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
                                onChange={(e) => setNewSch((p) => ({ ...p, assetGroupId: e.target.value }))}
                                disabled={newSch.scope !== "0"}
                                required={newSch.scope === "0"}
                            >
                                <option value="">{newSch.scope === "0" ? "Select Asset Group" : " "}</option>
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

                        {/* 4) Last date (dd/mm/yyyy) */}
                        <div className="col-md-4">
                            <label className="form-label">Last date</label>
                            <input
                                type="date"
                                className="form-control"
                                value={dmyToIso(newSch.lastDate) || ""}   // แปลง dd/mm/yyyy -> ISO ให้ input=date
                                onChange={(e) => {
                                    // รับค่าจาก calendar เป็น ISO แล้วแปลงกลับไปเก็บเป็น dd/mm/yyyy
                                    const iso = e.target.value;             // yyyy-MM-dd หรือ ""
                                    setNewSch((p) => ({ ...p, lastDate: iso ? isoToDmy(iso) : "" }));
                                }}
                                required
                            />
                        </div>

                        {/* 5) nextDueDate (auto + disabled) */}
                        <div className="col-md-4">
                            <label className="form-label">Next date (auto)</label>
                            <input
                                type="text"
                                className="form-control"
                                value={newSch.lastDate && newSch.cycle ? addMonthsDMY(newSch.lastDate, newSch.cycle) : ""}
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
                            <button type="button" className="btn btn-outline-secondary" data-bs-dismiss="modal">
                                Cancel
                            </button>
                            <button type="submit" className="btn btn-primary" disabled={saving}>
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
                    </div>
                </form>
            </Modal>
        </Layout>
    );
}

export default MaintenanceSchedule;
