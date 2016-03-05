package com.example.TeamExpense;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapp1.R;

import java.util.Calendar;
import java.util.List;

public class ExpensesView2 extends Activity {

    LinearLayout main_list_1, sub_list_1, sub_list_2, main_list;
    TableRow main_row_1, sub_row_1, sub_row_2;

    private String date_from, name, spent_for,opt;
    private boolean team_only;

    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_view_new2);

        db = new DatabaseHandler(this);

        main_list = (LinearLayout)findViewById(R.id.main_layout);
        main_list_1 = (LinearLayout)findViewById(R.id.sub_layout1);
        sub_list_1 = (LinearLayout)findViewById(R.id.sub_layout1_1);
        sub_list_2 = (LinearLayout)findViewById(R.id.sub_layout1_2);

        main_row_1 = (TableRow)findViewById(R.id.main_row_1);
        sub_row_1 = (TableRow)findViewById(R.id.sub_row_1);
        sub_row_2 = (TableRow)findViewById(R.id.sub_row_2);

        set_toggler(main_row_1, main_list_1);
        set_toggler(sub_row_1, sub_list_1);
        set_toggler(sub_row_2, sub_list_2);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            date_from = extras.getString("sel_date");
            name = extras.getString("sel_name");
            spent_for = extras.getString("sel_spent_for");
            team_only = extras.getBoolean("team_only");
            opt = extras.getString("show_option");
        }

        team_only = true;

        addExpenseListToLayout();
    }

    private void set_toggler(TableRow tr, final LinearLayout ll){
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (ll.isShown()) {
                    ll.setVisibility(View.GONE);
                } else {
                    ll.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void addExpenseListToLayout(){

        List<Expense> expenses = db.getMonthExpensesByDate(date_from, team_only);
        for (Expense texp : expenses) {
            main_list.addView(createRowView(texp));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public LinearLayout createRowView(final Expense texp){
        final LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setBackground(main_list_1.getBackground());

        final TableRow tableRow = new TableRow(getApplicationContext());
        tableRow.setBackground(main_row_1.getBackground());
        TextView textView;

        textView = new TextView(getApplicationContext());
        textView.setWidth(200);
        textView.setText(Expense.toDateString(texp.getDate()));
        //textView.setPadding(5, 5, 5, 5);
        tableRow.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setWidth(200);
        textView.setText(Expense.toCurrencyWithSymbol(texp.getAmt()));
        tableRow.addView(textView);

        tableRow.setClickable(true);
        //tableRow.setId(texp.getId());
        //tableRow.setBackgroundResource(android.R.drawable.list_selector_background);
        linearLayout.addView(tableRow);
        return linearLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expenses_view2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
