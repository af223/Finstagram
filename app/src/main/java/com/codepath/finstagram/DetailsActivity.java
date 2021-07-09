package com.codepath.finstagram;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.finstagram.adapters.CommentsAdapter;
import com.codepath.finstagram.models.Comment;
import com.codepath.finstagram.models.Like;
import com.codepath.finstagram.models.Post;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity allows the user to view the selected post in detail, like/unlike the post, view the number of likes,
 * and view comments on the post.
 * <p>
 * This activity appears when the user has clicked on a post in the home or profile tab, and can be started
 * from either PostsFragment.java or ProfileFragment.java. It starts the CommentActivity.java when the user
 * wants to add a comment to the post.
 */

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";
    private TextView tvAuthor;
    private ImageView ivPost;
    private TextView tvDescription;
    private TextView tvDate;
    private ImageView ivPFP;
    private ImageButton ibLike;
    private TextView tvNumLikes;
    private RecyclerView rvComments;
    private ImageButton ibComment;
    private SwipeRefreshLayout swipeContainer;
    private Boolean liked = false;
    private Like like;
    private CommentsAdapter adapter;
    private ArrayList<Comment> allComments;

    public static void loadComments(Post post, Context context, ArrayList<Comment> allComms,
                                    CommentsAdapter cAdapter, SwipeRefreshLayout sContainer) {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_USER);
        query.whereEqualTo(Comment.KEY_POST, post);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    Toast.makeText(context, "Error: Unable to load comments", Toast.LENGTH_SHORT).show();
                    return;
                }
                allComms.clear();
                allComms.addAll(objects);
                cAdapter.notifyDataSetChanged();
                sContainer.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvAuthor = findViewById(R.id.tvAuthor);
        ivPost = findViewById(R.id.ivPost);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        ivPFP = findViewById(R.id.ivPFP);
        ibLike = findViewById(R.id.ibLike);
        tvNumLikes = findViewById(R.id.tvNumLikes);
        rvComments = findViewById(R.id.rvComments);
        ibComment = findViewById(R.id.ibComment);
        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        allComments = new ArrayList<>();
        adapter = new CommentsAdapter(DetailsActivity.this, allComments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DetailsActivity.this);
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.setAdapter(adapter);

        loadPost(); // finish loading in anything dependent on the post
    }

    // fetch post from Parse based on objectID passed in from intent
    private void loadPost() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.getInBackground(getIntent().getStringExtra(Post.class.getSimpleName()), new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post", e);
                    Toast.makeText(DetailsActivity.this, "Error: Unable to load post details", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvDescription.setText(post.getDescription());
                ParseFile image = post.getImage();
                if (image != null) {
                    Glide.with(DetailsActivity.this).load(image.getUrl()).into(ivPost);
                }
                tvAuthor.setText(post.getUser().getUsername());
                tvDate.setText(Post.calculateTimeAgo(post.getCreatedAt()));
                ParseFile pfp = post.getPFP();
                if (pfp != null) {
                    Glide.with(DetailsActivity.this).load(pfp.getUrl()).transform(new CircleCrop()).into(ivPFP);
                }
                swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadComments(post, DetailsActivity.this, allComments, adapter, swipeContainer);
                    }
                });
                ibComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(DetailsActivity.this, CommentActivity.class);
                        i.putExtra(Post.class.getSimpleName(), post.getObjectId());
                        startActivity(i);
                    }
                });
                ibLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (liked) {
                            deleteLike(post);
                        } else {
                            addLike(post);
                        }
                    }
                });

                displayLikes(post);
                setLikeButton(post);
                loadComments(post, DetailsActivity.this, allComments, adapter, swipeContainer);
            }
        });
    }

    private void displayLikes(Post post) {
        if (post.getNumLikes() > 0) {
            tvNumLikes.setVisibility(View.VISIBLE);
            String val = post.getNumLikes() + " likes";
            tvNumLikes.setText(val);
        } else {
            tvNumLikes.setVisibility(View.GONE);
        }
    }

    // determine whether or not the user has liked the post, puts the like button accordingly
    private void setLikeButton(Post post) {
        ParseQuery<Like> queryLike = ParseQuery.getQuery(Like.class);
        queryLike.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());
        queryLike.whereEqualTo(Like.KEY_POST, post);
        queryLike.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting likes", e);
                    Toast.makeText(DetailsActivity.this, "Error: Unable to load likes", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!objects.isEmpty()) {
                    liked = true;
                    ibLike.setImageResource(R.drawable.ufi_heart_active);
                    like = objects.get(0);
                }
            }
        });
    }

    private void deleteLike(Post post) {
        like.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with unliking", e);
                    Toast.makeText(DetailsActivity.this, "Error: Unable to unlike", Toast.LENGTH_SHORT).show();
                    return;
                }
                ibLike.setImageResource(R.drawable.ufi_heart);
                liked = !liked;
                post.setNumLikes(post.getNumLikes() - 1);
                post.saveInBackground();
                displayLikes(post);
            }
        });
    }

    private void addLike(Post post) {
        like = new Like();
        like.setUser(ParseUser.getCurrentUser());
        like.setPost(post);
        like.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with liking", e);
                    Toast.makeText(DetailsActivity.this, "Error: Unable to like", Toast.LENGTH_SHORT).show();
                    return;
                }
                ibLike.setImageResource(R.drawable.ufi_heart_active);
                liked = !liked;
                post.setNumLikes(post.getNumLikes() + 1);
                post.saveInBackground();
                displayLikes(post);
            }
        });
    }
}