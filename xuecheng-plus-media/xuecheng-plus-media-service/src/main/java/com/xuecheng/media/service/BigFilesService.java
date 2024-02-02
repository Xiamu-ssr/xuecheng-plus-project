package com.xuecheng.media.service;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface BigFilesService {

    /**
     * 检查文件是否已经存在于文件系统
     *
     * @param fileMd5 文件md5
     * @return {@link RestResponse}<{@link Boolean}>
     *     <pre>
     *         true  - file exist <br>
     *         false - file not exist
     *     </pre>
     */
    public RestResponse<Boolean> checkfile(String fileMd5);

    /**
     * 检查文件块是否已经存在
     *
     * @param fileMd5 文件md5
     * @param chunk   块序号
     * @return {@link RestResponse}<{@link Boolean}>
     *     <pre>
     *         true  - chunk of file exist <br>
     *         false - chunk of file not exist
     *     </pre>
     */
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk);

    /**
     * 上传文件块
     *
     * @param fileMd5            文件md5
     * @param chunk              块序号
     * @param localChunkFilePath 本地区块文件路径
     * @return {@link RestResponse}
     */
    public RestResponse uploadchunk(String fileMd5, int chunk, String localChunkFilePath);

    /**
     * mergechunks
     *
     * @param companyId           公司id
     * @param fileMd5             文件md5
     * @param chunkTotal          块总数
     * @param uploadFileParamsDto 上传文件参数dto
     * @return {@link RestResponse}
     */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

}
