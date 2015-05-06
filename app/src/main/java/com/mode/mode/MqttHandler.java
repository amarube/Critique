package com.mode.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.andtinder.model.CardModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * Created by allanmarube on 5/5/15.
 */
public class MqttHandler implements MqttCallback {


    private MqttAndroidClient client;
    private Context ctx;
    private SessionManager session;

    public static String NAMESPACE = "critiq2/";


    //MqttHandler Instance
    private static MqttHandler ourInstance = new MqttHandler();

    public static String BASEURL = "tcp://messagesight.demos.ibm.com:1883";

    //returns MQTTHAndler instance
    public static MqttHandler getInstance() {
        return ourInstance;
    }

    //empty constructor
    private MqttHandler() {


    }


    //Gets context, broker URI and clientID, creates and returns an Android Client
    public MqttAndroidClient getClient(Context ctx, MqttCallback callback, String broker, String clientID) {

        if (client != null) {
            client.unregisterResources();
        }
        // persistStore = new MessageContainer();
        //client = new MqttAndroidClient(ctx, broker, clientID, persistStore );
        client = new MqttAndroidClient(ctx, broker, clientID);
        client.setCallback(this);

        return client;
    }

    public MqttAndroidClient getClientHandle() {
        return client;
    }

    //creates a client connection to broker and sets Connection options appropriately
    public void connect(Context ctx, IMqttActionListener listener) {
        this.ctx = ctx;

        session = new SessionManager(ctx);
        session.getId();


        try {
            System.out.println("Connecting to mqttbroker....");
            client.connect(ctx, listener);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


//    public void publishPost(CardModel card){
//        JSONObject post = new JSONObject();
//
//        String postId = UUID.randomUUID().toString();
//
//
//        try {
//            post.put("new_post", "New Post");
//            post.put("id", postId);
//            post.put("", isErase);
//            client.publish("critiq/"+session.getId()+"/"+ UUID.randomUUID(), post.toString().getBytes(), 0, false);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//
//    }


    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println("Message arrived: "+ s);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }


    //converts Base64 encoded string to a Bitmap
    public static Bitmap stringToBitmap(String encodedBitmap) {
        byte[] bitmapBytesOptImg = Base64.decode(encodedBitmap, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bitmapBytesOptImg, 0, bitmapBytesOptImg.length);
    }

    //converts a bitmap image to a string
    public static String bitmapToString(Bitmap bitmapImage) {
        if (bitmapImage == null)
            return null;
        Bitmap bitmap = bitmapImage;
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
        byte[] bitmapdata = blob.toByteArray();
        return Base64.encodeToString(bitmapdata, Base64.DEFAULT);
    }


}
