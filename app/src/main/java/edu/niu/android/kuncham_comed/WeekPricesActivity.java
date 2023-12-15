package edu.niu.android.kuncham_comed;


import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.time.format.DateTimeFormatter;
import com.google.gson.reflect.TypeToken;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import java.util.Locale;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;

public class WeekPricesActivity extends AppCompatActivity {
    private Executor executor;
    private OkHttpClient client;
    private Gson gson;
    private BarChart barChart;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_prices);
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = currentDate.format(formatter);

        String base_url = "https://hourlypricing.comed.com/api?type=week&date=";

        barChart = findViewById(R.id.barChart);

        // Initialize executor, OkHttpClient, and Gson
        executor = Executors.newFixedThreadPool(4);
        client = new OkHttpClient();
        gson = new Gson();

        // Formulate the API request
        Request request = new Request.Builder()
                .get()
                .url(base_url + formattedDate)
                .build();
        Log.d("url", String.valueOf(base_url + formattedDate));

        // Execute the API request asynchronously
        executor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    String json = responseBody.string();
                    Log.d("Json", String.valueOf(json));
                    List<WeekDataPoint> dataPoints = parseWeekJsonData(json);
                    Log.d("WEEKJI",String.valueOf(dataPoints));
                    if (dataPoints != null) {
                        runOnUiThread(() -> displayWeekBarChart(dataPoints));
                    } else {
                        Log.e("TodayPricesActivity", "Error parsing JSON data");
                    }
                } else {
                    Log.e("TodayPricesActivity", "Error loading data. Response code: " + response.code() + ", Message: " + response.message());
                }
            } catch (IOException e) {
                Log.e("TodayPricesActivity", "Error loading data", e);
            }
        });
    }

    public List<WeekDataPoint> parseWeekJsonData(String json) {
        try {
            Log.d("parseJsonData", "entered");
            JSONArray jsonArray = new JSONArray(json);
            List<WeekDataPoint> dataPoints = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray dataPointArray = jsonArray.getJSONArray(i);
                Log.d("dataPointArray", String.valueOf(dataPointArray));
                String dataPointString = dataPointArray.toString();
                WeekDataPoint dataPoint = parseWeekData(dataPointString);
                Log.d("Todays", String.valueOf(dataPoint));
                if (dataPoint != null) {
                    dataPoints.add(dataPoint);
                }
            }

            return dataPoints;
        } catch (JSONException e) {
            Log.e("TodayPricesActivity", "Error parsing JSON", e);
            return null;
        }
    }

    public WeekDataPoint parseWeekData(String data) {
        try {
            Log.d("ParseData", "entered");
            String[] parts = data.split(",(?=[^,]*$)");
            Log.d("parts", String.valueOf(parts));
            String dateStr = parts[0];
            Log.d("dateStr", String.valueOf(dateStr));
            String valueStr = parts[1].replaceAll("[^\\d.]", "");
            double value = Double.parseDouble(valueStr);
            Log.d("pdata", "hello");

            // Extract components from the date string
            String[] dateComponents = dateStr.split("[()]")[1].split(",");
            String yearStr = dateComponents[0].replaceAll("[^\\d.]", "");
            int year = Integer.parseInt(yearStr);
            String monthStr = dateComponents[1].replaceAll("[^\\d.]", "");
            int month = Integer.parseInt(monthStr) - 1; // Month is 0-based
            String dayStr = dateComponents[2].replaceAll("[^\\d.]", "");
            int day = Integer.parseInt(dayStr);
            String hourStr = dateComponents[3].replaceAll("[^\\d.]", "");
            int hour = Integer.parseInt(hourStr);
            String minuteStr = dateComponents[4].replaceAll("[^\\d.]", "");
            int minute = Integer.parseInt(minuteStr);
            String secondStr = dateComponents[5].replaceAll("[^\\d.]", "");
            int second = Integer.parseInt(secondStr);
            Log.d("pdata", String.valueOf(hour));

            // Create a Date object
            Date date = new Date(year - 1900, month, day, hour, minute, second);

            return new WeekDataPoint(date, value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayWeekBarChart(List<WeekDataPoint> dataPoints) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int[] colors = new int[7];
        Arrays.fill(colors, Color.TRANSPARENT);

        // Define the order of days of the week
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < dataPoints.size(); i++) {
            WeekDataPoint dataPoint = dataPoints.get(i);
            Log.d("Weekprint",String.valueOf(dataPoint));
            entries.add(new BarEntry(i, (float) dataPoint.getValue()));
            float datapoint = (float) dataPoint.getValue();
            if (datapoint < 5) {
                colors[i] = -16711936;
            } else if (datapoint >= 5 && datapoint <= 14) {
                colors[i] = -256;
            } else {
                colors[i] = -65536;
            }

            // Use the order defined in daysOfWeek
            labels.add(daysOfWeek[i % daysOfWeek.length]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Data Points");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        barChart.invalidate(); // refresh
    }


    public static class WeekDataPoint {
        private Date date;
        private double value;

        public WeekDataPoint(Date date, double value) {
            this.date = date;
            this.value = value;
        }

        public Date getDate() {
            return date;
        }

        public double getValue() {
            return value;
        }
    }
}
