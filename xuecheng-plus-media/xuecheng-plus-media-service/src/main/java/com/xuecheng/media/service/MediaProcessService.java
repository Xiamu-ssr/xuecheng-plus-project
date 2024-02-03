package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaProcessService {

    /**
     * 获取待处理视频列表
     *
     * @param shardIndex 执行器index
     * @param shardTotal 执行器总数
     * @param count      条数
     * @return {@link List}<{@link MediaProcess}>
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 尝试获取处理任务的资格
     *
     * @param id media_process id
     * @return int true-成功 false-失败
     */
    public boolean startTask(long id);

    /**
     * 保存视频处理结果
     *
     * @param id       media_process id
     * @param status   状态码
     * @param flieId   文件id media_file id
     * @param url      文件 url
     * @param errorMsg 错误消息
     */
    public void saveProcessFinishStatus(long id, String status, String flieId, String url, String errorMsg);


}
