package com.xuecheng.media.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.BigFilesService;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class BigFilesServiceImpl implements BigFilesService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;
    @Lazy
    @Autowired
    MediaFileService mediaFileService;
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        MediaFiles mediaFile = mediaFilesMapper.selectById(fileMd5);
        if (mediaFile != null){
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(mediaFile.getBucket())
                    .object(mediaFile.getFilePath())
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("查询Minio文件失败, 数据库有记录但是Minio无此数据, fileMd5={}", fileMd5);
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk) {
        String chunkFolder = getChunkFolderByMd5(fileMd5);
        String chunkPath = chunkFolder + chunk;
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkPath)
                .build();

        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null){
                return RestResponse.success(true);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            log.debug("从Minio未查询到指定块, fileMd5={}, chunkIndex={}", fileMd5, chunk);
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadchunk(String fileMd5, int chunk, String localChunkFilePath) {
        String mimeType = getMimeType(null);
        String chunkFolder = getChunkFolderByMd5(fileMd5);
        String chunkPath = chunkFolder + chunk;
        boolean bool = mediaFileService.upload2Minio(localChunkFilePath, bucket_video, chunkPath, mimeType);
        if (bool){
            return RestResponse.success(true);
        }else {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        String chunkFolder = getChunkFolderByMd5(fileMd5);
        String filename = uploadFileParamsDto.getFilename();
        String mergePath = getMergePathByMd5(fileMd5, filename.substring(filename.lastIndexOf(".")));
        //找到分块文件并合并
        List<ComposeSource> sources = Stream.iterate(0, i -> i+1).limit(chunkTotal)
                .map(i ->
                        ComposeSource.builder()
                                .bucket(bucket_video)
                                .object(chunkFolder + i)
                                .build()
                )
                .collect(Collectors.toList());
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(mergePath)
                .sources(sources)
                .build();
        ObjectWriteResponse composeObject = null;
        try {
            composeObject = minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错, bucket:{}, objectName:{}, errMessage:{}", bucket_video, mergePath, e.getMessage());
            return RestResponse.validfail(false, "文件合并过程异常");
        }
        //校验合并和文件和源文件是否一致
        String mergeMd5 = getMinioFileMd5(bucket_video, mergePath);
        if (mergeMd5.equals(fileMd5)){
            log.info("合并文件验证成功, sourceMd5: {}, mergeMd5: {}", fileMd5, mergeMd5);
        }else {
            log.error("合并文件验证异常, sourceMd5: {}, mergeMd5: {}", fileMd5, mergeMd5);
            return RestResponse.validfail(false, "合并得到的文件与源文件不一致");
        }
        //文件信息入库并//记录待处理任务
        MediaFiles mediaFiles = mediaFileService.upload2Mysql(fileMd5, companyId, uploadFileParamsDto, bucket_video, mergePath);
        if (mediaFiles == null){
            log.error("文件信息入库失败, fileMd5:{}", fileMd5);
            return RestResponse.validfail(false, "文件信息入库失败");
        }
        //删除文件块
        clearChunk(chunkFolder, chunkTotal);

        return RestResponse.success(true);
    }


    /**
     * 通过md5值获取文件在Minio上的路径
     *
     * @param md5 md5
     * @return {@link String}
     */
    private String getChunkFolderByMd5(String md5){
        return md5.substring(0, 1) + "/" + md5.substring(1, 2) + "/" + md5 + "/" + "chunk" + "/";
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
     * 获取Minio文件的md5
     *
     * @param bucket     bucket
     * @param objectName 文件路径
     * @return {@link String}
     */
    private String getMinioFileMd5(String bucket, String objectName){
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 清除块文件
     *
     * @param chunkFolder 块文件夹路径
     * @param total       数量
     */
    private void clearChunk(String chunkFolder, int total){
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> i + 1).limit(total)
                .map(i -> new DeleteObject(chunkFolder.concat(Integer.toString(i))))
                .collect(Collectors.toList());
        RemoveObjectsArgs objectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket_video)
                .objects(deleteObjects)
                .build();
        minioClient.removeObjects(objectsArgs);
    }

}
