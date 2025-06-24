// ---------------------------------------------
// File: frontend/src/api/dailyWorkReport.js
// ---------------------------------------------
import apiClient from './index'

/**
 * Search daily work reports with optional filters.
 * @param {Object} params
 * @param {string} [params.startDate] - ISO date string YYYY-MM-DD
 * @param {string} [params.endDate] - ISO date string YYYY-MM-DD
 * @param {string} [params.region] - region name
 * @param {string} [params.status] - ACTIVE, ALL, or DELETED
 * @returns {Promise<{reports: ReportResponse[], startDate: string, endDate: string, regionNames: string[]}>}
 */
export function searchReports(params = {}) {
  return apiClient.get('/api/daily-work-report/search', { params })
    .then(res => res.data)
}

/**
 * Fetch a single daily work report by ID.
 * @param {string} id - Report UUID
 * @returns {Promise<Object>} - ReportResponse object
 */
export function getReportById(id) {
  return apiClient
    .get(`/api/daily-work-report/view/${id}`)
    .then(res => res.data)
}

