// frontend/src/components/__tests__/ApexBarChart.test.jsx
import React from "react";
import { render } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import BarChart from "../BarChart";

// 🧪 Mock react-apexcharts เพื่อจับ props ที่ส่งเข้าไป
let lastProps = null;

vi.mock("react-apexcharts", () => {
  const MockChart = (props) => {
    lastProps = props;
    // ไม่ต้อง render chart จริง แค่ return null ก็พอ
    return null;
  };

  return {
    __esModule: true,
    default: MockChart,
  };
});

const getLastProps = () => lastProps;

describe("BarChart (ApexBarChart.jsx)", () => {
  it("ควรส่ง default options และ default series ให้ ReactApexChart เมื่อไม่ส่ง props อะไรเลย", () => {
    render(<BarChart />);

    const { options, series } = getLastProps();

    // ✅ ตรวจ chart basic config
    expect(options.chart.type).toBe("bar");
    expect(options.chart.toolbar.export.csv.filename).toBe("Revenue_Overview");
    expect(options.chart.toolbar.export.csv.headerCategory).toBe("Month");

    // ✅ ตรวจ default x-axis categories
    expect(options.xaxis.categories).toEqual([
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
    ]);

    // ✅ ตรวจ default y-axis title
    expect(options.yaxis.title.text).toBe("$ (thousands)");

    // ✅ ตรวจ default series ทั้ง 3 ชุด
    expect(series).toHaveLength(3);
    expect(series[0]).toEqual({
      name: "Net Profit",
      data: [44, 55, 57, 56, 61, 58, 63, 60, 66],
    });
    expect(series[1]).toEqual({
      name: "Revenue",
      data: [76, 85, 101, 98, 87, 105, 91, 114, 94],
    });
    expect(series[2]).toEqual({
      name: "Free Cash Flow",
      data: [35, 41, 36, 26, 45, 48, 52, 53, 41],
    });
  });

  it("ควรใช้ categories ที่ส่งเข้ามา ไม่ใช้ default", () => {
    const categories = ["Jan", "Feb", "Mar"];

    render(<BarChart categories={categories} />);

    const { options } = getLastProps();

    expect(options.xaxis.categories).toEqual(categories);
  });

  it("ควรใช้ series ที่ส่งเข้ามา ไม่ใช้ default series", () => {
    const customSeries = [{ name: "Custom", data: [1, 2, 3] }];

    render(<BarChart series={customSeries} />);

    const { series } = getLastProps();

    // ใช้ reference เดิมเลย
    expect(series).toBe(customSeries);
    expect(series).toEqual(customSeries);
  });

  it("ควรตั้งค่า filename และ headerCategory ใน export.csv ตาม title และ csvCategoryName ที่ส่งเข้าไป", () => {
    render(
      <BarChart
        title="Monthly Revenue 2024"
        csvCategoryName="Period"
      />
    );

    const { options } = getLastProps();

    expect(options.chart.toolbar.export.csv.filename).toBe(
      "Monthly_Revenue_2024"
    );
    expect(options.chart.toolbar.export.csv.headerCategory).toBe("Period");
  });

  it("ควรตั้งค่า y-axis title ตาม yTitle prop", () => {
    render(<BarChart yTitle="Units Sold" />);

    const { options } = getLastProps();

    expect(options.yaxis.title.text).toBe("Units Sold");
  });

  it("formatter ของ y-axis ควรแปลงค่าเป็นจำนวนเต็ม", () => {
    render(<BarChart />);

    const { options } = getLastProps();
    const formatter = options.yaxis.labels.formatter;

    // กรณีเป็นจำนวนเต็ม -> คืนค่า number เดิม
    expect(formatter(10)).toBe(10);

    // กรณีเป็นทศนิยม -> toFixed(0) -> string
    expect(formatter(10.2)).toBe("10");
    expect(formatter(10.8)).toBe("11");
  });

  it("formatter ของ tooltip ควรคืนค่าเป็น string", () => {
    render(<BarChart />);

    const { options } = getLastProps();
    const tooltipFormatter = options.tooltip.y.formatter;

    expect(tooltipFormatter(123)).toBe("123");
    expect(tooltipFormatter(45.6)).toBe("45.6");
  });
});
