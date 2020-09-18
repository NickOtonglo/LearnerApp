package pesh.mori.learnerapp;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class PostsViewHolder_SubPostsCard extends RecyclerView.ViewHolder {
    View mView;

    public PostsViewHolder_SubPostsCard(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setTitle(String title){
        TextView txtTitle = (TextView)mView.findViewById(R.id.txt_title) ;
        txtTitle.setText(title);
    }
    public void setTimestamp(String timestamp){
        TextView txtTime = (TextView)mView.findViewById(R.id.txt_time);
        txtTime.setText(timestamp);
    }
    public void setType(String type){
        TextView txtTime = (TextView)mView.findViewById(R.id.txt_type);
        txtTime.setText(type);
    }
    public void setDescription(String description){
        TextView txtTime = (TextView)mView.findViewById(R.id.txt_description);
        txtTime.setText(description);
    }
}
