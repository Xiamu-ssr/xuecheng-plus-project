package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * upload file
     *
     * @param companyId     公司id
     * @param dto           dto
     * @param localFilePath 本地文件路径
     * @return {@link UploadFileResultDto}
     */
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto dto, String localFilePath);

    public boolean upload2Minio(String localFilePath, String bucket, String objectName, String mimeType);

    public MediaFiles upload2Mysql(String fileMd5, Long companyId, UploadFileParamsDto dto, String bucket, String objectName);
}
