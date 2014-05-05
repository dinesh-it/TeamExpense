package com.example.myapp1;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "ddExpenseManager";
 
    // Expense table name
    private static final String TABLE_EXPENSES = "Expenses";
 
    // Expense Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";
    private static final String KEY_SPENT_FOR = "spentfor";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_AMT = "spentamt";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db){
    	String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_DATE + " INTEGER," + KEY_SPENT_FOR + " TEXT,"
                + KEY_COMMENT + " TEXT," + KEY_AMT + " TEXT" + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
    	onCreate(db);
    }
    
    public void addExpense(Expense exp){
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(KEY_NAME, exp.getName());
    	values.put(KEY_DATE, exp.getDate());
    	values.put(KEY_SPENT_FOR, exp.getSpentFor());
    	values.put(KEY_COMMENT, exp.getComment());
    	values.put(KEY_AMT, exp.getAmt());
    	db.insert(TABLE_EXPENSES, null, values);
        db.close();
    };
    
    public Expense getExpense(int id) {
        //SQLiteDatabase db = this.getReadableDatabase();
     
        //Cursor cursor = db.query(TABLE_EXPENSES, new String[] { KEY_ID,
        //        KEY_DATE, KEY_NAME, KEY_SPENT_FOR, KEY_COMMENT, KEY_AMT }, KEY_ID + "=?",
        //        new String[] { String.valueOf(id) }, null, null, null, null);
    	String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + " WHERE " + KEY_ID + " = " + id  ;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null)
            cursor.moveToFirst();  
        Expense exp = new Expense(cursor.getString(1), Long.parseLong(cursor.getString(2)),cursor.getString(3),cursor.getString(4),Float.parseFloat(cursor.getString(5)));
        return exp;
    }
    
    public boolean deleteExpense(int id){
    	SQLiteDatabase db = this.getReadableDatabase();
        if(db.delete(TABLE_EXPENSES, KEY_ID + "=?", new String[] { String.valueOf(id) }) == 1)
        	return true;
        return false;
    }
    
    public List<Expense> getAllExpenses() {
        List<Expense> expenseList = new ArrayList<Expense>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + " ORDER BY " + KEY_DATE + " DESC";
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Long.parseLong(cursor.getString(2)),cursor.getString(3),cursor.getString(4),Float.parseFloat(cursor.getString(5)));
                expenseList.add(exp);
            } while (cursor.moveToNext());
        }
        return expenseList;
    }
    
    public List<Expense> getMonthExpenses(String date_str) {
    	String date[] = date_str.split("/");
    	long start_date = Expense.toEpoch("01/"+date[1]+"/"+date[2]);
    	long end_date = Expense.toEpoch("01/"+ ( Integer.parseInt(date[1]) + 1 )+"/"+date[2]) - 1;
        List<Expense> expenseList = new ArrayList<Expense>();
        String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + 
        		" WHERE " + KEY_DATE + " BETWEEN " + start_date +" AND " + end_date +
        		" ORDER BY " + KEY_DATE + " DESC";
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Long.parseLong(cursor.getString(2)),cursor.getString(3),cursor.getString(4),Float.parseFloat(cursor.getString(5)));
                expenseList.add(exp);
            } while (cursor.moveToNext());
        }
        return expenseList;
    }
    
    public List<Expense> getFilteredExpenses(String date_str,String name, String spent_for){
    	String date[] = date_str.split("/");
    	long start_date = Expense.toEpoch("01/"+date[1]+"/"+date[2]);
    	long end_date = Expense.toEpoch("01/"+ ( Integer.parseInt(date[1]) + 1 )+"/"+date[2]) - 1;
        List<Expense> expenseList = new ArrayList<Expense>();
        String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + 
        		" WHERE " + KEY_DATE + " BETWEEN " + start_date +" AND " + end_date;
        if(name != null && name != "")
        	selectQuery += " AND " + KEY_NAME + " = '" + name + "'";
        if(spent_for != null && spent_for != "")
        	selectQuery += " AND " + KEY_SPENT_FOR + " = '" + spent_for + "'";
        
        selectQuery +=	" ORDER BY " + KEY_DATE + " DESC";
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Long.parseLong(cursor.getString(2)),cursor.getString(3),cursor.getString(4),Float.parseFloat(cursor.getString(5)));
                expenseList.add(exp);
            } while (cursor.moveToNext());
        }
        return expenseList;
    }
    
    public String[] getAllNames() {
        String selectQuery = "SELECT DISTINCT " + KEY_NAME +" FROM " + TABLE_EXPENSES;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String names[] = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
        	int i =0;
            do {
            	names[i++] = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return names;
    }
    
    public String[] getAllItems() {
        String selectQuery = "SELECT DISTINCT " + KEY_SPENT_FOR +" FROM " + TABLE_EXPENSES;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String items[] = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
        	int i =0;
            do {
            	items[i++] = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return items;
    }
}
