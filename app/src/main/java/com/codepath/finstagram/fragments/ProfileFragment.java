package com.codepath.finstagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.finstagram.R;
import com.codepath.finstagram.adapters.PostsAdapter;
import com.codepath.finstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This fragment allows the logged in user to view their own 20 most recent posts, and pull down
 * to refresh the feed. This fragment appears when the profile (right) option is selected on the bottom navigation.
 */

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private PostsAdapter adapter;
    private List<Post> allPosts;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView rvPosts;
    private ImageView ivPfp;
    private TextView tvName;
    private EndlessRecyclerViewScrollListener scrollListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivPfp = view.findViewById(R.id.ivPfp);
        tvName = view.findViewById(R.id.tvName);
        ParseFile pfp = ParseUser.getCurrentUser().getParseFile(Post.KEY_PFP);
        if (pfp != null) {
            Glide.with(getContext()).load(pfp.getUrl()).transform(new CircleCrop()).into(ivPfp);
        }
        tvName.setText(ParseUser.getCurrentUser().getUsername());
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts();
                scrollListener.resetState();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        rvPosts = view.findViewById(R.id.rvPosts);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvPosts.setLayoutManager(gridLayoutManager);

        // endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMorePosts();
            }
        };
        rvPosts.addOnScrollListener(scrollListener);
        rvPosts.setAdapter(adapter);
        queryPosts();
    }

    // load current user's older posts
    private void loadMorePosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.whereLessThan("createdAt", Post.lastPostTime);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    Toast.makeText(getContext(), "Error: Unable to load posts", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (posts.isEmpty()) {
                    Toast.makeText(getContext(), "No more posts to load!", Toast.LENGTH_SHORT).show();
                }
                for (Post post : posts) {
                    if (post.getCreatedAt().before(Post.lastPostTime)) {
                        Post.lastPostTime = post.getCreatedAt();
                    }
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    // get current user's 20 most recent posts
    private void queryPosts() {
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
                Post.lastPostTime = new Date();
                for (Post post : posts) {
                    if (post.getCreatedAt().before(Post.lastPostTime)) {
                        Post.lastPostTime = post.getCreatedAt();
                    }
                }
                allPosts.clear();
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
        });
    }
}
