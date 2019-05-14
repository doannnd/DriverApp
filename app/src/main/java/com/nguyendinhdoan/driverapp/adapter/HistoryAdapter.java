package com.nguyendinhdoan.driverapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.model.History;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<History> historyList;

    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_route, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        History history = historyList.get(position);

        viewHolder.dateTimeTextView.setText(history.getDateTime());
        viewHolder.startAddressTextView.setText(history.getStartAddress());
        viewHolder.endAddressTextView.setText(history.getEndAddress());
        viewHolder.tripPriceTextView.setText(context.getString(R.string.trip_price_text, history.getTripPrice()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTimeTextView;
        private TextView startAddressTextView;
        private TextView endAddressTextView;
        private TextView tripPriceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTimeTextView = itemView.findViewById(R.id.date_time_text_view);
            startAddressTextView = itemView.findViewById(R.id.tv_pick_up_address);
            endAddressTextView = itemView.findViewById(R.id.tv_drop_off_address);
            tripPriceTextView = itemView.findViewById(R.id.trip_price_text_view);
        }
    }
}
