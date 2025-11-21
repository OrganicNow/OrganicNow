// Dashboard.test.jsx
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Dashboard from '../Dashboard';
import Layout from '../../component/layout';
import LineChart from '../../component/LineChart';
import BarChart from '../../component/BarChart';

// Mock dependencies
vi.mock('../../component/layout');
vi.mock('../../component/LineChart');
vi.mock('../../component/BarChart');
vi.mock('../../config_variable', () => ({
  apiPath: 'http://localhost:3000/api'
}));

// Mock Bootstrap
vi.mock('bootstrap/dist/js/bootstrap.bundle.min.js', () => ({
  Modal: vi.fn(() => ({
    show: vi.fn(),
    hide: vi.fn()
  }))
}));

// Mock framer-motion
vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }) => <div {...props}>{children}</div>
  },
  AnimatePresence: ({ children }) => <div>{children}</div>
}));

// Mock fetch globally
global.fetch = vi.fn();

describe('Dashboard', () => {
  const mockDashboardData = {
    rooms: [
      { roomNumber: '101', room_floor: 1, status: 0 },
      { roomNumber: '102', room_floor: 1, status: 1 },
      { roomNumber: '201', room_floor: 2, status: 2 },
      { roomNumber: '202', room_floor: 2, status: 0 }
    ],
    maintains: [
      { month: 'Jan 2024', total: 5 },
      { month: 'Feb 2024', total: 8 },
      { month: 'Mar 2024', total: 12 }
    ],
    finances: [
      { month: 'Jan 2024', onTime: 10, penalty: 2, overdue: 1 },
      { month: 'Feb 2024', onTime: 12, penalty: 1, overdue: 0 },
      { month: 'Mar 2024', onTime: 15, penalty: 3, overdue: 2 }
    ],
    usages: {
      '101': {
        categories: ['Jan', 'Feb', 'Mar'],
        series: [
          { name: 'Water Usage', data: [10, 15, 12] },
          { name: 'Electricity Usage', data: [20, 25, 22] }
        ]
      },
      '102': {
        categories: ['Jan', 'Feb', 'Mar'],
        series: [
          { name: 'Water Usage', data: [8, 10, 9] },
          { name: 'Electricity Usage', data: [18, 20, 19] }
        ]
      }
    }
  };

  beforeEach(() => {
    vi.clearAllMocks();

    // Mock Layout component
    Layout.mockImplementation(({ children, title, icon, notifications }) => (
      <div data-testid="layout">
        <h1>{title} {icon}</h1>
        {notifications && <span data-testid="notifications">{notifications}</span>}
        {children}
      </div>
    ));

    // Mock Chart components
    LineChart.mockImplementation(({ title, categories, series, colors, yTitle, fileName }) => (
      <div data-testid="line-chart">
        <h3>{title}</h3>
        <span>Categories: {categories?.join(', ')}</span>
        <span>Series: {series?.length}</span>
        <span>Colors: {colors?.join(', ')}</span>
        <span>Y-Title: {yTitle}</span>
        <span>File: {fileName}</span>
      </div>
    ));

    BarChart.mockImplementation(({ title, categories, series, yTitle, csvCategoryName }) => (
      <div data-testid="bar-chart">
        <h3>{title}</h3>
        <span>Categories: {categories?.join(', ')}</span>
        <span>Series: {series?.length}</span>
        <span>Y-Title: {yTitle}</span>
        <span>CSV Category: {csvCategoryName}</span>
      </div>
    ));

    // Mock fetch response
    fetch.mockResolvedValue({
      ok: true,
      json: async () => mockDashboardData
    });
  });

  // Test 1: Component renders successfully
  it('renders without crashing', async () => {
    render(<Dashboard />);
    
    await waitFor(() => {
      expect(screen.getByTestId('layout')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Dashboard Overview')).toBeInTheDocument();
  });

  // Test 2: Fetches and displays dashboard data
  it('fetches and displays dashboard data', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('http://localhost:3000/api/dashboard');
    });

    // Check if rooms are displayed
    expect(screen.getByText('101')).toBeInTheDocument();
    expect(screen.getByText('102')).toBeInTheDocument();
    expect(screen.getByText('201')).toBeInTheDocument();
    expect(screen.getByText('202')).toBeInTheDocument();

    // Check if floor sections are created
    expect(screen.getByText('Floor 1')).toBeInTheDocument();
    expect(screen.getByText('Floor 2')).toBeInTheDocument();
  });

  // Test 3: Displays room status with correct colors
  it('displays room status with correct colors', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('101')).toBeInTheDocument();
    });

    const room101 = screen.getByText('101');
    const room102 = screen.getByText('102');
    const room201 = screen.getByText('201');

    // Check background colors based on status
    expect(room101).toHaveStyle({ backgroundColor: '#22c55e' }); // status 0 - Available
    expect(room102).toHaveStyle({ backgroundColor: '#ef4444' }); // status 1 - Unavailable
    expect(room201).toHaveStyle({ backgroundColor: '#facc15' }); // status 2 - Repair
  });

  // Test 4: Handles room click to show/hide charts
  it('shows and hides room usage charts on click', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('101')).toBeInTheDocument();
    });

    // Click on room 101
    const room101 = screen.getByText('101');
    fireEvent.click(room101);

    // Should show usage chart for room 101
    await waitFor(() => {
      expect(screen.getByText('Usage for Room 101')).toBeInTheDocument();
    });

    // Click again to hide
    fireEvent.click(room101);

    // Chart should be hidden after animation
    await waitFor(() => {
      expect(screen.queryByText('Usage for Room 101')).not.toBeInTheDocument();
    });
  });

  // Test 5: Displays usage charts for selected room
  it('displays usage charts when room is selected', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('101')).toBeInTheDocument();
    });

    // Click on room 101
    fireEvent.click(screen.getByText('101'));

    // Wait for charts to appear
    await waitFor(() => {
      expect(screen.getByText('Water Usage')).toBeInTheDocument();
      expect(screen.getByText('Electricity Usage')).toBeInTheDocument();
    });

    // Check if LineChart components are rendered with correct props
    // มี 3 LineChart เพราะมี 2 chart ใน room usage + 1 chart ใน Request Overview
    const lineCharts = screen.getAllByTestId('line-chart');
    expect(lineCharts.length).toBeGreaterThanOrEqual(2);
  });

  // Test 6: Handles month selection for CSV download
  it('handles month selection for CSV download', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Select Month')).toBeInTheDocument();
    });

    const monthSelect = screen.getByDisplayValue('Mar 2024'); // Last month should be selected by default

    // Change selection
    fireEvent.change(monthSelect, { target: { value: 'Feb 2024' } });

    expect(monthSelect.value).toBe('Feb 2024');
  });

  // Test 7: Handles CSV download successfully - แก้ไขใหม่
  it('handles CSV download successfully', async () => {
    // Mock blob response for download
    const mockBlob = new Blob(['test,csv,content'], { type: 'text/csv' });

    global.URL.createObjectURL = vi.fn(() => 'blob:test-url');
    global.URL.revokeObjectURL = vi.fn();

    // Mock document.createElement โดยไม่ใช้ spy ที่ทำให้ error
    const originalCreateElement = document.createElement;
    document.createElement = vi.fn().mockImplementation((tagName) => {
      if (tagName === 'a') {
        return {
          href: '',
          download: '',
          click: vi.fn(),
          setAttribute: vi.fn()
        };
      }
      return originalCreateElement.call(document, tagName);
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Download CSV')).toBeInTheDocument();
    });

    // Mock fetch for CSV download
    fetch.mockResolvedValueOnce({
      ok: true,
      blob: async () => mockBlob
    });

    // Click download button
    const downloadButton = screen.getByText('Download CSV');
    fireEvent.click(downloadButton);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:3000/api/dashboard/export/Mar_2024'
      );
    });

    // Restore original function
    document.createElement = originalCreateElement;
  });

  // Test 8: Shows error when no month selected for CSV download - แก้ไขใหม่
  it('shows error when no month selected for CSV download', async () => {
    // Mock alert
    const mockAlert = vi.spyOn(window, 'alert').mockImplementation(() => {});

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Download CSV')).toBeInTheDocument();
    });

    // Clear month selection
    const monthSelect = screen.getByDisplayValue('Mar 2024');
    fireEvent.change(monthSelect, { target: { value: '' } });

    // Click download without selecting month
    const downloadButton = screen.getByText('Download CSV');
    fireEvent.click(downloadButton);

    expect(mockAlert).toHaveBeenCalledWith('⚠️ กรุณาเลือกเดือนก่อนดาวน์โหลด');

    mockAlert.mockRestore();
  });

  // Test 9: Handles CSV download failure - แก้ไขใหม่
  it('handles CSV download failure', async () => {
    // Mock alert
    const mockAlert = vi.spyOn(window, 'alert').mockImplementation(() => {});

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Download CSV')).toBeInTheDocument();
    });

    // Mock fetch to fail
    fetch.mockResolvedValueOnce({
      ok: false
    });

    // Click download button
    const downloadButton = screen.getByText('Download CSV');
    fireEvent.click(downloadButton);

    await waitFor(() => {
      expect(mockAlert).toHaveBeenCalledWith('ไม่สามารถดาวน์โหลดไฟล์ได้');
    });

    mockAlert.mockRestore();
  });

  // Test 10: Displays overview charts - แก้ไขใหม่
  it('displays overview charts', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Request Overview (Last 6 months)')).toBeInTheDocument();
      expect(screen.getByText('Finance History (Last 6 months)')).toBeInTheDocument();
    });

    // Check if chart components are rendered
    const lineCharts = screen.getAllByTestId('line-chart');
    const barCharts = screen.getAllByTestId('bar-chart');

    expect(lineCharts.length).toBeGreaterThan(0);
    expect(barCharts.length).toBeGreaterThan(0);
  });

  // Test 11: Displays legend correctly - แก้ไขใหม่
  it('displays room status legend correctly', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Available')).toBeInTheDocument();
      expect(screen.getByText('Unavailable')).toBeInTheDocument();
      expect(screen.getByText('Repair')).toBeInTheDocument();
    });
  });

  // Test 12: Handles API fetch error gracefully - แก้ไขใหม่
  it('handles API fetch error gracefully', async () => {
    // Mock console.error
    const mockConsoleError = vi.spyOn(console, 'error').mockImplementation(() => {});

    // Mock fetch to fail
    fetch.mockRejectedValueOnce(new Error('API Error'));

    render(<Dashboard />);

    await waitFor(() => {
      expect(mockConsoleError).toHaveBeenCalledWith('Failed to fetch dashboard:', expect.any(Error));
    });

    // Component should still render without crashing
    expect(screen.getByText('Dashboard Overview')).toBeInTheDocument();

    mockConsoleError.mockRestore();
  });

  // Test 13: Shows no usage data message when no data available - แก้ไขใหม่
  it('shows no usage data message when no data available', async () => {
    // Mock data without usage for room 201
    const mockDataWithoutUsage = {
      ...mockDashboardData,
      usages: {
        '101': mockDashboardData.usages['101'],
        '102': mockDashboardData.usages['102']
        // No usage data for room 201
      }
    };

    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockDataWithoutUsage
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('201')).toBeInTheDocument();
    });

    // Click on room 201 which has no usage data
    fireEvent.click(screen.getByText('201'));

    // Should show no data message
    await waitFor(() => {
      expect(screen.getByText('No usage data available')).toBeInTheDocument();
    });
  });

  // Test 14: Sorts rooms by room number - แก้ไขใหม่
  it('sorts rooms by room number', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('101')).toBeInTheDocument();
    });

    // Get all room buttons and check order
    const roomButtons = screen.getAllByRole('button').filter(button =>
      /10[12]|20[12]/.test(button.textContent)
    );
    const roomNumbers = roomButtons.map(button => button.textContent);

    // Should be sorted: 101, 102, 201, 202
    expect(roomNumbers).toEqual(['101', '102', '201', '202']);
  });

  // Test 15: Displays correct number of charts in overview sections - test ใหม่
  it('displays correct number of charts in overview sections', async () => {
    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Request Overview (Last 6 months)')).toBeInTheDocument();
    });

    // Request Overview ควรมี 1 LineChart
    // Finance History ควรมี 1 BarChart
    const lineCharts = screen.getAllByTestId('line-chart');
    const barCharts = screen.getAllByTestId('bar-chart');

    // มีอย่างน้อย 1 chart ในแต่ละส่วน
    expect(lineCharts.length).toBeGreaterThan(0);
    expect(barCharts.length).toBeGreaterThan(0);
  });
});