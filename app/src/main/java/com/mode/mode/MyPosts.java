package com.mode.mode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.view.BaseCardStackAdapter;
import com.andtinder.view.CardContainer;
import com.andtinder.view.CardStackAdapter;
import com.andtinder.view.SimpleCardStackAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyPosts extends ActionBarActivity implements MqttCallback{

    private MqttAndroidClient client;
    private SessionManager session;
    private CardContainer mCardContainer;
    private MyCardStackAdapter adapter;
    private ArrayList<Post> currCards;
    private static int noOfCards = 0;

    private boolean isWaiting =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        session = new SessionManager(this);
        client = MqttHandler.getInstance().getClientHandle();
        //adapter = new SimpleCardStackAdapter(this);
        adapter = createAdapter(this);

        currCards = new ArrayList<>();


//        try {
//            //client.subscribe("critiq/"+session.getId()+"/+", 2);
//
//
//            System.out.println("Subscribed successfully");
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }

        client.setCallback(this);
        mCardContainer = (CardContainer) findViewById(R.id.layoutview2);

        session = new SessionManager(this);

        //adapter.add(createCardModel("Title1", "Description goes here", R.drawable.picture1));
       // adapter.add(createCardModel("Title2", "Description goes here", R.drawable.picture2));
        //adapter.add(createCardModel("Title3", "Description goes here", R.drawable.picture3));

        //adapter.pop();
       mCardContainer.setAdapter(adapter);
       //SimpleCardStackAdapter adap = (SimpleCardStackAdapter)mCardContainer.getAdapter();
       //adap.add(createCardModel("Title1", "Description goes here", R.drawable.picture1));

    }


    public Post createCardModel(String title, String description, String bitmapString){
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
            @Override
            public void onLike() {
                Log.i("Swipeable Cards","I like the card");
                MyPosts.noOfCards--;
                adapter.remove(cardModel);

                System.out.println("SIZE OF ADAPTER:"+ noOfCards);
                if (adapter.isEmpty()){

                    try {
                        client.subscribe(MqttHandler.NAMESPACE+session.getId()+"/+", 2);
                        //adapter = new SimpleCardStackAdapter(MyPosts.this);
                        adapter = createAdapter(MyPosts.this);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onDislike() {
                MyPosts.noOfCards--;
                Log.i("Swipeable Cards","I dislike the card");
                System.out.println("SIZE OF ADAPTER:"+ noOfCards);
                adapter.remove(cardModel);
                if (adapter.isEmpty()){
                    try {
                        client.subscribe(MqttHandler.NAMESPACE+session.getId()+"/+", 2);
                        //adapter = new SimpleCardStackAdapter(MyPosts.this);
                        adapter = createAdapter(MyPosts.this);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return cardModel;

    }



    @Override
    public void onResume(){
        super.onResume();
        client.setCallback(this);

        try {
           // adapter = new SimpleCardStackAdapter(MyPosts.this);
            adapter = createAdapter(MyPosts.this);
            client.subscribe(MqttHandler.NAMESPACE+session.getId()+"/+", 2);
            noOfCards = 0;
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onPause(){
        super.onPause();

        try {
            client.unsubscribe(MqttHandler.NAMESPACE + session.getId() + "/+");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        noOfCards = 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.createPost) {
            //start camera or look for pictures in background
            Intent intent = new Intent(this, CreatePost.class);
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

        System.out.println("Message arrived from...: "+ s);
        JSONObject post = new JSONObject(mqttMessage.toString());

        post.optString("type");
        post.optString("postid");
        post.optString("userid");
        String likes = post.optString("like");
        String dislikes = post.optString("dislike");
        String caption = post.optString("caption");
        String image = post.optString("image");
        Post cardModel = createCardModel(caption, "Yes: "+likes+" No: "+dislikes, image);
       // Post postNew = new Post(caption, "Yes: "+likes+" No: "+dislikes, new BitmapDrawable(getResources(), MqttHandler.stringToBitmap(image)));
        cardModel.postid = post.optString("postid");
        //postNew.card = cardModel;

       // adapter = new SimpleCardStackAdapter(this);

//      adapter.getCardModel(0).setDescription("HAHA");
        //adapter.pop();
//
//        if (post.optString("voters").length() > 1 && isWaiting){
//            adapter = new SimpleCardStackAdapter(this);
//            noOfCards = 0;
//        }

        adapter.add(cardModel);
        MyPosts.noOfCards++;

       // adapter.notify();

        mCardContainer.setAdapter(adapter);
        //mCardContainer.setAdapter(adapter);


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery complete");
//        isWaiting = true;
//        try {
//            client.unsubscribe(MqttHandler.NAMESPACE + session.getId() + "/+");
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }

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
