// MaintenanceDetails.test.jsx
import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import MaintenanceDetails from '../MaintenanceDetails';

// Mock all dependencies
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    login: vi.fn(),
    logout: vi.fn(),
    isAuthenticated: true,
    isLoading: false,
    user: { id: 1, username: 'testuser' }
  })
}));

vi.mock('../../component/layout', () => ({
  default: ({ children, title, icon, notifications }) => (
    <div data-testid="layout">
      <div data-testid="layout-title">{title}</div>
      <div data-testid="layout-icon">{icon}</div>
      <div data-testid="layout-notifications">{notifications}</div>
      {children}
    </div>
  )
}));

vi.mock('../../component/modal', () => ({
  default: ({ id, title, icon, size, scrollable, children }) => (
    <div data-testid={`modal-${id}`}>
      <div data-testid="modal-title">{title}</div>
      <div data-testid="modal-icon">{icon}</div>
      {children}
    </div>
  )
}));

vi.mock('../../component/useMessage', () => ({
  default: () => ({
    showMessageError: vi.fn(),
    showMessageSave: vi.fn(),
    showMessageConfirmDelete: vi.fn().mockResolvedValue({ isConfirmed: true })
  })
}));

vi.mock('../../config', () => ({
  default: {
    API_BASE: 'http://localhost:8080'
  }
}));

// Mock Bootstrap
vi.mock('bootstrap', () => ({
  Modal: vi.fn(() => ({
    show: vi.fn(),
    hide: vi.fn(),
    dispose: vi.fn()
  }))
}));

// Mock React Router hooks
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useLocation: vi.fn(),
    useNavigate: vi.fn(),
    useSearchParams: vi.fn(),
  };
});

// Mock environment variables
vi.stubGlobal('import.meta', {
  env: {
    VITE_API_URL: 'http://localhost:8080'
  }
});

// Mock fetch globally
global.fetch = vi.fn();

// Mock URL methods
global.URL.createObjectURL = vi.fn();
global.URL.revokeObjectURL = vi.fn();

const renderWithRouter = (component, routeParams = {}) => {
  const {
    initialEntries = ['/maintenance/1'],
    locationState = { id: 1 },
    searchParams = new URLSearchParams()
  } = routeParams;

  // Mock hooks
  vi.mocked(useLocation).mockReturnValue({
    pathname: '/maintenance/1',
    search: '',
    hash: '',
    state: locationState,
    key: 'test-key'
  });

  vi.mocked(useNavigate).mockReturnValue(vi.fn());
  vi.mocked(useSearchParams).mockReturnValue([searchParams, vi.fn()]);

  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <Routes>
        <Route path="/maintenance/:id" element={component} />
        <Route path="/maintenancerequest" element={<div>Maintenance Request Page</div>} />
      </Routes>
    </MemoryRouter>
  );
};

describe('MaintenanceDetails Component', () => {
  let mockNavigate;

  beforeEach(() => {
    vi.clearAllMocks();

    mockNavigate = vi.fn();
    vi.mocked(useNavigate).mockReturnValue(mockNavigate);

    // Default mock implementations
    vi.mocked(useLocation).mockReturnValue({
      pathname: '/maintenance/1',
      search: '',
      hash: '',
      state: { id: 1 },
      key: 'test-key'
    });

    vi.mocked(useSearchParams).mockReturnValue([
      new URLSearchParams(),
      vi.fn()
    ]);

    // Mock successful API responses
    global.fetch.mockImplementation((url) => {
      console.log('Fetch called with:', url);

      if (url.includes('/maintain/1')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            id: 1,
            roomId: 101,
            roomNumber: '101',
            roomFloor: '1',
            targetType: 0,
            issueTitle: 'Air Conditioner Issue',
            issueCategory: 0,
            issueDescription: 'AC not cooling properly',
            createDate: '2024-01-15T10:00:00',
            scheduledDate: '2024-01-20T14:00:00',
            finishDate: null,
            maintainType: 'fix',
            technicianName: 'John Doe',
            technicianPhone: '0812345678',
            workImageUrl: '/images/work1.jpg'
          })
        });
      }

      if (url.includes('/tenant/list')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve([
            {
              roomId: 101,
              status: 1,
              firstName: 'John',
              lastName: 'Smith',
              nationalId: '1234567890123',
              phoneNumber: '0898765432',
              email: 'john@example.com',
              contractName: 'Premium Package',
              signDate: '2024-01-01T00:00:00',
              startDate: '2024-01-01T00:00:00',
              endDate: '2024-12-31T00:00:00'
            }
          ])
        });
      }

      if (url.includes('/assets/101')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            result: [
              { assetId: 1, assetName: 'Air Conditioner', assetGroupName: 'Cooling' },
              { assetId: 2, assetName: 'Refrigerator', assetGroupName: 'Kitchen' }
            ]
          })
        });
      }

      // Default response
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({})
      });
    });
  });

  it('renders maintenance details correctly', async () => {
    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    expect(screen.getByTestId('layout-title')).toHaveTextContent('Maintenance Details');
    // ใช้ query ที่เฉพาะเจาะจงมากขึ้นเพื่อหลีกเลี่ยง element ที่ซ้ำกัน
    expect(screen.getByText(/Maintenance Request/)).toBeInTheDocument();
  });

  it('loads and displays maintenance data', async () => {
    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      // ใช้ getAllByText และตรวจสอบว่าเจออย่างน้อย 1 element
      const roomInfoElements = screen.getAllByText('Room Information');
      expect(roomInfoElements.length).toBeGreaterThan(0);

      const requestInfoElements = screen.getAllByText('Request Information');
      expect(requestInfoElements.length).toBeGreaterThan(0);
    });
  });

  it('loads and displays tenant information', async () => {
    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      const tenantInfoElements = screen.getAllByText('Tenant Information');
      expect(tenantInfoElements.length).toBeGreaterThan(0);
    });
  });

  it('shows loading state initially', async () => {
    // Make fetch hang to test loading state
    global.fetch.mockImplementation(() => new Promise(() => {}));

    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    // ใช้ getAllByText เนื่องจากมีหลาย element ที่มี text "Loading..."
    const loadingElements = screen.getAllByText('Loading...');
    expect(loadingElements.length).toBeGreaterThan(0);

    expect(screen.getByText('Loading tenant info...')).toBeInTheDocument();
  });

  it('handles API errors gracefully', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network error'));

    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      expect(screen.getByText('Failed to load maintenance.')).toBeInTheDocument();
    });
  });

  it('displays correct status badges for different states', async () => {
    // Test "Not Started" status
    global.fetch.mockImplementation((url) => {
      if (url.includes('/maintain/1')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            id: 1,
            roomNumber: '101',
            scheduledDate: null,
            finishDate: null,
            targetType: 0,
            issueTitle: 'Test Issue'
          })
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      // ใช้ regex เพื่อหาเฉพาะ badge เท่านั้น ไม่รวม option ใน dropdown
      const notStartedBadges = screen.getAllByText(/Not Started/).filter(element =>
        element.classList.contains('badge') ||
        element.closest('.badge')
      );
      expect(notStartedBadges.length).toBeGreaterThan(0);
    });
  });


  it('handles edit button click', async () => {
    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      // ใช้ getAllByText และเลือกปุ่ม Edit Request ที่แท้จริง
      const editButtons = screen.getAllByText('Edit Request').filter(element =>
        element.tagName === 'BUTTON'
      );
      expect(editButtons.length).toBeGreaterThan(0);

      fireEvent.click(editButtons[0]);
    });

    // Check if modal is rendered
    expect(screen.getByTestId('modal-editMaintainModal')).toBeInTheDocument();
  });

  it('handles form submission', async () => {
    global.fetch.mockImplementation((url, options) => {
      if (url.includes('/maintain/update/1') && options.method === 'PUT') {
        return Promise.resolve({
          ok: true,
          text: () => Promise.resolve('Success')
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    // Wait for data to load
    await waitFor(() => {
      expect(screen.getByTestId('modal-editMaintainModal')).toBeInTheDocument();
    });

    // Form should be rendered in modal
    expect(screen.getByText('Repair Information')).toBeInTheDocument();
  });

  it('handles quick action buttons', async () => {
    // Mock maintenance without scheduled date (Not Started)
    global.fetch.mockImplementation((url) => {
      if (url.includes('/maintain/1')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            id: 1,
            roomNumber: '101',
            scheduledDate: null,
            finishDate: null,
            targetType: 0,
            issueTitle: 'Test Issue'
          })
        });
      }
      if (url.includes('/maintain/update/1')) {
        return Promise.resolve({
          ok: true,
          text: () => Promise.resolve('Success')
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      // Should show Start Work button for Not Started status
      const startWorkButton = screen.getByText('Start Work');
      expect(startWorkButton).toBeInTheDocument();
    });
  });

  it('displays work evidence image when available', async () => {
    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      expect(screen.getByAltText('Work Evidence')).toBeInTheDocument();
    });
  });

  it('handles no tenant found scenario', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/maintain/1')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            id: 1,
            roomId: 999, // Non-existent room
            roomNumber: '999',
            targetType: 0,
            issueTitle: 'Test Issue'
          })
        });
      }
      if (url.includes('/tenant/list')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve([]) // No tenants
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    await act(async () => {
      renderWithRouter(<MaintenanceDetails />);
    });

    await waitFor(() => {
      expect(screen.getByText('No active tenant found for this room')).toBeInTheDocument();
    });
  });


});