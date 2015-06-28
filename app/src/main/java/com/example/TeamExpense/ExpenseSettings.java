package com.example.TeamExpense;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.myapp1.R;

public class ExpenseSettings extends PreferenceActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings_ui);
    }
}
