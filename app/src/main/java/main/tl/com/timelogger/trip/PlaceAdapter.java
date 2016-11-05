package main.tl.com.timelogger.trip;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.helper.ItemTouchHelperAdapter;
import main.tl.com.timelogger.helper.OnStartDragListener;
import main.tl.com.timelogger.model.City;
import main.tl.com.timelogger.model.Trip;

public class PlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private final List<City> mValues;
    private final View.OnClickListener mListener;
    final int TIME_ITEM = 0;
    Context context;
    private final OnStartDragListener mDragStartListener;
    private Trip trip;
    private TripDetails.OnPlacesActionListener onPlacesActionListener;
    private boolean inProgress = false;

    public PlaceAdapter(Context context, List<City> items, Trip trip, View.OnClickListener listener, TripDetails.OnPlacesActionListener onPlacesActionListener, OnStartDragListener dragStartListener) {
        mValues = items;
        mListener = listener;
        this.context = context;
        this.mDragStartListener = dragStartListener;
        this.trip = trip;
        this.onPlacesActionListener = onPlacesActionListener;
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
                    ((ViewHolder) holder).drag.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                                mDragStartListener.onStartDrag(holder);
                            }
                            return false;
                        }
                    });
                    ((ViewHolder) holder).mDays.setText(mValues.get(position).getDays() + " days");
                    ((ViewHolder) holder).mName.setText(((City) mValues.get(position)).getName());
                    ((ViewHolder) holder).mItem = (City) mValues.get(position);
                    ((ViewHolder) holder).flexible.setText(mValues.get(position).isFlexible() ? "Flexible" : "Not Flexible");

                    if (mValues.get(position).getDays() == 0) {
                        ((ViewHolder) holder).cost.setText("$ " + 0);
                    } else if (mValues.get(position).getReservation() != null && mValues.get(position).getReservation().getPrice() != 0) {
                        ((ViewHolder) holder).cost.setText("$ " + String.format("%.2f", mValues.get(position).getReservation().getPrice()));
                    } else {
                        ((ViewHolder) holder).cost.setText("");
                    }
                    //ImageLoaderUtil.displayImage(context, "https://farm9.staticflickr.com/8314/8024612029_bde3aa8f78.jpg", ((ViewHolder) holder).cityImage);
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        inProgress = true;
        mValues.get(fromPosition).setOrder(toPosition);
        mValues.get(toPosition).setOrder(fromPosition);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDatabase.child("trips").child(trip.getKey()).child("places").setValue(trip.getCities());
//                for (City city : trip.getCities()) {
//                    mDatabase.child("trips").child(trip.getKey()).child("places").child(city.getKey()).child("order").setValue(city.getOrder());
//                }
                inProgress = false;
            }
        }, 1000);
        Collections.swap(mValues, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
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
        public final ImageView cityImage;
        public final ImageView drag;
        public final TextView flexible;
        public final TextView cost;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mDate = (TextView) view.findViewById(R.id.startDate);
            mMenu = (ImageView) view.findViewById(R.id.menu);
            mDays = (TextView) view.findViewById(R.id.days);
            cityImage = (ImageView) view.findViewById(R.id.city_image);
            drag = (ImageView) view.findViewById(R.id.drag);
            flexible = (TextView) view.findViewById(R.id.flexible);
            cost = (TextView) view.findViewById(R.id.amount);
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
                                    if (onPlacesActionListener != null)
                                        onPlacesActionListener.deletePlace(mItem);
                                    break;
                                case R.id.edit:
                                    if (onPlacesActionListener != null)
                                        onPlacesActionListener.editPlace(mItem);
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

}
