package ro.pub.cs.systems.eim.practicaltest02v7.network

import android.util.Log
import ro.pub.cs.systems.eim.practicaltest02v7.general.Constants
import java.io.IOException
import java.net.ServerSocket

class ServerThread(private var port: String) : Thread() {
    @Volatile
    private var isRunning = false

    private var serverSocket: ServerSocket? = null
    private val communicationThreads = mutableListOf<CommunicationThread>()

    fun startServer() {
        if (isRunning) {
            Log.w(Constants.TAG, "Server is already running!")
            return
        }

        isRunning = true
        start()
        Log.v(Constants.TAG, "startServer() method was invoked on port $port")
    }

    fun stopServer() {
        isRunning = false

        // Oprește toate thread-urile de comunicare
        synchronized(communicationThreads) {
            communicationThreads.forEach { thread ->
                try {
                    thread.stopCommunication() // Folosește noua metodă
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Error stopping communication thread: ${e.message}")
                }
            }
            communicationThreads.clear()
        }

        try {
            serverSocket?.close()
            Log.v(Constants.TAG, "Server socket closed")
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        }

        Log.v(Constants.TAG, "stopServer() method was invoked")
    }

    override fun run() {
        try {
            val portNumber = port.toIntOrNull()

            if (portNumber == null || portNumber !in 1024..65535) {
                Log.e(Constants.TAG, "Invalid port number: $port")
                return
            }

            serverSocket = ServerSocket(portNumber)
            Log.v(Constants.TAG, "Server started on port $portNumber")
            Log.d(Constants.TAG, "Waiting for clients...")

            while (isRunning) {
                try {
                    val socket = serverSocket?.accept()

                    if (socket != null && isRunning) {
                        Log.d(Constants.TAG, "Client connected: ${socket.inetAddress.hostAddress}")

                        val communicationThread = CommunicationThread(socket)

                        // fara asta daca vreau sa inchid direct dupa raspuns
                        synchronized(communicationThreads) {
                            communicationThreads.add(communicationThread)
                        }

                        communicationThread.start()
                        cleanupFinishedThreads()
                    }
                } catch (e: IOException) {
                    if (isRunning) {
                        Log.e(Constants.TAG, "Error accepting connection: ${e.message}")
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "Server error: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        } finally {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(Constants.TAG, "Error closing server: ${e.message}")
            }
            Log.v(Constants.TAG, "Server thread stopped")
        }
    }

    private fun cleanupFinishedThreads() {
        synchronized(communicationThreads) {
            communicationThreads.removeAll { !it.isAlive }
        }
    }

    fun getActiveConnectionsCount(): Int {
        cleanupFinishedThreads()
        return synchronized(communicationThreads) {
            communicationThreads.size
        }
    }
}