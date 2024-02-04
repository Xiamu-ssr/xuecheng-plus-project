package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Lazy
    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto dto, String localFilePath) {
        //upload file to minio
        String filename = dto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String folderPath = getDefaultFolderPath();
        String fileMd5 = getFileMd5(localFilePath);
        String objectName = folderPath + fileMd5 + extension;
        boolean result = upload2Minio(localFilePath, bucket_mediafiles, objectName, mimeType);
        if (!result){
            XueChengPlusException.cast("upload file Fail.");
        }
        //upload info to mysql
        MediaFiles mediaFiles = mediaFileService.upload2Mysql(fileMd5, companyId, dto, bucket_mediafiles, objectName);
        if (mediaFiles == null){
            XueChengPlusException.cast("upload file info into Mysql Fail.");
        }
        // return
        UploadFileResultDto resultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, resultDto);
        return resultDto;
    }

    @Override
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取mime类型
     *
     * @param extension 后缀名
     * @return {@link String}
     */
    private String getMimeType(String extension){
        if (extension == null){
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 获取默认文件夹路径
     * <pre>
     *     2024-01-31
     *     2024/01/31/
     * </pre>
     * @return {@link String}
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/")+"/";
    }

    /**
     * 获取文件md5
     *
     * @param file file
     * @return {@link String}
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件md5
     *
     * @param filePath 文件路径
     * @return {@link String}
     */
    private String getFileMd5(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * upload File to Minio
     *
     * @param localFilePath 本地文件路径
     * @param bucket        bucket
     * @param objectName    对象名称
     * @param mimeType      mime类型
     */
    @Override
    public boolean upload2Minio(String localFilePath, String bucket, String objectName, String mimeType){

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .filename(localFilePath)
                    .bucket(bucket)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.info("upload file to Minio Success, bucket:{}, objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("upload file error, bucket:{}, objectName:{}, errMessage:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /**
     * upload file info into mysql
     *
     * @param fileMd5    文件md5
     * @param companyId  公司id
     * @param dto        dto
     * @param bucket     bucket
     * @param objectName 对象名称
     * @return {@link MediaFiles}
     */
    @Transactional
    @Override
    public MediaFiles upload2Mysql(String fileMd5, Long companyId, UploadFileParamsDto dto, String bucket, String objectName){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(dto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0){
                log.debug("save file info into mysql Fail. bucket:{}, objectName:{}", bucket, objectName);
                return null;
            }
            //如果是视频记录待处理任务
            if (mediaFiles.getFileType().equals("001002")){
                addWaitingTask(mediaFiles);
            }
        }
        return mediaFiles;
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    private void addWaitingTask(MediaFiles mediaFiles){
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //如果是avi视频则写入待处理任务
        if (mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }
}
