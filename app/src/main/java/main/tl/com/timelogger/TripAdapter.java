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

import main.tl.com.timelogger.TripFragment.OnListFragmentInteractionListener;
import main.tl.com.timelogger.model.Trip;

public class TripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Trip> mValues;
    private final OnListFragmentInteractionListener mListener;
    final int TIME_ITEM = 0;

    public TripAdapter(List<Trip> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.time_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TIME_ITEM:
                ((ViewHolder) holder).mName.setText(((Trip) mValues.get(position)).getName());
                ((ViewHolder) holder).mDate.setText(((Trip) mValues.get(position)).getDate());
                ((ViewHolder) holder).mItem = (Trip) mValues.get(position);
                ((ViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onClick(((ViewHolder) holder).mItem);
                        }
                    }
                });
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
        public Trip mItem;
        public final ImageView mMenu;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mDate = (TextView) view.findViewById(R.id.date);
            mMenu = (ImageView) view.findViewById(R.id.menu);
            if (mMenu != null)
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
