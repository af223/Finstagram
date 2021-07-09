package com.codepath.finstagram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.finstagram.DetailsActivity;
import com.codepath.finstagram.R;
import com.codepath.finstagram.models.Post;
import com.parse.ParseFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This adapter is for the Recycler View in ProfileFragment, where each view holder
 * displays the image that the user posted in a grid layout.
 */

public class PicsAdapter extends RecyclerView.Adapter<PicsAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> posts;

    public PicsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pic, parent, false);
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

        private final ImageView ivPic;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivPic = itemView.findViewById(R.id.ivPic);
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPic);
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
