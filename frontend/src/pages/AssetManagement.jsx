import React, { useMemo, useState, useEffect } from "react";
import axios from "axios";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import useMessage from "../component/useMessage";
import { pageSize as defaultPageSize, apiPath } from "../config_variable";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "../assets/css/asset.css";
import "../assets/css/alert.css";

function AssetManagement() {
  // ====== Data State ======
  const [assets, setAssets] = useState([]);
  const [assetGroups, setAssetGroups] = useState([]);
  const [selectedGroupId, setSelectedGroupId] = useState("ALL");

  // ====== Pagination State ======
  const [currentPage, setCurrentPage] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize, setPageSize] = useState(defaultPageSize || 10);

  // ====== Search/Sort State ======
  const [search, setSearch] = useState("");
  const [sortAsc, setSortAsc] = useState(true);

  // ====== Modal/Loading ======
  const [saving, setSaving] = useState(false);

  // ====== Asset Group form ======
  const [groupName, setGroupName] = useState("");
  const [editingGroupId, setEditingGroupId] = useState(null);
  const [monthlyAddonFee, setMonthlyAddonFee] = useState(0);
  const [oneTimeDamageFee, setOneTimeDamageFee] = useState(0);
  const [freeReplacement, setFreeReplacement] = useState(true);

  // ====== Asset form ======
  const [formName, setFormName] = useState("");
  const [formGroupId, setFormGroupId] = useState("");
  const [editingAssetId, setEditingAssetId] = useState(null);
  const [formQty, setFormQty] = useState(1);

  // ====== Custom Alert/Message Hooks ======
  const {
    showMessagePermission,
    showMessageError,
    showMessageSave,
    showMessageConfirmDelete,
  } = useMessage();

  // ========= Fetch Asset Groups =========
  const fetchGroups = async () => {
    try {
      const res = await axios.get(`${apiPath}/asset-group/list`, {
        withCredentials: true,
      });
      if (Array.isArray(res.data)) setAssetGroups(res.data);
      else setAssetGroups([]);
    } catch (err) {
      console.error("Error fetching asset groups:", err);
      setAssetGroups([]);
    }
  };

  // ========= Fetch Assets =========
  const fetchData = async (page = 1) => {
    try {
      const res = await axios.get(`${apiPath}/assets/all`, {
        withCredentials: true,
      });

      let rows = [];
      if (res.data?.result) rows = res.data.result;
      else if (Array.isArray(res.data)) rows = res.data;

      setAssets(rows);
      setTotalRecords(rows.length);
      setTotalPages(Math.max(1, Math.ceil(rows.length / pageSize)));
      setCurrentPage(page);
    } catch (err) {
      console.error("Error fetching assets:", err);
      setAssets([]);
      setTotalRecords(0);
      setTotalPages(1);
    }
  };

  // ========= useEffect (โหลดข้อมูลเริ่มต้น) =========
  useEffect(() => {
    fetchGroups();
    fetchData(1);
  }, [pageSize]);

  // ========= Filter Groups (สำหรับ sidebar) =========
  const filteredGroups = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return assetGroups;

    const groupMatches = assetGroups.filter((g) =>
      g.name?.toLowerCase().includes(q)
    );

    const assetMatches = assets
      .filter(
        (a) =>
          a.assetName?.toLowerCase().includes(q) ||
          String(a.assetId).includes(q)
      )
      .map((a) => assetGroups.find((g) => g.id === a.assetGroupId))
      .filter(Boolean);

    const allMatches = [...groupMatches, ...assetMatches];
    return Array.from(new Map(allMatches.map((g) => [g.id, g])).values());
  }, [assetGroups, assets, search]);

  const [filterStatus, setFilterStatus] = useState("ALL");

  // ========= Filter & Sort Assets =========
  const filteredAssets = useMemo(() => {
    const q = search.trim().toLowerCase();
    let rows = [...assets];

    if (selectedGroupId !== "ALL") {
      rows = rows.filter(
        (r) => String(r.assetGroupId) === String(selectedGroupId)
      );
    }

    if (q) {
      rows = rows.filter(
        (r) =>
          r.assetName?.toLowerCase().includes(q) ||
          String(r.assetId).includes(q)
      );
    }

    if (filterStatus && filterStatus !== "ALL") {
      rows = rows.filter((r) => r.status === filterStatus);
    }

    rows.sort((a, b) =>
      sortAsc
        ? a.assetName.localeCompare(b.assetName)
        : b.assetName.localeCompare(a.assetName)
    );

    return rows;
  }, [assets, search, sortAsc, selectedGroupId, assetGroups, filterStatus]);

  // ========= Pagination =========
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = Math.min(startIndex + pageSize, filteredAssets.length);
  const pageRows = filteredAssets.slice(startIndex, endIndex);

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) setCurrentPage(page);
  };

  const handlePageSizeChange = (size) => {
    setPageSize(size);
    setCurrentPage(1);
  };

  // ========= Form Clear =========
  const clearFormGroup = () => {
    setEditingGroupId(null);
    setGroupName("");
  };

  const clearFormAsset = (groupId) => {
    setEditingAssetId(null);
    setFormName("");
    setFormGroupId(groupId || "");
    setFormQty(1);
  };

  // ========= Validation =========
  const checkValidationGroup = () => {
    if (!groupName.trim()) {
      showMessageError("กรุณากรอกชื่อ Group");
      return false;
    }
    if (groupName.trim().length < 2) {
      showMessageError("ชื่อ Group ต้องมีอย่างน้อย 2 ตัวอักษร");
      return false;
    }
    return true;
  };

  // ========= CRUD Asset Group =========
  const handleSaveGroup = async () => {
    if (!checkValidationGroup()) return;

    try {
      setSaving(true);
      if (editingGroupId == null) {
        await axios.post(
          `${apiPath}/asset-group/create`,
          { assetGroupName: groupName },
          { withCredentials: true }
        );
        showMessageSave("สร้าง Group สำเร็จ");
      } else {
        await axios.put(
          `${apiPath}/asset-group/update/${editingGroupId}`,
          { assetGroupName: groupName },
          { withCredentials: true }
        );
        showMessageSave("แก้ไข Group สำเร็จ");
      }
      clearFormGroup();
      fetchGroups();
      document.getElementById("modalGroup_btnClose")?.click();
    } catch (err) {
      if (err.response?.status === 409) {
        showMessageError("ชื่อ Group ซ้ำ");
      } else if (err.response?.status === 401) {
        showMessagePermission();
      } else {
        showMessageError("บันทึก Group ไม่สำเร็จ");
      }
    } finally {
      setSaving(false);
    }
  };

  const onDeleteGroup = async (g) => {
    const groupAssets = assets.filter(
      (a) => String(a.assetGroupId) === String(g.id)
    );
    let result;
    if (groupAssets.length > 0) {
      result = await showMessageConfirmDelete(
        `${g.name}\n(มีอยู่ ${groupAssets.length} assets จะลบหรือไม่?)`
      );
    } else {
      result = await showMessageConfirmDelete(g.name);
    }

    if (!result.isConfirmed) return;

    try {
      await axios.delete(`${apiPath}/asset-group/delete/${g.id}`, {
        withCredentials: true,
      });

      setAssetGroups((prev) => prev.filter((x) => x.id !== g.id));
      setAssets((prev) =>
        prev.filter((x) => String(x.assetGroupId) !== String(g.id))
      );
      if (String(selectedGroupId) === String(g.id)) {
        setSelectedGroupId("ALL");
      }

      showMessageSave("ลบ Group สำเร็จ");
    } catch (err) {
      showMessageError("ลบ Group ไม่สำเร็จ");
    }
  };

  // ========= CRUD Asset =========
  const handleSaveAsset = async () => {
    if (!formName || !formGroupId) {
      showMessageError("กรุณากรอกชื่อและเลือกกลุ่ม");
      return;
    }

    try {
      setSaving(true);
      if (editingAssetId == null && parseInt(formQty) > 1) {
        await axios.post(`${apiPath}/assets/bulk`, null, {
          params: {
            assetGroupId: parseInt(formGroupId),
            name: formName.trim(),
            qty: parseInt(formQty),
          },
          withCredentials: true,
        });
        showMessageSave(`สร้าง ${formQty} ชิ้นสำเร็จ`);
      } else if (editingAssetId == null) {
        await axios.post(
          `${apiPath}/assets/create`,
          {
            assetName: formName.trim(),
            assetGroup: { id: parseInt(formGroupId) },
          },
          { withCredentials: true }
        );
        showMessageSave("สร้าง Asset สำเร็จ");
      } else {
        await axios.put(
          `${apiPath}/assets/update/${editingAssetId}`,
          {
            assetName: formName.trim(),
            assetGroup: { id: parseInt(formGroupId) },
          },
          { withCredentials: true }
        );
        showMessageSave("แก้ไข Asset สำเร็จ");
      }
      clearFormAsset();
      fetchData(currentPage);
      document.getElementById("modalAsset_btnClose")?.click();
    } catch (err) {
      console.error("Error saving asset:", err);
      showMessageError("เกิดข้อผิดพลาดในการบันทึก");
    } finally {
      setSaving(false);
    }
  };

  const onDeleteAsset = async (row) => {
    const result = await showMessageConfirmDelete(row.assetName);
    if (!result.isConfirmed) return;

    try {
      await axios.delete(`${apiPath}/assets/delete/${row.assetId}`, {
        withCredentials: true,
      });
      fetchData(currentPage);
      showMessageSave("ลบ Asset สำเร็จ");
    } catch (err) {
      showMessageError("ลบ Asset ไม่สำเร็จ");
    }
  };

  // ========= Summary =========
  const getSummaryForSelectedGroup = () => {
    let filtered = [...assets];
    if (selectedGroupId !== "ALL") {
      filtered = filtered.filter(
        (a) => String(a.assetGroupId) === String(selectedGroupId)
      );
    }
    return {
      total: filtered.length,
      inUse: filtered.filter((a) => a.status === "in_use").length,
      available: filtered.filter((a) => a.status === "available").length,
    };
  };

  const summary = getSummaryForSelectedGroup();

  // ========= UI =========
  return (
    <Layout title="Asset Management" icon="bi bi-box">
      <div className="container-fluid p-4">
        {/* ===== Summary Cards ===== */}
        <div className="row g-3 mb-4">
          <div className="col-md-3">
            <div className="card shadow-sm border-0 rounded-3 text-center p-3">
              {selectedGroupId === "ALL" ? (
                <>
                  <h6 className="text-muted mb-1">Total Groups</h6>
                  <h4 className="fw-bold">{assetGroups.length}</h4>
                </>
              ) : (
                <>
                  <h6 className="text-muted mb-1">Groups:</h6>
                  <h4 className="fw-bold">
                    {assetGroups.find(
                      (g) => String(g.id) === String(selectedGroupId)
                    )?.name || "-"}
                  </h4>
                </>
              )}
            </div>
          </div>

          <div className="col-md-3">
            <div className="card shadow-sm border-0 rounded-3 text-center p-3">
              <h6 className="text-muted mb-1">Total Assets</h6>
              <h4 className="fw-bold">{summary.total}</h4>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card shadow-sm border-0 rounded-3 text-center p-3">
              <h6 className="text-muted mb-1">In Use</h6>
              <h4 className="fw-bold text-warning">{summary.inUse}</h4>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card shadow-sm border-0 rounded-3 text-center p-3">
              <h6 className="text-muted mb-1">Available</h6>
              <h4
                className={`fw-bold ${
                  summary.available < 5 ? "text-danger" : "text-success"
                }`}
              >
                {summary.available}
              </h4>
            </div>
          </div>
        </div>

        <div className="card border-0 bg-white shadow-sm rounded-3 mb-4">
          <div className="card-body d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div className="d-flex align-items-center gap-3">
              <button
                className="btn btn-link tm-link p-0 d-inline-flex align-items-center "
                onClick={() => setSortAsc((s) => !s)}
              >
                <i className="bi bi-arrow-down-up me-1 gap-3"></i>
                Sort
              </button>
              <div className="input-group tm-search">
                <span className="input-group-text bg-white border-end-0">
                  <i className="bi bi-search"></i>
                </span>
                <input
                  type="text"
                  className="form-control border-start-0"
                  placeholder="Search asset / group"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </div>
            </div>

            <button
              type="button"
              className="btn btn-primary"
              data-bs-toggle="modal"
              data-bs-target="#groupModal"
              onClick={clearFormGroup}
            >
              <i className="bi bi-plus-lg me-1"></i> Create Asset Group
            </button>
          </div>
        </div>

        {/* ===== Sidebar & Asset Grid ===== */}
        <div className="row g-4">
          {/* ===== Sidebar ===== */}
          <div className="col-lg-3">
            <div className="sidebar-modern card border-0 shadow-sm rounded-4 p-3">
              <h6 className="fw-semibold text-primary mb-3">
                <i className="bi bi-diagram-3 me-2"></i>Asset Groups
              </h6>

              <div className="list-group modern-list">
                <button
                  type="button"
                  className={`list-group-item list-group-item-action ${
                    selectedGroupId === "ALL" ? "active" : ""
                  }`}
                  onClick={() => setSelectedGroupId("ALL")}
                >
                  <i className="bi bi-collection me-2"></i> All Groups
                </button>

                {filteredGroups.map((g) => {
                  const availableCount = assets.filter(
                    (a) =>
                      String(a.assetGroupId) === String(g.id) &&
                      a.status === "available"
                  ).length;
                  const threshold = g.threshold || 5;
                  const isLow = availableCount <= threshold;

                  return (
                    <div
                      key={g.id}
                      className={`list-group-item list-group-item-action d-flex justify-content-between align-items-center ${
                        String(selectedGroupId) === String(g.id) ? "active" : ""
                      }`}
                      onClick={() => setSelectedGroupId(g.id)}
                    >
                      {/* ✅ ฝั่งซ้าย: ชื่อ + icon เตือน */}
                      <div className="d-flex align-items-center gap-2">
                        {isLow && (
                          <i
                            className="bi bi-exclamation-circle-fill text-danger"
                            style={{ fontSize: "0.95rem" }}
                            title={`เหลือ ${availableCount} (ต่ำกว่า threshold ${threshold})`}
                          ></i>
                        )}
                        <span className="text-truncate">{g.name}</span>
                      </div>

                      {/* ✅ ฝั่งขวา: ปุ่ม action */}
                      <div className="btn-group btn-group-sm">
                        <button
                          className="btn text-success p-0 me-2"
                          data-bs-toggle="modal"
                          data-bs-target="#assetModal"
                          onClick={(e) => {
                            e.stopPropagation();
                            clearFormAsset(g.id);
                          }}
                        >
                          <i className="bi bi-plus-circle"></i>
                        </button>
                        <button
                          className="btn text-primary p-0 me-2"
                          data-bs-toggle="modal"
                          data-bs-target="#groupModal"
                          onClick={(e) => {
                            e.stopPropagation();
                            setEditingGroupId(g.id);
                            setGroupName(g.name);
                          }}
                        >
                          <i className="bi bi-pencil"></i>
                        </button>
                        <button
                          className="btn text-danger p-0"
                          onClick={(e) => {
                            e.stopPropagation();
                            onDeleteGroup(g);
                          }}
                        >
                          <i className="bi bi-trash"></i>
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* ===== Asset Table ===== */}
          <div className="col-lg-9">
            <div className="card border-0 shadow-sm rounded-3 overflow-hidden">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="text-center">Order</th>
                    <th>Asset Name</th>
                    <th>Group</th>
                    <th>Floor</th>
                    <th>Room</th>
                    <th>Status</th>
                    <th className="text-center">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pageRows.length ? (
                    pageRows.map((row, idx) => (
                      <tr key={row.assetId}>
                        <td className="text-center">{startIndex + idx + 1}</td>
                        <td>{row.assetName}</td>
                        <td>
                          {assetGroups.find((g) => g.id === row.assetGroupId)
                            ?.name || "-"}
                        </td>
                        <td>{row.floor || "-"}</td>
                        <td>{row.room || "-"}</td>
                        <td>
                          <span
                            className={`badge rounded-pill ${
                              row.status === "in_use"
                                ? "bg-warning text-dark"
                                : row.status === "available"
                                ? "bg-success"
                                : "bg-secondary"
                            }`}
                          >
                            {row.status}
                          </span>
                        </td>
                        <td className="text-center">
                          <button
                            className="btn btn-sm text-primary me-2"
                            data-bs-toggle="modal"
                            data-bs-target="#assetModal"
                            onClick={() => {
                              setEditingAssetId(row.assetId);
                              setFormName(row.assetName);
                              setFormGroupId(row.assetGroupId);
                            }}
                          >
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button
                            className="btn btn-sm text-danger"
                            onClick={() => onDeleteAsset(row)}
                          >
                            <i className="bi bi-trash"></i>
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="7" className="text-center py-4">
                        No assets found
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
              totalRecords={filteredAssets.length}
              onPageSizeChange={handlePageSizeChange}
            />
          </div>
        </div>
      </div>

      {/* ===== Group Modal ===== */}
      <Modal
        id="groupModal"
        title={editingGroupId ? "Edit Asset Group" : "Create Asset Group"}
        icon="bi bi-box"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSaveGroup();
          }}
        >
          <div className="mb-3">
            <label className="form-label">Group Name</label>
            <input
              type="text"
              className="form-control"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
            />
          </div>
          <div className="mb-3">
            <label>Monthly Add-on Fee</label>
            <input
              type="number"
              className="form-control"
              value={monthlyAddonFee}
              onChange={(e) => setMonthlyAddonFee(e.target.value)}
            />
          </div>

          <div className="mb-3">
            <label>One-time Damage Fee</label>
            <input
              type="number"
              className="form-control"
              value={oneTimeDamageFee}
              onChange={(e) => setOneTimeDamageFee(e.target.value)}
            />
          </div>

          <div className="form-check mb-3">
            <input
              className="form-check-input"
              type="checkbox"
              checked={freeReplacement}
              onChange={(e) => setFreeReplacement(e.target.checked)}
            />
            <label className="form-check-label">Free Replacement</label>
          </div>
          <div className="d-flex justify-content-center gap-3 pt-3 pb-2">
            <button
              type="button"
              className="btn btn-outline-secondary"
              data-bs-dismiss="modal"
              id="modalGroup_btnClose"
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </Modal>

      {/* ===== Asset Modal ===== */}
      <Modal
        id="assetModal"
        title={editingAssetId ? "Edit Asset" : "Create Asset"}
        icon="bi bi-box"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSaveAsset();
          }}
        >
          <div className="mb-3">
            <label className="form-label">Asset Name</label>
            <input
              type="text"
              className="form-control"
              value={formName}
              onChange={(e) => setFormName(e.target.value)}
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Asset Group</label>
            <select
              className="form-select"
              value={formGroupId}
              onChange={(e) => setFormGroupId(e.target.value)}
              disabled={!!editingAssetId}
            >
              <option value="">Select Group</option>
              {assetGroups.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.name}
                </option>
              ))}
            </select>
          </div>
          <div className="mb-3">
            <label className="form-label">Quantity (optional)</label>
            <input
              type="number"
              className="form-control"
              min="1"
              value={formQty}
              onChange={(e) => setFormQty(e.target.value)}
              disabled={!!editingAssetId}
            />
          </div>
          <div className="d-flex justify-content-center gap-3 pt-3 pb-2">
            <button
              type="button"
              className="btn btn-outline-secondary"
              data-bs-dismiss="modal"
              id="modalAsset_btnClose"
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  );
}

export default AssetManagement;
