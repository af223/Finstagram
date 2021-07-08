package com.codepath.finstagram.fragments;

import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.codepath.finstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * This fragment allows the logged in user to view their own 20 most recent posts, and pull down
 * to refresh the feed. This fragment appears when the profile (right) option is selected on the bottom navigation.
 */

public class ProfileFragment extends PostsFragment {

    // get current user's 20 most recent posts
    @Override
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    Toast.makeText(getContext(), "Error: Unable to load posts", Toast.LENGTH_SHORT).show();
                    return;
                }
                allPosts.clear();
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
        });
    }
}
