// Dashboard.jsx
import React, { useEffect, useState } from "react";
import { Line, Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  LineElement,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Tooltip,
  Legend,
} from "chart.js";

import Layout from "../component/layout";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

ChartJS.register(
  LineElement,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Tooltip,
  Legend
);

function Dashboard() {
  const [rooms, setRooms] = useState([]);
  const [maintains, setMaintains] = useState([]);
  const [finances, setFinances] = useState([]);

  // 👉 ดึงข้อมูลจาก backend
  useEffect(() => {
    fetch("http://localhost:8080/dashboard")
      .then((res) => res.json())
      .then((data) => {
        console.log("📊 Dashboard API:", data);

        // ✅ ใช้ข้อมูลจริงจาก backend
        setRooms(
          (data.rooms || []).map((room) => ({
            number: parseInt(room.roomNumber, 10),
            status: room.status, // 0=available, 1=unavailable, 2=repair
            floor: room.room_floor, // ⬅️ ใช้ค่าจาก backend
          }))
        );

        setMaintains(data.maintains || []);
        setFinances(data.finances || []);
      })
      .catch((err) => console.error("Failed to fetch dashboard:", err));
  }, []);

  // ✅ แยกห้องตาม floor จาก backend (unique floors)
  const floors = [...new Set(rooms.map((r) => r.floor))].sort((a, b) => a - b);

  // 👉 Chart: Maintain requests
  const requestOverviewData = {
    labels: maintains.map((m) => m.month),
    datasets: [
      {
        label: "Requests",
        data: maintains.map((m) => m.total),
        borderColor: "rgba(99,102,241,1)",
        backgroundColor: "rgba(99,102,241,0.2)",
        tension: 0.4,
        fill: true,
      },
    ],
  };

  // 👉 Chart: Finance overview
  const financeHistoryData = {
    labels: finances.map((f) => f.month),
    datasets: [
      {
        label: "On Time",
        data: finances.map((f) => f.onTime),
        backgroundColor: "rgb(166,70,255)",
      },
      {
        label: "Penalty",
        data: finances.map((f) => f.penalty),
        backgroundColor: "rgba(84,191,255)",
      },
      {
        label: "Overdue",
        data: finances.map((f) => f.overdue),
        backgroundColor: "rgba(255,108,191)",
      },
    ],
  };

  return (
    <Layout title="Dashboard" icon="pi pi-home" notifications={3}>
      <div className="container-fluid p-4">
        <div className="row g-4">
          {/* 🏠 Room Overview */}
          <div className="col-12">
            <div className="card border-0 shadow-sm rounded-3">
              <div className="card-body">
                <h5 className="card-title mb-3">Room Overview</h5>

                {/* ✅ แสดงทุกชั้นแบบ dynamic */}
                {floors.map((floor) => (
                  <div key={floor} className="mb-4">
                    <h6 className="fw-semibold mb-2">Floor {floor}</h6>
                    <div className="d-flex flex-nowrap overflow-auto gap-3 py-2 px-1">
                      {rooms
                        .filter((r) => r.floor === floor)
                        .sort((a, b) => a.number - b.number)
                        .map((room) => (
                          <div
                            key={room.number}
                            className="d-flex align-items-center justify-content-center text-white fw-bold rounded flex-shrink-0"
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
                            }}
                          >
                            {room.number}
                          </div>
                        ))}
                    </div>
                  </div>
                ))}

                {/* Legend */}
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

          {/* 📊 Request Overview (Left) */}
          <div className="col-lg-6">
            <div className="card border-0 shadow-sm rounded-3 h-100">
              <div className="card-body">
                <h5 className="card-title mb-3">
                  Request Overview (Last 12 months)
                </h5>
                <Line data={requestOverviewData} />
              </div>
            </div>
          </div>

          {/* 💰 Finance History (Right) */}
          <div className="col-lg-6">
            <div className="card border-0 shadow-sm rounded-3 h-100">
              <div className="card-body">
                <h5 className="card-title mb-3">
                  Finance History (Last 12 months)
                </h5>
                <Bar data={financeHistoryData} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Dashboard;
