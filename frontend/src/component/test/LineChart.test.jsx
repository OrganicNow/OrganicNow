// frontend/src/components/__tests__/LineChart.test.jsx
import React from "react";
import { render } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import LineChart from "../LineChart";

// เก็บ props ล่าสุดที่ส่งเข้า ReactApexChart
let lastChartProps = null;

// 🧪 mock react-apexcharts
vi.mock("react-apexcharts", () => {
  const MockChart = (props) => {
    lastChartProps = props;
    return <div data-testid="apex-line-chart-mock" />;
  };

  return {
    __esModule: true,
    default: MockChart,
  };
});

const getLastChartProps = () => lastChartProps;

describe("LineChart component", () => {
  beforeEach(() => {
    lastChartProps = null;
  });

  it("ควรส่ง options และ series เข้า ReactApexChart ตาม props ที่ให้มา", () => {
    const categories = ["Jan", "Feb", "Mar"];
    const series = [{ name: "Sales", data: [10, 20, 30] }];
    const colors = ["#111111", "#222222"];
    const fileName = "Revenue 2024";
    const categoryLabel = "Period";

    render(
      <LineChart
        title="Revenue Line"
        categories={categories}
        series={series}
        colors={colors}
        categoryLabel={categoryLabel}
        fileName={fileName}
      />
    );

    const { options, series: passedSeries, type, height } = getLastChartProps();

    // ✅ ReactApexChart ถูกเรียกด้วย type และ height ที่ถูกต้อง
    expect(type).toBe("line");
    expect(height).toBe(350);

    // ✅ series ที่ส่งเข้า chart ตรงกับที่ส่งเข้า prop
    expect(passedSeries).toBe(series);
    expect(passedSeries).toEqual(series);

    // ✅ categories ถูกส่งเข้า xaxis ถูกต้อง
    expect(options.xaxis.categories).toEqual(categories);

    // ✅ colors ใช้ค่าที่ส่งมา ไม่ใช่ undefined
    expect(options.colors).toEqual(colors);

    // ✅ ตรวจ export filenames และ category label
    const safeFileName = "Revenue_2024";
    expect(options.chart.toolbar.export.csv.filename).toBe(safeFileName);
    expect(options.chart.toolbar.export.csv.headerCategory).toBe(categoryLabel);
    expect(options.chart.toolbar.export.csv.headerValue).toBe("Value");
    expect(options.chart.toolbar.export.svg.filename).toBe(safeFileName);
    expect(options.chart.toolbar.export.png.filename).toBe(safeFileName);

    // ✅ legend อยู่ด้านบน
    expect(options.legend.position).toBe("top");
  });

  it("ควรตั้ง options.colors เป็น undefined ถ้าไม่ส่ง colors เข้ามา", () => {
    render(
      <LineChart
        categories={["Jan"]}
        series={[{ name: "Test", data: [1] }]}
      />
    );

    const { options } = getLastChartProps();

    // ถ้า colors ไม่ได้ส่งมา options.colors ควรจะ undefined
    expect(options.colors).toBeUndefined();
  });

  it("ควรใช้ categoryLabel กับ fileName ตามค่า default ถ้าไม่ส่ง props เข้ามา", () => {
    render(
      <LineChart
        categories={["Jan", "Feb"]}
        series={[{ name: "Test", data: [5, 10] }]}
      />
    );

    const { options } = getLastChartProps();

    // default fileName = "Chart" → safeFileName = "Chart"
    expect(options.chart.toolbar.export.csv.filename).toBe("Chart");
    expect(options.chart.toolbar.export.csv.headerCategory).toBe("Month");
    expect(options.chart.toolbar.export.csv.headerValue).toBe("Value");
  });

  it("formatter ของ y-axis ควรแสดงเฉพาะจำนวนเต็ม และคืน string ว่างถ้าไม่ใช่จำนวนเต็ม", () => {
    render(
      <LineChart
        categories={["Jan", "Feb"]}
        series={[{ name: "Test", data: [1, 2] }]}
      />
    );

    const { options } = getLastChartProps();
    const formatter = options.yaxis.labels.formatter;

    // จำนวนเต็ม → คืนค่าเดิม (number)
    expect(formatter(10)).toBe(10);
    expect(formatter(0)).toBe(0);

    // ทศนิยม → คืน string ว่าง
    expect(formatter(10.5)).toBe("");
    expect(formatter(3.14)).toBe("");
  });
});
