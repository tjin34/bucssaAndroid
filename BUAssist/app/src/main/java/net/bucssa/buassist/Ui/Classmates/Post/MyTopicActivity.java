package net.bucssa.buassist.Ui.Classmates.Post;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import net.bucssa.buassist.Api.ClassmateAPI;
import net.bucssa.buassist.Base.BaseActivity;
import net.bucssa.buassist.Bean.BaseEntity;
import net.bucssa.buassist.Bean.Classmate.Post;
import net.bucssa.buassist.Enum.Enum;
import net.bucssa.buassist.HttpUtils.RetrofitClient;
import net.bucssa.buassist.R;
import net.bucssa.buassist.Ui.Classmates.Adapter.PostListAdapter;
import net.bucssa.buassist.UserSingleton;
import net.bucssa.buassist.Util.Logger;
import net.bucssa.buassist.Util.ToastUtils;
import net.bucssa.buassist.Widget.CustomListViewForRefreshView;
import net.bucssa.buassist.Widget.LuluRefreshListView;
import net.bucssa.buassist.Widget.RefreshHelper;
import net.bucssa.buassist.Widget.RefreshView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by KimuraShin on 17/7/29.
 */

public class MyTopicActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.search)
    LinearLayout ll_search;

    @BindView(R.id.searchShadow)
    View searchShadow;

    @BindView(R.id.search_textView)
    LinearLayout fakeSearchBox;

    @BindView(R.id.tv_search)
    TextView tv_search;

    @BindView(R.id.et_search)
    EditText et_search;

    @BindView(R.id.search_editText)
    LinearLayout realSearchBox;

    @BindView(R.id.tv_cancel)
    TextView tv_cancel;

    @BindView(R.id.listView)
    ListView listView;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    @BindView(R.id.ivLulu)
    ImageView ivLulu;

    private List<Post> postList = new ArrayList<>();
    private PostListAdapter myAdapter;

    private int state = Enum.STATE_NORMAL;

    private int pageIndex = 1;
    private int pageSize = 10;
    private int totalCount = 0;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_refresh;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Override
    protected void initResAndListener() {
        tv_title.setText("我的话题");
        iv_back.setVisibility(View.VISIBLE);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ll_search.setVisibility(View.GONE);
        searchShadow.setVisibility(View.GONE);

        Glide.with(mContext)
                .asGif()
                .load(R.raw.pull)
                .into(ivLulu);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Glide.with(mContext)
                        .asGif()
                        .load(R.raw.refreshing)
                        .into(ivLulu);
                refreshData();
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                Glide.with(mContext)
                        .asGif()
                        .load(R.raw.pull)
                        .into(ivLulu);
                loadMore();
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
        postList = new ArrayList<>();
        getPost(pageIndex, pageSize);
    }


    private void changeByState() {
        switch (state) {
            case Enum.STATE_NORMAL:
                myAdapter = new PostListAdapter(mContext, postList);
                listView.setAdapter(myAdapter);
                break;
            case Enum.STATE_REFRESH:
                myAdapter.clear();
                myAdapter.addDatas(postList);
                refreshLayout.finishRefresh(1000);
                break;
            case Enum.STATE_MORE:
                if (postList.size() == 0) {
                    refreshLayout.finishLoadMore(1000);
                    ToastUtils.showToast(mContext, "已加载全部话题！");
                    break;
                }
                myAdapter.addDatas(postList);
                refreshLayout.finishLoadMore(1000);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshData();
    }

    private void getPost(int pageIndex, int pageSize){
        Observable<BaseEntity<List<Post>>> observable = RetrofitClient.createService(ClassmateAPI.class)
                .getPost(UserSingleton.USERINFO.getUid(),0, pageIndex, pageSize,UserSingleton.USERINFO.getToken());

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseEntity<List<Post>>>() {
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
                        ToastUtils.showToast(mContext, getString(R.string.snack_message_net_error));
                    }

                    @Override
                    public void onNext(BaseEntity<List<Post>> baseEntity) {
                        if (baseEntity.isSuccess()) {
                            if (baseEntity.getDatas() != null)
                                postList = baseEntity.getDatas();
                            totalCount = baseEntity.getTotal();
                            changeByState();
                        } else {
                            ToastUtils.showToast(mContext, baseEntity.getError());
                        }
                        Logger.d();
                    }
                });
    }
}
