package com.c.lib;

import android.app.Activity;

public class PermissionRequest {

    private Activity mActivity;
    private String[] mPermission;

    public PermissionRequest(Activity activity, String[] permission) {
        this.mActivity = activity;
        this.mPermission = permission;
    }

    public void request(){
        mActivity.requestPermissions(mPermission, 0x11);
    }
}
