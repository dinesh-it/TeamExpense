package com.example.TeamExpense;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Expense {
    int id;
    long date;
    String name;
    String spent_for;
    String comment;
    Float amt;
    private static String Month[] = {"Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static String Day[] = {"Sun","Mon","Tue","Wed", "Thu","Fri","Sat"};

    public Expense() {

    }

    public Expense(int tid, String tname, long tdate, String tspent_for, String tcomment, Float tamt) {
        this.id = tid;
        this.date = tdate;
        this.name = tname;
        this.spent_for = tspent_for;
        this.comment = tcomment;
        this.amt = tamt;
    }

    public Expense(String tname, long tdate, String tspent_for, String tcomment, Float tamt) {
        this.id = 0;
        this.date = tdate;
        this.name = tname;
        this.spent_for = tspent_for;
        this.comment = tcomment;
        this.amt = tamt;
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

}
