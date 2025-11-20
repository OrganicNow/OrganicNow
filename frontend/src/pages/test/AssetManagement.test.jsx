import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import React from 'react';
import Swal from 'sweetalert2';

// ===== MOCK ทุกอย่างก่อน import component =====

// Mock axios
vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

// Mock CSS
vi.mock('bootstrap/dist/js/bootstrap.bundle.min.js', () => ({}));
vi.mock('bootstrap/dist/css/bootstrap.min.css', () => ({}));
vi.mock('bootstrap-icons/font/bootstrap-icons.css', () => ({}));
vi.mock('../assets/css/asset.css', () => ({}));
vi.mock('../assets/css/alert.css', () => ({}));

// Mock config
vi.mock('../config_variable', () => ({
  pageSize: 10,
  apiPath: 'http://localhost:8080/api',
}));

// Mock Layout (mock แบบง่ายที่สุด ไม่ render component ภายใน)
vi.mock('../component/layout', () => ({
  default: ({ children, title }) => (
    <div data-testid="layout">
      <header>
        <h1>{title}</h1>
      </header>
      <main>{children}</main>
    </div>
  ),
}));

// Mock Modal
vi.mock('../component/modal', () => ({
  default: ({ children, id, title }) => (
    <div data-testid={`modal-${id}`}>
      <h2>{title}</h2>
      {children}
    </div>
  ),
}));

// Mock Pagination
vi.mock('../component/pagination', () => ({
  default: ({ currentPage, totalPages, totalRecords }) => (
    <div data-testid="pagination">
      <span>Page {currentPage} of {totalPages}</span>
      <span>Total: {totalRecords}</span>
    </div>
  ),
}));

// Mock NotificationBell
vi.mock('../component/NotificationBell', () => ({
  default: () => <div data-testid="notification-bell">Notifications</div>,
}));

// Mock AuthContext (ครบทุก function)
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 1, name: 'Test User', role: 'admin' },
    isAuthenticated: true,
    hasPermission: vi.fn(() => true),
    login: vi.fn(),
    logout: vi.fn(),
    checkAuth: vi.fn(),
  }),
  AuthProvider: ({ children }) => children,
}));

// Mock useNotifications
vi.mock('../../contexts/NotificationContext', () => ({
  useNotifications: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false,
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    fetchNotifications: vi.fn(),
    refreshNotifications: vi.fn(),
  }),
}));

// Mock useMessage
const mockShowMessageSave = vi.fn();
const mockShowMessageError = vi.fn();
const mockShowMessagePermission = vi.fn();
const mockShowMessageConfirmDelete = vi.fn(() => Promise.resolve({ isConfirmed: false }));

vi.mock('../component/useMessage', () => ({
  default: () => ({
    showMessageSave: mockShowMessageSave,
    showMessageError: mockShowMessageError,
    showMessagePermission: mockShowMessagePermission,
    showMessageConfirmDelete: mockShowMessageConfirmDelete,
  }),
}));
vi.mock('sweetalert2', () => ({
  fire: vi.fn().mockResolvedValue({}),
}));

// ===== ตอนนี้ค่อย import component =====
import axios from 'axios';
import AssetManagement from '../AssetManagement';

// Test data
const mockAssetGroups = [
  { id: 1, name: 'Electronics', monthlyAddonFee: 100, oneTimeDamageFee: 500, freeReplacement: true, threshold: 5 },
  { id: 2, name: 'Furniture', monthlyAddonFee: 50, oneTimeDamageFee: 300, freeReplacement: false, threshold: 3 },
];

const mockAssets = [
  { assetId: 1, assetName: 'Laptop Dell', assetGroupId: 1, status: 'available', room: 'A101' },
  { assetId: 2, assetName: 'Chair Office', assetGroupId: 2, status: 'in_use', room: 'B202' },
  { assetId: 3, assetName: 'Monitor Samsung', assetGroupId: 1, status: 'available', room: 'A101' },
];

const renderComponent = () => {
  return render(
    <BrowserRouter>
      <AssetManagement />
    </BrowserRouter>
  );
};

describe('AssetManagement Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    mockShowMessageConfirmDelete.mockResolvedValue({ isConfirmed: false });

    axios.get.mockImplementation((url) => {
      if (url.includes('/asset-group/list')) {
        return Promise.resolve({ data: mockAssetGroups });
      }
      if (url.includes('/assets/all')) {
        return Promise.resolve({ data: { result: mockAssets } });
      }
      return Promise.reject(new Error('Unknown URL'));
    });

    document.getElementById = vi.fn(() => ({
      click: vi.fn(),
    }));
  });

  describe('Initial Render', () => {
    it('should render the component', async () => {
      renderComponent();
      expect(screen.getByText('Asset Management')).toBeInTheDocument();
    });

    it('should fetch asset groups', async () => {
      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Electronics')).toBeInTheDocument();
        expect(screen.getByText('Furniture')).toBeInTheDocument();
      });
    });

    it('should fetch assets', async () => {
      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
        expect(screen.getByText('Chair Office')).toBeInTheDocument();
      });
    });
  });

  describe('Search', () => {
    it('should filter by search', async () => {
      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search asset / group');
      fireEvent.change(searchInput, { target: { value: 'Laptop' } });

      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
    });
  });

  describe('Create Group', () => {
    it('should validate empty name', async () => {
      renderComponent();

      await waitFor(() => {
        const saveButtons = screen.getAllByText('Save');
        fireEvent.click(saveButtons[0]);
      });

      expect(mockShowMessageError).toHaveBeenCalledWith('กรุณากรอกชื่อ Group');
    });

    it('should validate short name', async () => {
      renderComponent();

      await waitFor(() => {
        const inputs = screen.getAllByLabelText('Group Name');
        fireEvent.change(inputs[0], { target: { value: 'A' } });

        const saveButtons = screen.getAllByText('Save');
        fireEvent.click(saveButtons[0]);
      });

      expect(mockShowMessageError).toHaveBeenCalledWith('ชื่อ Group ต้องมีอย่างน้อย 2 ตัวอักษร');
    });

    it('should create group', async () => {
      axios.post.mockResolvedValueOnce({ data: { id: 3 } });

      renderComponent();

      await waitFor(() => {
        const inputs = screen.getAllByLabelText('Group Name');
        fireEvent.change(inputs[0], { target: { value: 'New Group' } });

        const saveButtons = screen.getAllByText('Save');
        fireEvent.click(saveButtons[0]);
      });

      await waitFor(() => {
        expect(mockShowMessageSave).toHaveBeenCalledWith('สร้าง Group สำเร็จ');
      });
    });
  });

  describe('Delete Group', () => {
    it('should delete with confirmation', async () => {
      mockShowMessageConfirmDelete.mockResolvedValueOnce({ isConfirmed: true });
      axios.delete.mockResolvedValueOnce({ data: {} });

      renderComponent();

      await waitFor(() => {
        const buttons = screen.getAllByRole('button');
        const deleteBtn = buttons.find(b => b.querySelector('.bi-trash'));
        if (deleteBtn) fireEvent.click(deleteBtn);
      });

      await waitFor(() => {
        expect(mockShowMessageSave).toHaveBeenCalledWith('ลบ Group สำเร็จ');
      });
    });
  });

  describe('Create Asset', () => {
    it('should validate asset form', async () => {
      renderComponent();

      await waitFor(() => {
        const saveButtons = screen.getAllByText('Save');
        if (saveButtons[1]) fireEvent.click(saveButtons[1]);
      });

      expect(mockShowMessageError).toHaveBeenCalledWith('กรุณากรอกชื่อและเลือกกลุ่ม');
    });

    it('should create asset', async () => {
      axios.post.mockResolvedValueOnce({ data: { assetId: 4 } });

      renderComponent();

      await waitFor(() => {
        const nameInputs = screen.getAllByLabelText('Asset Name');
        if (nameInputs[0]) {
          fireEvent.change(nameInputs[0], { target: { value: 'New Asset' } });
        }

        const selects = screen.getAllByLabelText('Asset Group');
        if (selects[0]) {
          fireEvent.change(selects[0], { target: { value: '1' } });
        }

        const saveButtons = screen.getAllByText('Save');
        if (saveButtons[1]) fireEvent.click(saveButtons[1]);
      });

      await waitFor(() => {
        expect(mockShowMessageSave).toHaveBeenCalled();
      });
    });
  });
});