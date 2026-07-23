package de.eddi.chat.data

import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    // ── Profil ────────────────────────────────────────────────────────────
    @GET("me")
    suspend fun getMe(@Header("Authorization") bearer: String): Response<MeResponse>

    // ── Kontakte ──────────────────────────────────────────────────────────
    @GET("contacts")
    suspend fun getContacts(@Header("Authorization") bearer: String): Response<ContactsResponse>

    @POST("contacts/{userId}")
    suspend fun addContact(
        @Header("Authorization") bearer: String,
        @Path("userId") userId: Int
    ): Response<GenericResponse>

    // ── User-Suche ────────────────────────────────────────────────────────
    @GET("users")
    suspend fun getUsers(
        @Header("Authorization") bearer: String,
        @Query("q") query: String = ""
    ): Response<UsersResponse>

    // ── Nachrichten ───────────────────────────────────────────────────────
    @GET("messages/{receiverId}")
    suspend fun getMessages(
        @Header("Authorization") bearer: String,
        @Path("receiverId") receiverId: Int
    ): Response<MessagesResponse>

    @POST("messages/{receiverId}")
    suspend fun sendMessage(
        @Header("Authorization") bearer: String,
        @Path("receiverId") receiverId: Int,
        @Body req: SendMessageRequest
    ): Response<GenericResponse>
}
