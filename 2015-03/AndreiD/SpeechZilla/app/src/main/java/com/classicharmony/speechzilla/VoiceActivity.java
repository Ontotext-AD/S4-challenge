package com.classicharmony.speechzilla;

import com.classicharmony.speechzilla.utils.CroutonStyles;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class VoiceActivity extends Activity {
    private static final int LISTENING_DIALOG = 0;
    private Handler _handler = null;
    private final Recognizer.Listener _listener;
    private Recognizer _currentRecognizer;
    private ListeningDialog _listeningDialog;
    private boolean _destroyed;
    private SpeechKit _speechKit;
    private boolean halt_action = false;
    private Button btn_startDictation;
    private Button button_analyze;
    private EditText edit_text_results;

    private class SavedState {
        String DialogText;
        String DialogLevel;
        boolean DialogRecording;
        Recognizer Recognizer;
        Handler Handler;
    }

    public VoiceActivity() {
        super();
        _listener = createListener();
        _currentRecognizer = null;
        _listeningDialog = null;
        _destroyed = true;
    }

    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        switch (id) {
            case LISTENING_DIALOG:
                _listeningDialog.prepare(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (_currentRecognizer != null) {
                            _currentRecognizer.stopRecording();
                            halt_action = true;
                            dialog.dismiss();
                        }
                    }
                });
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case LISTENING_DIALOG:
                return _listeningDialog;
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to this activity
        setContentView(R.layout.activity_voice);

        button_analyze = (Button) findViewById(R.id.button_analyze);
        edit_text_results = (EditText) findViewById(R.id.text_DictationResult);

        _speechKit = SpeechKit.initialize(getApplication().getApplicationContext(), AppInfo.SpeechKitAppId, AppInfo.SpeechKitServer, AppInfo.SpeechKitPort, AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
        _speechKit.connect();
        // TODO: Keep an eye out for audio prompts not working on the Droid 2 or other 2.2 devices.

        _speechKit.setDefaultRecognizerPrompts(null, null, null, null);
        btn_startDictation = (Button) findViewById(R.id.btn_startDictation);
        _destroyed = false;


        btn_startDictation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createListeningDialog();
                halt_action = false;
                SavedState savedState = (SavedState) getLastNonConfigurationInstance();
                if (savedState == null) {
                    // Initialize the handler, for access to this application's message queue
                    _handler = new Handler();
                } else {
                    // There was a recognition in progress when the OS destroyed/
                    // recreated this activity, so restore the existing recognition
                    _currentRecognizer = savedState.Recognizer;
                    _listeningDialog.setText(savedState.DialogText);
                    _listeningDialog.setLevel(savedState.DialogLevel);
                    _listeningDialog.setRecording(savedState.DialogRecording);
                    _handler = savedState.Handler;

                    if (savedState.DialogRecording) {
                        // Simulate onRecordingBegin() to start animation
                        _listener.onRecordingBegin(_currentRecognizer);
                    }

                    _currentRecognizer.setListener(_listener);
                }

                _listeningDialog.setText("Initializing...");
                showDialog(LISTENING_DIALOG);
                _listeningDialog.setStoppable(false);
                setResult("");
            }
        });


        button_analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Crouton.makeText(VoiceActivity.this,"Preparing to analyze text", CroutonStyles.GREEN).show();

                Intent i_analyze = new Intent(VoiceActivity.this, AnalyzeActivity.class);
                i_analyze.putExtra("text_to_analyze", edit_text_results.getText().toString());
                startActivity(i_analyze);
                edit_text_results.setText("");

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _destroyed = true;
        if (_currentRecognizer != null) {
            _currentRecognizer.cancel();
            _currentRecognizer = null;
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        if (_listeningDialog.isShowing() && _currentRecognizer != null) {
            // If a recognition is in progress, save it, because the activity
            // is about to be destroyed and recreated
            SavedState savedState = new SavedState();
            savedState.Recognizer = _currentRecognizer;
            savedState.DialogText = _listeningDialog.getText();
            savedState.DialogLevel = _listeningDialog.getLevel();
            savedState.DialogRecording = _listeningDialog.isRecording();
            savedState.Handler = _handler;

            _currentRecognizer = null; // Prevent onDestroy() from canceling
            return savedState;
        }
        return null;
    }

    private Recognizer.Listener createListener() {
        return new Recognizer.Listener() {
            @Override
            public void onRecordingBegin(Recognizer recognizer) {
                _listeningDialog.setText("Recording...");
                _listeningDialog.setStoppable(true);
                _listeningDialog.setRecording(true);

                // Create a repeating task to update the audio level
                Runnable r = new Runnable() {
                    public void run() {
                        if (_listeningDialog != null && _listeningDialog.isRecording() && _currentRecognizer != null) {
                            _listeningDialog.setLevel(Float.toString(_currentRecognizer.getAudioLevel()));
                            _handler.postDelayed(this, 300);
                        }
                    }
                };
                r.run();
            }

            @Override
            public void onRecordingDone(Recognizer recognizer) {
                _listeningDialog.setText("Processing...");
                _listeningDialog.setLevel("");
                _listeningDialog.setRecording(false);
                _listeningDialog.setStoppable(false);

            }

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                if (recognizer != _currentRecognizer) return;
                //if (_listeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
                _currentRecognizer = null;
                _listeningDialog.setRecording(false);

                // Display the error + suggestion in the edit box
                String detail = error.getErrorDetail();
                String suggestion = error.getSuggestion();

                if (suggestion == null) suggestion = "";
                setResult(detail + "\n" + suggestion);
                // for debugging purpose: printing out the speechkit session id
                android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
                        + _speechKit.getSessionId() + "]");
            }

            @Override
            public void onResults(Recognizer recognizer, Recognition results) {
                //if (_listeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
                _currentRecognizer = null;
                _listeningDialog.setRecording(false);
                int count = results.getResultCount();
                Recognition.Result[] rs = new Recognition.Result[count];
                for (int i = 0; i < count; i++) {
                    rs[i] = results.getResult(i);
                }
                setResults(rs);
                // for debugging purpose: printing out the speechkit session id
                android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
                        + _speechKit.getSessionId() + "]");
            }
        };
    }

    private void setResult(String result) {

        if (result.contains("Sorry, speech not recognized")) {
            result = " * ";
        }

        if (edit_text_results != null) {
            String old_text = edit_text_results.getText().toString();
            edit_text_results.setText(old_text + result + "\n");
        }

        if (!halt_action) {

            try {
                _currentRecognizer = _speechKit.createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener, _handler);
                _currentRecognizer.start();
            } catch (Exception ex) {
                Log.e("can'edit_text_results start recognizer", "!!!!!!!!!!!! <<<<<<<>>>>>>>> !!!!!!!!!!!!!!!!");
            }
        }


    }

    private void setResults(Recognition.Result[] results) {

        if (results.length > 0) {
            setResult(results[0].getText());
        } else {
            setResult("");
        }
    }

    private void createListeningDialog() {
        _listeningDialog = new ListeningDialog(this);


        _listeningDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (_currentRecognizer != null) // Cancel the current recognizer
                {
                    _currentRecognizer.cancel();
                    _currentRecognizer = null;
                }

                if (!_destroyed) {
                    VoiceActivity.this.removeDialog(LISTENING_DIALOG);
                    createListeningDialog();
                }
            }
        });
    }
}
