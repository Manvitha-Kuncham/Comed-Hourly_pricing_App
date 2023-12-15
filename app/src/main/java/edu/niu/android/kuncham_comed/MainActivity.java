/****************************************************************************
 *    CSCI 522               Final project           Fall 2023              *
 *    App Name: Kuncham_comed                                               *
 *    Class Name: MainActivity                                              *
 *    Developer(s): Manvitha Kuncham                                        *
 *    Due Date: December 01 2023                                            *
 *    Purpose: The provided Android code defines the MainActivity,          *
 *    orchestrating the user interface and interactions for presenting      *
 *    electricity energy pricing data across various time intervals through *
 *    a bar chart visualization. The onCreate() method initializes essential*
 *    components,such as the executor, OkHttpClient, and Gson objects,      *
 *    for making API requests.                                              *
 *    Notably, the startFiveMinuteActivity(), startToday(),                 *
 *    startWeek(), and startMonth() methods handle data retrieval and       *
 *    visualization for 5-minute, today's, week's, and month's pricing,     *
 *    respectively. These methods format dates, construct API URLs,         *
 *    execute asynchronous tasks for data fetching, and dynamically         *
 *    update the BarChart with parsed information. The UI allows users      *
 *    to navigate through different time periods, triggered by back and     *
 *    next buttons, while backTodayActivity(), backWeekActivity(),          *
 *    backMonthActivity(), nextTodayActivity(), nextWeekActivity(),         *
 *    and nextMonthActivity() manage date updates and chart refreshes.      *
 *    Additionally, the code includes functions for parsing JSON responses  *
 *    and formatting data points for display. Overall, this implementation  *
 *   provides a robust and user-friendly interface for exploring electricity*
 *    energy pricing trends over distinct time frames.                      *
 ************************************************************************** */

package edu.niu.android.kuncham_comed;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import android.util.TypedValue;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private Executor executor;
    private OkHttpClient client;
    private Gson gson;
    private BarChart barChart;
    private Button Mbutton;
    private Button Tbutton;
    private Button Fbutton;
    private Button Wbutton;
    private LocalDate currentWeekDate;
    private LocalDate currentTodayDate;
    private LocalDate currentMonthDate;
    private DateTimeFormatter Weekformatter;
    private DateTimeFormatter Todayformatter;
    private Button TodayBbutton;
    private Button TodayNbutton;
    private EditText TodayEbutton;
    private Button WeekBbutton;
    private Button WeekNbutton;
    private EditText WeekEbutton;
    private Button MonthBbutton;
    private Button MonthNbutton;
    private EditText MonthEbutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startToday(findViewById(R.id.Today));
        getCprice();
    }

    public void getCprice() {
        executor = Executors.newFixedThreadPool(4);
        client = new OkHttpClient();
        gson = new Gson();
        ZoneId zoneId = ZoneId.of("GMT-6");
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDateTime = currentDateTime.format(formatter);
        currentDateTime = ZonedDateTime.now(zoneId);
        ZonedDateTime oneHourBack = currentDateTime.minusHours(1);
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDateTime1 = oneHourBack.format(formatter);
        String b_url = "https://hourlypricing.comed.com/api?type=5minutefeed&datestart=" + formattedDateTime1 + "&dateend=" + formattedDateTime;
        Request request = new Request.Builder()
                .get()
                .url(b_url)
                .build();

        executor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    String json = responseBody.string();
                    List<FiveminPriceEntry> priceEntries = parseFiveminJson(json);
                    double cprice = priceEntries.get(priceEntries.size() - 1).getPrice();
                    runOnUiThread(() -> {

                        TextView TextField = findViewById(R.id.TextFieldId);
                        if (cprice < 5) {
                            TextField.setText(String.format(
                                    "Now is a good time to turn on larger appliances. Current price: %.2fç", cprice));
                            TextField.setBackgroundColor(getResources().getColor(R.color.green));
                            TextField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        } else if (cprice >= 5 && cprice <= 14) {
                            TextField.setText(String.format(
                                    "Be mindful of energy usage; prices are moderate. Current price: %.2fç", cprice));
                            TextField.setBackgroundColor(Color.parseColor("#FFA500"));
                            TextField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        } else {
                            TextField.setText(String.format(
                                    "High prices; limit energy usage. Current price: %.2fç", cprice));
                            TextField.setBackgroundColor(Color.RED);
                            TextField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        }
                    });
                } else {
                    Log.e("MainActivity", "Error loading data. Response code: " + response.code() + ", Message: " + response.message());
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Error loading data", e);
            }
        });
    }

    public void startFiveMinuteActivity(View view) {
        TodayBbutton = findViewById(R.id.backTodayButton);
        TodayNbutton = findViewById(R.id.nextTodayButton);
        TodayEbutton = findViewById(R.id.textTodayField);
        WeekBbutton = findViewById(R.id.backWeekButton);
        WeekNbutton = findViewById(R.id.nextWeekButton);
        WeekEbutton = findViewById(R.id.textField);
        MonthBbutton = findViewById(R.id.backMonthButton);
        MonthNbutton = findViewById(R.id.nextMonthButton);
        MonthEbutton = findViewById(R.id.textMonthField);
        Fbutton = findViewById(R.id.Minute);
        Tbutton = findViewById(R.id.Today);
        Wbutton = findViewById(R.id.Week);
        Mbutton = findViewById(R.id.Month);
        Tbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Wbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Fbutton.setBackgroundColor(Color.DKGRAY);
        Mbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        TodayBbutton.setVisibility(View.INVISIBLE);
        TodayNbutton.setVisibility(View.INVISIBLE);
        TodayEbutton.setVisibility(View.INVISIBLE);
        WeekBbutton.setVisibility(View.INVISIBLE);
        WeekNbutton.setVisibility(View.INVISIBLE);
        WeekEbutton.setVisibility(View.INVISIBLE);
        MonthBbutton.setVisibility(View.INVISIBLE);
        MonthNbutton.setVisibility(View.INVISIBLE);
        MonthEbutton.setVisibility(View.INVISIBLE);
        barChart = findViewById(R.id.barChart);

        executor = Executors.newFixedThreadPool(4);
        client = new OkHttpClient();
        gson = new Gson();
        ZoneId zoneId = ZoneId.of("GMT-6");
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDateTime = currentDateTime.format(formatter);
        currentDateTime = ZonedDateTime.now(zoneId);
        ZonedDateTime oneHourBack = currentDateTime.minusHours(1);
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDateTime1 = oneHourBack.format(formatter);
        String b_url = "https://hourlypricing.comed.com/api?type=5minutefeed&datestart=" + formattedDateTime1 + "&dateend=" + formattedDateTime;
        Request request = new Request.Builder()
                .get()
                .url(b_url)
                .build();

        executor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    String json = responseBody.string();
                    List<FiveminPriceEntry> priceEntries = parseFiveminJson(json);
                    runOnUiThread(() -> drawFiveminBarChart(priceEntries));
                } else {
                    Log.e("MainActivity", "Error loading data. Response code: " + response.code() + ", Message: " + response.message());
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Error loading data", e);
            }
        });
    }

    private List<FiveminPriceEntry> parseFiveminJson(String json) {
        List<FiveminPriceEntry> prices = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<FiveminPriceEntry>>() {
            }.getType();
            prices = gson.fromJson(json, listType);
        } catch (JsonSyntaxException e) {
            Log.e("MainActivity", "Error parsing JSON", e);
        }
        return prices;
    }

    private void drawFiveminBarChart(List<FiveminPriceEntry> prices) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int[] colors = new int[prices.size()];

        for (int i = prices.size() - 1; i >= 0; i--) {
            FiveminPriceEntry entry = prices.get(i);
            double price = entry.getPrice();

            if (price < 5) {
                colors[i] = getResources().getColor(R.color.green);
            } else if (price >= 5 && price <= 14) {
                colors[i] = getResources().getColor(R.color.holo_yellow);
            } else {
                colors[i] = -65536;
            }

            entries.add(new BarEntry(i, (float) price));
            labels.add(FiveminconvertTime(entry.getMillisUTC()));

        }

        BarDataSet dataSet = new BarDataSet(entries, "Price Entries");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis rightYAxis = barChart.getAxisRight();
        rightYAxis.setEnabled(false);
        xAxis.setLabelRotationAngle(-45f);
        barChart.getLegend().setEnabled(false);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private static String FiveminconvertTime(long inputTime) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        format.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        Date date = new Date(inputTime);
        return format.format(date);
    }

    static class FiveminPriceEntry {
        private long millisUTC;
        private double price;

        public long getMillisUTC() {
            return millisUTC;
        }

        public void setMillisUTC(long millisUTC) {
            this.millisUTC = millisUTC;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    public void startToday(View view) {
        currentTodayDate = LocalDate.now();
        Todayformatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        startTodayActivity();
    }

    public void backTodayActivity(View view) {
        currentTodayDate = currentTodayDate.minusDays(1);
        startTodayActivity();
    }

    public void nextTodayActivity(View view) {
        currentTodayDate = currentTodayDate.plusDays(1);
        startTodayActivity();
    }

    public void startTodayActivity() {
        TodayBbutton = findViewById(R.id.backTodayButton);
        TodayBbutton = findViewById(R.id.backTodayButton);
        TodayNbutton = findViewById(R.id.nextTodayButton);
        TodayEbutton = findViewById(R.id.textTodayField);
        WeekBbutton = findViewById(R.id.backWeekButton);
        WeekNbutton = findViewById(R.id.nextWeekButton);
        WeekEbutton = findViewById(R.id.textField);
        MonthBbutton = findViewById(R.id.backMonthButton);
        MonthNbutton = findViewById(R.id.nextMonthButton);
        MonthEbutton = findViewById(R.id.textMonthField);
        Fbutton = findViewById(R.id.Minute);
        Tbutton = findViewById(R.id.Today);
        Wbutton = findViewById(R.id.Week);
        Mbutton = findViewById(R.id.Month);
        Tbutton.setBackgroundColor(Color.DKGRAY);
        Wbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Fbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Mbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        TodayBbutton.setVisibility(View.VISIBLE);
        TodayNbutton.setVisibility(View.VISIBLE);
        TodayEbutton.setVisibility(View.VISIBLE);
        WeekBbutton.setVisibility(View.INVISIBLE);
        WeekNbutton.setVisibility(View.INVISIBLE);
        WeekEbutton.setVisibility(View.INVISIBLE);
        MonthBbutton.setVisibility(View.INVISIBLE);
        MonthNbutton.setVisibility(View.INVISIBLE);
        MonthEbutton.setVisibility(View.INVISIBLE);
        if (String.valueOf(currentTodayDate).equals(String.valueOf(LocalDate.now()))) {
            TodayNbutton.setVisibility(View.INVISIBLE);
        }


        String formattedDate = currentTodayDate.format(Todayformatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH);
        TodayEbutton.setText(currentTodayDate.format(outputFormatter));

        String base_url = "https://hourlypricing.comed.com/api?type=day&date=";

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

        // Execute the API request asynchronously
        executor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    String json = responseBody.string();
                    List<DataPoint> dataPoints = parseJsonData(json);

                    if (dataPoints != null) {
                        runOnUiThread(() -> displayBarChart(dataPoints));
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

    public List<DataPoint> parseJsonData(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            List<DataPoint> dataPoints = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray dataPointArray = jsonArray.getJSONArray(i);
                String dataPointString = dataPointArray.toString();
                DataPoint dataPoint = parseData(dataPointString);
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

    public DataPoint parseData(String data) {
        try {
            String[] parts = data.split(",(?=[^,]*$)");
            String dateStr = parts[0];
            String valueStr = parts[1].replaceAll("[^\\d.]", "");
            double value = Double.parseDouble(valueStr);

            String[] dateComponents = dateStr.split("[()]")[1].split(",");
            String yearStr = dateComponents[0].replaceAll("[^\\d.]", "");
            int year = Integer.parseInt(yearStr);
            String monthStr = dateComponents[1].replaceAll("[^\\d.]", "");
            int month = Integer.parseInt(monthStr) - 1;
            String dayStr = dateComponents[2].replaceAll("[^\\d.]", "");
            int day = Integer.parseInt(dayStr);
            String hourStr = dateComponents[3].replaceAll("[^\\d.]", "");
            int hour = Integer.parseInt(hourStr);
            String minuteStr = dateComponents[4].replaceAll("[^\\d.]", "");
            int minute = Integer.parseInt(minuteStr);
            String secondStr = dateComponents[5].replaceAll("[^\\d.]", "");
            int second = Integer.parseInt(secondStr);

            // Create a Date object
            Date date = new Date(year - 1900, month, day, hour, minute, second);

            return new DataPoint(date, value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayBarChart(List<DataPoint> dataPoints) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int[] colors = new int[dataPoints.size()];

        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint dataPoint = dataPoints.get(i);
            entries.add(new BarEntry(i, (float) dataPoint.getValue()));
            float datapoint = (float) dataPoint.getValue();
            if (datapoint < 5) {
                colors[i] = getResources().getColor(R.color.green);
            } else if (datapoint >= 5 && datapoint <= 14) {
                colors[i] = getResources().getColor(R.color.holo_yellow);
            } else {
                colors[i] = -65536;
            }
            labels.add(formatDataPoint(dataPoint));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Data Points");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        YAxis rightYAxis = barChart.getAxisRight();
        rightYAxis.setEnabled(false);
        barChart.getLegend().setEnabled(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        barChart.invalidate();
    }

    private String formatDataPoint(DataPoint dataPoint) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ha", Locale.US);
        return dateFormat.format(dataPoint.getDate());
    }

    public static class DataPoint {
        private Date date;
        private double value;

        public DataPoint(Date date, double value) {
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

    public void startWeek(View view) {
        currentWeekDate = LocalDate.now();
        Weekformatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        startWeekPricesActivity();
    }

    public void backWeekActivity(View view) {
        currentWeekDate = currentWeekDate.minusWeeks(1);
        startWeekPricesActivity();
    }

    public void nextWeekActivity(View view) {
        currentWeekDate = currentWeekDate.plusWeeks(1);
        startWeekPricesActivity();
    }

    public void startWeekPricesActivity() {
        TodayBbutton = findViewById(R.id.backTodayButton);
        TodayNbutton = findViewById(R.id.nextTodayButton);
        TodayEbutton = findViewById(R.id.textTodayField);
        WeekBbutton = findViewById(R.id.backWeekButton);
        WeekNbutton = findViewById(R.id.nextWeekButton);
        WeekEbutton = findViewById(R.id.textField);
        MonthBbutton = findViewById(R.id.backMonthButton);
        MonthNbutton = findViewById(R.id.nextMonthButton);
        MonthEbutton = findViewById(R.id.textMonthField);
        Fbutton = findViewById(R.id.Minute);
        Tbutton = findViewById(R.id.Today);
        Wbutton = findViewById(R.id.Week);
        Mbutton = findViewById(R.id.Month);
        Tbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Wbutton.setBackgroundColor(Color.DKGRAY);
        Fbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Mbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        TodayBbutton.setVisibility(View.INVISIBLE);
        TodayNbutton.setVisibility(View.INVISIBLE);
        TodayEbutton.setVisibility(View.INVISIBLE);
        WeekBbutton.setVisibility(View.VISIBLE);
        WeekNbutton.setVisibility(View.VISIBLE);
        WeekEbutton.setVisibility(View.VISIBLE);
        MonthBbutton.setVisibility(View.INVISIBLE);
        MonthNbutton.setVisibility(View.INVISIBLE);
        MonthEbutton.setVisibility(View.INVISIBLE);
        if (String.valueOf(currentWeekDate).equals(String.valueOf(LocalDate.now()))) {
            WeekNbutton.setVisibility(View.INVISIBLE);
        }


        String formattedDate = currentWeekDate.format(Weekformatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH);
        WeekEbutton.setText(currentWeekDate.format(outputFormatter));

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
        executor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    String json = responseBody.string();
                    List<WeekPricesActivity.WeekDataPoint> dataPoints = parseWeekJsonData(json);
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

    public List<WeekPricesActivity.WeekDataPoint> parseWeekJsonData(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            List<WeekPricesActivity.WeekDataPoint> dataPoints = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray dataPointArray = jsonArray.getJSONArray(i);
                String dataPointString = dataPointArray.toString();
                WeekPricesActivity.WeekDataPoint dataPoint = parseWeekData(dataPointString);
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

    public WeekPricesActivity.WeekDataPoint parseWeekData(String data) {
        try {
            String[] parts = data.split(",(?=[^,]*$)");
            String dateStr = parts[0];
            String valueStr = parts[1].replaceAll("[^\\d.]", "");
            double value = Double.parseDouble(valueStr);
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
            Date date = new Date(year - 1900, month, day, hour, minute, second);

            return new WeekPricesActivity.WeekDataPoint(date, value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayWeekBarChart(List<WeekPricesActivity.WeekDataPoint> dataPoints) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int[] colors = new int[7];
        Arrays.fill(colors, Color.TRANSPARENT);
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < dataPoints.size(); i++) {
            WeekPricesActivity.WeekDataPoint dataPoint = dataPoints.get(i);
            entries.add(new BarEntry(i, (float) dataPoint.getValue()));
            float datapoint = (float) dataPoint.getValue();
            if (datapoint < 5) {
                colors[i] = getResources().getColor(R.color.green);
            } else if (datapoint >= 5 && datapoint <= 14) {
                colors[i] = getResources().getColor(R.color.holo_yellow);
            } else {
                colors[i] = -65536;
            }
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

        barChart.invalidate();
    }

    public void startMonth(View view) {
        currentMonthDate = LocalDate.now();
        startMonthPricesActivity();
    }

    public void backMonthActivity(View view) {
        currentMonthDate = currentMonthDate.minusMonths(1);
        startMonthPricesActivity();
    }

    public void nextMonthActivity(View view) {
        currentMonthDate = currentMonthDate.plusMonths(1);
        startMonthPricesActivity();
    }

    public void startMonthPricesActivity() {
        TodayBbutton = findViewById(R.id.backTodayButton);
        TodayNbutton = findViewById(R.id.nextTodayButton);
        TodayEbutton = findViewById(R.id.textTodayField);
        WeekBbutton = findViewById(R.id.backWeekButton);
        WeekNbutton = findViewById(R.id.nextWeekButton);
        WeekEbutton = findViewById(R.id.textField);
        MonthBbutton = findViewById(R.id.backMonthButton);
        MonthNbutton = findViewById(R.id.nextMonthButton);
        MonthEbutton = findViewById(R.id.textMonthField);
        Fbutton = findViewById(R.id.Minute);
        Tbutton = findViewById(R.id.Today);
        Wbutton = findViewById(R.id.Week);
        Mbutton = findViewById(R.id.Month);
        Tbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Wbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Fbutton.setBackgroundColor(getResources().getColor(R.color.purple));
        Mbutton.setBackgroundColor(Color.DKGRAY);
        TodayBbutton.setVisibility(View.INVISIBLE);
        TodayNbutton.setVisibility(View.INVISIBLE);
        TodayEbutton.setVisibility(View.INVISIBLE);
        WeekBbutton.setVisibility(View.INVISIBLE);
        WeekNbutton.setVisibility(View.INVISIBLE);
        WeekEbutton.setVisibility(View.INVISIBLE);
        MonthBbutton.setVisibility(View.VISIBLE);
        MonthNbutton.setVisibility(View.VISIBLE);
        MonthEbutton.setVisibility(View.VISIBLE);
        if (String.valueOf(currentMonthDate).equals(String.valueOf(LocalDate.now()))) {
            MonthNbutton.setVisibility(View.INVISIBLE);
        }
        String formattedDate = currentMonthDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
        MonthEbutton.setText(currentMonthDate.format(outputFormatter));

        String base_url = "https://hourlypricing.comed.com/api?type=month&date=";

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

        // Execute the API request asynchronously
        executor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    String json = responseBody.string();
                    List<MonthDataPoint> dataPoints = parseMonthJsonData(json);
                    if (dataPoints != null) {
                        runOnUiThread(() -> displayMonthBarChart(dataPoints));
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

    public List<MonthDataPoint> parseMonthJsonData(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            List<MonthDataPoint> dataPoints = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray dataPointArray = jsonArray.getJSONArray(i);
                String dataPointString = dataPointArray.toString();
                MonthDataPoint dataPoint = parseMonthData(dataPointString);
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

    public MonthDataPoint parseMonthData(String data) {
        try {
            String[] parts = data.split(",(?=[^,]*$)");
            String dateStr = parts[0];

            // Set valueStr to "0.0" if it is null or empty
            String valueStr = (parts.length > 1 && parts[1] != null) ? parts[1].replaceAll("[^\\d.]", "") : "0.0";

            double value = Double.parseDouble(valueStr);

            String[] dateComponents = dateStr.split("[()]")[1].split(",");
            String yearStr = dateComponents[0].replaceAll("[^\\d.]", "");
            int year = Integer.parseInt(yearStr);
            String monthStr = dateComponents[1].replaceAll("[^\\d.]", "");
            int month = Integer.parseInt(monthStr) - 1;
            String dayStr = dateComponents[2].replaceAll("[^\\d.]", "");
            int day = Integer.parseInt(dayStr);
            String hourStr = dateComponents[3].replaceAll("[^\\d.]", "");
            int hour = Integer.parseInt(hourStr);
            String minuteStr = dateComponents[4].replaceAll("[^\\d.]", "");
            int minute = Integer.parseInt(minuteStr);
            String secondStr = dateComponents[5].replaceAll("[^\\d.]", "");
            int second = Integer.parseInt(secondStr);

            // Create a Date object
            Date date = new Date(year - 1900, month, day, hour, minute, second);

            return new MonthDataPoint(date, value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayMonthBarChart(List<MonthDataPoint> dataPoints) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int[] colors = new int[dataPoints.size()];

        for (int i = 0; i < dataPoints.size(); i++) {
            MonthDataPoint dataPoint = dataPoints.get(i);
            float datapoint = (float) dataPoint.getValue();
            if (datapoint < 5) {
                colors[i] = getResources().getColor(R.color.green);
            } else if (datapoint >= 5 && datapoint <= 14) {
                colors[i] = getResources().getColor(R.color.holo_yellow);
            } else {
                colors[i] = -65536;
            }
            entries.add(new BarEntry(i + 1, (float) dataPoint.getValue()));
            labels.add(String.valueOf(i));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Data Points");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        YAxis rightYAxis = barChart.getAxisRight();
        rightYAxis.setEnabled(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getLegend().setEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        barChart.invalidate();
    }

    public static class MonthDataPoint {
        private Date date;
        private double value;

        public MonthDataPoint(Date date, double value) {
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