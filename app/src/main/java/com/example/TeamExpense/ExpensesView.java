package com.example.TeamExpense;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp1.R;

public class ExpensesView extends Activity {


    private ScrollView sv;
    private LinearLayout filter_group;
    private List<Expense> current_list;
    private DatabaseHandler db;
    public static String comment = null;
    private String date_from, date_to, name, spent_for, opt;
    private boolean team_only = false;
    private AutoCompleteTextView name_tx, spent_for_tx;
    private EditText date_from_tx, date_to_tx;
    private Button search_btn;
    private List<Expense> import_list = null;

    private int year;
    private int month;
    private int day;
    private DatePicker date_picker;
    private float sub_total;

    final String sort_desc = "⬇";
    final String sort_asc = "⬆";
    final String sort = "⬍";
    private String date_sort = "desc";
    private String name_sort = "asc";
    private String item_sort = "asc";
    private String amt_sort = "asc";
    private String date_for = "";

    String file_choosed = "";

    static final int DATE_DIALOG_ID = 100;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_view);

        comment = null;
        sub_total = 0;
        db = new DatabaseHandler(this);
        sv = (ScrollView) findViewById(R.id.expense_view);

        filter_group = (LinearLayout) findViewById(R.id.filter_group);

        date_from_tx = (EditText) findViewById(R.id.list_date_from);
        date_to_tx = (EditText) findViewById(R.id.list_date_to);
        date_from_tx.setTextColor(Color.BLACK);
        date_from_tx.setHint("Date");
        date_from_tx.setMaxLines(4);
        date_from_tx.setSingleLine(false);

        name_tx = (AutoCompleteTextView) findViewById(R.id.list_name);
        name_tx.setTextColor(Color.BLACK);
        name_tx.setWidth(130);
        name_tx.setHint("Name");
        name_tx.setMaxLines(4);
        name_tx.setSingleLine(false);

        spent_for_tx = (AutoCompleteTextView) findViewById(R.id.list_spent);
        spent_for_tx.setTextColor(Color.BLACK);
        spent_for_tx.setWidth(135);
        spent_for_tx.setHint("Item name");
        spent_for_tx.setMaxLines(4);
        spent_for_tx.setSingleLine(false);

        search_btn = (Button) findViewById(R.id.list_search);

        Bundle extras = getIntent().getExtras();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) filter_group.getLayoutParams();
        params.height = 0;
        filter_group.setLayoutParams(params);
        filter_group.setVisibility(View.INVISIBLE);

        if (extras != null) {
            date_from = extras.getString("sel_date");
            date_from_tx.setText(date_from);
            name = extras.getString("sel_name");
            name_tx.setText(name);
            spent_for = extras.getString("sel_spent_for");
            spent_for_tx.setText(spent_for);
            team_only = extras.getBoolean("team_only");
            opt = extras.getString("show_option");
        }
        if (opt.equals("month")) {
            sv.addView(getExpenseTableView(db.getMonthExpenses(date_from, team_only)));
        } else if (opt.equals("filtered")) {
            sv.addView(getExpenseTableView(db.getFilteredExpenses(date_from, name, spent_for, team_only)));
        } else {
            sv.addView(getExpenseTableView(db.getAllExpenses()));
        }

        final Calendar calendar1 = Calendar.getInstance();
        year = calendar1.get(Calendar.YEAR);
        month = calendar1.get(Calendar.MONTH);
        day = calendar1.get(Calendar.DAY_OF_MONTH);
        date_picker = new DatePicker(getApplicationContext());

        setAutoCompletes();

        //sv.requestFocus();

        search_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                name = name_tx.getText().toString();
                date_from = date_from_tx.getText().toString();
                spent_for = spent_for_tx.getText().toString();
                String date_to = date_to_tx.getText().toString();
                if (date_to.equalsIgnoreCase("")) {
                    date_to = Expense.toDateString(db.getEndOfMonthEpoch(date_from));
                    date_to_tx.setText(date_to);
                }
                long from_epoch = Expense.toEpoch(date_from);
                long to_epoch = Expense.toEpoch(date_to);
                sv.removeAllViews();
                sv.addView(getExpenseTableView(db.getFilteredExpenses(from_epoch, to_epoch, name, spent_for, team_only, date_sort, name_sort, item_sort, amt_sort)));
            }
        });

        date_from_tx.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    date_for = "from";
                    showDialog(DATE_DIALOG_ID);
                }
            }
        });

        date_to_tx.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    date_for = "to";
                    showDialog(DATE_DIALOG_ID);
                }
            }
        });

        //sv.addView(btn1);
        //setContentView(sv);
        //onTouchEvent();


    }

    public void setAutoCompletes() {
        String[] name_list = db.getAllNames(null).clone();
        ArrayAdapter<String> names_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_list);
        name_tx.setAdapter(names_adapter);
        String[] items_list = db.getAllItems().clone();
        ArrayAdapter<String> items_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items_list);
        spent_for_tx.setAdapter(items_adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_menus, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        //user has long pressed your TextView
        menu.add(0, v.getId(), 0, "Copy to comments");

        //cast the received View to TextView so that you can get its text
        TextView tv = (TextView) v;
        comment = tv.getText().toString();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_all) {
            date_from = name = spent_for = "";
            sv.removeAllViews();
            sv.addView(getExpenseTableView(db.getAllExpenses()));
            return true;
        } else if (id == R.id.action_show_filtered) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) filter_group.getLayoutParams();
            if (filter_group.isShown()) {
                params.height = 0;
                filter_group.setLayoutParams(params);
                filter_group.setVisibility(View.INVISIBLE);
            } else {
                params.height = 120;
                filter_group.setLayoutParams(params);
                filter_group.setVisibility(View.VISIBLE);
            }
            return true;
        } else if (id == R.id.action_show_team_only) {
            team_only = !team_only;
            if (team_only) {
                item.setTitle("Show NonTeam also");
                item.setIcon(R.drawable.ic_action_add_group);
            } else {
                item.setTitle("Show Team Only");
                item.setIcon(R.drawable.ic_action_group);
            }
            sv.removeAllViews();
            sv.addView(getExpenseTableView(db.getFilteredExpenses(date_from, name, spent_for, team_only)));
            return true;
        } else if (id == R.id.action_export) {
            choose_file("export");
            return true;
        } else if (id == R.id.action_import) {
            choose_file("import");
            return true;
        } else if (id == R.id.action_show_sum) {
            if (sub_total == 0) {
                alert("Sum Value", "Select multiple Amount fields from table and select this option");
            } else {
                alert("Sum Value", "Sum of selected fields is: " + sub_total);
            }
            return true;
        }
        //setContentView(sv);
        return super.onOptionsItemSelected(item);
    }

    public void alert(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExpensesView.this);
        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.show();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;
            // set selected date into Text View
            if (date_for.equalsIgnoreCase("from")) {
                date_from_tx.setText(new StringBuilder().append(day).append('/').append(month + 1).append('/').append(year));
            } else {
                date_to_tx.setText(new StringBuilder().append(day).append('/').append(month + 1).append('/').append(year));
            }
            // set selected date into Date Picker
            date_picker.init(year, month, day, null);
            name_tx.requestFocus();
        }
    };

    public void exportData(String file_url) {
        if (current_list != null) {
            File dir = new File(file_url);
        /*
    	String extState = Environment.getExternalStorageState();
    	if(!extState.equals(Environment.MEDIA_MOUNTED) || extState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
    		dir = Environment.getExternalStorageDirectory();
    	}
    	else {
    	    dir = Environment.getDataDirectory();
    	}
    	dir = new File(Environment.getExternalStoragePublicDirectory("/"), "/TeamExpense/");
    	
    	*/
            if (!dir.getParentFile().exists()) {
                dir.getParentFile().mkdirs();
            }
            final File file = new File(dir.getAbsolutePath());
            if (!(file.exists())) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    notify("File Create Error: " + e.getMessage());
                    return;
                }
            }

            final ProgressDialog progressBar;
            final Handler progressBarHandler = new Handler();

            try {
                final FileOutputStream f1 = new FileOutputStream(file, false); //True = Append to file, false = Overwrite
                final PrintStream p = new PrintStream(f1);

                progressBar = new ProgressDialog(ExpensesView.this);
                progressBar.setCancelable(false);
                progressBar.setMessage("Exporting data!\n Please wait...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressBar.setProgress(0);
                progressBar.setMax(current_list.size());
                progressBar.show();
                //reset progress bar and filesize status

                String c1, c2, c3, c4, c5;
                c1 = "\"Date\"";
                c2 = "\"Paid By\"";
                c3 = "\"Spent For\"";
                c4 = "\"Amount\"";
                c5 = "\"Comments\"";

                p.println(c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5);

                final Object my_ui = ExpensesView.this;

                new Thread(new Runnable() {
                    public void run() {
                        int progressBarStatus = 0;
                        for (Expense exp : current_list) {
                            String c1, c2, c3, c4, c5;
                            c1 = "\"" + Expense.toDateString(exp.getDate()) + "\"";
                            c2 = "\"" + exp.getName() + "\"";
                            c3 = "\"" + exp.getSpentFor() + "\"";
                            c4 = "\"" + exp.getAmt() + "\"";
                            c5 = "\"" + exp.getComment() + "\"";
                            p.println(c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5);
                            progressBarStatus++;
                            progressBar.setProgress(progressBarStatus);
                        }
                        p.close();
                        progressBar.dismiss();
                        try {
                            f1.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                Toast.makeText((ExpensesView)my_ui, "Exported Successfuly! to\n" + file.getName(), Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                notify("File Write Error:" + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            notify("No data to export");
        }
    }

    public void importData(String file_url) {
        boolean success = true;
        if (import_list != null) {
            for (Expense exp : import_list) {
                db.addExpense(exp);
            }
            import_list = null;
        } else {
            File file = new File(file_url);
            notify("Chosen File: " + file.getName());
            if (!(file.exists())) {
                success = false;
                notify("ERROR: File Not Found");
                return;
            }
            try {
                InputStream ip = new FileInputStream(file_url);
                if (ip != null) {
                    InputStreamReader inputreader = new InputStreamReader(ip);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    line = buffreader.readLine();
                    if (line.equals("\"Date\",\"Paid By\",\"Spent For\",\"Amount\",\"Comments\",\"Group\",\"Mode\"")) {
                        import_list = new ArrayList<Expense>();
                        while ((line = buffreader.readLine()) != null) {
                            List<String> cols = new ArrayList<String>();
                            for (String col : line.split("\",")) {
                                col = col.replaceAll("^\"|\"$", "");
                                cols.add(col);
                            }
                            import_list.add(new Expense(cols.get(1), Expense.toEpoch(cols.get(0)), cols.get(2), cols.get(4), Float.parseFloat(cols.get(3)),cols.get(5),cols.get(6)));
                        }
                    } else {
                        success = false;
                        notify("Not a valid data file!");
                        import_list = null;
                    }
                    buffreader.close();
                    inputreader.close();
                }
                ip.close();
                sv.removeAllViews();
                sv.addView(getExpenseTableView(import_list));
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExpensesView.this);
                // set title
                alertDialogBuilder.setTitle("Import");
                alertDialogBuilder
                        .setMessage("Table loaded with import data.\nVerify and select Import again.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.show();
                success = false;
            } catch (Exception e) {
                success = false;
                notify("File Read Error:" + e.getMessage());
                import_list = null;
                return;
            }
        }
        if (success) {
            notify("Imported Successfuly!");
        }
    }

    public void choose_file(final String task) {
        if (task.equals("import") && import_list != null) {
            importData("");
            return;
        }
        SimpleFileDialog FolderChooseDialog = new SimpleFileDialog(ExpensesView.this, "File Choose", new SimpleFileDialog.SimpleFileDialogListener() {
            @Override
            public void onChosenDir(String chosenDir) {
                if (task.equals("import")) {
                    importData(chosenDir);
                } else
                    exportData(chosenDir);
            }
        });
        FolderChooseDialog.Default_File_Name = "team_expenses.csv";
        FolderChooseDialog.chooseFile_or_Dir();
    }

    public void notify(String msg) {
        Toast.makeText(ExpensesView.this, msg, Toast.LENGTH_LONG).show();
    }

    public static void handleNextKey(final EditText from, final EditText to) {
        from.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (to != null)
                        to.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }

    public TableLayout getExpenseTableView(List<Expense> expenses) {
        current_list = expenses; // for export data
        float total_amt = 0;
        notify("Filtered:" + date_from + "," + name + "," + spent_for + "," + team_only);
        TableLayout tableLayout;
        TableRow tableHead;
        TextView textView;

        if (expenses.size() > 0) {
            tableLayout = new TableLayout(getApplicationContext());

            //header
            tableHead = new TableRow(getApplicationContext());
            tableHead.setBackgroundColor(Color.rgb(0, 100, 255));

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(drawable.list_selector_background);
            //textView.setText("Date " + sort_desc);
            if (date_sort == null) {
                textView.setText("Date " + sort + "    ");
            } else if (date_sort.equalsIgnoreCase("desc")) {
                textView.setText("Date " + sort_desc + "    ");
            } else {
                textView.setText("Date " + sort_asc + "    ");
            }
            textView.setPadding(10, 10, 10, 10);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (date_sort == null) {
                        date_sort = "desc";
                    } else if (date_sort.equalsIgnoreCase("desc")) {
                        date_sort = "asc";
                    } else {
                        date_sort = null;
                    }
                    sv.removeAllViews();
                    sv.addView(getExpenseTableView(db.getFilteredExpenses(date_from, name, spent_for, team_only, date_sort, name_sort, item_sort, amt_sort)));
                }
            });
            tableHead.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(drawable.list_selector_background);
            //textView.setText("Paid By " + sort_asc);
            if (name_sort == null) {
                textView.setText("Paid By " + sort + "    ");
            } else if (name_sort.equalsIgnoreCase("desc")) {
                textView.setText("Paid By " + sort_desc + "    ");
            } else {
                textView.setText("Paid By " + sort_asc + "    ");
            }
            textView.setPadding(1, 10, 10, 10);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name_sort == null) {
                        name_sort = "desc";
                    } else if (name_sort.equalsIgnoreCase("desc")) {
                        name_sort = "asc";
                    } else {
                        name_sort = null;
                    }
                    sv.removeAllViews();
                    sv.addView(getExpenseTableView(db.getFilteredExpenses(date_from, name, spent_for, team_only, date_sort, name_sort, item_sort, amt_sort)));
                }
            });
            tableHead.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(drawable.list_selector_background);
            //textView.setText("Spent For" + sort_asc);
            if (item_sort == null) {
                textView.setText("Spent For " + sort + "    ");
            } else if (item_sort.equalsIgnoreCase("desc")) {
                textView.setText("Spent For " + sort_desc + "    ");
            } else {
                textView.setText("Spent For " + sort_asc + "    ");
            }
            textView.setPadding(1, 10, 10, 10);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item_sort == null) {
                        item_sort = "desc";
                    } else if (item_sort.equalsIgnoreCase("desc")) {
                        item_sort = "asc";
                    } else {
                        item_sort = null;
                    }
                    sv.removeAllViews();
                    sv.addView(getExpenseTableView(db.getFilteredExpenses(date_from, name, spent_for, team_only, date_sort, name_sort, item_sort, amt_sort)));
                }
            });
            tableHead.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(drawable.list_selector_background);
            //textView.setText("Amount " + sort_asc);
            if (amt_sort == null) {
                textView.setText("Amount " + sort + "    ");
            } else if (amt_sort.equalsIgnoreCase("desc")) {
                textView.setText("Amount " + sort_desc + "    ");
            } else {
                textView.setText("Amount " + sort_asc + "    ");
            }
            textView.setPadding(1, 10, 10, 10);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (amt_sort == null) {
                        amt_sort = "desc";
                    } else if (amt_sort.equalsIgnoreCase("desc")) {
                        amt_sort = "asc";
                    } else {
                        amt_sort = null;
                    }
                    sv.removeAllViews();
                    sv.addView(getExpenseTableView(db.getFilteredExpenses(date_from, name, spent_for, team_only, date_sort, name_sort, item_sort, amt_sort)));
                }
            });
            tableHead.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText("     Comments    ");
            textView.setPadding(1, 10, 10, 10);
            tableHead.addView(textView);

            tableLayout.addView(tableHead);

            for (Expense texp : expenses) {
                total_amt += Float.parseFloat(texp.getAmt().toString());
                tableLayout.addView(createRowView(texp));
            }

            final TableRow tableRow_total = new TableRow(getApplicationContext());

            textView = new TextView(getApplicationContext());
            textView.setText("");
            textView.setPadding(10, 10, 10, 10);
            tableRow_total.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setText("");
            textView.setPadding(10, 10, 10, 10);
            tableRow_total.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLUE);
            textView.setText("Total Amt:");
            textView.setPadding(1, 10, 10, 10);
            tableRow_total.addView(textView);
            textView.setBackgroundColor(Color.LTGRAY);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.rgb(255, 0, 0));
            textView.setText(Expense.toCurrencyWithSymbol(total_amt));
            textView.setPadding(0, 10, 10, 10);
            tableRow_total.addView(textView);
            textView.setBackgroundColor(Color.LTGRAY);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.DKGRAY);
            textView.setText("");
            textView.setPadding(1, 10, 10, 10);
            tableRow_total.addView(textView);

            tableLayout.addView(tableRow_total);
        } else {
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

    public TableRow createRowView(final Expense texp) {
        final TableRow tableRow = new TableRow(getApplicationContext());
        TextView textView;

        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLUE);
        textView.setText(Expense.toDateString(texp.getDate()));
        textView.setPadding(5, 5, 5, 5);
        tableRow.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.BLACK);
        textView.setText(texp.getName());
        textView.setPadding(1, 5, 5, 5);
        textView.setWidth(125);
        textView.setMaxLines(4);
        textView.setSingleLine(false);
        tableRow.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.DKGRAY);
        textView.setText(texp.getSpentFor());
        textView.setPadding(1, 5, 5, 5);
        textView.setWidth(135);
        textView.setMaxLines(4);
        textView.setSingleLine(false);
        tableRow.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.RED);
        textView.setText(Expense.toCurrencyWithSymbol(texp.getAmt()));
        textView.setPadding(1, 5, 1, 5);
        //textView.setPadding(left, top, right, bottom);
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sub_total += texp.getAmt();
                v.setBackgroundColor(Color.CYAN);
            }
        });
        textView.setWidth(135);
        tableRow.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setTextColor(Color.DKGRAY);
        String comment = texp.getComment();

        comment += " (" + texp.getPaymentMode() + ')';

        textView.setText(comment);
        registerForContextMenu(textView);
        textView.setPadding(1, 5, 10, 5);
        tableRow.addView(textView);

        tableRow.setClickable(true);
        tableRow.setId(texp.getId());
        tableRow.setBackgroundResource(drawable.list_selector_background);
        if (texp.getId() > 0)
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Expense.showEditDialog(tableRow.getId(), ExpensesView.this, db);
                }
            });
        return tableRow;
    }

}
