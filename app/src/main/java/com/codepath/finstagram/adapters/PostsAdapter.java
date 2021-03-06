package com.codepath.finstagram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.finstagram.DetailsActivity;
import com.codepath.finstagram.R;
import com.codepath.finstagram.models.Post;
import com.parse.ParseFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This adapter is for the Recycler View in PostsFragment, where each view holder
 * displays a post made by a user of the app. Each post contains the poster's username, the posted image,
 * and the caption.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvAuthor;
        private final ImageView ivPost;
        private final TextView tvDescription;
        private final TextView tvPostDate;
        private final ImageView ivPFP;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            ivPost = itemView.findViewById(R.id.ivPost);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            ivPFP = itemView.findViewById(R.id.ivPFP);
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            tvDescription.setText(post.getDescription());
            tvAuthor.setText(post.getUser().getUsername());
            tvPostDate.setText(Post.calculateTimeAgo(post.getCreatedAt()));
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPost);
            }
            ParseFile pfp = post.getPFP();
            if (pfp != null) {
                Glide.with(context).load(pfp.getUrl()).transform(new CircleCrop()).into(ivPFP);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                Intent i = new Intent(context, DetailsActivity.class);
                i.putExtra(Post.class.getSimpleName(), post.getObjectId());
                context.startActivity(i);
            }
        }
    }
}
