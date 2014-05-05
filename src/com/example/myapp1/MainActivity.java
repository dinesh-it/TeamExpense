package com.example.myapp1;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;


public class MainActivity extends Activity {

	 private Button btn1, btn2;
	 private EditText desc, amt;
	 public EditText date_field;
	 public AutoCompleteTextView spent_for, name;
	 private TextView status;
	 private DatePicker date_picker;
	 private TableLayout status_table;
	 
	 
	 private int year;
	 private int month;
	 private int day;
	 
	 static final int DATE_DIALOG_ID = 100;
	 private DatabaseHandler db;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date_field = (EditText)findViewById(R.id.editText1);
        date_field.setSingleLine(true);
        name = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
        name.setSingleLine(true);
        spent_for = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView2);
        spent_for.setSingleLine(true);
        desc = (EditText)findViewById(R.id.editText4);
        amt = (EditText)findViewById(R.id.editText5);
        amt.setSingleLine(true);
        status = (TextView)findViewById(R.id.textView2);
        btn1 = (Button)findViewById(R.id.button1);
        btn2 = (Button)findViewById(R.id.button2);
        date_picker = (DatePicker)findViewById(R.id.datePicker1);
        status_table = (TableLayout)findViewById(R.id.tableLayout);
        final Calendar calendar1 = Calendar.getInstance();
        year = calendar1.get(Calendar.YEAR);
        month = calendar1.get(Calendar.MONTH);
        day = calendar1.get(Calendar.DAY_OF_MONTH);
        date_field.setText(new StringBuilder().append(day).append('/').append(month+1).append('/').append(year));
        date_picker.init(year, month, day,null);
        date_picker.removeAllViews();
        db = new DatabaseHandler(this);
        setAutoCompletes();
        showStatus();
        name.requestFocus();
        this.addListenerOnButton();
        this.addListenerOnDateField();
    }
    
    public void addListenerOnDateField(){
    	date_field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					showDialog(DATE_DIALOG_ID);
				}
			}
    	});
    }
    
    public void setAutoCompletes(){
    	String[] name_list = db.getAllNames().clone();
        ArrayAdapter<String> names_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_list);
        name.setAdapter(names_adapter);
        String[] items_list = db.getAllItems().clone();
        ArrayAdapter<String> items_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items_list);
        spent_for.setAdapter(items_adapter);
    }

    public void addListenerOnButton(){
    	
    	// Save button action
    	btn1.setOnClickListener(new OnClickListener(){
    			public void onClick(View view){
    				if(date_field.getText() + "" == "" || name.getText() + "" == "" || spent_for.getText() + "" == "" || amt.getText() + "" == ""){
    					Toast.makeText(MainActivity.this, "ERROR: Incomplete form", Toast.LENGTH_LONG).show();
    				}
    				else {
    					saveExpense();
    				}
    		   }
    	});
    	
    	// View Transaction button action
    	btn2.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				showList(date_field.getText().toString(),name.getText().toString(),spent_for.getText().toString(),"month");
		   }
	});
    }
    
    private void saveExpense(){
    	long epoch_date = Expense.toEpoch(date_field.getText().toString());
		Expense exp = new Expense(name.getText().toString(),
				epoch_date,
				spent_for.getText().toString(),
				desc.getText().toString(),
				Float.parseFloat(""+amt.getText()));
		db.addExpense(exp);
		Toast.makeText(MainActivity.this, "Saved Successfuly", Toast.LENGTH_LONG).show();
		spent_for.setText("");
		desc.setText("");
		amt.setText("");
		showStatus();
		setAutoCompletes();
    }
    
    private void showList(String date,String name,String spent_for,String opt){
    	Intent listIntent = new Intent(MainActivity.this, ExpensesView.class);
    	if(date.equals(""))
    		date = day + "/" + month + "/" + year;
    	date_field.setText(date);
		listIntent.putExtra("sel_date",date);
		listIntent.putExtra("sel_name",name);
		listIntent.putExtra("sel_spent_for",spent_for);
		listIntent.putExtra("show_option", opt);
		MainActivity.this.startActivity(listIntent);
    }
    
    public void onBackPressed() {
    	if(name.getText() + "" == "" && spent_for.getText() + "" == "" && amt.getText() + "" == ""){
    		MainActivity.this.finish();
    	}
    	else {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
    	// set title
		alertDialogBuilder.setTitle("EXIT?");	
		alertDialogBuilder
		.setMessage("Your entries will be lost.\nDo you really want to exit?")
		.setCancelable(false)      				
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				MainActivity.this.finish();
			}
		  })
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});
		
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    	}
    }

    
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    */
    
    
    @Override
        protected Dialog onCreateDialog(int id) {
    	switch (id) {
            case DATE_DIALOG_ID:
               // set date picker as current date
               return new DatePickerDialog(this, datePickerListener, year, month,day);
            }
            return null;
        }
        private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear,int selectedMonth, int selectedDay) {
            	int prev_month = month;
            	int prev_year = year;
                year = selectedYear;
                month = selectedMonth;
                day = selectedDay;
                // set selected date into Text View
                date_field.setText(new StringBuilder().append(day).append('/').append(month+1).append('/').append(year));
                // set selected date into Date Picker
                date_picker.init(year, month, day, null);
                if(month != prev_month || year != prev_year)
            		showStatus();
            }
        };
        
        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
        	if(hasFocus)
        	{
        		status_table.removeAllViews();
        		showStatus();
        	}
        	super.onWindowFocusChanged(hasFocus);
        }
        
        public void showStatus(){
        	List<Expense> expenses = db.getMonthExpenses(date_field.getText().toString());
        	
        	status.setGravity(Gravity.CENTER);
        	if(expenses.size() < 1){
        		btn2.setEnabled(false);
        		status.setText("\n\nNo expense for month of date " + date_field.getText() + "\n" + "Monthly Status will appear here.");
        		return;
        	}
        	btn2.setEnabled(true);
        	JSONObject nameVice = new JSONObject();
        	JSONObject itemVice = new JSONObject();
        	JSONObject selfVice = new JSONObject();
        	for ( Expense exp : expenses){
        		  try {
        			  float n_amt = exp.getAmt();
        			  float i_amt = n_amt;
        			  float s_amt = n_amt;
        			  //Self Expense
        			  if(exp.getName().toString().equals(exp.getSpentFor().toString())){
        				  if(selfVice.has(exp.getName()))
        					  s_amt = s_amt + Float.parseFloat(selfVice.getString(exp.getName()));
        				  selfVice.put(exp.getName(), s_amt);
        				  //Toast.makeText(MainActivity.this, s_amt + "", Toast.LENGTH_LONG).show();
        			  }
        			  //Team Expense
        			  else{
        				  //Name level
        				  if(nameVice.has(exp.getName()))
        					  n_amt = n_amt + Float.parseFloat(nameVice.getString(exp.getName()));
        				  //Item level
            			  if(itemVice.has(exp.getSpentFor())){
            				  i_amt = i_amt + Float.parseFloat(itemVice.getString(exp.getSpentFor()));
            			  }
        				  nameVice.put(exp.getName(), n_amt);
        				  itemVice.put(exp.getSpentFor(), i_amt);
        			  }
        		  } catch (JSONException e) {
        		    e.printStackTrace();
        		    Toast.makeText(MainActivity.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
        		  }
        	}
        	status_table.removeAllViews();
        	status.setText("\n\nStatus for month of date " + date_field.getText());
        	
        	// Name vice table
        	if(nameVice.length() > 0){
        		Iterator<String> name_iter = nameVice.keys();
            	TableRow tableHead;
        	    TextView textView;
        		
        		//header
        		tableHead = new TableRow(getApplicationContext());
        		tableHead.setBackgroundColor(Color.LTGRAY);
        		
        		textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("Name");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);
                
                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("Spent(Rs.)");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);
                
                status_table.addView(tableHead);
                
                float total_amt = 0;
                while (name_iter.hasNext()) {
                    String key = name_iter.next();
                    try {
                        Object value = nameVice.get(key);
                        TableRow row;
                	    TextView col;
                		
                		row = new TableRow(getApplicationContext());
                		
                		col = new TextView(getApplicationContext());
                        col.setTextColor(Color.BLUE);
                        col.setText(key);
                        col.setWidth(150);
                        col.setMaxLines(4);
                        col.setSingleLine(false);
                        col.setPadding(10, 10, 10, 10);
                        row.addView(col);
                        
                        col = new TextView(getApplicationContext());
                        col.setTextColor(Color.DKGRAY);
                        col.setText(value.toString());
                        col.setPadding(10, 10, 10, 10);
                        row.addView(col);
                        total_amt += Float.parseFloat(value.toString());
                        
                        setRowClickListener(row,"team");
                        status_table.addView(row);
                        //status.append(key + " spent total " + value + " rs\n");
                    } catch (JSONException e) {
                    	Toast.makeText(MainActivity.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                    }
                }
                TableRow row;
        	    TextView col;
        		
        	    // Total
        		row = new TableRow(getApplicationContext());
        		
        		col = new TextView(getApplicationContext());
                col.setTextColor(Color.RED);
                col.setText("Total Spent:");
                col.setPadding(10, 10, 10, 10);
                row.addView(col);
                
                col = new TextView(getApplicationContext());
                col.setTextColor(Color.BLACK);
                col.setText(total_amt + "");
                col.setPadding(10, 10, 10, 10);
                row.addView(col);
                
                status_table.addView(row);
                
                // Each share
                row = new TableRow(getApplicationContext());
        		
        		col = new TextView(getApplicationContext());
                col.setTextColor(Color.RED);
                col.setText("Each One's Share:");
                col.setWidth(150);
                col.setMaxLines(4);
                col.setSingleLine(false);
                col.setPadding(10, 10, 10, 10);
                row.addView(col);
                
                col = new TextView(getApplicationContext());
                col.setTextColor(Color.BLACK);
                col.setText((total_amt / nameVice.length()) + "");
                col.setPadding(10, 10, 10, 10);
                row.addView(col);
                
                status_table.addView(row);
        	}
        	
        	// Self expense table
        	if(selfVice.length() > 0){
                Iterator<String> self_iter = selfVice.keys();
                TableRow tableHead;
        	    TextView textView;
          		//header
        		tableHead = new TableRow(getApplicationContext());
        		tableHead.setBackgroundColor(Color.LTGRAY);
        		
        		textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("Name");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);
                
                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("For Self     For Team     Total");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);
                
                status_table.addView(tableHead);
                
                while (self_iter.hasNext()) {
                    String key = self_iter.next();
                    try {
                        Object value = selfVice.get(key);
                        TableRow row;
                	    TextView col;
                		
                		row = new TableRow(getApplicationContext());
                		
                		col = new TextView(getApplicationContext());
                        col.setTextColor(Color.BLUE);
                        col.setText(key);
                        col.setWidth(150);
                        col.setMaxLines(4);
                        col.setSingleLine(false);
                        col.setPadding(10, 10, 10, 10);
                        row.addView(col);
                        float for_team = 0;
                        if(nameVice.has(key))
                        	for_team = Float.parseFloat(nameVice.getString(key));
                        float total =  for_team + Float.parseFloat(value.toString());
                        
                        col = new TextView(getApplicationContext());
                        col.setTextColor(Color.DKGRAY);
                        col.setText(value.toString() + "          " + for_team + "         " + total);
                        col.setPadding(10, 10, 10, 10);
                        row.addView(col);
                        
                        setRowClickListener(row,"self");
                        status_table.addView(row);
                        //status.append("For " + key + " you spent " + value + " rs\n");
                    } catch (JSONException e) {
                    	Toast.makeText(MainActivity.this, "!!!Something Went Wrong!!!" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        	
        	// TODO Loan table
            
        	// Item vice table
        	if(itemVice.length() > 0){
        		Iterator<String> item_iter = itemVice.keys();
                TableRow tableHead;
        	    TextView textView;
          		//header
        		tableHead = new TableRow(getApplicationContext());
        		tableHead.setBackgroundColor(Color.LTGRAY);
        		
        		textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("Item Name");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);
                
                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("Amount(Rs.)");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);
                
                status_table.addView(tableHead);
                
                while (item_iter.hasNext()) {
                    String key = item_iter.next();
                    try {
                        Object value = itemVice.get(key);
                        TableRow row;
                	    TextView col;
                		
                		row = new TableRow(getApplicationContext());
                		
                		col = new TextView(getApplicationContext());
                        col.setTextColor(Color.BLUE);
                        col.setText(key);
                        col.setWidth(150);
                        col.setMaxLines(4);
                        col.setSingleLine(false);
                        col.setPadding(10, 10, 10, 10);
                        row.addView(col);
                        
                        col = new TextView(getApplicationContext());
                        col.setTextColor(Color.DKGRAY);
                        col.setText(value.toString());
                        col.setPadding(10, 10, 10, 10);
                        row.addView(col);
                        
                        setRowClickListener(row,"item");
                        status_table.addView(row);
                        //status.append("For " + key + " you spent " + value + " rs\n");
                    } catch (JSONException e) {
                    	Toast.makeText(MainActivity.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                    }
                }
        	}
        }
        
        private void setRowClickListener(final TableRow tr,final String ptable){
        	tr.setClickable(true);
        	tr.setOnClickListener(new View.OnClickListener() {                      
                @Override
                public void onClick(View arg0) {
                	String name = null,spent_for = null;
                	String date = date_field.getText().toString();
                	TextView tv = (TextView)tr.getChildAt(0);
                	if(ptable.equals("item"))
                		spent_for = tv.getText().toString();
                	else if(ptable.equals("self"))
                		spent_for = name = tv.getText().toString();
                	else if(ptable.equals("team"))
                		name = tv.getText().toString();
                	
                	showList(date,name,spent_for,"filtered");
                }
            });
        }
}
