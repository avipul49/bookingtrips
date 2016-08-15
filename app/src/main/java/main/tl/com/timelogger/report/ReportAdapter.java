package main.tl.com.timelogger.report;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.TimeEntry;
import main.tl.com.timelogger.model.WeekDetail;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private final List<WeekDetail> mValues;
    private final ReportFragment.OnListFragmentInteractionListener mListener;

    public ReportAdapter(List<WeekDetail> items, ReportFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.update();
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public WeekDetail mItem;
        protected BarChart barChart;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            barChart = (BarChart) view.findViewById(R.id.chart);
            barChart.setDrawBarShadow(false);
            barChart.setDrawHighlightArrow(false);
            //barChart.setDrawGridBackground(false);
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisRight().setDrawGridLines(false);
            barChart.setDescription("");
            //barChart.getXAxis().setDrawGridLines(false);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void update() {
            mIdView.setText(mItem.getStartDate() + " - " + mItem.getEndDate());
            mContentView.setText("Average speed: " + mItem.getSpeed() + " Miles/Hour");
            ArrayList<BarEntry> entries = new ArrayList<>();
            int i = 0;
            ArrayList<String> labels = new ArrayList<>();

            for (TimeEntry timeEntry : mItem.getTimeEntries()) {
                entries.add(new BarEntry((float) timeEntry.getSpeed(), i++));
                labels.add(timeEntry.getDate().split("/")[1]);
            }

            BarDataSet dataset = new BarDataSet(entries, " Speed per day");
            BarData data = new BarData(labels, dataset);
            barChart.setData(data);
            barChart.getData().setHighlightEnabled(false);
        }
    }
}

