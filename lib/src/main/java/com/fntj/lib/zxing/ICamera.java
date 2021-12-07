package com.fntj.lib.zxing;


//import androidx.camera.core.Camera;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public interface ICamera {

    /**
     * 启动相机预览
     */
    void startCamera();

    /**
     * 停止相机预览
     */
    void stopCamera();

    /**
     * 获取{Camera}
     * @return
     */
//    @Nullable Camera getCamera();

    /**
     * 释放
     */
    void release();

}
