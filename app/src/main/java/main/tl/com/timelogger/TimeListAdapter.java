package main.tl.com.timelogger;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import main.tl.com.timelogger.TimeListFragment.OnListFragmentInteractionListener;
import main.tl.com.timelogger.model.TimeEntry;
import main.tl.com.timelogger.model.WeekDetail;

public class TimeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<DisplayItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    final int TIME_ITEM = 0;
    final int WEEK_ITEM = 1;

    public TimeListAdapter(List<DisplayItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == WEEK_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.week_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.time_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case WEEK_ITEM:
                ((HeaderViewHolder) holder).mWeek.setText(((WeekDetail) mValues.get(position)).getStartDate() + " - " + ((WeekDetail) mValues.get(position)).getEndDate());
                ((HeaderViewHolder) holder).mDetails.setText("Average speed: " + ((WeekDetail) mValues.get(position)).getSpeed() + " Total distance: " + ((WeekDetail) mValues.get(position)).getTotalDistance());
                break;
            case TIME_ITEM:
                ((ViewHolder) holder).mSpeed.setText("Speed: " + ((TimeEntry) mValues.get(position)).getSpeed() + " Miles/Hour");
                ((ViewHolder) holder).mDistance.setText("Distance: " + ((TimeEntry) mValues.get(position)).getDistance() + " Miles");
                ((ViewHolder) holder).mDate.setText(((TimeEntry) mValues.get(position)).getDate());
                ((ViewHolder) holder).mItem = (TimeEntry) mValues.get(position);
                ((ViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            //mListener.deleteTime(((ViewHolder) holder).mItem);
                        }
                    }
                });
                break;
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (mValues.get(position) instanceof WeekDetail) {
            return WEEK_ITEM;
        }
        return TIME_ITEM;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView mWeek;
        public final TextView mDetails;

        public HeaderViewHolder(View view) {
            super(view);
            mWeek = (TextView) view.findViewById(R.id.week);
            mDetails = (TextView) view.findViewById(R.id.details);
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDate;
        public final TextView mSpeed;
        public final TextView mDistance;
        public TimeEntry mItem;
        public final ImageView mMenu;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSpeed = (TextView) view.findViewById(R.id.speed);
            mDistance = (TextView) view.findViewById(R.id.distance);
            mDate = (TextView) view.findViewById(R.id.date);
            mMenu = (ImageView) view.findViewById(R.id.menu);
            mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    PopupMenu popup = new PopupMenu(view.getContext(), mMenu);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.time_item_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.delete:
                                    mListener.deleteTime(mItem);
                                    break;
                                case R.id.edit:
                                    mListener.editTime(mItem);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });


        }

    }

    public interface DisplayItem {

    }
}
