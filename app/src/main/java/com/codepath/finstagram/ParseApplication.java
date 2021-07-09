package com.codepath.finstagram;

import android.app.Application;

import com.codepath.finstagram.models.Comment;
import com.codepath.finstagram.models.Like;
import com.codepath.finstagram.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse model
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Like.class);
        ParseObject.registerSubclass(Comment.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.app_id))
                .clientKey(getString(R.string.client_key))
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
