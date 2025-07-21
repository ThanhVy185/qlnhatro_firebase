package com.huflit.qlnhatro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private EditText etStartDate, etEndDate;
    private Button btnFilterDaily, btnFilterMonthly;
    private BarChart barChartDaily;
    private LineChart lineChartMonthly;
    private Spinner spnMonth, spnYear;

    private final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Bind views
        etStartDate      = findViewById(R.id.etStartDate);
        etEndDate        = findViewById(R.id.etEndDate);
        btnFilterDaily   = findViewById(R.id.btnFilterDaily);
        barChartDaily    = findViewById(R.id.barChartDaily);
        spnMonth         = findViewById(R.id.spnMonth);
        spnYear          = findViewById(R.id.spnYear);
        btnFilterMonthly = findViewById(R.id.btnFilterMonthly);
        lineChartMonthly = findViewById(R.id.lineChartMonthly);

        // Disable description text and enable legend on charts
        barChartDaily.getDescription().setEnabled(false);
        barChartDaily.getLegend().setEnabled(true);
        lineChartMonthly.getDescription().setEnabled(false);
        lineChartMonthly.getLegend().setEnabled(true);

        // Initialize date fields to today
        String today = DATE_FORMAT.format(calendar.getTime());
        etStartDate.setText(today);
        etEndDate.setText(today);

        // DatePicker dialogs
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // Month spinner (1–12)
        List<String> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) months.add(String.valueOf(m));
        spnMonth.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months));

        // Year spinner (last 5 years)
        int currentYear = calendar.get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear; y++) {
            years.add(String.valueOf(y));
        }
        spnYear.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years));

        // Filter daily reportttt
        btnFilterDaily.setOnClickListener(v -> {
            try {
                Date from = DATE_FORMAT.parse(etStartDate.getText().toString());
                Date to   = DATE_FORMAT.parse(etEndDate.getText().toString());
                if (from.after(to)) {
                    Toast.makeText(this,
                            "Ngày bắt đầu phải trước ngày kết thúc",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                showDailyChart(from, to);
            } catch (ParseException ex) {
                Toast.makeText(this,
                        "Định dạng ngày không hợp lệ",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Filter monthly report
        btnFilterMonthly.setOnClickListener(v -> {
            int monthIndex = spnMonth.getSelectedItemPosition(); // 0-based
            int year = Integer.parseInt((String) spnYear.getSelectedItem());
            showMonthlyChart(monthIndex, year);
        });
    }

    private void showDatePicker(final EditText target) {
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (DatePicker dp, int yy, int mm, int dd) -> {
            Calendar c = Calendar.getInstance();
            c.set(yy, mm, dd);
            target.setText(DATE_FORMAT.format(c.getTime()));
        }, y, m, d).show();
    }

    private void showDailyChart(Date from, Date to) {
        // Mock data: replace with real query
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setTime(from);
        int idx = 0;
        while (!c.getTime().after(to)) {
            float value = (float)(Math.random() * 1000);
            entries.add(new BarEntry(idx, value));
            labels.add(c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1));
            c.add(Calendar.DATE, 1);
            idx++;
        }
        BarDataSet set = new BarDataSet(entries, "Doanh thu");
        set.setColor(getResources().getColor(R.color.orange_500, null));
        BarData data = new BarData(set);
        barChartDaily.setData(data);

        // X-axis labels
        barChartDaily.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartDaily.invalidate();
    }

    private void showMonthlyChart(int monthIdx, int year) {
        // Mock data: replace with real query
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(year, monthIdx, 1);
        int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= days; i++) {
            float value = (float)(Math.random() * 1000);
            entries.add(new Entry(i - 1, value));
            labels.add(String.valueOf(i));
        }
        LineDataSet set = new LineDataSet(entries, "Doanh thu");
        set.setColor(getResources().getColor(R.color.teal_700, null));
        set.setCircleColor(getResources().getColor(R.color.teal_700, null));
        LineData data = new LineData(set);
        lineChartMonthly.setData(data);

        // X-axis labels
        lineChartMonthly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartMonthly.invalidate();
    }
}
