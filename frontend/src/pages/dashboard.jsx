// frontend/src/pages/Dashboard.jsx
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
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [visibleRoom, setVisibleRoom] = useState(null);

  // ✅ ดึงข้อมูลจาก backend
  useEffect(() => {
    fetch("http://localhost:8080/dashboard")
      .then((res) => res.json())
      .then((data) => {
        console.log("📊 Dashboard API:", data);
        setRooms(data.rooms || []);
        setMaintains(data.maintains || []);
        setFinances(data.finances || []);
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

  const getRoomUsageData = (roomNumber) => ({
  title: `Room ${roomNumber} - Usage`,
  categories: ["Jan", "Feb", "Mar", "Apr"],
  series: [
    { name: "Electricity (kWh)", data: [20, 30, 25, 40] },
    { name: "Water (m³)", data: [5, 8, 6, 7] },
  ],
  yTitle: "Usage",
  csvCategoryName: "Month",
});

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
        <div className="row g-4">
          {/* 🏠 Room Overview */}
          <div className="col-12">
            <div className="card border-0 shadow-sm rounded-3">
              <div className="card-body">
                <h5 className="card-title mb-3">Room Overview</h5>

                {floors.map((floor) => (
                  <div key={floor} className="mb-4">
                    <h6 className="fw-semibold mb-2">Floor {floor}</h6>

                    {/* ✅ ใช้ room_floor แทนการคำนวณ */}
                    <div className="d-flex flex-wrap gap-3 py-2 px-1">
                      {rooms
                        .filter((r) => r.room_floor === floor)
                        .sort(
                          (a, b) =>
                            Number(a.roomNumber) - Number(b.roomNumber)
                        ) // ✅ แปลงเป็นตัวเลขก่อนเรียง
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

                    {/* ✅ แสดงกราฟแบบ fade-in/out ทีละตัว */}
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
                            <h6 className="fw-semibold text-primary mb-2">
                              Usage for Room {visibleRoom}
                            </h6>
                            <BarChart {...getRoomUsageData(visibleRoom)} />
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
                        style={{ backgroundColor: "#22c55e" }}>&nbsp;</span> Available
                  </span>
                  <span className="me-3">
                    <span className="badge me-1"
                          style={{ backgroundColor: "#ef4444" }}>&nbsp;</span> Unavailable
                  </span>
                  <span>
                    <span className="badge me-1"
                          style={{ backgroundColor: "#facc15" }}>&nbsp;</span> Repair
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
                  Request Overview (Last 12 months)
                </h5>
                <LineChart
                  title="Maintenance Requests"
                  categories={maintainCategories}
                  series={maintainSeries}
                />
              </div>
            </div>
          </div>

          {/* 💰 Finance History */}
          <div className="col-lg-6">
            <div className="card border-0 shadow-sm rounded-3 h-100">
              <div className="card-body">
                <h5 className="card-title mb-3">
                  Finance History (Last 12 months)
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
