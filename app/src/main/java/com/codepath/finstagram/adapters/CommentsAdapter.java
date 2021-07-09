package com.codepath.finstagram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.finstagram.R;
import com.codepath.finstagram.models.Comment;
import com.codepath.finstagram.models.Post;
import com.parse.ParseFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final Context context;
    private final List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @NotNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentsAdapter.ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCommenterPFP;
        private final TextView tvCommenterName;
        private final TextView tvComment;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivCommenterPFP = itemView.findViewById(R.id.ivCommenterPFP);
            tvCommenterName = itemView.findViewById(R.id.tvCommenterName);
            tvComment = itemView.findViewById(R.id.tvComment);
        }

        public void bind(Comment comment) {
            ParseFile pfp = comment.getUser().getParseFile(Post.KEY_PFP);
            if (pfp != null) {
                Glide.with(context).load(pfp.getUrl()).transform(new CircleCrop()).into(ivCommenterPFP);
            }
            tvCommenterName.setText(comment.getUser().getUsername());
            tvComment.setText(comment.getComment());
        }
    }
}
