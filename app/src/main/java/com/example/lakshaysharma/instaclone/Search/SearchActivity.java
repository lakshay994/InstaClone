package com.example.lakshaysharma.instaclone.Search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.Profile.ProfileActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.example.lakshaysharma.instaclone.Utils.SearchArrayAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private final Context mContext = SearchActivity.this;
    private static final int ACTIVITY_INDEX = 1;

    private EditText mSearch;
    private ListView mSearchList;

    private List<User> mUserList;
    private SearchArrayAdapter searchArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchList = findViewById(R.id.searchListView);
        mSearch = findViewById(R.id.searchEditText);

        hideKeyboard();
        setupBottomNavigation();
        initTextListener();
    }



    private void initTextListener(){

        mUserList = new ArrayList<>();

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String keyword = mSearch.getText().toString().toLowerCase(Locale.getDefault()).trim();
                searchForUser(keyword);
            }
        });

    }



    private void searchForUser(String keyword){

        Log.d(TAG, "searchForUser: searching for the keyword: " + keyword);
        mUserList.clear();

        if (TextUtils.isEmpty(keyword)){

        }
        else {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.users))
                    .orderByChild(getString(R.string.username))
                    .equalTo(keyword);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        Log.d(TAG, "onDataChange: found a user: " + data.getValue(User.class).getUsername());

                        mUserList.add(data.getValue(User.class));
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: Updating the search list");

        searchArrayAdapter = new SearchArrayAdapter(SearchActivity.this, R.layout.layout_user_listitem, mUserList);
        mSearchList.setAdapter(searchArrayAdapter);

        mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemClick: navigating to profile activity");
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_class), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);
            }
        });

    }

    private void hideKeyboard(){

        if (getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

    }

    /*set up the navigation animations*/
    public void setupBottomNavigation(){

        BottomNavigationViewEx navigation = findViewById(R.id.bottom_nav);

        Log.d(TAG, "setupBottomNavigation: Setting Up the Navigation Bar");
        BottomNavHelper.setupNav(navigation);

        Log.d(TAG, "setupBottomNavigation: Setting Up Nav Bar Highlight Function");
        BottomNavHelper.enableNavigation(mContext, this, navigation);

        //get the menu item position for highlighting
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_INDEX);
        menuItem.setChecked(true);
    }
}
