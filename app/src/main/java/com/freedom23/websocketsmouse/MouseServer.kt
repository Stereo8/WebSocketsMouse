package com.freedom23.websocketsmouse

import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import okhttp3.WebSocket

interface MouseServer {

    @Send
    fun sendGyro(gyroData: String)

    @Send
    fun recenter(message: String = "center")

    @Send
    fun levi(message: String = "levi")

    @Send
    fun desni(message: String = "desni")

    @Send
    fun close(message: String = "close")
}