package com.xuecheng.media.model.dto;

import com.xuecheng.media.model.po.MediaFiles;
import lombok.Data;
import lombok.ToString;

/**
 * 上传文件结果dto
 *
 * @author mumu
 * @date 2024/01/31
 */
@Data
@ToString(callSuper = true)
public class UploadFileResultDto extends MediaFiles {
}
