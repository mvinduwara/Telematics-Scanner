// app/src/main/java/com/example/telematicsscanner/MainActivity.java
package com.example.telematicsscanner.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.telematicsscanner.R;
import com.example.telematicsscanner.fragments.DashboardFragment;
import com.example.telematicsscanner.fragments.DiagnosticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load the DashboardFragment by default when the app opens
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment()).commit();
        }

        // Handle navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_diagnostics) {
                selectedFragment = new DiagnosticsFragment(); // <-- Uncommented!
            } else if (itemId == R.id.nav_history) {
                // selectedFragment = new HistoryFragment();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }
}