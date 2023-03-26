package com.side.project.video.utils.other.util;

public interface VideoTrimListener {
    void onStartTrim();
    void onFinishTrim(String url);
    void onCancel();
}
