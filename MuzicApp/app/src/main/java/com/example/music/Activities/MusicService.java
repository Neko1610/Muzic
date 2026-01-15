package com.example.music.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.music.Models.Song;
import com.example.music.MusicStateManager;
import com.example.music.R;

import java.util.concurrent.ExecutionException;

public class MusicService extends Service {

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_UPDATE_PROGRESS = "ACTION_UPDATE_PROGRESS";
    public static final String ACTION_SEEK = "ACTION_SEEK";

    private static final String CHANNEL_ID = "music_channel";
    private static MusicService instance;
    private MediaPlayer mediaPlayer;
    private Song currentSong;

    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    Intent intent = new Intent(ACTION_UPDATE_PROGRESS);
                    intent.putExtra("current", mediaPlayer.getCurrentPosition());
                    intent.putExtra("total", mediaPlayer.getDuration());
                    sendBroadcast(intent);
                    progressHandler.postDelayed(this, 1000);
                }
            } catch (Exception e) {
                Log.e("MusicService", "Error updating progress: " + e.getMessage());
            }
        }
    };

    public static MusicService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();
        Log.d("MusicService", "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        progressHandler.removeCallbacks(progressRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.d("MusicService", "Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;
        String action = intent.getAction();
        Log.d("MusicService", "Action received: " + action);

        switch (action) {
            case ACTION_PLAY:
                Song song = (Song) intent.getSerializableExtra("song");
                if (song != null) playSong(song);
                break;
            case ACTION_PAUSE:
                pauseSong();
                break;
            case ACTION_RESUME:
                resumeSong();
                break;
            case ACTION_STOP:
                stopSong();
                break;
            case ACTION_SEEK:
                int pos = intent.getIntExtra("position", 0);
                seekTo(pos);
                break;
        }
        return START_STICKY;
    }

    private void playSong(Song song) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(song.getMp3Url());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                currentSong = song;
                MusicStateManager.getInstance().setSong(song);
                MusicStateManager.getInstance().setPlaying(true);
                progressHandler.post(progressRunnable);

                showNotification(song, true);
                Log.d("MusicService", "Playing: " + song.getTitle());
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Intent doneIntent = new Intent(ACTION_UPDATE_PROGRESS);
                doneIntent.putExtra("completed", true);
                sendBroadcast(doneIntent);
                MusicStateManager.getInstance().setPlaying(false);
            });

        } catch (Exception e) {
            Log.e("MusicService", "Error playing song: " + e.getMessage());
        }
    }

    private void pauseSong() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                MusicStateManager.getInstance().setPlaying(false);
                showNotification(currentSong, false);
            }
        } catch (Exception e) {
            Log.e("MusicService", "pauseSong error: " + e.getMessage());
        }
    }

    private void resumeSong() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                MusicStateManager.getInstance().setPlaying(true);
                showNotification(currentSong, true);
            }
        } catch (Exception e) {
            Log.e("MusicService", "resumeSong error: " + e.getMessage());
        }
    }

    private void stopSong() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            progressHandler.removeCallbacks(progressRunnable);
            stopForeground(true);
            stopSelf();
            Log.d("MusicService", "Music stopped");
        } catch (Exception e) {
            Log.e("MusicService", "stopSong error: " + e.getMessage());
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Song song, boolean isPlaying) {
        try {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(this)
                        .asBitmap()
                        .load(song.getCoverUrl())
                        .submit()
                        .get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("MusicService", "Load image failed: " + e.getMessage());
            }

            // Actions
            PendingIntent playPending = PendingIntent.getService(
                    this, 0,
                    new Intent(this, MusicService.class)
                            .setAction(isPlaying ? ACTION_PAUSE : ACTION_RESUME),
                    PendingIntent.FLAG_IMMUTABLE
            );
            PendingIntent stopPending = PendingIntent.getService(
                    this, 1,
                    new Intent(this, MusicService.class).setAction(ACTION_STOP),
                    PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(song.getTitle())
                    .setContentText(song.getArtist())
                    .setSmallIcon(R.drawable.icon)
                    .setLargeIcon(bitmap)
                    .addAction(isPlaying ? R.drawable.ic_stop : R.drawable.ic_play,
                            isPlaying ? "Pause" : "Play", playPending)
                    .addAction(R.drawable.x, "Close", stopPending)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1))
                    .setOnlyAlertOnce(true)
                    .setOngoing(isPlaying)
                    .build();

            startForeground(1, notification);
        } catch (Exception e) {
            Log.e("MusicService", "showNotification error: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
