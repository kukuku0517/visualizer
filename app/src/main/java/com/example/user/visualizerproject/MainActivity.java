package com.example.user.visualizerproject;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.example.user.visualizerproject.utils.TunnelPlayerWorkaround;
import com.example.user.visualizerproject.visualizer.VisualizerView;
import com.example.user.visualizerproject.visualizer.renderer.BarGraphRenderer;
import com.example.user.visualizerproject.visualizer.renderer.CircleBarRenderer;
import com.example.user.visualizerproject.visualizer.renderer.CircleRenderer;
import com.example.user.visualizerproject.visualizer.renderer.LineRenderer;


/**
 * Demo to show how to use VisualizerView
 */
public class MainActivity extends Activity {
    private MediaPlayer mPlayer;
    private MediaPlayer mSilentPlayer;  /* to avoid tunnel player issue */
    private VisualizerView mVisualizerView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initTunnelPlayerWorkaround();
        init();
    }

    @Override
    protected void onPause()
    {
        cleanUp();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        cleanUp();
        super.onDestroy();
    }

    public static String getFilename(String title) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, "Music");
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = (title == null | title.equals("")) ? String.valueOf(System.currentTimeMillis()) : title;
        return (file.getAbsolutePath() + "/" + fileName +
              ".wav");
    }

    private void init()
    {

        File file = new File(getFilename("베베"));
        mPlayer = MediaPlayer.create(this, Uri.parse(file.getAbsolutePath()));
        mPlayer.setLooping(true);
        mPlayer.start();

        // We need to link the visualizer view to the media player so that
        // it displays something
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        mVisualizerView.link(mPlayer);

        // Start with just line renderer
        addLineRenderer();
    }

    private void cleanUp()
    {
        if (mPlayer != null)
        {
            mVisualizerView.release();
            mPlayer.release();
            mPlayer = null;
        }

        if (mSilentPlayer != null)
        {
            mSilentPlayer.release();
            mSilentPlayer = null;
        }
    }

    // Workaround (for Galaxy S4)
    //
    // "Visualization does not work on the new Galaxy devices"
    //    https://github.com/felixpalmer/android-visualizer/issues/5
    //
    // NOTE:
    //   This code is not required for visualizing default "test.mp3" file,
    //   because tunnel player is used when duration is longer than 1 minute.
    //   (default "test.mp3" file: 8 seconds)
    //
    private void initTunnelPlayerWorkaround() {
        // Read "tunnel.decode" system property to determine
        // the workaround is needed
        if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(this)) {
            mSilentPlayer = TunnelPlayerWorkaround.createSilentMediaPlayer(this);
        }
    }

    // Methods for adding renderers to visualizer
    private void addBarGraphRenderers()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(50f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(200, 56, 138, 252));
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
        mVisualizerView.addRenderer(barGraphRendererBottom);

        Paint paint2 = new Paint();
        paint2.setStrokeWidth(50f);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.argb(200, 181, 111, 233));
        BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(16, paint2, true);
        mVisualizerView.addMyRenderer(barGraphRendererTop);

    }

    private void addCircleBarRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
        mVisualizerView.addRenderer(circleBarRenderer);
    }

    private void addCircleRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
        mVisualizerView.addRenderer(circleRenderer);
    }

    private void addLineRenderer()
    {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.argb(88, 0, 128, 255));

        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(5f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
        mVisualizerView.addRenderer(lineRenderer);
    }

    // Actions for buttons defined in xml
    public void startPressed(View view) throws IllegalStateException, IOException
    {
        if(mPlayer.isPlaying())
        {
            return;
        }
        mPlayer.prepare();
        mPlayer.start();
    }

    public void stopPressed(View view)
    {
        mPlayer.stop();
    }

    public void barPressed(View view)
    {
        addBarGraphRenderers();
    }

    public void circlePressed(View view)
    {
        addCircleRenderer();
    }

    public void circleBarPressed(View view)
    {
        addCircleBarRenderer();
    }

    public void linePressed(View view)
    {
        addLineRenderer();
    }

    public void clearPressed(View view)
    {
        mVisualizerView.clearRenderers();
    }
}