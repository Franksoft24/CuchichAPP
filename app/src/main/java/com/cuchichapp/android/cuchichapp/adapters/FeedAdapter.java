package com.cuchichapp.android.cuchichapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cuchichapp.android.cuchichapp.PostDetailsActivity;
import com.cuchichapp.android.cuchichapp.R;
import com.cuchichapp.android.cuchichapp.model.Post;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyRecycleFeedViewHolder> {

    private final List<Post> post;
    private final List<String> key;
    private final Context context;

    public FeedAdapter(Context context, List<Post> post, List<String> key){
        this.post = post;
        this.context = context;
        this.key = key;
    }

    @NonNull
    @Override
    public FeedAdapter.MyRecycleFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent
            , int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_item,
                parent,
                false);

        MyRecycleFeedViewHolder holder = new MyRecycleFeedViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedAdapter.MyRecycleFeedViewHolder holder
            , int position) {
        holder.TitleTV.setText(this.post.get(getItemCount()-(position+1)).MyPostTitle);
        holder.ContentTV.setText(this.post.get(getItemCount()-(position+1)).MyPostContent);
        Glide.with(context)
                .load(this.post.get(getItemCount()-(position+1)).Img)
                .into(holder.imageView);


        holder.WatchMore.setOnClickListener((v -> {

            Intent intent = new Intent(context,PostDetailsActivity.class);
            intent.putExtra("Key",key.get(getItemCount()-(position+1)));
            context.startActivity(intent);

        }));

    }

    @Override
    public int getItemCount() {
        return post.size();
    }

    public class MyRecycleFeedViewHolder extends RecyclerView.ViewHolder{

        TextView TitleTV, ContentTV;
        ImageView imageView;
        Button WatchMore;

        public MyRecycleFeedViewHolder(View itemView) {
            super(itemView);

            this.ContentTV = (TextView) itemView.findViewById(R.id.ContentTextView);
            this.TitleTV = (TextView) itemView.findViewById(R.id.TitleTextView);
            this.imageView = (ImageView) itemView.findViewById(R.id.PostImageView);
            this.WatchMore = (Button) itemView.findViewById(R.id.WatchMoreCTA);

        }
    }

}
