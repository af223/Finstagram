package com.codepath.finstagram;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.finstagram.models.Like;
import com.codepath.finstagram.models.Post;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * This activity allows the user to view the selected post in detail. Namely, the user can see the
 * poster, image, caption, and timestamp.
 *
 * This activity appears when the user has clicked on a post in the feed/home tab, and is started
 * from PostsFragment.java.
 */

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";
    private TextView tvAuthor;
    private ImageView ivPost;
    private TextView tvDescription;
    private TextView tvDate;
    private ImageView ivPFP;
    private ImageButton ibLike;
    private Boolean liked = false;
    private Like like;
    private Post pCurr;

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

        // fetch post from Parse based on objectID passed in from intent
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
                pCurr = post;
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
                            like = (Like) objects.get(0);
                        }
                    }
                });

            }
        });

        ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liked) {
                    like.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Issue with unliking", e);
                                Toast.makeText(DetailsActivity.this, "Error: Unable to unlike", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(DetailsActivity.this, "Unliked", Toast.LENGTH_SHORT).show();
                            ibLike.setImageResource(R.drawable.ufi_heart);
                            liked = !liked;
                        }
                    });
                } else {
                    like = new Like();
                    like.put(Like.KEY_USER, ParseUser.getCurrentUser());
                    like.put(Like.KEY_POST, pCurr);
                    like.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Issue with liking", e);
                                Toast.makeText(DetailsActivity.this, "Error: Unable to like", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(DetailsActivity.this, "liked", Toast.LENGTH_SHORT).show();
                            ibLike.setImageResource(R.drawable.ufi_heart_active);
                            liked = !liked;
                        }
                    });
                }
            }
        });
    }
}