package com.example.TeamExpense;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.example.TeamExpense.list.Common;
import com.example.TeamExpense.list.MainActivity;
import com.example.TeamExpense.list.MyRecyclerAdapter;
import com.example.myapp1.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;


public class ExpenseStatus extends Activity {

    private Button saveBtn, viewBtn, me_btn, self_btn, show_calc_btn, clear_btn;
    public String Month[] = {"Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private EditText desc, amt;
    public EditText date_field, date_field_from, date_field_to;
    public AutoCompleteTextView spent_for, name, team_name;
    private TextView status, self_status, loan_status, item_status;
    private DatePicker date_picker;
    private TableLayout team_status_table, self_status_table, loan_status_table, item_status_table;
    private LinearLayout status_layout;
    private float share_amt;
    private JSONObject team_expense;
    private JSONObject loanVice = new JSONObject();
    private JSONObject nameVice = new JSONObject();
    private JSONObject itemVice = new JSONObject();
    private JSONObject selfVice = new JSONObject();
    private JSONObject loan_amt;
    private boolean sync_wifi_only = false;
    private String date_picker_for = "";
    private String non_team_users[];

    private DbxAccountManager mDbxAcctMgr;
    private final String dbxAppKey = "qlqquc9asiaal4g";
    private final String dbxSecretKey = "j6z8u6cgjdan0pg";


    private int year;
    private int month;
    private int day;

    static final int DATE_DIALOG_ID = 100;
    static final int REQUEST_LINK_TO_DBX = 22;
    static final int SETTINGS_SCREEN = 1;

    private DatabaseHandler db;

    //Preferences
    public boolean self_only = false;
    public boolean auto_sync = true;
    public String user_name = "";
    public String def_team_name = "";
    public boolean new_list_ui = false;

    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;
    private ArrayList<JSONObject> mDataset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date_field = (EditText) findViewById(R.id.editText1);
        date_field.setSingleLine(true);
        date_field_from = (EditText) findViewById(R.id.et_date_from);
        date_field_from.setSingleLine(true);
        date_field_to = (EditText) findViewById(R.id.et_date_to);
        date_field_to.setSingleLine(true);
        team_name = (AutoCompleteTextView) findViewById(R.id.team_name);
        team_name.setSingleLine(true);
        name = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        name.setSingleLine(true);
        spent_for = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
        spent_for.setSingleLine(true);
        this.handleNextKey(name, spent_for);
        desc = (EditText) findViewById(R.id.editText4);
        desc.setSingleLine(true);
        this.handleNextKey(spent_for, desc);
        amt = (EditText) findViewById(R.id.editText5);
        amt.setSingleLine(true);
        this.handleNextKey(desc, amt);
        this.handleNextKey(amt, null);
        status = (TextView) findViewById(R.id.textView2);
        self_status = (TextView) findViewById(R.id.textView3);
        loan_status = (TextView) findViewById(R.id.textView4);
        item_status = (TextView) findViewById(R.id.textView5);
        saveBtn = (Button) findViewById(R.id.button1);
        viewBtn = (Button) findViewById(R.id.button2);
        clear_btn = (Button) findViewById(R.id.button3);
        me_btn = (Button) findViewById(R.id.me_btn);
        self_btn = (Button) findViewById(R.id.self_btn);
        show_calc_btn = (Button) findViewById(R.id.btn_show_calc);
        date_picker = (DatePicker) findViewById(R.id.datePicker1);
        team_status_table = (TableLayout) findViewById(R.id.team_status_table);
        self_status_table = (TableLayout) findViewById(R.id.self_status_table);
        loan_status_table = (TableLayout) findViewById(R.id.loan_status_table);
        item_status_table = (TableLayout) findViewById(R.id.item_status_table);
        status_layout = (LinearLayout) findViewById(R.id.status_layout);
        self_status.setText("");
        loan_status.setText("");
        item_status.setText("");

        final Calendar calendar1 = Calendar.getInstance();
        year = calendar1.get(Calendar.YEAR);
        month = calendar1.get(Calendar.MONTH);
        day = calendar1.get(Calendar.DAY_OF_MONTH);
        date_field.setText(new StringBuilder().append(day).append('/').append(month + 1).append('/').append(year));
        date_picker.init(year, month, day, null);
        date_picker.removeAllViews();
        db = new DatabaseHandler(this);
        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), dbxAppKey, dbxSecretKey);
        setAutoCompletes();
        showStatus();
        name.requestFocus();
        this.addListenerOnButton();
        this.addListenerOnDateField();
        modifyUserPreferences();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (!def_team_name.equals("")) {
            team_name.setText(def_team_name);
        }
        if (self_only) {
            self_btn.setText("X");
            if (!user_name.equals("")) {
                name.setText(user_name);
                me_btn.setText("X");
                spent_for.requestFocus();
            } else {
                notify("Set your name in settings to pre fill your name.");
            }
            team_name.setText("SELF");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.item_wise_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //load_data_item_wise();

        adapter = new MyRecyclerAdapter(mDataset);
        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);
    }

    public void handleNextKey(final EditText from, final EditText to) {
        from.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (to != null)
                        to.requestFocus();
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //saveBtn.requestFocusFromTouch();
                    validateAndSave();
                    return true;
                }
                return false;
            }
        });
    }

    public void addListenerOnDateField() {
        date_field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    date_picker_for = "";
                    showDialog(DATE_DIALOG_ID);
                }
            }
        });
        date_field_from.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    date_picker_for = "from";
                    showDialog(DATE_DIALOG_ID);
                }
            }
        });
        date_field_to.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    date_picker_for = "to";
                    showDialog(DATE_DIALOG_ID);
                }
            }
        });
    }

    public void setAutoCompletes() {
        String[] name_list = db.getAllNames(null).clone();
        ArrayAdapter<String> names_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_list);
        name.setAdapter(names_adapter);
        String[] items_list = db.getAllItems().clone();
        ArrayAdapter<String> items_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items_list);
        spent_for.setAdapter(items_adapter);
    }

    public void addListenerOnButton() {

        // Save button action
        saveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                validateAndSave();
            }
        });

        // View Transaction button action
        viewBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                showList(date_field.getText().toString(), name.getText().toString(), spent_for.getText().toString(), "filtered", false);
            }
        });

        clear_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                clear_form();
            }
        });

        show_calc_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                show_calculator();
            }
        });

        me_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (user_name.equals("")) {
                    alert("No Name", "Goto Settings and set your name.");
                    return;
                }
                if (!name.getText().toString().equals(user_name)) {
                    name.setText(user_name);
                    me_btn.setText("X");
                    spent_for.requestFocus();
                } else {
                    name.setText("");
                    name.requestFocus();
                    me_btn.setText("Me");
                }

            }
        });

        self_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!self_only) {
                    if (user_name.equals("")) {
                        alert("No Name", "Goto Settings and set your name.");
                        return;
                    } else if (!name.getText().toString().equals("") && !name.getText().toString().equals(user_name)) {
                        alert("Self", "It seems this expense was not paid by you.");
                        return;
                    }
                    if (!spent_for.getText().toString().equals(user_name)) {
                        spent_for.setText(user_name);
                        name.setText(user_name);
                        desc.requestFocus();
                        self_btn.setText("X");
                        me_btn.setText("X");
                    } else {
                        spent_for.setText("");
                        spent_for.requestFocus();
                        self_btn.setText("Self");
                    }
                } else {
                    spent_for.setText("");
                    spent_for.requestFocus();
                    self_btn.setText("X");
                }
            }
        });
    }

    private void show_calculator() {
        // Show calculator dialogue
        final Dialog dialog = new Dialog(ExpenseStatus.this);
        dialog.setContentView(R.layout.caclulator);
        dialog.setTitle("Calculator");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        final int ADD = 1, SUB = 2, MUL = 3, DIV = 4, PER = 5;
        final int operation[] = new int[1];
        final float values[] = new float[2];
        values[0] = 0;
        values[1] = 0;
        final TextView history = (TextView) dialog.findViewById(R.id.calc_history);
        history.setMaxLines(4);
        final TextView answer = (TextView) dialog.findViewById(R.id.calc_answer);
        final EditText value = (EditText) dialog.findViewById(R.id.calc_text);

        Button plus, minus, multiply, divide, percentage, clear, ok_btn, equal;
        plus = (Button) dialog.findViewById(R.id.btn_plus);
        minus = (Button) dialog.findViewById(R.id.btn_minus);
        multiply = (Button) dialog.findViewById(R.id.btn_mul);
        divide = (Button) dialog.findViewById(R.id.btn_divide);
        percentage = (Button) dialog.findViewById(R.id.btn_percentage);
        clear = (Button) dialog.findViewById(R.id.btn_clear);
        ok_btn = (Button) dialog.findViewById(R.id.btn_ok);
        equal = (Button) dialog.findViewById(R.id.btn_equal);

        equal.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                answer.setText("");
                values[0] = values[1];
                String val = value.getText().toString();
                val.trim();
                if (!val.equalsIgnoreCase(""))
                    values[1] = Float.parseFloat(val);
                history.append(values[1] + "");
                values[1] = do_calc(operation[0], values);
                value.setText("");
                answer.setText(values[1] + "");
                values[0] = 0;
                values[1] = 0;
            }
        });
        plus.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                operation[0] = ADD;
                values[0] = values[1];
                String val = value.getText().toString();
                val.trim();
                if (!val.equalsIgnoreCase(""))
                    values[1] = Float.parseFloat(val);
                history.append(values[1] + " + ");
                values[1] = do_calc(operation[0], values);
                value.setText("");
                answer.setText(values[1] + "");
            }
        });
        minus.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                operation[0] = SUB;
                values[0] = values[1];
                String val = value.getText().toString();
                val.trim();
                if (!val.equalsIgnoreCase(""))
                    values[1] = Float.parseFloat(val);
                history.append(values[1] + " - ");
                values[1] = do_calc(operation[0], values);
                value.setText("");
                answer.setText(values[1] + "");
            }
        });
        multiply.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                operation[0] = MUL;
                values[0] = values[1];
                String val = value.getText().toString();
                val.trim();
                if (!val.equalsIgnoreCase(""))
                    values[1] = Float.parseFloat(val);
                history.append(values[1] + " * ");
                values[1] = do_calc(operation[0], values);
                value.setText("");
                answer.setText(values[1] + "");
            }
        });
        divide.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                operation[0] = DIV;
                values[0] = values[1];
                String val = value.getText().toString();
                val.trim();
                if (!val.equalsIgnoreCase(""))
                    values[1] = Float.parseFloat(val);
                history.append(values[1] + " / ");
                values[1] = do_calc(operation[0], values);
                value.setText("");
                answer.setText(values[1] + "");
            }
        });
        percentage.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                operation[0] = PER;
                values[0] = values[1];
                String val = value.getText().toString();
                val.trim();
                if (!val.equalsIgnoreCase(""))
                    values[1] = Float.parseFloat(val);
                history.append(values[1] + " % ");
                values[1] = do_calc(operation[0], values);
                value.setText("");
                answer.setText(values[1] + "");
            }
        });
        clear.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                answer.setText("");
                history.setText("");
                value.setText("");
                values[0] = 0;
                values[1] = 0;
            }
        });
        ok_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!answer.getText().toString().equalsIgnoreCase("")) {
                    amt.setText(answer.getText().toString());
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private float do_calc(int operation, float values[]) {
        final int ADD = 1, SUB = 2, MUL = 3, DIV = 4, PER = 5;
        float ret = values[1];
        if (values[0] > 0 && values[1] > 0) {
            switch (operation) {
                case ADD:
                    ret = values[0] + values[1];
                    break;
                case SUB:
                    ret = values[0] - values[1];
                    break;
                case MUL:
                    ret = values[0] * values[1];
                    break;
                case DIV:
                    ret = values[0] / values[1];
                    break;
                case PER:
                    ret = (values[0] / 100) * values[1];
                    break;
            }
        }
        return ret;
    }

    private void validateAndSave() {
        if (date_field.getText() + "" == "" || name.getText() + "" == "" || spent_for.getText() + "" == "" || amt.getText() + "" == "") {
            Toast.makeText(ExpenseStatus.this, "ERROR: Incomplete form", Toast.LENGTH_LONG).show();
        } else {
            saveExpense();
        }
    }

    private void saveExpense() {
        long epoch_date = Expense.toEpoch(date_field.getText().toString());
        Expense exp = new Expense(name.getText().toString(),
                epoch_date,
                spent_for.getText().toString(),
                desc.getText().toString(),
                Float.parseFloat("" + amt.getText()));
        db.addExpense(exp);
        Toast.makeText(ExpenseStatus.this, "Saved Successfuly", Toast.LENGTH_LONG).show();
        clear_form();
        showStatus();
    }

    private void clear_form() {
        name.setText("");
        name.requestFocus();
        spent_for.setText("");
        amt.setText("");
        desc.setText("");
        me_btn.setText("Me");
        self_btn.setText("Self");
        setAutoCompletes();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                notify("Connection Success!");
                if (mDbxAcctMgr.hasLinkedAccount()) {
                    syncExpenseToDropbox();
                }
            } else {
                notify("Authentication failed!");
            }
        } else if (requestCode == SETTINGS_SCREEN) {
            modifyUserPreferences();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void syncExpenseToDropbox() {
        if (!DatabaseHandler.isModified) {
            notify("You haven't modified anything!");
            return;
        }
        notify("Team Expense: Preparing for Sync!");
        try {
            DbxPath csv_path = new DbxPath("team_expense.csv");
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
            DbxFile csv_file;
            if (!dbxFs.exists(csv_path)) {
                csv_file = dbxFs.create(csv_path);
            } else {
                csv_file = dbxFs.open(csv_path);
            }
            PrintStream p = new PrintStream(csv_file.getWriteStream());
            String c1, c2, c3, c4, c5;
            c1 = "\"Date\"";
            c2 = "\"Paid By\"";
            c3 = "\"Spent For\"";
            c4 = "\"Amount\"";
            c5 = "\"Comments\"";
            p.println(c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5);
            List<Expense> expense_list = db.getAllExpenses();
            for (Expense exp : expense_list) {
                c1 = "\"" + Expense.toDateString(exp.getDate()) + "\"";
                c2 = "\"" + exp.getName() + "\"";
                c3 = "\"" + exp.getSpentFor() + "\"";
                c4 = "\"" + exp.getAmt() + "\"";
                c5 = "\"" + exp.getComment() + "\"";
                p.println(c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5);
            }
            p.close();
            csv_file.close();
            DatabaseHandler.isModified = false;
            if (!isNetworkAvailable()) {
                notify("Team Expense: Will be synced once you are online.");
            } else {
                notify("Team Expense: Synced!");
            }
        } catch (Exception e) {
            notify("Team Expense: Sync Failed! " + e.getMessage());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(ExpenseStatus.CONNECTIVITY_SERVICE);
        State wifi = connectivityManager.getNetworkInfo(1).getState();
        if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
            return true;
        } else if (!sync_wifi_only) {
            State mobile = connectivityManager.getNetworkInfo(0).getState();
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                return true;
            }
        }
        return false;
    }

    public void alert(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExpenseStatus.this);
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

    public void notify(String message) {
        Toast.makeText(ExpenseStatus.this, message, Toast.LENGTH_LONG).show();
    }

    private void showList(String date, String name, String spent_for, String opt, boolean team_only) {
        Intent listIntent;
        if (new_list_ui) {
            //listIntent = new Intent(ExpenseStatus.this, ExpensesView2.class);
            listIntent = new Intent(ExpenseStatus.this, MainActivity.class);
        } else {
            listIntent = new Intent(ExpenseStatus.this, ExpensesView.class);
        }

        if (date.equals(""))
            date = day + "/" + month + "/" + year;
        date_field.setText(date);
        listIntent.putExtra("sel_date", date);
        listIntent.putExtra("sel_name", name);
        listIntent.putExtra("sel_spent_for", spent_for);
        listIntent.putExtra("show_option", opt);
        listIntent.putExtra("team_only", team_only);
        ExpenseStatus.this.startActivity(listIntent);
    }

    public void onBackPressed() {
        if (auto_sync && DatabaseHandler.isModified && mDbxAcctMgr.hasLinkedAccount()) {
            syncExpenseToDropbox();
        }
        if (name.getText() + "" == "" && spent_for.getText() + "" == "" && amt.getText() + "" == "") {
            ExpenseStatus.this.finish();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExpenseStatus.this);
            // set title
            alertDialogBuilder.setTitle("EXIT?");
            alertDialogBuilder
                    .setMessage("Do you really want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ExpenseStatus.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private void modifyUserPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        self_only = prefs.getBoolean("pref_self_only", false);
        auto_sync = prefs.getBoolean("prefs_auto_sync", true);
        user_name = prefs.getString("pref_name", "");
        def_team_name = prefs.getString("pref_def_team", "");
        new_list_ui = prefs.getBoolean("pref_new_list", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), ExpenseSettings.class);
            startActivityForResult(i, SETTINGS_SCREEN);
        } else if (id == R.id.action_share_status_image) {
            Uri uri1 = Util.takeScreenshot(status_layout, "team_expense_status.jpg", ExpenseStatus.this);
            Uri uri2 = Util.takeScreenshot(loan_status_table, "team_expense_loans.jpg", ExpenseStatus.this);
            Uri uri3 = Util.takeScreenshot(item_status_table, "team_expense_items.jpg", ExpenseStatus.this);

            ArrayList<Uri> imageUriArray = new ArrayList<>();

            if (uri1 != null) {
                imageUriArray.add(uri1);
            }
            if (uri2 != null) {
                imageUriArray.add(uri2);
            }
            if (uri3 != null) {
                imageUriArray.add(uri3);
            }

            if (imageUriArray.size() > 0) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.setType("image/jpeg");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Expense Details");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Team Expense");
                sharingIntent.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, imageUriArray);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            } else {
                notify("Error: Unable to share!!!");
            }

        } else if (id == R.id.action_share_status_text) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, get_status_as_string());
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Team Expense Details");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.action_help) {
            Dialog dialog = new Dialog(ExpenseStatus.this);
            dialog.setContentView(R.layout.help_screen);
            dialog.setTitle("Help");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } else if (id == R.id.action_about) {
            Dialog dialog = new Dialog(ExpenseStatus.this);
            dialog.setContentView(R.layout.about_screen);
            dialog.setTitle("About");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } else if (id == R.id.action_sync_dropbox) {
            if (mDbxAcctMgr.hasLinkedAccount()) {
                syncExpenseToDropbox();
            } else {
                mDbxAcctMgr.startLink((Activity) this, REQUEST_LINK_TO_DBX);
            }
        } else if (id == R.id.action_splitup) {
            final Dialog dialog = new Dialog(ExpenseStatus.this);
            dialog.setContentView(R.layout.splitup_screen);
            dialog.setTitle("Share Calculator");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            final TextView tx = (TextView) dialog.findViewById(R.id.splitup_details);
            final EditText amt = (EditText) dialog.findViewById(R.id.split_amount);
            AutoCompleteTextView tmp = (AutoCompleteTextView) dialog.findViewById(R.id.setle_on);

            final LinearLayout full_dialog = (LinearLayout) dialog.findViewById(R.id.full_dialog);
            Button scr_shot_btn = (Button) dialog.findViewById(R.id.share_btn);
            scr_shot_btn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Uri uri1 = Util.takeScreenshot(full_dialog, "team_expense_splitups.jpg", ExpenseStatus.this);
                    if(uri1 != null){
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sharingIntent.setType("image/jpeg");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Expense Details");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Team Expense");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri1);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                }
            });

            tmp.setVisibility(View.INVISIBLE);
            TableRow tmp1 = (TableRow) dialog.findViewById(R.id.setle_btn_row);
            tmp1.removeAllViews();
            tx.setText("Team Split up details are:\n\n");
            amt.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (amt.getText().toString().equals("")) {
                        return;
                    }
                    int N = team_expense.length();
                    if (N < 1) {
                        tx.setText("Sorry! No one is there in Team to share.\n\n");
                        return;
                    }
                    float given_amt = Float.parseFloat(amt.getText() + "");
                    if (given_amt < 1) {
                        tx.setText("Team Split up details are:\n\n");
                        return;
                    }

                    Iterator<?> name_iter = team_expense.keys();
                    tx.setText("Team Split up details are:\n(Share + Loan)\n\n");
                    while (name_iter.hasNext()) {
                        String key = (String) name_iter.next();
                        try {
                            Object value = team_expense.get(key);
                            float splited_amt = ((given_amt / N) + share_amt) - Float.parseFloat(value.toString());
                            float have_to_pay;
                            float l_amt = 0;
                            if (loan_amt.has(key)) {
                                l_amt = Float.parseFloat(loan_amt.get(key).toString());
                            }
                            // l_amt negative means he got money
                            // positive means he gave money
                            l_amt = l_amt * -1;
                            have_to_pay = splited_amt + l_amt;
                            String txt = new String();
                            if (have_to_pay < 0) {
                                txt = key + " have to get** :" + Expense.toCurrencyWithSymbol((have_to_pay * -1));
                            } else {
                                txt = key + " have to give : " + Expense.toCurrencyWithSymbol(have_to_pay);
                            }
                            if (l_amt != 0) {
                                txt += "\n(" + Expense.toCurrency(splited_amt) + " + " + Expense.toCurrency(l_amt) + ")";
                            }
                            txt += "\n";
                            tx.append(txt);
                        } catch (Exception e) {
                            //Log.println(10, "Error", e.getMessage() + "===");
                            Toast.makeText(ExpenseStatus.this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        } else if (id == R.id.action_add_user) {
            final Dialog dialog = new Dialog(ExpenseStatus.this);
            dialog.setContentView(R.layout.add_user_screen);
            dialog.setTitle("Add User");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            final Spinner user_type = (Spinner) dialog.findViewById(R.id.user_type);
            final EditText user_name = (EditText) dialog.findViewById(R.id.new_user_name);
            Button add_btn = (Button) dialog.findViewById(R.id.add_user_btn);
            Button cancel_btn = (Button) dialog.findViewById(R.id.add_user_cancel);
            cancel_btn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            add_btn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    long epoch_date = Expense.toEpoch(date_field.getText().toString());
                    Expense exp = new Expense(user_name.getText().toString(),
                            epoch_date,
                            "User Added",
                            user_type.getSelectedItem().toString(),
                            (float) 0);
                    db.addExpense(exp);
                    Toast.makeText(ExpenseStatus.this, "Added Successfuly", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                }
            });
            dialog.show();
        } else if (id == R.id.action_frwd_loans) {
            final Calendar calendar1 = Calendar.getInstance();
            int t_year = calendar1.get(Calendar.YEAR);
            int t_month = calendar1.get(Calendar.MONTH) + 1;
            int t_day = calendar1.get(Calendar.DAY_OF_MONTH);
            int month = this.month + 1;
            if (t_month == month) {
                notify("Please select previous months.");
                return super.onOptionsItemSelected(item);
            }
            int year = this.year;
            List<Expense> expenses = db.getMonthExpenses("01/" + month + "/" + year, false);
            if (expenses.size() < 1) {
                notify("No expense for month " + Month[month - 1] + ", " + year);
            } else {
                if (loanVice.length() < 1) {
                    notify("No Loan for month " + Month[month - 1] + "/" + year);
                } else {
                    notify("Forwarding loans from month:" + Month[month - 1] + "/" + year);
                    Iterator<?> name_iter = loanVice.keys();

                    while (name_iter.hasNext()) {
                        String key = (String) name_iter.next();
                        try {
                            Object value = loanVice.get(key);
                            String loan_names[] = key.split(" gave to ");
                            Expense exp = new Expense(loan_names[1],
                                    Expense.toEpoch("28/" + month + "/" + year),
                                    loan_names[0],
                                    "Moved to " + t_day + "/" + t_month + "/" + t_year,
                                    Float.parseFloat(value.toString()));
                            db.addExpense(exp);

                            Expense exp1 = new Expense(loan_names[0],
                                    Expense.toEpoch(t_day + "/" + t_month + "/" + t_year),
                                    loan_names[1],
                                    "From month " + Month[month - 1] + ", " + year,
                                    Float.parseFloat(value.toString()));
                            db.addExpense(exp1);

                        } catch (JSONException e) {
                            Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } else if (id == R.id.action_setle_up) {
            final Dialog dialog = new Dialog(ExpenseStatus.this);
            dialog.setContentView(R.layout.splitup_screen);
            dialog.setTitle("Setle Up Details");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            final TextView tx = (TextView) dialog.findViewById(R.id.splitup_details);
            final EditText amt = (EditText) dialog.findViewById(R.id.split_amount);
            amt.setVisibility(View.INVISIBLE);
            final AutoCompleteTextView setle_on = (AutoCompleteTextView) dialog.findViewById(R.id.setle_on);
            Button setle_btn = (Button) dialog.findViewById(R.id.setle_btn);
            Button setle_as_loan = (Button) dialog.findViewById(R.id.setle_as_loan_btn);

            final LinearLayout full_dialog = (LinearLayout) dialog.findViewById(R.id.full_dialog);
            Button scr_shot_btn = (Button) dialog.findViewById(R.id.share_btn);
            scr_shot_btn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Uri uri1 = Util.takeScreenshot(full_dialog, "team_expense_setltups.jpg", ExpenseStatus.this);
                    if(uri1 != null){
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sharingIntent.setType("image/jpeg");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Expense Details");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Team Expense");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri1);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                }
            });

            if (team_expense.length() < 2) {
                tx.setText("Seems Expense not shared!");
                setle_on.setVisibility(View.INVISIBLE);
                TableRow tmp1 = (TableRow) dialog.findViewById(R.id.setle_btn_row);
                tmp1.removeAllViews();
                return true;
            }

            tx.setText("    Calculating...   ");
            Iterator<?> name_iter = team_expense.keys();
            JSONObject share_bal = new JSONObject();
            while (name_iter.hasNext()) {
                try {
                    String name = name_iter.next().toString();
                    Object value = team_expense.get(name);
                    share_bal.put(name, Float.parseFloat(value.toString()) - share_amt);
                } catch (Exception e) {
                    notify("Error in sharing Calc:" + e.getMessage());
                }
            }

            String names[] = db.getAllNames(null);
            String non_team_users[] = db.getAllNames("INDIVIDUAL");
            final JSONObject settled = new JSONObject();
            try {
                for (int i = 0; i < names.length; i++) {
                    if (indexOf(non_team_users, names[i]) == -1) {
                        // he_has will be positive if he spent extra,
                        // negative if he spent less
                        float he_has = Float.parseFloat(share_bal.get(names[i]).toString());
                        for (int j = 0; j < names.length; j++) {
                            if ((indexOf(non_team_users, names[j]) == -1) && (i != j)) {
                                if (he_has < 0) { // he spent less
                                    if (Float.parseFloat(share_bal.get(names[j]).toString()) > 0) { // some one has spent more
                                        // some one spent more then he spent less
                                        if (Float.parseFloat(share_bal.get(names[j]).toString()) > (he_has * -1)) {
                                            // who spent less should give to (->) some one who spent more then he spent less
                                            settled.put(names[i] + " -> " + names[j], (he_has * -1));
                                            // now some one got some money
                                            share_bal.put(names[j], Float.parseFloat(share_bal.get(names[j]).toString()) + he_has);
                                            he_has = 0;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (he_has < 0) {
                            for (int j = 0; j < names.length; j++) {
                                if ((indexOf(non_team_users, names[j]) == -1) && (i != j)) {
                                    if (he_has < 0) {
                                        if (Float.parseFloat(share_bal.get(names[j]).toString()) > 0) {
                                            if (Float.parseFloat(share_bal.get(names[j]).toString()) > 0) {
                                                settled.put(names[i] + " -> " + names[j], share_bal.get(names[j]).toString());
                                                share_bal.put(names[j], 0);
                                                he_has = Float.parseFloat(share_bal.get(names[j]).toString()) + he_has;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                notify("Error in settle:" + e.getMessage());
            }

            if (settled.length() > 0) {
                tx.setText("Setle back as follows to tally team share:\n\n");

                String[] items_list = db.getAllItems().clone();
                ArrayAdapter<String> items_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items_list);
                setle_on.setAdapter(items_adapter);

                setle_btn.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String setle_on_val = setle_on.getText().toString();
                        setle_on_val.trim();
                        if (setle_on_val.length() > 0) {
                            Iterator<?> settled_vals = settled.keys();
                            while (settled_vals.hasNext()) {
                                String key = (String) settled_vals.next();
                                try {
                                    Object value = settled.get(key);
                                    String names[] = key.split(" -> ");

                                    List<Expense> exp1 = db.getFilteredExpenses(date_field.getText().toString(), names[1], setle_on_val, true);
                                    Expense texp1 = exp1.get(0);
                                    if (texp1 != null) {
                                        texp1.amt = texp1.amt - Float.parseFloat(value.toString());
                                        if (texp1.amt > 0) {
                                            Expense texp0 = new Expense();
                                            texp0.name = names[0];
                                            texp0.spent_for = setle_on_val;
                                            texp0.comment = "Setled by " + names[1];
                                            texp0.date = Expense.toEpoch(date_field.getText().toString());
                                            texp0.amt = Float.parseFloat(value.toString());

                                            db.updateExpense(texp1);
                                            db.addExpense(texp0);

                                            Toast.makeText(ExpenseStatus.this, "Updated " + names[0] + " -> " + names[1] + " successfully", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(ExpenseStatus.this, "Please do manually for " + key, Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(ExpenseStatus.this, "Please do manually for " + key, Toast.LENGTH_LONG).show();
                                    }
                                    dialog.cancel();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(ExpenseStatus.this, "Setle on value is required", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                //String[] items_list = db.getAllItems().clone();
                //ArrayAdapter<String> items_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items_list);
                //amt.setAdapter(items_adapter);
                setle_as_loan.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String setle_on_val = setle_on.getText().toString();
                        setle_on_val.trim();
                        if (setle_on_val.length() > 0) {
                            Iterator<?> settled_vals = settled.keys();
                            while (settled_vals.hasNext()) {
                                String key = (String) settled_vals.next();
                                try {
                                    Object value = settled.get(key);
                                    String names[] = key.split(" -> ");

                                    List<Expense> exp1 = db.getFilteredExpenses(date_field.getText().toString(), names[1], setle_on_val, true);
                                    Expense texp1 = exp1.get(0);
                                    if (texp1 != null) {
                                        texp1.amt = texp1.amt - Float.parseFloat(value.toString());
                                        if (texp1.amt > 0) {
                                            Expense texp0 = new Expense();
                                            texp0.name = names[0];
                                            texp0.spent_for = setle_on_val;
                                            texp0.comment = "Setled by " + names[1];
                                            texp0.date = Expense.toEpoch(date_field.getText().toString());
                                            texp0.amt = Float.parseFloat(value.toString());

                                            Expense texp2 = new Expense();
                                            texp2.name = names[1];
                                            texp2.spent_for = names[0];
                                            texp2.comment = "Setled for " + setle_on_val;
                                            texp2.date = Expense.toEpoch(date_field.getText().toString());
                                            texp2.amt = Float.parseFloat(value.toString());

                                            db.updateExpense(texp1);
                                            db.addExpense(texp0);
                                            db.addExpense(texp2);

                                            Toast.makeText(ExpenseStatus.this, "Updated " + names[0] + " -> " + names[1] + " successfully", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(ExpenseStatus.this, "Please do manually for " + key, Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(ExpenseStatus.this, "Please do manually for " + key, Toast.LENGTH_LONG).show();
                                    }
                                    dialog.cancel();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(ExpenseStatus.this, "Setle on value is required", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Iterator<?> settled_vals = settled.keys();
                while (settled_vals.hasNext()) {
                    String key = (String) settled_vals.next();
                    try {
                        Object value = settled.get(key);
                        tx.append(key + "  : " + Expense.toCurrencyWithSymbol(Float.parseFloat(value.toString())) + "\n");
                    } catch (Exception e) {
                        notify("Error while printing:" + e.getMessage());
                    }
                }
            } else {
                tx.setText("It seems already well shared. :)");
                setle_on.setVisibility(View.INVISIBLE);
                TableRow tmp1 = (TableRow) dialog.findViewById(R.id.setle_btn_row);
                tmp1.removeAllViews();
            }
            tx.append("\n");
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
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
            int prev_month = month;
            int prev_year = year;
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            if (date_picker_for.equalsIgnoreCase("")) {
                // set selected date into Text View
                date_field.setText(new StringBuilder().append(day).append('/').append(month + 1).append('/').append(year));
                if (prev_month != month) {
                    date_field_from.setText(new StringBuilder().append(01).append('/').append(month + 1).append('/').append(year));
                    date_field_to.setText(Expense.toDateString(db.getEndOfMonthEpoch(date_field.getText().toString())));
                }
            } else if (date_picker_for.equalsIgnoreCase("from")) {
                date_field_from.setText(new StringBuilder().append(day).append('/').append(month + 1).append('/').append(year));
            } else if (date_picker_for.equalsIgnoreCase("to")) {
                date_field_to.setText(new StringBuilder().append(day).append('/').append(month + 1).append('/').append(year));
            }
            // set selected date into Date Picker
            date_picker.init(year, month, day, null);
            name.requestFocus();
            if (month != prev_month || year != prev_year)
                showStatus();
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            showStatus();
            if (ExpensesView.comment != null)
                desc.setText(ExpensesView.comment);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public static int indexOf(String arr[], String elmt) {
        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(elmt))
                return i;
        }
        return index;
    }

    public void showStatus() {
        String date[] = date_field.getText().toString().split("/");
        if (date_field_from.getText().toString().equalsIgnoreCase("")) {
            date_field_from.setText("01/" + date[1] + "/" + date[2]);
        }
        if (date_field_to.getText().toString().equalsIgnoreCase("")) {
            date_field_to.setText(Expense.toDateString(db.getEndOfMonthEpoch(date_field.getText().toString())));
        }
        List<Expense> expenses = db.getMonthExpenses(date_field_from.getText().toString(), date_field_to.getText().toString(), false);
        team_expense = new JSONObject();
        loan_amt = new JSONObject();
        team_status_table.removeAllViews();
        self_status_table.removeAllViews();
        loan_status_table.removeAllViews();
        item_status_table.removeAllViews();
        status.setText("");
        self_status.setText("");
        loan_status.setText("");
        item_status.setText("");

        status.setGravity(Gravity.CENTER);
        if (expenses.size() < 1) {
            viewBtn.setEnabled(false);
            status.setText("\n\nNo expense for month of date " + date_field.getText() + "\n" + "Monthly Status will appear here.");
            return;
        }
        viewBtn.setEnabled(true);
        nameVice = new JSONObject();
        itemVice = new JSONObject();
        selfVice = new JSONObject();
        loanVice = new JSONObject();
        String names[] = db.getAllNames(null);
        non_team_users = db.getAllIndividuals("01/" + date[1] + "/" + date[2]);
        for (Expense exp : expenses) {
            try {
                float n_amt = exp.getAmt();
                float i_amt = n_amt;
                float s_amt = n_amt;
                float l_amt = n_amt;
                //Self Expense
                if (exp.getName().toString().equals(exp.getSpentFor().toString())) {
                    if (selfVice.has(exp.getName()))
                        s_amt = s_amt + Float.parseFloat(selfVice.getString(exp.getName()));
                    selfVice.put(exp.getName(), s_amt);
                } else if (indexOf(names, exp.getSpentFor().toString()) != -1) {
                    String loan_txt = exp.getName() + " gave to " + exp.getSpentFor();
                    if (loanVice.has(loan_txt))
                        l_amt = l_amt + Float.parseFloat(loanVice.getString(loan_txt));
                    loanVice.put(loan_txt, l_amt);
                }
                //Team Expense
                else {
                    // Check if this transaction is belongs to team
                    if (indexOf(non_team_users, exp.name) == -1) {
                        //Name level
                        if (nameVice.has(exp.getName()))
                            n_amt = n_amt + Float.parseFloat(nameVice.getString(exp.getName()));
                        //Item level
                        if (itemVice.has(exp.getSpentFor())) {
                            i_amt = i_amt + Float.parseFloat(itemVice.getString(exp.getSpentFor()));
                        }
                        nameVice.put(exp.getName(), n_amt);
                        team_expense.put(exp.name, n_amt);
                        itemVice.put(exp.getSpentFor(), i_amt);
                    }
                }
                for (int i = 0; i < names.length; i++) {
                    for (int j = 0; j < names.length && i != j; j++) {
                        String t1 = names[i] + " gave to " + names[j];
                        String t2 = names[j] + " gave to " + names[i];
                        if (loanVice.has(t1) && loanVice.has(t2)) {
                            if (loanVice.getLong(t1) > loanVice.getLong(t2)) {
                                loanVice.put(t1, loanVice.getLong(t1) - loanVice.getLong(t2));
                                loanVice.remove(t2);
                                if (loanVice.getLong(t1) == (long) 0) {
                                    loanVice.remove(t1);
                                }
                            } else {
                                loanVice.put(t2, loanVice.getLong(t2) - loanVice.getLong(t1));
                                loanVice.remove(t1);
                                if (loanVice.getLong(t2) == (long) 0) {
                                    loanVice.remove(t2);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
            }
        }
        status.setText("Status for month " + Month[month] + ", " + year);

        if (self_only) {
            selfVice = nameVice;
            //nameVice = null;
        }

        // Name vice table
        if (!self_only && nameVice.length() > 0) {
            status.append("\n\nTeam Expense Share Details");
            Iterator<?> name_iter = nameVice.keys();
            TableRow tableHead;
            TextView textView;
            ProgressBar progressBar;

            //header
            tableHead = new TableRow(getApplicationContext());
            tableHead.setBackgroundColor(Color.LTGRAY);

//				progressBar = new ProgressBar(getApplicationContext());
            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText("Name");
            textView.setPadding(10, 10, 10, 10);
            tableHead.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText("Spent Amount");
            textView.setPadding(10, 10, 10, 10);
            tableHead.addView(textView);
//                tableHead.addView(progressBar);

            team_status_table.addView(tableHead);

            float total_amt = 0;
            while (name_iter.hasNext()) {
                String key = (String) name_iter.next();
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
                    col.setText(Expense.toCurrencyWithSymbol(Float.parseFloat(value.toString())));
                    col.setPadding(10, 10, 10, 10);
                    row.addView(col);
                    total_amt += Float.parseFloat(value.toString());

                    setRowClickListener(row, "team");
                    team_status_table.addView(row);
                    //status.append(key + " spent total " + value + " rs\n");
                } catch (JSONException e) {
                    Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
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
            col.setText(Expense.toCurrencyWithSymbol(total_amt));
            col.setPadding(10, 10, 10, 10);
            row.addView(col);

            team_status_table.addView(row);

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

            share_amt = total_amt / nameVice.length();

            col = new TextView(getApplicationContext());
            col.setTextColor(Color.BLACK);
            col.setText(Expense.toCurrencyWithSymbol(share_amt));
            col.setPadding(10, 10, 10, 10);
            row.addView(col);

            team_status_table.addView(row);
        }

        // Self expense table
        if (selfVice.length() > 0) {
            self_status.setText("Report of Self Expense");
            Iterator<?> self_iter = selfVice.keys();
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

            if (!self_only) {

                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("For Self");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);

                textView = new TextView(getApplicationContext());
                textView.setTextColor(Color.BLACK);
                textView.setText("For Team");
                textView.setPadding(10, 10, 10, 10);
                tableHead.addView(textView);

            }

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText("Total");
            textView.setPadding(10, 10, 5, 10);
            tableHead.addView(textView);

            self_status_table.addView(tableHead);

            while (self_iter.hasNext()) {
                String key = (String) self_iter.next();
                try {
                    Object value = selfVice.get(key);
                    TableRow row;
                    TextView col;

                    row = new TableRow(getApplicationContext());

                    col = new TextView(getApplicationContext());
                    col.setTextColor(Color.BLUE);
                    col.setText(key);
                    col.setWidth(120);
                    col.setMaxLines(4);
                    col.setSingleLine(false);
                    col.setPadding(1, 10, 5, 10);
                    row.addView(col);
                    float for_team = 0;
                    if (nameVice.has(key))
                        for_team = Float.parseFloat(nameVice.getString(key));
                    float total = for_team + Float.parseFloat(value.toString());

                    if (!self_only) {

                        col = new TextView(getApplicationContext());
                        col.setTextColor(Color.DKGRAY);
                        col.setText(Expense.toCurrency(Float.parseFloat(value.toString())));
                        col.setPadding(1, 10, 10, 10);
                        row.addView(col);

                        col = new TextView(getApplicationContext());
                        col.setTextColor(Color.DKGRAY);
                        col.setText(Expense.toCurrency(for_team));
                        col.setPadding(1, 10, 10, 10);
                        row.addView(col);

                    }

                    col = new TextView(getApplicationContext());
                    col.setTextColor(Color.DKGRAY);
                    col.setText(Expense.toCurrencyWithSymbol(total));
                    col.setPadding(1, 10, 5, 10);
                    row.addView(col);

                    setRowClickListener(row, "self");
                    self_status_table.addView(row);
                    //status.append("For " + key + " you spent " + value + " rs\n");
                } catch (JSONException e) {
                    Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!" + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }

        // Loan table
        if (loanVice.length() > 0) {

            loan_status.setText("Loan Details");
            Iterator<?> name_iter = loanVice.keys();
            TableRow tableHead;
            TextView textView;

            //header
            tableHead = new TableRow(getApplicationContext());
            tableHead.setBackgroundColor(Color.LTGRAY);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText("Loan Between");
            textView.setPadding(10, 10, 10, 10);
            tableHead.addView(textView);

            textView = new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setText("Loan Amount");
            textView.setPadding(10, 10, 10, 10);
            tableHead.addView(textView);

            loan_status_table.addView(tableHead);

            while (name_iter.hasNext()) {
                String key = (String) name_iter.next();
                try {
                    Object value = loanVice.get(key);
                    TableRow row;
                    TextView col;

                    row = new TableRow(getApplicationContext());

                    col = new TextView(getApplicationContext());
                    col.setTextColor(Color.BLUE);
                    col.setText(key);
                    col.setMaxLines(4);
                    col.setSingleLine(false);
                    col.setPadding(10, 10, 10, 10);
                    row.addView(col);

                    float amt = Float.parseFloat(value.toString());
                    col = new TextView(getApplicationContext());
                    col.setTextColor(Color.DKGRAY);
                    col.setText(Expense.toCurrencyWithSymbol(amt));
                    col.setPadding(10, 10, 10, 10);
                    row.addView(col);

                    // Check if this loan is among team members
                    String loaners[] = key.split(" gave to ");
                    if ((indexOf(non_team_users, loaners[0]) == -1) && (indexOf(non_team_users, loaners[0]) == -1)) {
                        if (!loan_amt.has(loaners[0])) {
                            loan_amt.put(loaners[0], 0);
                        }
                        if (!loan_amt.has(loaners[1])) {
                            loan_amt.put(loaners[1], 0);
                        }
                        //consider only team users loan
                        // so that split up wont mess up
                        if (indexOf(non_team_users, loaners[1]) == -1) {
                            loan_amt.put(loaners[0], Float.parseFloat(loan_amt.get(loaners[0]).toString()) + amt);
                        }
                        loan_amt.put(loaners[1], Float.parseFloat(loan_amt.get(loaners[1]).toString()) + (amt * -1));
                    }

                    setRowClickListener(row, "loan");
                    loan_status_table.addView(row);
                    //status.append(key + " spent total " + value + " rs\n");
                } catch (JSONException e) {
                    Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                }
            }
        }

        // Item vice table
        if (itemVice.length() > 0) {
            item_status.setText("Report in Each Item vise");
            Iterator<?> item_iter = itemVice.keys();
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
            textView.setText("Spent Amount");
            textView.setPadding(10, 10, 10, 10);
            tableHead.addView(textView);

            item_status_table.addView(tableHead);

            while (item_iter.hasNext()) {
                String key = (String) item_iter.next();
                try {
                    Object value = itemVice.get(key);
                    TableRow row;
                    TextView col;

                    row = new TableRow(getApplicationContext());

                    col = new TextView(getApplicationContext());
                    col.setTextColor(Color.BLUE);
                    col.setText(key);
                    col.setWidth(160);
                    col.setMaxLines(4);
                    col.setSingleLine(false);
                    col.setPadding(10, 10, 10, 10);
                    row.addView(col);

                    col = new TextView(getApplicationContext());
                    col.setTextColor(Color.DKGRAY);
                    col.setText(Expense.toCurrencyWithSymbol(Float.parseFloat(value.toString())));
                    col.setPadding(10, 10, 10, 10);
                    row.addView(col);

                    setRowClickListener(row, "item");
                    item_status_table.addView(row);
                    //status.append("For " + key + " you spent " + value + " rs\n");
                } catch (JSONException e) {
                    Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setRowClickListener(final TableRow tr, final String ptable) {
        tr.setClickable(true);
        tr.setBackgroundResource(drawable.list_selector_background);
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean team_only = true;
                String name = "", spent_for = "";
                String date = date_field.getText().toString();
                TextView tv = (TextView) tr.getChildAt(0);
                if (ptable.equals("item"))
                    spent_for = tv.getText().toString();
                else if (ptable.equals("self")) {
                    team_only = false;
                    name = tv.getText().toString();
                } else if (ptable.equals("team"))
                    name = tv.getText().toString();
                else if (ptable.equals("loan")) {
                    String str[] = tv.getText().toString().split(" gave to ");
                    name = str[0];
                    spent_for = str[1];
                    team_only = false;
                }

                showList(date, name, spent_for, "filtered", team_only);
            }
        });
    }

    private String get_status_as_string() {
        String status = "";

        // Name vice table
        if (!self_only && nameVice.length() > 0) {
            status = "Team Expense Share Details\n";

            String date[] = date_field.getText().toString().split("/");
            status += "01/" + date[1] + "/" + date[2] + " - " + Expense.toDateString(db.getEndOfMonthEpoch(date_field.getText().toString())) + "\n\n";

            Iterator<?> name_iter = nameVice.keys();

            float total_amt = 0;
            while (name_iter.hasNext()) {
                String key = (String) name_iter.next();
                try {
                    Object value = nameVice.get(key);
                    status += key + ":  " + Expense.toCurrencyWithSymbol(Float.parseFloat(value.toString())) + "\n";
                    total_amt += Float.parseFloat(value.toString());
                } catch (JSONException e) {
                    Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                }
            }

            // Total
            status += "\nTotal Spent:  " + Expense.toCurrencyWithSymbol(total_amt) + "\n";

            // Each share
            share_amt = total_amt / nameVice.length();
            status += "Each One's Share:  " + Expense.toCurrencyWithSymbol(share_amt) + "\n";
        }

        // Loan table
        if (loanVice.length() > 0) {

            status += "\n\nLoan Details:\n";

            loan_status.setText("Loan Details");
            Iterator<?> name_iter = loanVice.keys();

            while (name_iter.hasNext()) {
                String key = (String) name_iter.next();
                try {
                    Object value = loanVice.get(key);

                    // Check if this loan is among team members
                    String loaners[] = key.split(" gave to ");
                    if ((indexOf(non_team_users, loaners[0]) == -1) && (indexOf(non_team_users, loaners[0]) == -1)) {

                        float amt = Float.parseFloat(value.toString());
                        status += key + ":  " + Expense.toCurrencyWithSymbol(amt) + "\n";

                    }
                } catch (JSONException e) {
                    Toast.makeText(ExpenseStatus.this, "!!!Something Went Wrong!!!", Toast.LENGTH_LONG).show();
                }
            }
        }

        return status;
    }
}
