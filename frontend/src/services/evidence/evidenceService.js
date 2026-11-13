import apiClient from '../api/apiClient';

const investigationPath = (investigationId) => `/investigations/${investigationId}/evidence`;

export const evidenceService = {
  list: async (investigationId) => {
    const response = await apiClient.get(investigationPath(investigationId));
    return response.data;
  },

  get: async (evidenceId) => {
    const response = await apiClient.get(`/evidence/${evidenceId}`);
    return response.data;
  },

  create: async (investigationId, payload) => {
    const response = await apiClient.post(investigationPath(investigationId), payload);
    return response.data;
  },

  createFile: async (investigationId, metadata, file, onUploadProgress) => {
    const formData = new FormData();
    formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    formData.append('file', file);
    const response = await apiClient.post(investigationPath(investigationId), formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    });
    return response.data;
  },

  remove: async (evidenceId) => {
    await apiClient.delete(`/evidence/${evidenceId}`);
  },

  listThreatLookups: async (evidenceId) => {
    const response = await apiClient.get(`/evidence/${evidenceId}/threat-lookups`);
    return response.data;
  },

  lookupThreats: async (evidenceId) => {
    const response = await apiClient.post(`/evidence/${evidenceId}/threat-lookups`);
    return response.data;
  },

  listIndicators: async (evidenceId) => {
    const response = await apiClient.get(`/evidence/${evidenceId}/indicators`);
    return response.data;
  },

  listAnalysis: async (evidenceId) => {
    const response = await apiClient.get(`/evidence/${evidenceId}/analysis`);
    return response.data;
  },

  analyze: async (evidenceId) => {
    const response = await apiClient.post(`/evidence/${evidenceId}/analysis`);
    return response.data;
  },
};

export default evidenceService;
