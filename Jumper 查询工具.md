# 基于 Mybatis plus LambdaQuery 实现的 Jumper 查询工具
## 现状
+ 多条件数据查询时需要大量的编写 eq、lt、ge、like、in 等方法设置条件并且每个条件基本需要判空。
+ 简单查询通常会有controller -> service -> mapper。即使没有service 在controller中也会存在重复性代码。
+ 微服务环境下，获取其他服务数据信息 feignClient -> controller -> service -> mapper  每种数据获取都需要一套完整流程代码。

## 目标
简化条件参数设置过程，将基本查询抽象出共性以公共方式实现。  
将普通查询功能的controller、service层抽取出来，提供公共方式，直接通过mapper来实现查询。  
微服务下调用，实现公共controller查询方式后可以将feignClient 进行封装提供。并注入到容器中，方便使用。  
在公共controller中提供常用条件 eq、lt、ge、like、in、orderByAsc、orderByDesc等。

## QueryUtil方法
所有方法中的 条件对象 QueryDto、eq 或 like  都非必传 可以为 null
### 方法参数说明
```java
Integer current  查询页
Integer size     单页条数
IPage<R> page    分页对象
ConsumerConvert<T, R> convert  查询出的结果集PO 转 返回值Dto的实现函数
IService<R> service  实现Mybatis Plus IService<R> 接口的实现对象
QueryDto dto  统一查询条件对象
Object eq     等 条件对象
Object like   包含 条件对象
Object lt     小于 条件的参数对象
Object ge     大于等于 条件的参数对象
LambdaQueryWrapper<T> lambdaQueryWrapper 
LambdaQueryChainWrapper<T> lambdaQueryWrapper
```

### 构建查询对象
+ public static &lt;T> QueryDto&lt;T> buildQueryDto(T eq, T like)

### 构建分页对象
+ public static &lt;T> IPage&lt;T> buildIPage(Integer current, Integer size)  
```java
    IPage<T> page = QueryUtil.buildIPage(1, 20);
```
+ public static &lt;T, R> IPage&lt;T> buildIPage(IPage&lt;R> page, ConsumerConvert&lt;T, R> convert)
```java
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
```
或者
```java
    IPage<Dto> result = QueryUtil.buildIPage(page,i->{
       Dto dto = new Dto();
       dto.setXXX(i.getXXX());
       return dto;
    });
```

### 直接使用 IService<R> 或 BaseMapper<R> 进行查询并返回
#### 返回Dto对象
+ public static &lt;T, R> IPage&lt;T> queryPage(IService&lt;R> service, QueryDto dto, ConsumerConvert&lt;T, R> convert)
+ public static &lt;T, R> IPage&lt;T> queryPage(BaseMapper&lt;R> mapper, QueryDto dto, ConsumerConvert&lt;T, R> convert)
+ public static &lt;T, R> List&lt;T> query(IService&lt;R> service, QueryDto dto, ConsumerConvert&lt;T, R> convert)
+ public static &lt;T, R> List&lt;T> query(BaseMapper&lt;R> mapper, QueryDto dto, ConsumerConvert&lt;T, R> convert)
```java
    // 返回分页
    IPage<Dto> result = QueryUtil.queryPage(XXXDBService,query,i->Convert.convert(Dto.class,i));
    // 或者
    IPage<Dto> result = QueryUtil.queryPage(XXXDBService,query,i->{
        Dto dto = new Dto();
        dto.setXXX(i.getXXX());
        return dto;
    });

    // 返回列表
    List<Dto> result = QueryUtil.query(XXXDBService,query,i->Convert.convert(Dto.class,i));
    // 或者
    List<Dto> result = QueryUtil.query(XXXDBService,query,i->{
        Dto dto = new Dto();
        dto.setXXX(i.getXXX());
        return dto;
    });
```

#### 返回Po对象
+ public static &lt;R> IPage<R> queryPage(IService&lt;R> service, QueryDto dto)
+ public static &lt;R> IPage<R> queryPage(BaseMapper&lt;R> mapper, QueryDto dto)
+ public static &lt;R> List&lt;R> query(IService&lt;R> service, QueryDto dto)
+ public static &lt;R> List&lt;R> query(BaseMapper&lt;R> mapper, QueryDto dto)
```java
    // 返回分页
    IPage<Po> result = QueryUtil.queryPage(XXXDBService,query);
    
    // 返回列表
    List<Po> result = QueryUtil.query(XXXDBService,query);
```

### 设置 like 查询条件
+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setLike(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object like, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setLike(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object like, Class&lt;T> entityClass)
```java
    // IService 用法
    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setLike(XXXDBService.lambdaQuery(), query.getLike(), Po.class);
    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
    
    // BaseMapper 用法
    LambdaQueryChainWrapper<Po> queryWrapper = QueryUtil.setLike(new LambdaQueryWrapper<>(), query.getLike(), Po.class);
    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
```

### 设置 eq 查询条件
+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setEq(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setEq(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Class&lt;T> entityClass)
```java
    // IService 用法
    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setEq(XXXDBService.lambdaQuery(), query.getEq(), Po.class);
    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);

    // BaseMapper 用法
    LambdaQueryChainWrapper<Po> queryWrapper = QueryUtil.setEq(new LambdaQueryWrapper<>(), query.getEq(), Po.class);
    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
```

### 设置 eq 和 like 查询条件

+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setEqAndLike(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setEqAndLike(IService&lt;T> service, Object eq, Object like)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setEqAndLike(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setEqAndLike(Object eq, Object like, Class&lt;T> entityClass)
```java
    // IService 用法
    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setEqAndLike(XXXDBService.lambdaQuery()
                , query.getEq(), query.getLike(), Po.class);
    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);

    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setEqAndLike(XXXDBService, query.getEq(), query.getLike());
    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);

    // BaseMapper 用法
    LambdaQueryWrapper<Po> queryWrapper = QueryUtil.setEqAndLike(new LambdaQueryWrapper<>()
                , query.getEq(), query.getLike(), Po.class);
    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);

    LambdaQueryWrapper<Po> queryWrapper = QueryUtil.setEqAndLike(query.getEq(), query.getLike(), Po.class);
    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
```

### 设置 lt 和 ge 查询条件
+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setLtAndGe(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object lt, Object ge, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setLtAndGe(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object lt, Object ge, Class&lt;T> entityClass)
```java
    // IService 用法
    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setLtAndGe(XXXDBService.lambdaQuery()
                , query.getLt(), query.getGe(), Po.class);
    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);

    // BaseMapper 用法
    LambdaQueryWrapper<Po> queryWrapper = QueryUtil.setLtAndGe(new LambdaQueryWrapper<>()
                , query.getLt(), query.getGe(), Po.class);
    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
```

### 设置 等、包含、小于、大于等于全条件
一般用不到以下方法
+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setWrapper(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, QueryDto&lt;?> dto, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setWrapper(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, QueryDto&lt;?> dto, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryChainWrapper&lt;T> setWrapper(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Object lt, Object ge, Class&lt;T> entityClass)
+ public static &lt;T> LambdaQueryWrapper&lt;T> setWrapper(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Object lt, Object ge, Class&lt;T> entityClass)
 ```java
    // IService 用法
    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setWrapper(XXXDBService.lambdaQuery()
                , query.getEq(), query.getLike(), query.getLt(), query.getGe(), Po.class);
    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);

    // BaseMapper 用法
    LambdaQueryWrapper<Po> queryWrapper = new LambdaQueryWrapper<>();
    QueryUtil.setWrapper(queryWrapper, query.getEq(), query.getLike(), query.getLt(), query.getGe(), Po.class);
    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
 ```

### 设置 IN 条件
public static &lt;T> void in(Map&lt;String, List&lt;?>> in, Class&lt;?> entityClass, LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper)
public static &lt;T> void in(Map&lt;String, List&lt;?>> in, Class&lt;?> entityClass, LambdaQueryWrapper&lt;T> lambdaQueryWrapper)

### 设置排序
public static &lt;T> void orderBy(List&lt;String> order, boolean desc, Class&lt;?> entityClass, LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper)
public static &lt;T> void orderBy(List&lt;String> order, boolean desc, Class&lt;?> entityClass, LambdaQueryWrapper&lt;T> lambdaQueryWrapper)

## 查询Dto报文体 QueryDto

```java
    @ApiModel(value = "统一查询条件报文体")
    @Data
    @Accessors(chain = true)
    public static class QueryDto<T> {
        @ApiModelProperty(value = "分页-单页数量", dataType = "integer", allowableValues = "20")
        private Integer size = 20;
        @ApiModelProperty(value = "分页-页码", dataType = "integer", allowableValues = "1")
        private Integer current = 1;
        @ApiModelProperty(value = "等条件")
        private T eq;
        @ApiModelProperty(value = "包含条件")
        private T like;
        @ApiModelProperty(value = "小于条件")
        private T lt;
        @ApiModelProperty(value = "大于等于条件")
        private T ge;
        @ApiModelProperty(value = "其他条件")
        private T other;
        @ApiModelProperty(value = "in条件")
        private Map<String, List<?>> in;
        @ApiModelProperty(value = "排序字段-升序")
        private List<String> orderByAsc;
        @ApiModelProperty(value = "排序字段-降序")
        private List<String> orderByDesc;
    }
```
### 查询报文 JSON
```json
    {
        "eq": {
            "name": "xxxName"
        },
        "like": {
            "address": "xxx街道"
        },
        "ge": {
            "createTimr": "2023-01-01 00:00:00"
        },
        "lt": {
            "createTimr": "2023-01-02 00:00:00"
        },
        "in": {
            "id": [125,52,51]
        },
        "orderByDesc": [],
        "orderByAsc": []
    }
```

## 其他方法
+ 获取 BaseMapper&lt;T> 中T的类型  
public static &lt;T> Class&lt;T> getMapperEntityClass(BaseMapper&lt;T> mapper)
+ 获取SFunction 函数方法所表述的属性名
public static &lt;T> String lambdaFieldName(SFunction&lt;T, ?> function)
+ 构建查询对象  
public static &lt;T> QueryDto&lt;T> buildQueryDto(T eq, T like)
+ 据大于等于时间 加分量获取小于时间  
例如：2023-01-01 00:00:00  小于时间为2023-01-02 00:00:00
用法：lt = getLtByGe(ge,"D",1);
public static Date getLtByGe(Date ge, char unit, int val)

## 连表查询 或 自定义SQL查询
此工具主要简化 Mybatis Plus 基于 Lambda 的操作代码。  
所以连表或自定义SQL查询 需要依赖于Mybatis Plus 创建

+ 连表自定义查询示例
```java
    public interface CustomMapper extends BaseMapper<CustomPo>{
        @Select("select a.name,a.age,a.address,b.username,b.phone" +
                        " from UserDetail a left join UserAccount b" +
                        " ${ew.customSqlSegment}")
        List<CustomPo> find(@Param("ew") Wrapper<CustomPo> wrapper);

        @Data
        @Accessors(chain = true)
        class CustomPo {
            @TableField("a.name")
            private String name;
            @TableField("a.age")
            private Integer age;
            @TableField("a.address")
            private String address;
            @TableField("b.username")
            private String username;
            @TableField("b.phone")
            private String phone;
        }
    }
```

+ 使用 QueryUtil 查询
```java
    // 分页
    IPage<Dto> result = QueryUtil.queryPage(customMapper,query,i->Convert.convert(Dto.class,i));

    // 列表
    List<Dto> result = QueryUtil.query(customMapper,query,i->Convert.convert(Dto.class,i));

    // 返回数据对象
    List<Po> result = QueryUtil.query(XXXDBService,query);
    ...（方法操作与上述一直）
```
 


## 公共查询接口实现
解决微服务间查询数据 创建查询接口和查询相关代码
基于上述工具类构建一个公共Controller  根据表名（BaseMapper注册的BeanName） 动态查询表数据的实现
请求方式：Post
实例： 127.0.0.1:8080/jumper/page/user 或 127.0.0.1:8080/jumper/list/user
路径最后一级为变量 表示 IOC 中的 BaseMapper Bean Name
page与list  表示查询分页数据 和 例表数据

### 公共接口使用方式
微服务间调用使用FeignClient 即可。 也可在有接口权鉴的环境下对外暴露，或页面查询用。
```java
    public interface ServiceFeign {
        @PostMapping(value = "/jumper/page/userInfo")
        FeignResult<IPage<UserInfoDto>> queryUserInfoPage(QueryUtil.QueryDto dto);
    
        @PostMapping(value = "/jumper/list/userInfo")
        FeignResult<List<UserInfoDto>> queryUserInfoList(QueryUtil.QueryDto dto);
    
        class ServiceFeignImpl implements ServiceFeign {
            @Override
            public FeignResult<IPage<UserInfoDto>> queryUserInfoPage(QueryUtil.QueryDto dto) {
                return null;
            }
            @Override
            public FeignResult<List<UserInfoDto>> queryUserInfoList(QueryUtil.QueryDto dto) {
                return null;
            }
        }
    }
```

### 公共接口代码
```java
    /**
     * @ClassName JumperQueryController
     * @Description 依赖与 QueryUtil 工具构建的 公共数据查询接口
     * @Author Cyk
     * @Version 1.0
     * @since 2023/3/15 16:53
     **/
    @RequestMapping("/jumper")
    @RestController
    public class JumperQueryController {
    
        @Resource
        private ApplicationContext applicationContext;
    
        @PostMapping("page/{mapperBeanName}")
        public <T> ResultBody<IPage<?>> queryPage(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto dto) {
    
            BaseMapper<T> mapper = getBaseMapper(mapperBeanName);
    
            if (mapper != null) {
    
                IPage<T> page = QueryUtil.queryPage(mapper, dto);
    
                ResultBody result = convertDto(mapper, iConvert -> ResultBody.ok(QueryUtil.buildIPage(page, iConvert)));
                if (result != null) {
                    return result;
                }
    
                // 返回数据对象
                return ResultBody.ok(page);
            }
            // 返回方法不存在提示
            return ResultBody.build(ErrorCode.NOT_FOUND);
        }
    
        @PostMapping("list/{mapperBeanName}")
        public <T> ResultBody<List<?>> queryList(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto dto) {
    
            BaseMapper<T> mapper = getBaseMapper(mapperBeanName);
    
            if (mapper != null) {
    
                List<T> list = QueryUtil.query(mapper, dto);
    
                ResultBody result = convertDto(mapper, iConvert -> ResultBody.ok(QueryUtil.convertList(list, iConvert)));
                if (result != null) {
                    return result;
                }
    
                // 返回数据对象
                return ResultBody.ok(list);
            }
            // 返回方法不存在提示
            return ResultBody.build(ErrorCode.NOT_FOUND);
        }
    
        /**
         * 转换 Dto 对象
         *
         * @param mapper
         * @param function
         */
        private <T> ResultBody<?> convertDto(BaseMapper<T> mapper, Function<QueryUtil.IConvert, ResultBody<?>> function) {
            Map<String, QueryUtil.IConvert> convertBeans = applicationContext.getBeansOfType(QueryUtil.IConvert.class);
            for (QueryUtil.IConvert convert : convertBeans.values()) {
                Method convertMethod = convert.getClass().getMethods()[0];
                Class<?> poType = convertMethod.getParameterTypes()[0];
                if (poType.isAssignableFrom(QueryUtil.getMapperEntityClass(mapper))) {
                    // 返回转换Dto后的结果
                    return function.apply(convert);
                }
            }
            return null;
        }
    
        /**
         * 获取 BaseMapper<T> 实例
         *
         * @param mapperBeanName
         * @param <T>
         * @return
         */
        private <T> BaseMapper<T> getBaseMapper(String mapperBeanName) {
            try {
                if (!mapperBeanName.endsWith("Mapper")) mapperBeanName += "Mapper";
                return applicationContext.getBean(mapperBeanName, BaseMapper.class);
            } catch (Exception ignored) {
            }
            return null;
        }
    }
```

### 公共JumperFeignClient 实现
 + 动态 FeignClient 客户端工厂  
   使用方式
```java
     @RestController
     public class Controller {
         @Resource
         private JumperFeign jumperFeign;
     
         @PostMapping("/query/data")
         public TResult<IPage<DataDTO>> query(@RequestBody QueryUtil.QueryDto<DataDTO> query) {
             TResult<IPage<DataDTO>> result = jumperFeign.getClient("serverId").queryPage("MapperBeanName",query);
             return callBackLogDTOS;
         }
     }
```  
  实现代码
  ```java
      /**
       * 动态创建FeignClient 工具类
       */
      @Component
      public class FeignClientUtils implements ApplicationContextAware {
      
          private static ApplicationContext applicationContext = null;
          private static final Map<String, Object> BEAN_CACHE = new ConcurrentHashMap<>();
      
          @Override
          public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
              if (FeignClientUtils.applicationContext == null) {
                  FeignClientUtils.applicationContext = applicationContext;
              }
          }
      
          public static <T> T build(String serverName, String url, Class<T> targetClass) {
              return buildClient(serverName, url, targetClass, null);
          }
      
          public static <T> T build(String serverName, Class<T> targetClass) {
              return buildClient(serverName, null, targetClass, null);
          }
      
          public static <T> T build(String serverName, String url, Class<T> targetClass, Class<? extends T> fallback) {
              return buildClient(serverName, url, targetClass, fallback);
          }
      
          public static <T> T build(String serverName, Class<T> targetClass, Class<? extends T> fallback) {
              return buildClient(serverName, null, targetClass, fallback);
          }
      
          @SuppressWarnings("unchecked")
          private static <T> T buildClient(String serverName, String url, Class<T> targetClass, Class<? extends T> fallback) {
              T t = (T) BEAN_CACHE.get(serverName);
              if (Objects.isNull(t)) {
                  FeignClientBuilder.Builder<T> builder = new FeignClientBuilder(applicationContext).forType(targetClass, serverName).url(url).fallback(fallback);
                  t = builder.build();
                  BEAN_CACHE.put(serverName, t);
              }
              return t;
          }
      }
  ```
  + 动态JumperFeignClient 客户端工厂
  ```java
      public class JumperFeign {
      
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
          public JumperFeignClient getClient(String serverName,String url) {
              return FeignClientUtils.build(serverName, url, JumperFeignClient.class, JumperFeignClientImpl.class);
          }
      
      
          public interface JumperFeignClient {
              @PostMapping("/jumper/page/{mapperBeanName}")
              <T> TResult<IPage<T>> queryPage(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto<?> dto);
      
              @PostMapping("/jumper/list/{mapperBeanName}")
              <T> TResult<List<T>> queryList(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto<?> dto);
          }
      
          public static class JumperFeignClientImpl implements JumperFeignClient {
              @Override
              public <T> TResult<IPage<T>> queryPage(String mapperBeanName, QueryUtil.QueryDto<?> dto) {
                  return TResult.build(0, "服务不可用");
              }
      
              @Override
              public <T> TResult<List<T>> queryList(String mapperBeanName, QueryUtil.QueryDto<?> dto) {
                  return TResult.build(0, "服务不可用");
              }
          }
      }
  ```


