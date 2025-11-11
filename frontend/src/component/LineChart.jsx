// frontend/src/components/LineChart.jsx
import React from "react";
import ReactApexChart from "react-apexcharts";

const LineChart = ({
  title = "Line Chart",
  categories = [],
  series = [],
  categoryLabel = "Month", // ✅ เพิ่ม prop ใหม่สำหรับชื่อ category column
}) => {
  const options = {
    chart: {
      type: "line",
      height: 350,
      zoom: { enabled: false },
      toolbar: {
        show: true,
        tools: { download: true },
        export: {
          csv: {
            filename: title.replace(/\s+/g, "_"), // ใช้ชื่อกราฟเป็นชื่อไฟล์
            headerCategory: categoryLabel, // ✅ ใช้ชื่อ category ที่ส่งมา
            headerValue: "Value", // สามารถแก้เป็น “Requests”, “Amount” ฯลฯ ก็ได้
          },
          svg: {
            filename: title.replace(/\s+/g, "_"),
          },
          png: {
            filename: title.replace(/\s+/g, "_"),
          },
        },
      },
    },
    dataLabels: { enabled: false },
    stroke: { curve: "smooth" },
    grid: {
      row: { colors: ["#f3f3f3", "transparent"], opacity: 0.5 },
    },
    xaxis: {
      categories: categories,
      labels: { rotate: -45 },
    },
    yaxis: {
      labels: {
        // ✅ แสดงเฉพาะจำนวนเต็ม
        formatter: (value) => (Number.isInteger(value) ? value : ""),
      },
      tickAmount: 5,
      min: 0,
      forceNiceScale: true,
    },
    legend: { position: "top" },
  };

  return (
    <div className="apex-line-chart w-100">
      <ReactApexChart
        options={options}
        series={series}
        type="line"
        height={350}
      />
    </div>
  );
};

export default LineChart;
