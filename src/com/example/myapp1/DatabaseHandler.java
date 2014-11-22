package com.example.myapp1;

import java.util.ArrayList;
import java.util.Calendar;
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
    
    public static boolean isModified = false;
 
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
                + KEY_COMMENT + " TEXT," + KEY_AMT + " DECIMAL(5,2)" + ")";
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
        isModified = true;
    };
    
    public boolean updateExpense(Expense exp){
    	try{
    		SQLiteDatabase db = this.getWritableDatabase();
        	ContentValues values = new ContentValues();
        	values.put(KEY_NAME, exp.getName());
        	values.put(KEY_DATE, exp.getDate());
        	values.put(KEY_SPENT_FOR, exp.getSpentFor());
        	values.put(KEY_COMMENT, exp.getComment());
        	values.put(KEY_AMT, exp.getAmt());
        	db.update(TABLE_EXPENSES, values, KEY_ID + " = ?", new String[] { String.valueOf(exp.getId()) });
            db.close();
            isModified = true;
            return true;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }
    
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
        if(db.delete(TABLE_EXPENSES, KEY_ID + "=?", new String[] { String.valueOf(id) }) == 1){
        	isModified = true;
        	return true;
        }
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
    
    public List<Expense> getMonthExpenses(String from_date, boolean team_only){
    	String date[] = from_date.split("/");
    	from_date = "01/"+date[1]+"/"+date[2];
    	long to_date = this.getEndOfMonthEpoch(from_date);
    	return this.getMonthExpenses(from_date, Expense.toDateString(to_date), team_only);
    }
    
    public List<Expense> getMonthExpenses(String from_date,String to_date, boolean team_only) {
    	long start_date = Expense.toEpoch(from_date);
    	// add 86400 to get till end of the day
    	long end_date = Expense.toEpoch(to_date) + 86399;
        List<Expense> expenseList = new ArrayList<Expense>();
        String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + 
        		" WHERE " + KEY_DATE + " BETWEEN " + start_date +" AND " + end_date;
        if(team_only){
        	selectQuery += " AND " + KEY_SPENT_FOR + " NOT IN (";
        	String names[] = getAllNames(null);
        	for(int i=0;i< names.length;i++){
        		if(! (i == 0)){
        			selectQuery += ", ";
        		}
        		names[i] = names[i].replace("'", "''");
        		selectQuery += "'" + names[i] + "'";
        	}
        	selectQuery += ")";
        }
        
        selectQuery += " ORDER BY " + KEY_DATE + " DESC";
     
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
    
    public List<Expense> getFilteredExpenses(String date_str,String name, String spent_for,boolean team_only){
    	return getFilteredExpenses(date_str,name, spent_for,team_only, "desc",null,null,null);
    }
    
    public long getEndOfMonthEpoch(String date_str){
    	String date[] = date_str.split("/");
    	if(date_str.equals("")){
    		final Calendar calendar1 = Calendar.getInstance();
	        int year = calendar1.get(Calendar.YEAR);
	        int month = calendar1.get(Calendar.MONTH) + 1;
	        int day = calendar1.get(Calendar.DAY_OF_MONTH);
	        date = new String[] { day + "",month + "" ,year + ""};
    	}
    	return Expense.toEpoch("01/" + ( Integer.parseInt(date[1]) + 1 ) + "/" + date[2]) - 1;
    }
    
    public List<Expense> getFilteredExpenses(String date_str,String name, String spent_for,boolean team_only,String date_sort,String name_sort,String item_sort,String amt_sort){
    	String date[] = date_str.split("/");
    	long start_date = 0;
    	if(!date_str.equals("")){
    		start_date = Expense.toEpoch("01/"+date[1]+"/"+date[2]);
    	}
    	long end_date = this.getEndOfMonthEpoch(date_str);
    	return getFilteredExpenses(start_date,end_date,name, spent_for,team_only, "desc",null,null,null);
    }
    
    public List<Expense> getFilteredExpenses(long from_epoch,long to_epoch,String name, String spent_for,boolean team_only,String date_sort,String name_sort,String item_sort,String amt_sort){
        List<Expense> expenseList = new ArrayList<Expense>();
        String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + 
        		" WHERE " + KEY_DATE + " BETWEEN " + from_epoch +" AND " + to_epoch;
        if(name != null && ! name.equals(""))
        	name = name.replace("'", "''");
        	selectQuery += " AND " + KEY_NAME + " LIKE '%" + name + "%'";
        if(spent_for != null && ! spent_for.equals(""))
        	spent_for = spent_for.replace("'", "''");
        	selectQuery += " AND " + KEY_SPENT_FOR + " LIKE '%" + spent_for + "%'";
        if(team_only){
        	selectQuery += " AND " + KEY_SPENT_FOR + " NOT IN (";
        	String names[] = getAllNames(null);
        	for(int i=0;i< names.length;i++){
        		if(! (i == 0)){
        			selectQuery += ", ";
        		}
        		names[i] = names[i].replace("'", "''");
        		selectQuery += "'" + names[i] + "'";
        	}
        	selectQuery += ")";
        }
        
        String sort_qr = "";
        
        if(date_sort != null){
        	sort_qr += KEY_DATE + " " + date_sort;
        }
        if(name_sort != null){
        	if(!sort_qr.equalsIgnoreCase("")){
        		sort_qr += " , "; 
        	}
        	sort_qr += KEY_NAME + " " + name_sort;
        }
        if(item_sort != null){
        	if(!sort_qr.equalsIgnoreCase("")){
        		sort_qr += " , "; 
        	}
        	sort_qr += KEY_SPENT_FOR + " " + item_sort;
        }
        if(amt_sort != null){
        	if(!sort_qr.equalsIgnoreCase("")){
        		sort_qr += " , "; 
        	}
        	sort_qr += KEY_AMT + " " + amt_sort;
        }
        
        if(!sort_qr.equalsIgnoreCase("")){
            selectQuery +=	" ORDER BY " + sort_qr;
    	}
     
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
    
    public String[] getAllNames(String type) {
        String selectQuery = "SELECT DISTINCT " + KEY_NAME +" FROM " + TABLE_EXPENSES;
        if(type != null){
        	selectQuery += " WHERE " + KEY_COMMENT + " LIKE '%" + type + "%'";
        }
     
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
