package com.ruiguan.activities;

import static com.ruiguan.activities.MenuActivity.input_data;
import static com.ruiguan.fragments.FragmentMessage.mIp;
import static com.ruiguan.fragments.FragmentMessage.mIsConnectInternet;
import static com.test.connectservicelibrary.connectInternet.ToolClass.bytesToHexString;
import static com.test.connectservicelibrary.connectInternet.ToolClass.hexString2ByteArray;
import static com.test.connectservicelibrary.connectInternet.ToolClass.hexStringToString;

import static java.security.AccessController.getContext;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.ruiguan.ImplantFragment;
import com.ruiguan.R;
import com.ruiguan.baseUse.DefaultNavigationBar;
import com.ruiguan.fragments.FragmentList;
import com.ruiguan.fragments.FragmentManage;
import com.ruiguan.fragments.FragmentMessage;
import com.ruiguan.fragments.FragmentSingMessage;
import com.ruiguan.keyboardListener.KeyboardChangeListener;
import com.ruiguan.printer.PrintDataService;
import com.ruiguan.recyclerAdapter.Resou;
import com.ruiguan.storage.DataMemory;
import com.ruiguan.view.LineChartMarkView;
import com.ruiguan.wifiGather.connectSocket.ConnectSocket;
import com.test.connectservicelibrary.connectInternet.ConnectInternetManage;
import com.test.connectservicelibrary.connectInternet.JsonsRootBean;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
public class MeasureActivity extends SaveActivity {
    private final String CONNECTED = "已连接",CONNECTING = "连接中",DISCONNECT = "断线了";
    private String str_company;
    private String str_number;
    private String str_ratedSpeed;

    private Button startBtn;
    private Button stopBtn;
    private Button backBtn;
    private Button analysisBtn;
    private Button deviceBtn;

    private Drawable startBtnpressed;
    private Drawable stopBtnpressed;
    private Drawable analysisBtnpressed;
    private Drawable deviceBtnpressed;

    private TextView Xreal_txt;
    private TextView Yreal_txt;
    private TextView Zreal_txt;
    private TextView XMaxMea_txt;
    private TextView YMaxMea_txt;
    private TextView ZMaxMea_txt;
    private TextView statusAcc_txt;

    private TextView XMeaU_txt;
    private TextView YMeaU_txt;
    private TextView ZMeaU_txt;
    private TextView XMaxMeaU_txt;
    private TextView YMaxMeaU_txt;
    private TextView ZMaxMeaU_txt;

    private String str_Xreal;
    private String str_XMax;
    private String str_Yreal;
    private String str_YMax;
    private String str_Zreal;
    private String str_ZMax;

    private float FirstX=0.0f;
    private float FirstY=0.0f;
    private float FirstZ=0.0f;

    private float ChartYMaxX=0.0f;
    private float ChartYMaxY=0.0f;
    private float ChartYMaxZ=0.0f;
    private float ChartXMax=0.0f;

    private LineChart xchart;
    private LineChart ychart;
    private LineChart zchart;

    final int[] arrayId={0, R.drawable.number5,R.drawable.number4,R.drawable.number3,R.drawable.number2,R.drawable.number1};
    private ImageView count_txt;
    private LinearLayout mainLayout;

    private ArrayAdapter<String> adapter;
    private byte[] senddata;
    private byte[] overReceive;

    private DynamicLineChartManager dynamicLineChartManager_x;
    private DynamicLineChartManager dynamicLineChartManager_y;
    private DynamicLineChartManager dynamicLineChartManager_z;

    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合

    public static ArrayList<Float> xx = new ArrayList<>();//加速度信号
    public static ArrayList<Float> yy = new ArrayList<>();
    public static ArrayList<Float> zz = new ArrayList<>();

    private float xtemp=0.0f;
    private float ytemp=0.0f;
    private float ztemp=0.0f;
    private float xtempreceive=0.0f;
    private float ytempreceive=0.0f;
    private float ztempreceive=0.0f;

    private Handler handler = new Handler();
    private boolean Finish=false;
    private PrintDataService printDataService = null;
    private boolean PrintConnect = false;
    java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.000");

    private MeasureActivity.LocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;
    private String ModuleName;
    private ConnectSocket mConnectSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        ActivityCollector.addActivity(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        senddata=new byte[7];
        overReceive=new byte[17];
        mainLayout=findViewById(R.id.mainLayout);
        count_txt=findViewById(R.id.count_txt);

        str_company= input_data.getCom();
        str_number=input_data.getNumber();
        str_ratedSpeed=input_data.getRatedspeed();

        View.OnClickListener bl = new ButtonListener();
        startBtn = (Button) findViewById(R.id.startAccMeaBtn);
        stopBtn = (Button) findViewById(R.id.stopAccMeaBtn);
        backBtn= (Button) findViewById(R.id.backAccMeaBtn);
        deviceBtn= (Button) findViewById(R.id.DeviceAccBtn);
        analysisBtn= (Button) findViewById(R.id.analysisAccMeaBtn);
        setOnClickListener(startBtn, bl);
        setOnClickListener(stopBtn, bl);
        setOnClickListener(backBtn, bl);
        setOnClickListener(deviceBtn, bl);
        setOnClickListener(analysisBtn, bl);

        xchart=(LineChart)findViewById(R.id.xchart);
        ychart=(LineChart)findViewById(R.id.ychart);
        zchart=(LineChart)findViewById(R.id.zchart);

        Xreal_txt= (TextView) findViewById(R.id.Xreal_txt);
        Yreal_txt= (TextView) findViewById(R.id.Yreal_txt);
        Zreal_txt= (TextView) findViewById(R.id.Zreal_txt);
        XMaxMea_txt= (TextView) findViewById(R.id.XMaxMea_txt);
        YMaxMea_txt= (TextView) findViewById(R.id.YMaxMea_txt);
        ZMaxMea_txt= (TextView) findViewById(R.id.ZMaxMea_txt);

        XMeaU_txt= (TextView) findViewById(R.id.XMeaU_txt);
        YMeaU_txt= (TextView) findViewById(R.id.YMeaU_txt);
        ZMeaU_txt= (TextView) findViewById(R.id.ZMeaU_txt);
        XMaxMeaU_txt= (TextView) findViewById(R.id.XMaxMeaU_txt);
        YMaxMeaU_txt= (TextView) findViewById(R.id.YMaxMeaU_txt);
        ZMaxMeaU_txt= (TextView) findViewById(R.id.ZMaxMeaU_txt);
        statusAcc_txt=(TextView) findViewById(R.id.statusAcc_txt);
        names.add ("X轴(m/s^2)");
        names.add ("Y轴(m/s^2)");
        names.add ("Z轴(m/s^2)");

        colour.add (Color.argb (255, 25, 25, 112));            //定义X轴颜色
        colour.add (Color.argb (255, 128, 0, 0));            //定义Y轴颜色
        colour.add (Color.argb (255, 139, 0, 139));          //定义Z轴颜色
        dynamicLineChartManager_x = new MeasureActivity.DynamicLineChartManager(xchart, names.get (0), colour.get (0), 0);
        dynamicLineChartManager_y = new MeasureActivity.DynamicLineChartManager(ychart, names.get (1), colour.get (1), 1);
        dynamicLineChartManager_z = new MeasureActivity.DynamicLineChartManager(zchart, names.get (2), colour.get (2), 2);
        initAll();
        connectSocket(false);
        ShowWave();
    }
    private void initAll() {
        ModuleName = getIntent().getStringExtra("Name");
        initBroadcast();
    }
    private void initBroadcast() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.test.huichengwifi.STOP_LIST");
        mIntentFilter.addAction("com.test.huichengwifi.DISSERVICE");
        mIntentFilter.addAction("com.test.huichengwifi.REFRESH_LIST");
        mLocalReceiver = new MeasureActivity.LocalReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mLocalReceiver,mIntentFilter);
    }
    //建立socket连接
    private void connectSocket(boolean isReconnect){
        Log.e("AppRunTime","connectSocket执行..");
        if(mConnectSocket!=null){
            mConnectSocket.showdown();
        }
        mConnectSocket = null;
        if (isReconnect)
            mConnectSocket = new ConnectSocket(MeasureActivity.this,mMessageHandler,ModuleName);
        else
            mConnectSocket = new ConnectSocket(MeasureActivity.this,mMessageHandler,ModuleName,0);
    }
    //接收socket传来的信息,并传给显示的fragment
    private Handler mMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                //receive(msg.what, msg.obj.toString());
                if (msg.what == 0x04) {
                    setIsOnLine(msg.obj.toString());//根据返回的值设置是否在线
                }else{
                    Log.d("MeasureActivity","接收数据数组"+msg.obj.toString());
                    overReceive=hexString2ByteArray(msg.obj.toString());
                   /* String[] str_ReceiveData1=new String[17];
                    for (int i = 0; i <17; i++)
                    {
                        str_ReceiveData1[i] = myformat.format(overReceive[i]);
                    }
                    Log.d("RawData", "     " + str_ReceiveData1[0] + "  " + str_ReceiveData1[1] + " " + str_ReceiveData1[2] + "  " + str_ReceiveData1[3] + " " + str_ReceiveData1[4]
                            + "  " + str_ReceiveData1[5] + " " + str_ReceiveData1[6] + "  " + str_ReceiveData1[7] + " " + str_ReceiveData1[8] + "  " + str_ReceiveData1[9] + " " + str_ReceiveData1[10]
                            + "  " + str_ReceiveData1[11] + " " + str_ReceiveData1[12] + "  " + str_ReceiveData1[13] + " " + str_ReceiveData1[14] + str_ReceiveData1[15] + " " + str_ReceiveData1[16] );*/
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }
    });
    //接收函数
    private void receive(int what,Object data){
       //overReceive=hexString2ByteArray(data.toString());
        Log.d("MeasureActivity","已接收到数据！");
    }

    //设置在线断线状态
    private void setIsOnLine(String state){
        if (state.equals("connected")){
            setState(CONNECTED);
            Log.d("MeasureActivity","已连接！");
        }else {
            setState(DISCONNECT);
            Log.d("MeasureActivity","断开连接！");
        }
    }
    //发送函数
    private void send(String data){
        //局域网发送
        log("局域网发送");
        mConnectSocket.send(data);
    }

    void sendHandler(int what, Object data){
        Handler handler = FragmentSingMessage.getFragmentSingMessage().getSendFragmentHandler();
        Message message = handler.obtainMessage();
        message.what = what;
        if (data != null)
            message.obj = data;
        handler.sendMessage(message);
    }

    //app内广播
    private class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()!= null && intent.getAction().equals("com.test.huichengwifi.STOP_LIST"))
            {

            }
            if (intent.getAction()!= null && intent.getAction().equals("com.test.huichengwifi.DISSERVICE"))
            {

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateReceiveNumber(String data){
        Xreal_txt.setText("R: "+data);
       // ImplantFragment.mAcceptNumber = Integer.parseInt(data);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    private void setState(String state){
        switch (state){
            case CONNECTED://连接成功
                statusAcc_txt.setText("已连接");
                deviceBtnpressed = getResources().getDrawable(R.drawable.wifi1);
                deviceBtnpressed.setBounds(0, 0, deviceBtnpressed.getMinimumWidth(), deviceBtnpressed.getMinimumHeight());
                deviceBtn.setCompoundDrawables(null, deviceBtnpressed, null, null);
                setEnableButton();
                break;
            case CONNECTING://连接中
                statusAcc_txt.setText("连接中");
                deviceBtnpressed = getResources().getDrawable(R.drawable.wifi);
                deviceBtnpressed.setBounds(0, 0, deviceBtnpressed.getMinimumWidth(), deviceBtnpressed.getMinimumHeight());
                deviceBtn.setCompoundDrawables(null, deviceBtnpressed, null, null);
                setButton();
                break;
            case DISCONNECT://连接断开
                statusAcc_txt.setText("断开");
                deviceBtnpressed = getResources().getDrawable(R.drawable.wifi);
                deviceBtnpressed.setBounds(0, 0, deviceBtnpressed.getMinimumWidth(), deviceBtnpressed.getMinimumHeight());
                deviceBtn.setCompoundDrawables(null, deviceBtnpressed, null, null);
                setButton();
                break;
        }
    }

    private void setButton()
    {
        startBtn.setEnabled(false);
        startBtnpressed = getResources().getDrawable(R.drawable.start);
        startBtnpressed.setBounds(0, 0, startBtnpressed.getMinimumWidth(), startBtnpressed.getMinimumHeight());
        startBtn.setCompoundDrawables(null, startBtnpressed, null, null);

        stopBtn.setEnabled(false);
        stopBtnpressed = getResources().getDrawable(R.drawable.stop);
        stopBtnpressed.setBounds(0, 0, stopBtnpressed.getMinimumWidth(), stopBtnpressed.getMinimumHeight());
        stopBtn.setCompoundDrawables(null, stopBtnpressed, null, null);

        analysisBtn.setEnabled(false);
        analysisBtnpressed = getResources().getDrawable(R.drawable.fenxi);
        analysisBtnpressed.setBounds(0, 0, analysisBtnpressed.getMinimumWidth(), analysisBtnpressed.getMinimumHeight());
        analysisBtn.setCompoundDrawables(null, analysisBtnpressed, null, null);
    }
    private void setEnableButton()
    {
        startBtn.setEnabled(true);
        startBtnpressed = getResources().getDrawable(R.drawable.start1);
        startBtnpressed.setBounds(0, 0, startBtnpressed.getMinimumWidth(), startBtnpressed.getMinimumHeight());
        startBtn.setCompoundDrawables(null, startBtnpressed, null, null);

        stopBtn.setEnabled(true);
        stopBtnpressed = getResources().getDrawable(R.drawable.stop1);
        stopBtnpressed.setBounds(0, 0, stopBtnpressed.getMinimumWidth(), stopBtnpressed.getMinimumHeight());
        stopBtn.setCompoundDrawables(null, stopBtnpressed, null, null);

        analysisBtn.setEnabled(true);
        analysisBtnpressed = getResources().getDrawable(R.drawable.fenxi1);
        analysisBtnpressed.setBounds(0, 0, analysisBtnpressed.getMinimumWidth(), analysisBtnpressed.getMinimumHeight());
        analysisBtn.setCompoundDrawables(null, analysisBtnpressed, null, null);
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
                case R.id.startAccMeaBtn: {
                    startBtn.setEnabled(false);
                    startBtnpressed = getResources().getDrawable(R.drawable.start);
                    startBtnpressed.setBounds(0, 0, startBtnpressed.getMinimumWidth(), startBtnpressed.getMinimumHeight());
                    startBtn.setCompoundDrawables(null, startBtnpressed, null, null);
                    stopBtn.setEnabled(true);
                    stopBtnpressed = getResources().getDrawable(R.drawable.stop1);
                    stopBtnpressed.setBounds(0, 0, stopBtnpressed.getMinimumWidth(), stopBtnpressed.getMinimumHeight());
                    stopBtn.setCompoundDrawables(null, stopBtnpressed, null, null);
                    senddata[0]=0x4B;
                    senddata[1]=0x53;
                    senddata[2]=0x03;
                    senddata[3]=0x00;
                    senddata[4]=0x01;
                    senddata[5]=0x3A;
                    senddata[6]=0x3B;
                    send(bytesToHexString(senddata));
                    //mHoldBluetoothAcc.sendData(moduleAcc,senddata);
                    xx.clear();
                    yy.clear();
                    zz.clear();

                    for(int i=0;i<overReceive.length;i++)
                    {
                        overReceive[i]=0;
                    }

                    xtemp=0.0f;
                    ytemp=0.0f;
                    ztemp=0.0f;
                    xtempreceive=0.0f;
                    ytempreceive=0.0f;
                    ztempreceive=0.0f;

                    FirstX=0.00f;
                    FirstY=0.00f;
                    FirstZ=0.00f;
                    
                    ChartYMaxX=0.0f;
                    ChartYMaxY=0.0f;
                    ChartYMaxZ=0.0f;
                    ChartXMax=0.0f;

                    str_Xreal=myformat.format(xtemp);
                    str_XMax=myformat.format(ChartYMaxX);
                    str_Yreal=myformat.format(ytemp);
                    str_YMax=myformat.format(ChartYMaxY);
                    str_Zreal=myformat.format(ztemp);
                    str_ZMax=myformat.format(ChartYMaxZ);

                    Xreal_txt.setText(str_Xreal);
                    XMaxMea_txt.setText(str_XMax);
                    Yreal_txt.setText(str_Yreal);
                    YMaxMea_txt.setText(str_YMax);
                    Zreal_txt.setText(str_Zreal);
                    ZMaxMea_txt.setText(str_ZMax);

                    dynamicLineChartManager_x.clear();
                    dynamicLineChartManager_y.clear();
                    dynamicLineChartManager_z.clear();

                    mainLayout.setVisibility(View.GONE);
                    count_txt.setVisibility(View.VISIBLE);
                    createAnimationThread();
                   handler.postDelayed(ReceiveRunnable,10);
                }
                break;
                case R.id.stopAccMeaBtn: {
                    startBtn.setEnabled(true);
                    startBtnpressed = getResources().getDrawable(R.drawable.start1);
                    startBtnpressed.setBounds(0, 0, startBtnpressed.getMinimumWidth(), startBtnpressed.getMinimumHeight());
                    startBtn.setCompoundDrawables(null, startBtnpressed, null, null);
                    stopBtn.setEnabled(false);
                    stopBtnpressed = getResources().getDrawable(R.drawable.stop);
                    stopBtnpressed.setBounds(0, 0, stopBtnpressed.getMinimumWidth(), stopBtnpressed.getMinimumHeight());
                    stopBtn.setCompoundDrawables(null, stopBtnpressed, null, null);
                    senddata[0]=0x4B;
                    senddata[1]=0x53;
                    senddata[2]=0x03;
                    senddata[3]=0x00;
                    senddata[4]=0x02;
                    senddata[5]=0x3A;
                    senddata[6]=0x3B;
                    send(bytesToHexString(senddata));

                    for(int i=0;i<overReceive.length;i++)
                    {
                        overReceive[i]=0;
                    }

                    handler.removeCallbacks (ReceiveRunnable);
                    handler.post(ShowRunnable);
                    // handler.postDelayed(resultRunnable, 10);
                }
                break;
                case R.id.backAccMeaBtn: {
                    Intent intent1 = new Intent(MeasureActivity.this, MenuActivity.class);
                    startActivity(intent1);
                    finish();
                }
                break;
                default: {
                }
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
    Handler myHandler=new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            int index=(int)msg.obj;

            if(index>5){
                mainLayout.setVisibility(View.VISIBLE);
                count_txt.setVisibility(View.GONE);
            }else{
                count_txt.setImageResource(arrayId[index]);
                count_txt.setScaleX(0);
                count_txt.setScaleY(0);
            }
            String str_scaleX;
            String str_scaleY;
            str_scaleX="scaleX";
            str_scaleY="scaleY";
            //设置X方向上的缩放动画
            ObjectAnimator oa1=ObjectAnimator.ofFloat(count_txt,str_scaleX,0,1);
            oa1.setDuration(500);

            //设置Y方向上的缩放动画
            ObjectAnimator oa2=ObjectAnimator.ofFloat(count_txt,str_scaleY,0,1);
            oa2.setDuration(500);

            AnimatorSet set=new AnimatorSet();
            set.playTogether(oa1,oa2);
            set.start();
        }
    };

    public void createAnimationThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (int i = 1; i <= 6; i++) {
                    Message message = myHandler.obtainMessage();
                    message.obj = i;
                    myHandler.sendMessage(message);
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }


    private void showRatioDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MeasureActivity.this);
        //normalDialog.setIcon(R.drawable..);
        normalDialog.setTitle("提示！");
        normalDialog.setMessage("       请确保测量模块放置在水平面上！");
        normalDialog.setPositiveButton("确  定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.removeCallbacks (FirstRunnable);
                        handler.postDelayed(ReceiveRunnable,10);
                        Toast.makeText(MeasureActivity.this, "初始定位设置完毕", Toast.LENGTH_SHORT).show();
                    }
                });
        normalDialog.show();
    }

    Runnable FirstRunnable = new Runnable() {
        @Override
        public void run() {
            int ReceiveLength=49;
            int ReceiveNum=0;
            byte[] mReceive=new byte[ReceiveLength];
            String[] str_ReceiveData=new String[ReceiveLength];
            String[] str_ReceiveData1=new String[ReceiveLength];
            if(overReceive.length>=ReceiveLength)
            {
                for (int i = 0; i < ReceiveLength; i++)
                {
                    str_ReceiveData1[i] = myformat.format(overReceive[i]);
                }

                Log.d("RawDataFirst", "     " + str_ReceiveData1[0] + "  " + str_ReceiveData1[1] + " " + str_ReceiveData1[2] + "  " + str_ReceiveData1[3] + " " + str_ReceiveData1[4]
                        + "  " + str_ReceiveData1[5] + " " + str_ReceiveData1[6] + "  " + str_ReceiveData1[7] + " " + str_ReceiveData1[8] + "  " + str_ReceiveData1[9] + " " + str_ReceiveData1[10]
                        + "  " + str_ReceiveData1[11] + " " + str_ReceiveData1[12] + "  " + str_ReceiveData1[13] + " " + str_ReceiveData1[14] + str_ReceiveData1[15] + " " + str_ReceiveData1[16]
                        + "  "+ str_ReceiveData1[17] + "  " + str_ReceiveData1[18] + " " + str_ReceiveData1[19] + "  " + str_ReceiveData1[20] + " " + str_ReceiveData1[21] + "  " + str_ReceiveData1[22]
                        + " " + str_ReceiveData1[6] + "  " + str_ReceiveData1[7] + " " + str_ReceiveData1[8] + "  " + str_ReceiveData1[9] + " " + str_ReceiveData1[10] + "  " + str_ReceiveData1[11]
                        + " " + str_ReceiveData1[23] + "  " + str_ReceiveData1[24] + " " + str_ReceiveData1[25] + str_ReceiveData1[26] + " " + str_ReceiveData1[27] + "  "+ str_ReceiveData1[28]
                        + "  " + str_ReceiveData1[29] + " " + str_ReceiveData1[30] + "  " + str_ReceiveData1[31] + " " + str_ReceiveData1[32] + "  " + str_ReceiveData1[33] + " " + str_ReceiveData1[34]
                        + "  " + str_ReceiveData1[35] + " " + str_ReceiveData1[36] + "  " + str_ReceiveData1[37] + " " + str_ReceiveData1[38] + "  " + str_ReceiveData1[39] + " " +
                        str_ReceiveData1[40] + "  " + str_ReceiveData1[41] + " " + str_ReceiveData1[42] + str_ReceiveData1[43] + " " + str_ReceiveData1[44] + "  "+ "  "
                        + str_ReceiveData1[45] + " " + str_ReceiveData1[46] + str_ReceiveData1[47] + " " + str_ReceiveData1[48] );


                for(int i=0;i<overReceive.length;i++)
                {
                    if(overReceive[i]==0x4B)
                    {
                        break;
                    }else{
                        ReceiveNum++;
                    }
                }
                for(int i=0;i<ReceiveLength;i++)
                {
                    if((ReceiveNum+i)<overReceive.length)
                    {
                        //Log.d("First","ReceiveNum+i="+myformat.format(ReceiveNum+i));
                        mReceive[i]=overReceive[ReceiveNum+i];
                    }else{
                        mReceive[i]=overReceive[ReceiveNum+i-overReceive.length];
                    }
                }

                for (int i = 0; i < ReceiveLength; i++) {
                    str_ReceiveData[i] = myformat.format(mReceive[i]);
                }

                Log.d("MeasureActivityFirst", "     " + str_ReceiveData[0] + "  " + str_ReceiveData[1] + " " + str_ReceiveData[2] + "  " + str_ReceiveData[3] + " " + str_ReceiveData[4]
                        + "  " + str_ReceiveData[5] + " " + str_ReceiveData[6] + "  " + str_ReceiveData[7] + " " + str_ReceiveData[8] + "  " + str_ReceiveData[9] + " " + str_ReceiveData[10]
                        + "  " + str_ReceiveData[11] + " " + str_ReceiveData[12] + "  " + str_ReceiveData[13] + " " + str_ReceiveData[14] + str_ReceiveData[15] + " " + str_ReceiveData[16]
                        + "  "+ str_ReceiveData[17] + "  " + str_ReceiveData[18] + " " + str_ReceiveData[19] + "  " + str_ReceiveData[20] + " " + str_ReceiveData[21] + "  " + str_ReceiveData[22]
                        + " " + str_ReceiveData[6] + "  " + str_ReceiveData[7] + " " + str_ReceiveData[8] + "  " + str_ReceiveData[9] + " " + str_ReceiveData[10] + "  " + str_ReceiveData[11]
                        + " " + str_ReceiveData[23] + "  " + str_ReceiveData[24] + " " + str_ReceiveData[25] + str_ReceiveData[26] + " " + str_ReceiveData[27] + "  "+ str_ReceiveData[28]
                        + "  " + str_ReceiveData[29] + " " + str_ReceiveData[30] + "  " + str_ReceiveData[31] + " " + str_ReceiveData[32] + "  " + str_ReceiveData[33] + " " + str_ReceiveData[34]
                        + "  " + str_ReceiveData[35] + " " + str_ReceiveData[36] + "  " + str_ReceiveData[37] + " " + str_ReceiveData[38] + "  " + str_ReceiveData[39] + " "
                        + "  "+ str_ReceiveData[40] + "  " + str_ReceiveData[41] + " " + str_ReceiveData[42] + str_ReceiveData[43] + " " + str_ReceiveData[44] + "  "+ "  "
                        + "  " + str_ReceiveData[45] + " " + str_ReceiveData[46] + " "+ str_ReceiveData[47] + " " + str_ReceiveData[48] );

                if((mReceive[0]==0x4B) && (mReceive[1]==0x53)) {
                    switch (mReceive[2]) {
                        case 0x03:
                            for(int i=0;i<7;i++)
                            {
                                int tmp = 0;
                                tmp = (char) (mReceive[4+i*6] & 0xFF) * 256 + (char) (mReceive[3+i*6] & 0xFF);
                                tmp = tmp & 0xFFFF;
                                if (tmp <= 0x7FFF) xtempreceive = (float) tmp/ 1000.0f;
                                else xtempreceive = (float) (0x10000 - tmp) / 1000.0f * (-1.0f);

                                tmp = (char) (mReceive[6+i*6] & 0xFF) * 256 + (char) (mReceive[5+i*6] & 0xFF);
                                tmp = tmp & 0xFFFF;
                                if (tmp <= 0x7FFF) ytempreceive = (float) tmp/ 1000.0f;
                                else ytempreceive = (float) (0x10000 - tmp)/ 1000.0f * (-1.0f);

                                tmp = (char) (mReceive[8+i*6] & 0xFF) * 256 + (char) (mReceive[7+i*6] & 0xFF);
                                tmp = tmp & 0xFFFF;
                                if (tmp <= 0x7FFF) ztempreceive = (float) tmp / 1000.0f;
                                else ztempreceive = (float) (0x10000 - tmp) / 1000.0f * (-1.0f);

                                if(((ztempreceive)<16.0f) && ((ztempreceive)>-16.0f))
                                {
                                    FirstX=xtempreceive*9.8f;
                                    FirstY=ytempreceive*9.8f;
                                    FirstZ=ztempreceive*9.8f;
                                }
                            }
                            break;
                        default : break;
                    }
                }
                if(FirstZ>9.0f && FirstZ<12.0f){
                    handler.removeCallbacks (FirstRunnable);
                    handler.postDelayed(ReceiveRunnable,10);
                    Toast.makeText(MeasureActivity.this, "初始定位设置完毕", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("First","FirstX="+myformat.format(FirstX)+" "+"FirstY="+myformat.format(FirstY)+"  "+"FirstZ="+myformat.format(FirstZ));
                    handler.postDelayed(FirstRunnable,100);
                }
            }
        }
    };

    private Runnable ReceiveRunnable = new Runnable() {
        @Override
        public void run() {
            int ReceiveLength=17;
            int ReceiveNum=0;
            byte[] mReceive=new byte[ReceiveLength];
            String[] str_ReceiveData=new String[ReceiveLength];
            String[] str_ReceiveData1=new String[ReceiveLength];
            if(overReceive.length>=ReceiveLength)
            {
                for (int i = 0; i < ReceiveLength; i++)
                {
                    str_ReceiveData1[i] = myformat.format(overReceive[i]);
                }

                Log.d("RawData", "     " + str_ReceiveData1[0] + "  " + str_ReceiveData1[1] + " " + str_ReceiveData1[2] + "  " + str_ReceiveData1[3] + " " + str_ReceiveData1[4]
                        + "  " + str_ReceiveData1[5] + " " + str_ReceiveData1[6] + "  " + str_ReceiveData1[7] + " " + str_ReceiveData1[8] + "  " + str_ReceiveData1[9] + " " + str_ReceiveData1[10]
                        + "  " + str_ReceiveData1[11] + " " + str_ReceiveData1[12] + "  " + str_ReceiveData1[13] + " " + str_ReceiveData1[14] + str_ReceiveData1[15] + " " + str_ReceiveData1[16] );
                
                for(int i=0;i<overReceive.length;i++)
                {
                    if(overReceive[i]==0x4B)
                    {
                        break;
                    }else{
                        ReceiveNum++;
                    }
                }
                for(int i=0;i<ReceiveLength;i++)
                {
                    if((ReceiveNum+i)<overReceive.length)
                    {
                        mReceive[i]=overReceive[ReceiveNum+i];
                    }else{
                        mReceive[i]=overReceive[ReceiveNum+i-overReceive.length];
                    }
                }

                for (int i = 0; i < ReceiveLength; i++) {
                    str_ReceiveData[i] = myformat.format(mReceive[i]);
                }

                Log.d("MeasureActivity", "     " + str_ReceiveData[0] + "  " + str_ReceiveData[1] + " " + str_ReceiveData[2] + "  " + str_ReceiveData[3] + " " + str_ReceiveData[4]
                        + "  " + str_ReceiveData[5] + " " + str_ReceiveData[6] + "  " + str_ReceiveData[7] + " " + str_ReceiveData[8] + "  " + str_ReceiveData[9] + " " + str_ReceiveData[10]
                        + "  " + str_ReceiveData[11] + " " + str_ReceiveData[12] + "  " + str_ReceiveData[13] + " " + str_ReceiveData[14] + str_ReceiveData[15] + " " + str_ReceiveData[16]);
                Log.d("MeasureActivity","FirstX="+myformat.format(FirstX)+" "+"FirstY="+myformat.format(FirstY)+"  "+"FirstZ="+myformat.format(FirstZ));
                if((mReceive[0]==0x4B) && (mReceive[1]==0x53)) {
                    switch (mReceive[2]) {
                        case 0x03: {
                            int tmp = 0;
                                tmp = (char) (mReceive[4] & 0xFF) * 256 + (char) (mReceive[3] & 0xFF);
                                tmp = tmp & 0xFFFF;
                                if (tmp <= 0x7FFF) xtempreceive = (float) tmp / 1000.0f;
                                else xtempreceive = (float) (0x10000 - tmp) / 1000.0f * (-1.0f);

                                tmp = (char) (mReceive[6] & 0xFF) * 256 + (char) (mReceive[5] & 0xFF);
                                tmp = tmp & 0xFFFF;
                                if (tmp <= 0x7FFF) ytempreceive = (float) tmp / 1000.0f;
                                else ytempreceive = (float) (0x10000 - tmp) / 1000.0f * (-1.0f);

                                tmp = (char) (mReceive[8 ] & 0xFF) * 256 + (char) (mReceive[7] & 0xFF);
                                tmp = tmp & 0xFFFF;
                                if (tmp <= 0x7FFF) ztempreceive = (float) tmp / 1000.0f;
                                else ztempreceive = (float) (0x10000 - tmp) / 1000.0f * (-1.0f);

                                if((Math.abs(xtempreceive)>0.0f)&&(Math.abs(ytempreceive)>0.0f)&&(Math.abs(ztempreceive)>0.0f))
                                {
                                    xtemp = (xtempreceive * 9.8f - FirstX);
                                    ytemp = (ytempreceive * 9.8f- FirstY);
                                    ztemp = (ztempreceive * 9.8f - FirstZ);
                                }
                                Log.d("MeasureActivity", "xtemp=" + myformat.format(xtemp) + " " + "ytemp=" + myformat.format(ytemp) + "  " + "ztemp=" + myformat.format(ztemp));
                                if (Math.abs(ztemp) < (3.0f))
                                {
                                    //xyz数据m/s^2
                                    xx.add(xtemp);
                                    yy.add(ytemp);
                                    zz.add(ztemp);

                                    if (ChartYMaxX < Math.abs(xtemp)) {
                                        ChartYMaxX = Math.abs(xtemp);
                                    }
                                    if (ChartYMaxY < Math.abs(ytemp)) {
                                        ChartYMaxY = Math.abs(ytemp);
                                    }
                                    if (ChartYMaxZ < Math.abs(ztemp)) {
                                        ChartYMaxZ = Math.abs(ztemp);
                                    }

                                    str_Xreal=myformat.format(xtemp);
                                    str_XMax=myformat.format(ChartYMaxX);
                                    str_Yreal=myformat.format(ytemp);
                                    str_YMax=myformat.format(ChartYMaxY);
                                    str_Zreal=myformat.format(ztemp);
                                    str_ZMax=myformat.format(ChartYMaxZ);

                                    Xreal_txt.setText(str_Xreal);
                                    XMaxMea_txt.setText(str_XMax);
                                    Yreal_txt.setText(str_Yreal);
                                    YMaxMea_txt.setText(str_YMax);
                                    Zreal_txt.setText(str_Zreal);
                                    ZMaxMea_txt.setText(str_ZMax);
                                }
                        }
                        break;
                        default : break;
                    }
                }
                ChartXMax = ChartXMax + 0.0183f;
                if (ChartXMax > 100000) {
                    ChartXMax = 0;
                }
                //str_ZMax=myformat.format(ChartXMax);
                //ZMaxMea_txt.setText(str_ZMax);
                handler.postDelayed(ReceiveRunnable,10);
            }
        }
    };
    private Runnable ShowRunnable = new Runnable() {
        @Override
        public void run() {
            int x_length=xx.size();
            int y_length=yy.size();
            int z_length=zz.size();

            dynamicLineChartManager_x.clear();
            dynamicLineChartManager_y.clear();
            dynamicLineChartManager_z.clear();
            dynamicLineChartManager_x.setData(x_length,xx,0.0183f,1.0f);
            dynamicLineChartManager_x.setYAxis((ChartYMaxX*1.2f), (ChartYMaxX*1.2f*(-1.0f)), 4);
            dynamicLineChartManager_x.setXAxis((ChartXMax*1.2f), 0, 10,0);
            dynamicLineChartManager_x.desChart(names.get (0),40);

            dynamicLineChartManager_y.setData( y_length,yy,0.0183f,1.0f);
            dynamicLineChartManager_y.setYAxis((ChartYMaxY*1.2f), (ChartYMaxY*1.2f*(-1.0f)), 4);
            dynamicLineChartManager_y.setXAxis((ChartXMax*1.2f), 0, 10,0);
            dynamicLineChartManager_y.desChart(names.get (1),40);

            dynamicLineChartManager_z.setData(z_length,zz,0.0183f,1.0f);
            dynamicLineChartManager_z.setYAxis((ChartYMaxZ*1.2f), (ChartYMaxZ*1.2f*(-1.0f)), 4);
            dynamicLineChartManager_z.setXAxis((ChartXMax*1.2f), 0, 10,0);
            dynamicLineChartManager_z.desChart(names.get (2),40);
        }
    };

    public class DynamicLineChartManager implements OnChartGestureListener {
        private LineChart lineChart;
        private YAxis leftAxis;
        private YAxis rightAxis;
        private XAxis xAxis;
        private LineData lineData;
        private LineDataSet lineDataSet;
        private LineChartMarkView mv;
        private Legend legend;
        private int position;

        private void setData(int count, ArrayList<Float> value,float indexX,float indexY) {
            ArrayList<Entry> values = new ArrayList<>();
            // lineDataSet.clear();
            //lineChart.clear();
            for (int i = 0; i < count - 1; i++) {
                values.add(new Entry((float)(i*indexX), (float) (value.get(i)*indexY)));
            }
            lineDataSet.setValues(values);
            LineData data = new LineData(lineDataSet);
            lineChart.setData(data);
            lineChart.invalidate();
        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        }

        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {
            xchart.setVisibility(View.VISIBLE);
            ychart.setVisibility(View.VISIBLE);
            zchart.setVisibility(View.VISIBLE);
        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {
        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {
        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            xchart.setVisibility(View.VISIBLE);
            ychart.setVisibility(View.VISIBLE);
            zchart.setVisibility(View.VISIBLE);
        }
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        }

        //一条曲线
        public DynamicLineChartManager(LineChart mLineChart, String name, int color, int position) {
            this.lineChart = mLineChart;
            this.position = position;
            //滑动缩放相关
            lineChart.setOnChartGestureListener(this);
            lineChart.setTouchEnabled(true); // 设置是否可以触摸
            lineChart.setDragEnabled(true);// 是否可以拖拽
            lineChart.setScaleEnabled(true);// 是否可以缩放 x和y轴, 默认是true
            lineChart.setDoubleTapToZoomEnabled(true);//设置是否可以通过双击屏幕放大图表。默认是true
            //lineChart.setHighlightEnabled(false);  //If set to true, highlighting/selecting values via touch is possible for all underlying DataSets.
            lineChart.setHighlightPerDragEnabled(true);//能否拖拽高亮线(数据点与坐标的提示线)，默认是true

            //数据样式
            lineDataSet = new LineDataSet(null, "");
            lineDataSet.setLineWidth(0.5f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setColor(Color.GREEN);
            lineDataSet.setHighLightColor(Color.WHITE);
            //设置曲线填充
            lineDataSet.setDrawFilled(true);
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setDrawValues(false);
            lineDataSet.setHighlightEnabled(true);
            lineDataSet.setMode(LineDataSet.Mode.LINEAR);
            lineData = new LineData(lineDataSet);
            lineChart.setData(lineData);
            lineChart.animateX(10);
            lineChart.invalidate();
        }
        private void desChart(String name, int y) {
            //图标样式
            com.github.mikephil.charting.components.Description description = new Description();
            description.setText(name);
            description.setTextColor(Color.WHITE);
            description.setPosition(650,y);
            lineChart.setDescription(description);
            lineChart.setDrawGridBackground(false);
            lineChart.setDrawBorders(false);
            lineChart.invalidate();
        }
        private void addEntry(float speed){
            Entry entry = new Entry(lineDataSet.getEntryCount()*0.1f,speed);
            lineData.addEntry(entry, 0);
            xchart.notifyDataSetChanged();
            xchart.moveViewToX(0.00f);
            ychart.notifyDataSetChanged();
            ychart.moveViewToX(0.00f);
            zchart.notifyDataSetChanged();
            zchart.moveViewToX(0.00f);
            lineChart.invalidate();
        }

        private void freshChart() {
            lineChart.fitScreen();
        }
        private void clear() {
            lineDataSet.clear();
            lineChart.invalidate();
        }

        private void addPoint(float x,float y) {

            LineData data = lineChart.getData();
            if (data == null) {
                lineChart.setData(new LineData());
            } else {
                int count = (data.getDataSetCount() + 1);
                ArrayList<Entry> values = new ArrayList<>();
                //values.add(new Entry(x, y, ContextCompat.getDrawable(context,R.drawable.cross)));
                LineDataSet set = new LineDataSet(values, "DataSet " + count);
                set.setLineWidth(0.5f);

                set.setDrawIcons (true);
                // set.setFillDrawable (getDrawable (R.drawable.add));
                //  set.setCircleRadius(2.0f);
                int color =Color.argb (255, 220, 20, 60);
                set.setColor(color);
                set.setCircleColor(color);
                set.setHighLightColor(color);
                set.setFillColor (color);
                data.addDataSet(set);
                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }
        }

        public void setYAxis(float max, float min, int labelCount) {
            if (max < min) {
                return;
            }
            leftAxis = lineChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTextColor(Color.WHITE);
            leftAxis.setGranularityEnabled(false);
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMaximum(max);
            leftAxis.setAxisMinimum(min);
            leftAxis.setDrawLimitLinesBehindData(true);
            leftAxis.setLabelCount(labelCount, false);
            rightAxis = lineChart.getAxisRight();
            rightAxis.setEnabled(false);
            //图例
            legend = lineChart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextSize(10f);
            legend.setDrawInside(true);
            legend.setTextColor(Color.WHITE);// 颜色
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            mv = new LineChartMarkView(lineChart.getContext(), leftAxis.getValueFormatter());
            mv.setChartView(lineChart);
            lineChart.setMarker(mv);
            lineChart.invalidate();
        }

        public void setXAxis(float max, float min, int labelCount,int pos) {
            if (max < min) {
                return;
            }
            xAxis = lineChart.getXAxis();
            if(pos==0){
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            }else{
                xAxis.setPosition(XAxis.XAxisPosition.TOP);
            }
            xAxis.setDrawLabels(true);
            xAxis.setDrawGridLines(false);
            xAxis.setAxisMaximum(max);
            xAxis.setAxisMinimum(min);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setLabelCount(labelCount, false);
            lineChart.invalidate();
        }

        /**
         * 设置高限制线
         *
         * @param high
         * @param name
         */
        public void setHightLimitLine(float high, String name) {
            if (name == null) {
                name = "高限制线";
            }
            LimitLine hightLimit = new LimitLine(high, name);
            hightLimit.setLineWidth(0.1f);
            hightLimit.setTextSize(10f);
            hightLimit.enableDashedLine(8.0f, 4.0f, 4.0f);
            leftAxis.removeAllLimitLines(); //先清除原来的线，后面再加上，防止add方法重复绘制
            leftAxis.addLimitLine(hightLimit);
            hightLimit.setLineColor(Color.WHITE);
            lineChart.invalidate();
        }


        public void setLowLimitLine(float low, String name) {
            if (name == null) {
                name = "低限制线";
            }
            LimitLine hightLimit = new LimitLine(low, name);
            hightLimit.setLineWidth(0.1f);
            hightLimit.setTextSize(10f);
            hightLimit.setLineColor(Color.WHITE);
            hightLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            leftAxis.removeAllLimitLines(); //先清除原来的线，后面再加上，防止add方法重复绘制
            leftAxis.addLimitLine(hightLimit);
            hightLimit.enableDashedLine(8.0f, 4.0f, 4.0f);
            lineChart.invalidate();
        }

        public void setXHightLimitLine(float high, String name) {
            LimitLine hightLimit = new LimitLine(high, name);
            hightLimit.setLineWidth(0.1f);
            hightLimit.setTextSize(10f);
            hightLimit.enableDashedLine(8.0f, 4.0f, 4.0f);

            xAxis.addLimitLine(hightLimit);
            hightLimit.setLineColor(Color.WHITE);
            lineChart.invalidate();
        }

        public void ClearXHightLimitLine(float high, String name) {
            LimitLine hightLimit = new LimitLine(high, name);
            hightLimit.setLineWidth(0.1f);
            hightLimit.setTextSize(10f);
            hightLimit.enableDashedLine(8.0f, 4.0f, 4.0f);
            xAxis.addLimitLine(hightLimit);
            xAxis.removeAllLimitLines(); //先清除原来的线，后面再加上，防止add方法重复绘制
            hightLimit.setLineColor(Color.WHITE);
            lineChart.invalidate();
        }
    }
    public void ShowWave() {
        dynamicLineChartManager_x.setData(xx.size(),xx,0.0183f,1.0f);
        dynamicLineChartManager_x.setYAxis(10.0f, -10.0f, 4);
        dynamicLineChartManager_x.setXAxis(60, 0, 10,0);
        dynamicLineChartManager_x.setHightLimitLine(0f, "");
        dynamicLineChartManager_x.desChart(names.get (0),40);

        dynamicLineChartManager_y.setData( yy.size(),yy,0.0183f,1.0f);
        dynamicLineChartManager_y.setYAxis(10.0f, -10.0f, 4);
        dynamicLineChartManager_y.setXAxis(60, 0, 10,0);
        dynamicLineChartManager_y.setHightLimitLine(0f, "");
        dynamicLineChartManager_y.desChart(names.get (1),40);

        dynamicLineChartManager_z.setData(zz.size(),zz,0.0183f,1.0f);
        dynamicLineChartManager_z.setYAxis(10.0f, -10.0f, 4);
        dynamicLineChartManager_z.setXAxis(60, 0, 10,0);
        dynamicLineChartManager_z.setHightLimitLine(0f, "");
        dynamicLineChartManager_z.desChart(names.get (2),40);
    }
    @Override
    protected void onResume() {
        super.onResume();
        final String METHODTAG = ".onResume";
    }
    @Override
    protected void onDestroy() {
        ActivityCollector.removeActivity(this);
        super.onDestroy();
    }
}
