package com.ruiguan.popupWindows;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruiguan.DebugActivity;
import com.ruiguan.R;
import com.ruiguan.dialog.CommonDialog;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

public class ExplainMenu extends LinearLayout{

    @ViewById(R.id.set_permission)
    private TextView setPermission;

    @ViewById(R.id.explain_not_miui)
    private ScrollView notMiui;

    @ViewById(R.id.explain_miui)
    private ScrollView miui;

    @ViewById(R.id.set_permission_line)
    private View setPermissionLine;

    private CommonDialog.Builder builder;

    private int number = 0;

    public ExplainMenu(Context context) {
        this(context,null);
    }

    public ExplainMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ExplainMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context,R.layout.explain_menu,this);
        ViewUtils.inject(this);
        initData();
    }

    private void initData() {
        if (isMIUI()){
            setPermission.setVisibility(VISIBLE);
            setPermissionLine.setVisibility(VISIBLE);
            notMiui.setVisibility(GONE);
            miui.setVisibility(VISIBLE);
        }
    }

    @OnClick({R.id.set_permission,R.id.explain_text})
    public void onClick(View v) {
        if (v.getId() == R.id.set_permission) {
            if (builder !=null)
                builder.dismiss();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
            intent.setData(uri);
            try {
                ((Activity)getContext()).startActivityForResult(intent, 10);
            } catch (Exception e) {
                Log.e("TAG", "------????????????-----");
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(getContext(), "???????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }

        if(v.getId() == R.id.explain_text){
            number++;
            if (number > 5) {
                if (builder!=null)
                    builder.dismiss();
                Intent intent = new Intent(getContext(), DebugActivity.class);
                getContext().startActivity(intent);
            }
        }
    }

    public void setBuilder(CommonDialog.Builder builder) {
        this.builder = builder;
    }

    private static boolean isMIUI() {
        String manufacturer = Build.MANUFACTURER;
        //?????????????????????????????????,???????????????????????????huawei,???????????????meizu
        if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            return true;
        }
        return "hongmi".equalsIgnoreCase(manufacturer);
    }

}
