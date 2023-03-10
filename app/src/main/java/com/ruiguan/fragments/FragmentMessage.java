package com.ruiguan.fragments;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ruiguan.ImplantFragment;
import com.ruiguan.R;
import com.ruiguan.commonAdapter.ItemClickListener;
import com.ruiguan.commonAdapter.MulitiTypeSupport;
import com.ruiguan.commonAdapter.RecyclerCommonAdapter;
import com.ruiguan.commonAdapter.ViewHolder;
import com.ruiguan.dialog.CommonDialog;
import com.ruiguan.popupWindows.ServiceHint;
import com.ruiguan.recyclerAdapter.Resou;
import com.ruiguan.singleton.SingMessage;
import com.test.baselibrary.ioc.CheckNet;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.JsonsRootBean;


import java.util.ArrayList;
import java.util.List;

import static com.test.connectservicelibrary.connectInternet.ToolClass.changeHexString;
public class FragmentMessage extends Fragments implements ServiceHint.ServiceHintCallback {
    public static String mIp = "192.168.4.1";
    @ViewById(R.id.edit_sends)
    private EditText mEditTextSend;
    @ViewById(R.id.edit_sends_hex)
    private EditText mEditTextSendHex;
    private EditText mSendET;
    @ViewById(R.id.myRecycler)
    private RecyclerView mRecyclerView;
    @ViewById(R.id.circlelight)
    private ImageView mImageViewCircle;
    @ViewById(R.id.lighttip)
    private TextView mCircleLight;
    @ViewById(R.id.fragment_getdata)
    private TextView mReceiveTv;
    @ViewById(R.id.fragment_senddata)
    private TextView mSendNumber;
    @ViewById(R.id.fragment_click_accept)
    private ImageView mClickAcceptImage;
    @ViewById(R.id.fragment_click_send)
    private ImageView mClickSendImage;
    @ViewById(R.id.fragment_internet)
    private TextView mConnectInternetTv;
    private List<Resou> mDatas =new ArrayList<>();
    public static int mSendsNumber = 0;
    public static boolean mIsConnectInternet = false;
    public static boolean mSendHex = true;
    public static boolean mAccept = true;
    private SingMessage mSingMessage = SingMessage.getSingMessage();
    private Handler mTimeHandler = new Handler();
    private CommonDialog.Builder mServiceBuilder;
    private boolean mServiceError = false;
    private JsonsRootBean mJsonRootBean;
    public static boolean mConnectModule = false;//custom?????????????????????
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message,container,false);
        ViewUtils.inject(view,this);
        initAll();
        return view;
    }

    private void initAll() {
        FragmentSingMessage.getFragmentSingMessage().setReceiveFragMessageHandler(mReceiveFragMessageHandler);
        initRecycler();
        initHex();
        initConnectTime();
    }

    private void initRecycler(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        //????????????
        mRecyclerView.setAdapter(new RecyclerAdapter(this.getActivity(),mDatas));
    }

    private void initHex() {
        if (mAccept){
            acceptHex();
        }
        if (mSendHex){
            sendHex();
        }
        mSendET = mEditTextSend;
        /*if (mConnectInternetManage == null)
            return;
        mConnectInternetManage.setAccept(mAccept);
        mConnectInternetManage.setSendHex(mSendHex);*/
    }

    private void initConnectTime(){
        mIsConnectInternet = false;
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startServiceHint(4,null);
            }
        },4000);
    }

    private Handler mReceiveFragMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0x00){
                try {
                    updateRecycler(msg.obj.toString());//??????Recycler
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if (msg.what == 0x01){
                mJsonRootBean = (JsonsRootBean) msg.obj;//??????????????????????????????????????????IP???
            }

            if (msg.what == 0x03){
                updateReceiveNumber(msg.obj.toString());//????????????????????????
            }

            if (msg.what == 0x04) {
                setIsOnLine(msg.obj.toString());//????????????????????????????????????
            }

            if (msg.what == 0x07){
                //????????????????????????????????????  --> ?????????????????????????????????
                mTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendHandler(0x02,null);
                    }
                },3500);
            }
            if (msg.what == 0x08){
                //?????????????????????????????????
                mServiceError = true;
                connectInternet();
                startServiceHint(1,msg.obj.toString());
            }
            if (msg.what == 0x09){
                //????????????????????????
                mServiceError = true;
                if (mServiceBuilder != null)
                    mServiceBuilder.dismiss();
                if (msg.obj.toString().equals("??????????????????????????????????????????????????????app???????????????")){
                    connectInternet();
                }
                startServiceHint(1,msg.obj.toString());
            }
            if (msg.what == 0x10){
                if(msg.obj.toString().equals("1")){
                    //?????????????????? --> ????????????
                    mSingMessage.setServiceProgressBarEvolve(100,850);
                }

                if(msg.obj.toString().equals("2")){
                    //??????????????? --> ???????????????
                    mSingMessage.setServiceProgressBarEvolve(900,950);
                }

                if (msg.obj.toString().equals("3")){
                    //?????????????????????????????????????????????  -->3.5???
                    mSingMessage.setServiceProgressBarEvolve(650,970);
                }

                if (msg.obj.toString().equals("4")){
                    mServiceBuilder.dismiss();
                    startServiceHint(1,"????????????????????????????????????????????????????????????3??????????????????");
                    mTimeHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                           sendHandler(0x09,null);//??????WiFi????????????????????????
                        }
                    },3000);
                }
            }

            if (msg.what == 0x11){
                mImageViewCircle.setBackground(setDrawable(R.drawable.circle3));
                mCircleLight.setText(" ????????????..");
                mConnectInternetTv.setVisibility(View.INVISIBLE);
            }
            return false;
        }
    });

    //????????????????????????
    private void setIsOnLine(String state){
        if (state.equals("connected")){
            //?????????drawable????????????
            mConnectModule = true;
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle1));
            mCircleLight.setTextColor(Color.parseColor("#7E7E7E"));
            mCircleLight.setText(" ?????????");
            mTimeHandler.removeMessages(0);
            if (mServiceBuilder != null) {
                mServiceBuilder.dismiss();
                if (mSingMessage.getProgressBarMax() == 950) {//????????????????????????
                    String string = "\n????????????IP??????"+mJsonRootBean.getAddress()+"\n"+"????????????????????????: "+mJsonRootBean.getPort();
                    mDatas.add(new Resou(mIp,"?????????????????????????????????????????????"+string));
                    initRecycler();//????????????
                    mRecyclerView.setAdapter(
                            new RecyclerAdapter(getContext(),mDatas));
                }
                mSingMessage.setServiceProgressBarEvolve(1000,1000);
                mSingMessage.setServiceProgressBarNull();
            }
        }else {
            if (mCircleLight.getText().toString().equals(" ????????????.."))
                return;
            //?????????drawable????????????
            mConnectModule = false;
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle2));
            mCircleLight.setTextColor(Color.parseColor("#AE071B"));
            mCircleLight.setText(" ?????????..");
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateReceiveNumber(String data){
        mReceiveTv.setText("R: "+data);
        ImplantFragment.mAcceptNumber = Integer.parseInt(data);
    }

    //????????????????????????
    @OnClick(R.id.fragment_internet)
    @CheckNet
    private void connectInternet(){
        String modelIP = "192.168.4.1";
        if (modelIP.equals(mIp)){
            toast("?????????????????????????????????????????????,?????????????????????????????????");
            return;
        }
        if (!mIsConnectInternet){//???????????????
            mServiceError = false;
            startServiceHint(0,"");
            mSingMessage.setServiceProgressBarEvolve(0,100);
            mConnectInternetTv.setBackground(setDrawable(R.drawable.backbox_true));
            mConnectInternetTv.setText("???????????????");
            mIsConnectInternet = true;
            sendHandler(0x01, mReceiveTv);//???????????????
        }else {//???????????????
            startServiceHint(3,"");
            mSingMessage.setServiceProgressBarEvolve(0,650);
            sendHandler(0x03,null);//???????????????
            mConnectInternetTv.setBackground(setDrawable(R.drawable.backbox_false));
            mConnectInternetTv.setText("???????????????");
            mIsConnectInternet = false;
            mDatas.clear();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(
                    new RecyclerAdapter(getContext(),mDatas));
        }
    }

    //??????????????????
    @SuppressLint("SetTextI18n")
    @OnClick(R.id.btn_send)
    private void sendClick(View view){
        sendHandler(0x00, mSendET.getText().toString());
        Log.e("Scale",mSendET.getText().toString());
        int number = mSendET.getText().toString().length();
        if (mSendHex)
            number = number/3;
        else
            number = changeHexString(true,mSendET.getText().toString()).length()/3;
        mSendsNumber += number;
        mSendNumber.setText("S: "+mSendsNumber);
    }

    //?????????????????????
    @OnClick(R.id.dele_edit)
    private void clearInputBox(View view){
        mSendET.setText(null);
    }

    //??????Recycler
    @OnClick(R.id.dele_recy)
    private void clearRecycler(View view){
        if(mDatas!=null) {
            mDatas.clear();
            sign = true;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(
                    new RecyclerAdapter(getContext(), mDatas));
        }
    }

    //??????????????????Hex???????????????
    @OnClick({R.id.fragment_clickaccept,R.id.fragment_click_accept,
            R.id.fragment_clicksend,R.id.fragment_click_send})
    private void sendAcceptPatternClick(View v){
        switch (v.getId()){
            case R.id.fragment_click_accept:
                acceptHex();
                break;
            case R.id.fragment_clickaccept:
                acceptHex();
                break;
            case R.id.fragment_click_send:
                sendHex();
                break;
            case R.id.fragment_clicksend:
                sendHex();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void getHex(){
        FragmentMessage.mAccept = !FragmentMessage.mAccept;
        acceptHex();
        FragmentMessage.mSendHex = !FragmentMessage.mSendHex;
        sendHex();
        mSendNumber.setText("S: "+FragmentMessage.mSendsNumber);
        mReceiveTv.setText("R: "+ImplantFragment.mAcceptNumber);
    }

    private void acceptHex(){

        if(mAccept){
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_false));
            mAccept = false;
        }else {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_true));
            mAccept = true;
        }
        if (mIsConnectInternet)
            sendHandler(0x05,"Accept");
        if (ImplantFragment.mIsAllConnectService)
            sendHandler(0x05,"Accept");
    }

    private void sendHex(){
        /*if (isOnLongClick){
            toast("???????????????????????????");
            return;
        }*/
        if(mSendHex){
            mClickSendImage.setBackground(setDrawable(R.drawable.circle_click_false));
            mSendET = null;
            mEditTextSend.setVisibility(View.VISIBLE);
            mEditTextSendHex.setVisibility(View.GONE);
            mEditTextSend.setText(changeHexString(false,mEditTextSendHex.getText().toString()));
            mSendET = mEditTextSend;
            mSendHex = false;
        }else {
            mClickSendImage.setBackground(setDrawable(R.drawable.circle_click_true));
            mSendET = null;
            mEditTextSend.setVisibility(View.GONE);
            mEditTextSendHex.setVisibility(View.VISIBLE);
            mEditTextSendHex.setText(changeHexString(true,mEditTextSend.getText().toString()));
            mSendET = mEditTextSendHex;
            mSendHex = true;
        }
        if (mIsConnectInternet)
            sendHandler(0x05,"SendHex");
        if (ImplantFragment.mIsAllConnectService)
            sendHandler(0x05,"SendHex");
    }

    private void startServiceHint(int function,String errorMessage){
        if (function == 3 && mServiceError){
            return;
        }
        try {
            mSingMessage.setServiceManage(function);
            mServiceBuilder = new CommonDialog.Builder(getContext());
            mServiceBuilder.setView(R.layout.service_vessel).fullWidth().create().show();
            ServiceHint mServiceHint = mServiceBuilder.getView(R.id.service);
            if (function == 1)
                mServiceHint.setErrorMessage(errorMessage);
            mServiceHint.setServiceHintCallback(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //????????????
    private Drawable setDrawable(int drawable){
        Resources resources;
        try {
            resources = getActivity().getBaseContext().getResources();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return resources.getDrawable(drawable);
    }

    private boolean sign = true;
    private void initRecyclerState(){
        int i ;
        if(versions()){
            i = 14;
        }else {
            i = 8;
        }
        if(mDatas.size()>=i && sign) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(layoutManager);
            sign = false;
            Log.d("TAG_CONNEECT","????????????...");
        }
    }

    private void updateRecycler(String data){
        mDatas.add(new Resou(mIp,data));
        initRecyclerState();
        mRecyclerView.setAdapter(
                new RecyclerAdapter(getContext(),mDatas));
    }

    @Override
    public void stopWindows() {
        mServiceBuilder.dismiss();
    }

    @Override
    public void forceExit(int function) {
        if(function == 2) {
            //????????????
            mServiceBuilder.dismiss();
            mIsConnectInternet = false;
            sendHandler(0x09,null);
        }else if(function == 4){
            //????????????,??????????????????
            mServiceBuilder.dismiss();
            startServiceHint(3,"");
            mSingMessage.setServiceProgressBarEvolve(0,650);
            sendHandler(0x03,null);
        }
    }

    private  class RecyclerAdapter extends RecyclerCommonAdapter<Resou> {

        RecyclerAdapter(Context context, List<Resou> list) {
            super(context, list, new MulitiTypeSupport<Resou>() {
                @Override
                public int getLayoutId(Resou item) {
                    return R.layout.item_chat_friend;
                }
            });
        }

        @Override
        protected void convert(ViewHolder holder, Resou item, int position, ItemClickListener itemClickListener) {
            holder.setText(R.id.chat_text, item.getmStr());
        }
    }

    /***
     * ???????????????Android6.0??????
     * ***/
    private Boolean versions(){
        //Build.VERSION_CODES.M   Android6.0 ????????????
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    //???fragment add ???hide???????????????????????????
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            getHex();
            sendHandler(0x06,mReceiveTv);
            if (ImplantFragment.mIsAllConnectService){
                mConnectInternetTv.setVisibility(View.INVISIBLE);
            }
        }
    }
}
