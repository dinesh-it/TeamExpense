package com.example.TeamExpense.list;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.TeamExpense.DatabaseHandler;
import com.example.TeamExpense.Expense;
import com.example.myapp1.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private RecyclerView recycler_view;
    private MyRecyclerAdapter adapter;
    private ArrayList<JSONObject> exp_data_set;
    private TextView list_header;
    private String date_from, date_to, name, spent_for, opt;
    private boolean team_only = false;
    private DatabaseHandler db;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_recycler_list_layout);

        recycler_view = (RecyclerView) findViewById(R.id.recycler_list);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        list_header = (TextView) findViewById(R.id.list_view_title_text);

        db = new DatabaseHandler(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            date_from = extras.getString("sel_date");
            name = extras.getString("sel_name");
            spent_for = extras.getString("sel_spent_for");
            team_only = extras.getBoolean("team_only");
            opt = extras.getString("show_option");
        }
        if (opt.equals("month")) {
            load_data(db.getMonthExpenses(date_from, team_only));
        } else if (opt.equals("filtered")) {
            load_data(db.getFilteredExpenses(date_from, name, spent_for, team_only));
        } else {
            load_data(db.getAllExpenses());
        }

        MyRecyclerAdapter.OnItemClickListener click_listener = new MyRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JSONObject exp) {
                try {
                    int id = Integer.parseInt(exp.getString("Id").toString());
                    Expense.showEditDialog(id, MainActivity.this, db);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        list_header.setText("Expense detail list");

        adapter = new MyRecyclerAdapter(exp_data_set, click_listener);
        adapter.notifyDataSetChanged();
        recycler_view.setAdapter(adapter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void load_data(List<Expense> expenses) {

        if (expenses.size() < 1) {
            return;
        }

        int i = 0;

        exp_data_set = new ArrayList(expenses.size());
        for (Expense exp : expenses) {
            try {

                String date_str = Expense.toDateString(exp.getDate());
                String cmt = exp.getComment();
                String date[] = date_str.split("/");
                float amt = exp.getAmt();

                JSONObject day_exp = new JSONObject();

                day_exp.put("Id", exp.getId());
                day_exp.put("date_day", Expense.getWeekDay(date_str));
                day_exp.put("date_month", Expense.getStringMonth(date_str) + ", " + date[2]);
                day_exp.put("date", date[0]);

                String exp_loan_from = exp.getName();

                day_exp.put("loan_from", exp_loan_from);
                day_exp.put("loan_to", exp.getSpentFor());
                day_exp.put("comments", cmt);

                day_exp.put("amount", Expense.toCurrencyWithSymbol(amt));

                day_exp.put("pmt_mode", exp.getPaymentMode());

                exp_data_set.add(i, day_exp);

                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
                //Log.e("Data Formation",ex.getStackTrace() + "");
            }
        }

        Common.ExpenseSize = i;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
