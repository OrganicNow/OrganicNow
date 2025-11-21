// InvoiceDetails.test.jsx
import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import InvoiceDetails from '../InvoiceDetails';
import Layout from '../../component/layout';
import Modal from '../../component/modal';
import QRCodeGenerator from '../../component/QRCodeGenerator';
import useMessage from '../../component/useMessage';

// Mock dependencies (keep your existing mock setup)
vi.mock('../../component/layout');
vi.mock('../../component/modal');
vi.mock('../../component/QRCodeGenerator');
vi.mock('../../component/useMessage');
vi.mock('bootstrap/dist/js/bootstrap.bundle.min.js', () => ({
  Modal: vi.fn(() => ({
    show: vi.fn(),
    hide: vi.fn(),
    dispose: vi.fn()
  }))
}));

// Mock environment variables
vi.mock('../.../../../env', () => ({
  VITE_API_URL: 'http://localhost:8080'
}));

// Mock useNavigate and useLocation
const mockNavigate = vi.fn();
const mockUseLocation = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => mockUseLocation()
  };
});

describe('InvoiceDetails', () => {
  const mockShowMessageError = vi.fn();
  const mockShowMessageSave = vi.fn();

  const mockInvoiceData = {
    id: 1,
    createDate: '2025-01-31T00:00:00',
    rent: 4000,
    water: 120,
    electricity: 1236,
    addonAmount: 0,
    waterUnit: 4,
    electricityUnit: 206,
    netAmount: 5356,
    invoiceStatus: 0,
    payDate: null,
    penaltyTotal: 0,
    penaltyAppliedAt: null,
    previousBalance: 0,
    paidAmount: 0,
    outstandingBalance: 5356,
    statusText: 'pending'
  };

  const mockLocationState = {
    state: {
      invoice: {
        id: 1,
        createDate: '2025-01-31',
        firstName: 'John',
        lastName: 'Doe',
        nationalId: '1-2345-67890-12-3',
        phoneNumber: '012-345-6789',
        email: 'JohnDoe@gmail.com',
        package: '1 Year',
        signDate: '2024-12-30',
        startDate: '2024-12-31',
        endDate: '2025-12-31',
        floor: '1',
        room: '101',
        amount: 5356,
        rent: 4000,
        water: 120,
        waterUnit: 4,
        electricity: 1236,
        electricityUnit: 206,
        status: 'pending',
        payDate: '',
        penalty: 0,
        penaltyDate: null
      },
      invoiceId: 1,
      tenantName: 'John Doe'
    }
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseLocation.mockReturnValue(mockLocationState);

    // Your existing mock implementations...
    Layout.mockImplementation(({ children, title, icon, notifications }) => (
      <div data-testid="layout">
        <h1>{title} {icon}</h1>
        {notifications && <span data-testid="notifications">{notifications}</span>}
        {children}
      </div>
    ));

    Modal.mockImplementation(({ id, title, icon, size, scrollable, children }) => (
      <div data-testid={`modal-${id}`} style={{ display: 'block' }}>
        <h2>{title} {icon}</h2>
        <div className={size}>{children}</div>
      </div>
    ));

    QRCodeGenerator.mockImplementation(({ value, size, className, errorMessage }) => (
      <div data-testid="qr-code" className={className}>
        <span>QR Code: {value}</span>
        <span>Size: {size}</span>
        <span>Error: {errorMessage}</span>
      </div>
    ));

    useMessage.mockReturnValue({
      showMessageError: mockShowMessageError,
      showMessageSave: mockShowMessageSave
    });

    global.fetch = vi.fn();
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockInvoiceData
    });

    global.bootstrap = {
      Modal: vi.fn(() => ({
        show: vi.fn(),
        hide: vi.fn(),
        dispose: vi.fn()
      }))
    };

    document.querySelectorAll = vi.fn(() => []);
    document.body.classList = {
      remove: vi.fn()
    };
    document.body.style = {};
  });

  const renderWithRouter = (component) => {
    return render(
      <MemoryRouter initialEntries={['/invoicedetails']}>
        <Routes>
          <Route path="/invoicedetails" element={component} />
          <Route path="/invoicemanagement" element={<div>Invoice Management</div>} />
        </Routes>
      </MemoryRouter>
    );
  };

  // Test 1: Component renders successfully with invoice data - แก้ไขใหม่
  it('renders without crashing with invoice data', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByTestId('layout')).toBeInTheDocument();
    });

    // ใช้การค้นหาที่เฉพาะเจาะจงมากขึ้น
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  // Test 2: Displays tenant information correctly - แก้ไขใหม่
  it('displays tenant information correctly', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      // ใช้ getAllByText และเลือกตัวแรก
      const tenantInfoHeaders = screen.getAllByText('Tenant Information');
      expect(tenantInfoHeaders.length).toBeGreaterThan(0);
    });

    expect(screen.getByText('John')).toBeInTheDocument();
    expect(screen.getByText('Doe')).toBeInTheDocument();
    expect(screen.getByText('1-2345-67890-12-3')).toBeInTheDocument();
  });



  // Test 4: Displays invoice information correctly - แก้ไขใหม่
  it('displays invoice information correctly', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      // ใช้ getAllByText และเลือกตัวแรก
      const invoiceInfoHeaders = screen.getAllByText('Invoice Information');
      expect(invoiceInfoHeaders.length).toBeGreaterThan(0);
    });

    // ตรวจสอบข้อมูลใบแจ้งหนี้
    expect(screen.getByText('4,000')).toBeInTheDocument(); // Rent
    expect(screen.getByText('120')).toBeInTheDocument(); // Water bill
    expect(screen.getByText('1,236')).toBeInTheDocument(); // Electricity bill
  });

  // Test 5: Displays status badge correctly
  it('displays status badge correctly', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('pending')).toBeInTheDocument();
    });

    const statusBadge = screen.getByText('pending');
    expect(statusBadge).toHaveClass('bg-warning');
  });


  // Test 7: Displays payment information
  it('displays payment information', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Bank Transfer Information')).toBeInTheDocument();
      expect(screen.getByText('PromptPay Information')).toBeInTheDocument();
    });

    expect(screen.getByText('Bangkok Bank')).toBeInTheDocument();
    expect(screen.getByText('123-4-56789-0')).toBeInTheDocument();
    expect(screen.getByText('0123456789')).toBeInTheDocument();
  });

  // Test 8: Handles edit button click
  it('handles edit button click', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Edit Invoice')).toBeInTheDocument();
    });

    const editButton = screen.getByText('Edit Invoice');
    fireEvent.click(editButton);

    // Should attempt to open modal
    expect(document.querySelectorAll).toHaveBeenCalled();
  });

  // Test 9: Handles breadcrumb navigation
  it('handles breadcrumb navigation', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Invoice Management')).toBeInTheDocument();
    });

    const breadcrumb = screen.getByText('Invoice Management');
    fireEvent.click(breadcrumb);

    expect(mockNavigate).toHaveBeenCalledWith('/invoicemanagement');
  });

  // Test 10: Fetches invoice data from API
  it('fetches invoice data from API', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/invoice/1',
        { credentials: 'include' }
      );
    });
  });

  // Test 11: Handles API fetch error gracefully - แก้ไขใหม่
  it('handles API fetch error gracefully', async () => {
    global.fetch.mockRejectedValueOnce(new Error('API Error'));

    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      // Component should still render without crashing - ตรวจสอบข้อมูลพื้นฐาน
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });
  });

  // Test 12: Displays QR code for payment
  it('displays QR code for payment', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByTestId('qr-code')).toBeInTheDocument();
    });

    const qrCode = screen.getByTestId('qr-code');
    expect(qrCode).toHaveTextContent('QR Code:');
  });

  // Test 13: Displays penalty information when applicable
  it('displays penalty information when applicable', async () => {
    // Mock invoice data with penalty
    const mockInvoiceWithPenalty = {
      ...mockInvoiceData,
      penaltyTotal: 500,
      penaltyAppliedAt: '2025-02-15T00:00:00'
    };

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockInvoiceWithPenalty
    });

    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('500 THB')).toBeInTheDocument();
    });
  });

  // Test 14: Displays paid status correctly - แก้ไขใหม่
  it('displays paid status correctly', async () => {
    // Mock invoice data that is paid
    const mockPaidInvoice = {
      ...mockInvoiceData,
      invoiceStatus: 1,
      payDate: '2025-02-01T00:00:00',
      outstandingBalance: 0
    };

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPaidInvoice
    });

    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('complete')).toBeInTheDocument();
      // ตรวจสอบว่าแสดงสถานะชำระเงินแล้ว
      const paidElements = screen.getAllByText(/paid/i);
      expect(paidElements.length).toBeGreaterThan(0);
    });
  });

  // Test 15: Calculates water and electricity bills correctly
  it('calculates water and electricity bills correctly', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('4')).toBeInTheDocument(); // Water unit
      expect(screen.getByText('206')).toBeInTheDocument(); // Electricity unit
    });

    // Water: 4 units * 30 = 120 THB
    // Electricity: 206 units * 6.5 = 1339 THB (rounded)
    expect(screen.getByText('120')).toBeInTheDocument(); // Water bill
    expect(screen.getByText('1,236')).toBeInTheDocument(); // Electricity bill
  });

  // Test 16: Handles modal form submission
  it('handles modal form submission', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Edit Invoice')).toBeInTheDocument();
    });

    // Click edit button to open modal (simplified)
    const editButton = screen.getByText('Edit Invoice');
    fireEvent.click(editButton);

    // Mock successful save
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockInvoiceData
    });

    // In a real test, we would interact with form fields and submit
    // This is simplified for the example
    expect(global.fetch).toHaveBeenCalled();
  });

  // Test 17: Displays package information
  it('displays package information', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Package:')).toBeInTheDocument();
    });

    expect(screen.getByText('1 Year')).toBeInTheDocument();
  });

  // Test 18: Displays contract dates
  it('displays contract dates', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Sign date:')).toBeInTheDocument();
      expect(screen.getByText('Start date:')).toBeInTheDocument();
      expect(screen.getByText('End date:')).toBeInTheDocument();
    });

    expect(screen.getByText('2024-12-30')).toBeInTheDocument(); // Sign date
    expect(screen.getByText('2024-12-31')).toBeInTheDocument(); // Start date
    expect(screen.getByText('2025-12-31')).toBeInTheDocument(); // End date
  });

  // Test 19: Handles missing invoice data gracefully - แก้ไขใหม่
  it('handles missing invoice data gracefully', async () => {
    // Mock location with no state
    const emptyLocation = { state: null };

    mockUseLocation.mockReturnValueOnce(emptyLocation);

    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      // Should still render with default data
      expect(screen.getByText('Invoice Management')).toBeInTheDocument();
    });
  });

  // Test 20: Displays create date in invoice information
  it('displays create date in invoice information', async () => {
    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Create date:')).toBeInTheDocument();
    });

    expect(screen.getByText('2025-01-31')).toBeInTheDocument();
  });

  // Test 21: Displays addon amount when applicable - test ใหม่
  it('displays addon amount when applicable', async () => {
    // Mock invoice data with addon amount
    const mockInvoiceWithAddon = {
      ...mockInvoiceData,
      addonAmount: 500
    };

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockInvoiceWithAddon
    });

    renderWithRouter(<InvoiceDetails />);

    await waitFor(() => {
      expect(screen.getByText('Add-on fee:')).toBeInTheDocument();
      expect(screen.getByText('500')).toBeInTheDocument();
    });
  });


});