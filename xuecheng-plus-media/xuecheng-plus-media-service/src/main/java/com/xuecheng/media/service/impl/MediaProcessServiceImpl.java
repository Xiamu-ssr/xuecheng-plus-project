package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频转化服务impl
 *
 * @author mumu
 * @date 2024/02/02
 */
@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);
    }

    @Override
    public boolean startTask(long id) {
        return 0 < mediaProcessMapper.startTask(id);
    }

    @Override
    @Transactional
    public void saveProcessFinishStatus(long id, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(id);
        if (mediaProcess == null){
            return;
        }
        if (status.equals("3")){
            //如果任务执行失败
            //更新media_process状态
            mediaProcess.setStatus(status);
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
        }else if (status.equals("2")){
            //如果任务执行成功
            //更新media_file的url，比如从avi->mp4
            MediaFiles mediaFile = mediaFilesMapper.selectById(fileId);
            mediaFile.setUrl(url);
            mediaFilesMapper.updateById(mediaFile);

            //更新media_process状态
            mediaProcess.setStatus(status);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcess.setUrl(url);
            mediaProcessMapper.updateById(mediaProcess);

            //插入media_process_history
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            mediaProcessHistory.setId(null);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);

            //删除media_process
            mediaProcessMapper.deleteById(id);
        }
    }
}
