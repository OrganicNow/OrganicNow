// frontend/src/components/LineChart.jsx
import React from "react";
import ReactApexChart from "react-apexcharts";

const LineChart = ({
  title = "Line Chart",
  categories = [],
  series = [],
  colors = [],
  categoryLabel = "Month",
  fileName = "Chart",
}) => {
  const safeFileName = fileName.replace(/\s+/g, "_");

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
            filename: safeFileName,
            headerCategory: categoryLabel,
            headerValue: "Value",
          },
          svg: { filename: safeFileName },
          png: { filename: safeFileName },
        },
      },
    },
    colors: colors.length > 0 ? colors : undefined,
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
        formatter: (value) => (Number.isInteger(value) ? value : ""),
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
