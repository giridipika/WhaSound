package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.net.rtp.AudioStream;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.se.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.flex.FlexDelegate;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Object;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classify extends Fragment {
    private Button choose_file_button;
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
    InputStream in;
    byte[] audioBytes;
    Float[][] audioFile = new Float[1][44032];

    // For machine learning
    private MappedByteBuffer tfLiteModel;
    private Interpreter tfLite;
    private final Interpreter.Options ftliteOptions = new Interpreter.Options();
    float[] outputs;
    private RecognizeCommands recognizeCommands = null;
    private int modelInputLength;
    private int modelNumClasses;
    private FloatBuffer inputBuffer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        classify_view = inflater.inflate(R.layout.classify,container,false);

        // Both finds the classify and stop classify button
        choose_file_button = (Button) classify_view.findViewById(R.id.classify_button);

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
        // Creates the equal number of labels on the output file
        Log.i(LOG_TAG,"Labels file messages are :"+ displayedLabels);
        outputs = new float[displayedLabels.size()];

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
//            ftliteOptions.setNumThreads(1);
//            FlexDelegate flex = new FlexDelegate();
//            ftliteOptions.addDelegate(flex);
//            File openThis = new File(MODEL_FILENAME);
//            tfLite = new Interpreter(tfLiteModel,ftliteOptions);
            // tfLite = new Interpreter(openThis);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        Log.i(LOG_TAG,"TF lite file loaded. ");

        // To load the metadata and verify it
//        int [] inputShape = tfLite.getInputTensor(0).shape();
//        modelInputLength = inputShape[1];
//
//        int [] outputShape  = tfLite.getOutputTensor(0).shape();
//        modelNumClasses = outputShape[1];

        Log.i(LOG_TAG," "+modelNumClasses);
        if (modelNumClasses != displayedLabels.size()){
            Log.e(LOG_TAG,"The file's metadata is not the same");
        }else{
            Log.i(LOG_TAG,"The file's metadata is same");
        }
        Log.i(LOG_TAG," "+displayedLabels.size());
        inputBuffer = FloatBuffer.allocate(modelInputLength);

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

                    //open_audio_file(fileUri);
                    // Opens main audio file
                    try{
//                        FloatBuffer outputBuffer = FloatBuffer.allocate(modelNumClasses);
//                        inputBuffer.rewind();
//                        outputBuffer.rewind();
                        //tfLite.run(inputBuffer,outputBuffer);
//                        Log.i(LOG_TAG,"The output is :"+ Arrays.toString(outputBuffer.array()));
//                        SharedPreferences sharedPref = classify_view.getContext().getSharedPreferences(getString(R.string.ml_values), Context.MODE_PRIVATE); // To open in private mode, can only be seen
//                        // by our application
//                        SharedPreferences.Editor editor = sharedPref.edit(); // Opening the file to edit
//                        float [] arr = outputBuffer.array();
//                        String str = " ";
//                        for(int i=0;i<arr.length;i++){
//                            str = str + ", "+ arr[i];
//                        }
//                        editor.putString("FLOAT_ARR",str); // Putting in the string, Now Email keyword in SharedPref is associated with email entered by the user
//                        editor.apply(); // Applying the changes
//                        inputBuffer.clear();
//                        outputBuffer.clear();
                        openHistoryPage(); // This opens history page after everything is done
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

        //Todo : Change the audio file to a float pointer
        audioBytes = out.toByteArray();
        for (int i = 0;i < audioBytes.length;i++){
            float val = (float) audioBytes[i];
            inputBuffer.put(i,val);
            audioFile[0][i] = val;
        }
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
    // To open history page
    public void openHistoryPage(){
        ViewPager viewPager = getActivity().findViewById(R.id.view_pager);
        viewPager.setCurrentItem(2);
    }
}