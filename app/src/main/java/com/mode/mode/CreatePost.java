package com.mode.mode;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class CreatePost extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private MqttAndroidClient client;
    private SessionManager session;
    private boolean imageLoaded;
    private String imgDecodableString;
    private EditText caption;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLatitude;
    private String mLongitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        buildGoogleApiClient();


        caption = (EditText)findViewById(R.id.caption);

        client = MqttHandler.getInstance().getClientHandle();
        session = new SessionManager(this);
        imageLoaded = false;






//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Date date = new Date();
//        System.out.println(dateFormat.format(date));
//
//
//
//        try {
//
//            Date dat = dateFormat.parse(dateFormat.format(date));
//            System.out.println(date);
//            System.out.println(dat);
//
//            System.out.println("Equality test...");
//            if (date.toString().compareTo(dat.toString())==0){
//                System.out.println("EQUALL");
//            }
//
//
//            if (dateFormat.parse(dateFormat.format(date)).compareTo(date)==0){
//                System.out.println("EQUAL");
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static int RESULT_LOAD_IMG = 1;

    public void onChooseImageButtonClick(View view){
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    public void onPostButtonClick(View view){

        Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);

        JSONObject post = new JSONObject();

        String postId = UUID.randomUUID().toString();

        try {
            post.put("type", "create");
            post.put("postid", postId);
            post.put("userid", session.getId());
            post.put("like", 0);
            post.put("dislike", 0);
            post.put("caption", caption.getText().toString());
            post.put("image", MqttHandler.bitmapToString(bitmap));
            if (mLatitude != null && mLongitude != null) {
                post.put("lat", mLatitude);
                post.put("lng", mLongitude);
            }
            post.put("lastPub", session.getId());
            client.publish(MqttHandler.NAMESPACE + session.getId() + "/" + postId, post.toString().getBytes(), 0, true);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


        return;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked

            Toast.makeText(this, "Image Loaded", Toast.LENGTH_SHORT).show();
            imageLoaded = true;

            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                //ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView after decoding the String
                //imgView.setImageBitmap(BitmapFactory
                  //      .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }


    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("Connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            System.out.println(String.valueOf(mLastLocation.getLatitude()));
            System.out.println(String.valueOf(mLastLocation.getLongitude()));
            mLatitude = String.valueOf(mLastLocation.getLatitude());
            mLongitude = String.valueOf(mLastLocation.getLongitude());
        }


      //  System.out.println(String.valueOf(mLastLocation.getLatitude()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("FAILED");
        System.out.println("GOOGLE CONNECTION FAILED: "+connectionResult.toString());

    }
}
