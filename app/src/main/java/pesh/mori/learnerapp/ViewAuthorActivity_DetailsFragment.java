package pesh.mori.learnerapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Nick Otto on 14/06/2019.
 */

public class ViewAuthorActivity_DetailsFragment extends Fragment {

    private TextView mTextMessage,mName,mAbout,mFacebook,mGender,mLinkedin,mSkilldetails,mSkillsector,mTwitter;
    private DatabaseReference mBio;
    private String mAuthor,txtName;
    private DatabaseReference mUsers;
    private Uri mFileUri = null;
    private CircleImageView profile;
    private ProgressBar mProgressBar;
    private LinearLayout layoutInfo;

    public ViewAuthorActivity_DetailsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View detailsFragment = inflater.inflate(R.layout.fragment_author_details,container,false);

        mAuthor = ((ViewAuthorActivity)getActivity()).getAuthorId();

        mTextMessage = (TextView) detailsFragment.findViewById(R.id.message);

        mProgressBar = detailsFragment.findViewById(R.id.progress_bar);
        mName = (TextView) detailsFragment.findViewById(R.id.author_name);
        mFacebook = (TextView) detailsFragment.findViewById(R.id.txt_view_facbook);
        mGender = (TextView) detailsFragment.findViewById(R.id.author_gender);
        mAbout = (TextView) detailsFragment.findViewById(R.id.txt_view_about);
        mSkilldetails = (TextView) detailsFragment.findViewById(R.id.txt_view_occupation);
        mSkillsector= (TextView) detailsFragment.findViewById(R.id.txt_view_skill);
        mLinkedin = (TextView) detailsFragment.findViewById(R.id.txt_view_linkedin);
        mTwitter = (TextView) detailsFragment.findViewById(R.id.txt_view_twitter);
        profile = (CircleImageView) detailsFragment.findViewById(R.id.img_main_profile);
        layoutInfo = (LinearLayout) detailsFragment.findViewById(R.id.layout_info_section);

        preloadBio();

        return detailsFragment;
    }

    public void preloadBio(){
        mBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuthor);
        mBio.keepSynced(true);
        mBio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("hidden").exists() && dataSnapshot.child("hidden").getValue().equals("false")){
                        showInfo(dataSnapshot);
                    } else if (dataSnapshot.child("hidden").exists() && dataSnapshot.child("hidden").getValue().equals("true")){
                        hideInfo();
                    } else if (!dataSnapshot.child("hidden").exists()){
                        showInfo(dataSnapshot);
                    }
                } else {
                    hideInfo();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor);
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                if (!dataSnapshot.child("account_manager").exists()){
                    String profilePicture = String.valueOf(dataSnapshot.child("profile_picture").getValue());
                    if (!profilePicture.equals("")) {
                        mFileUri = Uri.parse(profilePicture);
                        Picasso.with(getContext()).load(mFileUri).into(profile);
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        profile.setImageResource(R.drawable.ic_baseline_person_24_theme);
                        mProgressBar.setVisibility(View.GONE);
                    }
                } else {
                    checkAccountManager(String.valueOf(dataSnapshot.child("account_manager").getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showInfo(DataSnapshot dataSnapshot){
        layoutInfo.setVisibility(View.VISIBLE);
        mAbout.setText(String.valueOf(dataSnapshot.child("about").getValue()));
        mFacebook.setText(String.valueOf(dataSnapshot.child("facebook").getValue()));
        mLinkedin.setText(String.valueOf(dataSnapshot.child("linkedin").getValue()));
        mGender.setVisibility(View.GONE);
//        mGender.setText("gender: "+ String.valueOf(dataSnapshot.child("gender").getValue()));
        mSkilldetails.setText(String.valueOf(dataSnapshot.child("skill_details").getValue()));
        mTwitter.setText(String.valueOf(dataSnapshot.child("twitter").getValue()));
        mSkillsector.setText(String.valueOf(dataSnapshot.child("skills_sector").getValue()));
    }

    private void hideInfo(){
        layoutInfo.setVisibility(View.GONE);
        mAbout.setVisibility(View.GONE);
        mFacebook.setVisibility(View.GONE);
        mLinkedin.setVisibility(View.GONE);
        mGender.setText(R.string.info_no_user_bio);
        mGender.setVisibility(View.VISIBLE);
        mSkilldetails.setVisibility(View.GONE);
        mTwitter.setVisibility(View.GONE);
        mSkillsector.setVisibility(View.GONE);
    }

    public void checkAccountManager(final String manager){
        if (manager.equals(getString(R.string.firebase_ref_users_account_manager))){
            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mName.setText(String.valueOf(dataSnapshot.child("fname").getValue()) + " " + (dataSnapshot.child("lname").getValue()));
                    String profilePicture = String.valueOf(dataSnapshot.child("profile_picture").getValue());
                    if (!profilePicture.equals("")) {
                        mFileUri = Uri.parse(profilePicture);
                        Picasso.with(getContext()).load(mFileUri).into(profile);
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        profile.setImageResource(R.drawable.ic_baseline_person_24_theme);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (manager.equals("facebook")){
            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Picasso.with(getContext()).load(String.valueOf(new URL(String.valueOf(dataSnapshot.child("profile_picture").getValue())))).into(profile);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (manager.equals("google")){
            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Picasso.with(getContext()).load(String.valueOf(new URL(String.valueOf(dataSnapshot.child("profile_picture").getValue())))).into(profile);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
