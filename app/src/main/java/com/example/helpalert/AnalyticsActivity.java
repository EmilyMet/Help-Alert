package com.example.helpalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    String id;
    ArrayList<Alert> alertList = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    ArrayList<String> counties = new ArrayList<>();
    HashMap<Integer, Integer> alertCountMap = new HashMap<>();
    DatabaseReference reffAlerts;
    int sun=0, mon=0, tue=0, wed=0, thu=0, fri=0, sat=0;
    String dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_analytics);
        mAuth = FirebaseAuth.getInstance();
        reffAlerts = FirebaseDatabase.getInstance("https://help-alert-c5e2d-default-rtdb.europe-west1.firebasedatabase.app").getReference("Alerts");

        navigationView = findViewById(R.id.navigation);
        navigationView.setSelectedItemId(R.id.analytics);
        navigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.buttonTrack:
                        // Handle the home button click
                        startActivity(new Intent(AnalyticsActivity.this, MainActivity.class));
                        return true;
                    case R.id.mapTrack:
                        // Handle the dashboard button click
                        startActivity(new Intent(AnalyticsActivity.this, MapTracking.class));
                        return true;
                    case R.id.analytics:
                        // Handle the notifications button click
                        startActivity(new Intent(AnalyticsActivity.this, AnalyticsActivity.class));
                        return true;
                    case R.id.account:
                        // Handle the notifications button click
                        startActivity(new Intent(AnalyticsActivity.this, AccountSettings.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            id = firebaseUser.getUid();
        }

        counties.addAll(Arrays.asList("Antrim", "Armagh", "Carlow", "Cavan", "Clare","Cork","Derry", "Donegal", "Down", "Dublin",
        "Fermanagh", "Galway", "Kerry", "Kildare", "Kilkenny", "Laois", "Leitrim", "Limerick", "Longford", "Louth", "Mayo", "Meath",
                "Monaghan", "Offaly", "Roscommon", "Sligo", "Tipperary", "Tyrone", "Waterford", "Westmeath", "Wexford", "Wicklow"));

        reffAlerts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    // Get the value of the child node as a Java object
                    Alert alert = childSnapshot.getValue(Alert.class);
                    alertList.add(alert);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(alert.getDate());
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    if (alertCountMap.containsKey(hour)) {
                        int count = alertCountMap.get(hour);
                        alertCountMap.put(hour, count + 1);
                    } else {
                        alertCountMap.put(hour, 1);
                    }

                    dayOfWeek = alert.getDayString();
                    switch (dayOfWeek) {
                        case "Sunday":
                            sun++;
                            break;
                        case "Monday":
                            mon++;
                            break;
                        case "Tuesday":
                            tue++;
                            break;
                        case "Wednesday":
                            wed++;
                            break;
                        case "Thursday":
                            thu++;
                            break;
                        case "Friday":
                            fri++;
                            break;
                        case "Saturday":
                            sat++;
                            break;
                        default:
                            // Handle unknown day
                            break;
                    }

                    BarChart barChart = findViewById(R.id.barChart);
                    ArrayList<BarEntry> alertsChart = new ArrayList<BarEntry>();
                    alertsChart.add(new BarEntry(1f, sun));
                    alertsChart.add(new BarEntry(2f, mon));
                    alertsChart.add(new BarEntry(3f, tue));
                    alertsChart.add(new BarEntry(4f, wed));
                    alertsChart.add(new BarEntry(5f, thu));
                    alertsChart.add(new BarEntry(6f, fri));
                    alertsChart.add(new BarEntry(7f, sat));

                    BarDataSet barDataSet = new BarDataSet(alertsChart, "Alerts");
                    barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    barDataSet.setValueTextSize(16f);

                    BarData barData = new BarData(barDataSet);
                    barChart.setFitBars(true);
                    barChart.setData(barData);
                    barChart.getDescription().setEnabled(false);
                    barChart.animateY(2000);

                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[] { "Days","Sun","Mon", "Tues", "Wed", "Thur", "Fri", "Sat" })); // Set the x-axis labels to custom strings
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setDrawGridLines(false);
                    xAxis.setDrawAxisLine(false);


                    // Step 2: Count the number of alerts that occurred at each time
                    HashMap<String, Integer> timeCountMap = new HashMap<>();
                    for (String time : times) {
                        int count = 0;
                        for (Alert a : alertList) {
                            if (a.getTimeString().equals(time)) {
                                count++;
                            }
                        }
                        timeCountMap.put(time, count);
                    }


                    LineChart lineChart = findViewById(R.id.lineChart);

                    ArrayList<Entry> entries = new ArrayList<>();
                    for (int hr = 0; hr < 24; hr++) {
                        if (alertCountMap.containsKey(hr)) {
                            int count = alertCountMap.get(hr);
                            entries.add(new Entry(hr, count));
                        } else {
                            entries.add(new Entry(hr, 0));
                        }
                    }

                    LineDataSet lineDataSet = new LineDataSet(entries, "Alert Frequency");
                    lineDataSet.setLineWidth(2);
                    lineDataSet.setCircleRadius(6);
                    lineDataSet.setCircleColor(Color.BLUE);
                    lineDataSet.setDrawCircleHole(false);
                    lineDataSet.setColor(Color.BLUE);
                    lineDataSet.setDrawValues(false);

                    LineData lineData = new LineData(lineDataSet);

                    lineChart.setData(lineData);

                    lineChart.getXAxis().setGranularity(1f);
                    lineChart.getXAxis().setGridLineWidth(1f); // set the grid line width to 1 pixel
                    lineChart.getXAxis().setDrawGridLines(true); // enable vertical grid lines
                    lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int hour = (int) value;
                            String amPm = hour < 12 ? "AM" : "PM";
                            hour = hour % 12;
                            if (hour == 0) {
                                hour = 12;
                            }
                            return String.format("%d %s", hour, amPm);
                        }
                    });
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);
                    lineChart.invalidate();
                    lineChart.setDrawGridBackground(true);
                    lineChart.setGridBackgroundColor(Color.WHITE);
                    lineChart.getXAxis().setDrawGridLines(true);
                    lineChart.getAxisLeft().setDrawGridLines(true);
                    lineChart.getAxisRight().setDrawGridLines(true);



                    HashMap<String, Integer> countyFrequency = new HashMap<>();
                    String address;
                    for (String county : counties) {
                        int count = 0;
                        for (Alert a : alertList) {
                            address = a.getAddress();
                            if(address.contains(county)){
                                count++;
                            }
                        }
                        countyFrequency.put(county, count);
                    }


                    List<PieEntry> pieEntries = new ArrayList<>();
                    for (String c : countyFrequency.keySet()) {
                        int count = countyFrequency.get(c);
                        if(count > 0) {
                            pieEntries.add(new PieEntry(count, c));
                        }
                    }

                    // Create a PieDataSet with the PieEntry list
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "Alerts by County");
                    pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    pieDataSet.setValueTextColor(Color.BLACK);
                    pieDataSet.setValueTextSize(16f);

                    // Create a PieData object with the PieDataSet
                    PieData pieData = new PieData(pieDataSet);

                    // Create a PieChart and set its data
                    PieChart pieChart = findViewById(R.id.pieChart);
                    pieChart.setData(pieData);
                    pieChart.getDescription().setEnabled(false);
                    pieChart.animateY(2000);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("AnalyticsActivity", error.toException());
            }
        });
    }
}