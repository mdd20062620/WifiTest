package com.ruiguan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ruiguan.R;
import com.ruiguan.base.BaseActivity;
import com.ruiguan.entity.inputData;

public class MenuActivity extends BaseActivity implements View.OnClickListener {
    public static inputData input_data=new inputData();
    private Button MeasureBtn;
    private Button SearchBtn;

    private String str_company;
    private String str_number;
    private String str_ratedspeed;
    private EditText company_txt,number_txt,ratedSpeed_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ActivityCollector.addActivity(this);

        MeasureBtn=(Button) findViewById(R.id.MeasureBtn);
        MeasureBtn.setOnClickListener(this);
        SearchBtn=(Button) findViewById(R.id.ScanBtn);
        SearchBtn.setOnClickListener(this);

        company_txt=(EditText)findViewById(R.id.company_txt);
        number_txt=(EditText)findViewById(R.id.number_txt);
        ratedSpeed_txt=(EditText)findViewById(R.id.ratedSpeed_txt);
        input_data=new inputData();
    }
    @Override
    public void onClick(View v) {
        str_company=company_txt.getText().toString();
        str_number=number_txt.getText().toString();
        str_ratedspeed=ratedSpeed_txt.getText().toString();
        input_data.setCom(str_company);
        input_data.setNumber(str_number);
        input_data.setRatedspeed(str_ratedspeed);
        switch (v.getId()){
            case R.id.MeasureBtn:
                if(!str_ratedspeed.isEmpty()){
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(MenuActivity.this, "请输入额定速度！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ScanBtn:
                Intent intent1 = new Intent(this, SaveActivity.class);
                startActivity(intent1);
                finish();
                break;
            default:
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}

