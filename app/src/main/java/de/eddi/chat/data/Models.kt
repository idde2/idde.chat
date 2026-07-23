package de.eddi.chat.data

// ── API-Basis-URL ─────────────────────────────────────────────────────────
// Für den Android-Emulator: 10.0.2.2 zeigt auf localhost des Host-PCs
// Für echten Server: domain + /chat/api/
const val BASE_URL = "https://eddi.cowdie.com/chat/api/"

// ── Datenmodelle (Request / Response) ────────────────────────────────────

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val email: String)

data class AuthUser(val id: Int, val username: String)
data class AuthResponse(val code: Int, val token: String?, val user: AuthUser?, val error: String?)

data class Contact(val id: Int, val username: String)
data class ContactsResponse(val code: Int, val contacts: List<Contact>)

data class UserItem(val id: Int, val username: String)
data class UsersResponse(val code: Int, val users: List<UserItem>)

data class Message(
    val sender_id: Int,
    val receiver_id: Int,
    val content: String,
    val time: String,
    val is_mine: Boolean
)
data class MessagesResponse(val code: Int, val messages: List<Message>)

data class SendMessageRequest(val content: String)
data class GenericResponse(val code: Int, val message: String?, val error: String?)

data class MeUser(val id: Int, val username: String, val email: String)
data class MeResponse(val code: Int, val user: MeUser?)
