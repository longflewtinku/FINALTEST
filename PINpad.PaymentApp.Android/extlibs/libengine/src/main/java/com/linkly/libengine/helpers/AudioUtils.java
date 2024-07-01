package com.linkly.libengine.helpers;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.linkly.libmal.IMalHardware;

import java.io.IOException;

import timber.log.Timber;

/***
 * Utils/helper class for any audio functionality
 */
public class AudioUtils {

    /***
     * Read MP3 File from assets and plays it ***WARNING BLOCKING***
     * @param context required to read the file from assets
     * @param audioFile audio file name
     * @return true if MP3 was successfully played.
     */
    public static boolean playAssetsMP3Sound(Context context, String audioFile) {
        boolean playedSound = false;
        MediaPlayer mediaPlayer = new MediaPlayer();

        try (AssetFileDescriptor audioFileFd = context.getAssets().openFd(audioFile)) {
            mediaPlayer.setDataSource(audioFileFd.getFileDescriptor(), audioFileFd.getStartOffset(), audioFileFd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Thread.sleep(mediaPlayer.getDuration()); // Block the thread during media playback
            playedSound = true;
        } catch (IOException e) {
            Timber.e(e, "Error playing Txn success music, playing beep instead");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Timber.e("Interrupt exception during During play MP3 sound");
        } catch (Exception e){
            Timber.e(e);
        } finally {
            mediaPlayer.release();
        }

        return playedSound;
    }

    /***
     * Utility function to handle transaction result audio
     * Either uses beep, MP3 audio defined in Assets
     * Depending on configuration the function should either play MP3 audio, beep or fallback to beep on failure.
     *
     * @param fileName name of file in Assets to be loaded
     * @param beepFrequency if using beep OR fallback to beep
     * @param beepDurationMS duration of beep if used.
     * @param useCustomAudio toggle flag to control if beep is used or not
     * @param context required to play audio
     * @param hardware mal context for beep
     */
    public static void playAudioResult(String fileName, int beepFrequency, int beepDurationMS, boolean useCustomAudio, Context context, IMalHardware hardware) {
        if(!useCustomAudio ||
                !playAssetsMP3Sound(context, fileName)) {
            hardware.beep(beepFrequency, beepDurationMS);
        }
    }
}
