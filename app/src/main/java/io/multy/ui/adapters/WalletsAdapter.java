package io.multy.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.activities.AssetActivity;

/**
 * Created by appscrunch on 16.11.17.
 */

public class WalletsAdapter extends RecyclerView.Adapter<WalletsAdapter.Holder> {

//    ArrayList<AssetsInfo.Asset> data;

    public WalletsAdapter() {
//        data = AssetsInfo.getInstance().assets;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_asset_item, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
//        holder.name.setText(data.get(position).name);
//        holder.amount.setText(String.valueOf(data.get(position).amountCoin));
//        holder.equals.setText(String.valueOf(data.get(position).costUsd));
        holder.itemView.setOnClickListener(view -> {
            Context ctx = view.getContext();
            ctx.startActivity(new Intent(ctx, AssetActivity.class));
        });
    }

    @Override
    public int getItemCount() {
//        if (data != null) {
//            return data.size();
//        }
        return 10;
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView name;
        @BindView(R.id.text_amount)
        TextView amount;
        @BindView(R.id.text_equals)
        TextView equals;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
