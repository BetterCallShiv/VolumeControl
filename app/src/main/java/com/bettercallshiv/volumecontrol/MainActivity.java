package com.bettercallshiv.volumecontrol;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.Slider;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String THEME_PREF = "theme_pref";
    private static final String IS_DARK_THEME = "is_dark_theme";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout volumeControlsContainer;
    private MaterialCardView permissionWarningCard;
    private AudioManager audioManager;
    private NotificationManager notificationManager;
    private SharedPreferences sharedPreferences;
    private List<VolumeStream> volumeStreams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(THEME_PREF, MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean(IS_DARK_THEME, false);
        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        initializeAudioManager();
        setupVolumeStreams();
        checkPermissions();
        createVolumeControls();
        setupThemeToggle();
    }
    

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        volumeControlsContainer = findViewById(R.id.volume_controls_container);
        permissionWarningCard = findViewById(R.id.permission_warning_card);
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.grant_permission_btn).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });
    }
    

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title));
        }
    }
    

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.drawer_open, R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    

    private void initializeAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    

    private void setupVolumeStreams() {
        volumeStreams = new ArrayList<>();
        volumeStreams.add(new VolumeStream(getString(R.string.media_voip_volume_name), getString(R.string.media_voip_volume_desc), 
            AudioManager.STREAM_MUSIC, R.drawable.ic_play_arrow));
        volumeStreams.add(new VolumeStream(getString(R.string.phone_calls_volume_name), getString(R.string.phone_calls_volume_desc), 
            AudioManager.STREAM_VOICE_CALL, R.drawable.ic_phone));
        volumeStreams.add(new VolumeStream(getString(R.string.ringtone_volume_name), getString(R.string.ringtone_volume_desc), 
            AudioManager.STREAM_RING, R.drawable.ic_phone));
        volumeStreams.add(new VolumeStream(getString(R.string.notification_volume_name), getString(R.string.notification_volume_desc), 
            AudioManager.STREAM_NOTIFICATION, R.drawable.ic_notifications));
        volumeStreams.add(new VolumeStream(getString(R.string.alarm_volume_name), getString(R.string.alarm_volume_desc), 
            AudioManager.STREAM_ALARM, R.drawable.ic_alarm));
        volumeStreams.add(new VolumeStream(getString(R.string.system_volume_name), getString(R.string.system_volume_desc), 
            AudioManager.STREAM_SYSTEM, R.drawable.ic_settings));
    }
    

    private void checkPermissions() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasPermission = notificationManager.isNotificationPolicyAccessGranted();
        }
        permissionWarningCard.setVisibility(hasPermission ? View.GONE : View.VISIBLE);
    }
    

    private void createVolumeControls() {
        volumeControlsContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (VolumeStream stream : volumeStreams) {
            View cardView = inflater.inflate(R.layout.volume_card, volumeControlsContainer, false);
            setupVolumeCard(cardView, stream);
            volumeControlsContainer.addView(cardView);
        }
    }
    

    private void setupVolumeCard(View cardView, VolumeStream stream) {
        ImageView icon = cardView.findViewById(R.id.volume_icon);
        TextView name = cardView.findViewById(R.id.volume_name);
        TextView description = cardView.findViewById(R.id.volume_description);
        TextView percentage = cardView.findViewById(R.id.volume_percentage);
        Slider slider = cardView.findViewById(R.id.volume_slider);
        icon.setImageResource(stream.getIconRes());
        name.setText(stream.getName());
        description.setText(stream.getDescription());
        cardView.setAlpha(0f);
        cardView.setTranslationY(50f);
        cardView.animate().alpha(1f).translationY(0f).setDuration(300).setStartDelay(volumeStreams.indexOf(stream) * 50L).start();
        int maxVolume = audioManager.getStreamMaxVolume(stream.getStreamType());
        int currentVolume = audioManager.getStreamVolume(stream.getStreamType());
        int volumePercent = maxVolume > 0 ? Math.round((currentVolume * 100f) / maxVolume) : 0;
        slider.setValue(volumePercent);
        updateVolumePercentage(percentage, volumePercent);
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            if (fromUser) {
                int newVolume = Math.round((value / 100f) * maxVolume);
                audioManager.setStreamVolume(stream.getStreamType(), newVolume, 0);
                updateVolumePercentage(percentage, Math.round(value));
                performHapticFeedback();
                percentage.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
                    .withEndAction(() -> percentage.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
            }
        });
        slider.addOnSliderTouchListener(new com.google.android.material.slider.Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(com.google.android.material.slider.Slider slider) {
                cardView.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start();
            }
            @Override
            public void onStopTrackingTouch(com.google.android.material.slider.Slider slider) {
                cardView.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        });
    }
    

    private void updateVolumePercentage(TextView percentage, int value) {
        int colorRes = value == 0 ? android.R.color.darker_gray :
                      value >= 70 ? R.color.slider_primary_variant :
                      R.color.slider_primary;
        percentage.setTextColor(getResources().getColor(colorRes, getTheme()));
        percentage.setText(String.format("%d%%", value));
    }
    

    private void performHapticFeedback() {
        try {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(10, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(10);
                }
            }
        } catch (Exception ignored) {
        }
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
        refreshVolumeControls();
    }
    

    private void refreshVolumeControls() {
        for (int i = 0; i < volumeControlsContainer.getChildCount(); i++) {
            View cardView = volumeControlsContainer.getChildAt(i);
            VolumeStream stream = volumeStreams.get(i);
            TextView percentage = cardView.findViewById(R.id.volume_percentage);
            Slider slider = cardView.findViewById(R.id.volume_slider);
            int maxVolume = audioManager.getStreamMaxVolume(stream.getStreamType());
            int currentVolume = audioManager.getStreamVolume(stream.getStreamType());
            int volumePercent = maxVolume > 0 ? Math.round((currentVolume * 100f) / maxVolume) : 0;
            slider.setValue(volumePercent);
            updateVolumePercentage(percentage, volumePercent);
        }
    }
    

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_share) {
            shareApp();
        } else if (itemId == R.id.nav_contact) {
            contactUs();
        } else if (itemId == R.id.nav_github) {
            openGitHub();
        } else if (itemId == R.id.nav_about) {
            showAboutDialog();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_chooser)));
    }
    

    private void contactUs() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(android.net.Uri.parse(getString(R.string.email_mailto)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_email_body));
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_email_chooser)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.no_email_app_toast), 
                          Toast.LENGTH_LONG).show();
        }
    }
    

    private void openGitHub() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
            android.net.Uri.parse(getString(R.string.github_full_url)));
        try {
            startActivity(browserIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.no_browser_toast), 
                          Toast.LENGTH_LONG).show();
        }
    }
    

    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        com.google.android.material.button.MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);
        com.google.android.material.button.MaterialButton btnGitHub = dialogView.findViewById(R.id.btn_github);
        btnOk.setOnClickListener(v -> dialog.dismiss());
        btnGitHub.setOnClickListener(v -> { openGitHub(); dialog.dismiss(); });
        dialog.show();
    }
    

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    

    private void setupThemeToggle() {
        View headerView = navigationView.getHeaderView(0);
        android.widget.ImageButton themeToggleBtn = headerView.findViewById(R.id.theme_toggle_btn);
        updateThemeIcon(themeToggleBtn);
        themeToggleBtn.setOnClickListener(v -> toggleTheme());
    }
    

    private void updateThemeIcon(android.widget.ImageButton themeToggleBtn) {
        boolean isDarkTheme = sharedPreferences.getBoolean(IS_DARK_THEME, false);
        themeToggleBtn.setImageResource(isDarkTheme ? R.drawable.ic_sun : R.drawable.ic_moon);
    }
    

    private void toggleTheme() {
        boolean newTheme = !sharedPreferences.getBoolean(IS_DARK_THEME, false);
        sharedPreferences.edit().putBoolean(IS_DARK_THEME, newTheme).apply();
        AppCompatDelegate.setDefaultNightMode(newTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
    }
    

    private static class VolumeStream {
        private final String name;
        private final String description;
        private final int streamType;
        private final int iconRes;
        public VolumeStream(String name, String description, int streamType, int iconRes) {
            this.name = name;
            this.description = description;
            this.streamType = streamType;
            this.iconRes = iconRes;
        }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getStreamType() { return streamType; }
        public int getIconRes() { return iconRes; }
    }
}