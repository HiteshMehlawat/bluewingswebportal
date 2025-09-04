export const API_CONFIG = {
  baseUrl: 'http://localhost:8080',
  // baseUrl: 'http://192.168.229.237:8080',
  auth: {
    login: '/api/auth/login',
    refresh: '/api/auth/refresh',
    logout: '/api/auth/logout',
  },
  admin: '/api/admin',
  adminDashboard: '/api/admin/dashboard-stats',
  clients: '/api/clients',
  staff: '/api/staff',
  staffPerformance: '/api/staff/performance',
  'staff-activities': '/api/staff-activities',
  tasks: '/api/tasks',
  documents: '/api/documents',
};
