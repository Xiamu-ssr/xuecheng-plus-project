package com.xuecheng.media.api;


import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.BigFilesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@Tag(name = "大文件管理接口", description = "视频等")
public class BigFilesController {
    @Autowired
    BigFilesService bigFilesService;

    @Operation(description = "文件上传前检查文件, 是否已经存在")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5){
        return bigFilesService.checkfile(fileMd5);
    }

    @Operation(description = "分块文件上传前的检测, 块是否已经存在")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) {
        return bigFilesService.checkchunk(fileMd5, chunk);
    }

    @Operation(description = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {

        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".temp");
        file.transferTo(tempFile);
        String localFilePath = tempFile.getAbsolutePath();
        return bigFilesService.uploadchunk(fileMd5, chunk, localFilePath);
    }

    @Operation(description = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal){

        Long companyId = 1232141425L;

        UploadFileParamsDto dto = new UploadFileParamsDto();
        dto.setFilename(fileName);
        dto.setFileType("001002");
        dto.setTags("视频文件");
        return bigFilesService.mergechunks(companyId, fileMd5, chunkTotal, dto);
    }
}
