import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { NotificationProvider } from './contexts/NotificationContext';

// Import pages
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

function App() {
  return (
    <NotificationProvider>
      <Router>
        <Routes>
          {/* ✅ Direct Routes - No authentication needed */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/roommanagement" element={<RoomManagement />} />
          <Route path="/tenantmanagement" element={<TenantManagement />} />
          <Route path="/invoicemanagement" element={<InvoiceManagement />} />
          <Route path="/maintenancerequest" element={<MaintenanceRequest />} />
          <Route path="/assetmanagement" element={<AssetManagement />} />
          <Route path="/packagemanagement" element={<PackageManagement />} />
          <Route path="/maintenanceschedule" element={<MaintenanceSchedule />} />
          
          {/* ✅ Detail Pages */}
          <Route path="/tenantdetail/:contractId" element={<TenantDetail />} />
          <Route path="/roomdetail/:roomId" element={<RoomDetail />} />
          <Route path="/invoicedetails" element={<InvoiceDetails />} />
          <Route path="/maintenancedetails" element={<MaintenanceDetails />} />
          
          {/* ✅ Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </NotificationProvider>
  );
}

export default App;
