package de.eddi.chat

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login   : NavKey
@Serializable data object Register : NavKey
@Serializable data object Contacts : NavKey

@Serializable
data class Chat(
    val receiverId: Int,
    val receiverName: String
) : NavKey

@Serializable
data class AddContact(val myId: Int) : NavKey

@Serializable data object Settings : NavKey
