import React, { useState, useEffect, useMemo } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Layout from "../component/layout";
import Modal from "../component/modal";
import useMessage from "../component/useMessage";
import { useToast } from "../component/Toast.jsx";
import Pagination from "../component/pagination";
import { pageSize as defaultPageSize, apiPath } from "../config_variable";
import "../assets/css/tenantmanagement.css";
import "../assets/css/alert.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

function TenantManagement() {
  const { showSuccess, showError, showWarning } = useToast();
  const navigate = useNavigate();

  // 📦 Pagination & Data
  const [data, setData] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(defaultPageSize);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);

  // 👤 Tenant form fields
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [nationalId, setNationalId] = useState("");
  const [signDate, setSignDate] = useState(
    () => new Date().toISOString().split("T")[0]
  );
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [deposit, setDeposit] = useState(0);
  const [rentAmountSnapshot, setRentAmountSnapshot] = useState(0);
  const [packageId, setPackageId] = useState("");
  const [selectedFloor, setSelectedFloor] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");

  // 🏢 Reference data
  const [rooms, setRooms] = useState([]);
  const [packages, setPackages] = useState([]);
  const [allPackages, setAllPackages] = useState([]);
  const [occupiedRoomIds, setOccupiedRoomIds] = useState([]);
  const [roomSizeForSelectedRoom, setRoomSizeForSelectedRoom] = useState(null);

  // 🔍 Filter + Sort + Search
  const [filters, setFilters] = useState({
    contractName: "ALL",
    floor: "ALL",
    room: "ALL",
  });
  const [sortAsc, setSortAsc] = useState(false);
  const [search, setSearch] = useState("");

  // 🔍 Search tenants (fuzzy via backend)
  useEffect(() => {
    const fetchSearchResults = async () => {
      const keyword = search.trim();
      if (keyword === "") {
        // ถ้าไม่มี keyword → โหลด list ปกติ
        fetchData(1);
        return;
      }

      try {
        const res = await axios.get(`${apiPath}/tenant/search`, {
          params: { keyword },
          withCredentials: true,
        });

        if (res.data && Array.isArray(res.data.results)) {
          setData(res.data.results);
          setTotalRecords(res.data.totalRecords ?? res.data.results.length);
          setTotalPages(1);
          setCurrentPage(1);
        } else {
          setData([]);
          setTotalRecords(0);
        }
      } catch (err) {
        console.error("Error searching tenants:", err);
        setData([]);
        setTotalRecords(0);
      }
    };

    // ⏳ debounce เพื่อไม่ยิง API ทุก keypress (รอ 400ms)
    const timeoutId = setTimeout(fetchSearchResults, 400);
    return () => clearTimeout(timeoutId);
  }, [search]);

  const {
    showMessagePermission,
    showMessageError,
    showMessageSave,
    showMessageConfirmDelete,
  } = useMessage();

  // 📅 Utility function: format date
  const formatDate = (v) => {
    if (!v) return "-";
    try {
      const d = new Date(v);
      const yyyy = d.getFullYear();
      const mm = String(d.getMonth() + 1).padStart(2, "0");
      const dd = String(d.getDate()).padStart(2, "0");
      return `${yyyy}-${mm}-${dd}`;
    } catch {
      return v;
    }
  };

  // 🧾 โหลด package ทั้งหมด
  useEffect(() => {
    const fetchPackages = async () => {
      try {
        const res = await axios.get(`${apiPath}/packages`, {
          withCredentials: true,
        });

        if (Array.isArray(res.data)) {
          setAllPackages(res.data);
          const activeSorted = res.data
            .filter((p) => p.is_active === 1)
            .sort((a, b) => a.duration - b.duration);
          setPackages(activeSorted);
        } else {
          setAllPackages([]);
          setPackages([]);
        }
      } catch (err) {
        console.error("Error fetching packages:", err);
        setAllPackages([]);
        setPackages([]);
      }
    };
    fetchPackages();
  }, []);

  // 💰 ตั้งค่า deposit / rent จาก package ที่เลือก
  useEffect(() => {
    if (packageId) {
      const pkg = packages.find((p) => p.id === parseInt(packageId));
      if (pkg) {
        const price = pkg.price ?? 0;
        setRentAmountSnapshot(price);
        setDeposit(price);
      }
    }
  }, [packageId, packages]);

  const packageLabel = (pkgId) => {
    const pkg = allPackages.find((p) => p.id === pkgId);
    return pkg ? pkg.contract_name : "-";
  };

  const packageColor = (contractName) => {
    const map = {
      "3 เดือน": "#FFC73B",
      "6 เดือน": "#EF98C4",
      "9 เดือน": "#87C6FF",
      "1 ปี": "#9691F9",
    };
    return map[contractName] || "#D3D3D3";
  };

  // 🏠 โหลดข้อมูลห้อง
  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const res = await axios.get(`${apiPath}/room/list`, {
          withCredentials: true,
        });
        if (Array.isArray(res.data)) setRooms(res.data);
        else if (Array.isArray(res.data.result)) setRooms(res.data.result);
        else setRooms([]);
      } catch (err) {
        console.error("Error fetching rooms:", err);
        setRooms([]);
      }
    };
    fetchRooms();
  }, []);

  useEffect(() => {
    if (selectedRoomId) {
      const selectedRoom = rooms.find(
        (r) => r.roomId === parseInt(selectedRoomId)
      );
      if (selectedRoom) {
        setRoomSizeForSelectedRoom(
          selectedRoom.roomSize ?? selectedRoom.room_size ?? null
        );
      } else {
        setRoomSizeForSelectedRoom(null);
      }
    } else {
      setRoomSizeForSelectedRoom(null);
    }
  }, [selectedRoomId, rooms]);

  // 📋 โหลด tenant ทั้งหมด
  const fetchData = async (page = 1) => {
    try {
      const res = await axios.get(`${apiPath}/tenant/list`, {
        withCredentials: true,
        params: { page, pageSize },
      });

      if (Array.isArray(res.data)) {
        const rows = res.data;
        setData(rows);
        setTotalRecords(rows.length);
        setTotalPages(Math.max(1, Math.ceil(rows.length / pageSize)));
      } else if (res.data && Array.isArray(res.data.results)) {
        const rows = res.data.results;
        setData(rows);
        const total = Number(res.data.totalRecords ?? rows.length);
        setTotalRecords(total);
        setTotalPages(Math.max(1, Math.ceil(total / pageSize)));
      } else {
        setData([]);
        setTotalRecords(0);
        setTotalPages(1);
      }

      setCurrentPage(page);
      await fetchOccupiedRooms();
    } catch (err) {
      console.error("Error fetching tenants:", err);
      setData([]);
      setTotalRecords(0);
      setTotalPages(1);
      setOccupiedRoomIds([]);
    }
  };

  useEffect(() => {
    fetchData(1);
  }, [pageSize]);

  // 🏢 ห้องที่ถูกจองแล้ว
  const fetchOccupiedRooms = async () => {
    try {
      const res = await axios.get(`${apiPath}/contracts/occupied-rooms`, {
        withCredentials: true,
      });
      if (Array.isArray(res.data)) setOccupiedRoomIds(res.data);
      else setOccupiedRoomIds([]);
    } catch (err) {
      console.error("Error fetching occupied rooms:", err);
      setOccupiedRoomIds([]);
    }
  };

  useEffect(() => {
    fetchOccupiedRooms();
  }, [data]);

  // 📄 Pagination control
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = Math.min(startIndex + pageSize, data.length);
  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
      fetchData(page);
    }
  };
  const handlePageSizeChange = (size) => {
    setPageSize(size);
    fetchData(1);
    setCurrentPage(1);
  };

  // 🧾 Create Tenant
  const handleSaveCreate = async () => {
    try {
      const payload = {
        firstName,
        lastName,
        email,
        phoneNumber,
        nationalId,
        roomId: selectedRoomId,
        packageId,
        startDate: startDate ? `${startDate}T00:00:00` : null,
        endDate: endDate ? `${endDate}T23:59:59` : null,
        deposit,
        rentAmountSnapshot,
        signDate: signDate ? `${signDate}T00:00:00` : null,
      };

      if (!checkValidation(payload)) return;

      const res = await axios.post(`${apiPath}/tenant/create`, payload, {
        withCredentials: true,
      });

      if (res.status === 200 || res.status === 201) {
        document.getElementById("modalForm_btnClose")?.click();
        fetchData(currentPage);
        showMessageSave();
        showSuccess("🎉 เพิ่มผู้เช่าใหม่สำเร็จแล้ว!");
      } else {
        showMessageError("Unexpected response: " + JSON.stringify(res.data));
        showError(`❌ เพิ่มผู้เช่าล้มเหลว: HTTP ${res.status}`);
      }
    } catch (e) {
      if (e.response) {
        const msg = e.response.data?.message || "";
        if (
          e.response.status === 409 &&
          msg.includes("duplicate_national_id")
        ) {
          showMessageError("NationalID Already Exists");
          showWarning("⚠️ บัตรประชาชนนี้มีอยู่ในระบบแล้ว");
          return;
        }
        if (e.response.status === 401) {
          showMessagePermission?.();
          showWarning("⚠️ ไม่มีสิทธิ์ในการเพิ่มผู้เช่า");
          return;
        }
        showMessageError(msg || "Server error");
        showError(`❌ เพิ่มผู้เช่าล้มเหลว: ${msg}`);
      } else {
        showMessageError(e.message || "Unexpected error");
        showError(`❌ เพิ่มผู้เช่าล้มเหลว: ${e.message}`);
      }
    }
  };

  // ✅ Validation
  const checkValidation = (payload) => {
    if (!payload.firstName) return showMessageError("กรุณากรอก First Name");
    if (!payload.lastName) return showMessageError("กรุณากรอก Last Name");
    if (!payload.nationalId) return showMessageError("กรุณากรอก National ID");
    if (!/^\d{13}$/.test(payload.nationalId))
      return showMessageError("National ID ต้องเป็นตัวเลข 13 หลัก");
    if (!payload.phoneNumber) return showMessageError("กรุณากรอก Phone Number");
    if (!/^\d{10}$/.test(payload.phoneNumber))
      return showMessageError("Phone Number ต้องเป็นตัวเลข 10 หลัก");
    if (!payload.email) return showMessageError("กรุณากรอก Email");
    if (!payload.roomId) return showMessageError("กรุณาเลือกห้อง");
    if (!payload.packageId) return showMessageError("กรุณาเลือก Package");
    if (!payload.startDate) return showMessageError("กรุณาเลือก Start Date");
    if (!payload.signDate) return showMessageError("กรุณาเลือก Sign Date");

    const sign = new Date(payload.signDate);
    const start = new Date(payload.startDate);
    if (start < sign)
      return showMessageError("Start Date ต้องมากกว่าหรือเท่ากับ Sign Date");

    return true;
  };

  // ❌ Delete Tenant
  const handleDelete = async (contractId) => {
    if (!contractId) {
      showMessageError("❌ contractId is missing");
      return;
    }

    try {
      const res = await axios.delete(`${apiPath}/tenant/delete/${contractId}`, {
        withCredentials: true,
      });

      if (res.status === 204) {
        showMessageSave("ลบข้อมูลสำเร็จ");
        showSuccess("🗑️ ลบผู้เช่าสำเร็จแล้ว!");
        fetchData(currentPage);
      } else {
        showMessageError("Unexpected response: " + res.status);
      }
    } catch (e) {
      if (e.response && e.response.status === 401) {
        showMessagePermission?.();
        showWarning("⚠️ ไม่มีสิทธิ์ในการลบข้อมูล");
      } else {
        showMessageError(e);
        showError(`❌ ลบผู้เช่าล้มเหลว: ${e.message}`);
      }
    }
  };

  // 🔁 Clear form
  const clearForm = () => {
    const today = new Date().toISOString().split("T")[0];
    setSignDate(today);
    setStartDate("");
    setEndDate("");
    setDeposit("");
    setRentAmountSnapshot("");
    setFirstName("");
    setLastName("");
    setEmail("");
    setPhoneNumber("");
    setNationalId("");
    setSelectedFloor("");
    setSelectedRoomId("");
    setPackageId("");
    fetchOccupiedRooms();
  };

  // ⏳ Auto calculate endDate from package duration
  useEffect(() => {
    if (startDate && packageId) {
      const pkg = packages.find((p) => p.id === parseInt(packageId));
      if (pkg && pkg.duration) {
        const start = new Date(startDate);
        const end = new Date(start);
        end.setMonth(end.getMonth() + pkg.duration);
        const yyyy = end.getFullYear();
        const mm = String(end.getMonth() + 1).padStart(2, "0");
        const dd = String(end.getDate()).padStart(2, "0");
        setEndDate(`${yyyy}-${mm}-${dd}`);
      }
    }
  }, [startDate, packageId, packages]);

  // 👁️ View Tenant Detail
  const handleViewTenant = (item) => {
    navigate(`/tenantdetail/${item.contractId}`);
  };

  // 📥 Download Contract PDF
  const handleDownloadPdf = async (contractId) => {
    const res = await axios.get(`${apiPath}/tenant/${contractId}/pdf`, {
      responseType: "blob",
      withCredentials: true,
    });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", `tenant_${contractId}_contract.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  // 🔍 Filter + Sort (หลังจาก search จาก backend แล้ว)
  const filteredData = useMemo(() => {
    // search ถูกจัดการโดย backend แล้ว ไม่ต้อง filter local อีก
    let rows = [...data];

    // ✅ Filter ตาม contract, floor, room
    if (filters.contractName !== "ALL")
      rows = rows.filter((r) => r.contractName === filters.contractName);

    if (filters.floor !== "ALL")
      rows = rows.filter((r) => String(r.floor) === String(filters.floor));

    if (filters.room !== "ALL")
      rows = rows.filter((r) => r.room === filters.room);

    // ✅ Sort ข้อมูล
    rows.sort((a, b) => {
      // ถ้า status = 0 (หมดสัญญา) ให้ไปอยู่ล่างสุด
      if (a.status === 0 && b.status !== 0) return 1;
      if (a.status !== 0 && b.status === 0) return -1;

      // เรียงตามวันที่เริ่มสัญญา
      const dateA = new Date(a.startDate || 0);
      const dateB = new Date(b.startDate || 0);
      return sortAsc ? dateA - dateB : dateB - dateA;
    });

    return rows;
  }, [data, filters, sortAsc]);

  // 🧩 Contract type dropdown for filter
  const contractTypeOptions = useMemo(() => {
    if (!Array.isArray(allPackages)) return [];
    const map = new Map();

    for (const p of allPackages) {
      if (p.contract_name && !map.has(p.contract_name)) {
        map.set(p.contract_name, {
          id: p.contract_name,
          name: p.contract_name,
          duration: p.duration,
        });
      }
    }

    return Array.from(map.values()).sort((a, b) => a.duration - b.duration);
  }, [allPackages]);

  return (
    <Layout title="Tenant Management" icon="pi pi-user" notifications={3}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          {/* Main */}
          <div className="col-lg-11">
            {/* Toolbar Card */}
            <div className="toolbar-wrapper card border-0 bg-white">
              <div className="card-header bg-white border-0 rounded-3">
                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                  {/* Left cluster */}
                  <div className="d-flex align-items-center gap-3">
                    <button
                      className="btn btn-link tm-link p-0"
                      data-bs-toggle="offcanvas"
                      data-bs-target="#tenantFilterCanvas"
                    >
                      <i className="bi bi-filter me-1"></i> Filter
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
                        placeholder="Search"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                      />
                    </div>
                  </div>

                  {/* Right cluster */}
                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#exampleModal"
                      onClick={clearForm}
                    >
                      <i className="bi bi-plus-lg me-1"></i> Create Tenant
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Data Table */}
            <div className="table-wrapper">
              <table className="table text-nowrap">
                <thead>
                  <tr>
                    <th className="text-center align-top header-color">
                      Order
                    </th>
                    <th className="align-top header-color">First name</th>
                    <th className="text-start align-top header-color">
                      Last name
                    </th>
                    <th className="text-center align-top header-color">
                      Floor
                    </th>
                    <th className="text-center align-top header-color">Room</th>
                    <th className="text-center align-top header-color">
                      Package
                    </th>
                    <th className="text-center align-top header-color">Rent</th>
                    <th className="text-start align-top header-color">
                      End date
                    </th>
                    <th className="text-start align-top header-color">
                      Phone Number
                    </th>
                    <th className="text-center align-top header-color">
                      Action
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {filteredData.length > 0 ? (
                    filteredData
                      .slice(startIndex, endIndex)
                      .map((item, idxInPage) => {
                        const globalIndex = startIndex + idxInPage;
                        const order = globalIndex + 1;
                        const rowKey =
                          item.tenantId ??
                          item.id ??
                          `${item.firstName}-${item.room}-${globalIndex}`;

                        return (
                          <tr
                            key={rowKey}
                            className={
                              item.status === 0 ? "table-secondary" : ""
                            } // 👈 ถ้า status=0 แสดงสีเทาอ่อน
                          >
                            {/* Order */}
                            <td className="align-top text-center">{order}</td>
                            <td className="align-top text-center">
                              {item.firstName}
                            </td>
                            <td className="align-top text-start">
                              {item.lastName}
                            </td>
                            <td className="align-top text-center">
                              {item.floor ?? "-"}
                            </td>
                            <td className="align-top text-center">
                              {item.room ?? "-"}
                            </td>

                            <td className="align-top text-start">
                              <span
                                className="badge rounded-pill px-3"
                                style={{
                                  backgroundColor: packageColor(
                                    packageLabel(item.packageId)
                                  ),
                                  color: "#fff",
                                }}
                              >
                                {packageLabel(item.packageId)}
                              </span>
                            </td>

                            <td className="align-top text-start">
                              {item.rentAmountSnapshot
                                ? `${Number(
                                    item.rentAmountSnapshot
                                  ).toLocaleString()}`
                                : "-"}
                            </td>
                            <td className="align-top text-start">
                              {formatDate(item.endDate)}
                            </td>
                            <td className="align-top text-start">
                              {item.phoneNumber || "-"}
                            </td>

                            <td className="align-top text-center">
                              <button
                                className="btn btn-sm form-Button-Edit"
                                onClick={() => handleViewTenant(item)}
                                aria-label="View"
                              >
                                <i className="bi bi-eye-fill"></i>
                              </button>
                              <button
                                className="btn btn-sm form-Button-Edit"
                                onClick={() =>
                                  handleDownloadPdf(item.contractId)
                                }
                              >
                                <i className="bi bi-file-earmark-pdf-fill"></i>
                              </button>
                              <button
                                className="btn btn-sm form-Button-Del"
                                onClick={async () => {
                                  const result = await showMessageConfirmDelete(
                                    item.firstName
                                  );
                                  if (result.isConfirmed) {
                                    handleDelete(item.contractId);
                                  }
                                }}
                                aria-label="Delete"
                              >
                                <i className="bi bi-trash-fill"></i>
                              </button>
                            </td>
                          </tr>
                        );
                      })
                  ) : (
                    <tr>
                      <td colSpan="12" className="text-center align-middle">
                        <div className="d-flex justify-content-center align-items-center">
                          Data Not Found
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <Pagination
              currentPage={currentPage}
              totalPages={Math.max(
                1,
                Math.ceil(filteredData.length / pageSize)
              )}
              onPageChange={handlePageChange}
              totalRecords={filteredData.length}
              onPageSizeChange={handlePageSizeChange}
            />
          </div>
          {/* /Main */}
        </div>
      </div>

      <Modal
        id="exampleModal"
        title="Add User"
        icon="bi bi-person-plus"
        size="modal-lg"
        scrollable="modal-dialog-scrollable"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSaveCreate();
          }}
        >
          {/* ---------- General Information ---------- */}
          <div className="mb-4">
            <div className="fw-semibold mb-2">General Information</div>

            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">First Name</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Tenant First Name"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Last Name</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Tenant Last Name"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">National ID</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Tenant National ID"
                  value={nationalId}
                  onChange={(e) => {
                    const val = e.target.value.replace(/\D/g, "");
                    if (val.length <= 13) setNationalId(val);
                  }}
                  maxLength={13}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Phone Number</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Tenant Phone Number"
                  value={phoneNumber}
                  onChange={(e) => {
                    const val = e.target.value.replace(/\D/g, "");
                    if (val.length <= 10) setPhoneNumber(val);
                  }}
                  maxLength={10}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input
                  type="email"
                  className="form-control"
                  placeholder="Tenant Email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>
          </div>
          {/* ---------- Room Information ---------- */}
          <div className="mb-4">
            <div className="fw-semibold mb-2">Room Information</div>
            <div className="row g-3">
              {/* Floor Select */}
              <div className="col-md-6">
                <label className="form-label">Floor</label>
                <div className="position-relative">
                  <select
                    className="form-select"
                    value={selectedFloor}
                    onChange={(e) => {
                      setSelectedFloor(e.target.value);
                      setSelectedRoomId("");
                    }}
                  >
                    <option value="">Select Floor</option>
                    {[...new Set(rooms.map((r) => r.roomFloor))].map(
                      (floor) => (
                        <option key={floor} value={floor}>
                          {floor}
                        </option>
                      )
                    )}
                  </select>
                </div>
              </div>

              {/* Room Select */}
              <div className="col-md-6">
                <label className="form-label">Room</label>
                <div className="position-relative">
                  <select
                    className="form-select"
                    value={selectedRoomId}
                    onChange={(e) => setSelectedRoomId(e.target.value)}
                    readOnly={!selectedFloor}
                  >
                    <option value="">
                      {selectedFloor ? "Select Room" : "Choose floor first"}
                    </option>
                    {rooms
                      .filter(
                        (r) => String(r.roomFloor) === String(selectedFloor)
                      )
                      .filter((r) => !occupiedRoomIds.includes(r.roomId)) // ✅ ห้องหมดสัญญาจะกลับมาเลือกได้
                      .map((r) => (
                        <option key={r.roomId} value={r.roomId}>
                          {r.roomNumber}
                        </option>
                      ))}
                  </select>
                </div>

                {/* 🔹 แสดงขนาดห้องหลังเลือก */}
                {roomSizeForSelectedRoom !== null && (
                  <div className="text-muted small mt-1">
                    Room Size:&nbsp;
                    {(() => {
                      // normalize เพื่อให้แน่ใจว่าเทียบได้ทุกกรณี
                      const v = String(roomSizeForSelectedRoom)
                        .trim()
                        .toLowerCase();
                      if (v === "0" || v === "studio") return "Studio";
                      if (v === "1" || v === "superior") return "Superior";
                      if (v === "2" || v === "deluxe") return "Deluxe";
                      return v; // fallback กรณี backend ส่งชื่ออื่น
                    })()}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* ---------- Contract Information ---------- */}
          <div className="mb-4">
            <div className="fw-semibold mb-2">Contract Information</div>

            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Package</label>
                <div className="position-relative">
                  <select
                    className="form-select"
                    value={packageId}
                    onChange={(e) => setPackageId(e.target.value)}
                    disabled={!selectedRoomId} // 🔒 ต้องเลือกห้องก่อนถึงเปิดได้
                  >
                    <option value="">
                      {selectedRoomId ? "Select Package" : "Select Room First"}
                    </option>

                    {packages
                      .filter((p) => {
                        if (roomSizeForSelectedRoom == null) return true;

                        const v = String(roomSizeForSelectedRoom)
                          .trim()
                          .toLowerCase();
                        const pkgV = String(p.room_size).trim().toLowerCase();

                        // handle both numeric and text matches (หลังสลับ Deluxe ↔ Superior)
                        if (v === pkgV) return true;
                        if (
                          (v === "studio" && pkgV === "0") ||
                          (v === "0" && pkgV === "studio")
                        )
                          return true;
                        if (
                          (v === "superior" && pkgV === "1") ||
                          (v === "1" && pkgV === "superior")
                        )
                          return true;
                        if (
                          (v === "deluxe" && pkgV === "2") ||
                          (v === "2" && pkgV === "deluxe")
                        )
                          return true;

                        return false;
                      })
                      .map((p) => {
                        let roomSizeLabel = "-";
                        switch (String(p.room_size)) {
                          case "0":
                            roomSizeLabel = "Studio";
                            break;
                          case "1":
                            roomSizeLabel = "Superior";
                            break;
                          case "2":
                            roomSizeLabel = "Deluxe";
                            break;
                          default:
                            roomSizeLabel = p.room_size || "-";
                        }

                        return (
                          <option key={p.id} value={p.id}>
                            {p.contract_name} – {roomSizeLabel}
                          </option>
                        );
                      })}
                  </select>
                </div>
              </div>
              <div className="col-md-6">
                <label className="form-label">Rent Amount</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Rent Amount"
                  value={rentAmountSnapshot}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Sign date</label>
                <input
                  type="date"
                  className="form-control"
                  value={signDate}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Start date</label>
                <input
                  type="date"
                  className="form-control"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">End date</label>
                <input
                  type="date"
                  className="form-control"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Deposit</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Deposit"
                  value={deposit}
                  readOnly
                />
              </div>
            </div>
          </div>

          {/* ---------- Footer Buttons ---------- */}
          <div className="d-flex justify-content-center gap-3 pt-3 pb-3">
            <button
              type="button"
              className="btn btn-outline-secondary"
              data-bs-dismiss="modal"
              id="modalForm_btnClose"
            >
              Cancel
            </button>

            <button type="submit" className="btn btn-primary">
              Save
            </button>
          </div>
        </form>
      </Modal>
      <div
        className="offcanvas offcanvas-end"
        tabIndex="-1"
        id="tenantFilterCanvas"
        data-bs-backdrop="static"
      >
        <div className="offcanvas-header">
          <h5 className="mb-0">
            <i className="bi bi-filter me-2"></i>Filters
          </h5>
          <button
            type="button"
            className="btn-close"
            data-bs-dismiss="offcanvas"
          ></button>
        </div>
        <div className="offcanvas-body">
          <div className="mb-3">
            <label className="form-label">Package</label>
            <select
              className="form-select"
              value={filters.contractName}
              onChange={(e) =>
                setFilters((f) => ({ ...f, contractName: e.target.value }))
              }
            >
              <option value="ALL">All</option>
              {contractTypeOptions.map((ct) => (
                <option key={ct.id} value={ct.id}>
                  {ct.name}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Floor</label>
            <select
              className="form-select"
              value={filters.floor}
              onChange={(e) =>
                setFilters((f) => ({ ...f, floor: e.target.value }))
              }
            >
              <option value="ALL">All</option>
              {[...new Set(rooms.map((r) => r.roomFloor))].map((floor) => (
                <option key={floor} value={floor}>
                  {floor}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Room</label>
            <select
              className="form-select"
              value={filters.room}
              onChange={(e) =>
                setFilters((f) => ({ ...f, room: e.target.value }))
              }
            >
              <option value="ALL">All</option>
              {rooms.map((r) => (
                <option key={r.id} value={r.roomNumber}>
                  {r.roomNumber}
                </option>
              ))}
            </select>
          </div>

          <div className="d-flex justify-content-between">
            <button
              className="btn btn-outline-secondary"
              onClick={() =>
                setFilters({ contractName: "ALL", floor: "ALL", room: "ALL" })
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

export default TenantManagement;
