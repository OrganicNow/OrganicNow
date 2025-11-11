import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import Layout from "../component/layout";
import Modal from "../component/modal";
import { apiPath } from "../config_variable";
import "../assets/css/tenantdetail.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import useMessage from "../component/useMessage";

function TenantDetail() {
  const tenantInfoRef = useRef(null);
  const [leftHeight, setLeftHeight] = useState(0);
  const [tenant, setTenant] = useState(null);

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [nationalId, setNationalId] = useState("");
  const [endDate, setEndDate] = useState("");
  const [deposit, setDeposit] = useState("");
  const [rentAmountSnapshot, setRentAmountSnapshot] = useState("");
  const [signDate, setSignDate] = useState("");
  const [startDate, setStartDate] = useState("");

  const navigate = useNavigate();
  const { contractId } = useParams();

  // ✅ ต้องวาง Hook ตรงนี้ก่อนการ return หรือเงื่อนไขใด ๆ
  const { showMessageError, showMessageSave } = useMessage();

  // ----------------------------------------------------
  // Auto resize listener
  // ----------------------------------------------------
  useEffect(() => {
    if (!tenantInfoRef.current) return;
    const observer = new ResizeObserver(([entry]) => {
      setLeftHeight(entry.contentRect.height);
    });
    observer.observe(tenantInfoRef.current);
    return () => observer.disconnect();
  }, []);

  // ----------------------------------------------------
  // Fetch tenant detail
  // ----------------------------------------------------
  const fetchTenantDetail = async () => {
    try {
      const res = await axios.get(`${apiPath}/tenant/${contractId}`, {
        withCredentials: true,
      });
      setTenant(res.data);
      setFirstName(res.data.firstName || "");
      setLastName(res.data.lastName || "");
      setEmail(res.data.email || "");
      setPhoneNumber(res.data.phoneNumber || "");
      setNationalId(res.data.nationalId || "");
      setEndDate(res.data.endDate?.split("T")[0] || "");
      setDeposit(res.data.deposit || "");
      setRentAmountSnapshot(res.data.rentAmountSnapshot || "");
      setSignDate(res.data.signDate?.split("T")[0] || "");
      setStartDate(res.data.startDate?.split("T")[0] || "");
    } catch (err) {
      console.error("Error fetching tenant detail:", err);
      navigate("/tenantmanagement");
    }
  };

  useEffect(() => {
    if (contractId) fetchTenantDetail();
  }, [contractId]);

  // ----------------------------------------------------
  // Helpers
  // ----------------------------------------------------
  const mapStatus = (status) => {
    switch (status) {
      case 0:
        return "Unpaid";
      case 1:
        return "Paid";
      case 2:
        return "Overdue";
      default:
        return "-";
    }
  };

  const getStatusColor = (status, penaltyTotal) => {
    if (penaltyTotal && penaltyTotal > 0) {
      return "status-danger";
    }
    switch (status) {
      case 0:
        return "status-warning";
      case 1:
        return "status-complete";
      case 2:
        return "status-danger";
      default:
        return "status-default";
    }
  };

  // ----------------------------------------------------
  // Validation
  // ----------------------------------------------------
  const checkValidation = (payload) => {
    if (!payload.firstName) {
      showMessageError("กรุณากรอก First Name");
      return false;
    }
    if (!payload.lastName) {
      showMessageError("กรุณากรอก Last Name");
      return false;
    }
    if (!payload.phoneNumber) {
      showMessageError("กรุณากรอก Phone Number");
      return false;
    }
    if (!/^\d{10}$/.test(payload.phoneNumber)) {
      showMessageError("Phone Number ต้องเป็นตัวเลข 10 หลัก");
      return false;
    }
    if (!payload.email) {
      showMessageError("กรุณากรอก Email");
      return false;
    }

    const sign = new Date(payload.signDate);
    const start = new Date(payload.startDate);
    if (start < sign) {
      showMessageError("Start Date ต้องมากกว่าหรือเท่ากับ Sign Date");
      return false;
    }

    if (tenant?.contractTypeId === 4 && payload.startDate && payload.endDate) {
      const startDateObj = new Date(payload.startDate);
      const endDateObj = new Date(payload.endDate);
      const oneYearLater = new Date(startDateObj);
      oneYearLater.setFullYear(startDateObj.getFullYear() + 1);

      if (endDateObj < oneYearLater) {
        showMessageError("End Date ต้องไม่น้อยกว่า 1 ปีหลังจาก Start Date");
        return false;
      }
    }

    return true;
  };

  // ----------------------------------------------------
  // Update Tenant
  // ----------------------------------------------------
  const handleSaveUpdate = async () => {
    if (!contractId) {
      showMessageError("Missing contractId");
      return false;
    }

    const payload = {
      firstName,
      lastName,
      email,
      phoneNumber,
      nationalId,
      startDate: startDate ? `${startDate}T00:00:00` : null,
      endDate: endDate ? `${endDate}T23:59:59` : null,
      deposit,
      rentAmountSnapshot,
      signDate: signDate ? `${signDate}T00:00:00` : new Date().toISOString(),
    };

    if (checkValidation(payload) === false) return false;

    try {
      const res = await axios.put(
        `${apiPath}/tenant/update/${contractId}`,
        payload,
        { withCredentials: true }
      );

      if (res.status === 200) {
        await fetchTenantDetail();
        document.getElementById("modalForm_btnClose")?.click();
        showMessageSave("อัปเดตข้อมูลสำเร็จ!");
      } else {
        showMessageError("Unexpected response: " + JSON.stringify(res.data));
      }
    } catch (e) {
      console.error("Update tenant error:", e);
      showMessageError(e.response?.data?.message || e.message);
    }
  };

  // ----------------------------------------------------
  // Render
  // ----------------------------------------------------
  if (!tenant) {
    return (
      <Layout title="Tenant Management" icon="pi pi-user">
        <div className="p-4">Loading...</div>
      </Layout>
    );
  }

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
                  <div className="d-flex align-items-center gap-2">
                    <span
                      className="breadcrumb-link text-primary"
                      style={{ cursor: "pointer" }}
                      onClick={() => navigate("/tenantmanagement")}
                    >
                      Tenant Management
                    </span>
                    <span className="text-muted">›</span>
                    <span className="breadcrumb-current">
                      {tenant?.firstName} {tenant?.lastName}
                    </span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <button
                      type="button"
                      className="btn btn-primary"
                      data-bs-toggle="modal"
                      data-bs-target="#exampleModal"
                    >
                      <i className="bi bi-pencil me-1"></i> Edit Tenant
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Detail Section */}
            <div className="table-wrapper-detail rounded-0">
              <div className="row g-4">
                {/* Tenant Info */}
                <div className="col-lg-4 d-flex flex-column" ref={tenantInfoRef}>
                  <div className="card border-0 shadow-sm rounded-3 mb-3 flex-fill">
                    <div className="card-body">
                      <h5 className="card-title">Tenant Information</h5>
                      <p><strong>First Name:</strong> {tenant?.firstName || "-"}</p>
                      <p><strong>Last Name:</strong> {tenant?.lastName || "-"}</p>
                      <p><strong>National ID:</strong> {tenant?.nationalId || "-"}</p>
                      <p><strong>Phone Number:</strong> {tenant?.phoneNumber || "-"}</p>
                      <p><strong>Email:</strong> {tenant?.email || "-"}</p>
                      <p>
                        <strong>Package:</strong>{" "}
                        <span className="badge bg-primary">
                          {tenant?.packageName || "-"}
                        </span>
                      </p>
                      <p>
                        <strong>Rent:</strong>{" "}
                        {tenant?.rentAmountSnapshot
                          ? `${tenant.rentAmountSnapshot}`
                          : "-"}
                      </p>
                      <p>
                        <strong>Sign Date:</strong>{" "}
                        {tenant?.signDate
                          ? new Date(tenant.signDate).toLocaleDateString("th-TH")
                          : "-"}
                      </p>
                      <p>
                        <strong>Start Date:</strong>{" "}
                        {tenant?.startDate
                          ? new Date(tenant.startDate).toLocaleDateString("th-TH")
                          : "-"}
                      </p>
                      <p>
                        <strong>End Date:</strong>{" "}
                        {tenant?.endDate
                          ? new Date(tenant.endDate).toLocaleDateString("th-TH")
                          : "-"}
                      </p>
                    </div>
                  </div>

                  <div className="card border-0 shadow-sm rounded-3 flex-fill">
                    <div className="card-body">
                      <h5 className="card-title">Room Information</h5>
                      <p><strong>Floor:</strong> {tenant?.floor || "-"}</p>
                      <p><strong>Room:</strong> {tenant?.room || "-"}</p>
                    </div>
                  </div>
                </div>

                {/* Payment History */}
                <div className="col-lg-8 d-flex flex-column">
                  <div className="card border-0 shadow-sm flex-grow-1 rounded-2">
                    <div className="card-body d-flex flex-column overflow-hidden">
                      <ul className="nav nav-tabs bg-white" id="historyTabs" role="tablist">
                        <li className="nav-item" role="presentation">
                          <button
                            className="nav-link active"
                            id="payment-tab"
                            data-bs-toggle="tab"
                            data-bs-target="#payment"
                            type="button"
                            role="tab"
                          >
                            Payment History
                          </button>
                        </li>
                      </ul>

                      <div className="tab-content mt-3 flex-grow-1">
                        <div className="tab-pane fade show active" id="payment" role="tabpanel">
                          <div className="row row-cols-1 row-cols-md-2 g-3">
                            {Array.isArray(tenant?.invoices) && tenant.invoices.length > 0 ? (
                              tenant.invoices.map((inv, idx) => (
                                <div className="col-lg-12" key={idx}>
                                  <div
                                    className={`status-card ${getStatusColor(
                                      inv.invoiceStatus,
                                      inv.penaltyTotal
                                    )} d-flex flex-column`}
                                  >
                                    <div className="row mb-1">
                                      <div className="col-4">
                                        <span className="label">Invoice date: </span>
                                        <span className="value">
                                          {inv.dueDate?.split("T")[0] || "-"}
                                        </span>
                                      </div>
                                      <div className="col-4">
                                        <span className="label">Invoice ID: </span>
                                        <span className="value">{inv.invoiceId}</span>
                                      </div>
                                      <div className="col-4">
                                        <span className="label">NET: </span>
                                        <span className="value">{inv.netAmount} Baht</span>
                                      </div>
                                    </div>
                                    <div className="row">
                                      <div className="col-4">
                                        <span className="label">Status: </span>
                                        <span className="value">
                                          {mapStatus(inv.invoiceStatus)}
                                        </span>
                                      </div>
                                      <div className="col-4">
                                        <span className="label">Pay date: </span>
                                        <span className="value">
                                          {inv.payDate?.split("T")[0] || "-"}
                                        </span>
                                      </div>
                                      <div className="col-4">
                                        <span className="label">Penalty: </span>
                                        <span className="value">
                                          {inv.penaltyTotal || "-"}
                                        </span>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              ))
                            ) : (
                              <p>No invoices found</p>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          {/* /Main */}
        </div>
      </div>

      {/* Modal (Edit Tenant) */}
      <Modal
        id="exampleModal"
        title="Edit Tenant"
        icon="bi bi-pencil"
        size="modal-lg"
        scrollable="modal-dialog-scrollable"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSaveUpdate();
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
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Last Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">National ID</label>
                <input
                  type="text"
                  className="form-control"
                  value={nationalId}
                  readOnly
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
              <div className="col-md-6">
                <label className="form-label">Floor</label>
                <input
                  type="text"
                  className="form-control"
                  defaultValue={tenant?.floor || ""}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Room</label>
                <input
                  type="text"
                  className="form-control"
                  defaultValue={tenant?.room || ""}
                  readOnly
                />
              </div>
            </div>
          </div>

          {/* ---------- Contract Information ---------- */}
          <div className="mb-4">
            <div className="fw-semibold mb-2">Contract Information</div>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Package</label>
                <input
                  type="text"
                  className="form-control"
                  defaultValue={tenant?.packageName || ""}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Rent Amount</label>
                <input
                  type="text"
                  className="form-control"
                  value={rentAmountSnapshot}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Sign Date</label>
                <input
                  type="date"
                  className="form-control"
                  value={signDate}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Start Date</label>
                <input
                  type="date"
                  className="form-control"
                  value={startDate}
                  readOnly
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">End Date</label>
                <div className="position-relative">
                  <input
                    type="date"
                    className="form-control"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    readOnly={tenant?.contractTypeId !== 4}
                    style={{
                      backgroundColor:
                        tenant?.contractTypeId !== 4 ? "#f5f5f5" : "white",
                      cursor:
                        tenant?.contractTypeId !== 4 ? "not-allowed" : "text",
                    }}
                  />
                  {tenant?.contractTypeId !== 4 && (
                    <small
                      className="text-muted position-absolute"
                      style={{ bottom: "-18px", left: "2px" }}
                    >
                      (แก้ไขได้เฉพาะสัญญา 1 ปี)
                    </small>
                  )}
                </div>
              </div>
              <div className="col-md-6">
                <label className="form-label">Deposit</label>
                <input
                  type="text"
                  className="form-control"
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
    </Layout>
  );
}

export default TenantDetail;