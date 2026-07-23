package de.eddi.chat.data

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    private const val TAG = "SocketManager"
    private const val SOCKET_URL = "https://eddi.cowdie.com"
    // Socket.IO Pfad: Muss den vollstaendigen Pfad inkl. /socket.io enthalten
    private const val SOCKET_PATH = "/chat/socket.io/"

    private var socket: Socket? = null
    private var messageCallback: ((String, String, Int) -> Unit)? = null

    fun connect(token: String, myId: Int, receiverId: Int) {
        Log.d(TAG, "Connecting socket to $SOCKET_URL with path $SOCKET_PATH (Target: $receiverId)")
        
        if (socket?.connected() == true) {
            emitJoin(token, receiverId)
            return
        }

        try {
            val opts = IO.Options().apply {
                path = SOCKET_PATH
                reconnection = true
                forceNew = true
                // Wir lassen die default transports (polling, websocket)
            }
            socket = IO.socket(SOCKET_URL, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected successfully! SID: ${socket?.id()}")
                emitJoin(token, receiverId)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.w(TAG, "Socket disconnected! Reason: ${args.getOrNull(0)}")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val err = args.getOrNull(0)
                Log.e(TAG, "Connect error: $err")
                if (err is Exception) err.printStackTrace()
            }

            socket?.on("msg") { args ->
                Log.d(TAG, "New message via socket: ${args.getOrNull(0)}")
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject ?: return@on
                    val content  = data.optString("content", "")
                    val sender   = data.optString("sender", "")
                    val senderId = data.optInt("sender_id", -1)
                    messageCallback?.invoke(content, sender, senderId)
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting socket", e)
        }
    }

    private fun emitJoin(token: String, receiverId: Int) {
        val joinData = JSONObject().apply {
            put("receiver", receiverId)
            put("token", token)
        }
        Log.d(TAG, "Emitting join for receiverId: $receiverId")
        socket?.emit("join", joinData)
    }

    fun setMessageCallback(cb: (content: String, sender: String, senderId: Int) -> Unit) {
        messageCallback = cb
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting socket")
        socket?.disconnect()
        socket?.off()
        socket = null
        messageCallback = null
    }
}
