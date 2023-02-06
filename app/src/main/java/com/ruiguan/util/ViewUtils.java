package com.ruiguan.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ruiguan.view.abpulltorefreshview.AbListViewFooter;
import com.ruiguan.view.abpulltorefreshview.AbListViewHeader;

/**
 * 视图工具
 */
public class ViewUtils {

    /**  UI设计的基准宽度. */
    public static int UI_WIDTH = 720;
    /**  UI设计的基准高度. */
    public static int UI_HEIGHT = 1280;
    /**  UI设计的密度. */
    public static int UI_DENSITY = 2;
    /** 无效值 */
    public static final int INVALID = Integer.MIN_VALUE;

    /**
     * 设置背景图片
     * @param view
     * @param drawable
     */
    @SuppressWarnings("deprecation")
    public static void setBackgroundDrawable(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        }else {
            view.setBackgroundDrawable(drawable);
        }
    }

    /**
     * 设置PX padding.
     * @param view the view
     * @param left the left padding in pixels
     * @param top the top padding in pixels
     * @param right the right padding in pixels
     * @param bottom the bottom padding in pixels
     */
    public static void setPadding(View view, int left,
                                  int top, int right, int bottom) {
        int scaledLeft = scaleValue(view.getContext(), left);
        int scaledTop = scaleValue(view.getContext(), top);
        int scaledRight = scaleValue(view.getContext(), right);
        int scaledBottom = scaleValue(view.getContext(), bottom);
        view.setPadding(scaledLeft, scaledTop, scaledRight, scaledBottom);
    }

    /**
     * 根据屏幕大小缩放.
     * @return the int
     */
    public static int scaleValue(Context context, float value) {
        DisplayMetrics mDisplayMetrics = getDisplayMetrics(context);
        //为了兼容尺寸小密度大的情况
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        //解决横屏比例问题
        if(width > height){
            width = mDisplayMetrics.heightPixels;
            height = mDisplayMetrics.widthPixels;
        }
        if(mDisplayMetrics.scaledDensity == UI_DENSITY){
            //密度
            if(width > UI_WIDTH){
                value = value*(1.3f - 1.0f/mDisplayMetrics.scaledDensity);
            }else if(width < UI_WIDTH){
                value = value*(1.0f - 1.0f/mDisplayMetrics.scaledDensity);
            }
        }else{
            //密度小屏幕大:缩小比例
            float offset = UI_DENSITY-mDisplayMetrics.scaledDensity;
            if(offset > 0.5f){
                value = value * 0.9f;
            }else{
                value = value * 0.95f;
            }

        }
        return scale(mDisplayMetrics.widthPixels,mDisplayMetrics.heightPixels, value);
    }
    /**
     * 获取屏幕尺寸与密度.
     *
     * @param context the context
     * @return mDisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources mResources;
        if (context == null){
            mResources = Resources.getSystem();

        }else{
            mResources = context.getResources();
        }
        //DisplayMetrics{density=1.5, width=480, height=854, scaledDensity=1.5, xdpi=160.421, ydpi=159.497}
        //DisplayMetrics{density=2.0, width=720, height=1280, scaledDensity=2.0, xdpi=160.42105, ydpi=160.15764}
        DisplayMetrics mDisplayMetrics = mResources.getDisplayMetrics();
        return mDisplayMetrics;
    }
    /**
     * 描述：根据屏幕大小缩放.
     *
     * @param displayWidth the display width
     * @param displayHeight the display height
     * @param pxValue the px value
     * @return the int
     */
    public static int scale(int displayWidth, int displayHeight, float pxValue) {
        if(pxValue == 0 ){
            return 0;
        }
        float scale = 1;
        try {
            int width = displayWidth;
            int height = displayHeight;
            //解决横屏比例问题
            if(width > height){
                width = displayHeight;
                height = displayWidth;
            }
            float scaleWidth = (float) width / UI_WIDTH;
            float scaleHeight = (float) height / UI_HEIGHT;
            scale = Math.min(scaleWidth, scaleHeight);
        } catch (Exception e) {
        }
        return Math.round(pxValue * scale + 0.5f);
    }

    /**
     * 缩放文字大小,这样设置的好处是文字的大小不和密度有关，
     * 能够使文字大小在不同的屏幕上显示比例正确
     * @param textView button
     * @param sizePixels px值
     * @return
     */
    public static void setTextSize(TextView textView, float sizePixels) {
        float scaledSize = scaleTextValue(textView.getContext(),sizePixels);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,scaledSize);
    }

    /**
     * 描述：根据屏幕大小缩放文本.
     * @param context the context
     * @param value the px value
     * @return the int
     */
    public static int scaleTextValue(Context context, float value) {
        return scaleValue(context, value);
    }

    /**
     * 测量这个view
     * 最后通过getMeasuredWidth()获取宽度和高度.
     * @param view 要测量的view
     * @return 测量过的view
     */
    public static void measureView(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight,
                    View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
        }
        view.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * 描述：是否需要Scale.
     * @param view
     * @return
     */
    public static boolean isNeedScale(View view){
        if (view instanceof AbListViewHeader){
            return false;
        }
        if (view instanceof AbListViewFooter){
            return false;
        }
        return true;
    }

    /**
     * 设置View的PX尺寸
     * @param view  如果是代码new出来的View，需要设置一个适合的LayoutParams
     * @param widthPixels
     * @param heightPixels
     */
    public static void setViewSize(View view, int widthPixels, int heightPixels){
        int scaledWidth = scaleValue(view.getContext(), widthPixels);
        int scaledHeight = scaleValue(view.getContext(), heightPixels);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params == null){
            return;
        }
        if (widthPixels != INVALID){
            params.width = scaledWidth;
        }
        if (heightPixels != INVALID && heightPixels!=1){
            params.height = scaledHeight;
        }
        view.setLayoutParams(params);
    }

    /**
     * 设置 PX margin.
     *
     * @param view the view
     * @param left the left margin in pixels
     * @param top the top margin in pixels
     * @param right the right margin in pixels
     * @param bottom the bottom margin in pixels
     */
    public static void setMargin(View view, int left, int top,
                                 int right, int bottom) {
        int scaledLeft = scaleValue(view.getContext(), left);
        int scaledTop = scaleValue(view.getContext(), top);
        int scaledRight = scaleValue(view.getContext(), right);
        int scaledBottom = scaleValue(view.getContext(), bottom);

        if(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams mMarginLayoutParams = (ViewGroup.MarginLayoutParams) view
                    .getLayoutParams();
            if (mMarginLayoutParams != null){
                if (left != INVALID) {
                    mMarginLayoutParams.leftMargin = scaledLeft;
                }
                if (right != INVALID) {
                    mMarginLayoutParams.rightMargin = scaledRight;
                }
                if (top != INVALID) {
                    mMarginLayoutParams.topMargin = scaledTop;
                }
                if (bottom != INVALID) {
                    mMarginLayoutParams.bottomMargin = scaledBottom;
                }
                view.setLayoutParams(mMarginLayoutParams);
            }
        }

    }

    /**
     * 按比例缩放View，以布局中的尺寸为基准
     * @param view
     */
    @SuppressLint("NewApi")
    public static void scaleView(View view){
        if(!isNeedScale(view)){
            return;
        }
        if (view instanceof TextView){
            TextView textView = (TextView) view;
            setTextSize(textView,textView.getTextSize());
        }
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) view.getLayoutParams();
        if (null != params){
            int width = INVALID;
            int height = INVALID;
            if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT
                    && params.width != ViewGroup.LayoutParams.MATCH_PARENT){
                width = params.width;
            }
            if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT
                    && params.height != ViewGroup.LayoutParams.MATCH_PARENT){
                height = params.height;
            }
            setViewSize(view,width,height);
            setPadding(view,view.getPaddingLeft(),view.getPaddingTop(),view.getPaddingRight(),view.getPaddingBottom());
        }
        // Margin
        if(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams mMarginLayoutParams = (ViewGroup.MarginLayoutParams) view
                    .getLayoutParams();
            if (mMarginLayoutParams != null){
                setMargin(view,mMarginLayoutParams.leftMargin,mMarginLayoutParams.topMargin,mMarginLayoutParams.rightMargin,mMarginLayoutParams.bottomMargin);
            }
        }
        if(Build.VERSION.SDK_INT>=16){
            //最大最小宽高
            int minWidth = scaleValue(view.getContext(),view.getMinimumWidth());
            int minHeight = scaleValue(view.getContext(),view.getMinimumHeight());
            view.setMinimumWidth(minWidth);
            view.setMinimumHeight(minHeight);
        }
    }

    public static int dp2px(Context context, int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, int spValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,spValue,context.getResources().getDisplayMetrics());
    }
}
