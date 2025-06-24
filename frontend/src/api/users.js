import apiClient from './index'

/**
 * Fetch all users from the backend.
 * @returns {Promise<UserDto[]>}
 */
export function listUsers() {
  return apiClient.get('/api/users')
    .then(response => response.data)
}

/**
 * Create or update a user on the server.
 * @param {Object} userDto  - The user data transfer object
 * @param {string} [userDto.id] - Optional user ID for updates
 * @param {string} userDto.email
 * @param {string} userDto.nickname
 * @param {string|null} [userDto.password] - Pass `null` to leave unchanged on update
 * @param {string} userDto.role - One of ADMIN, POWER_USER, USER, GUEST
 * @returns {Promise<UserDto>}
 */
export function upsertUser(userDto) {
  return apiClient.post('/api/users', userDto)
    .then(response => response.data)
}
