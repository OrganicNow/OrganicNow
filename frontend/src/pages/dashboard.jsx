import React, { useEffect, useState } from "react";
import LineChart from "../component/LineChart.jsx";
import BarChart from "../component/BarChart.jsx";
import Layout from "../component/layout";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import { motion, AnimatePresence } from "framer-motion";

function Dashboard() {
  const [rooms, setRooms] = useState([]);
  const [maintains, setMaintains] = useState([]);
  const [finances, setFinances] = useState([]);
  const [usages, setUsages] = useState({});
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [visibleRoom, setVisibleRoom] = useState(null);

  // ✅ dropdown เดือน
  const [months, setMonths] = useState([]);
  const [selectedMonth, setSelectedMonth] = useState("");

  // ✅ ดึงข้อมูลจาก backend
  useEffect(() => {
    fetch("http://localhost:8080/dashboard")
      .then((res) => res.json())
      .then((data) => {
        console.log("📊 Dashboard API:", data);
        setRooms(data.rooms || []);
        setMaintains(data.maintains || []);
        setFinances(data.finances || []);
        setUsages(data.usages || {});

        // ✅ สร้าง dropdown เดือน (จาก finance หรือ maintain)
        const uniqueMonths = [
          ...new Set((data.finances || []).map((f) => f.month)),
        ];
        setMonths(uniqueMonths);
        if (uniqueMonths.length > 0)
          setSelectedMonth(uniqueMonths[uniqueMonths.length - 1]); // ค่าเริ่มต้น = เดือนล่าสุด
      })
      .catch((err) => console.error("Failed to fetch dashboard:", err));
  }, []);

  // ✅ ใช้ room_floor จาก backend โดยตรง
  const floors = [...new Set(rooms.map((r) => r.room_floor))].sort(
    (a, b) => a - b
  );

  // ✅ toggle graph + delay animation
  const handleRoomClick = (roomNumber) => {
    if (selectedRoom === roomNumber) {
      setSelectedRoom(null);
      setTimeout(() => setVisibleRoom(null), 400);
    } else {
      setSelectedRoom(roomNumber);
      if (visibleRoom) {
        setVisibleRoom(null);
        setTimeout(() => setVisibleRoom(roomNumber), 400);
      } else {
        setVisibleRoom(roomNumber);
      }
    }
  };

  // ✅ ฟังก์ชันโหลด CSV
  // ✅ ฟังก์ชันโหลด CSV
  const handleDownloadCsv = async () => {
    if (!selectedMonth) {
      alert("⚠️ กรุณาเลือกเดือนก่อนดาวน์โหลด");
      return;
    }

    // 🔧 เปลี่ยน "Nov 2025" → "Nov_2025" เพื่อให้ URL ใช้งานได้
    const formattedMonth = selectedMonth.replace(" ", "_");

    try {
      const res = await fetch(
        `http://localhost:8080/dashboard/export/${formattedMonth}`
      );
      if (!res.ok) throw new Error("Failed to download CSV");

      // ✅ แปลง blob เป็นไฟล์ CSV
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `Usage_Report_${selectedMonth}.csv`;
      a.click();
      window.URL.revokeObjectURL(url);

      // ✅ แจ้งผลลัพธ์เมื่อสำเร็จ
      alert(`✅ ดาวน์โหลดไฟล์สำเร็จ: Usage_Report_${selectedMonth}.csv`);
    } catch (error) {
      console.error("❌ Download error:", error);
      alert("ไม่สามารถดาวน์โหลดไฟล์ได้");
    }
  };


  // ✅ Request Overview (รวม)
  const maintainCategories = maintains.map((m) => m.month);
  const maintainSeries = [
    { name: "Requests", data: maintains.map((m) => m.total) },
  ];

  // ✅ Finance Overview (รวม)
  const financeCategories = finances.map((f) => f.month);
  const financeSeries = [
    { name: "On Time", data: finances.map((f) => f.onTime) },
    { name: "Penalty", data: finances.map((f) => f.penalty) },
    { name: "Overdue", data: finances.map((f) => f.overdue) },
  ];

  return (
    <Layout title="Dashboard" icon="pi pi-home" notifications={3}>
      <div className="container-fluid p-4">
        {/* 🔽 ส่วนดาวน์โหลด CSV */}
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
          <h4 className="fw-semibold mb-0">
            Dashboard Overview
          </h4>
          <div className="d-flex align-items-center gap-2">
            <select
              className="form-select"
              style={{ width: "200px" }}
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(e.target.value)}
            >
              <option value="">Select Month</option>
              {months.map((m) => (
                <option key={m} value={m}>
                  {m}
                </option>
              ))}
            </select>

            <button
              className="btn btn-outline-primary d-flex align-items-center gap-2"
              onClick={handleDownloadCsv}
            >
              <i className="bi bi-download"></i>
              Download CSV
            </button>
          </div>
        </div>

        <div className="row g-4">
          {/* 🏠 Room Overview */}
          <div className="col-12">
            <div className="card border-0 shadow-sm rounded-3">
              <div className="card-body">
                <h5 className="card-title mb-3">Room Overview</h5>

                {floors.map((floor) => (
                  <div key={floor} className="mb-4">
                    <h6 className="fw-semibold mb-2">Floor {floor}</h6>

                    {/* ✅ แสดงปุ่มห้อง */}
                    <div className="d-flex flex-wrap gap-3 py-2 px-1">
                      {rooms
                        .filter((r) => r.room_floor === floor)
                        .sort(
                          (a, b) =>
                            Number(a.roomNumber) - Number(b.roomNumber)
                        )
                        .map((room) => (
                          <button
                            key={room.roomNumber}
                            className="border-0 text-white fw-bold rounded"
                            style={{
                              width: "85px",
                              height: "85px",
                              fontSize: "20px",
                              cursor: "pointer",
                              backgroundColor:
                                room.status === 0
                                  ? "#22c55e"
                                  : room.status === 1
                                    ? "#ef4444"
                                    : "#facc15",
                              transition:
                                "transform 0.15s ease, box-shadow 0.15s",
                              transform:
                                selectedRoom === room.roomNumber
                                  ? "scale(1.08)"
                                  : "scale(1)",
                              boxShadow:
                                selectedRoom === room.roomNumber
                                  ? "0 0 10px rgba(34,197,94,0.6)"
                                  : "none",
                            }}
                            onClick={() => handleRoomClick(room.roomNumber)}
                          >
                            {room.roomNumber}
                          </button>
                        ))}
                    </div>

                    {/* ✅ แสดงกราฟเส้นแบบ fade-in/out */}
                    <AnimatePresence mode="wait">
                      {visibleRoom &&
                        rooms.some(
                          (r) =>
                            r.room_floor === floor &&
                            r.roomNumber === visibleRoom
                        ) && (
                          <motion.div
                            key={visibleRoom}
                            initial={{ opacity: 0, y: -15 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -15 }}
                            transition={{ duration: 0.4, ease: "easeInOut" }}
                            className="mt-3"
                          >
                            <h6 className="fw-semibold text-primary mb-3">
                              Usage for Room {visibleRoom}
                            </h6>

                            {(() => {
                              const usage = usages?.[visibleRoom];
                              if (!usage)
                                return (
                                  <p className="text-muted fst-italic">
                                    No usage data available
                                  </p>
                                );

                              const categories = usage.categories || [];
                              const waterSeries = usage.series.find((s) =>
                                s.name.includes("Water")
                              );
                              const electricSeries = usage.series.find((s) =>
                                s.name.includes("Electricity")
                              );

                              return (
                                <div className="row">
                                  {/* 💧 Water Chart */}
                                  <div className="col-12 col-md-6 mb-4">
                                    <div className="card border-0 shadow-sm rounded-3 h-100">
                                      <div className="card-body">
                                        <h6 className="card-title text-info fw-semibold">
                                          Water Usage
                                        </h6>
                                        <LineChart
                                          title=""
                                          categories={categories}
                                          series={[waterSeries]}
                                          colors={["#3b82f6"]}
                                          yTitle="Water Unit"
                                          csvCategoryName="Month"
                                          fileName={`Water_Usage_Room_${visibleRoom}`}
                                        />
                                      </div>
                                    </div>
                                  </div>

                                  {/* ⚡ Electricity Chart */}
                                  <div className="col-12 col-md-6 mb-4">
                                    <div className="card border-0 shadow-sm rounded-3 h-100">
                                      <div className="card-body">
                                        <h6 className="card-title text-warning fw-semibold">
                                          Electricity Usage
                                        </h6>
                                        <LineChart
                                          title=""
                                          categories={categories}
                                          series={[electricSeries]}
                                          colors={["#facc15"]}
                                          yTitle="Electricity Unit"
                                          csvCategoryName="Month"
                                          fileName={`Electricity_Usage_Room_${visibleRoom}`}
                                        />
                                      </div>
                                    </div>
                                  </div>

                                </div>
                              );
                            })()}
                          </motion.div>
                        )}
                    </AnimatePresence>
                  </div>
                ))}

                {/* ✅ Legend */}
                <div className="mt-4 small text-center">
                  <span className="me-3">
                    <span
                      className="badge me-1"
                      style={{ backgroundColor: "#22c55e" }}
                    >
                      &nbsp;
                    </span>
                    Available
                  </span>
                  <span className="me-3">
                    <span
                      className="badge me-1"
                      style={{ backgroundColor: "#ef4444" }}
                    >
                      &nbsp;
                    </span>
                    Unavailable
                  </span>
                  <span>
                    <span
                      className="badge me-1"
                      style={{ backgroundColor: "#facc15" }}
                    >
                      &nbsp;
                    </span>
                    Repair
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* 📊 Request Overview */}
          <div className="col-lg-6">
            <div className="card border-0 shadow-sm rounded-3 h-100">
              <div className="card-body">
                <h5 className="card-title mb-3">
                  Request Overview (Last 6 months)
                </h5>
                <LineChart
                  title="Maintenance Requests"
                  categories={maintainCategories}
                  series={maintainSeries}
                  fileName={`Maintenance_request_6_month`}
                />
              </div>
            </div>
          </div>

          {/* 💰 Finance History */}
          <div className="col-lg-6">
            <div className="card border-0 shadow-sm rounded-3 h-100">
              <div className="card-body">
                <h5 className="card-title mb-3">
                  Finance History (Last 6 months)
                </h5>
                <BarChart
                  title="Finance History"
                  categories={financeCategories}
                  series={financeSeries}
                  yTitle="Transactions"
                  csvCategoryName="Month"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Dashboard;
