package com.codepath.finstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.finstagram.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.parceler.Parcels;

public class DetailsActivity extends AppCompatActivity {

    Post post;
    private TextView tvAuthor;
    private ImageView ivPost;
    private TextView tvDescription;
    private TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvAuthor = findViewById(R.id.tvAuthor);
        ivPost = findViewById(R.id.ivPost);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);

        //post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.getInBackground(getIntent().getStringExtra(Post.class.getSimpleName()), new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                tvDescription.setText(post.getDescription());
                ParseFile image = post.getImage();
                if (image != null) {
                    Glide.with(DetailsActivity.this).load(image.getUrl()).into(ivPost);
                }
                tvAuthor.setText(post.getUser().getUsername());
                tvDate.setText(Post.calculateTimeAgo(post.getCreatedAt()));
            }
        });
    }
}