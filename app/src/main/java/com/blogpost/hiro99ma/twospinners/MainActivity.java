package com.blogpost.hiro99ma.twospinners;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

interface ICommand {
    ///spinnerのアイテム名
    String name();
    ///実行する内容
    void execute();
}

public class MainActivity extends AppCompatActivity {

    private Spinner mSpinSub;
    private ArrayAdapter<String> mAdapterSub;

    private int mMainCategoryIdx = 0;
    private int mSubCategoryIdx = 0;

    private ICommand[][] mExecCommands = new ICommand[][] {
        //main1
        new ICommand[] {
                //main1
                new ICommand() {
                    @Override
                    public String name() {
                        return "main1";
                    }
                    @Override
                    public void execute() {}
                },
                //main2
                new ICommand() {
                    @Override
                    public String name() {
                        return "main2";
                    }
                    @Override
                    public void execute() {}
                },
                //main3
                new ICommand() {
                    @Override
                    public String name() {
                        return "main3";
                    }
                    @Override
                    public void execute() {}
                },
        },

        //main1
        new ICommand[] {
                //sub1-1
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub1-1";
                    }
                    @Override
                    public void execute() {
                        Log.d("main1", "sub1-1");
                    }
                },
                //sub1-2
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub1-2";
                    }
                    @Override
                    public void execute() {
                        Log.d("main1", "sub1-2");
                    }
                },
                //sub1-3
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub1-3";
                    }
                    @Override
                    public void execute() {
                        Log.d("main1", "sub1-3");
                    }
                },
        },

        //main2
        new ICommand[] {
                //sub2-1
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub2-1";
                    }
                    @Override
                    public void execute() {
                        Log.d("main2", "sub2-1");
                    }
                },
                //sub2-2
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub2-2";
                    }
                    @Override
                    public void execute() {
                        Log.d("main2", "sub2-2");
                    }
                },
        },

        //main3
        new ICommand[] {
                //sub3-1
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub3-1";
                    }
                    @Override
                    public void execute() {
                        Log.d("main3", "sub3-1");
                    }
                },
                //sub3-2
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub3-2";
                    }
                    @Override
                    public void execute() {
                        Log.d("main3", "sub3-2");
                    }
                },
                //sub3-3
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub3-3";
                    }
                    @Override
                    public void execute() {
                        Log.d("main3", "sub3-3");
                    }
                },
                //sub3-4
                new ICommand() {
                    @Override
                    public String name() {
                        return "sub3-4";
                    }
                    @Override
                    public void execute() {
                        Log.d("main3", "sub3-4");
                    }
                },
        },
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        for (ICommand cmd : mExecCommands[0]) {
            adapterMain.add(cmd.name());
        }
        spinMain.setAdapter(adapterMain);

        spinMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMainCategoryIdx = (int)id;
                mSubCategoryIdx = 0;

                mAdapterSub.clear();
                for (ICommand cmd : mExecCommands[mMainCategoryIdx + 1]) {
                    mAdapterSub.add(cmd.name());
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
                mExecCommands[mMainCategoryIdx + 1][mSubCategoryIdx].execute();
            }
        });
    }
}
