package com.mode.mode;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.andtinder.model.CardModel;

/**
 * Created by allanmarube on 5/5/15.
 */
public class Post extends CardModel {
    public CardModel card;
    public String left;
    public String right;
    public String owner;
    public String lat;
    public String lng;
    public String postid;

    public Post(String title, String description, Drawable drawable ){
        super(title, description,drawable);
    }

    public boolean equals(Object object)
    {
        //System.out.println("Checking for equality...");
        boolean sameSame = false;

        if (object != null && object instanceof Post)
        {
            sameSame = (this.postid.compareTo(((Post) object).postid)==0);
        }

        if(sameSame){
          //  System.out.println("They are the same....");
        }

        return sameSame;
    }



}
