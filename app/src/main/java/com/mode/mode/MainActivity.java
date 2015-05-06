package com.mode.mode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements MqttCallback, IMqttActionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    CardContainer mCardContainer;
    private MqttAndroidClient client;
    private SessionManager session;
    private static int noOfCards = 0;
    private MyCardStackAdapter adapter;
    private static boolean isConnected = false;
    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;
    private String mLatitude;
    private String mLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCardContainer = (CardContainer) findViewById(R.id.layoutview);

        session = new SessionManager(this);
        mCardContainer.setOrientation(Orientations.Orientation.Disordered);
        client = MqttHandler.getInstance().getClient(this,this, MqttHandler.BASEURL, MqttClient.generateClientId());
        client.setCallback(this);
//        MqttHandler.getInstance().connect(this, this);
//        adapter = new SimpleCardStackAdapter(this);
//        mCardContainer.setAdapter(adapter);
        buildGoogleApiClient();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    public Post createCardModel(final String title, String description, final String bitmapString,
                                     final String type, final String postId, final String userId,
                                     final String likes, final String dislikes, final String voters){




        Bitmap bm = MqttHandler.stringToBitmap(bitmapString);
        final Post cardModel = new Post(title, description,  new BitmapDrawable(getResources(), bm));
        cardModel.setOnClickListener(new CardModel.OnClickListener() {

            @Override
            public void OnClickListener() {
                Log.i("Swipeable Cards", "I am pressing the card");
                //Toast.makeText(MyPosts.this, cardModel.getDescription(), Toast.LENGTH_SHORT).show();


            }
        });

        cardModel.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            JSONObject post;
            @Override
            public void onLike() {
                Log.i("Swipeable Cards","I like the card");
                MainActivity.noOfCards--;

                adapter.remove(cardModel);

                post = new JSONObject();


                /*************************/

                try {
                    post.put("type", type);
                    post.put("postid", postId);
                    post.put("userid", userId);
                    post.put("like", Integer.parseInt(likes)+1);
                    post.put("dislike", dislikes);
                    post.put("caption", title);
                    post.put("image", bitmapString);
                    //post.put(session.getId(), true);
                    post.put("voters", voters+":"+session.getId());
                   // post.put("lastPub", session.getId());
                    System.out.println("PUBLISHING...LIKE");
                   // client.unsubscribe("critiq/+/+");
                    client.publish(MqttHandler.NAMESPACE + userId + "/" + postId, post.toString().getBytes(), 1, true);
                    //client.subscribe("critiq/+/+", 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttPersistenceException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }


                System.out.println("SIZE OF ADAPTER:"+ noOfCards);
                if (MainActivity.noOfCards ==0){
                    adapter = createAdapter(MainActivity.this);


//                    try {
//                        client.subscribe("critiq/+/+", 0);
//                        adapter = new SimpleCardStackAdapter(MainActivity.this);
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
                }

            }

            @Override
            public void onDislike() {
                MainActivity.noOfCards--;
                Log.i("Swipeable Cards","I dislike the card");
                System.out.println("SIZE OF ADAPTER:"+  noOfCards );

                adapter.remove(cardModel);

                post = new JSONObject();

                /*************************/

                try {
                    post.put("type", type);
                    post.put("postid", postId);
                    post.put("userid", userId);
                    post.put("dislike", Integer.parseInt(dislikes)+1);
                    post.put("like", likes);
                    post.put("caption", title);
                    post.put("image", bitmapString);
                   // post.put(session.getId(), true);
                    post.put("voters", voters+":"+session.getId());
                    // post.put("lastPub", session.getId());
                    System.out.println("PUBLISHING...UNLIKE");
                    // client.unsubscribe("critiq/+/+");
                    client.publish(MqttHandler.NAMESPACE + userId + "/" + postId, post.toString().getBytes(), 1, true);
                    //client.subscribe("critiq/+/+", 0);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttPersistenceException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                if (MainActivity.noOfCards ==0){
                    adapter = createAdapter(MainActivity.this);
//                    try {
//                        client.subscribe("critiq/+/+", 0);
//                        adapter = new SimpleCardStackAdapter(MainActivity.this);
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });

        return cardModel;

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_myPosts) {
            Intent intent = new Intent(this, MyPosts.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

        System.out.println("MainActivity string arrived: "+s);
        System.out.println("Message arrived from...: "+ s);
        JSONObject post = new JSONObject(mqttMessage.toString());

        System.out.println("SESSION ID: "+ session.getId());
        System.out.println(post.toString());
        System.out.println(mqttMessage.toString());

        String usersVoted = post.optString("voters");

        String userId = post.optString("userid");

        if (session.getId().compareTo(userId) == 0){
            return;
        }

        //System.out.println("LATITUDE: "+ Double.parseDouble(mLatitude)+20);
        //System.out.println("LONGITUDE: "+ Double.parseDouble(post.optString("lat")));

//        System.out.println("LATITUDE LEBGTH: "+ post.optString("lat").length());
//
//
//        if (post.optString("lat") != null || post.optString("lat").length() != 0 ) {
//
//            if (Double.parseDouble(mLatitude) + 20 < Double.parseDouble(post.optString("lat")) ||
//                    Double.parseDouble(mLatitude) - 20 > Double.parseDouble(post.optString("lat")) ||
//                    Double.parseDouble(mLongitude) + 20 < Double.parseDouble(post.optString("lng")) ||
//                    Double.parseDouble(mLatitude) - 20 > Double.parseDouble(post.optString("lng"))) {
//                return;
//            }
//        }

        String [] users = usersVoted.split(":");

        for (int i = 0; i < users.length; i++){
            if (users[i].compareTo(session.getId())==0){
                return;
            }
        }


//        if (post.optBoolean(session.getId(), false)){
//            System.out.println("Seen Before");
//            return;
//        }

        String type = post.optString("type");
        String postId = post.optString("postid");
        String likes = post.optString("like");
        String dislikes = post.optString("dislike");
        String caption = post.optString("caption");
        String image = post.optString("image");
        String voters = post.optString("voters");
        Post cardModel = createCardModel(caption, "Yes: "+likes+" No: "+dislikes, image, type, postId, userId , likes, dislikes, voters);
        cardModel.postid = postId;


        //adapter = new SimpleCardStackAdapter(this);

//      adapter.getCardModel(0).setDescription("HAHA");
        //adapter.pop();

        adapter.add(cardModel);
        MainActivity.noOfCards++;

        // adapter.notify();

        mCardContainer.setAdapter(adapter);
        //mCardContainer.setAdapter(adapter);

    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Toast.makeText(this, "Delivery complete", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        try {
            adapter = createAdapter(MainActivity.this);
            client.publish(MqttHandler.NAMESPACE+session.getId(),"".toString().getBytes(), 0, false);
            client.subscribe(MqttHandler.NAMESPACE+"+/+",0);


        } catch (MqttException e) {
            e.printStackTrace();
        }

        isConnected = true;


    }


    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

    }

    @Override
    public void onStop(){
        super.onStop();

    }

    @Override
    public void onResume(){
        super.onResume();
        client.setCallback(this);
        if(isConnected) {

            System.out.println("RECONNECTING....");
            try {
                adapter = createAdapter(MainActivity.this);
                noOfCards = 0;
                client.subscribe(MqttHandler.NAMESPACE+"+/+", 0);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
//        else {
//            MqttHandler.getInstance().connect(this, this);
//        }
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

        MqttHandler.getInstance().connect(this, this);
        adapter = createAdapter(this);
        mCardContainer.setAdapter(adapter);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            client.unsubscribe(MqttHandler.NAMESPACE+"+/+");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public MyCardStackAdapter createAdapter(Context context){

        MyCardStackAdapter adapter = new MyCardStackAdapter(context) {
            @Override
            protected View getCardView(int i, Post model, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.std_card_inner, parent, false);
                    assert convertView != null;
                }

                ((ImageView) convertView.findViewById(R.id.image)).setImageDrawable(model.getCardImageDrawable());
                ((TextView) convertView.findViewById(R.id.title)).setText(model.getTitle());
                ((TextView) convertView.findViewById(R.id.description)).setText(model.getDescription());

                return convertView;
            }

        };

        return adapter;
    }

}
