package com.xuecheng.content.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.feign.client.MediaClient;
import com.xuecheng.feign.config.MultipartSupportConfig;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    TeachplanMapper teachplanMapper;


    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachplanService teachplanService;
    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaClient mediaClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseById(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.getTreeNodes(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //约束
        //1. 课程正在审核可修改但不能再提交
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseById(courseId);
        if (courseBaseInfoDto == null){
            XueChengPlusException.cast("课程不存在");
        }
        String auditStatus = courseBaseInfoDto.getAuditStatus();
        if (auditStatus.equals("202003")){
            XueChengPlusException.cast("课程已提交请等待审核");
        }
        //2. 没有图片，没有课程计划，不能提交
        String pic = courseBaseInfoDto.getPic();
        if (StringUtils.isEmpty(pic)){
            XueChengPlusException.cast("请上传课程图片");
        }
        List<TeachplanDto> teachplanTree = teachplanService.getTreeNodes(courseId);
        if (CollectionUtils.isEmpty(teachplanTree)){
            XueChengPlusException.cast("请添加课程计划");
        }
        //处理
        //1. 结合多表信息插入到course_publish_pre
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));
        coursePublishPre.setTeachplan(JSON.toJSONString(teachplanTree));
        //2. 更新状态为已提交（正在审核）
        coursePublishPre.setStatus("202003");
        coursePublishPre.setAuditDate(LocalDateTime.now());
        //3. course_publish_pre有数据则更新，否则插入
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1 == null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //同步
        //1. course_base的状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void coursePublish(Long companyId, Long courseId) {
        //约束
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (ObjectUtils.isEmpty(coursePublishPre)){
            XueChengPlusException.cast("课程没有审核记录,无法发布");
        }
        if (!coursePublishPre.getStatus().equals("202004")){
            XueChengPlusException.cast("课程需审核通过才可发布");
        }
        //向course_publish写数据
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if (coursePublish == null){
            coursePublish = new CoursePublish();
            BeanUtils.copyProperties(coursePublishPre, coursePublish);
            coursePublishMapper.insert(coursePublish);
        }else {
            BeanUtils.copyProperties(coursePublishPre, coursePublish);
            coursePublishMapper.updateById(coursePublish);
        }
        //同步到course_base
        courseBaseMapper.update(new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, courseId)
                .set(CourseBase::getStatus, "203002")
        );
        //向mq_message写入数据
        mqMessageService.addMessage("course_publish", courseId.toString(), null, null);

        //向course_publish_pre删除数据
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            //configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            configuration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile(UUID.randomUUID().toString(),".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常courseId:{},reason:{}",courseId ,e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        UploadFileResultDto upload = mediaClient.upload(multipartFile, "course/" + courseId + ".html");
        if(upload==null){
            XueChengPlusException.cast("上传静态文件异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }
}
