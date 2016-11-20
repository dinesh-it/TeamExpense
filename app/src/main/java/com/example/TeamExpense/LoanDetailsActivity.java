package com.example.TeamExpense;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.TeamExpense.list.Common;
import com.example.TeamExpense.list.MyRecyclerAdapter;
import com.example.myapp1.R;

import org.json.JSONException;
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
    private TextView list_header;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_recycler_list_layout);

        recycler_view = (RecyclerView)findViewById(R.id.recycler_list);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        list_header = (TextView) findViewById(R.id.list_view_title_text);

        db = new DatabaseHandler(this);

        String loan_from = "";
        String loan_to = "";
        String month_date = "";
        String balance_amt = "";
        String bal_in_float = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            loan_from = extras.getString("loan_from");
            loan_to = extras.getString("loan_to");
            month_date = extras.getString("month_date");
            balance_amt = extras.getString("balance_amt");
            bal_in_float = extras.getString("balance_amt_flt");
        }

        float bal_loan = Float.parseFloat(bal_in_float);

        String title_text = "Loan's From " + loan_from + " to " + loan_to;
        title_text += "\nBalance = " + balance_amt;
        list_header.setText(title_text);

        load_loan_data(month_date, loan_from, loan_to, bal_loan);

        LoanReCyclerAdapter.OnItemClickListener click_listener = new LoanReCyclerAdapter.OnItemClickListener() {
            @Override public void onItemClick(JSONObject exp) {
                try {
                    int id = Integer.parseInt(exp.getString("Id").toString());
                    Expense.showEditDialog(id,LoanDetailsActivity.this,db);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        loan_adapter = new LoanReCyclerAdapter(loan_data_set,click_listener);
        loan_adapter.notifyDataSetChanged();
        recycler_view.setAdapter(loan_adapter);
    }

    public void load_loan_data(String month_date, String loan_from,String loan_to, float bal_amt){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<Expense> expenses = db.getLoanDetails(month_date, loan_from, loan_to);

        if(expenses.size() < 1){
            return;
        }

        int i = 0;
        float prev_amt = 0;

        loan_data_set = new ArrayList(expenses.size());
        for ( Expense exp : expenses){
            try {
                String date_str = Expense.toDateString(exp.getDate());
                String cmt = exp.getComment();
                String date[] = date_str.split("/");
                float amt = exp.getAmt();

                JSONObject day_exp = new JSONObject();

                day_exp.put("Id",exp.id);
                day_exp.put("date_day", Expense.getWeekDay(date_str));
                day_exp.put("date_month", Expense.getStringMonth(date_str) + ", " + date[2]);
                day_exp.put("date", date[0]);

                String exp_loan_from = exp.getName();
                bal_amt += prev_amt;

                if(exp_loan_from.equalsIgnoreCase(loan_from)) {
                    day_exp.put("amt_color", "green");
                    prev_amt = amt * -1;  //to subtract

                }else{
                    day_exp.put("amt_color", "red");
                    prev_amt = amt;
                }

                day_exp.put("loan_from", exp_loan_from);
                day_exp.put("loan_to", exp.getSpentFor());
                day_exp.put("comments", cmt);

                day_exp.put("amount", Expense.toCurrencyWithSymbol(amt));


                day_exp.put("amt_balance", "Bal: " + Expense.toCurrency(bal_amt));

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
