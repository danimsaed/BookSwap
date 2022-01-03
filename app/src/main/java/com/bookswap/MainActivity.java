package com.bookswap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bookswap.Fragments.HomeFragment;
import com.bookswap.Fragments.ProfileFragment;
import com.bookswap.Model.User;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    FloatingActionButton addPost;

    private GpsTracker gpsTracker;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private String address="";
    private String city="";

    public  String Uname="";
    public  String ProfileUrl="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        Toolbar toolbar = findViewById ( R.id.toolbar );
        setSupportActionBar ( toolbar );
        getSupportActionBar ().setTitle ( "" );


        addPost = findViewById ( R.id.addPost );

        addPost.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent ( getApplicationContext (),AddBookActivity.class);
                intent.putExtra ( "address",address );
                intent.putExtra ( "city",city );
                intent.putExtra ( "latitude",String.valueOf (latitude) );
                intent.putExtra ( "longitude",String.valueOf ( longitude ) );
                startActivity ( intent );
            }
        } );

        profile_image = findViewById ( R.id.profile_image );
        username = findViewById ( R.id.username );

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        reference = FirebaseDatabase.getInstance ().getReference ("Users").child ( firebaseUser.getUid () );

        updateLocationData ( address,city,String.valueOf (latitude),String.valueOf (longitude));

        reference.addValueEventListener ( new ValueEventListener () {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue ( User.class );
                username.setText ( Objects.requireNonNull ( user ).getUsername ()+" / "+address+", "+city);

                Uname = user.getUsername ();
                ProfileUrl = user.getImageURL ();


                if(user.getImageURL ().equals ( "default" ))
                {
                    profile_image.setImageResource ( R.drawable.userphoto);
                }
                else
                {
                    Glide.with ( getApplicationContext () ).load ( user.getImageURL () ).placeholder ( R.drawable.new_loader ).into ( profile_image );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        TabLayout tabLayout = findViewById ( R.id.tab_layout );
        ViewPager viewPager = findViewById ( R.id.viewpager );

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter ( getSupportFragmentManager () );

        viewPagerAdapter.addFragment ( new HomeFragment (),"Home" );
        viewPagerAdapter.addFragment ( new ProfileFragment (),"Profile" );

        viewPager.setAdapter ( viewPagerAdapter );

        tabLayout.setupWithViewPager ( viewPager );




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ().inflate ( R.menu.menu ,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId ())
        {
            case R.id.logout :
                FirebaseAuth.getInstance ().signOut ();
                //add this flag to save app from crash
                is_online ( "offline" );
                startActivity ( new Intent ( getApplicationContext (),StartActivity.class ).setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP ) );
                return true;

            case R.id.refresh :
                startActivity(getIntent());
                return true;
        }
        return  false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super ( fm );
            this.fragments = new ArrayList<> (  );
            this.titles = new ArrayList<> (  );

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get ( position );
        }

        @Override
        public int getCount() {
            return fragments.size ();
        }

        public void  addFragment(Fragment fragment,String title)
        {
            fragments.add ( fragment );
            titles.add ( title );
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get ( position );
        }
    }

    private void is_online(String is_online)
    {
        reference  = FirebaseDatabase.getInstance ().getReference ("Users").child ( firebaseUser.getUid () );

        HashMap<String,Object> hashMap = new HashMap<> (  );
        hashMap.put ( "is_online",is_online );
        reference.updateChildren ( hashMap );
    }

    private void updateLocationData(String address,String city,String latitude,String longitude)
    {

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        reference  = FirebaseDatabase.getInstance ().getReference ("Users").child ( firebaseUser.getUid () );

        HashMap<String,Object> hashMap = new HashMap<> (  );
        hashMap.put ( "address",address );
        hashMap.put ( "city",city );
        hashMap.put ( "latitude",String.valueOf (latitude) );
        hashMap.put ( "longitude",String.valueOf (longitude));
        reference.updateChildren ( hashMap );
        UserData.USERNAME = Uname;
        UserData.PROFILEURL = ProfileUrl;
        UserData.LATITUDE = String.valueOf ( latitude );
        UserData.LONGITUDE = String.valueOf ( longitude );
        UserData.ADDRESS =  address;
        UserData.CITY = city;
    }

    @Override
    protected void onResume() {
        super.onResume ();
        getLatitudeLongitude ();
        try {
            getLocation ( latitude,longitude );
        } catch (IOException e) {
            e.printStackTrace ();
        }
        is_online ( "online" );
        updateLocationData ( address,city,String.valueOf (latitude),String.valueOf (longitude));
        UserData.USERNAME = Uname;
        UserData.PROFILEURL = ProfileUrl;
        UserData.LATITUDE = String.valueOf ( latitude );
        UserData.LONGITUDE = String.valueOf ( longitude );
        UserData.ADDRESS =  address;
        UserData.CITY = city;

    }

    @Override
    protected void onPause() {
        super.onPause ();
        is_online ( "offline" );
    }

    @Override
    protected void onStart() {
        super.onStart ();
        getLatitudeLongitude ();
        try {
            getLocation ( latitude,longitude );
        } catch (IOException e) {
            e.printStackTrace ();
        }
        updateLocationData ( address,city,String.valueOf (latitude),String.valueOf (longitude));
        UserData.USERNAME = Uname;
        UserData.PROFILEURL = ProfileUrl;
        UserData.LATITUDE = String.valueOf ( latitude );
        UserData.LONGITUDE = String.valueOf ( longitude );
        UserData.ADDRESS =  address;
        UserData.CITY = city;
    }

    public void getLatitudeLongitude(){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    private void getLocation(double latitude,double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);

        if(!addresses.isEmpty ())
        {
            String addr = addresses.get(0).getSubLocality ();
            String ct = addresses.get(0).getLocality();

            address = addr;
            city = ct;
        }


    }


    public static class UserData
    {
        public static String USERNAME="";

        public static String PROFILEURL="";

        public static String LATITUDE="";

        public static String LONGITUDE="";

        public static String ADDRESS="";

        public static String CITY="";
    }
}
