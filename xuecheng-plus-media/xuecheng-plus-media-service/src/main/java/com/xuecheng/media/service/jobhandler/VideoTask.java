package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {

    @Autowired
    MediaProcessService mediaProcessService;
    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws InterruptedException {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //确定CPU核心数
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("CPU核心数processors = " + processors);
        //获取需要处理任务列表
        List<MediaProcess> mediaList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        int size = mediaList.size();
        log.info("取到视频数为:"+size);
        if (size <= 0){
            return;
        }
        //使用线程池并行处理
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaList.forEach(mediaProcess->{
            threadPool.execute(()->{
                try {
                    Long taskId = mediaProcess.getId();
                    //fileid and file md5
                    String fileId = mediaProcess.getFileId();
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();

                    //争抢单个任务处理资格
                    boolean b = mediaProcessService.startTask(taskId);
                    if (!b){
                        log.debug("shardIndex:{}-shardTotal:{}抢占任务失败,任务id:{}",shardIndex, shardTotal, taskId);
                    }else {
                        //处理
                        //下载Minio上的avi视频到本地
                        File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                        if (file == null) {
                            log.warn("下载视频出错,任务id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, mediaProcess.getUrl(), "下载视频出错");
                            return;
                        }
                        //源avi视频的路径
                        String sourceVideoPath = file.getAbsolutePath();
                        //转换后mp4文件的名称
                        String targetVideoName = fileId + ".mp4";
                        //转换后mp4文件的路径
                        File targetVideo = null;
                        try {
                            targetVideo = File.createTempFile(UUID.randomUUID().toString(), ".mp4");
                        } catch (IOException e) {
                            log.error("创建临时文件异常,{}", e.getMessage());
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, mediaProcess.getUrl(), "创建临时文件异常");
                            return;
                        }
                        String targetVideoPath = targetVideo.getAbsolutePath();
                        //视频工具类
                        //疑问？targetVideoPath应该是targetVideo所在文件夹？
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, sourceVideoPath, targetVideoName, targetVideoPath);
                        //开始视频转换，成功将返回success
                        String result = videoUtil.generateMp4();
                        if (!result.equals("success")) {
                            log.warn("视频转码失败,原因{},bucket:{},objectName:{}", result, bucket, objectName);
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, mediaProcess.getUrl(), "视频转码失败");
                            return;
                        } else {
                            //上传到Minio
                            boolean upload2Minio = mediaFileService.upload2Minio(targetVideoPath, bucket, changeSuffix(objectName, ".mp4"), "video/mp4");
                            if (!upload2Minio) {
                                log.warn("上传mp4到minio失败,fileid:{},", taskId);
                                mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, mediaProcess.getUrl(), "上传mp4到minio失败");
                                return;
                            }
                            //tips:这里使用源文件的md5而不是转码后的md5是为了将两份视频放在同一位置
                            String url = getMergePathByMd5(fileId, ".mp4");

                            //结果保存
                            mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                        }
                    }
                }finally {
                    countDownLatch.countDown();
                }

            });
        });

        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /**
     * 通过md5获取合并文件路径
     *
     * @param md5       md5
     * @param extension 扩展名
     * @return {@link String}
     */
    private String getMergePathByMd5(String md5, String extension){
        return md5.substring(0, 1) + "/" + md5.substring(1, 2) + "/" + md5 + "/" + md5 + extension;
    }

    private String changeSuffix(String source, String extension){
        int lastDotIndex = source.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return source.substring(0, lastDotIndex) + extension;
        }
        return source + extension;
    }
}
