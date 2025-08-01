// Frontend/src/utils/api.js
import React from 'react';

const API_BASE_URL = 'http://localhost:8080/api';

// Get token from localStorage
const getToken = () => {
  return localStorage.getItem('token');
};

// Get user from localStorage
const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

// API call helper with automatic token inclusion
export const apiCall = async (endpoint, method = 'GET', data = null, options = {}) => {
  const token = getToken();
  console.log(`API Call: ${method} ${endpoint}`);
  console.log(`Token available: ${!!token}`);
  
  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  // Add Authorization header if token exists
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log(`Authorization header added`);
  } else {
    console.warn(`No token found in localStorage`);
  }

  // Add body for POST/PUT requests
  if (data && (method === 'POST' || method === 'PUT')) {
    config.body = JSON.stringify(data);
  }

  // Add credentials for CORS
  config.credentials = 'include';

  // Determine the full URL - handle endpoints that already include the full path
  let fullUrl;
  if (endpoint.startsWith('/api/') || endpoint.startsWith('/patients') || endpoint.startsWith('/users') || endpoint.startsWith('/reports') || endpoint.startsWith('/audit-logs')) {
    // Endpoint already has the correct prefix, just add base server URL
    fullUrl = `http://localhost:8080${endpoint}`;
  } else {
    // Use API_BASE_URL for regular API endpoints
    fullUrl = `${API_BASE_URL}${endpoint}`;
  }

  try {
    console.log(`API call to: ${fullUrl}`);
    const response = await fetch(fullUrl, config);
    
    // Handle 401 (unauthorized) - token might be expired
    if (response.status === 401) {
      console.warn('Token expired or invalid, redirecting to login');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
      return null;
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Handle different response types
    if (options.responseType === 'text') {
      return await response.text();
    }
    
    return await response.json();
  } catch (error) {
    console.error('API call error:', error);
    throw error;
  }
};

// Axios-style helper for backwards compatibility
export const axiosCompatible = {
  get: (url, config = {}) => {
    const endpoint = url.replace('http://localhost:8080/api', '').replace('http://localhost:8080', '');
    return apiCall(endpoint, 'GET', null, config).then(data => ({ data }));
  },
  post: (url, data, config = {}) => {
    const endpoint = url.replace('http://localhost:8080/api', '').replace('http://localhost:8080', '');
    return apiCall(endpoint, 'POST', data, config).then(responseData => ({ data: responseData }));
  },
  put: (url, data, config = {}) => {
    const endpoint = url.replace('http://localhost:8080/api', '').replace('http://localhost:8080', '');
    return apiCall(endpoint, 'PUT', data, config).then(responseData => ({ data: responseData }));
  },
  delete: (url, config = {}) => {
    const endpoint = url.replace('http://localhost:8080/api', '').replace('http://localhost:8080', '');
    return apiCall(endpoint, 'DELETE', null, config).then(data => ({ data }));
  }
};

// Logout helper
export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/login';
};

// Check if user is authenticated
export const isAuthenticated = () => {
  const token = getToken();
  const user = getUser();
  return !!(token && user);
};

// Protected route wrapper
export const withAuth = (WrappedComponent) => {
  return function AuthenticatedComponent(props) {
    React.useEffect(() => {
      if (!isAuthenticated()) {
        window.location.href = '/login';
      }
    }, []);

    if (!isAuthenticated()) {
      return React.createElement('div', null, 'Redirecting to login...');
    }

    return React.createElement(WrappedComponent, props);
  };
};

export default apiCall;
