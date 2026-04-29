import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const stadiumApi = {
  getAll: () => api.get('/stadiums'),
  getById: (id) => api.get(`/stadiums/${id}`),
  create: (data) => api.post('/stadiums', null, { params: data }),
  update: (id, data) => api.put(`/stadiums/${id}`, null, { params: data }),
  delete: (id) => api.delete(`/stadiums/${id}`),
  getCourts: (stadiumId) => api.get(`/stadiums/${stadiumId}/courts`),
  addCourt: (stadiumId, data) => api.post(`/stadiums/${stadiumId}/courts`, null, { params: data }),
};

export const courtApi = {
  getAll: (params) => api.get('/stadiums/courts', { params }),
  getById: (id) => api.get(`/stadiums/courts/${id}`),
  update: (id, data) => api.put(`/stadiums/courts/${id}`, null, { params: data }),
  setActive: (id, active) => api.put(`/stadiums/courts/${id}/active`, null, { params: { active } }),
};

export const matchApi = {
  getAll: (params) => api.get('/matches', { params }),
  getById: (id) => api.get(`/matches/${id}`),
  create: (data) => api.post('/matches', data.participants || [], { 
    params: { ...data, participants: undefined } 
  }),
  updateStatus: (id, status) => api.put(`/matches/${id}/status`, null, { params: { status } }),
  start: (id) => api.post(`/matches/${id}/start`),
  complete: (id, data) => api.post(`/matches/${id}/complete`, null, { params: data }),
  confirm: (id) => api.post(`/matches/${id}/confirm`),
  reassignReferee: (id) => api.post(`/matches/${id}/reassign-referee`),
  getByCourt: (courtId) => api.get(`/matches/court/${courtId}`),
  getByReferee: (refereeId) => api.get(`/matches/referee/${refereeId}`),
  getByMonth: (year, month) => api.get('/matches/monthly', { params: { year, month } }),
};

export const refereeApi = {
  getAll: (params) => api.get('/referees', { params }),
  getById: (id) => api.get(`/referees/${id}`),
  create: (data) => api.post('/referees', null, { params: data }),
  update: (id, data) => api.put(`/referees/${id}`, null, { params: data }),
  setActive: (id, active) => api.put(`/referees/${id}/active`, null, { params: { active } }),
  submitLeave: (id, data) => api.post(`/referees/${id}/leave`, null, { params: data }),
  approveLeave: (id, index) => api.post(`/referees/${id}/leave/${index}/approve`),
};

export const insuranceApi = {
  getInfo: () => api.get('/insurance/info'),
  getById: (id) => api.get(`/insurance/${id}`),
  getByMatch: (matchId) => api.get(`/insurance/match/${matchId}`),
  getByParticipant: (idCard) => api.get(`/insurance/participant/${idCard}`),
  purchase: (data) => api.post('/insurance/purchase', null, { params: data }),
  purchaseBatch: (matchId, participants) => api.post('/insurance/purchase/batch', participants, { params: { matchId } }),
  cancel: (id) => api.put(`/insurance/${id}/cancel`),
};

export const weatherApi = {
  getCurrent: (date) => api.get('/weather/current', { params: { date } }),
  getHistory: (startDate, endDate) => api.get('/weather/history', { params: { startDate, endDate } }),
  isAbnormal: (date) => api.get('/weather/abnormal', { params: { date } }),
  record: (data) => api.post('/weather/record', null, { params: data }),
};

export const cancellationApi = {
  cancel: (matchId, data) => api.post(`/cancellation/${matchId}`, null, { params: data }),
  cancelDueToWeather: (matchId, weatherDescription) => 
    api.post(`/cancellation/${matchId}/weather`, null, { params: { weatherDescription } }),
  cancelDueToTeam: (matchId, note) => 
    api.post(`/cancellation/${matchId}/team`, null, { params: { note } }),
  reschedule: (matchId, data) => 
    api.post(`/cancellation/${matchId}/reschedule`, null, { params: data }),
  getRefundPolicy: () => api.get('/cancellation/refund-policy'),
};

export const reportApi = {
  getAll: (year) => api.get('/reports', { params: { year } }),
  getMonthly: (year, month) => api.get('/reports/monthly', { params: { year, month } }),
  generate: (year, month) => api.post('/reports/generate', null, { params: { year, month } }),
};

export default api;