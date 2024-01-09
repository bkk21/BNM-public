package com.khci.bnm.Movenet;

import android.content.res.AssetManager;
import android.view.Surface;


public class NcnnBodypose
{
    public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    public native boolean openCamera(int facing);
    public native boolean closeCamera();
    public native boolean setOutputWindow(Surface surface);
    public native boolean pose(int level);
    public native boolean setMotionStep(int step);
    public native boolean setIsStart(boolean start);

    public native int posenum();
    public native int posestep();

    public native int tts();

    public native int motion_step();

    static {
        System.loadLibrary("ncnnbodypose");
    }
}

