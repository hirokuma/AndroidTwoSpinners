package com.blogpost.hiro99ma.twospinners;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity {

    private Spinner mSpinSub;
    private ArrayAdapter<String> mAdapterSub;

    private int mMainCategoryIdx = 0;
    private int mSubCategoryIdx = 0;
    private Command[][] mExecCommands;

    //ここにテストクラスのインスタンスを追加する
    private ITestForm[] mTestForm = new ITestForm[] {
            Test1.getInstance(),
            Test2.getInstance(),
            Test3.getInstance(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExecCommands = new Command[mTestForm.length + 1][];
        mExecCommands[0] = new Command[mTestForm.length];
        int idx = 0;
        for (final ITestForm form : mTestForm) {
            mExecCommands[0][idx] = new Command() {
                @Override
                String name() {
                    return form.getCategoryName();
                }
            };
            idx++;
        }
        idx = 1;
        for (ITestForm form : mTestForm) {
            mExecCommands[idx] = form.getSubCategory();
            idx++;
        }

        Spinner spinMain = (Spinner) this.findViewById(R.id.spin_main_category);
        Button button = (Button)findViewById(R.id.button_exec);
        mSpinSub = (Spinner) this.findViewById(R.id.spin_sub_category);
        mAdapterSub = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        if ((spinMain == null) || (mSpinSub == null) || (button == null)) {
            mSpinSub = null;
            mAdapterSub = null;
            return;
        }

        //main category
        ArrayAdapter adapterMain = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        for (Command cmd : mExecCommands[0]) {
            adapterMain.add(cmd.name());
        }
        spinMain.setAdapter(adapterMain);

        spinMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMainCategoryIdx = (int)id;
                mSubCategoryIdx = 0;

                mAdapterSub.clear();
                //int idx = 1;
                for (Command cmd : mExecCommands[mMainCategoryIdx + 1]) {
                    mAdapterSub.add(cmd.name());
                    //mAdapterSub.add(String.valueOf(idx) + "." + cmd.name());
                    //idx++;
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
                mSubCategoryIdx = (int)id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("EXEC " + String.valueOf(mMainCategoryIdx + 1) + "-" + String.valueOf(mSubCategoryIdx + 1), "[" + mSpinSub.getSelectedItem().toString() + "]");
                mExecCommands[mMainCategoryIdx + 1][mSubCategoryIdx].execute(MainActivity.this);
            }
        });
    }
}
