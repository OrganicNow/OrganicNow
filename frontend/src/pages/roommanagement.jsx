import React, { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Layout from "../component/layout";
import Modal from "../component/modal";
import Pagination from "../component/pagination";
import { useToast } from "../component/Toast.jsx";
import { pageSize as defaultPageSize, apiPath } from "../config_variable";
import "../assets/css/roommanagement.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";


function RoomManagement() {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

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

  const [sortAsc, setSortAsc] = useState(true);

  const fetchRooms = async () => {
    try {
      const res = await axios.get(`${apiPath}/room/list`, { withCredentials: true });
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

  useEffect(() => {
    fetchRooms();
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

  const filteredData = useMemo(() => {
    let filtered = [...data];

    if (filters.floor !== "ALL")
      filtered = filtered.filter((room) => String(room.roomFloor) === filters.floor);

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

  // ✅ States สำหรับ modal “Add Room”
  const [roomNumber, setRoomNumber] = useState("");
  const [roomSize, setRoomSize] = useState("");
  const [selectedFloor, setSelectedFloor] = useState("");

  // ✅ บันทึกห้องใหม่ (เรียก backend)
  const handleSaveRoom = async (e) => {
    e.preventDefault();
    if (!roomNumber || !selectedFloor || !roomSize) {
      showError("Please fill all required fields!");
      return;
    }

    try {
      const payload = {
        roomNumber: roomNumber,
        roomFloor: parseInt(selectedFloor, 10),
        roomSize: roomSize,
      };

      await axios.post(`${apiPath}/room`, payload, { withCredentials: true });

      showSuccess("Room added successfully!");
      await fetchRooms();

      // ปิด modal
      document.getElementById("addRoomModal_btnClose").click();

      // รีเซ็ตฟอร์ม
      setRoomNumber("");
      setRoomSize("");
      setSelectedFloor("");
    } catch (err) {
      console.error("Error adding room:", err);
      showError("Failed to add room.");
    }
  };

  return (
    <Layout title="Room Management" icon="bi bi-building" notifications={3}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          <div className="col-lg-11 p-4">
            {/* ✅ Toolbar */}
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
                    >
                      <i className="bi bi-plus-lg me-1"></i> Add Room
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* ✅ Table */}
            {error && <p className="text-danger">{error}</p>}
            <div className="table-wrapper">
              <table className="table text-nowrap align-middle">
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
                        <tr key={item.roomId}>
                          <td>{startIdx + idx + 1}</td>
                          <td>{item.roomNumber}</td>
                          <td>{item.roomFloor}</td>
                          <td>{item.roomSize || "-"}</td>
                          <td>
                            <StatusPill status={item.status} />
                          </td>
                          <td className="text-center">
                            {item.requests?.some((req) => req.finishDate === null)
                              ? "●"
                              : "-"}
                          </td>
                          <td>
                            <button
                              className="btn btn-sm form-Button-Edit"
                              onClick={() => navigate(`/roomdetail/${item.roomId}`)}
                            >
                              <i className="bi bi-eye-fill" />
                            </button>
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
      <Modal id="addRoomModal" title="Add Room" icon="bi bi-building" size="modal-lg">
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
          <button type="button" className="btn-close" data-bs-dismiss="offcanvas"></button>
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
