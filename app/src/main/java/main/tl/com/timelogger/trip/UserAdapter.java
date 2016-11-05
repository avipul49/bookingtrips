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

import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.util.ImageLoaderUtil;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> mValues;
    private final UserListFragment.OnUserListActionListener mListener;

    public UserAdapter(List<User> items, UserListFragment.OnUserListActionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.userName.setText(mValues.get(position).getName());
        holder.email.setText(mValues.get(position).getEmail());
        ImageLoaderUtil.displayImage(holder.itemView.getContext(), mValues.get(position).getImageURL(), holder.userImage);
        if (mValues.get(position).getUid().equals(User.getCurrentUser().getUid())) {
            holder.mMenu.setVisibility(View.VISIBLE);
        } else {
            holder.mMenu.setVisibility(View.INVISIBLE);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onSelectUser(holder.mItem);
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
        public final TextView userName;
        public final TextView email;
        public final ImageView userImage;
        public User mItem;
        public ImageView mMenu;
        public TextView manager;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            userName = (TextView) view.findViewById(R.id.user_name);
            email = (TextView) view.findViewById(R.id.email);
            userImage = (ImageView) view.findViewById(R.id.user_image);
            mMenu = (ImageView) view.findViewById(R.id.menu);

            mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    PopupMenu popup = new PopupMenu(view.getContext(), mMenu);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.user_item_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.delete:
                                    mListener.deleteUser(mItem);
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + email.getText() + "'";
        }
    }
}
