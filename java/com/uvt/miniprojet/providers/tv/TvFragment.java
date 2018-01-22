package com.uvt.miniprojet.providers.tv;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.uvt.miniprojet.inherit.BackPressFragment;
import com.uvt.miniprojet.inherit.CollapseControllingFragment;
import com.uvt.miniprojet.MainActivity;
import com.uvt.miniprojet.R;
import com.uvt.miniprojet.util.Helper;
import com.uvt.miniprojet.util.Log;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * This fragment is used to play live video streams.
 */
public class TvFragment extends Fragment implements CollapseControllingFragment, BackPressFragment {

    private Activity mAct;
    private RelativeLayout rl;

    private JCVideoPlayerStandard jcVideoPlayerStandard;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rl = (RelativeLayout) inflater.inflate(R.layout.fragment_tv, container, false);
        jcVideoPlayerStandard = (JCVideoPlayerStandard) rl.findViewById(R.id.custom_videoplayer_standard);

        return rl;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        Helper.isOnlineShowDialog(mAct);

        String streamurl = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

        jcVideoPlayerStandard.setUp(streamurl, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");
        jcVideoPlayerStandard.startButton.performClick();

        updateWidth();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        updateWidth();
    }

    void updateWidth(){
        jcVideoPlayerStandard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                jcVideoPlayerStandard.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                ViewGroup.LayoutParams params = jcVideoPlayerStandard.getLayoutParams();
                double currentPlayerHeight = jcVideoPlayerStandard.getMeasuredHeight();
                double currentFrameHeight = rl.getMeasuredHeight();

                //If the view doesn't fit the screen in height, slim it's width down so it does
                if (currentPlayerHeight > currentFrameHeight) {
                    double ratio = currentFrameHeight / currentPlayerHeight;
                    params.width = (int) (ratio * rl.getMeasuredWidth());
                    jcVideoPlayerStandard.setLayoutParams(params);

                    Log.v("INFO", "player: ratio(n): " + ratio + " newWidth: " + (int) (ratio * rl.getMeasuredWidth()));
                }

                 Log.v("INFO", "player: called");

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    public boolean supportsCollapse() {
        return false;
    }

    @Override
    public boolean handleBackPress() {
        return JCVideoPlayer.backPress();
    }
}


