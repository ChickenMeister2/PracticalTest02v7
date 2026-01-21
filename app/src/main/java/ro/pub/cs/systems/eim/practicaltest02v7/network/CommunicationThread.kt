package ro.pub.cs.systems.eim.practicaltest02v7.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ro.pub.cs.systems.eim.practicaltest02v7.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v7.general.Utilities
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

class CommunicationThread(private val socket: Socket) : Thread() {

    @Volatile
    private var isRunning = true
    private val hashmap: HashMap<String, Pair<String, String>> = HashMap()
    var result : String = ""
    override fun run() {
        var bufferedReader: BufferedReader? = null
        var printWriter: PrintWriter? = null

        try {
            Log.v(
                Constants.TAG,
                "Connection opened with ${socket.inetAddress}:${socket.port}"
            )
//            val actiondata = timer_action.substring(0, timer_action.length - 2).split(",".toRegex())
//                .dropLastWhile { it.isEmpty() }.toTypedArray()

            bufferedReader = Utilities.getReader(socket)
            printWriter = Utilities.getWriter(socket)

            try {
                // Citește ce a scris clientul
                val address = bufferedReader.readLine()
//                val actionData =
                val commands = address.split(",").toTypedArray()
                // Dacă clientul s-a deconectat
                if (address == null) {
                    Log.d(Constants.TAG, "Client disconnected")
                } else {
                    Log.d(Constants.TAG, "Received request for: $address")
                }
                if(commands[0] == "set") {
                    Log.d(Constants.TAG, "Am adaugat pentru" + socket.port.toString())
                    hashmap[socket.port.toString()] = Pair(commands[1], commands[2])
                    result = "Added"
                }
                // Fetch content cu coroutines
                else if(commands[0] == "reset") {
                    Log.d(Constants.TAG, "Am sters pentru" + socket.port.toString())
                    hashmap.remove(socket.port.toString())
                    result = "Am sters cu succes"
                }
                else if(commands[0] == "poll") {
                    result = runBlocking {
                        getTime()
                    }
                }

                // Trimite rezultatul înapoi
                if (result?.startsWith("ERROR") == true) {
                    Log.e(Constants.TAG, "Error fetching content: $result")
                } else {
                    Log.d(Constants.TAG, "Sending response to client (${result?.length ?: 0} chars)")
                }

                printWriter.println(result ?: "ERROR - No response")
                printWriter.flush()

            } catch (e: IOException) {
                if (isRunning) {
                    Log.e(Constants.TAG, "Error in communication loop: ${e.message}")
                }
            }

        } catch (exception: Exception) {
            Log.e(Constants.TAG, "Communication thread error: ${exception.message}")
            if (Constants.DEBUG) {
                exception.printStackTrace()
            }
        } finally {
            // Cleanup
            try {
                bufferedReader?.close()
                printWriter?.close()
                socket.close()
                Log.v(Constants.TAG, "Connection closed with ${socket.inetAddress}")
            } catch (e: IOException) {
                Log.e(Constants.TAG, "Error closing connection: ${e.message}")
            }
        }
    }

    // Metodă pentru a opri thread-ul
    fun stopCommunication() {
        isRunning = false
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e(Constants.TAG, "Error stopping communication: ${e.message}")
        }
    }

    private fun getTime(): String {
        var utcTime: String = ""
        try {
            Log.i(Constants.TAG, "Am facut pooling")
            val socket = Socket("time.nist.gov", 13)
            val bufferedReader = Utilities.getReader(socket)
            bufferedReader.readLine()
            utcTime = bufferedReader.readLine()
            Log.i(Constants.TAG, "utcTime: " + utcTime)
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, ioException.message!!)
            ioException.printStackTrace()
        }

        return utcTime
    }
}