package com.xuecheng.feign.client;

import com.xuecheng.feign.pojo.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "search-service", path = "/search")
public interface SearchClient {

    @PostMapping("/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);

    @DeleteMapping("course/{id}")
    public Boolean delete(@PathVariable String id);
}
