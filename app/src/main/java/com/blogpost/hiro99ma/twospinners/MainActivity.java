package com.blogpost.hiro99ma.twospinners;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private Spinner mSpinSub;
    private ArrayAdapter<String> mAdapterSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinMain = (Spinner) this.findViewById(R.id.spin_main_category);
        mSpinSub = (Spinner) this.findViewById(R.id.spin_sub_category);
        mAdapterSub = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        if ((spinMain == null) || (mSpinSub == null)) {
            mSpinSub = null;
            mAdapterSub = null;
            return;
        }

        //main category
        spinMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < id + 3; i ++) {
                    mAdapterSub.add("item" + i);
                }
                mSpinSub.setAdapter(mAdapterSub);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //sub category
        mSpinSub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
