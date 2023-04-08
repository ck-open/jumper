package com.ck.core.feign;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ck.api.TResult;
import com.ck.core.mybatis.QueryDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * @ClassName 公共接口 JumperFeign 客户端
 * @Description mybatis plus LambdaQuery 查询公共方法
 * @Author Cyk
 * @Version 1.0
 * @since 2023/03/18 19:47
 **/
public class JumperFeign {
    private ConfigurableListableBeanFactory beanFactory;
    private ObjectMapper objectMapper;

    public JumperFeign(ConfigurableListableBeanFactory beanFactory, ObjectMapper objectMapper) {
        this.beanFactory = beanFactory;
        this.objectMapper = objectMapper;
        init();
    }


    /**
     * 获取 JumperFeignClient
     *
     * @param serverName 服务名称（ServerId）
     * @return
     */
    public JumperFeignClient getClient(String serverName) {
        return FeignClientUtils.build(serverName, JumperFeignClient.class, JumperFeignClientImpl.class);
    }

    /**
     * 获取 JumperFeignClient
     *
     * @param serverName 服务名称（ServerId）
     * @return
     */
    public JumperFeignClient getClient(String serverName, String url) {
        return FeignClientUtils.build(serverName, url, JumperFeignClient.class, JumperFeignClientImpl.class);
    }


    public interface JumperFeignClient {
        @PostMapping("/jumper/page/{mapperBeanName}")
        <T> TResult<IPage<T>> queryPage(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryDto<?> dto);

        @PostMapping("/jumper/list/{mapperBeanName}")
        <T> TResult<List<T>> queryList(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryDto<?> dto);
    }

    public static class JumperFeignClientImpl implements JumperFeignClient {
        @Override
        public <T> TResult<IPage<T>> queryPage(String mapperBeanName, QueryDto<?> dto) {
            return TResult.build(0, "服务不可用");
        }

        @Override
        public <T> TResult<List<T>> queryList(String mapperBeanName, QueryDto<?> dto) {
            return TResult.build(0, "服务不可用");
        }
    }


    /**
     * 处理 IPage 接口接收返回值 feign 无法序列化的问题
     */
    private void init() {
        if (this.objectMapper == null) {
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            this.objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            // 排序key
            this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            //忽略空bean转json错误
            this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            //忽略在json字符串中存在，在java类中不存在字段，防止错误。
            this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // 注入到容器
            this.beanFactory.registerSingleton("jacksonObjectMapper", this.objectMapper);
        }

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addDeserializer(IPage.class, new StdDeserializer(IPage.class) {
            @Override
            public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                JsonNode node = jsonParser.getCodec().readTree(jsonParser);
                String s = node.toString();
                ObjectMapper om = new ObjectMapper();
                Page page = om.readValue(s, Page.class);
                return page;
            }
        }); // 处理 IPage 接口接收返回值 feign 无法序列化的问题
        this.objectMapper.registerModule(simpleModule);
    }
}
