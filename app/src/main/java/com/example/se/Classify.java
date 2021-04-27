package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classify extends Fragment {
    private Button choose_file_button;
    private Button stop_classify;
    public static final int PICKFILE_RESULT_CODE = 1;
    private Uri fileUri;
    private String filePath; // This is the final file path
    View classify_view;

    // To load from asset folder
    private static final String LABEL_FILENAME = "file:///android_asset/labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/soundclassifier.tflite";
    private static final String LOG_TAG = "Log tagges is here";

    // For label and modelfile
    private List<String> labels = new ArrayList<String>();
    private List<String> displayedLabels = new ArrayList<>();

    // For the audio file
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BufferedInputStream in;
    byte[] audioBytes;

    // For machine learning
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    private MappedByteBuffer tfLiteModel;
    private Interpreter tfLite;
    private Map<Object,Object> outputMap = new HashMap<>();
    private final Interpreter.Options ftliteOptions = new Interpreter.Options();
    private RecognizeCommands recognizeCommands = null;
    // ToDo : Remove this if not needed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        classify_view = inflater.inflate(R.layout.classify,container,false);

        // Both finds the classify and stop classify button
        choose_file_button = (Button) classify_view.findViewById(R.id.classify_button);
        stop_classify = (Button) classify_view.findViewById(R.id.stop_classify);

        // For labels file
        String actualLabelFilename = LABEL_FILENAME.split("file:///android_asset/",-1)[1];
        Log.i(LOG_TAG,"Reading labels from " + actualLabelFilename);

        BufferedReader br = null;
        try{
            br = new BufferedReader(new InputStreamReader(classify_view.getContext().getAssets().open(actualLabelFilename)));
            String line;
            while ((line = br.readLine()) != null){
                labels.add(line);
                if (line.charAt(0) != '_'){
                    displayedLabels.add(line.substring(0,1).toUpperCase()+ line.substring(1));
                }
            }
        } catch (IOException e){
            throw new RuntimeException("Problem reading the label file!",e);
        }

        Log.i(LOG_TAG,"Labels file messages are :"+ displayedLabels);

        // ToDo : Implement Recognize Commands if not working

        // Opening the model file
        String actualModelFilename = MODEL_FILENAME.split("file:///android_asset/",-1)[1];
        try{
            tfLiteModel = loadModelFile(classify_view.getContext().getAssets(), actualModelFilename);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        Log.i(LOG_TAG,"The modal file is :"+actualModelFilename);
        Log.i(LOG_TAG,"The actual content is :"+tfLiteModel);

        // ToDo : Model file opened here
        try{
            ftliteOptions.setNumThreads(1);
            tfLite = new Interpreter(tfLiteModel,ftliteOptions);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        Log.i(LOG_TAG,"TF lite file loaded. ");

        choose_file_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
                // At this point we have the path of the file
                // File path working.
                /*
                    For pie chart we can have : https://github.com/PhilJay/MPAndroidChart
                 * */
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
                    open_audio_file(fileUri);
                    // Opens main audio file
                    try{
                        outputMap.put(0,"outputScores");
                        // Todo : Remove another loadModelFile @Depreciated
                        tfLite.run(audioBytes,outputMap);
                        Log.i(LOG_TAG,"The output is :"+ outputMap);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
                break;
        }
    }

    public void open_audio_file(Uri filePath){
        try{
            in = new BufferedInputStream(getContext().getContentResolver().openInputStream(filePath));
            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0)
            {
                out.write(buff, 0, read);
            }
            out.flush();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        audioBytes = out.toByteArray();
        Log.i(LOG_TAG,"The audio file is " + audioBytes.toString());
    }

    // This method loads the TF lite file
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFileName)
            throws IOException{
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }

}