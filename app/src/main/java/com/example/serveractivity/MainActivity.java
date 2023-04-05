package com.example.serveractivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Button button_sent;
    EditText smessage;
    TextView chat, display_status;
    String str, msg = "";
    int serverport = 10000;
    Socket client;
    Handler handler = new Handler();
    WifiManager wmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wmanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wmanager.setWifiEnabled(true);
        String ip =
                Formatter.formatIpAddress(wmanager.getConnectionInfo().getIpAddress());
        smessage = (EditText) findViewById(R.id.smessage);
        chat = (TextView) findViewById(R.id.chat);
        display_status = (TextView)
                findViewById(R.id.display_status);

        display_status.setText("Server hosted on " + ip);
        Thread serverThread = new Thread(new serverThread());
        serverThread.start();
        button_sent = (Button) findViewById(R.id.button_sent);
        button_sent.setEnabled(false);
        button_sent.setOnClickListener(v -> {

            if(smessage.getText() != null && !smessage.getText().toString().equals("")){
                Thread sentThread = new Thread(new sentMessage());
                sentThread.start();
            }
        });
    }

    class sentMessage implements Runnable {
        @Override
        public void run() {
            try {
                DataOutputStream os = new
                        DataOutputStream(client.getOutputStream());
                str = smessage.getText().toString();
                str = str + "\n";
                msg = msg + "Server : " + str;
                handler.post(() -> {
                    chat.setText(msg);
                    smessage.setText("");
                });
                os.writeBytes(str);
                os.flush();
//                os.close();

            } catch (Exception e) {
                System.out.println(e);

            }
        }
    }

    public class serverThread implements Runnable {
        @Override
        public void run() {
            try {

                ServerSocket serverSocket = new ServerSocket(serverport);
                client = serverSocket.accept();
                handler.post(() ->{
                    display_status.setText("Connected to client");
                    smessage.setEnabled(true);
                    button_sent.setEnabled(true);
                } );

                /*******************************************
                 setup i/p streams
                 ******************************************/

                DataInputStream in = new
                        DataInputStream(client.getInputStream());
                String line = null;
                while ((line = in.readLine()) != null) {
                    msg = msg + "Client  : " + line + "\n";
                    handler.post(() -> chat.setText(msg));
                }
                in.close();
                client.close();

            } catch (Exception e) {
                System.out.println(e);

            }
        }
    }
}