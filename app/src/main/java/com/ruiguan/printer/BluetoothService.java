 package com.ruiguan.printer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.ruiguan.R;
import java.util.ArrayList;
import java.util.HashMap;

public class BluetoothService {
    private Context context = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList<BluetoothDevice> bondDevices = null;// 用于存放已配对蓝牙设备
    private Button searchDevices= null;
    private ListView bondDevicesListView= null;
    private boolean isBondPress = false;
    public boolean isBondPress() {
        return isBondPress;
    }
    private void addBondDevicesToListView() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>> ();
        int count = this.bondDevices.size();
        System.out.println("已绑定设备数量：" + count);
        for (int i = 0; i < count; i++) {
            HashMap<String, Object> map = new HashMap<String, Object> ();
            map.put("deviceName", this.bondDevices.get(i).getName());
            data.add(map);// 把item项的数据加到data中
        }
        String[] from = { "deviceName" };
        int[] to = { R.id.device_name };
        SimpleAdapter simpleAdapter = new SimpleAdapter (this.context, data, R.layout.bonddevice_item, from, to);
        // 把适配器装载到listView中
        this.bondDevicesListView.setAdapter(simpleAdapter);
        this.bondDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        BluetoothDevice device = bondDevices.get(arg2);
                        SharedPreferences bleSharedPreferences = context.getSharedPreferences( "BLE_Info", Activity.MODE_PRIVATE );
                        SharedPreferences.Editor bleeditor = bleSharedPreferences.edit();
                        bleeditor.putString("Printer", device.getAddress());
                        bleeditor.putBoolean("BondPrinter",true);
                        bleeditor.apply();
                        isBondPress = true;
                    }
                });
    }

    public BluetoothService(Context context, ListView bondDevicesListView) {
        this.context = context;
        this.bondDevicesListView = bondDevicesListView;
        this.bondDevices = new ArrayList<BluetoothDevice> ();
        this.initIntentFilter();
    }

    private void initIntentFilter() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter ();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        context.registerReceiver(receiver, intentFilter);
    }
    /**
     * 打开蓝牙
     */
    public void openBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, 1);
    }
    /**
     * 关闭蓝牙
     */
    public void closeBluetooth() {
        this.bluetoothAdapter.disable();
    }
    /**
     * 判断蓝牙是否打开
     *
     * @return boolean
     */
    public boolean isOpen()
    {
        return this.bluetoothAdapter.isEnabled();
    }

    /**
     * 搜索蓝牙设备
     */
    public void searchDevices() {
        this.bondDevices.clear();
        // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
        this.bluetoothAdapter.startDiscovery();
    }

    /**
     * 添加未绑定蓝牙设备到list集合
     *
     * @param device
     */
    public void addUnbondDevices(BluetoothDevice device) {
        System.out.println("未绑定设备名称：" + device.getName());
        if (!this.bondDevices.contains(device)) {
            this.bondDevices.add(device);
        }
    }
    /**
     * 添加已绑定蓝牙设备到list集合
     *
     * @param device
     */
    public void addBandDevices(BluetoothDevice device) {
        System.out.println("已绑定设备名称：" + device.getName());
        if (!this.bondDevices.contains(device)) {
            this.bondDevices.add(device);
        }
    }
    /**
     * 蓝牙广播接收器
     */
    private BroadcastReceiver receiver = new BroadcastReceiver () {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 addBandDevices(device);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                System.out.println("设备搜索完毕");
                addBondDevicesToListView();
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    System.out.println("--------打开蓝牙-----------");
                    searchDevices.setEnabled(true);
                    bondDevicesListView.setEnabled(true);
                } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    System.out.println("--------关闭蓝牙-----------");
                    searchDevices.setEnabled(false);
                    bondDevicesListView.setEnabled(false);
                }
            }
        }

    };
}

