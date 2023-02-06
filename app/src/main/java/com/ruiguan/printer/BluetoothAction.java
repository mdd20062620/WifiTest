package com.ruiguan.printer;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.ruiguan.R;

public class BluetoothAction {
    private Activity activity = null;
    private ListView unbondDevices = null;
    private ListView bondDevices = null;
    private Context context = null;
    private Boolean isPress = false;
    private BluetoothService bluetoothService = null;

    public BluetoothAction(Context context, ListView bondDevices,Activity activity) {
        super();
        this.context = context;
        this.unbondDevices = unbondDevices;
        this.bondDevices = bondDevices;
        this.activity = activity;
        this.isPress = isPress;
        this.bluetoothService = new BluetoothService(this.context,this.bondDevices);
        bluetoothService.searchDevices();
    }

    public void setUnbondDevices(ListView unbondDevices) {
        this.unbondDevices = unbondDevices;
    }

    /**
     * 初始化界面
     */
    public void initView() {

        if (this.bluetoothService.isOpen()) {
            System.out.println("蓝牙有开!");
        }
        if (!this.bluetoothService.isOpen()) {
            System.out.println("蓝牙没开!");
        }
    }

    public boolean getPress(){
        return this.bluetoothService.isBondPress();
    }
    private void searchDevices() {
        bluetoothService.searchDevices();
    }
}
