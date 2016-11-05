package main.tl.com.timelogger.trip;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.Trip;

/**
 * Created by vipulmittal on 14/10/16.
 */
public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {

    private final List<Trip> mValues;
    private SelectOption selectOption;
    private Context context;
    private int selected = 0;

    public SuggestionsAdapter(Context context, List<Trip> items, SelectOption selectOption) {
        mValues = items;
        this.selectOption = selectOption;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.suggestions_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (position == 0)
            holder.name.setText("Your selection");
        else {
            holder.name.setText("Option " + position);
        }
        if (selected == position) {
            holder.mView.setBackgroundColor(context.getResources().getColor(R.color.lightBlue));
        } else {
            holder.mView.setBackgroundColor(Color.WHITE);
        }
        holder.position = position;
        holder.cost.setText(String.format("$ %.2f", mValues.get(position).getTotalCost()));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView name;
        public final TextView cost;
        public Trip mItem;
        public int position = 0;

        public ViewHolder(View view) {
            super(view);
            mView = view.findViewById(R.id.root);
            name = (TextView) view.findViewById(R.id.name);
            cost = (TextView) view.findViewById(R.id.cost);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected = position;
                    selectOption.onSelectedOption(mItem);
                }
            });
        }
    }

    interface SelectOption {
        void onSelectedOption(Trip trip);
    }
}
