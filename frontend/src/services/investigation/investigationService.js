import apiClient from '../api/apiClient';

const BASE = '/investigations';

const buildParams = ({ status, page, size, sort } = {}) => {
  const params = {};
  if (status) params.status = status;
  if (page != null) params.page = page;
  if (size != null) params.size = size;
  if (sort) params.sort = sort;
  return params;
};

export const investigationService = {
  list: async (opts = {}) => {
    const res = await apiClient.get(BASE, { params: buildParams(opts) });
    return res.data;
  },

  getById: async (id) => {
    const res = await apiClient.get(`${BASE}/${id}`);
    return res.data;
  },

  create: async ({ title, description, category }) => {
    const res = await apiClient.post(BASE, { title, description, category });
    return res.data;
  },

  update: async (id, payload) => {
    const res = await apiClient.patch(`${BASE}/${id}`, payload);
    return res.data;
  },

  remove: async (id) => {
    await apiClient.delete(`${BASE}/${id}`);
  },

  getGraph: async (id) => {
    const res = await apiClient.get(`${BASE}/${id}/graph`);
    return res.data;
  },

  getCampaign: async (id) => {
    const res = await apiClient.get(`${BASE}/${id}/campaign`);
    return res.data;
  },

  generateGraph: async (id) => {
    const res = await apiClient.post(`${BASE}/${id}/graph/generate`);
    return res.data;
  },

  getRiskAssessment: async (id) => {
    const res = await apiClient.get(`${BASE}/${id}/risk-assessment`);
    return res.data;
  },

  assessRisk: async (id) => {
    const res = await apiClient.post(`${BASE}/${id}/risk-assessment`);
    return res.data;
  },

  createReport: async (id) => {
    const res = await apiClient.post(`${BASE}/${id}/reports`);
    return res.data;
  },

  listReports: async (id) => {
    const res = await apiClient.get(`${BASE}/${id}/reports`);
    return res.data;
  },

  getReport: async (reportId) => {
    const res = await apiClient.get(`/reports/${reportId}`);
    return res.data;
  },

  seedDemoCampaign: async () => {
    const res = await apiClient.post('/demo/seed');
    return res.data;
  },

  resetDemoCampaign: async () => {
    const res = await apiClient.delete('/demo/reset');
    return res.data;
  },

  getDemoStatus: async () => {
    const res = await apiClient.get('/demo/status');
    return res.data;
  },
};

export default investigationService;
