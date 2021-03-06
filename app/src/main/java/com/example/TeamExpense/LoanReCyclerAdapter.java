package com.example.TeamExpense;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.TeamExpense.list.Common;
import com.example.myapp1.R;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dinesh on 27/4/16.
 */
public class LoanReCyclerAdapter extends RecyclerView.Adapter<LoanReCyclerAdapter.CustomViewHolder> {
    private ArrayList<JSONObject> loan_data_set;
    //Activity activity;

    public interface OnItemClickListener {
        void onItemClick(JSONObject clicked_exp);
    }

    private final OnItemClickListener list_click_listener;

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View loan_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loan_detail_view,null);

        CustomViewHolder viewHolder = new CustomViewHolder(loan_view);

        return viewHolder;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LoanReCyclerAdapter(ArrayList<JSONObject> myDataset, OnItemClickListener listener) {
        Log.i("DD", "Loan Detail Adapter called");
        loan_data_set = myDataset;
        list_click_listener = listener;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        final JSONObject exp = loan_data_set.get(position);

        try {
            holder.bind(loan_data_set.get(position), list_click_listener);
            holder.date_day.setText((exp.get("date_day").toString()));
            holder.date.setText(exp.get("date").toString());
            holder.date_month.setText((exp.get("date_month").toString()));

            holder.loan_from.setText(exp.get("loan_from").toString());
            holder.loan_to.setText(exp.get("loan_to").toString());
            holder.comments.setText(exp.get("comments").toString());

            holder.amount.setText(exp.get("amount").toString());
            String color = exp.get("amt_color").toString();
            if(color.equalsIgnoreCase("green")) {
                holder.amount.setTextColor(Color.parseColor("#00733a"));
            }
            else{
                holder.amount.setTextColor(Color.RED);
            }

            String pmt_mode = exp.get("pmt_mode").toString();
            holder.pmt_mode.setText(pmt_mode);

            if(pmt_mode.equalsIgnoreCase("CASH")) {
                holder.pmt_mode.setTextColor(Color.parseColor("#dd4535"));
            }
            else if(pmt_mode.equalsIgnoreCase("CC")){
                holder.pmt_mode.setTextColor(Color.parseColor("#37a753"));
            }
            else if(pmt_mode.equalsIgnoreCase("DC")){
                holder.pmt_mode.setTextColor(Color.parseColor("#f5bd17"));
            }
            else if(pmt_mode.equalsIgnoreCase("ONLINE")){
                holder.pmt_mode.setTextColor(Color.parseColor("#4483ea"));
            }

            holder.amt_bal.setText("Tot: " + exp.get("amt_balance").toString());
            holder.joiner.setText("to");
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d("","");
        }
    }

    @Override
    public int getItemCount() {
        return loan_data_set.size();
        //return Common.ExpenseSize;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView date, date_day, date_month, loan_from, loan_to, comments, amount, amt_bal, joiner, pmt_mode;

        public CustomViewHolder(View view) {
            super(view);

            date_day = (TextView) view.findViewById(R.id.expense_date_day);
            date = (TextView) view.findViewById(R.id.expense_date);
            date_month = (TextView) view.findViewById(R.id.expense_date_month);

            loan_from = (TextView) view.findViewById(R.id.loan_from);
            loan_to = (TextView) view.findViewById(R.id.loan_to);
            comments = (TextView) view.findViewById(R.id.loan_for_text);

            amount = (TextView) view.findViewById(R.id.loan_amount);
            amt_bal = (TextView) view.findViewById(R.id.loan_amount_balance);
            joiner = (TextView) view.findViewById(R.id.exp_joiner);
            pmt_mode = (TextView) view.findViewById(R.id.pmt_mode);
        }

        public void bind(final JSONObject item, final OnItemClickListener listener) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemClick(item);
                    return true;
                }
            });
        }
    }
}
