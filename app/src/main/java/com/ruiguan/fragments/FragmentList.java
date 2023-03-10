package com.ruiguan.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ruiguan.R;
import com.ruiguan.commonAdapter.ItemClickListener;
import com.ruiguan.dialog.CommonDialog;
import com.ruiguan.fragments.threadPool.GetModuleDate;
import com.ruiguan.fragments.threadPool.HCThreadPool;
import com.ruiguan.fragments.threadPool.ModuleMessage;
import com.ruiguan.fragments.threadPool.PostTest;
import com.ruiguan.fragments.threadPool.ThreadCallBack;
import com.ruiguan.recyclerAdapter.FragmentListAdapter;
import com.ruiguan.recyclerAdapter.Resou;
import com.ruiguan.storage.DataMemory;
import com.test.baselibrary.ioc.CheckNet;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.ConnectInternetManage;
import com.test.connectservicelibrary.connectInternet.JsonsRootBean;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentList extends Fragments{

    @ViewById(R.id.fragment_list_recycler)
    private RecyclerView mRecyclerView;

    @ViewById(R.id.fragment_list_ip)
    private TextView mServiceIpTv;

    @ViewById(R.id.fragment_list_port)
    private TextView mServicePortTv;

    @ViewById(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @ViewById(R.id.fragment_list_scan)
    private FrameLayout mScanFragment;

    @ViewById(R.id.fragment_list_group)
    private LinearLayout mButtonGroup;

    private FragmentManage mFragmentManage;
    private final int mFragmentId = 32117;

    private LocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private List<Resou> mDatas =new ArrayList<>();

    private Handler mListenerHandler = new Handler();

    private FragmentListAdapter mListAdapter;

    private ConnectInternetManage mConnectInternetManage;//???????????????????????????

    private DataMemory mDataMemory;

    private boolean mSetServiceMess = true;

    private final String mServiceIP = "120.25.163.9";

    public static boolean mConnectWork = false;//????????????????????????

    private int mSucceedNumber = 0,mErrorNumber = 0;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list,container,false);
        ViewUtils.inject(view,this);

        initAll();//???????????????

        testServersPost();//?????????????????????

        updateRecycler();//??????recycler

        setClickListener();//??????????????????

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initAll() {
        FragmentSingMessage.getFragmentSingMessage().setReceiveFragConnectHandler(mServiceHandler);
        initFragments();
        initBroadcast();
        initService();
        initRefresh();
        initRecycler();
    }

    private void initFragments() {
        mFragmentManage = new FragmentManage(R.id.fragment_list_scan);
        mFragmentManage.initFragment(mFragmentId,new FragmentScan(),getActivity().getSupportFragmentManager().beginTransaction());
    }

    private void initRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//?????????????????????
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                refresh();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initService() {
        mConnectInternetManage = new ConnectInternetManage(getContext(),
                mServiceIP,mServiceHandler);
        mDataMemory = new DataMemory(Objects.requireNonNull(getContext()));
        mServiceIpTv.setText(mDataMemory.getServiceIp());
    }

    private void initRecycler(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        //????????????
        mListAdapter= new FragmentListAdapter(getContext(), mDatas, R.layout.fragment_item);
        mRecyclerView.setAdapter(mListAdapter);
    }
    private void initData(){
        //mDatas = FragmentSingMessage.getFragmentSingMessage().getModelList();
        /*mDatas.add(new Resou("192.168.1.121","111",true));
        mDatas.add(new Resou("192.168.1.120","222",true));
        mDatas.add(new Resou("192.168.1.118","333",true));
        mDatas.add(new Resou("192.168.1.125","444",true));
        mDatas.add(new Resou("192.168.1.126","555",true));
        mDatas.add(new Resou("192.168.1.127","666",true));
        mDatas.add(new Resou("192.168.1.128","777",true));
        mDatas.add(new Resou("192.168.1.12","888",true));
        mDatas.add(new Resou("192.168.1.13","999",true));
        mDatas.add(new Resou("192.168.1.14","000",true));
        mDatas.add(new Resou("192.168.1.11","aaa",true));
        mDatas.add(new Resou("192.168.1.10","bbb",true));
        mDatas.add(new Resou("192.168.1.9","ccc",true));*/
        mDatas.addAll(FragmentSingMessage.getFragmentSingMessage().getModelList());
        if (mDatas.size() == 0)
            toast("?????????????????????????????????????????????????????????????????????");
    }

    private void initBroadcast() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.test.huichengwifi.STOP_BROADCAST");
        mIntentFilter.addAction("com.test.huichengwifi.ContinueSetup");
        mLocalReceiver = new LocalReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(mLocalReceiver,mIntentFilter);
    }

    private void testServersPost() {
        PostTest.TextPostCallback textPostCallback = new PostTest.TextPostCallback() {
            @Override
            public void state(boolean state) {
                if (state) {
                    mServicePortTv.setText(mDataMemory.getServicePort());
                    mButtonGroup.setVisibility(View.GONE);
                } else {
                    toast(mDataMemory.getServicePort() + "???????????????");
                }
            }
        };
        if (mDataMemory.getServiceIp()!=null && mDataMemory.getServicePort()!=null)
            new PostTest(mDataMemory.getServiceIp(),mDataMemory.getServicePort(),textPostCallback,getActivity());
    }

    //????????????????????????
    private void refresh(){
        if (mConnectWork){
            toast("???????????????????????????..");
            return;
        }
        mDatas.clear();
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mScanFragment.setVisibility(View.VISIBLE);
        mFragmentManage.delete(mFragmentId,getActivity().getSupportFragmentManager().beginTransaction());
        mFragmentManage.initFragment(mFragmentId,new FragmentScan(),getActivity().getSupportFragmentManager().beginTransaction());
    }

    @CheckNet
    @OnClick(R.id.fragment_list_get_service)
    private void getService(){
        if (mConnectWork){
            toast("???????????????????????????????????????...");
            return;
        }
        toast("??????..");
        mConnectInternetManage.getServiceMessage();
    }

    private boolean isConnectService = false;//?????????????????????????????????????????????????????????????????????
    @CheckNet
    @OnClick(R.id.fragment_list_connect)
    private void connectService(){

        if (mConnectWork){
            toast("???????????????????????????????????????...");
            return;
        }
        if (mServiceIpTv.getText().toString().length()<"1.1.1.1".length() || mServicePortTv.getText().toString().isEmpty()){
            toast("??????????????????????????????????????????IP?????????");
            return;
        }

        log("????????????????????????????????????");
        int clickNumber = 0;
        for (Resou mData : mDatas) {
            if (mData.getClickBoolean())
                clickNumber++;
        }
        if (clickNumber == 0){
            toast("??????????????????????????????");
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(mServiceIpTv.getText().toString());
        list.add(mServicePortTv.getText().toString());
        sendHandler(0x10,list);
        isConnectService = true;
    }

    @OnClick(R.id.fragment_list_disconnect)
    private void disconnectService(){
        if (mConnectWork){
            toast("???????????????????????????????????????...");
            return;
        }
        final List<String> list = new ArrayList<>();
        List<Resou> models = new ArrayList<>();
        for (Resou mData : mDatas) {
            if(mData.getClickBoolean()){
                list.add(mData.getmIP());
                models.add(mData);
                mData.setState(1);
            }
        }
        mDatas = models;
        if (models.size() == 0){
            toast("??????????????????????????????");
            return;
        }
        initRecycler();
        toast("????????????????????????????????????????????????"+((models.size()/5+1)*2)+"??????");
        new HCThreadPool(list,mThreadCallBack, getContext());
        mConnectWork = true;
        mSetServiceMess = false;
    }

    @OnClick(R.id.fragment_list_more)
    private void more(){
        if (mConnectWork){
            toast("???????????????????????????????????????...");
            return;
        }
        Map<String,String> map = mDataMemory.getServersMap();//*
        if (map == null||map.size()<2){
            toast("????????????????????????????????????????????????");
            return;
        }
        CommonDialog.Builder builder = new CommonDialog.Builder(getContext());
        builder.setView(R.layout.fragment_more_vessel).fullWidth().create().show();
        MoreServersMessage setupMessage = builder.getView(R.id.fragment_more_id);
        setupMessage.setBuilder(builder).setServersMessage(mServiceIpTv,mServicePortTv);
    }

    private Handler mServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0x01){
                JsonsRootBean jsonsRootBean = (JsonsRootBean) msg.obj;
                if (jsonsRootBean.getErrCode()!=0){
                    toast("?????????????????????...");
                    return false;
                }
                mServiceIpTv.setText(mServiceIP);
                mServicePortTv.setText(String.valueOf(jsonsRootBean.getIntPort()));
            }
            if (msg.what == 0x04 && isConnectService){
                if(msg.obj.toString().equals("connected")){
                    setServiceMessage();
                }else if (msg.obj.toString().equals("delConnect")){
                    toast("??????IP?????????");
                }
            }
            if (msg.what == 0x08){
                toast(msg.obj.toString());
            }
            return false;
        }
    });

    //???????????????????????????
    private ThreadCallBack mThreadCallBack = new ThreadCallBack() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void accomplish() {
            log("??????????????????...");
            getMessageWindows();//?????????????????????????????????
        }

        @Override
        public void setOk(String ip) {
            setUpdateRecyclerView(ip,3);//???????????????????????????
            ++mSucceedNumber;
        }

        @Override
        public void setError(String ip) {
            setUpdateRecyclerView(ip,4);//???????????????????????????
            ++mErrorNumber;
        }

        @Override
        public void dispose(String ip) {
            setUpdateRecyclerView(ip,2);//????????????????????????
        }
    };

    private ModuleMessage mModuleMessage = new ModuleMessage() {
        @Override
        public void getMessage(String ip, String post, int position) {
            mDatas.get(position).setServersIP(ip);
            mDatas.get(position).setServersPost(post);
            mListAdapter.notifyDataSetChanged();
        }

        @Override
        public void error(String e, int position) {
            log("e:"+e);
            mDatas.get(position).setServersIP("????????????(1.6??????????????????AT????????????)");
            mDatas.get(position).setServersPost("????????????");
            mListAdapter.notifyDataSetChanged();
        }
    };


    private void setServiceMessage(){
        final List<String> list = new ArrayList<>();
        List<Resou> models = new ArrayList<>();
        for (Resou mData : mDatas) {
            if(mData.getClickBoolean()){
                mData.setState(1);
                models.add(mData);
                list.add(mData.getmIP());
            }
        }
        mDatas = models;
        initRecycler();
        mSetServiceMess = true;
        mConnectWork = true;
        String names = "";
        toast("????????????????????????????????????????????????"+((models.size()/5+1)*2)+"??????");
        for (Resou model : models) {
            names+="??????: "+model.getmStr()+"\n"+"IP: "+model.getmIP()+"\n"+"\n";
        }
        mDataMemory.saveServicePort(mServiceIpTv.getText().toString(),
                mServicePortTv.getText().toString(),names);//??????IP????????????
        new HCThreadPool(list,mThreadCallBack,mServiceIpTv.getText().toString(),
                mServicePortTv.getText().toString(),getContext());
    }

    private void updateRecycler(){
        mRecyclerView.setAdapter(mListAdapter);
        setClickListener();
    }

    private void setClickListener(){
        mListenerHandler.post(new Runnable() {
            @Override
            public void run() {
                showListData();
            }
        });
    }

    //????????????????????????????????????
    private void getMessageWindows(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (mSetServiceMess) {
                    intent = new Intent("com.test.huichengwifi.STOP_LIST");
                    String string = mServiceIpTv.getText().toString().equals(mServiceIP)?"?????????????????????":mServiceIpTv.getText().toString();
                    intent.putExtra("message",string+":"+mServicePortTv.getText().toString());
                } else {
                    intent = new Intent("com.test.huichengwifi.DISSERVICE");
                }
                mConnectWork = false;
                isConnectService = false;
                CommonDialog.Builder builder = new CommonDialog.Builder(getContext());
                builder.setView(R.layout.fragment_exit_vessel).fullWidth().create().show();
                SetupMessage setupMessage = builder.getView(R.id.setup_message);
                setupMessage.setBuilder(builder).hideHint(mSetServiceMess).setMessageTv(mSucceedNumber,mErrorNumber).setMessageIntent(intent);
            }
        });
    }

    private void setUpdateRecyclerView(String ip,int state){
        for (Resou mData : mDatas) {
            if (mData.getmIP().equals(ip)){
                mData.setState(state);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    //item ????????????
    private void showListData(){
        mRecyclerView.setAdapter(mListAdapter);
        //??????????????????
        mListAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(final int position, View view) {
                switch (view.getId()){
                    case R.id.fragment_item_message:
                        getModuleMessage(position);
                        break;
                    case R.id.fragment_item_unfold:
                        getModuleMessage(position);
                        mDatas.get(position).setHintLinearLayout(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case R.id.fragment_item_hide:
                        mDatas.get(position).setHintLinearLayout(View.GONE);
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case R.id.fragment_item_fill:
                        if (!(mDatas.get(position).getServersPost().equals("????????????????????????")||
                                mDatas.get(position).getServersPost().equals("????????????")))
                            mServicePortTv.setText(mDatas.get(position).getServersPost());
                        else
                            toast("?????????????????????");
                            break;
                        default:
                            mDatas.get(position).setClick();
                            mListAdapter.notifyDataSetChanged();
                            setButtonGroup();
                }
            }
        });
    }

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()!= null && intent.getAction().equals("com.test.huichengwifi.STOP_BROADCAST")) {
                //?????????????????????FragmentList??????
                //mFragmentManage.delete(mFragmentId, getActivity().getSupportFragmentManager().beginTransaction());
                mScanFragment.setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                initData();
                updateRecycler();
            }
            if (intent.getAction()!=null && intent.getAction().equals("com.test.huichengwifi.ContinueSetup")){
                toast("??????????????????????????????????????????");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }
    }

    private void getModuleMessage(final int position){
        mDatas.get(position).setServersPost("");
        mDatas.get(position).setServersIP("?????????...");
        mListAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GetModuleDate().postJson(mDatas.get(position).getmIP(),position,mModuleMessage,getActivity());
            }
        }).start();
    }

    private void setButtonGroup(){
        if (mServiceIpTv.getText().toString().length()<"1.1.1.1".length() ||
                mServicePortTv.getText().toString().isEmpty()){
            mButtonGroup.setVisibility(View.VISIBLE);
            return;
        }
        for (Resou mData : mDatas) {
            if (mData.getClickBoolean()){
                mButtonGroup.setVisibility(View.VISIBLE);
                return;
            }
        }
        mButtonGroup.setVisibility(View.GONE);
    }

    private void log(String log){
        Log.d("AppRunFragmentList",log);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatas.clear();
        FragmentSingMessage.getFragmentSingMessage().clearModelList();
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
    }
}
