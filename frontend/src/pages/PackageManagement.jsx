import React, { useEffect, useMemo, useState } from "react";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import { pageSize as defaultPageSize, apiPath } from "../config_variable";
import * as bootstrap from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import useMessage from "../component/useMessage";

/* ========= API via fetch ========= */
async function getJSON(url, opts = {}) {
    const res = await fetch(url, {
        credentials: "include",
        headers: { Accept: "application/json", ...(opts.headers || {}) },
        ...opts,
    });
    if (!res.ok) throw new Error(`HTTP ${res.status} for ${url}`);
    if (res.status === 204) return null;
    const text = await res.text();
    if (!text) return null;
    try {
        return JSON.parse(text);
    } catch {
        return null;
    }
}

const API = {
    listPackages: () => getJSON(`${apiPath}/packages`),
    listContractTypes: () => getJSON(`${apiPath}/contract-types`),

    // พยายามดึง room sizes จากหลาย endpoint เพื่อให้ robust
    listRoomSizes: async () => {
        // 1) ถ้ามี /rooms/sizes (ส่งกลับเช่น [1,2,3,4])
        try {
            const sizes1 = await getJSON(`${apiPath}/rooms/sizes`);
            if (Array.isArray(sizes1) && sizes1.length) return sizes1.map(Number);
        } catch {}

        // 2) ถ้า /rooms (ส่ง list ของห้อง) => unique room_size
        try {
            const rooms = await getJSON(`${apiPath}/rooms`);
            if (Array.isArray(rooms) && rooms.length) {
                const set = new Set(
                    rooms
                        .map((r) => r.room_size ?? r.roomSize ?? r.size ?? null)
                        .filter((v) => v !== null && v !== undefined)
                        .map(Number)
                );
                if (set.size) return Array.from(set).sort((a, b) => a - b);
            }
        } catch {}

        // 3) fallback: derive จาก packages ที่มีอยู่
        try {
            const pkgs = await API.listPackages();
            const set = new Set(
                (pkgs || [])
                    .map((p) => p.room_size ?? p.roomSize ?? null)
                    .filter((v) => v !== null && v !== undefined)
                    .map(Number)
            );
            if (set.size) return Array.from(set).sort((a, b) => a - b);
        } catch {}

        return []; // ไม่มีข้อมูล
    },

    createPackage: (body) =>
        getJSON(`${apiPath}/packages`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        }),

    updateActive: async (id) => {
        const res = await fetch(`${apiPath}/packages/${id}/toggle`, {
            method: "PATCH",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json().catch(() => null);
    },

    deletePackage: (id) => getJSON(`${apiPath}/packages/${id}`, { method: "DELETE" }),
};

/* ===================== สีตามเดือน + ยูทิลสี ===================== */
const COLOR_BY_MONTHS = { 3: "#FFC73B", 6: "#EF98C4", 9: "#87C6FF", 12: "#9691F9" };
const hashToColor = (str) => {
    let h = 0;
    for (let i = 0; i < str.length; i++) {
        h = (h << 5) - h + str.charCodeAt(i);
        h |= 0;
    }
    const hue = Math.abs(h) % 360;
    return `hsl(${hue}, 70%, 70%)`;
};
const withColor = (pkg) => {
    if (pkg.color) return pkg;
    const key = String(pkg.contractTypeName || pkg.label || pkg.id || "");
    const derived = COLOR_BY_MONTHS[pkg.months] || hashToColor(key);
    return { ...pkg, color: derived };
};

/* ===================== ปิดโมดัลแบบชัวร์ ===================== */
const closeModalSafely = (id) => {
    const el = document.getElementById(id);
    if (!el) return;
    const inst = bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el, { backdrop: true });
    el.addEventListener(
        "hidden.bs.modal",
        () => {
            document.querySelectorAll(".modal-backdrop").forEach((d) => d.remove());
            document.body.classList.remove("modal-open");
            document.body.style.removeProperty("overflow");
            document.body.style.removeProperty("paddingRight");
        },
        { once: true }
    );
    inst.hide();
};

const hideOffcanvasSafely = (id) => {
    const el = document.getElementById(id);
    const inst = el ? bootstrap.Offcanvas.getInstance(el) || new bootstrap.Offcanvas(el) : null;
    if (inst) inst.hide();
};

/* ========= Helpers ========= */

// === Room Size mapping ===
const ROOM_SIZE_NAME = {
    0: "Studio",
    1: "Superior",
    2: "Deluxe",
};
const roomSizeLabel = (v) => ROOM_SIZE_NAME?.[Number(v)] ?? `Size ${v}`;

const labelFromMonths = (m) => (m === 12 ? "1 Year" : `${m} Month`);

// ===================== Mapping DTO จาก backend =====================
const mapDtoToRow = (dto) => {
    const ct = dto.contractType || dto.contract_type || null;

    const contractTypeId =
        dto.contractTypeId ??
        dto.contract_type_id ??
        (ct && (ct.id ?? ct.contractTypeId ?? ct.contract_type_id)) ??
        null;

    const months = Number((ct && (ct.months ?? ct.duration)) ?? dto.months ?? dto.duration ?? 0);

    const name =
        (ct && (ct.name ?? ct.contract_name)) ??
        dto.contract_name ??
        (months ? `${months} เดือน` : "Unknown");

    const roomSize = Number(dto.room_size ?? dto.roomSize ?? 0);

    return withColor({
        id: dto.id,
        contractTypeId,
        contractTypeName: name,
        label: name,
        months,
        roomSize, // ✅ เก็บ room size
        roomSizeName: roomSizeLabel(roomSize),
        rent: Number(dto.price),
        active: Number(dto.is_active) === 1 || dto.is_active === true || dto.isActive === 1,
        createDate: dto.createDate || "-",
    });
};

function PackageManagement() {
    const [packages, setPackages] = useState([]);
    const [contractTypes, setContractTypes] = useState([]); // [{id,name,months}]
    const [roomSizes, setRoomSizes] = useState([]); // [1,2,3,...]
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const { showMessageSave, showMessageError } = useMessage();

    // ฟอร์มสร้างใหม่
    const [newPkg, setNewPkg] = useState({
        contractTypeId: null,
        roomSize: null, // ✅ เพิ่ม
        months: 0,
        rent: 5000,
        createDate: new Date().toISOString().slice(0, 10),
        active: true,
    });

    // ฟิลเตอร์/ค้นหา/เรียง
    const [filters, setFilters] = useState({
        contractTypeId: "ALL",
        roomSize: "ALL", // ✅ เพิ่ม
        active: "ALL",
        rentMin: "",
        rentMax: "",
        dateFrom: "",
        dateTo: "",
    });

    const openCreateModal = () => {
        hideOffcanvasSafely("packageFilterCanvas");

        // default ให้ contractType และ roomSize ถ้ายังไม่ตั้ง
        setNewPkg((p) => {
            const ctId = p.contractTypeId ?? (contractTypes[0]?.id ?? null);
            const months = contractTypes.find((x) => String(x.id) === String(ctId))?.months ?? p.months;
            const rs = p.roomSize ?? (roomSizes[0] ?? null);
            return { ...p, contractTypeId: ctId, months, roomSize: rs };
        });

        const el = document.getElementById("createPackageModal");
        if (el) {
            const inst = bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el, { backdrop: "static" });
            inst.show();
        }
    };

    const [search, setSearch] = useState("");
    const [sortAsc, setSortAsc] = useState(true);

    /* ===== Load ===== */
    const fetchContractTypes = async () => {
        try {
            const raw = await API.listContractTypes();
            const rows = (raw || []).map((r) => ({
                id: r.id ?? r.contractTypeId ?? r.contract_type_id,
                name: r.name ?? r.contract_name ?? labelFromMonths(Number(r.months ?? r.duration ?? 0)),
                months: Number(r.months ?? r.duration ?? 0),
            }));
            setContractTypes(rows);
            setNewPkg((p) =>
                p.contractTypeId
                    ? p
                    : rows[0]
                        ? { ...p, contractTypeId: rows[0].id, months: rows[0].months }
                        : p
            );
        } catch (e) {
            console.error("fetchContractTypes error:", e);
        }
    };

    const fetchRoomSizes = async () => {
        try {
            const sizes = await API.listRoomSizes();
            setRoomSizes(sizes);
            setNewPkg((p) => (p.roomSize ? p : sizes[0] ? { ...p, roomSize: sizes[0] } : p));
        } catch (e) {
            console.error("fetchRoomSizes error:", e);
        }
    };

    const fetchPackages = async () => {
        setLoading(true);
        setErr("");
        try {
            const data = await API.listPackages();
            setPackages((data || []).map(mapDtoToRow));
        } catch (e) {
            console.error("fetchPackages error:", e);
            setErr("Error fetching packages");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchContractTypes();
        fetchRoomSizes();
        fetchPackages();
    }, []);

    // toggle: ให้คุมเป็นคู่ (contractTypeId, roomSize)
    const toggleActive = (row) => {
        const willActive = !row.active;
        const targetTypeId = row.contractTypeId;
        const targetRoomSize = row.roomSize;

        setPackages((prev) => {
            // ตัวที่เป็นคู่เดียวกัน
            const samePairActive = prev.filter(
                (p) =>
                    p.contractTypeId === targetTypeId &&
                    p.roomSize === targetRoomSize &&
                    p.id !== row.id &&
                    p.active === true
            );

            // อัปเดตสถานะ UI แบบ optimistic เฉพาะคู่เดียวกัน
            const updated = prev.map((p) =>
                p.contractTypeId === targetTypeId && p.roomSize === targetRoomSize
                    ? { ...p, active: p.id === row.id ? willActive : false }
                    : p
            );

            Promise.allSettled([
                API.updateActive(row.id),
                ...samePairActive.map((p) => API.updateActive(p.id)),
            ])
                .then(() => fetchPackages())
                .catch((err) => console.error("toggleActive error:", err));

            return updated;
        });
    };

    /* ===== Filter + Search + Sort ===== */
    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        let rows = [...packages];

        rows = rows.filter((p) => {
            if (filters.contractTypeId !== "ALL" && String(p.contractTypeId) !== String(filters.contractTypeId)) {
                return false;
            }
            if (filters.roomSize !== "ALL" && Number(p.roomSize) !== Number(filters.roomSize)) {
                return false;
            }
            if (filters.active !== "ALL") {
                const want = filters.active === "TRUE";
                if (p.active !== want) return false;
            }
            if (filters.rentMin !== "" && p.rent < Number(filters.rentMin)) return false;
            if (filters.rentMax !== "" && p.rent > Number(filters.rentMax)) return false;
            return true;
        });

        if (q) {
            rows = rows.filter(
                (p) =>
                    (p.contractTypeName || "").toLowerCase().includes(q) ||
                    String(p.rent).includes(q) ||
                    (p.createDate && p.createDate.includes(q)) ||
                    String(p.roomSize).includes(q)
            );
        }

        // sort by createDate asc/desc
        const key = (x) => (x.createDate === "-" ? "" : x.createDate);
        rows.sort((a, b) => (sortAsc ? key(a).localeCompare(key(b)) : key(b).localeCompare(key(a))));

        // เสริม: จัดเรียงให้สวย: contractTypeId → roomSize → rent
        rows.sort((a, b) => {
            if (a.contractTypeId !== b.contractTypeId) return a.contractTypeId - b.contractTypeId;
            if (a.roomSize !== b.roomSize) return a.roomSize - b.roomSize;
            return a.rent - b.rent;
        });

        return rows;
    }, [packages, filters, search, sortAsc]);

    /* ===== Pagination ===== */
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

    /* ===== Create ===== */
    const [saving, setSaving] = useState(false);

    const handleSaveCreate = async () => {
        if (!newPkg.contractTypeId) {
            setErr("Please select contract type");
            return;
        }
        if (newPkg.roomSize === null || newPkg.roomSize === undefined) {
            setErr("Please select room size");
            return;
        }

        setSaving(true);
        try {
            const payload = {
                price: Number(newPkg.rent),
                is_active: newPkg.active ? 1 : 0,
                contract_type_id: Number(newPkg.contractTypeId),
                room_size: Number(newPkg.roomSize), // ✅ ส่ง room_size ไป backend
            };

            await API.createPackage(payload);

            closeModalSafely("createPackageModal");
            showMessageSave("สร้าง Package สำเร็จ!");

            setTimeout(() => {
                fetchPackages();
            }, 250);

            setNewPkg((p) => ({ ...p, rent: 5000, active: true }));
        } catch (e) {
            console.error("createPackage error:", e);
            showMessageError("เกิดข้อผิดพลาดในการสร้าง Package");
        } finally {
            setSaving(false);
        }
    };

    const clearFilters = () =>
        setFilters({
            contractTypeId: "ALL",
            roomSize: "ALL",
            active: "ALL",
            rentMin: "",
            rentMax: "",
            dateFrom: "",
            dateTo: "",
        });

    const hasAnyFilter =
        filters.contractTypeId !== "ALL" ||
        filters.roomSize !== "ALL" ||
        filters.active !== "ALL" ||
        filters.rentMin !== "" ||
        filters.rentMax !== "" ||
        !!filters.dateFrom ||
        !!filters.dateTo;

    const filterSummary = [];
    if (filters.contractTypeId !== "ALL") {
        const ct = contractTypes.find((c) => String(c.id) === String(filters.contractTypeId));
        filterSummary.push(`Package: ${ct ? ct.name : filters.contractTypeId}`);
    }
    if (filters.roomSize !== "ALL")
        filterSummary.push(`Room Size: ${roomSizeLabel(filters.roomSize)}`);
    if (filters.active !== "ALL")
        filterSummary.push(`Status: ${filters.active === "TRUE" ? "Active" : "Inactive"}`);
    if (filters.rentMin !== "") filterSummary.push(`Rent ≥ ${filters.rentMin}`);
    if (filters.rentMax !== "") filterSummary.push(`Rent ≤ ${filters.rentMax}`);
    if (filters.dateFrom) filterSummary.push(`From ${filters.dateFrom}`);
    if (filters.dateTo) filterSummary.push(`To ${filters.dateTo}`);

    return (
        <Layout title="Package Management" icon="bi bi-sticky" notifications={0}>
            <div className="container-fluid">
                <div className="row min-vh-100">
                    <div className="col-lg-11">
                        {/* Toolbar */}
                        <div className="toolbar-wrapper card border-0 bg-white">
                            <div className="card-header bg-white border-0 rounded-3">
                                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                                    <div className="d-flex align-items-center gap-3">
                                        <button
                                            className="btn btn-link tm-link p-0"
                                            data-bs-toggle="offcanvas"
                                            data-bs-target="#packageFilterCanvas"
                                        >
                                            <i className="bi bi-filter me-1"></i> Filter
                                            {hasAnyFilter && <span className="badge bg-primary ms-2">●</span>}
                                        </button>

                                        <button className="btn btn-link tm-link p-0" onClick={() => setSortAsc((s) => !s)}>
                                            <i className="bi bi-arrow-down-up me-1"></i> Sort
                                        </button>

                                        <div className="input-group tm-search">
                      <span className="input-group-text bg-white border-end-0">
                        <i className="bi bi-search"></i>
                      </span>
                                            <input
                                                type="text"
                                                className="form-control border-start-0"
                                                placeholder="Search package"
                                                value={search}
                                                onChange={(e) => setSearch(e.target.value)}
                                            />
                                        </div>
                                    </div>

                                    <div className="d-flex align-items-center gap-2">
                                        <button type="button" className="btn btn-primary" onClick={openCreateModal}>
                                            <i className="bi bi-plus-lg me-1"></i> Create Package
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

                        {err && <div className="alert alert-danger mt-3">{err}</div>}
                        {loading && <div className="alert alert-info mt-3">Loading packages...</div>}

                        {/* Table */}
                        <div className="table-wrapper">
                            <table className="table text-nowrap align-middle">
                                <thead>
                                <tr className="header-color">
                                    <th>Order</th>
                                    <th>Package</th>
                                    <th>Room size</th>
                                    <th>Rent</th>
                                    <th>Action</th>
                                </tr>
                                </thead>
                                <tbody>
                                {pageRows.length ? (
                                    pageRows.map((item, idx) => (
                                        <tr key={item.id}>
                                            <td className="align-middle text-start">
                                                {(currentPage - 1) * pageSize + idx + 1}
                                            </td>
                                            <td className="align-middle text-start">
                          <span
                              className="badge rounded-pill px-3 py-2"
                              style={{ backgroundColor: withColor(item).color }}
                          >
                            {item.contractTypeName}
                          </span>
                                            </td>
                                            <td className="align-middle text-start">{item.roomSizeName}</td>
                                            <td className="align-middle text-start">{item.rent.toLocaleString()}</td>
                                            <td className="align-middle pe-3">
                                                <div className="form-check form-switch d-inline-flex">
                                                    <input
                                                        className="form-check-input"
                                                        type="checkbox"
                                                        role="switch"
                                                        checked={item.active}
                                                        onChange={() => toggleActive(item)}
                                                        aria-label={`Toggle ${item.contractTypeName} size ${item.roomSize}`}
                                                    />
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="5" className="text-center">
                                            No packages found
                                        </td>
                                    </tr>
                                )}
                                </tbody>
                            </table>
                        </div>

                        <Pagination
                            currentPage={currentPage}
                            totalPages={totalPages}
                            onPageChange={setCurrentPage}
                            totalRecords={totalRecords}
                            onPageSizeChange={setPageSize}
                        />
                    </div>
                </div>
            </div>

            {/* Create Package Modal */}
            <Modal id="createPackageModal" title="Create Package" con="bi bi-sticky" size="modal-lg">
                <form
                    onSubmit={(e) => {
                        e.preventDefault();
                        handleSaveCreate();
                    }}
                >
                    <div className="row g-3">
                        <div className="col-md-6">
                            <label className="form-label">Contract type</label>
                            <select
                                className="form-select"
                                value={newPkg.contractTypeId ?? ""}
                                onChange={(e) => {
                                    const id = e.target.value;
                                    const ct = contractTypes.find((c) => String(c.id) === String(id));
                                    setNewPkg((p) => ({
                                        ...p,
                                        contractTypeId: id,
                                        months: ct?.months ?? p.months,
                                    }));
                                }}
                                required
                            >
                                {contractTypes.length === 0 && <option value="">Loading...</option>}
                                {contractTypes.map((ct) => (
                                    <option key={ct.id} value={ct.id}>
                                        {ct.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Room Size</label>
                            <select
                                className="form-select"
                                value={newPkg.roomSize ?? ""}
                                onChange={(e) => setNewPkg((p) => ({ ...p, roomSize: Number(e.target.value) }))}
                                required
                            >
                                {roomSizes.length === 0 && <option value="">Loading...</option>}
                                {roomSizes.map((sz) => (
                                    <option key={sz} value={sz}>
                                        {roomSizeLabel(sz)}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Rent</label>
                            <input
                                type="number"
                                className="form-control"
                                value={newPkg.rent}
                                onChange={(e) => setNewPkg((p) => ({ ...p, rent: Number(e.target.value) }))}
                                required
                            />
                        </div>

                        {/*<div className="col-md-6 d-flex align-items-end">*/}
                        {/*    <div className="form-check form-switch">*/}
                        {/*        <input*/}
                        {/*            className="form-check-input"*/}
                        {/*            type="checkbox"*/}
                        {/*            role="switch"*/}
                        {/*            checked={newPkg.active}*/}
                        {/*            onChange={(e) => setNewPkg((p) => ({ ...p, active: e.target.checked }))}*/}
                        {/*            id="newPkgActive"*/}
                        {/*        />*/}
                        {/*        <label className="form-check-label ms-2" htmlFor="newPkgActive">*/}
                        {/*            Active*/}
                        {/*        </label>*/}
                        {/*    </div>*/}
                        {/*</div>*/}

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

            {/* Filters Offcanvas */}
            <div className="offcanvas offcanvas-end"
                 tabIndex="-1" id="packageFilterCanvas"
                 aria-labelledby="packageFilterCanvasLabel"
                 data-bs-backdrop="static">
                <div className="offcanvas-header">
                    <h5 id="packageFilterCanvasLabel" className="mb-0">
                        <i className="bi bi-filter me-2"></i>Filters
                    </h5>
                    <button type="button" className="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
                </div>

                <div className="offcanvas-body">
                    <div className="row g-3">
                        <div className="col-12">
                            <label className="form-label">Package</label>
                            <select
                                className="form-select"
                                value={filters.contractTypeId}
                                onChange={(e) => setFilters((f) => ({ ...f, contractTypeId: e.target.value }))}
                            >
                                <option value="ALL">All</option>
                                {contractTypes.map((ct) => (
                                    <option key={ct.id} value={ct.id}>
                                        {ct.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-12">
                            <label className="form-label">Room Size</label>
                            <select
                                className="form-select"
                                value={filters.roomSize}
                                onChange={(e) => setFilters((f) => ({ ...f, roomSize: e.target.value }))}
                            >
                                <option value="ALL">All</option>
                                {roomSizes.map((sz) => (
                                    <option key={sz} value={sz}>
                                        {roomSizeLabel(sz)}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="col-12">
                            <label className="form-label">Status</label>
                            <select
                                className="form-select"
                                value={filters.active}
                                onChange={(e) => setFilters((f) => ({ ...f, active: e.target.value }))}
                            >
                                <option value="ALL">All</option>
                                <option value="TRUE">Active</option>
                                <option value="FALSE">Inactive</option>
                            </select>
                        </div>

                        <div className="col-md-6">
                            <label className="form-label">Rent min</label>
                            <input
                                type="number"
                                className="form-control"
                                value={filters.rentMin}
                                onChange={(e) => setFilters((f) => ({ ...f, rentMin: e.target.value }))}
                                placeholder="e.g. 4500"
                            />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Rent max</label>
                            <input
                                type="number"
                                className="form-control"
                                value={filters.rentMax}
                                onChange={(e) => setFilters((f) => ({ ...f, rentMax: e.target.value }))}
                                placeholder="e.g. 6000"
                            />
                        </div>

                        <div className="col-12 d-flex justify-content-between mt-2">
                            <button type="button" className="btn btn-outline-secondary" onClick={clearFilters}>
                                Clear
                            </button>
                            <button type="button" className="btn btn-primary" data-bs-dismiss="offcanvas">
                                Apply
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
}

export default PackageManagement;
