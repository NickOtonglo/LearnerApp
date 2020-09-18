package pesh.mori.learnerapp;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

/**
 * Created by Nick Otto on 19/06/2019.
 */

public class PostsViewHolder_SmallCard extends RecyclerView.ViewHolder {
    View mView;

    public PostsViewHolder_SmallCard(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setTitle(String title){
        TextView txtTitle = (TextView)mView.findViewById(R.id.txt_title_downl) ;
        txtTitle.setText(title);
    }
    public void setTimestamp(String timestamp){
        TextView txtTime = (TextView)mView.findViewById(R.id.txt_time_downl);
        txtTime.setText(timestamp);
    }
    public void setAudioImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
        imgUpload.setImageResource(R.drawable.preview_audio);
    }
    public void setVideoImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
        imgUpload.setImageResource(R.drawable.preview_video);
    }
    public void setImageImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
        imgUpload.setImageResource(R.drawable.preview_image);
    }
    public void setNull(){
        TextView txtEmpty = mView.findViewById(R.id.txt_upload_empty);
        txtEmpty.setText(R.string.info_no_posts_to_display);
    }
    public void setDocImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
        imgUpload.setImageResource(R.drawable.preview_doc);
    }
    public void setThumbnail(Context ctx, String imageThumb){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
        Picasso.with(ctx).load(imageThumb).into(imgUpload);
    }
}
