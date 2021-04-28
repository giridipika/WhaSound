package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;

public class History extends Fragment {
    // Variables
    private static String HISTORY_TAG = "History";
    private float [] yData;
    private String [] xData = {"Background Noise","Cow","Dog","Hen","Sheep"};
    PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Simply can make changes to the history_view to make use of on-create methods
        View history_view = inflater.inflate(R.layout.history, container, false);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.ml_values), Context.MODE_PRIVATE);
        String defaultValue = "Random";
        String ml_recognized_model = sharedPref.getString("FLOAT_ARR", defaultValue); // Similar to map; email is the key and defaultValue is what it implies
        float[] ml_recognized_val = getFloatArray(ml_recognized_model);
        yData = ml_recognized_val; // Assigning the values
        pieChart = (PieChart) history_view.findViewById(R.id.idPieChart);
        Description d = new Description();
        d.setText("Your latest Classification");
        pieChart.setDescription(d);
        pieChart.setHoleRadius(25f);
        pieChart.setRotationEnabled(true);
        pieChart.setDrawEntryLabels(true);
        return history_view;
    }

    // Coverts string to float []
    public float[] getFloatArray(String str) {
        if (str != null) {
            String str1[] = str.split(",");
            float arr[] = new float[str1.length - 1];
            // at i=0 it is space so start from 1
            for (int i = 1; i < str1.length; i++) {
                arr[i - 1] = Float.parseFloat(str1[i]);
            }
            return arr;
        }
        return null;
    }
}
