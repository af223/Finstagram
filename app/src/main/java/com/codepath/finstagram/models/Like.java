package com.codepath.finstagram.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;


@ParseClassName("Like")
public class Like extends ParseObject {
    public static final String KEY_POST = "post";
    public static final String KEY_USER = "user";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public ParseObject getPost() {
        return getParseObject(KEY_POST);
    }
}
