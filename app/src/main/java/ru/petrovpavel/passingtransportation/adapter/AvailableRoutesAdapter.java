package ru.petrovpavel.passingtransportation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ru.petrovpavel.passingtransportation.R;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.listener.OnRouteItemClickListener;

public class AvailableRoutesAdapter extends RecyclerView.Adapter<AvailableRoutesAdapter.ViewHolder> {

    private List<Route> mData;
    private LayoutInflater mInflater;
    private OnRouteItemClickListener mClickListener;

    // data is passed into the constructor
    public AvailableRoutesAdapter(Context context, List<Route> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.available_routes_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mData != null) {
            Route route = mData.get(position);
            holder.originOfferView.setText((CharSequence) route.getOrigin().getAlias());
            holder.destinationOfferView.setText((CharSequence) route.getDestination().getAlias());
            holder.paymentOfferView.setText((CharSequence) route.getPayment().toString());
            holder.weightOfferView.setText((CharSequence) route.getWeight().toString());
            if(mClickListener != null){
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onItemClick(route);

                    }
                });
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setListener(OnRouteItemClickListener listener){
        this.mClickListener = listener;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView originOfferView;
        TextView destinationOfferView;
        TextView weightOfferView;
        TextView paymentOfferView;

        ViewHolder(View itemView) {
            super(itemView);
            originOfferView = itemView.findViewById(R.id.origin_offer);
            destinationOfferView = itemView.findViewById(R.id.destination_offer);
            weightOfferView = itemView.findViewById(R.id.weight_offer);
            paymentOfferView = itemView.findViewById(R.id.payment_offer);
        }
    }
}