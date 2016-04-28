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

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View loan_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loan_detail_view,null);

        CustomViewHolder viewHolder = new CustomViewHolder(loan_view);

        return viewHolder;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LoanReCyclerAdapter(ArrayList<JSONObject> myDataset) {
        Log.e("DD", "Loan Detail Adapter called");
        loan_data_set = myDataset;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        final JSONObject exp = loan_data_set.get(position);

        try {
            //holder.date_day.setText((exp.get("date_day").toString()));
            holder.date.setText(exp.get("date").toString());
            holder.date_month.setText((exp.get("date_month").toString()));

            holder.loan_between.setText(exp.get("loan_between").toString());
            holder.comments.setText(exp.get("comments").toString());

            holder.amount.setText(exp.get("amount").toString());
            String color = exp.get("amt_color").toString();
            if(color.equalsIgnoreCase("green")) {
                holder.amount.setTextColor(Color.parseColor("#015029"));
            }
            else{
                holder.amount.setTextColor(Color.RED);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d("","");
        }
    }

    @Override
    public int getItemCount() {
        return Common.ExpenseSize;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView date, date_day, date_month, loan_between, comments, amount;

        public CustomViewHolder(View view) {
            super(view);

            date_day = (TextView) view.findViewById(R.id.expense_date_day);
            date = (TextView) view.findViewById(R.id.expense_date);
            date_month = (TextView) view.findViewById(R.id.expense_date_month);

            loan_between = (TextView) view.findViewById(R.id.loan_between_text);
            comments = (TextView) view.findViewById(R.id.loan_for_text);

            amount = (TextView) view.findViewById(R.id.loan_amount);
        }
    }
}