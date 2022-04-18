package com.stuffaboutcode.bluedot;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.stuffaboutcode.logger.Log;

public class VasYDroopi extends AppCompatActivity {

    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private StringBuffer mInStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private MonImage monView;
    private Segment segment;

    String address = null;
    String deviceName = null;

    private ProgressDialog progress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vasydroopi);
        monView = (MonImage) findViewById(R.id.monImage);
        Context context = getApplicationContext();
        monView.context = context;
        Intent newint = getIntent();
        deviceName = newint.getStringExtra(Devices.EXTRA_NAME);
        address = newint.getStringExtra(Devices.EXTRA_ADDRESS);

        // Get the bluetooth port number from preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int port_number = 1;
        // if the default port is not used, get the port
        if (!sharedPreferences.getBoolean("default_port", true)) {
            String port_value = sharedPreferences.getString("port", "0");
            port_number = Integer.parseInt(port_value);
        }

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        // Initialize the buffer for incoming messages
        mInStringBuffer = new StringBuffer("");

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, port_number,true);
        // envoyer un 1 entête puis 4 données
        send(buildMessage("1", 2, 3, 4, 5));
    }

    private String buildMessage(String operation, int col, int row, double x, double y) {
        return (operation + "," + String.valueOf(col) + "," + String.valueOf(row) + "," + String.valueOf(x) + "," + String.valueOf(y) + "\n");
    }

    private void disconnect() {
        if (mChatService != null) {
            mChatService.stop();
        };
        finish();
    }

    public void send(String message) { // utilise le chat service bluetooth pour envoyer un tableau d'octets (bytes)
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "cant send message - not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private void parseData(String data) {
        //msg(data);

        // add the message to the buffer
        mInStringBuffer.append(data);

        // debug - log data and buffer
        //Log.d("data", data);
        //Log.d("mInStringBuffer", mInStringBuffer.toString());
        //msg(data.toString());

        // find any complete messages
        String[] messages = mInStringBuffer.toString().split("\\n");
        int noOfMessages = messages.length;
        // does the last message end in a \n, if not its incomplete and should be ignored
        if (!mInStringBuffer.toString().endsWith("\n")) {
            noOfMessages = noOfMessages - 1;
        }

        // clean the data buffer of any processed messages
        if (mInStringBuffer.lastIndexOf("\n") > -1)
            mInStringBuffer.delete(0, mInStringBuffer.lastIndexOf("\n") + 1);

        // process messages
        for (int messageNo = 0; messageNo < noOfMessages; messageNo++) {
            processMessage(messages[messageNo]);
        }
    }

    private void processMessage(String message) {
        msg(message);
       /* // Debug
        // msg(message);
        String parameters[] = message.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        boolean invalid = false;

        //****************  Check the message = IL FAUT L'ADAPTER A NOTRE PROTOCOLE ?
        if (parameters.length > 0) {
            switch (parameters[0]) {
                case "4":
                    //********* NOTRE PROTOCOLE A FAIRE invalid = processSetMatrixMessage(parameters);
                    msg("4 - " + message +"'");
                    break;
                case "5":
                    //********* NOTRE PROTOCOLE invalid = processSetCellMessage(parameters);
                    msg("4 - " + message +"'");
                    break;
                default:
                    msg("ceci est un " + message +"'");
                    invalid = true;
            }
        }

        if (invalid) {
            //msg("Error - Invalid message received '" + message +"'");
        }*/
    }
/*
    private boolean processSetMatrixMessage(String parameters[]) {
        // "4,[color],[square],[border],[visible],[cols],[rows]"
        boolean invalid = false;

        // check length
        if (parameters.length == 7) {

            // cols
            //*********** matrix.setCols(Integer.parseInt(parameters[5]));

            // rows
            //*********** matrix.setRows(Integer.parseInt(parameters[6]));

            //color
            //********** String color = convertColor(parameters[1]);
            /********** if (!color.equals("")) {
                matrix.setColor(Color.parseColor(color));
            } else {
                invalid = true;
            }

            matrix.setSquare(parameters[2].equals("1"));

            matrix.setBorder(parameters[3].equals("1"));

            matrix.setVisible(parameters[4].equals("1"));

            matrix.update();

        } else {
            invalid = true;
        }
        return invalid;
    }
    */

    private void msg(String message) {
        TextView statusView = (TextView)findViewById(R.id.status);
        statusView.setText(message);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.d("status","connected");
                            msg("Connected to " + deviceName);
                            //********* UI matrix.setVisibility(View.VISIBLE);
                            // L'envoi de message au serveur via un send();
                            send("Bienvenue sur le Serveur Droopy "+ "\n");
                            Toast.makeText(getApplicationContext(), "Où souhaitez-vous allez ?", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.d("status","connecting");
                            msg("Connecting to " + deviceName);
                            //********* UI matrix.setVisibility(View.INVISIBLE);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Log.d("status","not connected");
                            msg("Not connected");
                            disconnect();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readData = new String(readBuf, 0, msg.arg1);
                    // message received
                    parseData(readData);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }

    };

    public void envoyer(android.view.View view) {
        send("X_start= " + monView.x1 + "\n" + "Y_start= " + monView.y1 + "\n");
        send("X_stop= " + monView.x2 + "\n" + "Y_stop= " + monView.y2 + "\n");
    }


/* EditText position_x = (EditText) findViewById(R.id.pos_x);
        int posX = (int) Integer.parseInt(position_x.getText().toString());

        EditText position_y = (EditText) findViewById(R.id.pos_y);
        int posY = (int) Integer.parseInt(position_y.getText().toString());

        send(posX + " " + posY + "\n");*/
    //}

    public void clear(android.view.View view){
        EditText position_x = (EditText)findViewById(R.id.pos_x);
        position_x.setText("");

        EditText position_y = (EditText)findViewById(R.id.pos_y);
        position_y.setText("");
    }

    public void pos(android.view.View view){/*
        int i=0;
        monView.get_pos[i]= monView.lastTouchDownXY[0];
        i++;
        monView.get_pos[i]= monView.lastTouchDownXY[1];
        if(i==5)
        {
            i=0;
        }*/
    }

}
