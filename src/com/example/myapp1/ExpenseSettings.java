package com.example.myapp1;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ExpenseSettings extends PreferenceActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings_ui);
    }
}
