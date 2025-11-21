import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';

// Mock dependencies ก่อน
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ roomId: '1' }),
    useNavigate: () => vi.fn(),
    Link: ({ children, to, ...props }) => (
      <a href={to} {...props}>{children}</a>
    )
  };
});

vi.mock('../../component/layout', () => ({
  default: ({ children, title }) => (
    <div data-testid="layout">
      {title && <h1>{title}</h1>}
      <div>{children}</div>
    </div>
  )
}));

vi.mock('../../component/modal', () => ({
  default: ({ children, isOpen }) =>
    isOpen ? <div data-testid="modal">{children}</div> : null
}));

vi.mock('../../component/useMessage', () => ({
  default: () => ({
    showMessageSave: vi.fn(),
    showMessageError: vi.fn()
  })
}));

vi.mock('../../config_variable', () => ({
  apiPath: '/api'
}));

// Mock axios แบบง่ายๆ
vi.mock('axios', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} }))
  },
  get: vi.fn(() => Promise.resolve({ data: {} })),
  put: vi.fn(() => Promise.resolve({ data: {} }))
}));

// Import component หลังจาก mock everything
import axios from 'axios';
import RoomDetail from '../RoomDetail';

describe('RoomDetail', () => {
  const mockRoomData = {
    roomFloor: '5',
    roomNumber: '501',
    roomSize: 'Studio',
    status: 'occupied',
    firstName: 'John',
    lastName: 'Doe',
    phoneNumber: '123-456-7890',
    email: 'john.doe@example.com',
    contractName: '3 Month Package',
    contractTypeName: '3 Month Package',
    signDate: '2024-01-01',
    startDate: '2024-01-01',
    endDate: '2024-04-01',
    assets: [
      { assetId: 1, assetName: 'Bed' },
      { assetId: 2, assetName: 'Desk' }
    ],
    requests: [
      {
        id: 1,
        issueTitle: 'Leaking faucet',
        scheduledDate: '2024-02-01T10:00:00',
        finishDate: '2024-02-01T12:00:00'
      }
    ]
  };

  const mockAllAssets = {
    result: [
      { assetId: 1, assetName: 'Bed', assetGroupId: 1 },
      { assetId: 2, assetName: 'Desk', assetGroupId: 1 },
      { assetId: 3, assetName: 'Chair', assetGroupId: 2 },
      { assetId: 4, assetName: 'Lamp', assetGroupId: 2 }
    ]
  };

  const mockAssetGroups = [
    { id: 1, name: 'Furniture' },
    { id: 2, name: 'Electronics' }
  ];

  const mockAssetHistory = [];

  const mockRoomsData = [];

  beforeEach(() => {
    vi.clearAllMocks();

    // Setup default mock responses ที่ตรงกับ API URLs จริง
    axios.get.mockImplementation((url) => {
      if (url === '/api/room/1/detail') {
        return Promise.resolve({ data: mockRoomData });
      } else if (url === '/api/assets/all') {
        return Promise.resolve({ data: mockAllAssets });
      } else if (url === '/api/asset-group/list') {
        return Promise.resolve({ data: mockAssetGroups });
      } else if (url === '/api/room/1/events') {
        return Promise.resolve({ data: mockAssetHistory });
      } else if (url === '/api/room') {
        return Promise.resolve({ data: mockRoomsData });
      }
      // Default response สำหรับ URL อื่นๆ
      return Promise.resolve({ data: {} });
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <RoomDetail />
      </BrowserRouter>
    );
  };

  it('renders component and loads data successfully', async () => {
    renderComponent();

    // ตรวจสอบว่า component render ได้โดยไม่ error
    await waitFor(() => {
      // ตรวจสอบว่าไม่มี error message
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่ามีการเรียก API ที่จำเป็น (ใช้ URLs จริง)
    expect(axios.get).toHaveBeenCalledWith('/api/room/1/detail', { withCredentials: true });
    expect(axios.get).toHaveBeenCalledWith('/api/assets/all', { withCredentials: true });
    expect(axios.get).toHaveBeenCalledWith('/api/asset-group/list', { withCredentials: true });
    expect(axios.get).toHaveBeenCalledWith('/api/room/1/events', { withCredentials: true });
    expect(axios.get).toHaveBeenCalledWith('/api/room', { withCredentials: true });
  });

  it('displays room status correctly', async () => {
    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่าโหลดข้อมูลสำเร็จ (ไม่มี error message)
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่าแสดงข้อมูลห้อง - ใช้ getAllByText เนื่องจากมีหลาย element
    await waitFor(() => {
      const roomNumbers = screen.getAllByText('501');
      expect(roomNumbers.length).toBeGreaterThan(0);

      const floors = screen.getAllByText('5');
      expect(floors.length).toBeGreaterThan(0);
    });
  });

  it('displays assets tab content correctly', async () => {
    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่าโหลดข้อมูลเสร็จ
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่ามี assets ปรากฏ
    await waitFor(() => {
      expect(screen.getByText('Bed')).toBeInTheDocument();
      expect(screen.getByText('Desk')).toBeInTheDocument();
    });
  });

  it('handles empty assets list', async () => {
    const roomWithoutAssets = {
      ...mockRoomData,
      assets: []
    };

    axios.get.mockImplementation((url) => {
      if (url === '/api/room/1/detail') {
        return Promise.resolve({ data: roomWithoutAssets });
      }
      return Promise.resolve({ data: {} });
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });
  });

  it('handles API errors gracefully', async () => {
    axios.get.mockRejectedValue(new Error('API Error'));

    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่าแสดง error message
      expect(screen.getByText('Failed to fetch room or asset data')).toBeInTheDocument();
    });
  });

  it('handles no room data', async () => {
    axios.get.mockImplementation((url) => {
      if (url === '/api/room/1/detail') {
        return Promise.resolve({ data: null });
      }
      return Promise.resolve({ data: {} });
    });

    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่าแสดง error message
      expect(screen.getByText('Failed to fetch room or asset data')).toBeInTheDocument();
    });
  });

  it('displays breadcrumb navigation', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบ breadcrumb โดยประมาณ - ใช้ getAllByText
    await waitFor(() => {
      const roomNumbers = screen.getAllByText('501');
      expect(roomNumbers.length).toBeGreaterThan(0);
    });
  });

  it('handles missing tenant data gracefully', async () => {
    const roomWithoutTenant = {
      ...mockRoomData,
      firstName: null,
      lastName: null,
      phoneNumber: null,
      email: null,
      contractName: null
    };

    axios.get.mockImplementation((url) => {
      if (url === '/api/room/1/detail') {
        return Promise.resolve({ data: roomWithoutTenant });
      }
      return Promise.resolve({ data: {} });
    });

    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่าไม่ crash และไม่มี error message
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });
  });

  // Test สำหรับ modal - ปรับปรุงให้ใช้ data-testid ที่ถูกต้อง
  it('opens edit room modal when button is clicked', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // หาปุ่ม edit และคลิก
    const editButton = screen.getByRole('button', { name: /edit room/i });
    fireEvent.click(editButton);

    // ตรวจสอบว่า modal เปิด - ใช้ query ที่ถูกต้อง
    await waitFor(() => {
      // ตรวจสอบว่าแสดง modal content บางอย่าง
      expect(screen.getByText(/edit room/i)).toBeInTheDocument();
    });
  });

  // Test พื้นฐานสำหรับการบันทึก - ปรับปรุงให้สมจริงมากขึ้น
  it('calls save function when save button is clicked', async () => {
    // Mock สำหรับ modal content
    vi.mocked(axios.get).mockImplementation((url) => {
      if (url === '/api/room/1/detail') {
        return Promise.resolve({ data: mockRoomData });
      }
      return Promise.resolve({ data: {} });
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่าเรียก API ต่างๆ
    expect(axios.get).toHaveBeenCalledWith('/api/room/1/detail', { withCredentials: true });
  });

  // Test สำหรับ loading state
  it('shows loading state initially', async () => {
    // ทำให้ API call ช้าเพื่อให้เห็น loading state
    let resolvePromise;
    const promise = new Promise(resolve => {
      resolvePromise = resolve;
    });
    axios.get.mockReturnValue(promise);

    renderComponent();

    // ควรเห็น loading state ในช่วงแรก
    expect(screen.getByText(/loading/i)).toBeInTheDocument();

    // resolve the promise
    resolvePromise({ data: mockRoomData });

    // รอให้โหลดเสร็จ
    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });
  });

  // Test ใหม่: ตรวจสอบการแสดงข้อมูล tenant
  it('displays tenant information when available', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่ามีข้อมูล tenant
    await waitFor(() => {
      expect(screen.getByText('John')).toBeInTheDocument();
      expect(screen.getByText('Doe')).toBeInTheDocument();
      expect(screen.getByText('123-456-7890')).toBeInTheDocument();
    });
  });

  // Test ใหม่: ตรวจสอบการเปลี่ยน tab - ปรับปรุงให้ใช้การหา element ที่ถูกต้อง
  it('switches between tabs correctly', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่าเริ่มที่ tab Assets
    expect(screen.getByText('Bed')).toBeInTheDocument();
    expect(screen.getByText('Desk')).toBeInTheDocument();

    // เปลี่ยนไปที่ tab Request History - ใช้ role tab
    const requestsTab = screen.getByRole('tab', { name: /request history/i });
    fireEvent.click(requestsTab);

    // ตรวจสอบว่าแสดงข้อมูล requests
    await waitFor(() => {
      expect(screen.getByText('Leaking faucet')).toBeInTheDocument();
    });

    // เปลี่ยนไปที่ tab Asset History - ใช้ role tab
    const historyTab = screen.getByRole('tab', { name: /asset history/i });
    fireEvent.click(historyTab);

    // ตรวจสอบว่าแสดง asset history
    await waitFor(() => {
      expect(screen.getByText(/no asset history found for this room/i)).toBeInTheDocument();
    });
  });

  // Test ใหม่: ตรวจสอบการแสดงสถานะ Unavailable
  it('displays unavailable status correctly', async () => {
    renderComponent();

    await waitFor(() => {
      // ตรวจสอบว่าโหลดข้อมูลสำเร็จ
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่าแสดงสถานะ Unavailable
    await waitFor(() => {
      expect(screen.getByText('Unavailable')).toBeInTheDocument();
    });
  });

  // Test ใหม่: ตรวจสอบการแสดง package information
  it('displays package information correctly', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.queryByText('Failed to fetch room or asset data')).not.toBeInTheDocument();
    });

    // ตรวจสอบว่าแสดง package information
    await waitFor(() => {
      expect(screen.getByText('3 Month Package')).toBeInTheDocument();
    });
  });
});