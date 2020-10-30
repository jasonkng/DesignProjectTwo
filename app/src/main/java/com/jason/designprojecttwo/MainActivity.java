package com.jason.designprojecttwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jason.designprojecttwo.Fragment.HomeFragment;
import com.jason.designprojecttwo.Fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        loadFragment(new HomeFragment());
    }
    
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = 
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break; 
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            break;
                            //Add more cases here if more fragments created
                    }
                    return loadFragment(selectedFragment);
                }
            };

    private boolean loadFragment(Fragment selectedFragment) {
        if  (selectedFragment!=null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
        return false;
    }
}