import React, { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import { pageSize as defaultPageSize, apiPath } from "../config_variable";
import useMessage from "../component/useMessage";
import "../assets/css/roommanagement.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

function RoomManagement() {
  const navigate = useNavigate();

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [pageSize, setPageSize] = useState(defaultPageSize);
  const [data, setData] = useState([]);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({
    floor: "ALL",
    roomSize: "ALL",
    status: "ALL",
    pendingRequests: "ALL",
    search: "",
  });

  const {
    showMessagePermission,
    showMessageError,
    showMessageSave,
    showMessageConfirmDelete,
  } = useMessage();

  const [modalAssets, setModalAssets] = useState([]);
  const [selectedAsset, setSelectedAsset] = useState([]);
  const [sortAsc, setSortAsc] = useState(true);

  const [roomNumber, setRoomNumber] = useState("");
  const [roomSize, setRoomSize] = useState("");
  const [selectedFloor, setSelectedFloor] = useState("");

  // ✅ ดึงข้อมูลห้อง
  const fetchRooms = async () => {
    try {
      const res = await axios.get(`${apiPath}/room/list`, {
        withCredentials: true,
      });
      if (Array.isArray(res.data)) {
        const sortedData = [...res.data].sort(
          (a, b) => parseInt(a.roomNumber, 10) - parseInt(b.roomNumber, 10)
        );
        setData(sortedData);
      } else {
        setError("Invalid response format");
      }
    } catch (err) {
      console.error("Error fetching rooms:", err);
      setError("Failed to fetch room data");
    }
  };

  // ✅ ดึงข้อมูล asset ที่ available
  const fetchAvailableAssets = async () => {
    try {
      const res = await axios.get(`${apiPath}/assets/available`, {
        withCredentials: true,
      });
      if (res.data && Array.isArray(res.data.result)) {
        setModalAssets(res.data.result);
        setSelectedAsset([]); // 🧹 ล้าง checkbox ทุกครั้งหลังโหลด assets ใหม่
      } else {
        setModalAssets([]);
        setError("Failed to fetch available assets");
      }
    } catch (err) {
      console.error("Error fetching assets:", err);
      setError("Failed to fetch asset data");
    }
  };

  useEffect(() => {
    fetchRooms();
    fetchAvailableAssets();
  }, []);

  useEffect(() => {
    const total = data.length;
    const pages = Math.max(1, Math.ceil(total / pageSize));
    setTotalRecords(total);
    setTotalPages(pages);
    setCurrentPage((p) => Math.min(Math.max(1, p), pages));
  }, [data, pageSize]);

  const startIdx = (currentPage - 1) * pageSize;

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) setCurrentPage(page);
  };

  const handlePageSizeChange = (size) => {
    setPageSize(size);
    setCurrentPage(1);
  };

  const getPendingRequestsCount = (room) => {
    if (!room.requests) return 0;
    return room.requests.filter((req) => req.finishDate === null).length;
  };

  const filteredData = useMemo(() => {
    let filtered = [...data];

    if (filters.floor !== "ALL")
      filtered = filtered.filter(
        (room) => String(room.roomFloor) === filters.floor
      );

    if (filters.status !== "ALL")
      filtered = filtered.filter((room) => room.status === filters.status);

    if (filters.roomSize !== "ALL")
      filtered = filtered.filter((room) => room.roomSize === filters.roomSize);

    if (filters.pendingRequests !== "ALL") {
      filtered = filtered.filter((room) => {
        if (filters.pendingRequests === "pending") {
          return room.requests?.some((req) => req.finishDate === null);
        }
        return room.requests?.every((req) => req.finishDate !== null);
      });
    }

    if (filters.search.trim() !== "") {
      const keyword = filters.search.toLowerCase();
      filtered = filtered.filter(
        (room) =>
          room.roomNumber.toLowerCase().includes(keyword) ||
          room.status.toLowerCase().includes(keyword)
      );
    }

    filtered.sort((a, b) => {
      const roomA = parseInt(a.roomNumber, 10);
      const roomB = parseInt(b.roomNumber, 10);
      return sortAsc ? roomA - roomB : roomB - roomA;
    });

    return filtered;
  }, [data, filters, sortAsc]);

  const handleSort = () => setSortAsc((prev) => !prev);

  const StatusPill = ({ status }) => (
    <span
      className={`badge rounded-pill ${
        status === "repair"
          ? "bg-warning text-dark"
          : status === "occupied"
          ? "bg-danger"
          : "bg-success"
      }`}
    >
      {status === "repair"
        ? "Repair"
        : status === "occupied"
        ? "Unavailable"
        : "Available"}
    </span>
  );

  const handleDeleteRoom = async (roomId) => {
<<<<<<< HEAD
    const result = await showMessageConfirmDelete(`room #${roomId}`);
    if (!result.isConfirmed) return;

    try {
      await axios.delete(`${apiPath}/room/${roomId}`, { withCredentials: true });
      showMessageSave();
      await fetchRooms();              // ✅ refresh rooms
      await fetchAvailableAssets();    // ✅ refresh asset list
=======

    try {
      await axios.delete(`${apiPath}/room/${roomId}`, {
        withCredentials: true,
      });
      showMessageSave("Room deleted successfully!");
      await fetchRooms(); // ✅ refresh rooms
      await fetchAvailableAssets(); // ✅ refresh asset list
>>>>>>> f88e7a40f80460f3b336a41bbe20336a38657894
    } catch (err) {
      console.error("Error deleting room:", err);
      showMessageError("Failed to delete room.");
    }
  };

  // ✅ เลือก asset
  const handleAssetSelect = (e, assetId) => {
    setSelectedAsset((prev) => {
      if (e.target.checked) {
        return prev.includes(assetId) ? prev : [...prev, assetId];
      } else {
        return prev.filter((id) => id !== assetId);
      }
    });
  };

  // ✅ บันทึกห้อง
  const handleSaveRoom = async (e) => {
    e.preventDefault();

    if (!roomNumber || !selectedFloor || !roomSize) {
      showMessageError("กรุณากรอกข้อมูลให้ครบทุกช่อง");
      return;
    }

    try {
      const payload = {
        roomNumber,
        roomFloor: parseInt(selectedFloor, 10),
        roomSize,
      };

      const res = await axios.post(`${apiPath}/room`, payload, {
        withCredentials: true,
      });

      const newRoom = res.data;
      const newRoomId = newRoom.roomId || newRoom.id;

      if (selectedAsset.length > 0 && newRoomId) {
        await axios.put(`${apiPath}/room/${newRoomId}/assets`, selectedAsset, {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        });
      }

      // ✅ แสดงการ์ด success
      showMessageSave("บันทึกข้อมูลสำเร็จ");

      await fetchRooms();
      await fetchAvailableAssets();

      document.getElementById("addRoomModal_btnClose").click();

      setRoomNumber("");
      setRoomSize("");
      setSelectedFloor("");
      setSelectedAsset([]);
    } catch (err) {
      console.error("Error adding room:", err);

      // ❌ แสดงการ์ด error
      showMessageError("เกิดข้อผิดพลาดในการเพิ่มห้อง");
    }
  };

  return (
    <Layout title="Room Management" icon="bi bi-building" notifications={3}>
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
                      data-bs-target="#roomFilterCanvas"
                    >
                      <i className="bi bi-filter me-1"></i> Filter
                    </button>

                    <button
                      className="btn btn-link tm-link p-0"
                      onClick={handleSort}
                    >
                      <i className="bi bi-arrow-down-up me-1"></i> Sort
                    </button>

                    <div className="input-group tm-search">
                      <span className="input-group-text bg-white border-end-0">
                        <i className="bi bi-search text-secondary"></i>
                      </span>
                      <input
                        type="text"
                        className="form-control border-start-0"
                        placeholder="Search"
                        value={filters.search}
                        onChange={(e) =>
                          setFilters({ ...filters, search: e.target.value })
                        }
                      />
                    </div>
                  </div>

                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#addRoomModal"
                      onClick={() => {
                        setSelectedAsset([]);
                        console.log(
                          "🧹 Cleared selectedAsset when opening modal"
                        );
                      }}
                    >
                      <i className="bi bi-plus-lg me-1"></i> Add Room
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Table */}
            {error && <p className="text-danger">{error}</p>}
            <div className="table-wrapper">
              <table className="table text-nowrap align-middle room-table">
                <thead>
                  <tr className="header-color">
                    <th>Order</th>
                    <th>Room</th>
                    <th>Floor</th>
                    <th>Size</th>
                    <th>Status</th>
                    <th>Pending Requests</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredData.length > 0 ? (
                    filteredData
                      .slice(startIdx, startIdx + pageSize)
                      .map((item, idx) => (
                        <tr key={item.roomId || item.id}>
                          <td>{startIdx + idx + 1}</td>
                          <td>{item.roomNumber}</td>
                          <td>{item.roomFloor}</td>
                          <td>{item.roomSize || "-"}</td>
                          <td>
                            <StatusPill status={item.status} />
                          </td>
                          <td className="text-center">
                            {getPendingRequestsCount(item)}
                          </td>
                          <td>
                            <div className="d-flex gap-2">
                              <button
                                className="btn btn-sm form-Button-Edit"
                                onClick={() =>
                                  navigate(
                                    `/roomdetail/${item.roomId || item.id}`
                                  )
                                }
                              >
                                <i className="bi bi-eye-fill" />
                              </button>

                              {/* 🗑️ ปุ่มลบ */}
                              <button
                                className="btn btn-sm form-Button-Del"
                                onClick={async () => {
                                  const result = await showMessageConfirmDelete(
                                    item.roomNumber
                                  );
                                  if (result.isConfirmed) {
                                    handleDeleteRoom(item.roomId);
                                  }
                                }}
                                aria-label="Delete"
                              >
                                <i className="bi bi-trash-fill"></i>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))
                  ) : (
                    <tr>
                      <td colSpan="7" className="text-center py-3 text-muted">
                        Data Not Found
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

      {/* ✅ Modal: Add Room */}
      <Modal
        id="addRoomModal"
        title="Add Room"
        icon="bi bi-building"
        size="modal-lg"
      >
        <form onSubmit={handleSaveRoom}>
          <div className="mb-3">
            <label className="form-label">Room Number</label>
            <input
              type="text"
              className="form-control"
              placeholder="Room Number"
              value={roomNumber}
              onChange={(e) => setRoomNumber(e.target.value)}
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Floor</label>
            <input
              type="number"
              className="form-control"
              placeholder="Enter Floor"
              value={selectedFloor}
              onChange={(e) => setSelectedFloor(e.target.value)}
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Room Size</label>
            <select
              className="form-select"
              value={roomSize}
              onChange={(e) => setRoomSize(e.target.value)}
            >
              <option value="">Select Size</option>
              <option value="Studio">Studio</option>
              <option value="Superior">Superior</option>
              <option value="Deluxe">Deluxe</option>
            </select>
          </div>

          {/* ✅ Asset Selection */}
          <div className="mb-3">
            <label className="form-label">Select Assets for this Room</label>
            <div className="d-flex flex-wrap gap-3">
              {modalAssets.length > 0 ? (
                modalAssets.map((asset) => {
                  const id = asset.assetId ?? asset.id;
                  return (
                    <div key={id} className="form-check">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id={`asset-${id}`}
                        value={id}
                        checked={selectedAsset.includes(id)}
                        onChange={(e) => handleAssetSelect(e, id)}
                      />
                      <label
                        className="form-check-label"
                        htmlFor={`asset-${id}`}
                      >
                        {asset.assetName}
                      </label>
                    </div>
                  );
                })
              ) : (
                <p className="text-muted">No available assets</p>
              )}
            </div>
          </div>

          <div className="d-flex justify-content-center gap-3 pt-3">
            <button
              type="button"
              className="btn btn-outline-secondary"
              data-bs-dismiss="modal"
              id="addRoomModal_btnClose"
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Save
            </button>
          </div>
        </form>
      </Modal>
      {/* ✅ Offcanvas Filter */}
      <div
        className="offcanvas offcanvas-end"
        tabIndex="-1"
        id="roomFilterCanvas"
        data-bs-backdrop="static"
      >
        <div className="offcanvas-header">
          <h5 className="mb-0">
            <i className="bi bi-filter me-2"></i> Filters
          </h5>
          <button
            type="button"
            className="btn-close"
            data-bs-dismiss="offcanvas"
          ></button>
        </div>

        <div className="offcanvas-body">
          {/* 🟦 Floor */}
          <div className="mb-3">
            <label className="form-label">Floor</label>
            <select
              className="form-select"
              value={filters.floor}
              onChange={(e) =>
                setFilters({ ...filters, floor: e.target.value })
              }
            >
              <option value="ALL">All</option>
              {[...new Set(data.map((r) => r.roomFloor))].map((floor) => (
                <option key={floor} value={floor}>
                  {floor}
                </option>
              ))}
            </select>
          </div>

          {/* 🟦 Room Size */}
          <div className="mb-3">
            <label className="form-label">Room Size</label>
            <select
              className="form-select"
              value={filters.roomSize}
              onChange={(e) =>
                setFilters({ ...filters, roomSize: e.target.value })
              }
            >
              <option value="ALL">All</option>
              {[...new Set(data.map((r) => r.roomSize || "-"))].map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </div>

          {/* 🟦 Status */}
          <div className="mb-3">
            <label className="form-label">Status</label>
            <select
              className="form-select"
              value={filters.status}
              onChange={(e) =>
                setFilters({ ...filters, status: e.target.value })
              }
            >
              <option value="ALL">All</option>
              <option value="available">Available</option>
              <option value="occupied">Occupied</option>
              <option value="repair">Repair</option>
            </select>
          </div>

          {/* 🟦 Pending Requests */}
          <div className="mb-3">
            <label className="form-label">Pending Requests</label>
            <select
              className="form-select"
              value={filters.pendingRequests}
              onChange={(e) =>
                setFilters({ ...filters, pendingRequests: e.target.value })
              }
            >
              <option value="ALL">All</option>
              <option value="pending">Pending Only</option>
              <option value="none">No Pending</option>
            </select>
          </div>

          {/* 🟦 Buttons */}
          <div className="d-flex justify-content-between mt-4">
            <button
              className="btn btn-outline-secondary"
              onClick={() =>
                setFilters({
                  floor: "ALL",
                  roomSize: "ALL",
                  status: "ALL",
                  pendingRequests: "ALL",
                  search: "",
                })
              }
            >
              Clear
            </button>
            <button className="btn btn-primary" data-bs-dismiss="offcanvas">
              Apply
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default RoomManagement;
