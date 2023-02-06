package com.ruiguan.base;

import android.app.Application;
import com.ruiguan.R;
import org.xutils.BuildConfig;
import org.xutils.x;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class BaseApplication extends Application {
    private boolean islogin = false;
    @Override
    public void onCreate() {
        super.onCreate();

        //CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/楷体.ttf").setFontAttrId(R.attr.fontPath).build());

       /* CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/楷体.ttf")
                .setFontAttrId(R.attr.fontPath)
                .addCustomViewWithSetTypeface(TypefaceSupport.class)
                .addCustomStyle(TextField.class, R.attr.textFieldStyle)
                .build()
        );*/
        x.Ext.init(this);
        x.Ext.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        x.Ext.setDebug(BuildConfig.DEBUG);
    }
    public boolean isIslogin() {
        return islogin;
    }
    public void setIslogin(boolean islogin) {
        this.islogin = islogin;
    }

}
