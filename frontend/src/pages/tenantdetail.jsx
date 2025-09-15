import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import Layout from "../component/layout";
import Modal from "../component/modal";
import { apiPath } from "../config_variable";
import "../assets/css/tenantmanagement.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

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

  useEffect(() => {
    if (!tenantInfoRef.current) return;
    const observer = new ResizeObserver(([entry]) => {
      setLeftHeight(entry.contentRect.height);
    });
    observer.observe(tenantInfoRef.current);
    return () => observer.disconnect();
  }, []);

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
        return "status-warning"; // Unpaid
      case 1:
        return "status-complete"; // Paid
      case 2:
        return "status-danger"; // Overdue
      default:
        return "status-default";
    }
  };

  if (!tenant) {
    return (
      <Layout title="Tenant Management" icon="pi pi-user">
        <div className="p-4">Loading...</div>
      </Layout>
    );
  }

  const handleSaveUpdate = async () => {
    try {
      if (!contractId) {
        alert("Missing contractId");
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

      const res = await axios.put(
        `${apiPath}/tenant/update/${contractId}`,
        payload,
        { withCredentials: true }
      );

      if (res.status === 200) {
        await fetchTenantDetail(); // refresh ข้อมูล

      if (res.status === 200) {
        await fetchTenantDetail(); // refresh ข้อมูล

        // ✅ ปิด modal ด้วยการ click ปุ่ม Cancel
        document.getElementById("modalForm_btnClose")?.click();

        alert("✅ Update tenant success!");
      }
      } else {
        alert("❌ Unexpected response: " + JSON.stringify(res.data));
      }
    } catch (e) {
      console.error("Update tenant error:", e);
      alert("❌ Error: " + (e.response?.data?.message || e.message));
    }
  };

  return (
    <Layout title="Tenant Management" icon="pi pi-user" notifications={3}>
      <div className="container-fluid">
        <div className="row min-vh-100">
          {/* Main */}
          <div className="col-lg-11 p-4">
            {/* Toolbar Card */}
            <div className="toolbar-wrapper card border-0 bg-white">
              <div className="card-header bg-white border-0 rounded-3">
                <div className="tm-toolbar d-flex justify-content-between align-items-center">
                  {/* Breadcrumb */}
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
                  {/* Right cluster */}
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
                <div className="col-lg-4" ref={tenantInfoRef}>
                  <div className="card border-0 shadow-sm rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Tenant Information</h5>
                      <p>
                        <span className="label">First Name:</span>{" "}
                        <span className="value">{tenant?.firstName || "-"}</span>
                      </p>
                      <p>
                        <span className="label">Last Name:</span>{" "}
                        <span className="value">{tenant?.lastName || "-"}</span>
                      </p>
                      <p>
                        <span className="label">National ID:</span>{" "}
                        <span className="value">{tenant?.nationalId || "-"}</span>
                      </p>
                      <p>
                        <span className="label">Phone Number:</span>{" "}
                        <span className="value">{tenant?.phoneNumber || "-"}</span>
                      </p>
                      <p>
                        <span className="label">Email:</span>{" "}
                        <span className="value">{tenant?.email || "-"}</span>
                      </p>
                      <p>
                        <span className="label">Package:</span>
                        <span className="value">
                          <span className="package-badge badge bg-primary">
                            {tenant?.packageName || "-"}
                          </span>
                        </span>
                      </p>
                      <p>
                        <span className="label">Sign Date:</span>{" "}
                        <span className="value">
                          {tenant?.signDate?.split("T")[0] || "-"}
                        </span>
                      </p>
                      <p>
                        <span className="label">Start Date:</span>{" "}
                        <span className="value">
                          {tenant?.startDate?.split("T")[0] || "-"}
                        </span>
                      </p>
                      <p>
                        <span className="label">End Date:</span>{" "}
                        <span className="value">
                          {tenant?.endDate?.split("T")[0] || "-"}
                        </span>
                      </p>
                    </div>
                  </div>

                  <div className="card border-0 shadow-sm mt-3 rounded-2">
                    <div className="card-body">
                      <h5 className="card-title">Room Information</h5>
                      <p>
                        <span className="label">Floor:</span>{" "}
                        <span className="value">{tenant?.floor || "-"}</span>
                      </p>
                      <p>
                        <span className="label">Room:</span>{" "}
                        <span className="value">{tenant?.room || "-"}</span>
                      </p>
                    </div>
                  </div>
                </div>

                {/* Payment History */}
                <div className="col-lg-8 d-flex flex-column">
                  <div
                    className="card border-0 shadow-sm flex-grow-1 rounded-2"
                    style={{
                      height: `${leftHeight}px`,
                      overflowY: "auto",
                      paddingRight: "8px",
                    }}
                  >
                    <div className="card-body d-flex flex-column overflow-hidden">
                      <ul
                        className="nav nav-tabs bg-white"
                        id="historyTabs"
                        role="tablist"
                      >
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

                      <div
                        className="tab-content mt-3 overflow-auto flex-grow-1"
                        style={{ maxHeight: "500px" }}
                      >
                        <div
                          className="tab-pane fade show active"
                          id="payment"
                          role="tabpanel"
                        >
                          <div className="row row-cols-1 row-cols-md-2 g-3">
                            {tenant?.invoices?.length > 0 ? (
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
                                        <span className="label">Invoice date:  </span>
                                        <span className="value">
                                          {inv.dueDate?.split("T")[0] || "-"}
                                        </span>
                                      </div>
                                      <div className="col-4">
                                        <span className="label">Invoice ID: </span>
                                        <span className="value">
                                          {inv.invoiceId}
                                        </span>
                                      </div>
                                      <div className="col-4">
                                        <span className="label">NET:  </span>
                                        <span className="value">
                                          {inv.netAmount} Baht
                                        </span>
                                      </div>
                                    </div>
                                    <div className="row">
                                      <div className="col-4">
                                        <span className="label">Status:  </span>
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

      {/* Modal (แก้ไข Tenant) */}
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
            fetchTenantDetail()
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
                  type="tel"
                  className="form-control"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
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
                <input
                  type="date"
                  className="form-control"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                />
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