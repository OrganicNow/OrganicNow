import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { NotificationProvider } from './contexts/NotificationContext';
import { AuthProvider, useAuth } from './contexts/AuthContext';

// Import pages
import Login from './pages/Login';
import Dashboard from './pages/dashboard';
import RoomManagement from './pages/roommanagement';
import TenantManagement from './pages/tenantmanagement';
import InvoiceManagement from './pages/Invoicemanagement';
import MaintenanceRequest from './pages/maintenancerequest';
import AssetManagement from './pages/AssetManagement';
import PackageManagement from './pages/PackageManagement';
import MaintenanceSchedule from './pages/MaintenanceSchedule';

// Import detail pages
import TenantDetail from './pages/tenantdetail';
import RoomDetail from './pages/roomdetail';
import InvoiceDetails from './pages/Invoicedetails';
import MaintenanceDetails from './pages/MaintenanceDetails';

// Simple Protected Route Component
function ProtectedRoute({ children, requiredPermission = 'admin' }) {
  const { isAuthenticated, hasPermission } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (!hasPermission(requiredPermission)) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h2>ไม่มีสิทธิ์เข้าถึง</h2>
      <p>คุณไม่มีสิทธิ์ในการเข้าถึงหน้านี้</p>
    </div>;
  }
  
  return children;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Login Route */}
      <Route path="/login" element={<Login />} />
      
      {/* Protected Routes */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      } />
      
      <Route path="/roommanagement" element={
        <ProtectedRoute>
          <RoomManagement />
        </ProtectedRoute>
      } />
      
      <Route path="/tenantmanagement" element={
        <ProtectedRoute>
          <TenantManagement />
        </ProtectedRoute>
      } />
      
      <Route path="/invoicemanagement" element={
        <ProtectedRoute>
          <InvoiceManagement />
        </ProtectedRoute>
      } />
      
      <Route path="/maintenancerequest" element={
        <ProtectedRoute>
          <MaintenanceRequest />
        </ProtectedRoute>
      } />
      
      <Route path="/assetmanagement" element={
        <ProtectedRoute>
          <AssetManagement />
        </ProtectedRoute>
      } />
      
      {/* Super Admin Only Route */}
      <Route path="/packagemanagement" element={
        <ProtectedRoute requiredPermission="super_admin">
          <PackageManagement />
        </ProtectedRoute>
      } />
      
      <Route path="/maintenanceschedule" element={
        <ProtectedRoute>
          <MaintenanceSchedule />
        </ProtectedRoute>
      } />
      
      {/* Detail Pages */}
      <Route path="/tenantdetail/:contractId" element={
        <ProtectedRoute>
          <TenantDetail />
        </ProtectedRoute>
      } />
      
      <Route path="/roomdetail/:roomId" element={
        <ProtectedRoute>
          <RoomDetail />
        </ProtectedRoute>
      } />
      
      <Route path="/invoicedetails" element={
        <ProtectedRoute>
          <InvoiceDetails />
        </ProtectedRoute>
      } />
      
      <Route path="/maintenancedetails" element={
        <ProtectedRoute>
          <MaintenanceDetails />
        </ProtectedRoute>
      } />
      
      {/* Fallback */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <Router>
          <AppRoutes />
        </Router>
      </NotificationProvider>
    </AuthProvider>
  );
}

export default App;
