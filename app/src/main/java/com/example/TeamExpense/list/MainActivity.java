package com.example.TeamExpense.list;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.example.TeamExpense.DatabaseHandler;
import com.example.TeamExpense.Expense;
import com.example.TeamExpense.ExpenseStatus;
import com.example.myapp1.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity {
    private RecyclerView mRecyclerView,mRecyclerView2,mRecyclerView3;
    private MyRecyclerAdapter adapter,adapter2,adapter3;
    private ArrayList<JSONObject> mDataset;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.recyclerviewtest);

        mRecyclerView = (RecyclerView)findViewById(R.id.recycleriew);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String date_from = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            date_from = extras.getString("sel_date");
        }

        load_data(date_from);

        adapter = new MyRecyclerAdapter(mDataset);
        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);
    }

    public void load_data(String date_from){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<Expense> expenses = db.getMonthExpenses(date_from, true);

        if(expenses.size() < 1){;
            return;
        };

        String prev_date_str = "";
        String comments = "";
        float amt = 0;
        int i = 0;

        mDataset = new ArrayList(expenses.size());
        JSONObject name_vise = new JSONObject();
        for ( Expense exp : expenses){
            try {
                float n_amt = exp.getAmt();
                String name = exp.getName();

                if(n_amt == 0){
                    continue;
                }

                if(!name_vise.isNull(name)){
                    name_vise.put(name, Float.parseFloat(name_vise.get(name).toString()) + n_amt);
                }
                else{
                    name_vise.put(name,n_amt);
                }

                String date_str = Expense.toDateString(exp.getDate());

                amt = amt + n_amt;
                String cmt = exp.getComment();
                comments = comments + exp.getSpentFor();

                if(cmt != null && !cmt.equalsIgnoreCase("")){
                    comments += " (" + cmt + ")";
                }

                comments += ", ";

                if(!prev_date_str.equalsIgnoreCase(date_str)) {
                    prev_date_str = date_str;
                    String date[] = date_str.split("/");
                    String spent_details = "";

                    Iterator<String> names = name_vise.keys();
                    while (names.hasNext()) {
                        String key = names.next();
                        try {
                            Object value = name_vise.get(key);
                            spent_details += key + "(" + value.toString() + "), ";

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject day_exp = new JSONObject();
                    day_exp.put("spent_details",spent_details);
                    day_exp.put("date_day", Expense.getWeekDay(date_str));
                    day_exp.put("date_month", Expense.getStringMonth(date_str) + ", " + date[2]);
                    day_exp.put("date", date[0]);
                    day_exp.put("total", Expense.toCurrencyWithSymbol(amt));
                    day_exp.put("comments", comments);

                    Log.d("DD", "" + date_str + "," + amt + "," + comments);

                    mDataset.add(i, day_exp);

                    i++;
                    amt = 0;
                    comments = "";
                    name_vise = new JSONObject();
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
                //Log.e("Data Formation",ex.getStackTrace() + "");
            }
        }

        Common.ExpenseSize = i;
    }
}
