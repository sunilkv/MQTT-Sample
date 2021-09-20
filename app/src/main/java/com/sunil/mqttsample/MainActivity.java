package com.sunil.mqttsample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sunil.mqttsample.databinding.ActivityMainBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {


    static String TAG = "MQTT MainActivity";
    ActivityMainBinding binding;
    String server_URL = "tcp://broker.hivemq.com:1883";
    String topic = "test/topic";
    //Empty means all clients Messages(Broadcast Message)
    //if Specify the Client ID then only client related message will be recieved..
    String clientId = "";
    MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        initMQTT();
    }

    private void init() {
        binding.requestMqttLl.setVisibility(View.GONE);
        binding.fetchedMsgTv.setText("");
        binding.mqttServerUrlEt.setText(server_URL);
        binding.topicEt.setText(topic);

        binding.connectMqttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                server_URL = binding.mqttServerUrlEt.getText().toString();
                initMQTT();
            }
        });

        binding.getMqttMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeTopic();
            }
        });

    }

    private void initMQTT() {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), server_URL, clientId);

        try {
            IMqttToken token = mqttAndroidClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: MQTT Connection Success");
                    binding.connetMqttLl.setVisibility(View.GONE);
                    binding.requestMqttLl.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "OnFAilure: " + exception.getMessage());

                }
            });
        } catch (Exception e) {
            Log.d(TAG, "initMQTT: " + e.getMessage());
        }

    }


    public void subscribeTopic() {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                    getMessage();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void getMessage() {
        binding.fetchedMsgTv.setText("");
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                try {
                    mqttAndroidClient.connect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                Log.d(TAG, "messageArrived: " + message.toString());
                binding.fetchedMsgTv.setText(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void disconnect() {

        try {
            IMqttToken token = mqttAndroidClient.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: MQTT Disconnect Success");
                    binding.connetMqttLl.setVisibility(View.VISIBLE);
                    binding.requestMqttLl.setVisibility(View.GONE);
                    binding.fetchedMsgTv.setText("");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "OnFAilure: " + exception.getMessage());

                }
            });
        } catch (Exception e) {
            Log.d(TAG, "initMQTT: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.disconnect:
                disconnect();
                return true;
        }
        return false;
    }
}