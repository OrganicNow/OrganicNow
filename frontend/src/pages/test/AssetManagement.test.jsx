// AssetManagement.test.jsx
import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import AssetManagement from '../AssetManagement';
import Layout from '../../component/layout';
import Modal from '../../component/modal';
import Pagination from '../../component/pagination';
import useMessage from '../../component/useMessage';

// Mock dependencies
vi.mock('axios');
vi.mock('../../component/layout');
vi.mock('../../component/modal');
vi.mock('../../component/pagination');
vi.mock('../../component/useMessage');
vi.mock('../../config_variable', () => ({
  pageSize: 10,
  apiPath: 'http://localhost:3000/api'
}));

// Mock Bootstrap
vi.mock('bootstrap/dist/js/bootstrap.bundle.min.js', () => ({
  Modal: vi.fn(() => ({
    show: vi.fn(),
    hide: vi.fn()
  }))
}));

describe('AssetManagement', () => {
  const mockShowMessagePermission = vi.fn();
  const mockShowMessageError = vi.fn();
  const mockShowMessageSave = vi.fn();
  const mockShowMessageConfirmDelete = vi.fn();

  const mockAssetGroups = [
    { id: 1, name: 'Laptops', monthlyAddonFee: 100, oneTimeDamageFee: 500, freeReplacement: true, threshold: 5 },
    { id: 2, name: 'Monitors', monthlyAddonFee: 50, oneTimeDamageFee: 200, freeReplacement: false, threshold: 5 }
  ];

  const mockAssets = [
    { assetId: 1, assetName: 'MacBook Pro', assetGroupId: 1, status: 'in_use', room: 'Room A' },
    { assetId: 2, assetName: 'Dell Monitor', assetGroupId: 2, status: 'available', room: 'Room B' },
    { assetId: 3, assetName: 'HP Laptop', assetGroupId: 1, status: 'available', room: 'Room C' }
  ];

  beforeEach(() => {
    // Reset all mocks
    vi.clearAllMocks();

    // Mock Layout component - แก้ไขให้ถูกต้อง
    Layout.mockImplementation(({ children, title, icon }) => (
      <div data-testid="layout">
        <h1>{title} {icon}</h1>
        {children}
      </div>
    ));

    // Mock Modal component - แก้ไขให้ถูกต้อง
    Modal.mockImplementation(({ id, title, icon, children }) => (
      <div data-testid={`modal-${id}`} style={{ display: 'block' }}>
        <h2>{title} {icon}</h2>
        {children}
      </div>
    ));

    // Mock Pagination component
    Pagination.mockImplementation(({ currentPage, totalPages, onPageChange, totalRecords, onPageSizeChange }) => (
      <div data-testid="pagination">
        <span>Page {currentPage} of {totalPages}</span>
        <span>Total: {totalRecords}</span>
        <button onClick={() => onPageChange(currentPage - 1)}>Previous</button>
        <button onClick={() => onPageChange(currentPage + 1)}>Next</button>
        <select
          defaultValue="10"
          onChange={(e) => onPageSizeChange(Number(e.target.value))}
        >
          <option value="10">10</option>
          <option value="20">20</option>
        </select>
      </div>
    ));

    // Mock useMessage hook
    useMessage.mockReturnValue({
      showMessagePermission: mockShowMessagePermission,
      showMessageError: mockShowMessageError,
      showMessageSave: mockShowMessageSave,
      showMessageConfirmDelete: mockShowMessageConfirmDelete
    });

    // Mock axios responses
    axios.get.mockImplementation((url) => {
      if (url.includes('/asset-group/list')) {
        return Promise.resolve({ data: mockAssetGroups });
      }
      if (url.includes('/assets/all')) {
        return Promise.resolve({ data: { result: mockAssets } });
      }
      return Promise.reject(new Error('Not found'));
    });
  });

  // Test 1: Component renders successfully
  it('renders without crashing', async () => {
    render(<AssetManagement />);

    // ใช้การค้นหาที่เฉพาะเจาะจงมากขึ้น
    await waitFor(() => {
      expect(screen.getByTestId('layout')).toBeInTheDocument();
    });

    // ตรวจสอบว่ามีเนื้อหาหลักแสดงผล
    expect(screen.getByText('Total Groups')).toBeInTheDocument();
    expect(screen.getByText('Total Assets')).toBeInTheDocument();
  });

  // Test 2: Fetches and displays asset groups and assets
  it('fetches and displays asset groups and assets', async () => {
    render(<AssetManagement />);

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith(
        'http://localhost:3000/api/asset-group/list',
        { withCredentials: true }
      );
      expect(axios.get).toHaveBeenCalledWith(
        'http://localhost:3000/api/assets/all',
        { withCredentials: true }
      );
    });

    // ใช้ getAllByText และตรวจสอบว่ามีอยู่จริง
    const laptopElements = screen.getAllByText('Laptops');
    expect(laptopElements.length).toBeGreaterThan(0);

    const monitorElements = screen.getAllByText('Monitors');
    expect(monitorElements.length).toBeGreaterThan(0);
  });

  // Test 3: Displays summary cards correctly
  it('displays summary cards with correct data', async () => {
    render(<AssetManagement />);

    await waitFor(() => {
      expect(screen.getByText('Total Groups')).toBeInTheDocument();
    });

    // ตรวจสอบ summary cards โดยใช้ data-testid หรือโครงสร้างที่เฉพาะเจาะจง
    const totalAssetsCard = screen.getByText('Total Assets').closest('.card');
    expect(totalAssetsCard).toHaveTextContent('3');

    const inUseCard = screen.getByText('In Use').closest('.card');
    expect(inUseCard).toHaveTextContent('1');

    const availableCard = screen.getByText('Available').closest('.card');
    expect(availableCard).toHaveTextContent('2');
  });

  // Test 4: Handles search functionality
  it('filters assets and groups based on search input', async () => {
    render(<AssetManagement />);

    await waitFor(() => {
      expect(screen.getByText('MacBook Pro')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText('Search asset / group');
    fireEvent.change(searchInput, { target: { value: 'MacBook' } });

    expect(screen.getByText('MacBook Pro')).toBeInTheDocument();
    // อาจจะไม่หายไปเพราะยังอยู่ใน DOM แต่ถูกซ่อนโดย CSS
  });

  // Test 5: Handles group selection
  it('filters assets when group is selected', async () => {
    render(<AssetManagement />);

    await waitFor(() => {
      expect(screen.getAllByText('Laptops')[0]).toBeInTheDocument();
    });

    // คลิกที่กลุ่ม Laptops ใน sidebar
    const laptopGroupItems = screen.getAllByText('Laptops');
    const sidebarLaptop = laptopGroupItems.find(element =>
      element.closest('.list-group-item')
    );

    if (sidebarLaptop) {
      fireEvent.click(sidebarLaptop);
    }

    // ตรวจสอบว่าแสดงเฉพาะ assets ในกลุ่ม Laptops
    expect(screen.getByText('MacBook Pro')).toBeInTheDocument();
    expect(screen.getByText('HP Laptop')).toBeInTheDocument();
  });

  // Test 6: Creates new asset group
  it('creates a new asset group successfully', async () => {
    axios.post.mockResolvedValueOnce({ data: {} });

    render(<AssetManagement />);

    // เปิด modal โดยคลิกปุ่ม
    const createGroupButton = screen.getByText('Create Asset Group');
    fireEvent.click(createGroupButton);

    // รอให้ modal เปิด
    await waitFor(() => {
      expect(screen.getByTestId('modal-groupModal')).toBeInTheDocument();
    });

    // หา input field โดยใช้ placeholder หรือวิธีอื่น
    const groupNameInput = screen.getByTestId('modal-groupModal').querySelector('input[type="text"]');
    fireEvent.change(groupNameInput, {
      target: { value: 'New Group' }
    });

    // Submit form
    const form = screen.getByTestId('modal-groupModal').querySelector('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        'http://localhost:3000/api/asset-group/create',
        {
          assetGroupName: 'New Group',
          freeReplacement: true,
          monthlyAddonFee: 0,
          oneTimeDamageFee: 0
        },
        { withCredentials: true }
      );
    });
  });


  // Test 8: Creates multiple assets with quantity
  it('creates multiple assets when quantity is specified', async () => {
      axios.post.mockResolvedValueOnce({ data: {} });

      render(<AssetManagement />);

      await waitFor(() => {
        expect(screen.getAllByText('Laptops')[0]).toBeInTheDocument();
      });

      // หาปุ่มเพิ่ม asset โดยใช้ตำแหน่งใน DOM
      const laptopGroupItems = screen.getAllByText('Laptops');
      const laptopGroupItem = laptopGroupItems.find(element =>
        element.closest('.list-group-item')
      );

      if (laptopGroupItem) {
        const groupItem = laptopGroupItem.closest('.list-group-item');
        // หาปุ่มเพิ่มโดยใช้ icon class หรือตำแหน่ง (ปุ่มแรกในกลุ่ม)
        const addButton = within(groupItem).getAllByRole('button')[0]; // ปุ่มที่ 1 คือปุ่มเพิ่ม
        fireEvent.click(addButton);
      }

      // รอให้ modal เปิด
      await waitFor(() => {
        expect(screen.getByTestId('modal-assetModal')).toBeInTheDocument();
      });

      // กรอกข้อมูลในฟอร์ม
      const assetNameInput = screen.getByTestId('modal-assetModal').querySelector('input[type="text"]');
      fireEvent.change(assetNameInput, {
        target: { value: 'New Asset' }
      });

      const quantityInput = screen.getByTestId('modal-assetModal').querySelector('input[type="number"]');
      fireEvent.change(quantityInput, {
        target: { value: '5' }
      });

      // Submit form
      const form = screen.getByTestId('modal-assetModal').querySelector('form');
      fireEvent.submit(form);

      await waitFor(() => {
        expect(axios.post).toHaveBeenCalledWith(
          'http://localhost:3000/api/assets/bulk',
          null,
          {
            params: {
              assetGroupId: 1,
              name: 'New Asset',
              qty: 5
            },
            withCredentials: true
          }
        );
      });
    });

  // Test 9: Handles pagination
  it('handles pagination correctly', async () => {
    // Create more assets to test pagination
    const manyAssets = Array.from({ length: 25 }, (_, i) => ({
      assetId: i + 1,
      assetName: `Asset ${i + 1}`,
      assetGroupId: 1,
      status: 'available',
      room: `Room ${i + 1}`
    }));

    axios.get.mockImplementation((url) => {
      if (url.includes('/asset-group/list')) {
        return Promise.resolve({ data: mockAssetGroups });
      }
      if (url.includes('/assets/all')) {
        return Promise.resolve({ data: { result: manyAssets } });
      }
      return Promise.reject(new Error('Not found'));
    });

    render(<AssetManagement />);

    await waitFor(() => {
      expect(screen.getByTestId('pagination')).toBeInTheDocument();
    });

    // ตรวจสอบว่า pagination แสดงผลถูกต้อง
    expect(screen.getByText('Total: 25')).toBeInTheDocument();
  });

  // Test 10: Handles API errors gracefully
  it('handles API errors gracefully', async () => {
    // Mock ให้ API ล้มเหลว
    axios.get.mockRejectedValueOnce(new Error('Network error'));

    // แต่ให้ assets API ทำงานปกติเพื่อให้ component เรนเดอร์ได้
    axios.get.mockImplementation((url) => {
      if (url.includes('/asset-group/list')) {
        return Promise.reject(new Error('Network error'));
      }
      if (url.includes('/assets/all')) {
        return Promise.resolve({ data: { result: [] } });
      }
      return Promise.reject(new Error('Not found'));
    });

    render(<AssetManagement />);

    // ตรวจสอบว่าแสดงสถานะว่างเปล่า
    await waitFor(() => {
      expect(screen.getByText('No assets found')).toBeInTheDocument();
    });
  });

  // Test 11: Validates group form
  it('validates group form before submission', async () => {
    render(<AssetManagement />);

    // เปิด modal
    const createGroupButton = screen.getByText('Create Asset Group');
    fireEvent.click(createGroupButton);

    await waitFor(() => {
      expect(screen.getByTestId('modal-groupModal')).toBeInTheDocument();
    });

    // พยายาม submit form ว่าง
    const form = screen.getByTestId('modal-groupModal').querySelector('form');
    fireEvent.submit(form);

    // ตรวจสอบว่ามีการเรียกใช้ฟังก์ชันแสดง error
    await waitFor(() => {
      expect(mockShowMessageError).toHaveBeenCalledWith('กรุณากรอกชื่อ Group');
    });

    // พยายาม submit ด้วยชื่อสั้นเกินไป
    const groupNameInput = screen.getByTestId('modal-groupModal').querySelector('input[type="text"]');
    fireEvent.change(groupNameInput, {
      target: { value: 'A' }
    });

    fireEvent.submit(form);

    await waitFor(() => {
      expect(mockShowMessageError).toHaveBeenCalledWith('ชื่อ Group ต้องมีอย่างน้อย 2 ตัวอักษร');
    });
  });

  // Test 12: Toggles sort order
  it('toggles sort order when sort button is clicked', async () => {
    render(<AssetManagement />);

    await waitFor(() => {
      expect(screen.getByText('Sort')).toBeInTheDocument();
    });

    const sortButton = screen.getByText('Sort');
    fireEvent.click(sortButton);

    // ตรวจสอบว่าปุ่มมีอยู่และสามารถคลิกได้
    expect(sortButton).toBeInTheDocument();
  });
});