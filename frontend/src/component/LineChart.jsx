// frontend/src/components/LineChart.jsx
import React from "react";
import ReactApexChart from "react-apexcharts";

const LineChart = ({
  title = "Line Chart",
  categories = [],
  series = [],
  colors = [], // ✅ เพิ่ม prop สำหรับรับสีเส้น
  categoryLabel = "Month",
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
            filename: title.replace(/\s+/g, "_"),
            headerCategory: categoryLabel,
            headerValue: "Value",
          },
          svg: { filename: title.replace(/\s+/g, "_") },
          png: { filename: title.replace(/\s+/g, "_") },
        },
      },
    },
    colors: colors.length > 0 ? colors : undefined, // ✅ ใช้สีที่ส่งมา หรือ default ถ้าไม่มี
    dataLabels: { enabled: false },
    stroke: { curve: "smooth", width: 3 },
    grid: {
      row: { colors: ["#f3f3f3", "transparent"], opacity: 0.5 },
    },
    xaxis: {
      categories: categories,
      labels: { rotate: -45 },
    },
    yaxis: {
      labels: {
        formatter: (value) => (Number.isInteger(value) ? value : ""), // ✅ แสดงเฉพาะจำนวนเต็ม
      },
      tickAmount: 5,
      min: 0,
      forceNiceScale: true,
    },
    legend: { position: "top" },
    markers: {
      size: 4,
      hover: { sizeOffset: 3 },
    },
  };

  return (
    <div className="apex-line-chart w-100">
      <ReactApexChart options={options} series={series} type="line" height={350} />
    </div>
  );
};

export default LineChart;
