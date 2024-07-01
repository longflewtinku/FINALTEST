package com.linkly.libui.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

import timber.log.Timber;


// DEMO CLASS from PAX
public class SpeechUtils {
    private static SpeechUtils sInstance;
    private TextToSpeech mSpeech;
    private boolean mLanguageAvailable;

    private SpeechUtils(){ }
    public static SpeechUtils getInstance(){
        if (sInstance == null){
            sInstance = new SpeechUtils();
        }
        return sInstance;
    }
    /**
     * call this init method before first {@link #speak(String)} call
     * to give some time to set the speech up, e.g. in #onResume method of MainActivity
     * @param context use application's context   * */
    public void init(Context context) {
        if (mSpeech != null) return;
        mSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS){
                //set language
                Locale current = context.getResources().getConfiguration().getLocales().get(0);
                int ret = mSpeech.setLanguage(current);
                mLanguageAvailable=TextToSpeech.LANG_COUNTRY_AVAILABLE == ret;

                // if install failed, try en_GB as a fallback
                if( !mLanguageAvailable ) {
                    ret = mSpeech.setLanguage(Locale.UK);
                    mLanguageAvailable=TextToSpeech.LANG_COUNTRY_AVAILABLE == ret;
                }
            }
        });
    }

    public void setSpeechRate(float rate) {
        mSpeech.setSpeechRate(rate);
    }

    public Boolean isSpeaking() {
        return mSpeech.isSpeaking();
    }

    public void stop(){
        if (mLanguageAvailable) {
            mSpeech.stop();
        }
    }
    /**
     * call this method to speak the text, new text will be spoken after existing ones
     * @param words text to be read by the device engine
     * */
    public void speak(String words) {
        speak(words, 0,null);
    }

    public void speak(String words, int silentTimeOutMs) {
            speak(words, silentTimeOutMs,null);
    }


    /**
     * @param words text to be read by the device engine
     * @param listener callback for completion
     * */
    private void speak(final String words, int silentTimeoutMs, final SpeakCompleteListener listener) {
        if (mLanguageAvailable) {
            String utteranceId = "PAX_utterandId";
            mSpeech.speak(words, TextToSpeech.QUEUE_ADD, null, words);
            if (silentTimeoutMs > 0) {
                mSpeech.playSilentUtterance(silentTimeoutMs, TextToSpeech.QUEUE_ADD, utteranceId);
            }
            mSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Doing nothing because we're required to override abstract class methods
                }
                @Override
                public void onDone(String utteranceId) {
                    Timber.i( "utterance:" + utteranceId +" finished");
                    if (listener != null && utteranceId.equals(words)) {
                        listener.onCompleted(true);
                    }
                }

                /**
                 *  @deprecated (This method was deprecated in API level 21. Use { @ link onError ( java.lang.String, int)} instead)
                 */
                @Deprecated
                @Override
                public void onError(String s) {
                    Timber.e("onError:%s", utteranceId);
                    if (listener !=null&& utteranceId.equals(words)) {
                        listener.onCompleted(false);
                    }
                }
                @Override
                public void onError(String s,int errorCode) {
                    Timber.e("onError:%s with error code : %d", utteranceId,errorCode);
                    if (listener !=null&& utteranceId.equals(words)) {
                        listener.onCompleted(false);
                    }
                }
            });
        }
    }

    public interface SpeakCompleteListener {
        void onCompleted(boolean success);
    }
}
