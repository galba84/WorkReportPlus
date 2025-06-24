// ---------------------------------------------
// File: frontend/src/api/auditLog.js
// ---------------------------------------------
import apiClient from './index'

/**
 * Fetch audit log entries from the backend.
 * @returns {Promise<AuditLogRecord[]>}
 */
export function listAuditLogs() {
  return apiClient.get('/api/audit-log')
    .then(res => res.data)
}
