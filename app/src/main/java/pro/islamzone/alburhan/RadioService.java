package pro.islamzone.alburhan;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.*;
//import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
//import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
/**
 * Created by Alf on 7/5/2017.
 */

public class RadioService extends Service {
	private static boolean playing = false;
	    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
			    private static SimpleExoPlayer rPlayer;
				 private PowerManager.WakeLock wl;
				 private PowerManager pm;
    private BandwidthMeter bandwidthMeter;
    private ExtractorsFactory extractorsFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private TrackSelector trackSelector;
    private DefaultBandwidthMeter defaultBandwidthMeter;
    private DataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;
    //private static MediaPlayer player;
    private String station;
    private Context context;

    public static boolean isPlaying(){
		return playing;
				    }

    @Override
    public void onCreate() {
		     pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        super.onCreate();
        context = getApplicationContext();
		


    }

    /**
     * IMPORTANT!!! Remember to:
     * intent.putExtra(context.getString(R.string.station_path_string), "blablabla.m3u8");
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: Backup Alarm in case of null intent or no internet.
        if(intent == null) Log.e ("intent: ", "null");
        else {
            String newStation = intent.getStringExtra(context.getString(R.string.station_path_string));

                stopPlayer();
                station = newStation;
                startPlayer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startPlayer(){

                if(station != null) {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(new Intent(context.getString(R.string.loading_station_filter))
                                    .putExtra(context.getString(R.string.loading_station_boolean),true));

                    stopPlayer();
		        bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();
        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
		        defaultBandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Alburhan mobile app"), defaultBandwidthMeter);
        DefaultAllocator allocator = new DefaultAllocator(true, 64*1024);
LoadControl loadControl = new DefaultLoadControl(allocator, 30000, 30000, 10000, 10000);
        mediaSource = new ExtractorMediaSource(Uri.parse(station), dataSourceFactory, extractorsFactory, null, null);
        rPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        rPlayer.prepare(mediaSource);                    
		//wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");            
					                    rPlayer.setPlayWhenReady(true);
playing=true;
                                lbm.sendBroadcast(new Intent(getResources().getString(R.string.play_started_filter)));
                    lbm.sendBroadcast(new Intent(getResources().getString(R.string.loading_station_filter))
                                    .putExtra(getResources().getString(R.string.loading_station_boolean),false));

                }

    }

    private void stopPlayer(){
        if(rPlayer != null && rPlayer.getPlayWhenReady()) {
            rPlayer.setPlayWhenReady(false);
			playing=false;
			            //wl.release();
            rPlayer.release();
            rPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
    }
}
