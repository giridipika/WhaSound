package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import org.tensorflow.lite.Interpreter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Classify extends Fragment {
    private Button choose_file_button;
    private Button stop_classify;
    public static final int PICKFILE_RESULT_CODE = 1;
    private Uri fileUri;
    private String filePath; // This is the final file path
    View classify_view;

    // For live sound recognition
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);

    // File inputs for tflite and labels.txt
    private static final String LABEL_FILENAME = "file:///android_asset/conv_actions_labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/conv_actions_frozen.tflite";
    private static final String HANDLE_THREAD_NAME = "CameraBackground";

    // To request the audio
    private static final int REQUEST_RECORD_AUDIO = 13;
    private static final long AVERAGE_WINDOW_DURATION_MS = 1000;
    private static final float DETECTION_THRESHOLD = 0.50f;
    private static final int SUPPRESSION_MS = 1500;
    private static final int MINIMUM_COUNT = 3;

    // For recording
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 30;

    // Other necessary constant
    private static final String LOG_TAG = "Error";

    // Various threads used for recognition and recording
    int recordingOffset = 0;
    boolean shouldContinue = true;
    private Thread recordingThread; // Thread used for recording
    boolean shouldContinueRecognition = true; // To check for recognition
    private Thread recognitionThread; // Thread used for recognition
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private final ReentrantLock tfLiteLock = new ReentrantLock();
    private List<String> labels = new ArrayList<String>();
    private List<String> displayedLabels = new ArrayList<>();

    // For machine learning model
    private final Interpreter.Options tfLiteOptions = new Interpreter.Options();
    private MappedByteBuffer tfLiteModel;
    private Interpreter tfLite;
    private RecognizeCommands recognizeCommands = null;

    // Basically opens the model file from Assets
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Other variables
    private Handler handler = new Handler();
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        classify_view = inflater.inflate(R.layout.classify,container,false);
        choose_file_button = (Button) classify_view.findViewById(R.id.classify_button);
        stop_classify = (Button) classify_view.findViewById(R.id.stop_classify);

        // Reading the labels start here
        String actualLabelFilename = LABEL_FILENAME.split("file:///android_asset/", -1)[1];
        Log.i(LOG_TAG, "Reading labels from: " + actualLabelFilename);

        // To read the labels.txt file
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getContext().getAssets().open(actualLabelFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }
        System.out.println(br);
        // The label file is now read under br
        // Our app will be able to recognize the animal sounds now
        recognizeCommands =
                new RecognizeCommands(
                        labels,
                        AVERAGE_WINDOW_DURATION_MS,
                        DETECTION_THRESHOLD,
                        SUPPRESSION_MS,
                        MINIMUM_COUNT,
                        MINIMUM_TIME_BETWEEN_SAMPLES_MS);

        // The actual Model File-name is not tfLiteModel
        String actualModelFilename = MODEL_FILENAME.split("file:///android_asset/", -1)[1];
        try {
            tfLiteModel = loadModelFile(getContext().getAssets(), actualModelFilename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(actualModelFilename);

        stop_classify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldContinue = false;
                // This will stop if the threads are running
                stopRecognition();
                stopRecording();
            }
        });

        choose_file_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // These start the various threads
                // Start the recording and recognition threads.
                requestMicrophonePermission();
                startRecording();
                //startRecognition();
                // This is to open file chooser, no longer needed
//                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//                chooseFile.setType("*/*");
//                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
//                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
//                // File path working.
//                /*
//                    For pie chart we can have : https://github.com/PhilJay/MPAndroidChart
//                **/
            }



        });
        return classify_view;
    }

    // This gets the file path
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    fileUri = data.getData();
                    filePath = fileUri.getPath();
                    System.out.println("The selected file path is :"+filePath);
                }
                break;
        }
    }

    // This method requestsMicrophone Permission
    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[] {android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    // On permission is granted
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording(); // Starts the recording
                startRecognition(); // Starts the recognition
        }
    }
    // Starts Recording
    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }

    // Stops Recording
    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;
    }

    // This method records the audio and places it in a file
    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;
            } finally {
                recordingBufferLock.unlock();
            }
        }

        record.stop();
        record.release();
    }

    // Starts the recognition
    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    // Stops the recognition
    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;
    }
    // This method is used by start recognition
    private void recognize() {
        Log.v(LOG_TAG, "Start recognition");
        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[][] floatInputBuffer = new float[RECORDING_LENGTH][1];
        float[][] outputScores = new float[1][labels.size()];
        int[] sampleRateList = new int[] {SAMPLE_RATE};

        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            long startTime = new Date().getTime();
            // The recording thread places data in this round-robin buffer, so lock to
            // make sure there's no writing happening and then copy it to our own
            // local version.
            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingBufferLock.unlock();
            }

            // We need to feed in float values between -1.0f and 1.0f, so divide the
            // signed 16-bit inputs.
            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i][0] = inputBuffer[i] / 32767.0f;
            }

            Object[] inputArray = {floatInputBuffer, sampleRateList};
            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, outputScores);

            // Run the model.
            tfLiteLock.lock();
            try {
                tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
            } finally {
                tfLiteLock.unlock();
            }

            // Use the smoother to figure out if we've had a real recognition event.
            long currentTime = System.currentTimeMillis();
//            final RecognizeCommands.RecognitionResult result =
//                    recognizeCommands.processLatestResults(outputScores[0], currentTime);
//            lastProcessingTimeMs = new Date().getTime() - startTime;
//            runOnUiThread(
//                    new Runnable() {
//                        @Override
//                        public void run() {
//
//                            inferenceTimeTextView.setText(lastProcessingTimeMs + " ms");
//
//                            // If we do have a new command, highlight the right list entry.
//                            if (!result.foundCommand.startsWith("_") && result.isNewCommand) {
//                                int labelIndex = -1;
//                                for (int i = 0; i < labels.size(); ++i) {
//                                    if (labels.get(i).equals(result.foundCommand)) {
//                                        labelIndex = i;
//                                    }
//                                }
//
//                                switch (labelIndex - 2) {
//                                    case 0:
//                                        selectedTextView = yesTextView;
//                                        break;
//                                    case 1:
//                                        selectedTextView = noTextView;
//                                        break;
//                                    case 2:
//                                        selectedTextView = upTextView;
//                                        break;
//                                    case 3:
//                                        selectedTextView = downTextView;
//                                        break;
//                                    case 4:
//                                        selectedTextView = leftTextView;
//                                        break;
//                                    case 5:
//                                        selectedTextView = rightTextView;
//                                        break;
//                                    case 6:
//                                        selectedTextView = onTextView;
//                                        break;
//                                    case 7:
//                                        selectedTextView = offTextView;
//                                        break;
//                                    case 8:
//                                        selectedTextView = stopTextView;
//                                        break;
//                                    case 9:
//                                        selectedTextView = goTextView;
//                                        break;
//                                }
//
//                                if (selectedTextView != null) {
//                                    selectedTextView.setBackgroundResource(R.drawable.round_corner_text_bg_selected);
//                                    final String score = Math.round(result.score * 100) + "%";
//                                    selectedTextView.setText(selectedTextView.getText() + "\n" + score);
//                                    selectedTextView.setTextColor(
//                                            getResources().getColor(android.R.color.holo_orange_light));
//                                    handler.postDelayed(
//                                            new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    String origionalString =
//                                                            selectedTextView.getText().toString().replace(score, "").trim();
//                                                    selectedTextView.setText(origionalString);
//                                                    selectedTextView.setBackgroundResource(
//                                                            R.drawable.round_corner_text_bg_unselected);
//                                                    selectedTextView.setTextColor(
//                                                            getResources().getColor(android.R.color.darker_gray));
//                                                }
//                                            },
//                                            750);
//                                }
//                            }
//                        }
//                    });
            try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        Log.v(LOG_TAG, "End recognition");
    }

    // This is to increase the number of threads we will set to a minimum possible
//    @Override
//    public void onClick(View v) {
//        if ((v.getId() != R.id.plus) && (v.getId() != R.id.minus)) {
//            return;
//        }
//
//        String threads = threadsTextView.getText().toString().trim();
//        int numThreads = Integer.parseInt(threads);
//        if (v.getId() == R.id.plus) {
//            numThreads++;
//        } else {
//            if (numThreads == 1) {
//                return;
//            }
//            numThreads--;
//        }
//
//        final int finalNumThreads = numThreads;
//        threadsTextView.setText(String.valueOf(finalNumThreads));
//        backgroundHandler.post(
//                () -> {
//                    tfLiteOptions.setNumThreads(finalNumThreads);
//                    recreateInterpreter();
//                });
//    }

//    // This might be used later - will come back to this
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        backgroundHandler.post(
//                () -> {
//                    tfLiteOptions.setUseNNAPI(isChecked);
//                    recreateInterpreter();
//                });
//        if (isChecked) apiSwitchCompat.setText("NNAPI");
//        else apiSwitchCompat.setText("TFLITE");
//    }

    // This recreates the interpreter
    private void recreateInterpreter() {
        tfLiteLock.lock();
        try {
            if (tfLite != null) {
                tfLite.close();
                tfLite = null;
            }
            tfLite = new Interpreter(tfLiteModel, tfLiteOptions);
            tfLite.resizeInput(0, new int[] {RECORDING_LENGTH, 1});
            tfLite.resizeInput(1, new int[] {1});
        } finally {
            tfLiteLock.unlock();
        }
    }

    // Starts the background thread
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    // Stops the Background Thread
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e("amlan", "Interrupted when stopping background thread", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopBackgroundThread();
    }
}
