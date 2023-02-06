package com.ruiguan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.ruiguan.R;

import java.text.DecimalFormat;
import java.util.List;

public class LineChartMarkView extends MarkerView {

    private TextView tvValue;
    private IAxisValueFormatter xAxisValueFormatter;
    private DecimalFormat tf = new DecimalFormat ("0.00");
    private DecimalFormat df = new DecimalFormat ("0.000");

    public LineChartMarkView(Context context, IAxisValueFormatter xAxisValueFormatter) {
        super (context, R.layout.layout_markview);
        this.xAxisValueFormatter = xAxisValueFormatter;
        tvValue = findViewById (R.id.tv_value);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        //展示自定义X轴值 后的X轴内容
        tvValue.setText (tf.format (e.getX())+" , "+df.format (e.getY ()));
        super.refreshContent (e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF (-(getWidth ()/2), -2*getHeight ());
    }
}