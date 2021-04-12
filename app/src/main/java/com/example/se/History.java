package com.example.se;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class History extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Simply can make changes to the history_view to make use of on-create methods
        View history_view = inflater.inflate(R.layout.history,container,false);
        /*
        For pie chart we can have : https://github.com/PhilJay/MPAndroidChart
        * */
        return history_view;
    }
}
