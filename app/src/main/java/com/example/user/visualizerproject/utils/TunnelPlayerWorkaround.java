/**
 * Copyright 2013, Haruki Hasegawa
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */

package com.example.user.visualizerproject.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


public class TunnelPlayerWorkaround {
  private static final String TAG = "TunnelPlayerWorkaround";

  private static final String SYSTEM_PROP_TUNNEL_DECODE_ENABLED = "tunnel.decode";

  private TunnelPlayerWorkaround()
  {
  }

  /**
   * Obtain "tunnel.decode" system property value
   * 
   * @param context Context
   * @return Whether tunnel player is enabled
   */
  public static boolean isTunnelDecodeEnabled(Context context)
  {
    return SystemPropertiesProxy.getBoolean(
        context, SYSTEM_PROP_TUNNEL_DECODE_ENABLED, false);
  }

  /**
   * Create silent MediaPlayer instance to avoid tunnel player issue
   * 
   * @param context Context
   * @return MediaPlayer instance
   */


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

  public static MediaPlayer createSilentMediaPlayer(Context context)
  {
    boolean result = false;

    MediaPlayer mp = null;
    try {
      mp = MediaPlayer.create(context, Uri.parse(getFilename("베베")));
      mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

      // NOTE: start() is no needed
      // mp.start();

      result = true;
    } catch (RuntimeException e) {
      Log.e(TAG, "createSilentMediaPlayer()", e);
    } finally {
      if (!result && mp != null) {
        try {
          mp.release();
        } catch (IllegalStateException e) {
        }
      }
    }

    return mp;
  }
}
