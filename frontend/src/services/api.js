import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for logging
api.interceptors.request.use(
  (config) => {
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export const designApi = {
  /**
   * Generate a new system design
   */
  generate: async (request) => {
    const response = await api.post('/generate', request);
    return response.data;
  },

  /**
   * Generate a preview (no persist)
   */
  generatePreview: async (request) => {
    const response = await api.post('/generate-preview', request);
    return response.data;
  },

  /**
   * Get a design by ID
   */
  getById: async (id) => {
    const response = await api.get(`/designs/${id}`);
    return response.data;
  },

  /**
   * List designs with pagination
   */
  list: async (params = {}) => {
    const response = await api.get('/designs', { params });
    return response.data;
  },
};

export default api;
