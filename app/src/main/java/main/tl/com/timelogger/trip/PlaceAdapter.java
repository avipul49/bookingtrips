package main.tl.com.timelogger.trip;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.City;

public class PlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<City> mValues;
    private final View.OnClickListener mListener;
    final int TIME_ITEM = 0;

    public PlaceAdapter(List<City> items, View.OnClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TIME_ITEM:
                try {
                    Calendar cal1 = new GregorianCalendar();
                    Calendar cal2 = new GregorianCalendar();

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

                    Date date = sdf.parse(((City) mValues.get(position)).getStartDate());
                    cal1.setTime(date);
                    date = sdf.parse(((City) mValues.get(position)).getEndDate());
                    cal2.setTime(date);

                    //cal1.set(2008, 8, 1);
                    //cal2.set(2008, 9, 31);
                    ((ViewHolder) holder).mDays.setText(daysBetween(cal1.getTime(), cal2.getTime()) + " days");
                    ((ViewHolder) holder).mName.setText(((City) mValues.get(position)).getName());
                    ((ViewHolder) holder).mDate.setText("From " + ((City) mValues.get(position)).getStartDate() + " to " + ((City) mValues.get(position)).getEndDate());
                    ((ViewHolder) holder).mItem = (City) mValues.get(position);
                    ((ViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mListener) {
                                mListener.onClick(v);
                            }
                        }
                    });
                } catch (Exception ex) {
                }
                break;
        }


    }

    @Override
    public int getItemViewType(int position) {
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
        public final TextView mName;
        public City mItem;
        public final ImageView mMenu;
        public final TextView mDays;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mDate = (TextView) view.findViewById(R.id.date);
            mMenu = (ImageView) view.findViewById(R.id.menu);
            mDays = (TextView) view.findViewById(R.id.days);
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
                                    //mListener.deleteTime(mItem);
                                    break;
                                case R.id.edit:
                                    //mListener.editTime(mItem);
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

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
}
