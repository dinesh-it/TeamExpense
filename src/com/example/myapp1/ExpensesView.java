package com.example.myapp1;


import java.util.Calendar;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ExpensesView extends Activity {
	
	
	private ScrollView sv;
    private DatabaseHandler db;
    private String date, name, spent_for,opt;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    db = new DatabaseHandler(this);
	    sv = new ScrollView(getApplicationContext());
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	       date = extras.getString("sel_date");
	       name = extras.getString("sel_name");
	       spent_for = extras.getString("sel_spent_for");
	       opt = extras.getString("show_option");
	    }
	    if(opt.equals("month")){
	    	sv.addView(getExpenseTableView(db.getMonthExpenses(date)));
	    }
	    else if(opt.equals("filtered")){
			sv.addView(getExpenseTableView(db.getFilteredExpenses(date,name,spent_for)));
	    }
	    else{
			sv.addView(getExpenseTableView(db.getAllExpenses()));
	    }
        //sv.addView(btn1);
        setContentView(sv);
        //onTouchEvent();
        
        
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		int id = item.getItemId();
		if(id == R.id.action_cur_month){
			final Calendar calendar1 = Calendar.getInstance();
	        int year = calendar1.get(Calendar.YEAR);
	        int month = calendar1.get(Calendar.MONTH) + 1;
	        int day = calendar1.get(Calendar.DAY_OF_MONTH);
			sv.removeAllViews();
			sv.addView(getExpenseTableView(db.getMonthExpenses(day + "/" + month + "/" + year)));
			return true;
		}
		else if(id == R.id.action_show_all){
			sv.removeAllViews();
			sv.addView(getExpenseTableView(db.getAllExpenses()));
			return true;
		}
		else if(id == R.id.action_show_filtered){
			sv.removeAllViews();
			sv.addView(getExpenseTableView(db.getFilteredExpenses(date,name,spent_for)));
			return true;
		}
		else if(id == R.id.action_show_sel_month){
			sv.removeAllViews();
			sv.addView(getExpenseTableView(db.getMonthExpenses(date)));
			return true;
		}
		//setContentView(sv);
		return super.onOptionsItemSelected(item);
	}
	
	public TableLayout getExpenseTableView(List<Expense> expenses){
		TableLayout tableLayout;
		TableRow tableHead;
	    TextView textView;
	    
	    if(expenses.size() > 0){
		tableLayout = new TableLayout(getApplicationContext());
		
		//header
		tableHead = new TableRow(getApplicationContext());
		tableHead.setBackgroundColor(Color.LTGRAY);
		
		textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLACK);
        textView.setText("Date");
        textView.setPadding(10, 10, 10, 10);
        tableHead.addView(textView);
        
        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLACK);
        textView.setText("Spent By");
        textView.setPadding(1, 10, 10, 10);
        tableHead.addView(textView);
        
        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLACK);
        textView.setText("Spent For");
        textView.setPadding(1, 10, 10, 10);
        tableHead.addView(textView);
        
        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLACK);
        textView.setText("INR(Rs)");
        textView.setPadding(1, 10, 10, 10);
        tableHead.addView(textView);
        
        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLACK);
        textView.setText("     Comments");
        textView.setPadding(1, 10, 10, 10);
        tableHead.addView(textView);
        
        tableLayout.addView(tableHead);
        
        for (Expense texp : expenses) {
        	final TableRow tableRow = new TableRow(getApplicationContext());
            
            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLUE);
            textView.setText(Expense.toDateString(texp.getDate()));
            textView.setPadding(10, 10, 10, 10);
            tableRow.addView(textView);
            
            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(texp.getName());
            textView.setPadding(1, 10, 10, 10);
            textView.setWidth(130);
            textView.setMaxLines(4);
            textView.setSingleLine(false);
            tableRow.addView(textView);
            
            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.DKGRAY);
            textView.setText(texp.getSpentFor());
            textView.setPadding(1, 10, 10, 10);
            textView.setWidth(140);
            textView.setMaxLines(4);
            textView.setSingleLine(false);
            tableRow.addView(textView);
            
            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.RED);
            textView.setText(texp.getAmt().toString());
            textView.setPadding(5, 10, 10, 10);
            //textView.setPadding(left, top, right, bottom);
            textView.setWidth(100);
            textView.setMaxLines(4);
            textView.setSingleLine(false);
            tableRow.addView(textView);
            
            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.DKGRAY);
            textView.setText(texp.getComment());
            textView.setPadding(1, 10, 10, 10);
            tableRow.addView(textView);
            
            tableRow.setClickable(true);
            tableRow.setId(texp.getId());
            
            tableRow.setOnClickListener(new View.OnClickListener() {                      
                @Override
                public void onClick(View arg0) {
                	Expense e = db.getExpense(tableRow.getId());
                	String msg = "Spent on: " + Expense.toDateString(e.getDate()) +
                			"\nBy: "+ e.getName() + 
                			"\nFor: " + e.getSpentFor() + 
                			"\n  " + e.getComment() + 
                			"\nAmount: Rs. " + e.getAmt();
                	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExpensesView.this);
                	// set title
            		alertDialogBuilder.setTitle("Details");	
            		alertDialogBuilder
            		.setMessage(msg)
            		.setCancelable(false)      				
            		.setPositiveButton("Close",new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog,int id) {
            				dialog.cancel();
            			}
            		  })
            		.setNegativeButton("Delete",new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog,int id) {
            				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExpensesView.this);
                        	// set title
                    		alertDialogBuilder.setTitle("Delete?");	
                    		alertDialogBuilder
                    		.setMessage("Are you sure?\nYou want to delete this item?")
                    		.setCancelable(false)      				
                    		.setPositiveButton("Delete",new DialogInterface.OnClickListener() {
                    			public void onClick(DialogInterface dialog,int id) {
                    				if(db.deleteExpense(tableRow.getId())){
                    					Toast.makeText(ExpensesView.this, "Deleted Successfuly!", Toast.LENGTH_LONG).show();
                    					tableRow.removeAllViews();
                    				}
                    				else
                    					Toast.makeText(ExpensesView.this, "ERROR: Deleted failed", Toast.LENGTH_LONG).show();
                    			}
                    		  })
                    		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    			public void onClick(DialogInterface dialog,int id) {
                    				dialog.cancel();
                    			}
                    		});
                    		alertDialogBuilder.show();
            			}
            		});
            		alertDialogBuilder.show();
                }
            });
            tableLayout.addView(tableRow);
        }
	    }
	    else{
	    	tableLayout = new TableLayout(getApplicationContext());
	    	
	    	tableHead = new TableRow(getApplicationContext());
	    	textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.RED);
            textView.setText("No Data Available");
            textView.setPadding(10, 10, 10, 10);
            textView.setGravity(Gravity.CENTER);
            tableHead.addView(textView);
            tableHead.setGravity(Gravity.CENTER);
            
            tableLayout.addView(tableHead);
            
            tableHead = new TableRow(getApplicationContext());
	    	textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.GRAY);
            textView.setText("Filter will take month, name, Spent_for\nfrom the previous form.");
            textView.setPadding(10, 10, 10, 10);
            textView.setGravity(Gravity.CENTER);
            tableHead.addView(textView);
            tableHead.setGravity(Gravity.CENTER);
            
            tableLayout.addView(tableHead);
	    }
        return tableLayout;
	}

}
