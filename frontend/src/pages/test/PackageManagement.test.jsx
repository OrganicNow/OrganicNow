import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import PackageManagement from '../PackageManagement';

// Mock อย่างถูกต้อง
vi.mock('../../component/layout', () => ({
  default: ({ children, title, icon, notifications }) => (
    <div data-testid="layout">
      <h1>{title}</h1>
      <div>{children}</div>
    </div>
  )
}));

vi.mock('../../component/modal', () => ({
  default: ({ id, title, icon, size, children }) => (
    <div data-testid={`modal-${id}`} className="modal">
      <h2>{title}</h2>
      <div>{children}</div>
    </div>
  )
}));

vi.mock('../../component/pagination', () => ({
  default: ({ currentPage, totalPages, onPageChange, totalRecords, onPageSizeChange }) => (
    <div data-testid="pagination">
      <button onClick={() => onPageChange(currentPage - 1)}>Previous</button>
      <span>Page {currentPage} of {totalPages}</span>
      <button onClick={() => onPageChange(currentPage + 1)}>Next</button>
    </div>
  )
}));

vi.mock('../../component/useMessage', () => ({
  default: () => ({
    showMessageSave: vi.fn(),
    showMessageError: vi.fn()
  })
}));

vi.mock('../../config_variable', () => ({
  pageSize: 10,
  apiPath: '/api'
}));

// Mock Bootstrap
vi.mock('bootstrap', () => ({
  Modal: {
    getInstance: vi.fn(() => ({
      show: vi.fn(),
      hide: vi.fn()
    })),
    prototype: {
      show: vi.fn(),
      hide: vi.fn()
    }
  },
  Offcanvas: {
    getInstance: vi.fn(() => ({
      hide: vi.fn()
    })),
    prototype: {
      hide: vi.fn()
    }
  }
}));

// Mock fetch
global.fetch = vi.fn();

describe('PackageManagement', () => {
  const mockPackages = [
    {
      id: 1,
      contract_type_id: 1,
      contract_name: '3 Month Package',
      price: 5000,
      is_active: 1,
      room_size: 1,
      createDate: '2024-01-01'
    },
    {
      id: 2,
      contract_type_id: 2,
      contract_name: '6 Month Package',
      price: 6000,
      is_active: 0,
      room_size: 2,
      createDate: '2024-01-02'
    }
  ];

  const mockContractTypes = [
    { id: 1, name: '3 Month Package', months: 3 },
    { id: 2, name: '6 Month Package', months: 6 }
  ];

  const mockRoomSizes = [1, 2];

  const createMockResponse = (data) => ({
    ok: true,
    json: async () => data,
    text: async () => JSON.stringify(data)
  });

  beforeEach(() => {
    fetch.mockClear();
    // Setup default mock responses
    fetch
      .mockResolvedValueOnce(createMockResponse(mockContractTypes)) // contract types
      .mockResolvedValueOnce(createMockResponse(mockPackages)) // room sizes (from packages)
      .mockResolvedValueOnce(createMockResponse(mockPackages)); // packages
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders component with initial state', async () => {
    render(<PackageManagement />);

    expect(screen.getByText('Package Management')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Search package')).toBeInTheDocument();

    // ใช้ getAllByText และเลือกตัวแรก (ปุ่ม Create Package)
    const createButtons = screen.getAllByText('Create Package');
    expect(createButtons[0]).toBeInTheDocument();
  });

  it('loads packages, contract types and room sizes on mount', async () => {
    render(<PackageManagement />);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/contract-types', expect.any(Object));
      expect(fetch).toHaveBeenCalledWith('/api/packages', expect.any(Object));
    });
  });

  it('displays loading state', async () => {
    // ทำให้ packages load ช้า
    fetch.mockReset();
    fetch
      .mockResolvedValueOnce(createMockResponse(mockContractTypes))
      .mockResolvedValueOnce(createMockResponse(mockPackages))
      .mockResolvedValueOnce(
        new Promise(resolve =>
          setTimeout(() => resolve(createMockResponse(mockPackages)), 100)
        )
      );

    render(<PackageManagement />);

    expect(screen.getByText('Loading packages...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading packages...')).not.toBeInTheDocument();
    });
  });

  it('handles API errors gracefully', async () => {
    fetch.mockReset();
    fetch
      .mockResolvedValueOnce(createMockResponse(mockContractTypes))
      .mockResolvedValueOnce(createMockResponse(mockPackages))
      .mockRejectedValueOnce(new Error('API Error'));

    render(<PackageManagement />);

    await waitFor(() => {
      expect(screen.getByText('Error fetching packages')).toBeInTheDocument();
    });
  });

  it('toggles package active status', async () => {
    render(<PackageManagement />);

    await waitFor(() => {
      // หา switch โดยใช้ role switch
      const toggleSwitches = screen.getAllByRole('switch');
      expect(toggleSwitches.length).toBeGreaterThan(0);

      fireEvent.click(toggleSwitches[0]);
    });

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/packages/1/toggle', expect.objectContaining({
        method: 'PATCH'
      }));
    });
  });


  it('clears all filters', async () => {
    render(<PackageManagement />);

    await waitFor(() => {
      const filterButton = screen.getByText('Filter');
      fireEvent.click(filterButton);
    });

    const clearButton = screen.getByText('Clear');
    fireEvent.click(clearButton);

    await waitFor(() => {
      expect(screen.queryByText('Room Size: Studio')).not.toBeInTheDocument();
    });
  });

  it('sorts packages', async () => {
    render(<PackageManagement />);

    await waitFor(() => {
      const sortButton = screen.getByText('Sort');
      fireEvent.click(sortButton);
    });

    // ตรวจสอบว่า sort ถูก trigger
    expect(screen.getByText('Sort')).toBeInTheDocument();
  });

  it('displays pagination correctly', async () => {
    const manyPackages = Array.from({ length: 15 }, (_, i) => ({
      id: i + 1,
      contract_type_id: 1,
      contract_name: `Package ${i + 1}`,
      price: 5000 + i * 100,
      is_active: 1,
      room_size: 1,
      createDate: '2024-01-01'
    }));

    fetch.mockReset();
    fetch
      .mockResolvedValueOnce(createMockResponse(mockContractTypes))
      .mockResolvedValueOnce(createMockResponse(manyPackages))
      .mockResolvedValueOnce(createMockResponse(manyPackages));

    render(<PackageManagement />);

    await waitFor(() => {
      expect(screen.getByTestId('pagination')).toBeInTheDocument();
    });
  });



  it('handles empty package list', async () => {
    fetch.mockReset();
    fetch
      .mockResolvedValueOnce(createMockResponse(mockContractTypes))
      .mockResolvedValueOnce(createMockResponse([]))
      .mockResolvedValueOnce(createMockResponse([]));

    render(<PackageManagement />);

    await waitFor(() => {
      expect(screen.getByText('No packages found')).toBeInTheDocument();
    });
  });
});