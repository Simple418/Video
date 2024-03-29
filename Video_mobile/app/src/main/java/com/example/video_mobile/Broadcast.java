package com.example.video_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.video_mobile.Utils.StatusBarUtils;

import java.util.Timer;
import java.util.TimerTask;


public class Broadcast extends AppCompatActivity {
    public static final int UPDATA_VIDEO_NUM = 1;
    private CustomVideoView videoView;
    private MediaController controller;//控制器
    private RelativeLayout videoLayout;
    private LinearLayout controllerLayout;//播放器的总控制布局
    private SeekBar play_seek, volume_seek;//播放进度和音量控制进度
    private ImageView play_controller_image, screen_image,volume_Image;
    private TextView current_time_tv, totally_time_tv;
    private String path;
    private int screen_width, screen_height;
    private AudioManager audioManager;//音量控制器
    private boolean  is_full= false;//判断屏幕转向
    private ImageView show_pc;

    private int currentPosition;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setWindowStatusBarColor(Broadcast.this,R.color.theme);
        //实例化音量控制器
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        setContentView(R.layout.activity_broadcast);

        initView();
        initData();
        initViewOnClick();


        /**
         * 本地播放
         */
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/VID_20181210_170534.mp4";
        videoView.setVideoPath(path);

        play_controller_image.setImageResource(R.mipmap.video_play_blue);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(videoView!=null){
            videoView.seekTo(currentPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPosition = videoView.getCurrentPosition();
        handler.removeMessages(UPDATA_VIDEO_NUM);

        if(videoView!=null){
            //视频暂停
            videoView.pause();
            play_controller_image.setImageResource(R.mipmap.video_play_blue);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(videoView!=null){
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(UPDATA_VIDEO_NUM);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 判断当前屏幕的横竖屏状态
        int screenOritentation = getResources().getConfiguration().orientation;

       // String screen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE  ? "横向屏幕" : "竖向屏幕";
        //Toast.makeText(this , "系统的屏幕方向发生改变\n" + "修改后的屏幕方向为:" + screen, Toast.LENGTH_LONG ).show();

        if (screenOritentation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏时处理
            setVideoScreenSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
            volume_seek.setVisibility(View.VISIBLE);
            volume_Image.setVisibility(View.VISIBLE);
            is_full = true;

            getWindow().clearFlags((WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN));
            getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        } else {
            //竖屏时处理
            setVideoScreenSize(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(Broadcast.this,240));
            is_full = false;
            volume_seek.setVisibility(View.GONE);
            volume_Image.setVisibility(View.GONE);
            //清除全屏标记，重新添加
            getWindow().clearFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
            getWindow().addFlags((WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN));
        }
    }

    /**
     * 通过handler对播放进度和时间进行更新
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATA_VIDEO_NUM) {
                //获取视频播放的当前时间
                int currentTime = videoView.getCurrentPosition();
                //获取视频的总时间
                int totally = videoView.getDuration();
                //格式化显示时间
                updataTimeFormat(totally_time_tv, totally);
                updataTimeFormat(current_time_tv, currentTime);
                //设置播放进度
                play_seek.setMax(totally);
                play_seek.setProgress(currentTime);
                //自己通知自己更新
                handler.sendEmptyMessageDelayed(UPDATA_VIDEO_NUM, 500);//500毫秒刷新
            }
        }
    };



    /**
     * 设置横竖屏时的视频大小
     *
     * @param width
     * @param height
     */
    private void setVideoScreenSize(int width, int height) {
        //设置视频和控制组件的layout
        ViewGroup.LayoutParams videoLayoutLayoutParams= videoLayout.getLayoutParams();
        videoLayoutLayoutParams.width = width;
        videoLayoutLayoutParams.height = height;
        videoLayout.setLayoutParams(videoLayoutLayoutParams);
        //System.out.println("大小"+width+height);


        //获取视频控件的布局参数
        ViewGroup.LayoutParams videoViewLayoutParams = videoView.getLayoutParams();
        //设置视频范围
        videoViewLayoutParams.width = width;
        videoViewLayoutParams.height = height;
        videoView.setLayoutParams(videoViewLayoutParams);
    }

    /**
     * 时间格式化
     *
     * @param textView    时间控件
     * @param millisecond 总时间 毫秒
     */
    private void updataTimeFormat(TextView textView, int millisecond) {
        //将毫秒转换为秒
        int second = millisecond / 1000;
        //计算小时
        int hh = second / 3600;
        //计算分钟
        int mm = second % 3600 / 60;
        //计算秒
        int ss = second % 60;
        //判断时间单位的位数
        String str = null;
        if (hh != 0) {//表示时间单位为三位
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        //将时间赋值给控件
        textView.setText(str);
    }


    /**
     * 按钮点击事件
     */
    private void initViewOnClick() {



        //设置全屏按钮点击事件
        screen_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(is_full){
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//控制屏幕横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//控制屏幕竖屏
                }
                else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//控制屏幕横屏
                }
            }
        });

        //播放按钮事件
        play_controller_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断播放按钮的状态
                if (videoView.isPlaying()) {
                    play_controller_image.setImageResource(R.mipmap.video_play_blue);
                    //视频暂停
                    videoView.pause();
                    //当视频处于暂停状态，停止handler的刷新
                    handler.removeMessages(UPDATA_VIDEO_NUM);
                } else {
                    play_controller_image.setImageResource(R.mipmap.iv_pause);
                    videoView.start();
                    //当视频播放时，通知刷新
                    handler.sendEmptyMessage(UPDATA_VIDEO_NUM);
                }
            }
        });
        //播放进度条事件
        play_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置当前的播放时间
                updataTimeFormat(current_time_tv, progress);
                if (videoView.getDuration() == progress) {
                    play_controller_image.setImageResource(R.mipmap.video_play_blue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //拖动视频进度时，停止刷新
                handler.removeMessages(UPDATA_VIDEO_NUM);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止拖动后，获取总进度
                int totall = seekBar.getProgress();
                //设置VideoView的播放进度
                videoView.seekTo(totall);
                //重新handler刷新
                handler.sendEmptyMessage(UPDATA_VIDEO_NUM);

            }
        });
        //音量控制条事件
        volume_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置音量变动后系统的值
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        show_pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent wifi = new Intent(Broadcast.this,wifiConnect.class);
                startActivity(wifi);
            }
        });
    }


    protected void initView() {
        videoView = (CustomVideoView) findViewById(R.id.video_view);
        videoLayout = (RelativeLayout) findViewById(R.id.videolayout);
        controllerLayout = (LinearLayout) findViewById(R.id.main_controller_liner);
        play_seek = (SeekBar) findViewById(R.id.main_play_seek);
        volume_seek = (SeekBar) findViewById(R.id.main_volume_seek);
        current_time_tv = (TextView) findViewById(R.id.main_current_time);
        totally_time_tv = (TextView) findViewById(R.id.main_totally_time);
        play_controller_image = (ImageView) findViewById(R.id.play_pasue_image);
        screen_image = (ImageView) findViewById(R.id.main_screen_image);
        volume_Image = (ImageView) findViewById(R.id.volume_image);
        DisplayMetrics metric = new DisplayMetrics();
        screen_width = metric.widthPixels;
        screen_height = metric.heightPixels;
        show_pc = findViewById(R.id.show_pc);
    }


    protected void initData() {
        //获取设置音量的最大值
        int volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setMax(volumeMax);
        //获取设置当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setProgress(currentVolume);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, Broadcast.class);

        context.startActivity(starter);
    }

    /**
     * 将dp转换成px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context,float dpValue){
        final float scale = context.getResources ().getDisplayMetrics ().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
