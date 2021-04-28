package com.example.se;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class History extends Fragment {
    // Variables
    private static String TAG = "History";
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
        Log.i(TAG,""+ ml_recognized_val[0]);
        yData = ml_recognized_val; // Assigning the values
        pieChart = (PieChart) history_view.findViewById(R.id.idPieChart);
        Description d = new Description();
        d.setText("Your latest Classification");
        pieChart.setDescription(d);
        pieChart.setHoleRadius(25f);
        pieChart.setRotationEnabled(true);
        pieChart.setDrawEntryLabels(true);

        addDataSet();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: Value select from chart.");
                Log.d(TAG, "onValueSelected: " + e.toString());
                Log.d(TAG, "onValueSelected: " + h.toString());

                int pos1 = e.toString().indexOf("(sum): ");
                String sales = e.toString().substring(pos1 + 7);

                for(int i = 0; i < yData.length; i++){
                    if(yData[i] == Float.parseFloat(sales)){
                        pos1 = i;
                        break;
                    }
                }
                String employee = xData[pos1 + 1];
                Toast.makeText(history_view.getContext(), "Employee " + employee + "\n" + "Sales: $" + sales + "K", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        return history_view;
    }

    private void addDataSet() {
        Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
        }

        for(int i = 1; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Animal Sound");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.GRAY);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
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
