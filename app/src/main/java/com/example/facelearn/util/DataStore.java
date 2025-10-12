package com.example.facelearn.util;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataStore {
    private static final String TAG = "DataStore";
    private Context context;
    private SharedPreferences name;
    private SharedPreferences vector;

    public List<Record> getList() {
        return this.name.getAll().entrySet().stream()
                .map(entry -> new Record(entry.getKey(), entry.getValue().toString()))
                .collect(Collectors.toList());
    }

    public static class Record {
        public final String key;
        public final String name;

        public Record(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }
    public static class Result {
        public final double point;
        public final String name;

        public Result(double point, String name) {
            this.point = point;
            this.name = name;
        }
    }
    public DataStore(Context context){
        this.context = context;
        this.name = context.getSharedPreferences("dataStoreName", Context.MODE_PRIVATE);
        this.vector = context.getSharedPreferences("dataStoreVec", Context.MODE_PRIVATE);
    }

    public void add(String name, String vecStr) {
        String key = String.valueOf(this.vector.getAll().size() + 1);
        this.name.edit().putString(key, name).apply();
        this.vector.edit().putString(key, vecStr).apply();
    }

    public Result nearest(String vecStr) {
        Result result = new Result(-1.0, "unknown");
        Map<String, ?> data = this.vector.getAll();
        double[] a = toDoubleArray(vecStr);
        Log.d(TAG, "a length: " + a.length);
        for (Map.Entry<String, ?> keyValue : data.entrySet()) {
            if (keyValue.getValue() instanceof String) {
                double[] b = toDoubleArray((String) keyValue.getValue());
                Log.d(TAG, "b length: " + b.length);
                double point = dotProduct(a,b);

                if (result == null || result.point < point) {
                    result = new Result(point, this.name.getString(keyValue.getKey(), ""));
                }
            }
        }
        return result;
    }

    private double[] toDoubleArray(String vecStr) {
        String[] strs = vecStr.replaceAll("[\\[\\],]", " ").split(" ");
        ArrayList<Double> str = new ArrayList<>();
        for (int i=0; i<strs.length; i++) {
            try{
                double value = Double.parseDouble(strs[i]);
                str.add(value);
            }catch(NumberFormatException e) {

            }
        }
        return str.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = .0;
        for (int i=0; i<a.length; i++) {
            sum += a[i]*b[i];
        }
        return sum;
    }
}
