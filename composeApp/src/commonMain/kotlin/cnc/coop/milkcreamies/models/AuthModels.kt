/*
 * Copyright 2025  MkaoCodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cnc.coop.milkcreamies.models

import kotlinx.serialization.Serializable
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import cnc.coop.milkcreamies.core.constants.AppConstants

// Request models for authentication
@Serializable
data class UserRegistration(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class UserLogin(
    val username: String,
    val password: String
)

// Response models
@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: String,
    val lastLoginAt: String?
)

@Serializable
data class LoginResponse(
    val message: String,
    val user: UserResponse,
    val sessionId: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String? = null
)

@Serializable
enum class UserRole {
    USER
}

@Serializable
data class UserDataEntry(
    val id: Int,
    val userId: Int,
    val dataKey: String,
    val dataValue: String,
    val createdAt: String,
    val updatedAt: String?
)

@Serializable
data class UserDataCreate(
    val dataKey: String,
    val dataValue: String
)

@Serializable
data class UserDataUpdate(
    val dataValue: String
)

// Client configuration
@Serializable
data class ApiConfig(
    val baseUrl: String = AppConstants.Api.BASE_URL,
    val apiKey: String = "dairy-app-secret-key-12345"
)

// Session management
@Serializable
data class UserSession(
    val user: UserResponse,
    val sessionId: String,
    val isLoggedIn: Boolean = true
)

// API Client class for handling authentication and requests
class MilkCooperativeApiClient(private val config: ApiConfig = ApiConfig()) {
    private var currentSession: UserSession? = null

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }
    
    // Authentication methods
    suspend fun signup(request: UserRegistration): Result<UserResponse> {
        return try {
            val response = httpClient.post("${config.baseUrl}/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
                header("X-API-Key", config.apiKey)
            }
            
            if (response.status.isSuccess()) {
                try {
                    val userResponse = response.body<UserResponse>()
                    Result.success(userResponse)
                } catch (serializationError: Exception) {
                    // Handle serialization errors specifically
                    val responseText = response.body<String>()
                    Result.failure(Exception("Signup response parsing failed: ${serializationError.message}. Response: $responseText"))
                }
            } else {
                try {
                    val error = response.body<ErrorResponse>()
                    Result.failure(Exception("Signup failed: ${error.error} - ${error.message}"))
                } catch (serializationError: Exception) {
                    // If we can't parse the error response, use the raw response
                    val responseText = response.body<String>()
                    Result.failure(Exception("Signup failed with status ${response.status.value}: $responseText"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Signup request failed: ${e.message}", e))
        }
    }
    
    suspend fun login(request: UserLogin): Result<UserSession> {
        return try {
            val response = httpClient.post("${config.baseUrl}/auth/signin") {
                contentType(ContentType.Application.Json)
                setBody(request)
                header("X-API-Key", config.apiKey)
            }
            
            if (response.status.isSuccess()) {
                try {
                    val loginResponse = response.body<LoginResponse>()
                    val session = UserSession(
                        user = loginResponse.user,
                        sessionId = loginResponse.sessionId
                    )
                    currentSession = session
                    Result.success(session)
                } catch (serializationError: Exception) {
                    // Handle serialization errors specifically
                    val responseText = response.body<String>()
                    Result.failure(Exception("Login response parsing failed: ${serializationError.message}. Response: $responseText"))
                }
            } else {
                try {
                    val error = response.body<ErrorResponse>()
                    Result.failure(Exception("Login failed: ${error.error} - ${error.message}"))
                } catch (serializationError: Exception) {
                    // If we can't parse the error response, use the raw response
                    val responseText = response.body<String>()
                    Result.failure(Exception("Login failed with status ${response.status.value}: $responseText"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login request failed: ${e.message}", e))
        }
    }
    
    fun logout() {
        currentSession = null
    }
    
    fun isLoggedIn(): Boolean = currentSession?.isLoggedIn == true
    
    fun getCurrentUser(): UserResponse? = currentSession?.user
    
    // User data methods
    suspend fun getUserData(userId: Int): Result<List<UserDataEntry>> {
        return try {
            if (!isLoggedIn()) {
                return Result.failure(Exception("Authentication required"))
            }
            
            val currentUserId = currentSession?.user?.id
            if (currentUserId != userId) {
                return Result.failure(Exception("Access denied: Can only access your own data"))
            }
            
            val response = httpClient.get("${config.baseUrl}/user-data/$userId") {
                header("X-API-Key", config.apiKey)
                header("Authorization", "Bearer ${currentSession?.sessionId}")
            }
            
            if (response.status.isSuccess()) {
                val userData = response.body<List<UserDataEntry>>()
                Result.success(userData)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception("Failed to get user data: ${error.error} - ${error.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUserData(userId: Int, dataKey: String, dataValue: String): Result<UserDataEntry> {
        return try {
            if (!isLoggedIn()) {
                return Result.failure(Exception("Authentication required"))
            }
            
            val currentUserId = currentSession?.user?.id
            if (currentUserId != userId) {
                return Result.failure(Exception("Access denied: Can only manage your own data"))
            }
            
            val request = UserDataCreate(dataKey, dataValue)
            val response = httpClient.post("${config.baseUrl}/user-data/$userId") {
                contentType(ContentType.Application.Json)
                setBody(request)
                header("X-API-Key", config.apiKey)
                header("Authorization", "Bearer ${currentSession?.sessionId}")
            }
            
            if (response.status.isSuccess()) {
                val userData = response.body<UserDataEntry>()
                Result.success(userData)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception("Failed to create user data: ${error.error} - ${error.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserData(userId: Int, dataId: Int, dataValue: String): Result<UserDataEntry> {
        return try {
            if (!isLoggedIn()) {
                return Result.failure(Exception("Authentication required"))
            }
            
            val currentUserId = currentSession?.user?.id
            if (currentUserId != userId) {
                return Result.failure(Exception("Access denied: Can only manage your own data"))
            }
            
            val request = UserDataUpdate(dataValue)
            val response = httpClient.put("${config.baseUrl}/user-data/$userId/$dataId") {
                contentType(ContentType.Application.Json)
                setBody(request)
                header("X-API-Key", config.apiKey)
                header("Authorization", "Bearer ${currentSession?.sessionId}")
            }
            
            if (response.status.isSuccess()) {
                val userData = response.body<UserDataEntry>()
                Result.success(userData)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception("Failed to update user data: ${error.error} - ${error.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUserData(userId: Int, dataId: Int): Result<String> {
        return try {
            if (!isLoggedIn()) {
                return Result.failure(Exception("Authentication required"))
            }
            
            val currentUserId = currentSession?.user?.id
            if (currentUserId != userId) {
                return Result.failure(Exception("Access denied: Can only manage your own data"))
            }
            
            val response = httpClient.delete("${config.baseUrl}/user-data/$userId/$dataId") {
                header("X-API-Key", config.apiKey)
                header("Authorization", "Bearer ${currentSession?.sessionId}")
            }
            
            if (response.status.isSuccess()) {
                Result.success("User data deleted successfully")
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception("Failed to delete user data: ${error.error} - ${error.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
