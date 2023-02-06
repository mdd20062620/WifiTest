package com.ruiguan.activities;
import static com.ruiguan.activities.MenuActivity.input_data;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ruiguan.R;
import com.ruiguan.activities.ActivityCollector;
import com.ruiguan.activities.DB;
import com.ruiguan.activities.MainActivity;
import com.ruiguan.base.BaseActivity;
import com.ruiguan.printer.PrintDataService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class SaveActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private Context context;
    private DB saveDB;
    private Cursor mCursor;
    private Button delCurBtn;
    private Button delAllBtn;
    private Button printCurBtn;
    private Button printAllBtn;
    private Button exportCurBtn;
    private Button exportAllBtn;
    private Button backBtn;
    private Button exitBtn;

    private Drawable delCurBtnpressed;
    private Drawable delAllBtnpressed;
    private Drawable printCurBtnpressed;
    private Drawable printAllBtnpressed;
    private Drawable exportCurBtnpressed;
    private Drawable exportAllBtnpressed;

    private String str_AccUnit;
    private String str_SpeedUnit;
    private String str_SampleUnit;

    private String str_company;
    private String str_number;
    private String str_ratedSpeed;
    private String str_date;
    private String str_SpeedMax;
    private String str_disMax;
    private String str_AMax;
    private String str_AMin;
    private String str_AAMax;
    private String str_AAPk;
    private String str_V95;
    private String str_avgSpeed;
    private String str_A95;
    private String str_D95;

    private String str_xPkMax;
    private String str_xA95;
    private String str_yPkMax;
    private String str_yA95;
    private String str_zPkMax;
    private String str_zA95;
    private String str_SoundMax;

    private String str_SoundMaxRes="";
    private String str_disRes="合格";
    private String str_SpeedMaxRes="";
    private String str_ASpeedMaxRes="";
    private String str_DSpeedMaxRes="";
    private String str_AASpeedMaxRes="合格";
    private String str_PkSpeedMaxRes="合格";
    private String str_AvgSpeedRes="";
    private String str_V95Res="";
    private String str_A95Res="";
    private String str_D95Res="";
    private String str_xPkMaxRes="";
    private String str_xA95Res="";
    private String str_yPkMaxRes="";
    private String str_yA95Res="";
    private String str_zPkMaxRes="";
    private String str_zA95Res="";

    private ListView savelistView;
    private int data_ID = 0,ID = 0;
    private Handler handler = new Handler();
    java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.000");
    private boolean isPrinterReady = false;
    private PrintDataService printDataService = null;
    private BluetoothDevice printDevice= null;
    private boolean PrintConnect = false;
    private String deviceAddress = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ActivityCollector.addActivity(this);
        context = getApplicationContext();

        SharedPreferences shares1 = getSharedPreferences( "unitValue", Activity.MODE_PRIVATE );
        if(!shares1.getBoolean("unitValueDecive",false))
        {
            // cabliBalanceCoeff=1.0f;
            Toast.makeText(SaveActivity.this, "未输入参数！", Toast.LENGTH_SHORT).show();
        }else {
            str_AccUnit=shares1.getString("AccUnit","");
            str_SpeedUnit=shares1.getString("SpeedUnit","");
            str_SampleUnit=shares1.getString("SampleUnit","");
        }
        printCurBtn = (Button) findViewById(R.id.printCurAccBtn);
        printAllBtn = (Button) findViewById(R.id.printAllAccBtn);
        delCurBtn = (Button) findViewById(R.id.delCurAccBtn);
        delAllBtn= (Button) findViewById(R.id.delAllAccBtn);
        exportCurBtn= (Button) findViewById(R.id.exportCurAccBtn);
        exportAllBtn= (Button) findViewById(R.id.exportAllAccBtn);
        backBtn= (Button) findViewById(R.id.backAccSaveBtn);
        exitBtn= (Button) findViewById(R.id.exitAccSaveBtn);
        View.OnClickListener bl = new SaveActivity.ButtonListener();
        setOnClickListener(printCurBtn, bl);
        setOnClickListener(printAllBtn, bl);
        setOnClickListener(delCurBtn, bl);
        setOnClickListener(delAllBtn, bl);
        setOnClickListener(exportCurBtn, bl);
        setOnClickListener(exportAllBtn, bl);
        setOnClickListener(backBtn, bl);
        setOnClickListener(exitBtn, bl);
        setUpViews();
        savelistView.setOnItemClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        final String METHODTAG = ".onResume";
    }
    //销毁后调用
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveDB.close();
        mCursor.close();
        ActivityCollector.removeActivity(this);
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
                case R.id.printCurAccBtn: {
                    printCurBtn.setEnabled(false);
                    printCurBtnpressed = getResources().getDrawable(R.drawable.printcur);
                    printCurBtnpressed.setBounds(0, 0, printCurBtnpressed.getMinimumWidth(), printCurBtnpressed.getMinimumHeight());
                    printCurBtn.setCompoundDrawables(null, printCurBtnpressed, null, null);
                    if (printDataService == null) {           //首次连接打印机
                        SharedPreferences shares = getSharedPreferences("BLE_Info", Activity.MODE_PRIVATE);
                        if (!shares.getBoolean("BondPrinter", false)) {
                            Toast.makeText(SaveActivity.this, "未找到配对打印机！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SaveActivity.this.getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        handler.postDelayed(PrinterRunnable, 100);          //启动监听蓝牙设备进程
                    } else {          //打印数据
                        printDataService.putDevice(printDevice);
                        if(mCursor.moveToFirst()){
                            ID = mCursor.getInt(0);
                            if (ID == data_ID) {
                                str_date= mCursor.getString(1);
                                str_company= mCursor.getString(2);
                                str_number= mCursor.getString(3);
                                str_ratedSpeed= mCursor.getString(4);
                                str_disMax= mCursor.getString(5);
                                str_SpeedMax= mCursor.getString(6);
                                str_AMax= mCursor.getString(7);
                                str_AMin= mCursor.getString(8);
                                str_AAMax= mCursor.getString(9);
                                str_AAPk= mCursor.getString(10);
                                str_V95= mCursor.getString(11);
                                str_avgSpeed= mCursor.getString(12);
                                str_A95= mCursor.getString(13);
                                str_D95= mCursor.getString(14);
                                str_xPkMax= mCursor.getString(15);
                                str_xA95= mCursor.getString(16);
                                str_yPkMax= mCursor.getString(17);
                                str_yA95= mCursor.getString(18);
                                str_zPkMax= mCursor.getString(19);
                                str_zA95= mCursor.getString(20);
                                str_SoundMax= mCursor.getString(21);

                                str_SoundMaxRes= mCursor.getString(22);
                                str_disRes= mCursor.getString(23);
                                str_SpeedMaxRes= mCursor.getString(24);
                                str_ASpeedMaxRes= mCursor.getString(25);
                                str_DSpeedMaxRes= mCursor.getString(26);
                                str_AASpeedMaxRes= mCursor.getString(27);
                                str_PkSpeedMaxRes= mCursor.getString(28);
                                str_AvgSpeedRes= mCursor.getString(29);
                                str_V95Res= mCursor.getString(30);
                                str_A95Res= mCursor.getString(31);
                                str_D95Res= mCursor.getString(32);
                                str_xPkMaxRes= mCursor.getString(33);
                                str_xA95Res= mCursor.getString(34);
                                str_yPkMaxRes= mCursor.getString(35);
                                str_yA95Res= mCursor.getString(36);
                                str_zPkMaxRes= mCursor.getString(37);
                                str_zA95Res= mCursor.getString(38);
                                PrintMeasureData();
                                break;
                            }
                            while(mCursor.moveToNext()){//遍历数据表中的数据
                                ID = mCursor.getInt(0);
                                if (ID == data_ID) {
                                    str_date= mCursor.getString(1);
                                    str_company= mCursor.getString(2);
                                    str_number= mCursor.getString(3);
                                    str_ratedSpeed= mCursor.getString(4);
                                    str_disMax= mCursor.getString(5);
                                    str_SpeedMax= mCursor.getString(6);
                                    str_AMax= mCursor.getString(7);
                                    str_AMin= mCursor.getString(8);
                                    str_AAMax= mCursor.getString(9);
                                    str_AAPk= mCursor.getString(10);
                                    str_V95= mCursor.getString(11);
                                    str_avgSpeed= mCursor.getString(12);
                                    str_A95= mCursor.getString(13);
                                    str_D95= mCursor.getString(14);
                                    str_xPkMax= mCursor.getString(15);
                                    str_xA95= mCursor.getString(16);
                                    str_yPkMax= mCursor.getString(17);
                                    str_yA95= mCursor.getString(18);
                                    str_zPkMax= mCursor.getString(19);
                                    str_zA95= mCursor.getString(20);
                                    str_SoundMax= mCursor.getString(21);

                                    str_SoundMaxRes= mCursor.getString(22);
                                    str_disRes= mCursor.getString(23);
                                    str_SpeedMaxRes= mCursor.getString(24);
                                    str_ASpeedMaxRes= mCursor.getString(25);
                                    str_DSpeedMaxRes= mCursor.getString(26);
                                    str_AASpeedMaxRes= mCursor.getString(27);
                                    str_PkSpeedMaxRes= mCursor.getString(28);
                                    str_AvgSpeedRes= mCursor.getString(29);
                                    str_V95Res= mCursor.getString(30);
                                    str_A95Res= mCursor.getString(31);
                                    str_D95Res= mCursor.getString(32);
                                    str_xPkMaxRes= mCursor.getString(33);
                                    str_xA95Res= mCursor.getString(34);
                                    str_yPkMaxRes= mCursor.getString(35);
                                    str_yA95Res= mCursor.getString(36);
                                    str_zPkMaxRes= mCursor.getString(37);
                                    str_zA95Res= mCursor.getString(38);
                                    PrintMeasureData();
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
                case R.id.printAllAccBtn: {
                    printAllBtn.setEnabled(false);
                    printAllBtnpressed = getResources().getDrawable(R.drawable.printall);
                    printAllBtnpressed.setBounds(0, 0, printAllBtnpressed.getMinimumWidth(), printAllBtnpressed.getMinimumHeight());
                    printAllBtn.setCompoundDrawables(null, printAllBtnpressed, null, null);
                    if (printDataService == null) {           //首次连接打印机
                        SharedPreferences shares = getSharedPreferences("BLE_Info", Activity.MODE_PRIVATE);
                        if (!shares.getBoolean("BondPrinter", false)) {
                            Toast.makeText(SaveActivity.this, "未找到配对打印机！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SaveActivity.this.getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        handler.postDelayed(PrinterRunnable, 100);          //启动监听蓝牙设备进程
                    } else {          //打印数据
                        printDataService.putDevice(printDevice);
                        if(mCursor.moveToFirst()){
                            str_date= mCursor.getString(1);
                            str_company= mCursor.getString(2);
                            str_number= mCursor.getString(3);
                            str_ratedSpeed= mCursor.getString(4);
                            str_disMax= mCursor.getString(5);
                            str_SpeedMax= mCursor.getString(6);
                            str_AMax= mCursor.getString(7);
                            str_AMin= mCursor.getString(8);
                            str_AAMax= mCursor.getString(9);
                            str_AAPk= mCursor.getString(10);
                            str_V95= mCursor.getString(11);
                            str_avgSpeed= mCursor.getString(12);
                            str_A95= mCursor.getString(13);
                            str_D95= mCursor.getString(14);
                            str_xPkMax= mCursor.getString(15);
                            str_xA95= mCursor.getString(16);
                            str_yPkMax= mCursor.getString(17);
                            str_yA95= mCursor.getString(18);
                            str_zPkMax= mCursor.getString(19);
                            str_zA95= mCursor.getString(20);
                            str_SoundMax= mCursor.getString(21);

                            str_SoundMaxRes= mCursor.getString(22);
                            str_disRes= mCursor.getString(23);
                            str_SpeedMaxRes= mCursor.getString(24);
                            str_ASpeedMaxRes= mCursor.getString(25);
                            str_DSpeedMaxRes= mCursor.getString(26);
                            str_AASpeedMaxRes= mCursor.getString(27);
                            str_PkSpeedMaxRes= mCursor.getString(28);
                            str_AvgSpeedRes= mCursor.getString(29);
                            str_V95Res= mCursor.getString(30);
                            str_A95Res= mCursor.getString(31);
                            str_D95Res= mCursor.getString(32);
                            str_xPkMaxRes= mCursor.getString(33);
                            str_xA95Res= mCursor.getString(34);
                            str_yPkMaxRes= mCursor.getString(35);
                            str_yA95Res= mCursor.getString(36);
                            str_zPkMaxRes= mCursor.getString(37);
                            str_zA95Res= mCursor.getString(38);
                            PrintMeasureData();
                            while(mCursor.moveToNext()){//遍历数据表中的数据
                                str_date= mCursor.getString(1);
                                str_company= mCursor.getString(2);
                                str_number= mCursor.getString(3);
                                str_ratedSpeed= mCursor.getString(4);
                                str_disMax= mCursor.getString(5);
                                str_SpeedMax= mCursor.getString(6);
                                str_AMax= mCursor.getString(7);
                                str_AMin= mCursor.getString(8);
                                str_AAMax= mCursor.getString(9);
                                str_AAPk= mCursor.getString(10);
                                str_V95= mCursor.getString(11);
                                str_avgSpeed= mCursor.getString(12);
                                str_A95= mCursor.getString(13);
                                str_D95= mCursor.getString(14);
                                str_xPkMax= mCursor.getString(15);
                                str_xA95= mCursor.getString(16);
                                str_yPkMax= mCursor.getString(17);
                                str_yA95= mCursor.getString(18);
                                str_zPkMax= mCursor.getString(19);
                                str_zA95= mCursor.getString(20);
                                str_SoundMax= mCursor.getString(21);

                                str_SoundMaxRes= mCursor.getString(22);
                                str_disRes= mCursor.getString(23);
                                str_SpeedMaxRes= mCursor.getString(24);
                                str_ASpeedMaxRes= mCursor.getString(25);
                                str_DSpeedMaxRes= mCursor.getString(26);
                                str_AASpeedMaxRes= mCursor.getString(27);
                                str_PkSpeedMaxRes= mCursor.getString(28);
                                str_AvgSpeedRes= mCursor.getString(29);
                                str_V95Res= mCursor.getString(30);
                                str_A95Res= mCursor.getString(31);
                                str_D95Res= mCursor.getString(32);
                                str_xPkMaxRes= mCursor.getString(33);
                                str_xA95Res= mCursor.getString(34);
                                str_yPkMaxRes= mCursor.getString(35);
                                str_yA95Res= mCursor.getString(36);
                                str_zPkMaxRes= mCursor.getString(37);
                                str_zA95Res= mCursor.getString(38);
                                PrintMeasureData();
                            }
                        }
                    }
                }
                break;
                case R.id.delCurAccBtn: {
                    delCurBtn.setEnabled(false);
                    delCurBtnpressed = getResources().getDrawable(R.drawable.delcur);
                    delCurBtnpressed.setBounds(0, 0, delCurBtnpressed.getMinimumWidth(), delCurBtnpressed.getMinimumHeight());
                    delCurBtn.setCompoundDrawables(null, delCurBtnpressed, null, null);
                    delete();
                }
                break;
                case R.id.delAllAccBtn:
                {
                    delAllBtn.setEnabled(false);
                    delAllBtnpressed = getResources().getDrawable(R.drawable.delall);
                    delAllBtnpressed.setBounds(0, 0, delAllBtnpressed.getMinimumWidth(), delAllBtnpressed.getMinimumHeight());
                    delAllBtn.setCompoundDrawables(null,delAllBtnpressed, null, null);
                    while(mCursor.moveToFirst())
                    {
                        data_ID = mCursor.getInt(0);
                        delete();
                    }
                }
                break;
                case R.id.exportCurAccBtn: {
                    exportCurBtn.setEnabled(false);
                    exportCurBtnpressed = getResources().getDrawable(R.drawable.exportcur);
                    exportCurBtnpressed.setBounds(0, 0, exportCurBtnpressed.getMinimumWidth(), exportCurBtnpressed.getMinimumHeight());
                    exportCurBtn.setCompoundDrawables(null,exportCurBtnpressed, null, null);
                    if(mCursor.moveToFirst()){
                        ID = mCursor.getInt(0);
                        if (ID == data_ID) {
                            str_date= mCursor.getString(1);
                            str_company= mCursor.getString(2);
                            str_number= mCursor.getString(3);
                            str_ratedSpeed= mCursor.getString(4);
                            str_disMax= mCursor.getString(5);
                            str_SpeedMax= mCursor.getString(6);
                            str_AMax= mCursor.getString(7);
                            str_AMin= mCursor.getString(8);
                            str_AAMax= mCursor.getString(9);
                            str_AAPk= mCursor.getString(10);
                            str_V95= mCursor.getString(11);
                            str_avgSpeed= mCursor.getString(12);
                            str_A95= mCursor.getString(13);
                            str_D95= mCursor.getString(14);
                            str_xPkMax= mCursor.getString(15);
                            str_xA95= mCursor.getString(16);
                            str_yPkMax= mCursor.getString(17);
                            str_yA95= mCursor.getString(18);
                            str_zPkMax= mCursor.getString(19);
                            str_zA95= mCursor.getString(20);
                            str_SoundMax= mCursor.getString(21);

                            str_SoundMaxRes= mCursor.getString(22);
                            str_disRes= mCursor.getString(23);
                            str_SpeedMaxRes= mCursor.getString(24);
                            str_ASpeedMaxRes= mCursor.getString(25);
                            str_DSpeedMaxRes= mCursor.getString(26);
                            str_AASpeedMaxRes= mCursor.getString(27);
                            str_PkSpeedMaxRes= mCursor.getString(28);
                            str_AvgSpeedRes= mCursor.getString(29);
                            str_V95Res= mCursor.getString(30);
                            str_A95Res= mCursor.getString(31);
                            str_D95Res= mCursor.getString(32);
                            str_xPkMaxRes= mCursor.getString(33);
                            str_xA95Res= mCursor.getString(34);
                            str_yPkMaxRes= mCursor.getString(35);
                            str_yA95Res= mCursor.getString(36);
                            str_zPkMaxRes= mCursor.getString(37);
                            str_zA95Res= mCursor.getString(38);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                saveFileToPDF();
                            }else{
                                CreatePdf();
                            }
                            Toast.makeText(SaveActivity.this, "数据已导出到手机根目录/Documents/电梯乘运质量测试报告", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        while(mCursor.moveToNext()) {//遍历数据表中的数据
                            ID = mCursor.getInt(0);
                            if (ID == data_ID) {
                                str_date= mCursor.getString(1);
                                str_company= mCursor.getString(2);
                                str_number= mCursor.getString(3);
                                str_ratedSpeed= mCursor.getString(4);
                                str_disMax= mCursor.getString(5);
                                str_SpeedMax= mCursor.getString(6);
                                str_AMax= mCursor.getString(7);
                                str_AMin= mCursor.getString(8);
                                str_AAMax= mCursor.getString(9);
                                str_AAPk= mCursor.getString(10);
                                str_V95= mCursor.getString(11);
                                str_avgSpeed= mCursor.getString(12);
                                str_A95= mCursor.getString(13);
                                str_D95= mCursor.getString(14);
                                str_xPkMax= mCursor.getString(15);
                                str_xA95= mCursor.getString(16);
                                str_yPkMax= mCursor.getString(17);
                                str_yA95= mCursor.getString(18);
                                str_zPkMax= mCursor.getString(19);
                                str_zA95= mCursor.getString(20);
                                str_SoundMax= mCursor.getString(21);

                                str_SoundMaxRes= mCursor.getString(22);
                                str_disRes= mCursor.getString(23);
                                str_SpeedMaxRes= mCursor.getString(24);
                                str_ASpeedMaxRes= mCursor.getString(25);
                                str_DSpeedMaxRes= mCursor.getString(26);
                                str_AASpeedMaxRes= mCursor.getString(27);
                                str_PkSpeedMaxRes= mCursor.getString(28);
                                str_AvgSpeedRes= mCursor.getString(29);
                                str_V95Res= mCursor.getString(30);
                                str_A95Res= mCursor.getString(31);
                                str_D95Res= mCursor.getString(32);
                                str_xPkMaxRes= mCursor.getString(33);
                                str_xA95Res= mCursor.getString(34);
                                str_yPkMaxRes= mCursor.getString(35);
                                str_yA95Res= mCursor.getString(36);
                                str_zPkMaxRes= mCursor.getString(37);
                                str_zA95Res= mCursor.getString(38);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                    saveFileToPDF();
                                }else{

                                    CreatePdf();
                                }
                                Toast.makeText(SaveActivity.this, "数据已导出到手机根目录/Documents/电梯乘运质量测试报告", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                }
                break;
                case R.id.exportAllAccBtn: {
                    exportAllBtn.setEnabled(false);
                    exportAllBtnpressed = getResources().getDrawable(R.drawable.exportall);
                    exportAllBtnpressed.setBounds(0, 0, exportAllBtnpressed.getMinimumWidth(), exportAllBtnpressed.getMinimumHeight());
                    exportAllBtn.setCompoundDrawables(null,exportAllBtnpressed, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Log.d("SaveActivity", "xtemp=saveFileToPDFAll");
                        saveFileToPDFAll();
                    }else{
                        Log.d("SaveActivity", "xtemp=CreatePdfAll");
                        CreatePdfAll();
                    }
                    Toast.makeText(SaveActivity.this, "数据已导出到手机根目录/Documents/电梯乘运质量测试报告", Toast.LENGTH_SHORT).show();
                }
                break;
                case R.id.backAccSaveBtn: {
                    Intent intent = new Intent(SaveActivity.this, MeasureActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
                case R.id.exitAccSaveBtn: {
                    finish();
                }
                break;
                default: {
                }
                break;
            }
            handler.postDelayed(sendRunnable, 1000);
        }
    }
    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            delCurBtn.setEnabled(true);
            delCurBtnpressed = getResources().getDrawable(R.drawable.delcur1);
            delCurBtnpressed.setBounds(0, 0, delCurBtnpressed.getMinimumWidth(), delCurBtnpressed.getMinimumHeight());
            delCurBtn.setCompoundDrawables(null, delCurBtnpressed, null, null);

            delAllBtn.setEnabled(true);
            delAllBtnpressed = getResources().getDrawable(R.drawable.delall1);
            delAllBtnpressed.setBounds(0, 0, delAllBtnpressed.getMinimumWidth(), delAllBtnpressed.getMinimumHeight());
            delAllBtn.setCompoundDrawables(null, delAllBtnpressed, null, null);

            printCurBtn.setEnabled(true);
            printCurBtnpressed = getResources().getDrawable(R.drawable.printcur1);
            printCurBtnpressed.setBounds(0, 0, printCurBtnpressed.getMinimumWidth(), printCurBtnpressed.getMinimumHeight());
            printCurBtn.setCompoundDrawables(null, printCurBtnpressed, null, null);

            printAllBtn.setEnabled(true);
            printAllBtnpressed = getResources().getDrawable(R.drawable.printall1);
            printAllBtnpressed.setBounds(0, 0, printAllBtnpressed.getMinimumWidth(), printAllBtnpressed.getMinimumHeight());
            printAllBtn.setCompoundDrawables(null, printAllBtnpressed, null, null);

            exportCurBtn.setEnabled(true);
            exportCurBtnpressed = getResources().getDrawable(R.drawable.exportcur1);
            exportCurBtnpressed.setBounds(0, 0, exportCurBtnpressed.getMinimumWidth(), exportCurBtnpressed.getMinimumHeight());
            exportCurBtn.setCompoundDrawables(null, exportCurBtnpressed, null, null);

            exportAllBtn.setEnabled(true);
            exportAllBtnpressed = getResources().getDrawable(R.drawable.exportall1);
            exportAllBtnpressed.setBounds(0, 0, exportAllBtnpressed.getMinimumWidth(), exportAllBtnpressed.getMinimumHeight());
            exportAllBtn.setCompoundDrawables(null, exportAllBtnpressed, null, null);
        }
    };
    public void setUpViews(){
        saveDB = new DB(this);
        mCursor = saveDB.select();
        savelistView = (ListView)findViewById(R.id.savelistAcc);
        savelistView.setAdapter(new saveListAdapter(this, mCursor));
        savelistView.setOnItemClickListener(this);
    }
    Runnable PrinterRunnable = new Runnable() {
        @Override
        public void run() {
            if(PrintConnect){         //连接成功并已经打印数据，则关闭蓝牙
                handler.removeCallbacks(PrinterRunnable);
                PrintConnect = false;
            }else{
                SharedPreferences shares = getSharedPreferences( "BLE_Info", Activity.MODE_PRIVATE );
                if(shares.getBoolean("BondPrinter",false))
                {
                    printDataService = new PrintDataService(SaveActivity.this,shares.getString("Printer",""));
                    //Toast.makeText(overActivity.this,"蓝牙打印机连接中...",Toast.LENGTH_LONG).show();
                }
                if(printDataService != null){
                    PrintConnect = printDataService.connect();
                    if(PrintConnect){
                        Toast.makeText(SaveActivity.this,"蓝牙打印机连接成功...",Toast.LENGTH_LONG).show();
                        handler.removeCallbacks (PrinterRunnable);
                    }
                }
                handler.postDelayed(PrinterRunnable,100);
            }
        }
    };
    //打印测试数据
    private void PrintMeasureData(){
        printDataService.send("\n*******************************\n");
        printDataService.send("电梯乘运质量测试结果");
        printDataService.send("\n*******************************\n");
        printDataService.send("受检单位"+": "+str_company+"\n");//
        printDataService.send("设备编号"+": "+ str_number+"\n");//
        printDataService.send("检测时间"+": "+ str_date+"\n");//
        printDataService.send("额定速度"+": "+ str_ratedSpeed+"m/s"+"\n");//
        printDataService.send("测试项目"+": "+ " 测试数据 "+" 单位 "+" 结果 "+"\n");//
        printDataService.send("最大噪声"+": "+ str_SoundMax+"dB"+str_SoundMaxRes+"\n");//
        printDataService.send("最大速度"+": "+ str_SpeedMax+str_SpeedUnit+str_SpeedMaxRes+"\n");//
        printDataService.send("平均速度"+": "+ str_avgSpeed+str_SpeedUnit+str_AvgSpeedRes+"\n");//
        printDataService.send("最大加速度"+": "+ str_AMax+str_AccUnit+str_ASpeedMaxRes+"\n");//
        printDataService.send("最大减速度"+": "+ str_AMin+str_AccUnit+str_DSpeedMaxRes+"\n");//
        printDataService.send("A95加速度"+": "+ str_A95+str_AccUnit+str_A95Res+"\n");//
        printDataService.send("A95减速度"+": "+ str_D95+str_AccUnit+str_D95Res+"\n");//
        printDataService.send("V95速度"+": "+ str_V95+str_SpeedUnit+str_V95Res+"\n");//
        printDataService.send("运行距离"+": "+ str_disMax+"m"+"合格"+"\n");//
        printDataService.send("最大加加速度"+": "+ str_AAMax+"m/s^3"+"合格"+"\n");//
        printDataService.send("加速度最大峰峰值"+": "+ str_AAPk+str_AccUnit+"合格"+"\n");//

        printDataService.send("X轴最大峰峰值"+": "+ str_xPkMax+str_AccUnit+str_xPkMaxRes+"\n");//
        printDataService.send("X轴A95峰峰值"+": "+ str_xA95+str_AccUnit+str_xA95Res+"\n");//
        printDataService.send("Y轴最大峰峰值"+": "+ str_yPkMax+str_AccUnit+str_yPkMaxRes+"\n");//
        printDataService.send("Y轴A95峰峰值"+": "+ str_yA95+str_AccUnit+str_yA95Res+"\n");//
        printDataService.send("Z轴最大峰峰值"+": "+ str_zPkMax+str_AccUnit+str_zPkMaxRes+"\n");//
        printDataService.send("Z轴A95峰峰值"+": "+ str_zA95+str_AccUnit+str_zA95Res+"\n");//
        printDataService.send("*******************************\n\n\n\n");
        Toast.makeText(SaveActivity.this,"打印完成！",Toast.LENGTH_SHORT).show();
    }

    public void add(){
        Date curDate =  new Date(System.currentTimeMillis());//获取当前时间
        SimpleDateFormat formatter   =   new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
        String   str_date   =   formatter.format(curDate);
        saveDB.insert(str_date);
        mCursor.requery();
        savelistView.invalidateViews();
    }
    public void delete(){
        if (data_ID == 0) {
            return;
        }
        saveDB.delete(data_ID);
        mCursor.requery();
        savelistView.invalidateViews();
        Toast.makeText(this, "删除成功!", Toast.LENGTH_SHORT).show();
    }

    public void update(){
        saveDB.update(data_ID);
        mCursor.requery();
        savelistView.invalidateViews();
        Toast.makeText(this, "Update Successed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCursor.moveToPosition(position);
        data_ID = mCursor.getInt(0);
        Toast.makeText(this, "已选中!", Toast.LENGTH_SHORT).show();
    }

    public class saveListAdapter extends BaseAdapter {
        private Context mContext;
        private Cursor mCursor;
        public saveListAdapter(Context context,Cursor cursor) {
            mContext = context;
            mCursor = cursor;
        }
        @Override
        public int getCount() {
            return mCursor.getCount();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getListView(position, convertView, parent);
        }
    }
    @SuppressLint("SetTextI18n")
    private View getListView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =getLayoutInflater().inflate(R.layout.save_listacc, null);//加载布局
        }
        mCursor.moveToPosition(position);

        TextView date_txt = (TextView) convertView.findViewById(R.id.data1);
        TextView company_txt = (TextView) convertView.findViewById(R.id.data2);
        TextView deviceNum_txt = (TextView) convertView.findViewById(R.id.data3);
        TextView ratedSpeed_txt = (TextView) convertView.findViewById(R.id.data4);
        TextView disMax_txt = (TextView) convertView.findViewById(R.id.data5);
        TextView speedMax_txt = (TextView) convertView.findViewById(R.id.data6);
        TextView avgSpeed_txt = (TextView) convertView.findViewById(R.id.data7);
        TextView V95_txt = (TextView) convertView.findViewById(R.id.data8);
        TextView AMax_txt = (TextView) convertView.findViewById(R.id.data9);
        TextView AMin_txt = (TextView) convertView.findViewById(R.id.data10);
        TextView A95_txt = (TextView) convertView.findViewById(R.id.data11);
        TextView D95_txt = (TextView) convertView.findViewById(R.id.data12);
        TextView AAMax_txt = (TextView) convertView.findViewById(R.id.data13);
        TextView AAPk_txt = (TextView) convertView.findViewById(R.id.data14);

        TextView xPkMax_txt = (TextView) convertView.findViewById(R.id.data15);
        TextView xA95_txt = (TextView) convertView.findViewById(R.id.data16);
        TextView yPkMax_txt = (TextView) convertView.findViewById(R.id.data17);
        TextView yA95_txt = (TextView) convertView.findViewById(R.id.data18);
        TextView zPkMax_txt = (TextView) convertView.findViewById(R.id.data19);
        TextView zA95_txt = (TextView) convertView.findViewById(R.id.data20);
        TextView SoundMax_txt = (TextView) convertView.findViewById(R.id.data21);

        date_txt.setText("检测时间："+mCursor.getString(1));
        company_txt.setText("受检单位："+mCursor.getString(2));
        deviceNum_txt.setText("设备编号："+mCursor.getString(3));
        ratedSpeed_txt.setText("额定速度："+mCursor.getString(4)+"  "+"m/s");
        disMax_txt.setText("运行距离："+mCursor.getString(5)+"  "+"m"+"  "+mCursor.getString(23));
        speedMax_txt.setText("最大速度："+mCursor.getString(6)+"  "+"m/s"+"  "+mCursor.getString(24));
        avgSpeed_txt.setText("平均速度："+mCursor.getString(12)+"  "+"m/s"+"  "+mCursor.getString(30));
        V95_txt.setText("V95速度："+mCursor.getString(11)+"  "+"m/s"+"  "+mCursor.getString(29));
        AMax_txt.setText("最大加速度："+mCursor.getString(7)+"  "+"m/s^2"+"  "+mCursor.getString(25));
        AMin_txt.setText("最大减速度："+mCursor.getString(8)+"  "+"m/s^2"+"  "+mCursor.getString(26));
        A95_txt.setText("A95加速度："+mCursor.getString(13)+"  "+"m/s^2"+"  "+mCursor.getString(31));
        D95_txt.setText("A95减速度："+mCursor.getString(14)+"  "+"m/s^2"+"  "+mCursor.getString(32));
        AAMax_txt.setText("最大加加速度："+mCursor.getString(9)+"  "+"m/s^3"+"  "+mCursor.getString(27));
        AAPk_txt.setText("加速度最大峰峰值："+mCursor.getString(10)+"  "+"m/s^2"+"  "+mCursor.getString(28));

        xPkMax_txt.setText("X轴最大峰峰值："+mCursor.getString(15)+"  "+"m/s^2"+"  "+mCursor.getString(33));
        xA95_txt.setText("X轴A95峰峰值："+mCursor.getString(16)+"  "+"m/s^2"+"  "+mCursor.getString(34));
        yPkMax_txt.setText("Y轴最大峰峰值："+mCursor.getString(17)+"  "+"m/s^2"+"  "+mCursor.getString(35));
        yA95_txt.setText("Y轴A95峰峰值："+mCursor.getString(18)+"  "+"m/s^2"+"  "+mCursor.getString(36));
        zPkMax_txt.setText("Z轴最大峰峰值："+mCursor.getString(19)+"  "+"m/s^2"+"  "+mCursor.getString(37));
        zA95_txt.setText("Z轴A95峰峰值："+mCursor.getString(20)+"  "+"m/s^2"+"  "+mCursor.getString(38));
        SoundMax_txt.setText("最大噪声："+mCursor.getString(21)+"  "+"dB"+"  "+mCursor.getString(22));
        return convertView;
    }
    //权限读写
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public void notifySystemToScan(String filePath) {         //将文件修改信息通知到系统中
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        SaveActivity.this.sendBroadcast(intent);
    }
    private void CreatePdfAll(){
        verifyStoragePermissions(SaveActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doc = new Document(PageSize.A4, 36, 36, 36, 36);// 创建一个document对象
                FileOutputStream fos;
                Paragraph pdfcontext;
                try {
                    //创建目录
                    File destDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "电梯乘运质量测试报告"+ File.separator);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                        notifySystemToScan(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "电梯乘运质量测试报告"+ File.separator );
                    }

                    Uri uri = Uri.fromFile(destDir);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri);
                    getApplication().getApplicationContext().sendBroadcast(intent);

                    SimpleDateFormat formatter   =   new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
                    Date mcurDate =  new Date(System.currentTimeMillis());//获取当前时间
                    String   str_curDate   =   formatter.format(mcurDate);

                    fos = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "电梯乘运质量测试报告" + File.separator + str_curDate+".pdf"); // pdf_address为Pdf文件保存到sd卡的路径
                    PdfWriter.getInstance(doc, fos);
                    doc.open();
                    doc.setPageCount(1);

                    if(mCursor.moveToFirst()){
                        str_date= mCursor.getString(1);
                        str_company= mCursor.getString(2);
                        str_number= mCursor.getString(3);
                        str_ratedSpeed= mCursor.getString(4);
                        str_disMax= mCursor.getString(5);
                        str_SpeedMax= mCursor.getString(6);
                        str_AMax= mCursor.getString(7);
                        str_AMin= mCursor.getString(8);
                        str_AAMax= mCursor.getString(9);
                        str_AAPk= mCursor.getString(10);
                        str_V95= mCursor.getString(11);
                        str_avgSpeed= mCursor.getString(12);
                        str_A95= mCursor.getString(13);
                        str_D95= mCursor.getString(14);
                        str_xPkMax= mCursor.getString(15);
                        str_xA95= mCursor.getString(16);
                        str_yPkMax= mCursor.getString(17);
                        str_yA95= mCursor.getString(18);
                        str_zPkMax= mCursor.getString(19);
                        str_zA95= mCursor.getString(20);
                        str_SoundMax= mCursor.getString(21);

                        str_SoundMaxRes= mCursor.getString(22);
                        str_disRes= mCursor.getString(23);
                        str_SpeedMaxRes= mCursor.getString(24);
                        str_ASpeedMaxRes= mCursor.getString(25);
                        str_DSpeedMaxRes= mCursor.getString(26);
                        str_AASpeedMaxRes= mCursor.getString(27);
                        str_PkSpeedMaxRes= mCursor.getString(28);
                        str_AvgSpeedRes= mCursor.getString(29);
                        str_V95Res= mCursor.getString(30);
                        str_A95Res= mCursor.getString(31);
                        str_D95Res= mCursor.getString(32);
                        str_xPkMaxRes= mCursor.getString(33);
                        str_xA95Res= mCursor.getString(34);
                        str_yPkMaxRes= mCursor.getString(35);
                        str_yA95Res= mCursor.getString(36);
                        str_zPkMaxRes= mCursor.getString(37);
                        str_zA95Res= mCursor.getString(38);

                        pdfcontext = new Paragraph("电梯乘运质量测试报告",setChineseTitleFont());
                        pdfcontext.setAlignment(Element.ALIGN_CENTER);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(8);
                        doc.add(pdfcontext);
                        //创建一个有3列的表格
                        pdfcontext = new Paragraph("受检单位："+str_company+"      "+"额定速度："+str_ratedSpeed+"m/s",setChineseTextFont());
                        pdfcontext.setAlignment(Element.ALIGN_LEFT);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("设备编号："+str_number+"      "+"检测时间："+str_date,setChineseTextFont());
                        pdfcontext.setAlignment(Element.ALIGN_LEFT);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(8);
                        doc.add(pdfcontext);
                        //创建一个有3列的表格
                        PdfPTable table1 = new PdfPTable(8);
                        table1.setWidthPercentage(99);
                        //定义一个表格单元
                        PdfPCell cell2 = new PdfPCell();
                        cell2.setMinimumHeight(20);
                        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

                        PdfPTable mtable2 = new PdfPTable(8);
                        mtable2.setSplitLate(false);
                        mtable2.setSplitRows(true);
                        mtable2.setWidthPercentage(99);
                        mtable2.setWidths(new float[]{300,200,200,200,300,200,200,200});
                        cell2.setColspan(1);

                        cell2.setBackgroundColor(new BaseColor (195,195,195));
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor(255,255,255));
                        cell2.setPhrase(new Phrase("最大噪声",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SoundMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("dB",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SoundMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor(255,255,255));
                        cell2.setPhrase(new Phrase("最大速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("平均速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_avgSpeed,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AvgSpeedRes,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("最大加速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_ASpeedMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("最大减速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AMin,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_DSpeedMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("A95加速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_A95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_A95Res,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("A95减速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_D95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_D95Res,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("V95速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_V95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_V95Res,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(" 运行距离",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_disMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("m ",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("最大加加速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AAMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("m/s^3",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("加速度最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AAPk,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor (195,195,195));
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor(255,255,255));
                        cell2.setPhrase(new Phrase("X轴最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xPkMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xPkMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("X轴A95峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xA95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xA95Res,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("Y轴最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yPkMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yPkMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("Y轴A95峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yA95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yA95Res,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("Z轴最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zPkMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zPkMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("Z轴A95峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zA95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zA95Res,setChineseFont()))    ;mtable2.addCell(cell2);
                        doc.add(mtable2);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        while(mCursor.moveToNext()){//遍历数据表中的数据
                            str_date= mCursor.getString(1);
                            str_company= mCursor.getString(2);
                            str_number= mCursor.getString(3);
                            str_ratedSpeed= mCursor.getString(4);
                            str_disMax= mCursor.getString(5);
                            str_SpeedMax= mCursor.getString(6);
                            str_AMax= mCursor.getString(7);
                            str_AMin= mCursor.getString(8);
                            str_AAMax= mCursor.getString(9);
                            str_AAPk= mCursor.getString(10);
                            str_V95= mCursor.getString(11);
                            str_avgSpeed= mCursor.getString(12);
                            str_A95= mCursor.getString(13);
                            str_D95= mCursor.getString(14);
                            str_xPkMax= mCursor.getString(15);
                            str_xA95= mCursor.getString(16);
                            str_yPkMax= mCursor.getString(17);
                            str_yA95= mCursor.getString(18);
                            str_zPkMax= mCursor.getString(19);
                            str_zA95= mCursor.getString(20);
                            str_SoundMax= mCursor.getString(21);

                            str_SoundMaxRes= mCursor.getString(22);
                            str_disRes= mCursor.getString(23);
                            str_SpeedMaxRes= mCursor.getString(24);
                            str_ASpeedMaxRes= mCursor.getString(25);
                            str_DSpeedMaxRes= mCursor.getString(26);
                            str_AASpeedMaxRes= mCursor.getString(27);
                            str_PkSpeedMaxRes= mCursor.getString(28);
                            str_AvgSpeedRes= mCursor.getString(29);
                            str_V95Res= mCursor.getString(30);
                            str_A95Res= mCursor.getString(31);
                            str_D95Res= mCursor.getString(32);
                            str_xPkMaxRes= mCursor.getString(33);
                            str_xA95Res= mCursor.getString(34);
                            str_yPkMaxRes= mCursor.getString(35);
                            str_yA95Res= mCursor.getString(36);
                            str_zPkMaxRes= mCursor.getString(37);
                            str_zA95Res= mCursor.getString(38);

                            pdfcontext = new Paragraph("电梯乘运质量测试报告",setChineseTitleFont());
                            pdfcontext.setAlignment(Element.ALIGN_CENTER);
                            doc.add(pdfcontext);
                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(8);
                            doc.add(pdfcontext);
                            //创建一个有3列的表格
                            pdfcontext = new Paragraph("受检单位："+str_company+"      "+"额定速度："+str_ratedSpeed+"m/s",setChineseTextFont());
                            pdfcontext.setAlignment(Element.ALIGN_LEFT);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("设备编号："+str_number+"      "+"检测时间："+str_date,setChineseTextFont());
                            pdfcontext.setAlignment(Element.ALIGN_LEFT);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(8);
                            doc.add(pdfcontext);
                            //创建一个有3列的表格
                            PdfPTable table = new PdfPTable(8);
                            table.setWidthPercentage(99);
                            //定义一个表格单元
                            PdfPCell cell = new PdfPCell();
                            cell.setMinimumHeight(20);
                            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                            PdfPTable mtable = new PdfPTable(8);
                            mtable.setSplitLate(false);
                            mtable.setSplitRows(true);
                            mtable.setWidthPercentage(99);
                            mtable.setWidths(new float[]{300,200,200,200,300,200,200,200});
                            cell.setColspan(1);

                            cell.setBackgroundColor(new BaseColor (195,195,195));
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setBackgroundColor(new BaseColor(255,255,255));
                            cell.setPhrase(new Phrase("最大噪声",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SoundMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("dB",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SoundMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("最大速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("平均速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_avgSpeed,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AvgSpeedRes,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("最大加速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_ASpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("最大减速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AMin,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_DSpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("A95加速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_A95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_A95Res,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("A95减速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_D95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_D95Res,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("V95速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_V95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_V95Res,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("运行距离",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_disMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("m",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("最大加加速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AAMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("m/s^3",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("加速度最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AAPk,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setBackgroundColor(new BaseColor (195,195,195));
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setBackgroundColor(new BaseColor(255,255,255));
                            cell.setPhrase(new Phrase("X轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xPkMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("X轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xA95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xA95Res,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("Y轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yPkMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("Y轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yA95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yA95Res,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("Z轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zPkMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("Z轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zA95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zA95Res,setChineseFont()))    ;mtable.addCell(cell);
                            doc.add(mtable);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);
                        }
                    }
                    doc.close();
                    fos.flush();
                    fos.close();
                    notifySystemToScan(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "电梯乘运质量测试报告" +  File.separator + str_curDate+".pdf");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //创建PDF文件-----------------------------------------------------------------
    Document doc;
    private void CreatePdf(){
        verifyStoragePermissions(SaveActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doc = new Document(PageSize.A4, 36, 36, 36, 36);// 创建一个document对象
                FileOutputStream fos;
                Paragraph pdfcontext;
                try {
                    //创建目录
                    File destDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "电梯乘运质量测试报告" + File.separator);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                        notifySystemToScan(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "电梯乘运质量测试报告" + File.separator );
                    }
                    Uri uri = Uri.fromFile(destDir);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri);
                    getApplication().getApplicationContext().sendBroadcast(intent);
                    Date curDate =  new Date(System.currentTimeMillis());//获取当前时间
                    fos = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "电梯乘运质量测试报告"  + File.separator + str_date+".pdf"); // pdf_address为Pdf文件保存到sd卡的路径
                    PdfWriter.getInstance(doc, fos);
                    doc.open();
                    doc.setPageCount(1);
                    pdfcontext = new Paragraph("电梯乘运质量测试报告",setChineseTitleFont());
                    pdfcontext.setAlignment(Element.ALIGN_CENTER);
                    doc.add(pdfcontext);
                    pdfcontext = new Paragraph("\n\r");
                    pdfcontext.setLeading(8);
                    doc.add(pdfcontext);
                    //创建一个有3列的表格
                    pdfcontext = new Paragraph("受检单位："+str_company+"      "+"额定速度："+str_ratedSpeed+"m/s",setChineseTextFont());
                    pdfcontext.setAlignment(Element.ALIGN_LEFT);
                    doc.add(pdfcontext);

                    pdfcontext = new Paragraph("设备编号："+str_number+"      "+"检测时间："+str_date,setChineseTextFont());
                    pdfcontext.setAlignment(Element.ALIGN_LEFT);
                    doc.add(pdfcontext);

                    pdfcontext = new Paragraph("\n\r");
                    pdfcontext.setLeading(8);
                    doc.add(pdfcontext);
                    //创建一个有6列的表格
                    PdfPTable table = new PdfPTable(8);
                    table.setWidthPercentage(99);
                    //定义一个表格单元
                    PdfPCell cell = new PdfPCell();
                    cell.setMinimumHeight(20);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                    PdfPTable mtable = new PdfPTable(8);
                    mtable.setWidthPercentage(99);
                    mtable.setWidths(new float[]{300,200,200,200,300,200,200,200});
                    cell.setColspan(1);

                    cell.setBackgroundColor(new BaseColor (195,195,195));
                    cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);

                    cell.setBackgroundColor(new BaseColor(255,255,255));
                    cell.setPhrase(new Phrase("最大噪声",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SoundMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("dB",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SoundMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("最大速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SpeedMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("平均速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_avgSpeed,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AvgSpeedRes,setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("最大加速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_ASpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("最大减速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AMin,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_DSpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("A95加速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_A95,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_A95Res,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("A95减速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_D95,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_D95Res,setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("V95速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_V95,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_V95Res,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("运行距离",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_disMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("m",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("最大加加速度",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AAMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("m/s^3",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("加速度最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AAPk,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                    cell.setBackgroundColor(new BaseColor (195,195,195));
                    cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                    cell.setBackgroundColor(new BaseColor(255,255,255));
                    cell.setPhrase(new Phrase("X轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_xPkMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_xPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("X轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_xA95,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_xA95Res,setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("Y轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_yPkMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_yPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("Y轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_yA95,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_yA95Res,setChineseFont()))    ;mtable.addCell(cell);

                    cell.setPhrase(new Phrase("Z轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_zPkMax,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_zPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase("Z轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_zA95,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                    cell.setPhrase(new Phrase(str_zA95Res,setChineseFont()))    ;mtable.addCell(cell);

                    doc.add(mtable);
                    doc.close();
                    fos.flush();
                    fos.close();
                    notifySystemToScan(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "电梯乘运质量测试报告"  +  File.separator + str_date+".pdf");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private void savePdfAndLogUri(String orgFilePath,String fileName,String filePath) {
        try {
            InputStream in = new FileInputStream(new File(orgFilePath));
            Uri savedFileUri = savePDFFile(context, in, "files/pdf", fileName, filePath);
            Log.d("URI: ", savedFileUri.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    @NonNull
    private Uri savePDFFile(@NonNull final Context context, @NonNull InputStream in,
                            @NonNull final String mimeType,
                            @NonNull final String displayName, @Nullable final String subFolder) throws IOException {
        String relativeLocation = Environment.DIRECTORY_DOCUMENTS;
        if (!TextUtils.isEmpty(subFolder)) {
            relativeLocation += File.separator + subFolder;
        }

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);
        contentValues.put(MediaStore.Video.Media.TITLE, "SomeName");
        contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        final ContentResolver resolver = context.getContentResolver();
        OutputStream stream = null;
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Files.getContentUri("external");
            uri = resolver.insert(contentUri, contentValues);
            ParcelFileDescriptor pfd;
            try {
                assert uri != null;
                pfd = getContentResolver().openFileDescriptor(uri, "w");
                assert pfd != null;
                FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                byte[] buf = new byte[4 * 1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
                pfd.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            contentValues.clear();
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
            getContentResolver().update(uri, contentValues, null, null);
            stream = resolver.openOutputStream(uri);
            if (stream == null) {
                throw new IOException("Failed to get output stream.");
            }
            return uri;
        } catch (IOException e) {
            // Don't leave an orphan entry in the MediaStore
            resolver.delete(uri, null, null);
            throw e;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
    //复制沙盒私有文件到Documents共目录下
    //orgFilePath是要复制的文件私有目录路径
    //displayName复制后文件要显示的文件名称带后缀（如xx.txt）
    @RequiresApi(Build.VERSION_CODES.Q)
    public static void copyPrivateToDocuments(Context context,String orgFilePath,String displayName){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");//MediaStore对应类型名
        values.put(MediaStore.Files.FileColumns.TITLE, displayName);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Download/Test");//公共目录下目录名

        Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;//内部存储的Download路径
        ContentResolver resolver = context.getContentResolver();

        Uri insertUri = resolver.insert(external, values);//使用ContentResolver创建需要操作的文件
        //Log.i("Test--","insertUri: " + insertUri);

        InputStream ist=null;
        OutputStream ost = null;
        try {
            ist=new FileInputStream(new File(orgFilePath));
            if (insertUri != null) {
                ost = resolver.openOutputStream(insertUri);
            }
            if (ost != null) {
                byte[] buffer = new byte[4096];
                int byteCount = 0;
                while ((byteCount = ist.read(buffer)) != -1) {  // 循环从输入流读取 buffer字节
                    ost.write(buffer, 0, byteCount);        // 将读取的输入流写入到输出流
                }
                // write what you want
            }
        } catch (IOException e) {
            //Log.i("copyPrivateToDownload--","fail: " + e.getCause());
        } finally {
            try {
                if (ist != null) {
                    ist.close();
                }
                if (ost != null) {
                    ost.close();
                }
            } catch (IOException e) {
                //Log.i("copyPrivateToDownload--","fail in close: " + e.getCause());
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private void  saveFileToPDFAll()
    {
        verifyStoragePermissions(SaveActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doc = new Document(PageSize.A4, 36, 36, 36, 36);// 创建一个document对象
                FileOutputStream fos;
                Paragraph pdfcontext;
                try {
                    //创建目录
                    File destDir = new File(context.getExternalFilesDir("").getAbsolutePath()+ File.separator + "电梯乘运质量测试报告"+ File.separator);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                        notifySystemToScan(context.getExternalFilesDir("").getAbsolutePath()+ File.separator + "电梯乘运质量测试报告"+ File.separator );
                    }

                    Uri uri = Uri.fromFile(destDir);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri);
                    getApplication().getApplicationContext().sendBroadcast(intent);

                    SimpleDateFormat formatter   =   new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
                    Date mcurDate =  new Date(System.currentTimeMillis());//获取当前时间
                    String   str_curDate   =   formatter.format(mcurDate);

                    fos = new FileOutputStream(context.getExternalFilesDir("").getAbsolutePath()+ File.separator + "电梯乘运质量测试报告" + File.separator + str_curDate +".pdf"); // pdf_address为Pdf文件保存到sd卡的路径
                    PdfWriter.getInstance(doc, fos);
                    doc.open();
                    doc.setPageCount(1);

                    if(mCursor.moveToFirst()){
                        str_date= mCursor.getString(1);
                        str_company= mCursor.getString(2);
                        str_number= mCursor.getString(3);
                        str_ratedSpeed= mCursor.getString(4);
                        str_disMax= mCursor.getString(5);
                        str_SpeedMax= mCursor.getString(6);
                        str_AMax= mCursor.getString(7);
                        str_AMin= mCursor.getString(8);
                        str_AAMax= mCursor.getString(9);
                        str_AAPk= mCursor.getString(10);
                        str_V95= mCursor.getString(11);
                        str_avgSpeed= mCursor.getString(12);
                        str_A95= mCursor.getString(13);
                        str_D95= mCursor.getString(14);
                        str_xPkMax= mCursor.getString(15);
                        str_xA95= mCursor.getString(16);
                        str_yPkMax= mCursor.getString(17);
                        str_yA95= mCursor.getString(18);
                        str_zPkMax= mCursor.getString(19);
                        str_zA95= mCursor.getString(20);
                        str_SoundMax= mCursor.getString(21);

                        str_SoundMaxRes= mCursor.getString(22);
                        str_disRes= mCursor.getString(23);
                        str_SpeedMaxRes= mCursor.getString(24);
                        str_ASpeedMaxRes= mCursor.getString(25);
                        str_DSpeedMaxRes= mCursor.getString(26);
                        str_AASpeedMaxRes= mCursor.getString(27);
                        str_PkSpeedMaxRes= mCursor.getString(28);
                        str_AvgSpeedRes= mCursor.getString(29);
                        str_V95Res= mCursor.getString(30);
                        str_A95Res= mCursor.getString(31);
                        str_D95Res= mCursor.getString(32);
                        str_xPkMaxRes= mCursor.getString(33);
                        str_xA95Res= mCursor.getString(34);
                        str_yPkMaxRes= mCursor.getString(35);
                        str_yA95Res= mCursor.getString(36);
                        str_zPkMaxRes= mCursor.getString(37);
                        str_zA95Res= mCursor.getString(38);

                        pdfcontext = new Paragraph("电梯乘运质量测试报告",setChineseTitleFont());
                        pdfcontext.setAlignment(Element.ALIGN_CENTER);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(8);
                        doc.add(pdfcontext);
                        //创建一个有3列的表格
                        pdfcontext = new Paragraph("受检单位："+str_company+"      "+"额定速度："+str_ratedSpeed+"m/s",setChineseTextFont());
                        pdfcontext.setAlignment(Element.ALIGN_LEFT);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("设备编号："+str_number+"      "+"检测时间："+str_date,setChineseTextFont());
                        pdfcontext.setAlignment(Element.ALIGN_LEFT);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(8);
                        doc.add(pdfcontext);

                        //创建一个有3列的表格
                        PdfPTable table1 = new PdfPTable(8);
                        table1.setWidthPercentage(99);
                        //定义一个表格单元
                        PdfPCell cell2 = new PdfPCell();
                        cell2.setMinimumHeight(20);
                        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

                        PdfPTable mtable2 = new PdfPTable(8);
                        mtable2.setSplitLate(false);
                        mtable2.setSplitRows(true);
                        mtable2.setWidthPercentage(99);
                        mtable2.setWidths(new float[]{300,200,200,200,300,200,200,200});
                        cell2.setColspan(1);

                        cell2.setBackgroundColor(new BaseColor (195,195,195));
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor(255,255,255));
                        cell2.setPhrase(new Phrase("最大噪声",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SoundMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("dB",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SoundMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor(255,255,255));
                        cell2.setPhrase(new Phrase("最大速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("平均速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_avgSpeed,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AvgSpeedRes,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("最大加速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_ASpeedMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("最大减速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AMin,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_DSpeedMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("A95加速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_A95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_A95Res,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("A95减速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_D95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_D95Res,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("V95速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_V95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_V95Res,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("运行距离",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_disMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("m",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("最大加加速度",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AAMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("m/s^3",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("加速度最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AAPk,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor (195,195,195));
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setBackgroundColor(new BaseColor(255,255,255));
                        cell2.setPhrase(new Phrase("X轴最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xPkMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xPkMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("X轴A95峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xA95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_xA95Res,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("Y轴最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yPkMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yPkMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("Y轴A95峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yA95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_yA95Res,setChineseFont()))    ;mtable2.addCell(cell2);

                        cell2.setPhrase(new Phrase("Z轴最大峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zPkMax,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zPkMaxRes,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase("Z轴A95峰峰值",setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zA95,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable2.addCell(cell2);
                        cell2.setPhrase(new Phrase(str_zA95Res,setChineseFont()))    ;mtable2.addCell(cell2);
                        doc.add(mtable2);
                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(6);
                        doc.add(pdfcontext);

                        while(mCursor.moveToNext()){//遍历数据表中的数据
                            str_date= mCursor.getString(1);
                            str_company= mCursor.getString(2);
                            str_number= mCursor.getString(3);
                            str_ratedSpeed= mCursor.getString(4);
                            str_disMax= mCursor.getString(5);
                            str_SpeedMax= mCursor.getString(6);
                            str_AMax= mCursor.getString(7);
                            str_AMin= mCursor.getString(8);
                            str_AAMax= mCursor.getString(9);
                            str_AAPk= mCursor.getString(10);
                            str_V95= mCursor.getString(11);
                            str_avgSpeed= mCursor.getString(12);
                            str_A95= mCursor.getString(13);
                            str_D95= mCursor.getString(14);
                            str_xPkMax= mCursor.getString(15);
                            str_xA95= mCursor.getString(16);
                            str_yPkMax= mCursor.getString(17);
                            str_yA95= mCursor.getString(18);
                            str_zPkMax= mCursor.getString(19);
                            str_zA95= mCursor.getString(20);
                            str_SoundMax= mCursor.getString(21);

                            str_SoundMaxRes= mCursor.getString(22);
                            str_disRes= mCursor.getString(23);
                            str_SpeedMaxRes= mCursor.getString(24);
                            str_ASpeedMaxRes= mCursor.getString(25);
                            str_DSpeedMaxRes= mCursor.getString(26);
                            str_AASpeedMaxRes= mCursor.getString(27);
                            str_PkSpeedMaxRes= mCursor.getString(28);
                            str_AvgSpeedRes= mCursor.getString(29);
                            str_V95Res= mCursor.getString(30);
                            str_A95Res= mCursor.getString(31);
                            str_D95Res= mCursor.getString(32);
                            str_xPkMaxRes= mCursor.getString(33);
                            str_xA95Res= mCursor.getString(34);
                            str_yPkMaxRes= mCursor.getString(35);
                            str_yA95Res= mCursor.getString(36);
                            str_zPkMaxRes= mCursor.getString(37);
                            str_zA95Res= mCursor.getString(38);

                            pdfcontext = new Paragraph("电梯乘运质量测试报告",setChineseTitleFont());
                            pdfcontext.setAlignment(Element.ALIGN_CENTER);
                            doc.add(pdfcontext);
                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(8);
                            doc.add(pdfcontext);
                            //创建一个有3列的表格
                            pdfcontext = new Paragraph("受检单位："+str_company+"      "+"额定速度："+str_ratedSpeed+"m/s",setChineseTextFont());
                            pdfcontext.setAlignment(Element.ALIGN_LEFT);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("设备编号："+str_number+"      "+"检测时间："+str_date,setChineseTextFont());
                            pdfcontext.setAlignment(Element.ALIGN_LEFT);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(8);
                            doc.add(pdfcontext);

                            //创建一个有3列的表格
                            PdfPTable table = new PdfPTable(8);
                            table.setWidthPercentage(99);
                            //定义一个表格单元
                            PdfPCell cell = new PdfPCell();
                            cell.setMinimumHeight(20);
                            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                            PdfPTable mtable = new PdfPTable(8);
                            mtable.setSplitLate(false);
                            mtable.setSplitRows(true);
                            mtable.setWidthPercentage(99);
                            mtable.setWidths(new float[]{300,200,200,200,300,200,200,200});
                            cell.setColspan(1);
                            cell.setBackgroundColor(new BaseColor(195,195,195));
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setBackgroundColor(new BaseColor(255,255,255));
                            cell.setPhrase(new Phrase("最大噪声",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SoundMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("dB",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SoundMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("最大速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("平均速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_avgSpeed,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AvgSpeedRes,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("最大加速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_ASpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("最大减速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AMin,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_DSpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("A95加速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_A95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_A95Res,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("A95减速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_D95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_D95Res,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("V95速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_V95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_V95Res,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("运行距离",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_disMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("m",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("最大加加速度",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AAMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("m/s^3",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("加速度最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AAPk,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setBackgroundColor(new BaseColor (195,195,195));
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                            cell.setBackgroundColor(new BaseColor(255,255,255));
                            cell.setPhrase(new Phrase("X轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xPkMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("X轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xA95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_xA95Res,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("Y轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yPkMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("Y轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yA95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_yA95Res,setChineseFont()))    ;mtable.addCell(cell);

                            cell.setPhrase(new Phrase("Z轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zPkMax,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase("Z轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zA95,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                            cell.setPhrase(new Phrase(str_zA95Res,setChineseFont()))    ;mtable.addCell(cell);
                            doc.add(mtable);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);

                            pdfcontext = new Paragraph("\n\r");
                            pdfcontext.setLeading(6);
                            doc.add(pdfcontext);
                        }
                    }
                    doc.close();
                    fos.flush();
                    fos.close();
                    notifySystemToScan(context.getExternalFilesDir("").getAbsolutePath()+ File.separator + "电梯乘运质量测试报告" +  File.separator + str_curDate +".pdf");
                    savePdfAndLogUri(context.getExternalFilesDir("").getAbsolutePath() + File.separator + "电梯乘运质量测试报告"  +  File.separator+ str_curDate +".pdf",str_curDate +".pdf","电梯乘运质量测试报告" );
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private void saveFileToPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            verifyStoragePermissions(SaveActivity.this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doc = new Document(PageSize.A4, 36, 36, 36, 36);// 创建一个document对象
                    FileOutputStream fos;
                    Paragraph pdfcontext;

                    try {
                        //创建目录
                        File destDir = new File(context.getExternalFilesDir("").getAbsolutePath() + File.separator + "电梯乘运质量测试报告" + File.separator);
                        if (!destDir.exists()) {
                            destDir.mkdirs();
                            notifySystemToScan(context.getExternalFilesDir("").getAbsolutePath() + File.separator + "电梯乘运质量测试报告" + File.separator);
                        }
                        str_company=input_data.getCom();
                        str_number=input_data.getNumber();
                        str_ratedSpeed=input_data.getRatedspeed();

                        Uri uri = Uri.fromFile(destDir);
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri);
                        getApplication().getApplicationContext().sendBroadcast(intent);

                        fos = new FileOutputStream(context.getExternalFilesDir("").getAbsolutePath()  + File.separator + "电梯乘运质量测试报告"  + File.separator  + str_date+".pdf"); // pdf_address为Pdf文件保存到sd卡的路径
                        PdfWriter.getInstance(doc, fos);
                        doc.open();
                        doc.setPageCount(1);
                        pdfcontext = new Paragraph("电梯乘运质量测试报告",setChineseTitleFont());
                        pdfcontext.setAlignment(Element.ALIGN_CENTER);
                        doc.add(pdfcontext);
                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(8);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("受检单位："+str_company+"      "+"额定速度："+str_ratedSpeed+"m/s",setChineseTextFont());
                        pdfcontext.setAlignment(Element.ALIGN_LEFT);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("设备编号："+str_number+"      "+"检测时间："+str_date,setChineseTextFont());
                        pdfcontext.setAlignment(Element.ALIGN_LEFT);
                        doc.add(pdfcontext);

                        pdfcontext = new Paragraph("\n\r");
                        pdfcontext.setLeading(8);
                        doc.add(pdfcontext);

                        //创建一个有6列的表格
                        PdfPTable table = new PdfPTable(8);
                        table.setWidthPercentage(99);
                        //定义一个表格单元
                        PdfPCell cell = new PdfPCell();
                        cell.setMinimumHeight(20);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                        PdfPTable mtable = new PdfPTable(8);
                        mtable.setWidthPercentage(99);
                        mtable.setWidths(new float[]{300,200,200,200,300,200,200,200});
                        cell.setColspan(1);

                        cell.setBackgroundColor(new BaseColor(195,195,195));
                        cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                        cell.setBackgroundColor(new BaseColor(255,255,255));
                        cell.setPhrase(new Phrase("最大噪声",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SoundMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("dB",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SoundMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(" ",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("",setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("最大速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SpeedMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("平均速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_avgSpeed,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AvgSpeedRes,setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("最大加速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_ASpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("最大减速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AMin,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_DSpeedMaxRes,setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("A95加速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_A95,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_A95Res,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("A95减速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_D95,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_D95Res,setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("V95速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_V95,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_SpeedUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_V95Res,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("运行距离",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_disMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("m",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("最大加加速度",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AAMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("m/s^3",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("加速度最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AAPk,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("合格",setChineseFont()))    ;mtable.addCell(cell);

                        cell.setBackgroundColor(new BaseColor (195,195,195));
                        cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("测试项目",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("数据",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("单位",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("结果",setChineseFont()))    ;mtable.addCell(cell);

                        cell.setBackgroundColor(new BaseColor(255,255,255));
                        cell.setPhrase(new Phrase("X轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_xPkMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_xPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("X轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_xA95,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_xA95Res,setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("Y轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_yPkMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_yPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("Y轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_yA95,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_yA95Res,setChineseFont()))    ;mtable.addCell(cell);

                        cell.setPhrase(new Phrase("Z轴最大峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_zPkMax,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_zPkMaxRes,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase("Z轴A95峰峰值",setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_zA95,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_AccUnit,setChineseFont()))    ;mtable.addCell(cell);
                        cell.setPhrase(new Phrase(str_zA95Res,setChineseFont()))    ;mtable.addCell(cell);

                        doc.add(mtable);
                        doc.close();
                        fos.flush();
                        fos.close();
                        notifySystemToScan(context.getExternalFilesDir("").getAbsolutePath() + File.separator + "电梯乘运质量测试报告"  +  File.separator+ str_date +".pdf");
                        savePdfAndLogUri(context.getExternalFilesDir("").getAbsolutePath() + File.separator + "电梯乘运质量测试报告"  +  File.separator+ str_date +".pdf",str_date +".pdf","电梯乘运质量测试报告" );
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    public Font setChineseTextFont() {
        BaseFont bf = null;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
                    BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, 14, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }
    /**
     * 设置PDF字体(较为耗时)
     */
    public Font setChineseFont() {
        BaseFont bf = null;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
                    BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, 12, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }

    public Font setChineseTitleFont() {
        BaseFont bf = null;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
                    BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, 20, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }
}
