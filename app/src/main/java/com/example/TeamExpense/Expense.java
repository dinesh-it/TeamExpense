package com.example.TeamExpense;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapp1.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Expense {
    private int id;
    private long date;
    private String name, spent_for, comment;
    private Float amt;
    private String payment_mode = "CASH";
    private String group = "OTHER";
    private static String Month[] = {"Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static String Day[] = {"Sun","Mon","Tue","Wed", "Thu","Fri","Sat"};

    public Expense() {

    }

    public Expense(int tid, String tname, long tdate, String tspent_for, String tcomment, Float tamt, String group, String mode) {
        this.id = tid;
        this.date = tdate;
        this.name = tname;
        this.spent_for = tspent_for;
        this.comment = tcomment;
        this.amt = tamt;

        if(mode == null || mode.equalsIgnoreCase("")){
            mode = "CASH";
        }

        this.payment_mode = mode.toUpperCase();

        if(group == null || group.equalsIgnoreCase("")){
            group = "OTHER";
        }
        this.group = group.toUpperCase();
    }

    public Expense(String tname, long tdate, String tspent_for, String tcomment, Float tamt, String group, String mode) {
        this.id = 0;
        this.date = tdate;
        this.name = tname;
        this.spent_for = tspent_for;
        this.comment = tcomment;
        this.amt = tamt;

        if(mode == null || mode.equalsIgnoreCase("")){
            mode = "CASH";
        }

        this.payment_mode = mode.toUpperCase();

        if(group == null || group.equalsIgnoreCase("")){
            group = "OTHER";
        }
        this.group = group.toUpperCase();
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public long getDate() {
        return this.date;
    }

    public String getSpentFor() {
        return this.spent_for;
    }

    public String getComment() {
        return this.comment;
    }

    public Float getAmt() {
        return this.amt;
    }

    public Float setAmt(float amt) {
        this.amt = amt;
        return this.amt;
    }

    public String getPaymentMode() { return this.payment_mode.toUpperCase(); }

    public String getGroup() { return this.group.toUpperCase(); }

    public static long toEpoch(String str_date) {
        long epoch_date;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", new Locale("english", "India"));
        Date gmt = new Date();
        try {
            gmt = formatter.parse(str_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        epoch_date = gmt.getTime();
        return epoch_date;
    }

    public static String toDateString(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", new Locale("english", "India"));
        Date gmt = new Date(date);
        String asString = formatter.format(gmt);
        return asString;
    }

    public static String toCurrencyWithSymbol(float val) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
        return formatter.format(val);
    }

    public static String toCurrency(float val) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "in"));
        return formatter.format(val);
    }

    public static String getWeekDay(String date_str) {
        String date[] = date_str.split("/");
        final Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));
        return Day[calendar1.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static String getStringMonth(String date_str) {
        String date[] = date_str.split("/");
        int month = Integer.parseInt(date[1]);
        return Month[month - 1];
    }

    public static void showEditDialog(final int exp_id, final Context context, final DatabaseHandler db){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.expense_edit);
        dialog.setTitle("Edit Expense");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        final EditText desc, amt, date_field;
        final AutoCompleteTextView spent_for, name;
        final Spinner pmt_mode_s, group_name;
        Button save_btn, cancel_btn, del_btn;
        date_field = (EditText)dialog.findViewById(R.id.editText1);
        date_field.setSingleLine(true);
        date_field.setFocusable(false);

        name = (AutoCompleteTextView)dialog.findViewById(R.id.autoCompleteTextView1);
        name.setSingleLine(true);
        name.setFocusable(false);
        spent_for = (AutoCompleteTextView)dialog.findViewById(R.id.autoCompleteTextView2);
        spent_for.setSingleLine(true);
        spent_for.setFocusable(false);
        ExpensesView.handleNextKey(name, spent_for);
        desc = (EditText)dialog.findViewById(R.id.editText4);
        desc.setSingleLine(true);
        desc.setFocusable(false);
        ExpensesView.handleNextKey(spent_for, desc);
        amt = (EditText)dialog.findViewById(R.id.editText5);
        amt.setSingleLine(true);
        amt.setFocusable(false);
        ExpensesView.handleNextKey(desc, amt);

        pmt_mode_s = (Spinner) dialog.findViewById(R.id.pmt_mode);
        group_name = (Spinner) dialog.findViewById(R.id.group_name);

        ExpensesView.handleNextKey(amt, null);
        save_btn = (Button)dialog.findViewById(R.id.save_btn);
        save_btn.setText("Edit");
        cancel_btn = (Button)dialog.findViewById(R.id.close_btn);
        del_btn = (Button)dialog.findViewById(R.id.del_btn);
        Expense e = db.getExpense(exp_id);
        name.setText(e.getName());
        date_field.setText(Expense.toDateString(e.getDate()));
        spent_for.setText(e.getSpentFor());
        desc.setText(e.getComment());
        amt.setText(e.getAmt() + "");

        Expense.setAutoCompletes(name,spent_for,group_name,db, context);

        // Save button action
        save_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Button btn = (Button)view;
                if(btn.getText().toString().equals("Edit")){
                    date_field.setFocusableInTouchMode(true);
                    name.setFocusableInTouchMode(true);
                    spent_for.setFocusableInTouchMode(true);
                    desc.setFocusableInTouchMode(true);
                    amt.setFocusableInTouchMode(true);
                    btn.setText("Update");
                    return;
                }
                if(date_field.getText() + "" == "" || name.getText() + "" == "" || spent_for.getText() + "" == "" || amt.getText() + "" == ""){
                    Toast.makeText(context, "ERROR: Incomplete form", Toast.LENGTH_LONG).show();
                }
                else {
                    String pmt_mode = String.valueOf(pmt_mode_s.getSelectedItem());
                    String pmt_group = String.valueOf(group_name.getSelectedItem());
                    Expense exp = new Expense(exp_id,
                            name.getText().toString(),
                            Expense.toEpoch(date_field.getText().toString()),
                            spent_for.getText().toString(),
                            desc.getText().toString(),
                            Float.parseFloat(""+amt.getText()),pmt_group,pmt_mode);
                    if(db.updateExpense(exp)){

                        Toast.makeText(context, "Updated Successfuly!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(context, "ERROR: Update failed!", Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                }
            }
        });

        // Cancel button action
        cancel_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                dialog.cancel();
            }
        });

        // Delete button action
        del_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Delete?");
                alertDialogBuilder
                        .setMessage("Are you sure?\nYou want to delete this item?")
                        .setCancelable(false)
                        .setPositiveButton("Delete",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog1,int id) {
                                if(db.deleteExpense(exp_id)){
                                    Toast.makeText(context, "Deleted Successfuly!", Toast.LENGTH_LONG).show();
                                    dialog.cancel();
                                }
                                else
                                    Toast.makeText(context, "ERROR: Delete failed", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog1,int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.show();
            }
        });

        dialog.show();
    }

    public static void setAutoCompletes(AutoCompleteTextView name, AutoCompleteTextView spent_for, Spinner group_name, DatabaseHandler db,Context c) {
        String[] name_list = db.getAllNames(null).clone();
        ArrayAdapter<String> names_adapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, name_list);
        name.setAdapter(names_adapter);

        String[] items_list = db.getAllItems().clone();
        ArrayAdapter<String> items_adapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, items_list);
        spent_for.setAdapter(items_adapter);

        String[] groups = db.getAllGroups();
        if(groups.length == 0 || groups.length < 2){
            groups = new String[2];
            groups[0] = "ROOM";
            groups[1] = "SELF";
        }
        ArrayAdapter<String> groups_adapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, groups);
        group_name.setAdapter(groups_adapter);
    }

}
