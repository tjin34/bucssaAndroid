package net.bucssa.buassist.Ui.Classmates;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.bucssa.buassist.Api.ClassmateAPI;
import net.bucssa.buassist.Base.BaseActivity;
import net.bucssa.buassist.Bean.BaseEntity;
import net.bucssa.buassist.Bean.Classmate.Class;
import net.bucssa.buassist.HttpUtils.RetrofitClient;
import net.bucssa.buassist.R;
import net.bucssa.buassist.Ui.Classmates.Adapter.ClassListAdapter;
import net.bucssa.buassist.Ui.Classmates.Class.ClassDetailActivity;
import net.bucssa.buassist.Ui.Classmates.Class.FindClassActivity;
import net.bucssa.buassist.Ui.Classmates.Group.MyGroupActivity;
import net.bucssa.buassist.Ui.Classmates.Group.TopRankActivity;
import net.bucssa.buassist.Ui.Classmates.Post.MyTopicActivity;
import net.bucssa.buassist.UserSingleton;
import net.bucssa.buassist.Util.Logger;
import net.bucssa.buassist.Util.ToastUtils;

import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by KimuraShin on 17/7/29.
 */

public class ClassmateActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.lv_class)
    ListView lv_class;

    @BindView(R.id.tv_notification)
    TextView tv_notification;

    @BindView(R.id.tv_myGroup)
    TextView tv_myGroup;

    @BindView(R.id.tv_myTopic)
    TextView tv_myTopic;

    @BindView(R.id.tv_topRank)
    TextView tv_topRank;

    @BindView(R.id.iv_addClass)
    ImageView iv_addClass;

    private List<Class> classList;
    private int totalCount = 0;

    private ClassListAdapter myAdapter;

    private static final int ADD_CLASS = 101;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_classmate_final;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initResAndListener() {
        tv_title.setText("大腿课友");
        iv_back.setVisibility(View.VISIBLE);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_addClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FindClassActivity.class);
                intent.putExtra("preActivity", 1);
                startActivityForResult(intent, ADD_CLASS);
            }
        });

        tv_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FindClassActivity.class);
                intent.putExtra("preActivity", 1);
                startActivityForResult(intent, ADD_CLASS);
            }
        });

        tv_myGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, MyGroupActivity.class));
            }
        });

        tv_myTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, MyTopicActivity.class));
            }
        });

        tv_topRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, TopRankActivity.class));
            }
        });


        getClassCollection(0, 0);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_CLASS:
                getClassCollection(0,0);
                break;
            default:
                getClassCollection(0,0);
                break;
        }
    }

    private void getClassCollection(int pageIndex, int pageSize){
        Observable<BaseEntity<List<Class>>> observable = RetrofitClient.createService(ClassmateAPI.class)
                .getClassCollection(UserSingleton.USERINFO.getUid(), pageIndex, pageSize, UserSingleton.USERINFO.getToken());

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseEntity<List<Class>>>() {
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
                    public void onNext(BaseEntity<List<Class>> baseEntity) {
                        if (baseEntity.isSuccess()) {
                            classList = baseEntity.getDatas();
                            totalCount = baseEntity.getTotal();
                            if (totalCount > 0 && classList.size() > 0) {
                                tv_notification.setVisibility(View.GONE);
                                lv_class.setVisibility(View.VISIBLE);
                                myAdapter = new ClassListAdapter(mContext, classList);
                                myAdapter.setOnClassItemClickListener(new ClassListAdapter.onClassItemClickListener() {
                                    @Override
                                    public void onClassItemClick(Class classItem) {
                                        Intent intent = new Intent(mContext, ClassDetailActivity.class);
                                        intent.putExtra("Class", classItem);
                                        startActivityForResult(intent, 0x02);
                                    }
                                });
                                lv_class.setAdapter(myAdapter);
                            }
                        } else {
                            ToastUtils.showToast(mContext, baseEntity.getError());
                        }
                        Logger.d();
                    }
                });
    }
}
