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
import java.util.HashMap;
import java.util.Map;

public class Classify extends Fragment {
    private Button choose_file_button;
    public static final int PICKFILE_RESULT_CODE = 1;
    private Uri fileUri;
    private String filePath; // This is the final file path
    View classify_view;
    // To pass to the interpreter constructor
    File newFile = new File("src/main/assets/soundclassifier.tflite");
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        classify_view = inflater.inflate(R.layout.classify,container,false);
        choose_file_button = (Button) classify_view.findViewById(R.id.classify_button);

        try {
            Interpreter interpreter = new Interpreter(newFile);
            Map<String, Object> inputs = new HashMap<>();
            Map<String, Object> outputs = new HashMap<>();
            interpreter.runSignature(inputs, outputs, "mySignature");
            interpreter.close();
        } catch (Exception E){

        }

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
                }
                break;
        }
    }
}
