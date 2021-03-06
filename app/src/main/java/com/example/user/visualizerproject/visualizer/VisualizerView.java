/**
 * Copyright 2011, Felix Palmer
 * <p>
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.example.user.visualizerproject.visualizer;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import com.example.user.visualizerproject.Recognition.MFCC;
import com.example.user.visualizerproject.visualizer.renderer.Renderer;


/**
 * A class that draws visualizations of data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture } and
 * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
 */
public class VisualizerView extends View {
    private static final String TAG = "VisualizerView";

    private byte[] mBytes;
    private byte[] mFFTBytes;
    private double[] dctDatas;
    private Rect mRect = new Rect();
    private Visualizer mVisualizer;

    private Set<Renderer> mRenderers;
    private Renderer myRenderer;

    private Paint mFlashPaint = new Paint();
    private Paint mFadePaint = new Paint();

    public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context) {
        this(context, null, 0);
    }

    private void init() {
        mBytes = null;
        mFFTBytes = null;
dctDatas=null;
        mFlashPaint.setColor(Color.argb(122, 255, 255, 255));
        mFadePaint.setColor(Color.argb(238, 255, 255, 255)); // Adjust alpha to change how quickly the image fades
        mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));

        mRenderers = new HashSet<Renderer>();
    }

    /**
     * Links the visualizer to a player
     * @param player - MediaPlayer instance to link to
     */
    public void link(MediaPlayer player) {
        if (player == null) {
            throw new NullPointerException("Cannot link to null MediaPlayer");
        }

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(player.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        // Pass through Visualizer data to VisualizerView
        Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                         int samplingRate) {
                updateVisualizerFFT(bytes);
            }
        };

        mVisualizer.setDataCaptureListener(captureListener,
                Visualizer.getMaxCaptureRate() / 2, true, true);

        // Enabled Visualizer and disable when we're done with the stream
        mVisualizer.setEnabled(true);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mVisualizer.setEnabled(false);
            }
        });
    }

    public void addRenderer(Renderer renderer) {
        if (renderer != null) {
            mRenderers.add(renderer);
        }
    }

    public void addMyRenderer(Renderer renderer) {
       myRenderer=renderer;
    }



    public void clearRenderers() {
        mRenderers.clear();
    }

    /**
     * Call to release the resources used by VisualizerView. Like with the
     * MediaPlayer it is good practice to call this method
     */
    public void release() {
        mVisualizer.release();
    }

    /**
     * Pass data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
     * @param bytes
     */
    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    /**
     * Pass FFT data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
     * @param bytes
     */

    private int numCepstra = 12;
    private static final int SAMPLING_RATE = 1020;
    private static final int SAMPLE_PER_FRAME = 256;

    public static float[] toDoubleArray(byte[] byteArray) {
        int times = Float.SIZE / Byte.SIZE;
        float[] doubles = new float[byteArray.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getFloat();
        }
        return doubles;
    }

    public float[] extractFloatDataFromAmplitudeByteArray(byte[] audioBytes) {
        float[] audioData;

//        if (format.getSampleSizeInBits() == 16) {
        int nlengthInSamples = audioBytes.length / 2;
        audioData = new float[nlengthInSamples];
//            if (format.isBigEndian()) {
//                for (int i = 0; i < nlengthInSamples; i++) {
//                    /* First byte is MSB (high order) */
//                    int MSB = audioBytes[2 * i];
//					/* Second byte is LSB (low order) */
//                    int LSB = audioBytes[2 * i + 1];
//                    audioData[i] = MSB << 8 | (255 & LSB);
//                }
//            } else {
        for (int i = 0; i < nlengthInSamples; i++) {
                    /* First byte is LSB (low order) */
            int LSB = audioBytes[2 * i];
					/* Second byte is MSB (high order) */
            int MSB = audioBytes[2 * i + 1];
            audioData[i] = MSB << 8 | (255 & LSB);
        }
//            }

//        else if (format.getSampleSizeInBits() == 8) {
//            int nlengthInSamples = audioBytes.length;
//            audioData = new float[nlengthInSamples];
//            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
//                for (int i = 0; i < audioBytes.length; i++) {
//                    audioData[i] = audioBytes[i];
//                }
//            } else {
//                for (int i = 0; i < audioBytes.length; i++) {
//                    audioData[i] = audioBytes[i] - 128;
//                }
//            }
//        }// end of if..else
        // System.out.println("PCM Returned===============" +
        // audioData.length);
        return audioData;
    }


    public void updateVisualizerFFT(byte[] bytes) {
        mFFTBytes = bytes;

        MFCC mfcc = new MFCC(SAMPLE_PER_FRAME, SAMPLING_RATE, numCepstra);
        double[] result = mfcc.mydoMFCC(extractFloatDataFromAmplitudeByteArray(bytes));
        dctDatas=result;
        for(double d:result){
            System.out.print(d);
        }
        invalidate();
    }

    boolean mFlash = false;

    /**
     * Call this to make the visualizer flash. Useful for flashing at the start
     * of a song/loop etc...
     */
    public void flash() {
        mFlash = true;
        invalidate();
    }

    Bitmap mCanvasBitmap;
    Canvas mCanvas;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create canvas once we're ready to draw
        mRect.set(0, 0, getWidth(), getHeight());

        if (mCanvasBitmap == null) {
            mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
        }
        if (mCanvas == null) {
            mCanvas = new Canvas(mCanvasBitmap);
        }

        if (mBytes != null) {
            // Render all audio renderers
            AudioData audioData = new AudioData(mBytes);
            for (Renderer r : mRenderers) {
                r.render(mCanvas, audioData, mRect);
            }

        }

        if (mFFTBytes != null) {
            // Render all FFT renderers
            FFTData fftData = new FFTData(mFFTBytes);
            for (Renderer r : mRenderers) {
                r.render(mCanvas, fftData, mRect);
            }
        }

        if(dctDatas!=null){
            if(myRenderer!=null){
                DCTData dctData = new DCTData(dctDatas);
                myRenderer.render(mCanvas, dctData, mRect);
            }

        }


        // Fade out old contents
        mCanvas.drawPaint(mFadePaint);

        if (mFlash) {
            mFlash = false;
            mCanvas.drawPaint(mFlashPaint);
        }

        canvas.drawBitmap(mCanvasBitmap, new Matrix(), null);
    }
}