import axios from 'axios';

/**
 * API Configuration with Environment Support
 * Automatically switches between local development and production deployment
 * 
 * Security Features:
 * - JWT Bearer token management
 * - Automatic token refresh on expiration
 * - CORS support for cross-origin requests
 * - Request/Response interceptors for standardization
 */

// Determine API base URL based on environment
const getApiBaseUrl = () => {
  const env = process.env.NODE_ENV;
  const customApiUrl = process.env.REACT_APP_API_URL;
  
  if (customApiUrl) {
    return customApiUrl;
  }
  
  if (env === 'production') {
    // For production, use Render backend URL
    return process.env.REACT_APP_API_URL || 'https://ecommerce-backend-kf68.onrender.com';
  }
  
  // Development environment
  return 'http://localhost:8080';
};

const API_BASE_URL = getApiBaseUrl();

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * Request Interceptor
 * Adds authentication token to all requests
 */
api.interceptors.request.use(
  (config) => {
    // Get access token from localStorage
    const accessToken = localStorage.getItem('accessToken') || localStorage.getItem('jwtToken');
    
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * Handles token refresh on 401 Unauthorized responses
 */
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // If error is 401 and we haven't retried yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        
        if (!refreshToken) {
          // No refresh token, redirect to login
          logoutUser();
          window.location.href = '/login';
          return Promise.reject(error);
        }
        
        // Request new access token
        const response = await axios.post(`${API_BASE_URL}/ecom/auth/refresh`, {
          refreshToken: refreshToken
        });
        
        // Store new tokens
        localStorage.setItem('accessToken', response.data.accessToken);
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken);
        }
        
        // Update authorization header with new token
        originalRequest.headers.Authorization = `Bearer ${response.data.accessToken}`;
        
        // Retry original request
        return api(originalRequest);
        
      } catch (refreshError) {
        // Refresh failed, logout user
        logoutUser();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

/**
 * Authentication Functions
 */

/**
 * Login user and store tokens
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise} Authentication response with tokens
 */
export const loginUser = async (email, password) => {
  try {
    const response = await api.post('/ecom/auth/login', {
      email,
      password
    });
    
    // Store tokens
    localStorage.setItem('accessToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);
    localStorage.setItem('userId', response.data.userId);
    localStorage.setItem('userRole', response.data.role);
    localStorage.setItem('tokenExpiry', Date.now() + response.data.expiresIn);
    
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

/**
 * Register new user
 * @param {object} userData - User registration data
 * @returns {Promise} Registration response
 */
export const registerUser = async (userData) => {
  try {
    const response = await api.post('/ecom/customers', userData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

/**
 * Refresh access token using refresh token
 * @returns {Promise} New access token
 */
export const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  if (!refreshToken) {
    throw new Error('No refresh token found');
  }
  
  try {
    const response = await axios.post(`${API_BASE_URL}/ecom/auth/refresh`, {
      refreshToken: refreshToken
    });
    
    localStorage.setItem('accessToken', response.data.accessToken);
    if (response.data.refreshToken) {
      localStorage.setItem('refreshToken', response.data.refreshToken);
    }
    
    return response.data;
  } catch (error) {
    logoutUser();
    throw error;
  }
};

/**
 * Get current logged-in user info
 * @returns {Promise} Current user data
 */
export const getCurrentUser = async () => {
  try {
    const response = await api.get('/ecom/auth/me');
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

/**
 * Logout user and clear stored data
 */
export const logoutUser = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('userRole');
  localStorage.removeItem('tokenExpiry');
  localStorage.removeItem('jwtToken'); // Legacy
};

/**
 * Check if user is authenticated
 * @returns {boolean} True if user has valid token
 */
export const isAuthenticated = () => {
  const token = localStorage.getItem('accessToken') || localStorage.getItem('jwtToken');
  const expiry = localStorage.getItem('tokenExpiry');
  
  if (!token) return false;
  
  // Check if token is expired
  if (expiry && Date.now() > parseInt(expiry)) {
    logoutUser();
    return false;
  }
  
  return true;
};

/**
 * Get authorization headers for manual requests
 * @returns {object} Headers object with Bearer token
 */
export const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken') || localStorage.getItem('jwtToken');
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
};

/**
 * Get user role for permission checking
 * @returns {string} User role (ROLE_ADMIN, ROLE_USER, etc)
 */
export const getUserRole = () => {
  return localStorage.getItem('userRole');
};

/**
 * Check if user is admin
 * @returns {boolean} True if user is admin
 */
export const isAdmin = () => {
  return getUserRole() === 'ROLE_ADMIN';
};

export default api;
