package main.tl.com.timelogger.trip;

/**
 * Created by vipulmittal on 15/08/16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.Message;
import main.tl.com.timelogger.model.User;

/**
 * Created by vipulmittal on 10/08/16.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    List<Message> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public MessageAdapter(Context context, ArrayList<Message> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        switch (data.get(position).getType()) {
            case "ACTION":
                return 0;
            case "TEXT":
                if (data.get(position).getUserId().equals(User.getCurrentUser().getUid())) {
                    return 1;
                }
                return 2;
        }
        return 0;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.message_view_action;
        switch (viewType) {
            case 1:
                layout = R.layout.message_view_sent;
                break;
            case 2:
                layout = R.layout.message_view_received;
                break;
        }
        View view = inflater.inflate(layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Message current = data.get(position);
        holder.text.setText(current.getMessage());
        SimpleDateFormat outgoingFormat = new SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        switch (getItemViewType(position)) {
            case 1:
                holder.time.setText(outgoingFormat.format(new Date(current.getDate())));
                break;
            case 2:
                holder.name.setText(current.getUserName());
                holder.time.setText(outgoingFormat.format(new Date(current.getDate())));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        TextView time;
        TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            time = (TextView) itemView.findViewById(R.id.time);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
