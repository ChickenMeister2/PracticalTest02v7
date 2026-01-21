package ro.pub.cs.systems.eim.practicaltest02v7.network

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import ro.pub.cs.systems.eim.practicaltest02v7.general.Constants
import ro.pub.cs.systems.eim.practicaltest02v7.general.Utilities
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

class ClientAsyncTask(private var textViewResponse : TextView) :
    AsyncTask<String?, String?, Void?>() {

    companion object {
        var socket: Socket? = null
        var printWriter: PrintWriter? = null
        var bufferedReader: BufferedReader? = null
        var isConnected = false
    }

    override fun doInBackground(vararg params: String?): Void? {
        try {
            val serverAddress: String? = params[0]
            val serverPort = params[1]!!.toInt()

            // Conectare la server
            socket = Socket(serverAddress, serverPort)
            Log.v(
                Constants.TAG,
                "Connection opened with ${socket!!.inetAddress}:${socket!!.localPort}"
            )

            // Inițializează reader și writer
            printWriter = Utilities.getWriter(socket!!)
            bufferedReader = Utilities.getReader(socket!!)
            isConnected = true

            // Așteaptă să primească date de la server
            var currentLine: String?
            while (bufferedReader!!.readLine().also { currentLine = it } != null) {
                publishProgress(currentLine)
            }

        } catch (exception: Exception) {
            Log.e(Constants.TAG, "An exception has occurred: ${exception.message}")
            if (Constants.DEBUG) {
                exception.printStackTrace()
            }
            isConnected = false
        } finally {
            try {
                socket?.close()
                isConnected = false
                Log.v(Constants.TAG, "Connection closed")
            } catch (e: IOException) {
                Log.e(Constants.TAG, "Error closing socket: ${e.message}")
            }
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        isConnected = false
    }
    override fun onPreExecute() {
        textViewResponse.text = ""
    }

    override fun onProgressUpdate(vararg progress: String?) {
        textViewResponse.append(progress[0] + "\n")
    }
    fun sendUrl(url: String) {
        if (isConnected && printWriter != null) {
            Thread {
                try {
                    printWriter!!.println(url)
                    printWriter!!.flush()
                    Log.d(Constants.TAG, "Sent Command to server: $url")
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Error sending Command: ${e.message}")
                }
            }.start()
        } else {
            Log.e(Constants.TAG, "Not connected to server!")
        }
    }
}