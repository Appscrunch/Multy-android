package io.multy.ui.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.DataManager;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.AssetActivity;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import timber.log.Timber;

public class AssetTransactionsAdapter extends RecyclerView.Adapter<AssetTransactionsAdapter.Holder> {

    private List<TransactionHistory> transactions;

    public AssetTransactionsAdapter() {
        transactions = new ArrayList<>();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        DecimalFormat fiatFormat = new DecimalFormat("#.##");
        TransactionHistory transaction = transactions.get(position);
        boolean isInput = true;
        switch (transaction.getTxStatus()) {
            case Constants.TX_STATUS_IN_MEMPOOL_INCOMING:
                isInput = true;
                break;
            case Constants.TX_STATUS_IN_BLOCK_INCOMING:
                isInput = true;
                break;
            case Constants.TX_STATUS_IN_MEMPOOL_OUTCOMING:
                isInput = false;
                break;
            case Constants.TX_STATUS_IN_BLOCK_OUTCOMING:
                isInput = false;
                break;
            case Constants.TX_STATUS_IN_BLOCK_CONFIRMED:
                isInput = true;
                break;
            case Constants.TX_STATUS_REJECTED:
                isInput = false;
                break;
        }
        String addressFirst;
        String addressSecond;
        if (isInput) {
            if (transaction.getInputs().size() > Constants.ZERO) {
                addressFirst = transaction.getInputs().get(Constants.ZERO).getAddress();
                addressFirst = addressFirst.substring(Constants.ZERO, 6).concat(Constants.BULLETS_FIVE).concat(addressFirst.substring(addressFirst.length() - 6, addressFirst.length()));
                holder.address.setText(addressFirst);
                if (transaction.getInputs().size() > Constants.ONE) {
                    addressSecond = transaction.getInputs().get(transaction.getInputs().size() - 1).getAddress();
                    addressSecond = addressSecond.substring(Constants.ZERO, 6).concat(Constants.BULLETS_FIVE).concat(addressSecond.substring(addressSecond.length() - 6, addressSecond.length()));
                    holder.address.append("\n");
                    holder.address.append(addressSecond);
                }
            } else {
                Timber.e("There are no inputs inside %s transaction", transaction.getTxId());
            }
            holder.operationImage.setImageDrawable(holder.operationImage.getContext().getDrawable(R.drawable.ic_recieve));
        } else {
            if (transaction.getOutputs().size() > Constants.ZERO) {
                addressFirst = transaction.getOutputs().get(Constants.ZERO).getAddress();
                if (!addressFirst.equals(transaction.getAddress())) {
                    addressFirst = addressFirst.substring(Constants.ZERO, 6).concat(Constants.BULLETS_FIVE).concat(addressFirst.substring(addressFirst.length() - 6, addressFirst.length()));
                    holder.address.setText(addressFirst);
                }
                if (transaction.getOutputs().size() > Constants.ONE) {
                    addressSecond = transaction.getOutputs().get(transaction.getOutputs().size() - 1).getAddress();
                    if (!addressSecond.equals(transaction.getAddress())) {
                        addressSecond = addressSecond.substring(Constants.ZERO, 6).concat(Constants.BULLETS_FIVE).concat(addressSecond.substring(addressSecond.length() - 6, addressSecond.length()));
                        if (!TextUtils.isEmpty(holder.address.getText())) {
                            holder.address.append("\n");
                        }
                        holder.address.append(addressSecond);
                    }
                }
            } else {
                Timber.e("There are no inputs inside %s transaction", transaction.getTxId());
            }
            holder.operationImage.setImageDrawable(holder.operationImage.getContext().getDrawable(R.drawable.ic_send));
        }

        double amount = Double.parseDouble(transaction.getTxOutAmount()) / Math.pow(10, 8);

        holder.amount.setText(String.valueOf(amount));
        ArrayList<TransactionHistory.StockExchangeRate> stockRates = transaction.getStockExchangeRates();
        double fiatExchangePrice = stockRates != null && stockRates.size() > 0 ? stockRates.get(0).getBtcUsd() : 16000;
        String amountFiatString = fiatFormat.format(fiatExchangePrice * amount).concat(Constants.SPACE).concat(CurrencyCode.USD.name());
        holder.fiat.setText(amountFiatString);
//        holder.comment.setText(transaction.);

        Date date = new Date(transaction.getBlockTime());
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.ENGLISH);
        holder.time.setText(formatter.format(transaction.getBlockTime() * 1000));
//        formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
//        Timber.e("timestamp %s", transaction.getBlockTime());
//        Timber.e("date %s", formatter.format(date.getTime() * 1000));
//        holder.date.setText(formatter.format(transaction.getBlockTime() * 1000));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<TransactionHistory> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_date)
        TextView date;

        @BindView(R.id.image_operation)
        ImageView operationImage;

        @BindView(R.id.text_address)
        TextView address;

        @BindView(R.id.text_time)
        TextView time;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_fiat)
        TextView fiat;

        @BindView(R.id.text_comment)
        TextView comment;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
