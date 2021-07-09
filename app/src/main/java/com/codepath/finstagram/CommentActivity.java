package com.codepath.finstagram;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.finstagram.adapters.CommentsAdapter;
import com.codepath.finstagram.models.Comment;
import com.codepath.finstagram.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * This activity allows the user to view and create comments for the selected post. For each comment,
 * the user can see the commenter's username, profile picture, and comment.
 * <p>
 * This activity appears when the user has clicked on the comment icon from DetailsActivity.java. The
 * intent from DetailsActivity.java contains the objectId for the selected post.
 */

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";
    private EditText etComment;
    private Button btnPostComment;
    private RecyclerView rvComments;
    private ArrayList<Comment> allComments;
    private CommentsAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        etComment = findViewById(R.id.etComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);
        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        allComments = new ArrayList<>();
        adapter = new CommentsAdapter(this, allComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        loadPost();
    }

    private void loadPost() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.getInBackground(getIntent().getStringExtra(Post.class.getSimpleName()), new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post", e);
                    Toast.makeText(CommentActivity.this, "Error: Unable to load post details", Toast.LENGTH_SHORT).show();
                    return;
                }
                swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        DetailsActivity.loadComments(post, CommentActivity.this, allComments, adapter, swipeContainer);
                    }
                });
                DetailsActivity.loadComments(post, CommentActivity.this, allComments, adapter, swipeContainer);
                btnPostComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String comment = etComment.getText().toString();
                        if (comment.isEmpty()) {
                            Toast.makeText(CommentActivity.this, "Can't post empty comment!", Toast.LENGTH_SHORT).show();
                        } else {
                            postComment(comment, post);
                        }
                    }
                });
            }
        });
    }

    private void postComment(String comment, Post post) {
        Comment comm = new Comment();
        comm.setUser(ParseUser.getCurrentUser());
        comm.setComment(comment);
        comm.put(Comment.KEY_POST, post);
        comm.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving comment: ", e);
                    Toast.makeText(CommentActivity.this, "Error while saving: unable to comment", Toast.LENGTH_SHORT).show();
                }
                allComments.add(0, comm);
                adapter.notifyItemInserted(0);
                etComment.setText("");
            }
        });
    }
}