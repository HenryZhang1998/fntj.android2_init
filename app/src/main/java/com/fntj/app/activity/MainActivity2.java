package com.fntj.app.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.fntj.app.MyApplication;
import com.fntj.app.R;
import com.fntj.app.ann.MenuAnimations;
import com.fntj.app.handler.RobotHandler;
import com.fntj.app.listener.NavDistClickListener;
import com.fntj.app.listener.ScannerOnDataListener;
import com.fntj.app.manager.FNBatteryManager;
import com.fntj.app.net.NetworkUtil;
import com.fntj.app.util.SU;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.interfaces.OnBottomDragListener;
import com.fntj.lib.zb.util.StringUtil;
import com.iflytek.cloud.SpeechUtility;
import com.speech.processor.SpeechPlugin;

import java.util.Map;


public class MainActivity2 extends BaseActivity implements OnBottomDragListener {

    public static Intent createIntent(Context context, String ifly_id, String ifly_id2, String reeman_id) {
        Intent it = new Intent(context, MainActivity2.class);

        it.putExtra("ifly_id", ifly_id);
        it.putExtra("ifly_id2", ifly_id2);
        it.putExtra("reeman_id", reeman_id);

        return it;
    }

    public static MainActivity2 instance;

    public Handler handler = new Handler();
    private ScannerOnDataListener scannerOnDataListener;
    private RobotHandler robotHandler;
    private FragmentManager fragmentManager;

    private String ifly_id;
    private String ifly_id2;
    private String reeman_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        instance = this;

        intent = getIntent();

        ifly_id = intent.getStringExtra("ifly_id");
        ifly_id2 = intent.getStringExtra("ifly_id2");
        reeman_id = intent.getStringExtra("reeman_id");

        SpeechUtility.createUtility(this, "appid=" + ifly_id);

        //SpeechPlugin.CreateSpeechUtility(MyApplication.getInstance(), RobotHandler.IFLY_ID, RobotHandler.REEMAN_ID);
        SpeechPlugin.CreateSpeechUtility(MyApplication.getInstance(), ifly_id2, reeman_id);     // ????????????????????????ID???????????????????????????ID

        RobotHandler.getInstance().init();

        FNBatteryManager.getInstance().init();

        RobotHandler.getInstance().registPower(FNBatteryManager.getInstance(), (action, it) -> {

            System.out.println("---->" + action);
        });

        int b = FNBatteryManager.getInstance().getBattery();

        System.out.println("Battery------>" + b);

        fragmentManager = this.getSupportFragmentManager();

        initView();

        initEvent();

        initData();
    }

    static int[] imgs = {
            R.drawable.xuanzhexingzuo000,
            R.drawable.xuanzhexingzuo1,
            R.drawable.xuanzhexingzuo2,
            R.drawable.xuanzhexingzuo3,
            R.drawable.xuanzhexingzuo4
    };

    private ImageView ivMainTop;
    private Animator animatorOut, animatorIn;

    private int imageIndex;
    private boolean imageAni = true;

    private OnClickImageButton menuClick = new OnClickImageButton();
    private LottieAnimationView ltc_view;

    @Override
    public void initView() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);//????????????

        setContentView(R.layout.activity_main2, this);

        RelativeLayout layout = findViewById(R.id.layoutMain);
        layout.getBackground().setAlpha(150);//0~255???????????? 0???????????????255?????????

        ivMainTop = findView(R.id.ivMainTop);
        ltc_view = findViewById(R.id.ltc_view);

        imageIndex = 0;
        ivMainTop.setImageDrawable(getResources().getDrawable(imgs[imageIndex]));

        animatorOut = AnimatorInflater.loadAnimator(this, R.animator.out_anim);
        animatorIn = AnimatorInflater.loadAnimator(this, R.animator.in_anim);

        animatorOut.setTarget(ivMainTop);
        animatorIn.setTarget(ivMainTop);

        ivMainTop.setOnLongClickListener((v) -> {
            imageAni = false;

            animatorOut.cancel();
            animatorIn.cancel();

            animatorOut.end();
            animatorIn.end();

            imageIndex++;
            if (imageIndex == imgs.length) {
                imageIndex = 0;
            }
            ivMainTop.setImageDrawable(getResources().getDrawable(imgs[imageIndex]));
            return true;
        });

        animatorOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                super.onAnimationEnd(animation);

                imageIndex++;
                if (imageIndex == imgs.length) {
                    imageIndex = 0;
                }
                ivMainTop.setImageDrawable(getResources().getDrawable(imgs[imageIndex]));

                doIn();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });

        animatorIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                doOut();
            }
        });


        MenuAnimations.initOffset(this);

        buttons_wrapper_layout = (RelativeLayout) findViewById(R.id.buttons_wrapper_layout);//??????????????????
        buttons_show_hide_button_layout = (RelativeLayout) findViewById(R.id.buttons_show_hide_button_layout);//????????????
        buttons_show_hide_button_layout.setOnClickListener(v -> {
            if (!isShowing) {
                String txt = "?????????????????????????????????????????????";
                showShortToast(txt);
                robotHandler.doSpeek(txt);

                MenuAnimations.startAnimationsIn(buttons_wrapper_layout, 300);//????????????
            } else {
                MenuAnimations.startAnimationsOut(buttons_wrapper_layout, 300);//????????????
            }
            isShowing = !isShowing;
        });

        //???????????????
        for (int i = 0; i < buttons_wrapper_layout.getChildCount(); i++) {
            buttons_wrapper_layout.getChildAt(i).setOnClickListener(menuClick);
        }

        findViewById(R.id.layoutMain).setOnClickListener((v) -> {
            hideMenus();
        });

        doOut();
        showClickTip(10000);
    }

    private void showClickTip(int time) {
        handler.postDelayed(() -> {

            if (ltc_view.getVisibility() == View.VISIBLE) {
                ltc_view.pauseAnimation();
                ltc_view.setVisibility(View.INVISIBLE);
                showClickTip(10000);
            } else {
                if (!isShowing) {
                    ltc_view.setVisibility(View.VISIBLE);
                    ltc_view.resumeAnimation();
                }

                showClickTip(5000);
            }


        }, time);
    }

    private void hideMenus() {
        if (isShowing) {
            buttons_show_hide_button_layout.callOnClick();
        }
    }

    private void doOut() {
        handler.postDelayed(() -> {
            runOnUiThread(() -> {
                if (imageAni) {
                    animatorOut.start();
                }
            });
        }, 10000);
    }

    private void doIn() {
        handler.post(() -> {
            runOnUiThread(() -> {
                if (imageAni) {
                    animatorIn.start();
                }
            });
        });
    }

    @Override
    public void initData() {
        handler.postDelayed(() -> {
            String welcome = getString(R.string.welcome);
            showShortToast(welcome);
            robotHandler.doSpeek(welcome);
        }, 500);
    }

    @Override
    public void initEvent() {

        //??????????????????
        scannerOnDataListener = new ScannerOnDataListener(this, handler);
        scannerOnDataListener.init();

        robotHandler = RobotHandler.getInstance();
    }

    @Override
    public void onDragBottom(boolean rightToLeft) {
        return;
    }

    public void knowledgeClick(View view) {

        hideMenus();

        if (!NetworkUtil.isNetworkAvailable(context)) {
            Toast.makeText(context, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        toActivity(KnowOneActivity.createIntent(context));
    }

    public void progressClick(View view) {
        hideMenus();
        scannerOnDataListener.showDialog();
    }

    private String voiceAction = "";

    @Override
    protected void onResume() {
        super.onResume();

        robotHandler.onResume();

        robotHandler.registMoveStatus(this, (code) -> {
            if (4 == code) {
                //?????????
                robotHandler.doSpeek("????????????????????????????????????");
            }
        });
        robotHandler.registPower(this, (action, intent) -> {

            if (RobotHandler.BROADCAST_bat_reached.equalsIgnoreCase(action)) {
                robotHandler.setCharging(true);

                if (NavDistClickListener.instance != null) {
                    NavDistClickListener.instance.complete();
                    NavDistClickListener.instance = null;
                }

                handler.post(() -> {
                    //NavDistClickListener.Dissmiss();
                    showShortToast("??????????????????????????????????????????");
                });

                robotHandler.doSpeek("??????????????????????????????????????????");
            }

            if (RobotHandler.BROADCAST_POWER_CONNECTE.equalsIgnoreCase(action)) {
                robotHandler.setCharging(true);

                handler.post(() -> {

                    NavDistClickListener.Dissmiss();

                    showShortToast("????????????");
                });

                robotHandler.doSpeek("?????????????????????????????????????????????");
            }

            if (RobotHandler.BROADCAST_DOCKNOTFOUND.equalsIgnoreCase(action)) {
                robotHandler.setCharging(false);
                handler.post(() -> {

                    NavDistClickListener.Dissmiss();

                    showShortToast("?????????????????????");
                });

                robotHandler.doSpeek("??????????????????????????????????????????????????????");
            }

            if (RobotHandler.BROADCAST_DOCKINGFAILURE.equalsIgnoreCase(action)) {
                robotHandler.setCharging(false);
                handler.post(() -> {

                    NavDistClickListener.Dissmiss();

                    showShortToast("?????????????????????");
                });

                robotHandler.doSpeek("????????????????????????????????????????????????????????????????????????");
            }

            if (Intent.ACTION_BATTERY_CHANGED.equalsIgnoreCase(action)) {

                int power = FNBatteryManager.getInstance().getBattery();
                if (power >= 90) {
                    robotHandler.setCharging(false);
                }

                if (power < 20 && !robotHandler.isCharging()) {
                    handler.post(() -> {
                        showShortToast("???????????????" + power + "%");
                    });

                    robotHandler.doSpeek("?????????????????????" + power + "????????????????????????????????????????????????");

                    handler.postDelayed(() -> {
                        robotHandler.goCharge();
                    }, 5000);
                }
            }
        });

        robotHandler.registFace(this, (result) -> {
            //???????????????
            showShortToast("???????????????");
        });

        robotHandler.registVoice(this, (voice) -> {
            if (StringUtil.isEmpty(voice)) {
                return;
            }

            voiceAction = "";

            //???????????????
            showShortToast("??????????????????" + voice);

            if (voice.contains("??????") || voice.contains("??????") || voice.contains("??????")) {
                robotHandler.doSpeek("??????????????????????????????");
                return;
            }

            for (Map.Entry<String, String> kv : RobotHandler.Voices.entrySet()) {
                if (voice.contains(kv.getKey())) {
                    voiceAction = kv.getValue();
                    break;
                }
            }

            if (StringUtil.isEmpty(voiceAction)) {
                for (Map.Entry<String, String> kv : RobotHandler.Locations.entrySet()) {
                    if (voice.contains(kv.getKey())) {
                        voiceAction = kv.getKey();
                        break;
                    }
                }
            }

            if (StringUtil.isEmpty(voiceAction)) {
                //robotHandler.doSpeek("??????????????????????????????????????????");
                return;
            }

            final String action = voiceAction;
            boolean num = SU.isNumber(action);

            int power = FNBatteryManager.getInstance().getBattery();
            if (power > 20 || action.contains("??????")) {
                handler.post(() -> {
                    if (action.contains("??????")) {
                        robotHandler.doSpeek(String.format("????????????????????????%s", action));
                    } else {
                        robotHandler.doSpeek(String.format("??????????????????????????????%s???????????????", num ? action + "??????" : action));
                    }

                    doAction(action, true);
                });

                handler.postDelayed(() -> {
                    robotHandler.doNavAction(action);
                }, 5000);

            } else if (power < 20) {
                handler.post(() -> {
                    robotHandler.doSpeek(String.format("????????????????????????????????????????????????%s", num ? action + "??????" : action));
                });
            }
        });

        FNBatteryManager.getInstance().register(this, (total, current, percent) -> {
            if (percent < 20 && !robotHandler.isGoingCharge()) {

                robotHandler.stopSpeek();
                robotHandler.cancelNav();
                robotHandler.doSpeek("???????????????????????????????????????20???????????????????????????");

                handler.postDelayed(() -> {
                    robotHandler.goCharge();
                }, 5000);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        robotHandler.onPause();
        robotHandler.unregistMoveStatus(this);
        robotHandler.unregistPower(this);
        robotHandler.unregistFace(this);
        robotHandler.unregistVoice(this);

        FNBatteryManager.getInstance().unregist(this);
    }

    @Override
    protected void onDestroy() {
        if (scannerOnDataListener != null) {
            scannerOnDataListener.unInit();
        }

        robotHandler.onDestroy();

        FNBatteryManager.getInstance().distory();

        super.onDestroy();
    }

    private boolean isShowing;
    private RelativeLayout buttons_wrapper_layout;
    private RelativeLayout buttons_show_hide_button_layout;

    /**
     * ????????????
     */
    public class OnClickImageButton implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.button_home:
                    showShortToast("?????????");
                    MenuAnimations.startAnimationsOut(buttons_wrapper_layout, 300);//????????????
                    isShowing = !isShowing;
                    doAction("??????", false);
                    break;
                case R.id.button_charge:
                    showShortToast("?????????");
                    MenuAnimations.startAnimationsOut(buttons_wrapper_layout, 300);//????????????
                    isShowing = !isShowing;
                    doAction("??????", false);
                    break;
                case R.id.button_wc:
                    showShortToast("?????????");
                    MenuAnimations.startAnimationsOut(buttons_wrapper_layout, 300);//????????????
                    isShowing = !isShowing;
                    doAction("?????????", false);
                    break;
            }
        }
    }

    public void doAction(final String action, boolean autoStart) {

        boolean goHome = !action.contains("??????") && !action.contains("??????");

        NavDistClickListener listener = NavDistClickListener.create(MainActivity2.this, robotHandler, action, goHome, (d, a, isTask) -> {
            //????????????

            context.showShortToast("????????????!");
            robotHandler.doSpeek("?????????????????????????????????????????????????????????????????????");
            robotHandler.cancelNav();

            handler.postDelayed(() -> {
                robotHandler.goHome();
            }, 5000);

        }, (d, a) -> {
            //????????????

            String completeSpeek = String.format("?????????%s??????", action.contains("??????") ? "?????????" : action);
            if (goHome) {
                completeSpeek += "?????????????????????????????????????????????";
            }
            robotHandler.doSpeek(completeSpeek);

            if (goHome) {
                handler.postDelayed(() -> {
                    robotHandler.goHome();
                }, 5000);
            }

        }, 1);

        if (autoStart) {
            listener.dismissThis();
            listener.confirmStart();
        } else {
            listener.start();
        }
    }
}
