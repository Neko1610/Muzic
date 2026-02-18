package com.example.music.Service;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.example.music.Models.Song;
import com.example.music.R;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.audio.AudioAttributes;

public class MusicService extends Service {
    public static final String ACTION_UPDATE_PROGRESS = "ACTION_UPDATE_PROGRESS";

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_SEEK = "ACTION_SEEK";

    private static final String CHANNEL_ID = "music_channel";

    private ExoPlayer player;
    private Song currentSong;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        1500,   // minBufferMs
                        3000,   // maxBufferMs
                        1000,   // bufferForPlaybackMs
                        1500    // bufferForPlaybackAfterRebufferMs
                )
                .build();

        player = new ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();


        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                true
        );

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int state) {

                if (state == Player.STATE_READY) {
                    progressHandler.removeCallbacks(progressRunnable);
                    progressHandler.post(progressRunnable);
                }

                if (state == Player.STATE_ENDED) {
                    Intent doneIntent = new Intent(ACTION_UPDATE_PROGRESS);
                    doneIntent.putExtra("completed", true);
                    sendBroadcast(doneIntent);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {

                MusicStateManager.getInstance().setPlaying(isPlaying);

                if (currentSong != null) {
                    // 🔥 Chỉ update notification, không rebuild quá nhiều
                    showNotification(currentSong, isPlaying);
                }
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;

        switch (intent.getAction()) {

            case ACTION_PLAY:
                Song song = (Song) intent.getSerializableExtra("song");
                if (song != null) playSong(song);
                break;

            case ACTION_PAUSE:
                if (player != null) player.pause();
                break;

            case ACTION_RESUME:
                if (player != null) player.play();
                break;

            case ACTION_STOP:
                stopSong();
                break;

            case ACTION_SEEK:
                int pos = intent.getIntExtra("position", 0);
                if (player != null) player.seekTo(pos);
                break;
        }

        return START_STICKY;
    }

    private void playSong(Song song) {

        currentSong = song;

        MediaItem mediaItem = MediaItem.fromUri(song.getMp3Url());

        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);

        MusicStateManager.getInstance().setSong(song);

        Log.d("MusicService", "Playing: " + song.getTitle());
    }

    private void stopSong() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID,
                            "Music Player",
                            NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Song song, boolean isPlaying) {

        Glide.with(this)
                .asBitmap()
                .load(song.getCoverUrl())
                .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {

                        buildNotification(song, isPlaying, bitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}
                });
    }
    private final android.os.Handler progressHandler = new android.os.Handler();

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null) {

                Intent intent = new Intent(ACTION_UPDATE_PROGRESS);
                intent.putExtra("current", (int) player.getCurrentPosition());
                intent.putExtra("total", (int) player.getDuration());

                sendBroadcast(intent);

                if (player.isPlaying()) {
                    progressHandler.postDelayed(this, 1000);
                }
            }
        }
    };

    private void buildNotification(Song song, boolean isPlaying, Bitmap bitmap) {

        PendingIntent playPending = PendingIntent.getService(
                this, 0,
                new Intent(this, MusicService.class)
                        .setAction(isPlaying ? ACTION_PAUSE : ACTION_RESUME),
                PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent stopPending = PendingIntent.getService(
                this, 1,
                new Intent(this, MusicService.class)
                        .setAction(ACTION_STOP),
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
