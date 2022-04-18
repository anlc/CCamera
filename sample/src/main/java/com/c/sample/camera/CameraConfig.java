package com.c.sample.camera;

import android.hardware.camera2.CameraAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraConfig {

    private final CameraManagerFacade mCameraManagerFacade;
    private List<CameraManagerFacade.Facing> mFacingList;

    public CameraConfig(CameraManagerFacade cameraManagerFacade) {
        this.mCameraManagerFacade = cameraManagerFacade;
    }

    public void initFacing(String[] cameraIdList) throws CameraAccessException {
        List<CameraManagerFacade.Facing> facingList = new ArrayList<>();
        for (String cameraId : cameraIdList) {
            CameraManagerFacade.Facing facing = mCameraManagerFacade.getFacing(cameraId);
            facingList.add(facing);
        }
        mFacingList = Collections.unmodifiableList(facingList);
    }

    public List<CameraManagerFacade.Facing> getFacingList() {
        return mFacingList;
    }
}
