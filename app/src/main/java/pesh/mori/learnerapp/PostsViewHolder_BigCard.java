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

public class PostsViewHolder_BigCard extends RecyclerView.ViewHolder {
    View mView;

    public PostsViewHolder_BigCard(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setTitle(String title){
        TextView txtTitle = (TextView)mView.findViewById(R.id.txt_title_big) ;
        txtTitle.setText(title);
    }
    public void setTimestamp(String timestamp){
        TextView txtTime = (TextView)mView.findViewById(R.id.txt_time_big);
        txtTime.setText(timestamp);
    }
    public void setAudioImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_thumbnail_big);
        imgUpload.setImageResource(R.drawable.preview_audio_hi_res);
    }
    public void setVideoImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_thumbnail_big);
        imgUpload.setImageResource(R.drawable.preview_video_hi_res);
    }
    public void setImageImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_thumbnail_big);
        imgUpload.setImageResource(R.drawable.preview_image_hi_res);
    }
    public void setDocImage(){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_thumbnail_big);
        imgUpload.setImageResource(R.drawable.preview_doc_hi_res);
    }
    public void setThumbnail(Context ctx, String imageThumb){
        ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_thumbnail_big);
        Picasso.with(ctx).load(imageThumb).into(imgUpload);
    }
    public void setAuthorImage(Context ctx, String image){
        ImageView imgAuthor = (ImageView)mView.findViewById(R.id.img_author_big);
        Picasso.with(ctx).load(image).into(imgAuthor);
    }
    public void setAuthorImage(){
        ImageView imgAuthor = (ImageView)mView.findViewById(R.id.img_author_big);
        imgAuthor.setImageResource(R.drawable.ic_baseline_person_24_theme);
    }
    public void setAdminImage(){
        ImageView imgAuthor = (ImageView)mView.findViewById(R.id.img_author_big);
        imgAuthor.setImageResource(R.drawable.ic_baseline_person_24_theme);
    }
    public void setAuthorName(String name){
        TextView txtAuthorName = (TextView)mView.findViewById(R.id.txt_author_big);
        txtAuthorName.setText(name);
    }
    public void setNull(){
        TextView txtEmpty = mView.findViewById(R.id.txt_upload_empty);
        txtEmpty.setText(R.string.info_no_posts_to_display);
    }
}
