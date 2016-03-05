package com.example.TeamExpense.list;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.TeamExpense.Expense;
import com.example.myapp1.R;

import org.json.JSONObject;

import java.util.ArrayList;


public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder> {
    private ArrayList<JSONObject> mDataset;
    //Activity activity;

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test,null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);


        return viewHolder;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyRecyclerAdapter(ArrayList<JSONObject> myDataset) {
        Log.e("Adapter called","~~~~~~~~~~~");
        mDataset = myDataset;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        final JSONObject exp = mDataset.get(position);

        try {
            Log.d("DD BIND", exp.get("date").toString());
            holder.spent_details.setText(exp.get("spent_details").toString());
            holder.date_month.setText((exp.get("date_month").toString()));
            holder.date_day.setText((exp.get("date_day").toString()));
            holder.total_exp.setText(exp.get("total").toString());
            holder.date.setText(exp.get("date").toString());
            holder.comments.setText(exp.get("comments").toString());
        }
        catch(Exception e){
            Log.e("onBind", e.getMessage());
        }


        //final int percent = (int)((amt / Common.max) * 100);
        //holder.text1.setText(name);
        //holder.text2.setText(amt + "");
        //holder.progressBar.setProgress(percent);

        /*holder.text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return Common.ExpenseSize;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView date, total_exp, comments, date_day, date_month, spent_details;
        //public ProgressBar progressBar;

        public CustomViewHolder(View view) {
            super(view);
            spent_details = (TextView) view.findViewById(R.id.spent_details);
            date_day = (TextView) view.findViewById(R.id.expense_date_day);
            date_month = (TextView) view.findViewById(R.id.expense_date_month);
            date = (TextView) view.findViewById(R.id.expense_date);
            total_exp = (TextView) view.findViewById(R.id.day_total_expense);
            comments = (TextView) view.findViewById(R.id.day_expense_comments);
            //progressBar = (ProgressBar) view.findViewById(R.id.p1);
        }
    }
}
