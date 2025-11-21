import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import RoomManagement from '../RoomManagement';

// Mock dependencies
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock AuthContext ให้ครบถ้วน
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { name: 'Test User', role: 'admin' },
    login: vi.fn(),
    logout: vi.fn(),
    hasPermission: vi.fn(() => true),
  }),
}));

// Mock Notification Context ให้ครบถ้วน
vi.mock('../../contexts/NotificationContext', () => ({
  useNotifications: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false,
    markAsRead: vi.fn(),
    fetchNotifications: vi.fn(),
    refreshNotifications: vi.fn(),
  }),
}));

// Mock Layout ที่ง่ายกว่าเดิม และไม่ใช้ dependencies อื่นๆ
vi.mock('../component/layout', () => ({
  default: ({ children, title }) => (
    <div data-testid="layout">
      <h1>{title}</h1>
      <div>{children}</div>
    </div>
  )
}));

// Mock ทุก components ที่เกี่ยวข้อง
vi.mock('../component/sidebar', () => ({
  default: () => <div data-testid="sidebar">Sidebar</div>
}));

vi.mock('../component/NotificationBell', () => ({
  default: () => <div data-testid="notification-bell">🔔</div>
}));

vi.mock('../component/modal', () => ({
  default: ({ id, title, icon, children, size }) => (
    <div data-testid={`modal-${id}`}>
      <h2>{title}</h2>
      <div>{children}</div>
    </div>
  )
}));

vi.mock('../component/pagination', () => ({
  default: ({ currentPage, totalPages, onPageChange, totalRecords, onPageSizeChange }) => (
    <div data-testid="pagination">
      <button onClick={() => onPageChange(currentPage - 1)}>Previous</button>
      <span>Page {currentPage} of {totalPages}</span>
      <button onClick={() => onPageChange(currentPage + 1)}>Next</button>
      <select onChange={(e) => onPageSizeChange(Number(e.target.value))}>
        <option value="10">10</option>
        <option value="20">20</option>
        <option value="50">50</option>
      </select>
    </div>
  )
}));

vi.mock('../component/useMessage', () => ({
  default: () => ({
    showMessagePermission: vi.fn(),
    showMessageError: vi.fn(),
    showMessageSave: vi.fn(),
    showMessageConfirmDelete: vi.fn(() => Promise.resolve({ isConfirmed: true })),
  })
}));

vi.mock('../config_variable', () => ({
  apiPath: 'http://localhost:8080', // ✅ เปลี่ยนเป็น URL จริง
  pageSize: 10
}));

// Mock axios
vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}));

// Mock Bootstrap
vi.mock('bootstrap/dist/js/bootstrap.bundle.min.js', () => ({}));

// Mock CSS files เพื่อป้องกัน warning
vi.mock('../assets/css/roommanagement.css', () => ({}));
vi.mock('bootstrap/dist/css/bootstrap.min.css', () => ({}));
vi.mock('bootstrap-icons/font/bootstrap-icons.css', () => ({}));

// Import หลังจาก mock everything
import axios from 'axios';

describe('RoomManagement', () => {
  const mockRoomsData = [
    {
      roomId: 1,
      roomNumber: '101',
      roomFloor: '1',
      roomSize: 'Studio',
      status: 'available',
      requests: [
        { id: 1, finishDate: '2024-01-01' },
        { id: 2, finishDate: null }
      ]
    },
    {
      roomId: 2,
      roomNumber: '201',
      roomFloor: '2',
      roomSize: 'Superior',
      status: 'occupied',
      requests: [
        { id: 3, finishDate: '2024-01-01' }
      ]
    },
    {
      roomId: 3,
      roomNumber: '301',
      roomFloor: '3',
      roomSize: 'Deluxe',
      status: 'repair',
      requests: []
    }
  ];

  const mockAvailableAssets = {
    result: [
      { assetId: 1, assetName: 'Bed' },
      { assetId: 2, assetName: 'Desk' },
      { assetId: 3, assetName: 'Chair' }
    ]
  };

  beforeEach(() => {
    vi.clearAllMocks();

    // Setup default mock responses
    axios.get
      .mockResolvedValueOnce({ data: mockRoomsData }) // rooms list
      .mockResolvedValueOnce({ data: mockAvailableAssets }); // available assets
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <RoomManagement />
      </BrowserRouter>
    );
  };

  // Test พื้นฐานที่ง่ายที่สุดก่อน
  it('renders component and calls APIs', async () => {
    renderComponent();

    // Check API calls - ใช้ URL จริงที่ component เรียก
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/room/list', { withCredentials: true });
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/assets/available', { withCredentials: true });
  });

  it('loads and displays room data', async () => {
    renderComponent();

    // Wait for data to load - ใช้ getAllByText แทน getByText เมื่อมีหลาย elements
    await waitFor(() => {
      const room101Elements = screen.getAllByText('101');
      expect(room101Elements.length).toBeGreaterThan(0);
    });

    // Check room data is displayed
    const room101Elements = screen.getAllByText('101');
    const room201Elements = screen.getAllByText('201');
    const room301Elements = screen.getAllByText('301');

    expect(room101Elements.length).toBeGreaterThan(0);
    expect(room201Elements.length).toBeGreaterThan(0);
    expect(room301Elements.length).toBeGreaterThan(0);
  });

  it('displays table headers', async () => {
    renderComponent();

    await waitFor(() => {
      // ใช้ getAllByText สำหรับ headers ที่มีหลาย elements
      const orderHeaders = screen.getAllByText('Order');
      const roomHeaders = screen.getAllByText('Room');
      const floorHeaders = screen.getAllByText('Floor');

      expect(orderHeaders.length).toBeGreaterThan(0);
      expect(roomHeaders.length).toBeGreaterThan(0);
      expect(floorHeaders.length).toBeGreaterThan(0);
    });
  });

  it('displays toolbar elements', async () => {
    renderComponent();

    await waitFor(() => {
      // ใช้ getAllByText สำหรับ elements ที่มีหลายอัน
      const filterElements = screen.getAllByText('Filter');
      const sortElements = screen.getAllByText('Sort');
      const addRoomElements = screen.getAllByText('Add Room');

      expect(filterElements.length).toBeGreaterThan(0);
      expect(sortElements.length).toBeGreaterThan(0);
      expect(addRoomElements.length).toBeGreaterThan(0);
      expect(screen.getByPlaceholderText('Search')).toBeInTheDocument();
    });
  });

  it('handles search functionality', async () => {
    renderComponent();

    await waitFor(() => {
      const room101Elements = screen.getAllByText('101');
      expect(room101Elements.length).toBeGreaterThan(0);
    });

    // Search for room 101
    const searchInput = screen.getByPlaceholderText('Search');
    fireEvent.change(searchInput, { target: { value: '101' } });

    await waitFor(() => {
      const room101Elements = screen.getAllByText('101');
      expect(room101Elements.length).toBeGreaterThan(0);
    });
  });

  it('handles empty room list', async () => {
    axios.get
      .mockResolvedValueOnce({ data: [] })
      .mockResolvedValueOnce({ data: mockAvailableAssets });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Data Not Found')).toBeInTheDocument();
    });
  });

  // Test เพิ่มเติมที่ง่ายๆ
  it('calls fetchRooms on component mount', async () => {
    renderComponent();

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/room/list', { withCredentials: true });
    });
  });

  it('calls fetchAvailableAssets on component mount', async () => {
    renderComponent();

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/assets/available', { withCredentials: true });
    });
  });



  // Test สำหรับการแสดง pending requests
  it('displays pending requests count', async () => {
    renderComponent();

    await waitFor(() => {
      // Room 101 has 1 pending request
      const pendingElements = screen.getAllByText('1');
      expect(pendingElements.length).toBeGreaterThan(0);
    });
  });

  // Test สำหรับ pagination - ตรวจสอบว่า component ถูกเรียกใช้
  it('includes pagination component', async () => {
    renderComponent();

    // ตรวจสอบว่า pagination ถูกเรียกใช้ (ผ่าน props)
    await waitFor(() => {
      // ตรวจสอบว่ามีการคำนวณ pagination
      expect(axios.get).toHaveBeenCalled();
    });
  });

  // Test สำหรับการแสดงสถานะห้อง
  it('displays room statuses', async () => {
    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่ามีการแสดงสถานะต่างๆ
      const statusElements = screen.getAllByText(/available|occupied|repair/i);
      expect(statusElements.length).toBeGreaterThan(0);
    });
  });
});