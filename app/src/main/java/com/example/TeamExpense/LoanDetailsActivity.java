package com.example.TeamExpense;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.TeamExpense.list.Common;
import com.example.TeamExpense.list.MyRecyclerAdapter;
import com.example.myapp1.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dinesh on 27/4/16.
 */
public class LoanDetailsActivity extends Activity {
    private RecyclerView recycler_view;
    private LoanReCyclerAdapter loan_adapter;
    private ArrayList<JSONObject> loan_data_set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_recycler_list_layout);

        recycler_view = (RecyclerView)findViewById(R.id.recycler_list);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        String loan_from = "";
        String loan_to = "";
        String month_date = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            loan_from = extras.getString("loan_from");
            loan_to = extras.getString("loan_to");
            month_date = extras.getString("month_date");
        }

        load_loan_data(month_date, loan_from, loan_to);

        loan_adapter = new LoanReCyclerAdapter(loan_data_set);
        loan_adapter.notifyDataSetChanged();
        recycler_view.setAdapter(loan_adapter);
    }

    public void load_loan_data(String month_date, String loan_from,String loan_to){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<Expense> expenses = db.getLoanDetails(month_date, loan_from, loan_to);

        if(expenses.size() < 1){
            return;
        }

        int i = 0;

        loan_data_set = new ArrayList(expenses.size());
        for ( Expense exp : expenses){
            try {
                String date_str = Expense.toDateString(exp.getDate());
                String cmt = exp.getComment();
                String date[] = date_str.split("/");
                float amt = exp.getAmt();

                JSONObject day_exp = new JSONObject();

                day_exp.put("date_day", Expense.getWeekDay(date_str));
                day_exp.put("date_month", Expense.getStringMonth(date_str) + ", " + date[2]);
                day_exp.put("date", date[0]);

                String loan_between = exp.getName();

                if(loan_between.equalsIgnoreCase(loan_from)) {
                    day_exp.put("amt_color", "green");
                }else{
                    day_exp.put("amt_color", "red");
                }

                loan_between += " -> ";
                loan_between += exp.getSpentFor();

                day_exp.put("loan_between", loan_between);
                day_exp.put("comments", cmt);

                day_exp.put("amount", Expense.toCurrencyWithSymbol(amt));

                loan_data_set.add(i, day_exp);

                i++;
            }
            catch(Exception ex){
                ex.printStackTrace();
                //Log.e("Data Formation",ex.getStackTrace() + "");
            }
        }

        Common.ExpenseSize = i;
    }
}