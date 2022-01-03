package com.bookswap.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bookswap.MainActivity;
import com.bookswap.Model.BookPost;
import com.bookswap.Model.User;
import com.bookswap.R;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPageAdapter extends PagerAdapter {

    private List<BookPost> mBookPosts;
    private LayoutInflater mLayoutInflater;
    private Context context;

    ExtendedFloatingActionButton initiate_request;


    private  String mProfileURL="";

    public ViewPageAdapter(Context context, List<BookPost> mBookPosts) {
        this.mBookPosts = mBookPosts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mBookPosts.size ();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @NonNull
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        View itemView = LayoutInflater.from ( context ).inflate ( R.layout.book_post_item,container,false );

        ImageView bookImage = itemView.findViewById ( R.id.book_image );
        ImageView profileImage = itemView.findViewById ( R.id.profile_image );

        final TextView username = itemView.findViewById ( R.id.username );
        final TextView bookName = itemView.findViewById ( R.id.book_name );
        TextView bookDesc = itemView.findViewById ( R.id.book_desc );
        TextView bookTags = itemView.findViewById ( R.id.book_tags );
        TextView time = itemView.findViewById ( R.id.time );
        TextView date = itemView.findViewById ( R.id.date );
        TextView distanceKM = itemView.findViewById ( R.id.distance );

        initiate_request = itemView.findViewById ( R.id.initiate_request );


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(mBookPosts.get(position).getUID());

        reference.addListenerForSingleValueEvent (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue (User.class);

                assert user != null;
                mProfileURL = user.getImageURL ();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );


        String mUsername = MainActivity.UserData.USERNAME;



        //logic to add nearby bookposts
        double myLatitude = Double.parseDouble(MainActivity.UserData.LATITUDE);
        double myLongitude = Double.parseDouble(MainActivity.UserData.LONGITUDE);
        double userLatitude = Double.parseDouble(mBookPosts.get(position).getLatitude());
        double userLongitude = Double.parseDouble(mBookPosts.get(position).getLongitude());

        double myDistance = distance(myLatitude, myLongitude, userLatitude, userLongitude);
        myDistance = myDistance + myDistance /10;
        distanceKM.setText ( String.format ( Locale.US, "%.2f KM  ", myDistance));


        username.setText (mUsername);
        bookName.setText ( mBookPosts.get ( position ).getBook_name () );
        bookDesc.setText ( mBookPosts.get ( position ).getBook_desc () );
        bookTags.setText ( mBookPosts.get ( position ).getBook_tags () );
        time.setText ( mBookPosts.get ( position ).getTime () );
        date.setText ( mBookPosts.get ( position ).getDate () );
        username.setText ( mBookPosts.get ( position ).getUsername () );


        //set image into image view using glide library
        Glide.with (context).load ( mBookPosts.get ( position ).getBook_imageURL () ).centerCrop ().dontTransform ().placeholder ( R.drawable.new_loader ).into(bookImage);

        if(mProfileURL.equals ( "default" ))
        {
            profileImage.setImageResource ( R.drawable.userphoto);
        }
        else
        {
            Glide.with ( context ).load (mBookPosts.get ( position ).getProfile_imageURL () ).placeholder ( R.drawable.userphoto ).into ( profileImage );
        }

        container.addView ( itemView );
        return  itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView ( (LinearLayout)object );
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);

        // distance in miles
        dist = dist * 60 * 1.1515;

        //convert into kilometer
        dist = dist * 1.609344;

        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



}
