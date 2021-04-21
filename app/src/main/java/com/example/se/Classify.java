package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import org.tensorflow.lite.Interpreter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Classify extends Fragment {
    private Button choose_file_button;
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

    // For recording
    short[] recordingBuffer = new short[RECORDING_LENGTH];

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        classify_view = inflater.inflate(R.layout.classify,container,false);
        choose_file_button = (Button) classify_view.findViewById(R.id.classify_button);

        choose_file_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
                // File path working.
                /*
                    For pie chart we can have : https://github.com/PhilJay/MPAndroidChart
                **/
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
}
