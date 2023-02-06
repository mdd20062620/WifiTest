package com.ruiguan.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

import com.ruiguan.R;
import com.ruiguan.activities.ActivityCollector;
import com.ruiguan.activities.MenuActivity;
import com.ruiguan.base.BaseActivity;

public class PrintScanActivity extends BaseActivity {
    private Context context = null;
    private Boolean isPress = false;
    private BluetoothAction bluetoothAction;
    private ListView bondDevices;
    private Button backPrintBtn;
    private Button exitPrintBtn;

    Handler handler = new Handler ();
    Runnable runnable = new Runnable () {
        @Override
        public void run() {
            if(bluetoothAction.getPress()){                //是否有新打印机配对
                finish();
            }
            handler.postDelayed(runnable,500);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.printerscan_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ActivityCollector.addActivity(this);

        bondDevices = (ListView) findViewById(R.id.bondDevices);
        backPrintBtn=(Button) findViewById(R.id.backPrintBtn);
        exitPrintBtn=(Button) findViewById(R.id.exitPrintBtn);
        View.OnClickListener bl = new PrintScanActivity.ButtonListener();
        setOnClickListener(backPrintBtn, bl);
        setOnClickListener(exitPrintBtn, bl);
        initListener();
    }
    public void setOnClickListener(final Button button, final View.OnClickListener buttonListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button != null) {
                    button.setOnClickListener(buttonListener);
                }
            }
        });
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            final String METHODTAG = ".ButtonListener.onClick";
            switch (v.getId()) {
                case R.id.backPrintBtn: {
                    handler.removeCallbacks (runnable);
                    Intent intent1 = new Intent(PrintScanActivity.this, MenuActivity.class);
                    startActivity(intent1);
                    finish();
                }
                break;
                case R.id.exitPrintBtn: {
                    handler.removeCallbacks (runnable);
                    finish();
                }
                break;
                default: {
                }
                break;
            }
        }
    }

    private void initListener() {
        bluetoothAction = new BluetoothAction(this.context,bondDevices,PrintScanActivity.this);
        bluetoothAction.initView();
      //  searchBtn.setOnClickListener(bluetoothAction);
        handler.postDelayed(runnable,500);
    }
}
