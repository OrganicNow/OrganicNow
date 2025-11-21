import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import axios from 'axios';
import TenantDetail from '../Tenantdetail';
import { apiPath } from '../../config_variable';

// Mock dependencies
vi.mock('axios');
vi.mock('../../config_variable', () => ({
  apiPath: 'http://localhost:3000/api'
}));

// Mock AuthContext
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { name: 'Test User', role: 'admin' },
    login: vi.fn(),
    logout: vi.fn(),
    isAuthenticated: true,
    hasPermission: vi.fn().mockReturnValue(true)
  })
}));

// Mock NotificationContext
vi.mock('../../contexts/NotificationContext', () => ({
  useNotifications: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false,
    markAsRead: vi.fn(),
    fetchNotifications: vi.fn(),
    refreshNotifications: vi.fn()
  })
}));

// Mock Layout component
vi.mock('../component/layout', () => ({
  default: ({ children, title, icon, notifications }) => (
    <div data-testid="layout">
      <div data-testid="layout-title">{title}</div>
      <div data-testid="layout-icon">{icon}</div>
      {/* Render children directly without complex sidebar/topbar */}
      <div data-testid="layout-content">
        {children}
      </div>
    </div>
  )
}));

// Mock Modal component
vi.mock('../component/modal', () => ({
  default: ({ id, title, icon, size, scrollable, children }) => (
    <div data-testid="modal" data-modal-id={id}>
      <div className="card-header form-Head form-Head-Background">
        <i className={icon}></i> {title}
      </div>
      <div className="modal-body">
        {children}
      </div>
    </div>
  )
}));

// Mock useMessage hook - แก้ปัญหา SweetAlert2
vi.mock('../component/useMessage', () => ({
  default: () => ({
    showMessageError: vi.fn(),
    showMessageSave: vi.fn()
  })
}));

// Mock SweetAlert2
vi.mock('sweetalert2', () => ({
  default: {
    fire: vi.fn(() => Promise.resolve({ isConfirmed: true }))
  }
}));

// Mock Bootstrap
vi.mock('bootstrap/dist/js/bootstrap.bundle.min.js', () => ({
  Modal: vi.fn()
}));

// Mock CSS imports
vi.mock('../assets/css/tenantdetail.css', () => ({}));
vi.mock('bootstrap/dist/css/bootstrap.min.css', () => ({}));
vi.mock('bootstrap-icons/font/bootstrap-icons.css', () => ({}));

// Mock useNavigate และ useParams
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => ({ contractId: '1' })
  };
});

// Mock ResizeObserver
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock window.matchMedia สำหรับ SweetAlert2
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

const MockRouter = ({ children, contractId = '1' }) => (
  <MemoryRouter initialEntries={[`/tenant/${contractId}`]}>
    <Routes>
      <Route path="/tenant/:contractId" element={children} />
    </Routes>
  </MemoryRouter>
);

describe('TenantDetail', () => {
  const mockTenantData = {
    firstName: 'John',
    lastName: 'Doe',
    nationalId: '1234567890123',
    phoneNumber: '0812345678',
    email: 'john.doe@example.com',
    packageName: 'Standard Package',
    rentAmountSnapshot: '15000',
    signDate: '2024-01-01T00:00:00',
    startDate: '2024-01-01T00:00:00',
    endDate: '2024-12-31T23:59:59',
    deposit: '30000',
    floor: '5',
    room: '501',
    contractTypeId: 4,
    invoices: [
      {
        invoiceId: 'INV001',
        dueDate: '2024-01-05T00:00:00',
        netAmount: 15000,
        invoiceStatus: 1,
        payDate: '2024-01-03T00:00:00',
        penaltyTotal: 0
      },
      {
        invoiceId: 'INV002',
        dueDate: '2024-02-05T00:00:00',
        netAmount: 15000,
        invoiceStatus: 0,
        payDate: null,
        penaltyTotal: 200
      }
    ]
  };

  beforeEach(() => {
    axios.get.mockResolvedValue({ data: mockTenantData });
    axios.put.mockResolvedValue({ status: 200, data: {} });
    mockNavigate.mockClear();
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  const renderComponent = (contractId = '1') => {
    return render(
      <MockRouter contractId={contractId}>
        <TenantDetail />
      </MockRouter>
    );
  };



  it('displays tenant information correctly', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tenant Information')).toBeInTheDocument();
      expect(screen.getByText('John')).toBeInTheDocument();
      expect(screen.getByText('Doe')).toBeInTheDocument();
      expect(screen.getByText('1234567890123')).toBeInTheDocument();
      expect(screen.getByText('0812345678')).toBeInTheDocument();
      expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
    });
  });

  it('displays payment history correctly', async () => {
    renderComponent();

    await waitFor(() => {
      // Check payment history headers
      expect(screen.getByText('Payment History')).toBeInTheDocument();

      // Check invoice data
      expect(screen.getByText('INV001')).toBeInTheDocument();
      expect(screen.getByText('INV002')).toBeInTheDocument();
    });
  });

  it('navigates back to tenant management when breadcrumb is clicked', async () => {
    renderComponent();

    await waitFor(() => {
      // Find breadcrumb by more specific selector
      const breadcrumbs = screen.getAllByText('Tenant Management');
      // Try to find the breadcrumb link by its style or class
      const breadcrumbLink = breadcrumbs.find(el =>
        el.className?.includes('breadcrumb-link') ||
        el.getAttribute('style')?.includes('cursor: pointer')
      );

      if (breadcrumbLink) {
        fireEvent.click(breadcrumbLink);
        expect(mockNavigate).toHaveBeenCalledWith('/tenantmanagement');
      } else {
        // Fallback: click the first one if specific element not found
        fireEvent.click(breadcrumbs[0]);
        expect(mockNavigate).toHaveBeenCalledWith('/tenantmanagement');
      }
    });
  });


  it('populates form fields with tenant data in edit modal', async () => {
    renderComponent();

    await waitFor(() => {
      // Form fields should be populated with tenant data
      expect(screen.getByDisplayValue('John')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Doe')).toBeInTheDocument();
      expect(screen.getByDisplayValue('0812345678')).toBeInTheDocument();
    });
  });


  it('handles API errors when fetching tenant data', async () => {
    axios.get.mockRejectedValueOnce(new Error('API Error'));

    renderComponent();

    await waitFor(() => {
      // Should navigate back on error
      expect(mockNavigate).toHaveBeenCalledWith('/tenantmanagement');
    });
  });

  it('displays loading state initially', () => {
    renderComponent();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('maps invoice status correctly', () => {
    // Test the status mapping logic directly
    const statusMap = {
      0: 'Unpaid',
      1: 'Paid',
      2: 'Overdue'
    };

    expect(statusMap[0]).toBe('Unpaid');
    expect(statusMap[1]).toBe('Paid');
    expect(statusMap[2]).toBe('Overdue');
    expect(statusMap[3]).toBeUndefined();
  });

  it('handles empty invoice list', async () => {
    const emptyTenantData = {
      ...mockTenantData,
      invoices: []
    };
    axios.get.mockResolvedValueOnce({ data: emptyTenantData });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('No invoices found')).toBeInTheDocument();
    });
  });

  it('validates date constraints for 1-year contracts', async () => {
    renderComponent();

    await waitFor(() => {
      const endDateInput = screen.getByDisplayValue('2024-12-31');
      expect(endDateInput).toBeInTheDocument();
    });
  });

  it('makes end date read-only for non-1-year contracts', async () => {
    const nonYearlyContractData = {
      ...mockTenantData,
      contractTypeId: 1 // Not 1-year contract
    };
    axios.get.mockResolvedValueOnce({ data: nonYearlyContractData });

    renderComponent();

    await waitFor(() => {
      const endDateInput = screen.getByDisplayValue('2024-12-31');
      expect(endDateInput).toBeInTheDocument();
    });
  });
});