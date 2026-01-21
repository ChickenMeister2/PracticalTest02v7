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
                if(commands[0] == "set")
                    hashmap[socket.inetAddress.toString()] = Pair(commands[1], commands[2])
                // Fetch content cu coroutines
                else if(commands[0] == "reset")
                    hashmap.remove(socket.inetAddress.toString())
                else if(commands[])
                val result = runBlocking {
                    getTime()
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

//    private suspend fun fetchWebContent(address: String): String = withContext(Dispatchers.IO) {
//        // Verifică cache-ul
//        if (hashmap.containsKey(address)) {
//            Log.d(Constants.TAG, "Returning cached content for: $address")
//            return@withContext hashmap[address]!!
//        }
//
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url(address)
//            .build()
//
//        Log.d(Constants.TAG, "Fetching: ${request.url}")
//
//        try {
//            client.newCall(request).execute().use { response ->
//                if (response.isSuccessful && response.body != null) {
//                    val content = response.body!!.string()
//                    hashmap[address] = content  // Salvează în cache
//                    content
//
//                    // val html = response.body!!.string()
//                    //
//                    //                // Parsează HTML-ul cu Jsoup
//                    //                val doc = Jsoup.parse(html)
//                    //
//                    //                // Extrage doar body-ul (fără <head>, <script>, etc.)
//                    //                val bodyContent = doc.body().text() // Doar text, fără taguri
//                    //                // SAU
//                    //                // val bodyContent = doc.body().html() // Cu taguri HTML
//                    //
//                    //                hashmap[address] = bodyContent
//                    //                bodyContent
//                } else {
//                    "ERROR - ${response.code}"
//                }
//            }
//        } catch (e: IOException) {
//            Log.e(Constants.TAG, "IOException: ${e.message}")
//            "ERROR - ${e.message}"
//        }
//    }

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
            val socket = Socket("time.nist.gov", 13)
            val bufferedReader = Utilities.getReader(socket)
            bufferedReader.readLine()
            utcTime = bufferedReader.readLine()
            Log.i("PracticalTest02", "utcTime: " + utcTime)
        } catch (ioException: IOException) {
            Log.e("PracticalTest02", ioException.message!!)
            ioException.printStackTrace()
        }

        return utcTime
    }
}