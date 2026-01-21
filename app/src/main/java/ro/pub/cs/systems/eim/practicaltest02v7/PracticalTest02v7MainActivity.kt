package ro.pub.cs.systems.eim.practicaltest02v7

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ro.pub.cs.systems.eim.practicaltest02v7.network.ClientAsyncTask
import ro.pub.cs.systems.eim.practicaltest02v7.network.ServerThread

class PracticalTest02v7MainActivity : AppCompatActivity() {
    private var clientTask: ClientAsyncTask? = null
    lateinit var editTextPortPornireServer : EditText
    lateinit var buttonPornireServer : Button

    lateinit var editTextIpClient : EditText
    lateinit var editTextPortClient : EditText
    lateinit var buttonConectareServer: Button

    lateinit var editTextComanda : EditText

    lateinit var buttonSend : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practical_test02v7_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextPortPornireServer = findViewById(R.id.edit_text_port)
        buttonPornireServer = findViewById(R.id.button_start_server)

        editTextIpClient = findViewById(R.id.edit_text_ip_client)
        editTextPortClient = findViewById(R.id.edit_text_port_client)
        buttonPornireServer = findViewById(R.id.button_start_server)

        buttonConectareServer = findViewById(R.id.button_connect_srv)


        editTextComanda = findViewById(R.id.edit_text_comanda)
        buttonSend = findViewById(R.id.button_send)

        // pornirea serverului
        buttonPornireServer.setOnClickListener{ ServerThread(editTextPortPornireServer.text.toString()).startServer() }

        // conectarea la server
        buttonConectareServer.setOnClickListener {
            val host = editTextIpClient.text.toString();
            val port = editTextPortClient.text.toString();
            if (host.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "host" + host + " " + port, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            clientTask = ClientAsyncTask()
            clientTask!!.execute(host, port)

            Toast.makeText(this, "Conectare la server...", Toast.LENGTH_SHORT).show()
        }

        buttonSend.setOnClickListener {
            val url = editTextComanda.text.toString()

            if (url.isEmpty()) {
                Toast.makeText(this, "Introdu O comanda!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ClientAsyncTask.isConnected) {
                clientTask?.sendUrl(url)
                Toast.makeText(this, "Trimitere comanda...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nu ești conectat la server!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}