package com.example.se;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Summary extends Fragment {
    TextView imported_text_view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View summary_view = inflater.inflate(R.layout.summary, container, false);
        imported_text_view = summary_view.findViewById(R.id.change_this_text);
        imported_text_view.setText("Firstly, the audio file to be classified is downloaded or recorded with the help" +
                " of device's microphone. The application only supports .wav files therefore the user is requested to convert any other files to .wav "+
                "before recognition. After the conversion of the files, when the user selects the audio file following steps are performed: \n"+
                "\n 1. The user locates the converted/downloaded .wav file on their device."+
                "\n 2. The selected audio file is converted to a byte buffer."+
                "\n 3. The byte buffer is added to a two dimensional float buffer"+
                "\n 4. The tflite model is loaded inside the application"+
                "\n 5. The converted float buffer is passed in through the opened model"+
                "\n 6. The model returns value in the form of float array"+
                "\n 7. The float array is saved into the memory "+
                "\n 8. The saved float array is dereferenced inside history page."+
                "\n 9. The results are displayed in a pie-chart"+
                "\n 10.The pie-chart displays what the result corresponds to once clicked on it.");
        return summary_view;
    }
}
