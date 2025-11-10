// frontend/src/components/ApexBarChart.jsx
import React from "react";
import ReactApexChart from "react-apexcharts";

const BarChart = ({
  title = "Revenue Overview",
  categories = [],
  series = [],
  yTitle = "$ (thousands)",
  csvCategoryName = "Month", // ✅ เพิ่ม prop สำหรับชื่อ category column เวลา export CSV
}) => {
  const options = {
    chart: {
      type: "bar",
      height: 350,
      toolbar: {
        show: true,
        tools: { download: true },
        export: {
          csv: {
            filename: title.replace(/\s+/g, "_"), // ✅ ใช้ชื่อไฟล์ตาม title
            headerCategory: csvCategoryName, // ✅ ใช้ชื่อ category column ที่ส่งเข้ามา
          },
        },
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "55%",
        borderRadius: 5,
        borderRadiusApplication: "end",
      },
    },
    dataLabels: { enabled: false },
    stroke: {
      show: true,
      width: 2,
      colors: ["transparent"],
    },
    xaxis: {
      categories:
        categories.length > 0
          ? categories
          : ["Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct"],
    },
    yaxis: {
      title: { text: yTitle },
      labels: {
        formatter: (val) =>
          Number.isInteger(val) ? val : val.toFixed(0), // ✅ ให้แกน Y เป็นจำนวนเต็ม
      },
    },
    fill: { opacity: 1 },
    tooltip: {
      y: {
        formatter: (val) => `${val}`,
      },
    },
  };

  const chartSeries =
    series.length > 0
      ? series
      : [
          { name: "Net Profit", data: [44, 55, 57, 56, 61, 58, 63, 60, 66] },
          { name: "Revenue", data: [76, 85, 101, 98, 87, 105, 91, 114, 94] },
          { name: "Free Cash Flow", data: [35, 41, 36, 26, 45, 48, 52, 53, 41] },
        ];

  return (
    <div className="apex-bar-chart">
      <ReactApexChart options={options} series={chartSeries} type="bar" height={350} />
    </div>
  );
};

export default BarChart;
