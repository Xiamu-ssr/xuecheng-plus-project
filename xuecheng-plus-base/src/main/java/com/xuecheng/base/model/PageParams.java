package com.xuecheng.base.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    //当前页码
    @Schema(description = "页码")
    private Long pageNo = 1L;

    //每页记录数默认值
    @Schema(description = "每页条目数量")
    private Long pageSize =10L;
}
