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

                // ✅ ใช้ data.rooms (ไม่ใช่ roomStatuses)
                setRooms(
                    (data.rooms || []).map((room) => ({
                        number: parseInt(room.roomNumber, 10),
                        status: room.status, // 0 = available, 1 = unavailable, 2 = repair
                    }))
                );

                setMaintains(data.maintains || []);
                setFinances(data.finances || []);
            })
            .catch((err) => console.error("Failed to fetch dashboard:", err));
    }, []);

    // 👉 summary ห้อง
    const summary = {
        available: rooms.filter(r => r.status === 0).length,
        unavailable: rooms.filter(r => r.status === 1).length,
        repair: rooms.filter(r => r.status === 2).length,
    };

    // 👉 จัดกลุ่มห้องตามชั้น
    const floor1 = rooms.filter((r) => r.number >= 101 && r.number <= 112);
    const floor2 = rooms.filter((r) => r.number >= 201 && r.number <= 212);

    // 👉 Chart: Maintain requests
    const requestOverviewData = {
        labels: maintains.map((m) => m.month),
        datasets: [
            {
                label: "Requests",
                data: maintains.map((m) => m.total),
                borderColor: "rgba(99, 102, 241, 1)",
                backgroundColor: "rgba(99, 102, 241, 0.2)",
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
                backgroundColor: "rgba(34,197,94,0.8)", // green
            },
            {
                label: "Penalty",
                data: finances.map((f) => f.penalty),
                backgroundColor: "rgba(234,179,8,0.8)", // yellow
            },
            {
                label: "Overdue",
                data: finances.map((f) => f.overdue),
                backgroundColor: "rgba(239,68,68,0.8)", // red
            },
        ],
    };

    return (
        <Layout title="Dashboard" icon="pi pi-home" notifications={3}>
            <div className="container-fluid">
                <div className="row min-vh-100">
                    <div className="col-lg-11">
                        <div className="table-wrapper-detail rounded-0">
                            <div className="row g-4">
                                {/* Room Overview */}
                                <div className="col-lg-5">
                                    <div className="card border-0 shadow-sm rounded-2 h-100">
                                        <div className="card-body">
                                            <h5 className="card-title mb-3">Room Overview</h5>
                                            <div className="row">
                                                {/* Floor 1 */}
                                                <div className="col-6">
                                                    <h6 className="text-center">Floor 1</h6>
                                                    <div className="container">
                                                        {Array.from({
                                                            length: Math.ceil(floor1.length / 2),
                                                        }).map((_, rowIndex) => (
                                                            <div className="row mb-3" key={rowIndex}>
                                                                {floor1
                                                                    .slice(rowIndex * 2, rowIndex * 2 + 2)
                                                                    .map((room) => (
                                                                        <div
                                                                            className="col-6 d-flex justify-content-center"
                                                                            key={room.number}
                                                                        >
                                                                            <div
                                                                                className="d-flex align-items-center justify-content-center text-white fw-bold rounded"
                                                                                style={{
                                                                                    width: "85px",
                                                                                    height: "85px",
                                                                                    fontSize: "20px",
                                                                                    cursor: "pointer",
                                                                                    transition:
                                                                                        "transform 0.2s ease, filter 0.2s ease",
                                                                                    backgroundColor:
                                                                                        room.status === 0
                                                                                            ? "#22c55e" // available
                                                                                            : room.status === 1
                                                                                                ? "#ef4444" // unavailable
                                                                                                : "#facc15", // repair
                                                                                }}
                                                                            >
                                                                                {room.number}
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                                {/* Floor 2 */}
                                                <div className="col-6">
                                                    <h6 className="text-center">Floor 2</h6>
                                                    <div className="container">
                                                        {Array.from({
                                                            length: Math.ceil(floor2.length / 2),
                                                        }).map((_, rowIndex) => (
                                                            <div className="row mb-3" key={rowIndex}>
                                                                {floor2
                                                                    .slice(rowIndex * 2, rowIndex * 2 + 2)
                                                                    .map((room) => (
                                                                        <div
                                                                            className="col-6 d-flex justify-content-center"
                                                                            key={room.number}
                                                                        >
                                                                            <div
                                                                                className="d-flex align-items-center justify-content-center text-white fw-bold rounded"
                                                                                style={{
                                                                                    width: "85px",
                                                                                    height: "85px",
                                                                                    fontSize: "20px",
                                                                                    cursor: "pointer",
                                                                                    transition:
                                                                                        "transform 0.2s ease, filter 0.2s ease",
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
                                                                        </div>
                                                                    ))}
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            </div>
                                            {/* Legend */}
                                            <div className="mt-3 small text-center">
                        <span className="me-3">
                          <span className="badge bg-success me-1">&nbsp;</span>{" "}
                            Available
                        </span>
                                                <span className="me-3">
                          <span className="badge bg-danger me-1">&nbsp;</span>{" "}
                                                    Unavailable
                        </span>
                                                <span>
                          <span className="badge bg-warning me-1">&nbsp;</span>{" "}
                                                    Repair
                        </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {/* Right side */}
                                <div className="col-lg-7 d-flex flex-column">
                                    {/* Maintain Overview */}
                                    <div className="card border-0 shadow-sm flex-grow-1 mb-3 rounded-2">
                                        <div className="card-body">
                                            <h5 className="card-title">
                                                Request Overview (Last 12 months)
                                            </h5>
                                            <Line data={requestOverviewData} />
                                        </div>
                                    </div>

                                    <div className="row g-3">
                                        {/* Finance History */}
                                        <div className="col-lg-7">
                                            <div className="card border-0 shadow-sm h-100 rounded-2">
                                                <div className="card-body">
                                                    <h5 className="card-title">
                                                        Finance History (Last 12 months)
                                                    </h5>
                                                    <Bar data={financeHistoryData} />
                                                </div>
                                            </div>
                                        </div>

                                        {/* Summary */}
                                        <div className="col-lg-5">
                                            <div className="card border-0 shadow-sm h-100 rounded-2">
                                                <div className="card-body">
                                                    <h5 className="card-title">Summary</h5>
                                                    <ul className="list-unstyled">
                                                        <li className="d-flex justify-content-between">
                                                            <span>Available (0)</span>
                                                            <span className="text-success fw-bold">
                                {summary.available}
                              </span>
                                                        </li>
                                                        <li className="d-flex justify-content-between">
                                                            <span>Unavailable (1)</span>
                                                            <span className="text-danger fw-bold">
                                {summary.unavailable}
                              </span>
                                                        </li>
                                                        <li className="d-flex justify-content-between">
                                                            <span>Repair (2)</span>
                                                            <span className="text-warning fw-bold">
                                {summary.repair}
                              </span>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                {/* End right side */}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
}

export default Dashboard;
