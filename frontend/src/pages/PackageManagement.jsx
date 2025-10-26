import React, { useEffect, useMemo, useState } from "react";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
// import { pageSize as defaultPageSize } from "../config_variable";
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
  // ปลอดภัยเมื่อ body ว่าง (เช่น 200/201 but no content)
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
  createPackage: (body) =>
    getJSON(`${apiPath}/packages`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    }),
  /**
   * อัปเดตสถานะเปิด/ปิดแบบ fallback หลายรูปแบบ
   */
  updateActive: async (id) => {
    try {
      const res = await fetch(`${apiPath}/packages/${id}/toggle`, {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }

      const data = await res.json().catch(() => null);
      console.log("✅ toggle success:", data);
      return data;
    } catch (err) {
      console.error("❌ updateActive error:", err);
      throw err;
    }
  },

  deletePackage: (id) =>
    getJSON(`${apiPath}/packages/${id}`, { method: "DELETE" }),
};

/* ===================== สีตามเดือน + ยูทิลสี ===================== */
const COLOR_BY_MONTHS = {
  3: "#FFC73B",
  6: "#EF98C4",
  9: "#87C6FF",
  12: "#9691F9",
};
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

  const inst =
    bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el, { backdrop: true });

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
  const inst = el
    ? bootstrap.Offcanvas.getInstance(el) || new bootstrap.Offcanvas(el)
    : null;
  if (inst) inst.hide();
};

/* ========= Helpers ========= */
const labelFromMonths = (m) => (m === 12 ? "1 Year" : `${m} Month`);

// ===================== Mapping DTO จาก backend =====================
  const mapDtoToRow = (dto) => {
    // รองรับทุกแบบของ field ที่ backend อาจส่งมา
    const ct =
      dto.contractType || // แบบ CamelCase
      dto.contract_type || // แบบ snake_case
      null;

    // ✅ ดึง contractTypeId ให้แน่นอนที่สุด
    const contractTypeId =
      dto.contractTypeId ??
      dto.contract_type_id ??
      (ct && (ct.id ?? ct.contractTypeId ?? ct.contract_type_id)) ??
      null;

    // ✅ ดึง months และชื่อแพ็กเกจ
    const months = Number(
      (ct && (ct.months ?? ct.duration)) ??
        dto.months ??
        dto.duration ??
        0
    );

    const name =
      (ct && (ct.name ?? ct.contract_name)) ??
      dto.contract_name ??
      (months ? `${months} เดือน` : "Unknown");

    // ✅ log ดูทีละตัวว่ามี contractTypeId หรือไม่
    console.log("mapDtoToRow:", {
      id: dto.id,
      contractTypeId,
      raw: dto,
    });

    return withColor({
      id: dto.id,
      contractTypeId,
      contractTypeName: name,
      label: name,
      months,
      rent: Number(dto.price),
      active: Number(dto.is_active) === 1 || dto.is_active === true,
      createDate: dto.createDate || "-",
    });
  };

function PackageManagement() {
  const [packages, setPackages] = useState([]);
  const [contractTypes, setContractTypes] = useState([]); // [{id,name,months}]
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const { showMessageSave, showMessageError, showMessageConfirmDelete } = useMessage();


  // ฟอร์มสร้างใหม่ (เลือกจาก contractType)
  const [newPkg, setNewPkg] = useState({
    contractTypeId: null,
    months: 0,
    rent: 5000,
    createDate: new Date().toISOString().slice(0, 10),
    active: true,
  });

  // ฟิลเตอร์/ค้นหา/เรียง
  const [filters, setFilters] = useState({
    contractTypeId: "ALL",
    active: "ALL",
    rentMin: "",
    rentMax: "",
    dateFrom: "",
    dateTo: "",
  });

  const openCreateModal = () => {
    hideOffcanvasSafely("packageFilterCanvas");

    // ค่า default ให้ newPkg ก่อนเปิด modal
    if (!newPkg.contractTypeId && contractTypes.length) {
      const first = contractTypes[0];
      setNewPkg((p) => ({
        ...p,
        contractTypeId: String(first.id),
        months: first.months,
      }));
    }

    const el = document.getElementById("createPackageModal");
    if (el) {
      const inst =
        bootstrap.Modal.getInstance(el) ||
        new bootstrap.Modal(el, { backdrop: "static" });
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
        name:
          r.name ??
          r.contract_name ??
          labelFromMonths(Number(r.months ?? r.duration ?? 0)),
        months: Number(r.months ?? r.duration ?? 0),
      }));
      setContractTypes(rows);

      // default select
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
    fetchPackages();
  }, []);

  const toggleActive = (row) => {
    const willActive = !row.active;
    const targetTypeId = row.contractTypeId;

    // ✅ ใช้ state ล่าสุดใน callback
    setPackages((prev) => {
      const sameTypeActive = prev.filter(
        (p) =>
          p.contractTypeId === targetTypeId &&
          p.id !== row.id &&
          p.active === true
      );

      // อัปเดตสถานะ UI แบบ Optimistic
      const updated = prev.map((p) =>
        p.contractTypeId === targetTypeId
          ? { ...p, active: p.id === row.id ? willActive : false }
          : p
      );

      // ยิง API เบื้องหลังแบบ async ไม่บล็อก
      Promise.allSettled([
        API.updateActive(row.id, willActive),
        ...sameTypeActive.map((p) => API.updateActive(p.id, false)),
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
      if (
        filters.contractTypeId !== "ALL" &&
        String(p.contractTypeId) !== String(filters.contractTypeId)
      )
        return false;
      if (filters.active !== "ALL") {
        const want = filters.active === "TRUE";
        if (p.active !== want) return false;
      }
      if (filters.rentMin !== "" && p.rent < Number(filters.rentMin))
        return false;
      if (filters.rentMax !== "" && p.rent > Number(filters.rentMax))
        return false;
      return true;
    });

    if (q) {
      rows = rows.filter(
        (p) =>
          (p.contractTypeName || "").toLowerCase().includes(q) ||
          String(p.rent).includes(q) ||
          (p.createDate && p.createDate.includes(q))
      );
    }

    const key = (x) => (x.createDate === "-" ? "" : x.createDate);
    rows.sort((a, b) =>
      sortAsc ? key(a).localeCompare(key(b)) : key(b).localeCompare(key(a))
    );

    // 🟡 <==== คุณเพิ่มตรงนี้เลย
    // ✅ จัดเรียงให้สวย ตาม contractTypeId → duration → rent
    rows.sort((a, b) => {
      if (a.contractTypeId !== b.contractTypeId)
        return a.contractTypeId - b.contractTypeId;
      if (a.duration !== b.duration) return a.duration - b.duration;
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtered, currentPage, pageSize]);

  /* ===== Create (ปิดโมดัลหลังเซฟ และไม่โชว์ "Error creating package") ===== */
  const [saving, setSaving] = useState(false);

  // แทนที่ฟังก์ชันเดิมทั้งก้อนด้วยอันนี้
  const handleSaveCreate = async () => {
    if (!newPkg.contractTypeId) {
      setErr("Please select contract type");
      return;
    }

    setSaving(true);
    try {
      const idNum = Number(newPkg.contractTypeId);
      const payload = {
        price: Number(newPkg.rent),
        is_active: newPkg.active ? 1 : 0,
        contract_type_id: idNum,
      };

      await API.createPackage(payload);

      // ✅ ปิด modal แบบชัวร์
      closeModalSafely("createPackageModal");

      // ✅ แจ้งเตือน SweetAlert เมื่อสร้างสำเร็จ
      showMessageSave("สร้าง Package สำเร็จ!");

      // ✅ โหลดข้อมูลใหม่หลังจากแจ้งเตือน
      setTimeout(() => {
        fetchPackages();
      }, 250);

      // ✅ รีเซ็ตค่า form
      setNewPkg((p) => ({ ...p, rent: 5000, active: true }));
    } catch (e) {
      console.error("createPackage error:", e);

      // ❌ แจ้งเตือน SweetAlert กรณี error
      showMessageError("เกิดข้อผิดพลาดในการสร้าง Package");
    } finally {
      setSaving(false);
    }
  };

  const clearFilters = () =>
    setFilters({
      contractTypeId: "ALL",
      active: "ALL",
      rentMin: "",
      rentMax: "",
      dateFrom: "",
      dateTo: "",
    });

  const hasAnyFilter =
    filters.contractTypeId !== "ALL" ||
    filters.active !== "ALL" ||
    filters.rentMin !== "" ||
    filters.rentMax !== "" ||
    !!filters.dateFrom ||
    !!filters.dateTo;

  const filterSummary = [];
  if (filters.contractTypeId !== "ALL") {
    const ct = contractTypes.find(
      (c) => String(c.id) === String(filters.contractTypeId)
    );
    filterSummary.push(`Package: ${ct ? ct.name : filters.contractTypeId}`);
  }
  if (filters.active !== "ALL")
    filterSummary.push(
      `Status: ${filters.active === "TRUE" ? "Active" : "Inactive"}`
    );
  if (filters.rentMin !== "") filterSummary.push(`Rent ≥ ${filters.rentMin}`);
  if (filters.rentMax !== "") filterSummary.push(`Rent ≤ ${filters.rentMax}`);
  if (filters.dateFrom) filterSummary.push(`From ${filters.dateFrom}`);
  if (filters.dateTo) filterSummary.push(`To ${filters.dateTo}`);

  return (
    <Layout title="Package Management" icon="bi bi-sticky" notifications={0}>
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
                      data-bs-target="#packageFilterCanvas"
                    >
                      <i className="bi bi-filter me-1"></i> Filter
                      {hasAnyFilter && (
                        <span className="badge bg-primary ms-2">●</span>
                      )}
                    </button>

                    <button
                      className="btn btn-link tm-link p-0"
                      onClick={() => setSortAsc((s) => !s)}
                    >
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
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={openCreateModal}
                    >
                      <i className="bi bi-plus-lg me-1"></i> Create Package
                    </button>
                  </div>
                </div>

                <div className={`collapse ${hasAnyFilter ? "show" : ""}`}>
                  <div className="pt-2 d-flex flex-wrap gap-2">
                    {filterSummary.map((txt, idx) => (
                      <span
                        key={idx}
                        className="badge bg-light text-dark border"
                      >
                        {txt}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {err && <div className="alert alert-danger mt-3">{err}</div>}
            {loading && (
              <div className="alert alert-info mt-3">Loading packages...</div>
            )}

            {/* Table */}
            <div className="table-wrapper mt-3" style={{ overflowX: 'auto' }}>
              <table className="table text-nowrap w-100">
                <colgroup>
                    <col style={{ width: 80 }} />   {/* Order */}
                    <col style={{ width: 160 }} />  {/* Package */}
                    <col style={{ width: 180 }} />  {/* Rent */}
                    <col style={{ width: 100 }} />  {/* Action */}
                </colgroup>

                <thead>
                <tr>
                    <th className="text-start align-middle header-color">Order</th>
                    <th className="text-start align-middle header-color">Package</th>
                    <th className="text-start align-middle header-color">Rent</th>
                    <th className="text-end align-middle header-color pe-3">Action</th>
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
                        <td className="align-middle text-start">
                          {item.rent.toLocaleString()}
                        </td>
                        <td className="align-middle pe-3">
                          <div className="form-check form-switch d-inline-flex">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              role="switch"
                              checked={item.active}
                              onChange={() => toggleActive(item)}
                              aria-label={`Toggle ${item.contractTypeName}`}
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
      <Modal
        id="createPackageModal"
        title="Create Package"
        con="bi bi-sticky"
        size="modal-lg"
      >
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
                  const ct = contractTypes.find(
                    (c) => String(c.id) === String(id)
                  );
                  setNewPkg((p) => ({
                    ...p,
                    contractTypeId: id,
                    months: ct?.months ?? p.months,
                  }));
                }}
                required
              >
                {contractTypes.length === 0 && (
                  <option value="">Loading...</option>
                )}
                {contractTypes.map((ct) => (
                  <option key={ct.id} value={ct.id}>
                    {ct.name}
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
                onChange={(e) =>
                  setNewPkg((p) => ({ ...p, rent: Number(e.target.value) }))
                }
                required
              />
            </div>

            <div className="col-md-6 d-flex align-items-end">
              <div className="form-check form-switch">
                <input
                  className="form-check-input"
                  type="checkbox"
                  role="switch"
                  checked={newPkg.active}
                  onChange={(e) =>
                    setNewPkg((p) => ({ ...p, active: e.target.checked }))
                  }
                  id="newPkgActive"
                />
                <label className="form-check-label ms-2" htmlFor="newPkgActive">
                  Active
                </label>
              </div>
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
        id="packageFilterCanvas"
        aria-labelledby="packageFilterCanvasLabel"
      >
        <div className="offcanvas-header">
          <h5 id="packageFilterCanvasLabel" className="mb-0">
            <i className="bi bi-filter me-2"></i>Filters
          </h5>
          <button
            type="button"
            className="btn-close"
            data-bs-dismiss="offcanvas"
            aria-label="Close"
          ></button>
        </div>

        <div className="offcanvas-body">
          <div className="row g-3">
            <div className="col-12">
              <label className="form-label">Package</label>
              <select
                className="form-select"
                value={filters.contractTypeId}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, contractTypeId: e.target.value }))
                }
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
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={filters.active}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, active: e.target.value }))
                }
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
                onChange={(e) =>
                  setFilters((f) => ({ ...f, rentMin: e.target.value }))
                }
                placeholder="e.g. 4500"
              />
            </div>
            <div className="col-md-6">
              <label className="form-label">Rent max</label>
              <input
                type="number"
                className="form-control"
                value={filters.rentMax}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, rentMax: e.target.value }))
                }
                placeholder="e.g. 6000"
              />
            </div>

            <div className="col-12 d-flex justify-content-between mt-2">
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={clearFilters}
              >
                Clear
              </button>
              <button
                type="button"
                className="btn btn-primary"
                data-bs-dismiss="offcanvas"
              >
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
