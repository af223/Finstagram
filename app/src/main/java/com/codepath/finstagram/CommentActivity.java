package com.codepath.finstagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.finstagram.adapters.CommentsAdapter;
import com.codepath.finstagram.models.Comment;
import com.codepath.finstagram.models.Post;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";
    private EditText etComment;
    private Button btnPostComment;
    private RecyclerView rvComments;
    private ArrayList<Comment> allComments;
    private CommentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        etComment = findViewById(R.id.etComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);

        allComments = new ArrayList<>();
        adapter = new CommentsAdapter(this, allComments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.setAdapter(adapter);

        loadPost();

    }

    private void loadPost() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.getInBackground(getIntent().getStringExtra(Post.class.getSimpleName()), new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post", e);
                    Toast.makeText(CommentActivity.this, "Error: Unable to load post details", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseQuery<Comment> queryC = ParseQuery.getQuery(Comment.class);
                queryC.include(Comment.KEY_USER);
                Log.i(TAG, getIntent().getStringExtra(Post.class.getSimpleName()));
                queryC.whereEqualTo(Comment.KEY_POST, post);
                queryC.addDescendingOrder(Post.KEY_CREATED_AT);
                queryC.findInBackground(new FindCallback<Comment>() {
                    @Override
                    public void done(List<Comment> objects, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue with getting comments", e);
                            Toast.makeText(CommentActivity.this, "Error: Unable to load comments", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(TAG, "got comments");
                        allComments.clear();
                        allComments.addAll(objects);
                        adapter.notifyDataSetChanged();

                        btnPostComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String comment = etComment.getText().toString();
                                if (comment.isEmpty()) {
                                    Toast.makeText(CommentActivity.this, "Can't post empty comment!", Toast.LENGTH_SHORT).show();
                                } else {
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
                        });

                    }
                });
            }
        });
    }
}