// Login.test.jsx
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import Login from '../Login';
import { useAuth } from '../../contexts/AuthContext';

// Mock the AuthContext
vi.mock('../../contexts/AuthContext');

// Mock Dashboard component for routing test
const MockDashboard = () => <div>Dashboard Page</div>;

const renderWithRouter = (component) => {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={component} />
        <Route path="/dashboard" element={<MockDashboard />} />
      </Routes>
    </MemoryRouter>
  );
};

describe('Login Component', () => {
  const mockLogin = vi.fn();
  let mockUseAuth;

  beforeEach(() => {
    vi.clearAllMocks();

    // Mock implementation
    mockUseAuth = vi.mocked(useAuth);

    // Default mock implementation
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      isLoading: false,
      logout: vi.fn(),
      user: null
    });
  });

  it('renders login form correctly', () => {
    renderWithRouter(<Login />);

    expect(screen.getByText('OrganicNow Login')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'เข้าสู่ระบบ' })).toBeInTheDocument();
    expect(screen.getByText('แสดง')).toBeInTheDocument();
  });

  it('shows loading spinner when auth is loading', () => {
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      isLoading: true,
      logout: vi.fn(),
      user: null
    });

    renderWithRouter(<Login />);

    expect(screen.getByText('กำลังตรวจสอบสิทธิ์...')).toBeInTheDocument();
  });

  it('redirects to dashboard when user is authenticated', () => {
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      isAuthenticated: true,
      isLoading: false,
      logout: vi.fn(),
      user: null
    });

    renderWithRouter(<Login />);

    // Should redirect to dashboard
    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
  });

  it('handles form submission successfully', async () => {
    mockLogin.mockResolvedValue({ success: true });

    renderWithRouter(<Login />);

    fireEvent.change(screen.getByPlaceholderText('Username'), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByPlaceholderText('Password'), {
      target: { value: 'password123' }
    });

    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('testuser', 'password123');
    });
  });

  it('shows error message when login fails', async () => {
    const errorMessage = 'Invalid credentials';
    mockLogin.mockResolvedValue({
      success: false,
      message: errorMessage
    });

    renderWithRouter(<Login />);

    fireEvent.change(screen.getByPlaceholderText('Username'), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByPlaceholderText('Password'), {
      target: { value: 'wrongpassword' }
    });

    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }));

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('toggles password visibility', () => {
    renderWithRouter(<Login />);

    const passwordInput = screen.getByPlaceholderText('Password');
    const toggleButton = screen.getByText('แสดง');

    // Initially should be password type
    expect(passwordInput.type).toBe('password');

    // Click to show password
    fireEvent.click(toggleButton);
    expect(passwordInput.type).toBe('text');
    expect(screen.getByText('ซ่อน')).toBeInTheDocument();

    // Click to hide password again
    fireEvent.click(screen.getByText('ซ่อน'));
    expect(passwordInput.type).toBe('password');
    expect(screen.getByText('แสดง')).toBeInTheDocument();
  });

  it('disables submit button and shows loading text during login', async () => {
    // Create a promise that doesn't resolve immediately
    let resolveLogin;
    const loginPromise = new Promise(resolve => {
      resolveLogin = resolve;
    });
    mockLogin.mockReturnValue(loginPromise);

    renderWithRouter(<Login />);

    fireEvent.change(screen.getByPlaceholderText('Username'), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByPlaceholderText('Password'), {
      target: { value: 'password123' }
    });

    const submitButton = screen.getByRole('button', { name: 'เข้าสู่ระบบ' });
    fireEvent.click(submitButton);

    // Button should be disabled and show loading text
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent('กำลังเข้าสู่ระบบ...');

    // Resolve the login promise
    resolveLogin({ success: true });

    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
      expect(submitButton).toHaveTextContent('เข้าสู่ระบบ');
    });
  });

  it('shows default error message when no specific message is provided', async () => {
    mockLogin.mockResolvedValue({ success: false });

    renderWithRouter(<Login />);

    fireEvent.change(screen.getByPlaceholderText('Username'), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByPlaceholderText('Password'), {
      target: { value: 'password123' }
    });

    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }));

    await waitFor(() => {
      expect(screen.getByText('เข้าสู่ระบบไม่สำเร็จ')).toBeInTheDocument();
    });
  });

  it('validates required fields', async () => {
    renderWithRouter(<Login />);

    const submitButton = screen.getByRole('button', { name: 'เข้าสู่ระบบ' });
    fireEvent.click(submitButton);

    // The form should prevent submission and show browser validation
    // We can check that login was not called
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('clears error message on new form submission', async () => {
    // First, create a failing login
    mockLogin.mockResolvedValueOnce({
      success: false,
      message: 'First error'
    });

    renderWithRouter(<Login />);

    // Fill and submit form to trigger error
    fireEvent.change(screen.getByPlaceholderText('Username'), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByPlaceholderText('Password'), {
      target: { value: 'wrongpassword' }
    });

    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }));

    await waitFor(() => {
      expect(screen.getByText('First error')).toBeInTheDocument();
    });

    // Now mock a successful login for the next attempt
    mockLogin.mockResolvedValueOnce({ success: true });

    // Submit form again
    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }));

    // Error message should be cleared
    await waitFor(() => {
      expect(screen.queryByText('First error')).not.toBeInTheDocument();
    });
  });



  it('updates input values correctly', () => {
    renderWithRouter(<Login />);

    const usernameInput = screen.getByPlaceholderText('Username');
    const passwordInput = screen.getByPlaceholderText('Password');

    fireEvent.change(usernameInput, { target: { value: 'newuser' } });
    fireEvent.change(passwordInput, { target: { value: 'newpassword' } });

    expect(usernameInput.value).toBe('newuser');
    expect(passwordInput.value).toBe('newpassword');
  });
});