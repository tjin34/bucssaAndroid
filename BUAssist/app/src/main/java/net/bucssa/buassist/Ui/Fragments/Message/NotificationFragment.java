package net.bucssa.buassist.Ui.Fragments.Message;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.bucssa.buassist.Api.PersonalMessageAPI;
import net.bucssa.buassist.Api.SystemMessageAPI;
import net.bucssa.buassist.Base.BaseFragment;
import net.bucssa.buassist.Bean.BaseEntity;
import net.bucssa.buassist.Bean.Message.Chat;
import net.bucssa.buassist.Bean.Message.SystemNotification;
import net.bucssa.buassist.Enum.Enum;
import net.bucssa.buassist.HttpUtils.RetrofitClient;
import net.bucssa.buassist.R;
import net.bucssa.buassist.Ui.Fragments.Message.Adapter.SystemAdapter;
import net.bucssa.buassist.UserSingleton;
import net.bucssa.buassist.Util.Logger;
import net.bucssa.buassist.Util.ToastUtils;
import net.bucssa.buassist.Widget.LuluRefreshListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tjin3 on 2017/11/12.
 */

public class NotificationFragment extends BaseFragment{

    @BindView(R.id.lv_message)
    LuluRefreshListView lv_message;

    private List<SystemNotification> chatList = new ArrayList<>();
    private SystemAdapter myAdapter;
    private int state = Enum.STATE_NORMAL;

    int pageIndex = 1;//当前页
    int pageSize = 10;//每一页数量
    int totalCount = 0;//总数

    private final static int REFRESH_COMPLETE = 0;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    lv_message.setOnRefreshComplete();
                    myAdapter.notifyDataSetChanged();
                    lv_message.setSelection(0);
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_personal_message;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();
    }

    @Override
    protected void initAllMembersView(Bundle savedInstanceState) {

        lv_message.setOnBaiduRefreshListener(new LuluRefreshListView.OnBaiduRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            mHandler.sendEmptyMessage(REFRESH_COMPLETE);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    /**
     * 下拉刷新
     */
    public void refreshData() {
        pageIndex = 1;
        state = Enum.STATE_REFRESH;
        initData();
    }

    /**
     * 上拉刷新
     */
    private void loadMore() {
        pageIndex++;
        state = Enum.STATE_MORE;
        initData();
    }

    private void initData() {
        getSystemMessages();
    }

    private void changeByState() {
        switch (state) {
            case Enum.STATE_NORMAL:
                myAdapter = new SystemAdapter(context, chatList);
                lv_message.setAdapter(myAdapter);
                break;
            case Enum.STATE_REFRESH:
                myAdapter.clearData();
                myAdapter.addData(0, chatList);
                break;
//            case Enum.STATE_MORE:
//                myAdapter.clearData();
//                myAdapter.addItems(chatList, myAdapter.getItemCount());
//                mRefreshLayout.finishRefreshLoadMore();
//                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 101:
                refreshData();
                break;
        }
    }

    private void getSystemMessages() {
        Observable<BaseEntity<List<SystemNotification>>> observable = RetrofitClient.createService(SystemMessageAPI.class)
                .getChats(UserSingleton.USERINFO.getUid(), UserSingleton.USERINFO.getToken(),
                        pageIndex, pageSize);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseEntity<List<SystemNotification>>>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        Logger.d();
                    }

                    @Override
                    public void onCompleted() {
                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        ToastUtils.showToast(context, getString(R.string.snack_message_net_error));
                    }

                    @Override
                    public void onNext(BaseEntity<List<SystemNotification>> baseEntity) {
                        if (baseEntity.isSuccess()) {
                            chatList = baseEntity.getDatas();
                            totalCount = baseEntity.getTotal();
                            changeByState();
                        } else {
                            ToastUtils.showToast(context, baseEntity.getError());
                        }
                        Logger.d();
                    }
                });
    }
}