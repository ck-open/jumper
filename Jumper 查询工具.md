# 基于 Mybatis plus LambdaQuery 实现的 Jumper 查询工具
## 现状
>+ 多条件数据查询时需要大量的编写 eq、lt、ge、like、in 等方法设置条件并且每个条件基本需要判空。
>+ 简单查询通常会有controller -> service -> mapper。即使没有service 在controller中也会存在重复性代码。
>+ 微服务环境下，获取其他服务数据信息 feignClient -> controller -> service -> mapper  每种数据获取都需要一套完整流程代码。
>+ A服务查询B服务的查询逻辑写在B服务的查询接口中
>+ 每个服务都暴露了很多非本服务业务范围的查询接口

## 目标
>+ 简化条件参数设置过程，将基本查询抽象出共性以公共方式实现。  
>+ 将普通查询功能的controller、service层抽取出来，提供公共方式，直接通过mapper来实现查询。  
>+ 微服务下调用，实现公共controller查询方式后可以将feignClient 进行封装提供。并注入到容器中，方便使用。  
>+ 在公共controller中提供常用条件 eq、lt、ge、like、in、orderByAsc、orderByDesc等。
>+ 消灭服务暴露的非业务性查询接口
>+ 消灭连表查询编写的Mapper 或 xml配置

## QueryUtil方法
>所有方法中的 条件对象 QueryDto、eq 或 like  都非必传 可以为 null
>### 方法参数说明
>>```java
>> Integer current  查询页
>> Integer size     单页条数
>> IPage<R> page    分页对象
>> ConsumerConvert<T, R> convert  查询出的结果集PO 转 返回值Dto的实现函数
>> IService<R> service  实现Mybatis Plus IService<R> 接口的实现对象
>> QueryDto dto  统一查询条件对象
>> Object eq     等 条件对象
>> Object like   包含 条件对象
>> Object lt     小于 条件的参数对象
>> Object ge     大于等于 条件的参数对象
>> LambdaQueryWrapper<T> lambdaQueryWrapper 
>> LambdaQueryChainWrapper<T> lambdaQueryWrapper
>>```
>
>### 构建查询对象
>> + public static &lt;T> QueryDto&lt;T> buildQueryDto(T eq, T like)
>
>### 构建分页对象
>> + public static &lt;T> IPage&lt;T> buildIPage(Integer current, Integer size)  
>>> ```java
>>>     IPage<T> page = QueryUtil.buildIPage(1, 20);
>>> ```
>> + public static &lt;T, R> IPage&lt;T> buildIPage(IPage&lt;R> page, ConsumerConvert&lt;T, R> convert)
>>> ```java
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> ```
>>> 或者
>>> ```java
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,i->{
>>>        Dto dto = new Dto();
>>>        dto.setXXX(i.getXXX());
>>>        return dto;
>>>     });
>>> ```
>
>### 直接使用 IService<R> 或 BaseMapper<R> 进行查询并返回
>> #### 返回Dto对象
>>> + public static &lt;T, R> IPage&lt;T> queryPage(IService&lt;R> service, QueryDto dto, ConsumerConvert&lt;T, R> convert)
>>> + public static &lt;T, R> IPage&lt;T> queryPage(BaseMapper&lt;R> mapper, QueryDto dto, ConsumerConvert&lt;T, R> convert)
>>> + public static &lt;T, R> List&lt;T> query(IService&lt;R> service, QueryDto dto, ConsumerConvert&lt;T, R> convert)
>>> + public static &lt;T, R> List&lt;T> query(BaseMapper&lt;R> mapper, QueryDto dto, ConsumerConvert&lt;T, R> convert)
>>>> ```java
>>>>     // 返回分页
>>>>     IPage<Dto> result = QueryUtil.queryPage(XXXDBService,query,i->Convert.convert(Dto.class,i));
>>>>     // 或者
>>>>     IPage<Dto> result = QueryUtil.queryPage(XXXDBService,query,i->{
>>>>         Dto dto = new Dto();
>>>>         dto.setXXX(i.getXXX());
>>>>         return dto;
>>>>     });
>>>> 
>>>>     // 返回列表
>>>>     List<Dto> result = QueryUtil.query(XXXDBService,query,i->Convert.convert(Dto.class,i));
>>>>     // 或者
>>>>     List<Dto> result = QueryUtil.query(XXXDBService,query,i->{
>>>>         Dto dto = new Dto();
>>>>         dto.setXXX(i.getXXX());
>>>>         return dto;
>>>>     });
>>>> ```

>> #### 返回Po对象
>>> + public static &lt;R> IPage<R> queryPage(IService&lt;R> service, QueryDto dto)
>>> + public static &lt;R> IPage<R> queryPage(BaseMapper&lt;R> mapper, QueryDto dto)
>>> + public static &lt;R> List&lt;R> query(IService&lt;R> service, QueryDto dto)
>>> + public static &lt;R> List&lt;R> query(BaseMapper&lt;R> mapper, QueryDto dto)
>>>> ```java
>>>>     // 返回分页
>>>>     IPage<Po> result = QueryUtil.queryPage(XXXDBService,query);
>>>>     
>>>>     // 返回列表
>>>>     List<Po> result = QueryUtil.query(XXXDBService,query);
>>>> ```

>### 设置 like 查询条件
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setLike(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object like, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setLike(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object like, Class&lt;T> entityClass)
>>> ```java
>>>     // IService 用法
>>>     LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setLike(XXXDBService.lambdaQuery(), query.getLike(), Po.class);
>>>     IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>>     
>>>     // BaseMapper 用法
>>>     LambdaQueryChainWrapper<Po> queryWrapper = QueryUtil.setLike(new LambdaQueryWrapper<>(), query.getLike(), Po.class);
>>>     IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> ```

>### 设置 eq 查询条件
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setEq(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setEq(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Class&lt;T> entityClass)
>>> ```java
>>>     // IService 用法
>>>     LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setEq(XXXDBService.lambdaQuery(), query.getEq(), Po.class);
>>>     IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> 
>>>     // BaseMapper 用法
>>>     LambdaQueryChainWrapper<Po> queryWrapper = QueryUtil.setEq(new LambdaQueryWrapper<>(), query.getEq(), Po.class);
>>>     IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> ```

>### 设置 eq 和 like 查询条件
>> 
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setEqAndLike(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setEqAndLike(IService&lt;T> service, Object eq, Object like)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setEqAndLike(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setEqAndLike(Object eq, Object like, Class&lt;T> entityClass)
>>> ```java
>>>     // IService 用法
>>>     LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setEqAndLike(XXXDBService.lambdaQuery()
>>>                 , query.getEq(), query.getLike(), Po.class);
>>>     IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> 
>>>     LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setEqAndLike(XXXDBService, query.getEq(), query.getLike());
>>>     IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> 
>>>     // BaseMapper 用法
>>>     LambdaQueryWrapper<Po> queryWrapper = QueryUtil.setEqAndLike(new LambdaQueryWrapper<>()
>>>                 , query.getEq(), query.getLike(), Po.class);
>>>     IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> 
>>>     LambdaQueryWrapper<Po> queryWrapper = QueryUtil.setEqAndLike(query.getEq(), query.getLike(), Po.class);
>>>     IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> ```
>
>### 设置 lt 和 ge 查询条件
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setLtAndGe(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object lt, Object ge, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setLtAndGe(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object lt, Object ge, Class&lt;T> entityClass)
>>> ```java
>>>     // IService 用法
>>>     LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setLtAndGe(XXXDBService.lambdaQuery()
>>>                 , query.getLt(), query.getGe(), Po.class);
>>>     IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> 
>>>     // BaseMapper 用法
>>>     LambdaQueryWrapper<Po> queryWrapper = QueryUtil.setLtAndGe(new LambdaQueryWrapper<>()
>>>                 , query.getLt(), query.getGe(), Po.class);
>>>     IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
>>>     IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> ```
>
>### 设置 等、包含、小于、大于等于全条件
> 一般用不到以下方法
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setWrapper(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, QueryDto&lt;?> dto, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setWrapper(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, QueryDto&lt;?> dto, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryChainWrapper&lt;T> setWrapper(LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Object lt, Object ge, Class&lt;T> entityClass)
>> + public static &lt;T> LambdaQueryWrapper&lt;T> setWrapper(LambdaQueryWrapper&lt;T> lambdaQueryWrapper, Object eq, Object like, Object lt, Object ge, Class&lt;T> entityClass)
>>> ```java
>>>    // IService 用法
>>>    LambdaQueryChainWrapper<Po> queryChainWrapper = QueryUtil.setWrapper(XXXDBService.lambdaQuery()
>>>                , query.getEq(), query.getLike(), query.getLt(), query.getGe(), Po.class);
>>>    IPage<Po> page = queryChainWrapper.page(QueryUtil.buildIPage(query.getCurrent(), query.getSize()));
>>>    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>>
>>>    // BaseMapper 用法
>>>    LambdaQueryWrapper<Po> queryWrapper = new LambdaQueryWrapper<>();
>>>    QueryUtil.setWrapper(queryWrapper, query.getEq(), query.getLike(), query.getLt(), query.getGe(), Po.class);
>>>    IPage<Po> page = XXXMapper().selectPage(QueryUtil.buildIPage(query.getCurrent(), query.getSize()),queryWrapper);
>>>    IPage<Dto> result = QueryUtil.buildIPage(page,PoToDtoConvert::PoToDto);
>>> ```
>
>### 设置 IN 条件
>> + public static &lt;T> void in(Map&lt;String, List&lt;?>> in, Class&lt;?> entityClass, LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper)
>> + public static &lt;T> void in(Map&lt;String, List&lt;?>> in, Class&lt;?> entityClass, LambdaQueryWrapper&lt;T> lambdaQueryWrapper)
>
>### 设置排序
>> + public static &lt;T> void orderBy(List&lt;String> order, boolean desc, Class&lt;?> entityClass, LambdaQueryChainWrapper&lt;T> lambdaQueryWrapper)
>> + public static &lt;T> void orderBy(List&lt;String> order, boolean desc, Class&lt;?> entityClass, LambdaQueryWrapper&lt;T> lambdaQueryWrapper)

## 查询Dto报文体 QueryDto
> ```java
>     @ApiModel(value = "统一查询条件报文体")
>     @Data
>     @Accessors(chain = true)
>     public static class QueryDto<T> {
>         @ApiModelProperty(value = "分页-单页数量", dataType = "integer", allowableValues = "20")
>         private Integer size = 20;
>         @ApiModelProperty(value = "分页-页码", dataType = "integer", allowableValues = "1")
>         private Integer current = 1;
>         @ApiModelProperty(value = "等条件")
>         private T eq;
>         @ApiModelProperty(value = "包含条件")
>         private T like;
>         @ApiModelProperty(value = "小于条件")
>         private T lt;
>         @ApiModelProperty(value = "大于等于条件")
>         private T ge;
>         @ApiModelProperty(value = "其他条件")
>         private T other;
>         @ApiModelProperty(value = "in条件")
>         private Map<String, List<?>> in;
>         @ApiModelProperty(value = "排序字段-升序")
>         private List<String> orderByAsc;
>         @ApiModelProperty(value = "排序字段-降序")
>         private List<String> orderByDesc;
>     }
> ```
> ### 查询报文 JSON
>> ```json
>>     {
>>         "eq": {
>>             "name": "xxxName"
>>         },
>>         "like": {
>>             "address": "xxx街道"
>>         },
>>         "ge": {
>>             "createTimr": "2023-01-01 00:00:00"
>>         },
>>         "lt": {
>>             "createTimr": "2023-01-02 00:00:00"
>>         },
>>         "in": {
>>             "id": [125,52,51]
>>         },
>>         "orderByDesc": [],
>>         "orderByAsc": []
>>     }
>> ```

## 其他方法
> + 获取 BaseMapper&lt;T> 中T的类型  
>> public static &lt;T> Class&lt;T> getMapperEntityClass(BaseMapper&lt;T> mapper)
> + 获取SFunction 函数方法所表述的属性名
>> public static &lt;T> String lambdaFieldName(SFunction&lt;T, ?> function)
> + 构建查询对象  
>> public static &lt;T> QueryDto&lt;T> buildQueryDto(T eq, T like)
> + 据大于等于时间 加分量获取小于时间  
>> 例如：2023-01-01 00:00:00  小于时间为2023-01-02 00:00:00
>> 用法：lt = getLtByGe(ge,"D",1);
>> public static Date getLtByGe(Date ge, char unit, int val)

## 连表查询 或 自定义SQL查询
> 此工具主要简化 Mybatis Plus 基于 Lambda 的操作代码。  
> 所以连表或自定义SQL查询 需要依赖于Mybatis Plus 创建
> 
> + 连表自定义查询示例
>> ```java
>>     public interface CustomMapper extends BaseMapper<CustomPo>{
>>         @Select("select a.name,a.age,a.address,b.username,b.phone" +
>>                         " from UserDetail a left join UserAccount b" +
>>                         " ${ew.customSqlSegment}")
>>         List<CustomPo> find(@Param("ew") Wrapper<CustomPo> wrapper);
>> 
>>         @Data
>>         @Accessors(chain = true)
>>         class CustomPo {
>>             @TableField("a.name")
>>             private String name;
>>             @TableField("a.age")
>>             private Integer age;
>>             @TableField("a.address")
>>             private String address;
>>             @TableField("b.username")
>>             private String username;
>>             @TableField("b.phone")
>>             private String phone;
>>         }
>>     }
>> ```
>> 或者 (使用 queryPage 或 queryList 方法需要下面这样写)
>> ```java
>>     public interface CustomMapper extends BaseMapper<CustomPo>{
>> 
>>         @TableName(value = " UserDetail a left join UserAccount b ")
>>         @Data
>>         @Accessors(chain = true)
>>         class CustomPo {
>>             @TableField("a.name")
>>             private String name;
>>             @TableField("a.age")
>>             private Integer age;
>>             @TableField("a.address")
>>             private String address;
>>             @TableField("b.username")
>>             private String username;
>>             @TableField("b.phone")
>>             private String phone;
>>         }
>>     }
>> ```
> 
> + 使用 QueryUtil 查询
>> ```java
>>     // 分页
>>     IPage<Dto> result = QueryUtil.queryPage(customMapper,query,i->Convert.convert(Dto.class,i));
>> 
>>     // 列表
>>     List<Dto> result = QueryUtil.query(customMapper,query,i->Convert.convert(Dto.class,i));
>> 
>>     // 返回数据对象
>>     List<Po> result = QueryUtil.query(customMapper,query);
>>     ...（方法操作与上述一直）
>> ```
 


## 公共查询接口实现
> + 解决微服务间查询数据 创建查询接口和查询相关代码
> + 基于上述工具类构建一个公共Controller  根据表名（BaseMapper注册的BeanName） 动态查询表数据的实现
> + 请求方式：Post
> + 实例： 127.0.0.1:8080/jumper/page/user 或 127.0.0.1:8080/jumper/list/user
> + 路径最后一级为变量 表示 IOC 中的 BaseMapper Bean Name
> + page与list  表示查询分页数据 和 例表数据
> 
> ### 公共 Feign 代码
> 微服务间调用使用FeignClient 即可。 也可在有接口权鉴的环境下对外暴露，或页面查询用。
>> ```java
>>     public interface ServiceFeign {
>>         @PostMapping(value = "/jumper/page/userInfo")
>>         FeignResult<IPage<UserInfoDto>> queryUserInfoPage(QueryUtil.QueryDto dto);
>>     
>>         @PostMapping(value = "/jumper/list/userInfo")
>>         FeignResult<List<UserInfoDto>> queryUserInfoList(QueryUtil.QueryDto dto);
>>     
>>         class ServiceFeignImpl implements ServiceFeign {
>>             @Override
>>             public FeignResult<IPage<UserInfoDto>> queryUserInfoPage(QueryUtil.QueryDto dto) {
>>                 return null;
>>             }
>>             @Override
>>             public FeignResult<List<UserInfoDto>> queryUserInfoList(QueryUtil.QueryDto dto) {
>>                 return null;
>>             }
>>         }
>>     }
>> ```
> 
> ### 公共 Controller 代码
> ```java
>     /**
>      * @ClassName JumperQueryController
>      * @Description 依赖与 QueryUtil 工具构建的 公共数据查询接口
>      * @Author Cyk
>      * @Version 1.0
>      * @since 2023/3/15 16:53
>      **/
>     @RequestMapping("/jumper")
>     @RestController
>     public class JumperQueryController {
>     
>         @Resource
>         private ApplicationContext applicationContext;
>     
>         @PostMapping("page/{mapperBeanName}")
>         public <T> ResultBody<IPage<?>> queryPage(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto dto) {
>     
>             BaseMapper<T> mapper = getBaseMapper(mapperBeanName);
>     
>             if (mapper != null) {
>     
>                 IPage<T> page = QueryUtil.queryPage(mapper, dto);
>     
>                 ResultBody result = convertDto(mapper, iConvert -> ResultBody.ok(QueryUtil.buildIPage(page, iConvert)));
>                 if (result != null) {
>                     return result;
>                 }
>     
>                 // 返回数据对象
>                 return ResultBody.ok(page);
>             }
>             // 返回方法不存在提示
>             return ResultBody.build(ErrorCode.NOT_FOUND);
>         }
>     
>         @PostMapping("list/{mapperBeanName}")
>         public <T> ResultBody<List<?>> queryList(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto dto) {
>     
>             BaseMapper<T> mapper = getBaseMapper(mapperBeanName);
>     
>             if (mapper != null) {
>     
>                 List<T> list = QueryUtil.query(mapper, dto);
>     
>                 ResultBody result = convertDto(mapper, iConvert -> ResultBody.ok(QueryUtil.convertList(list, iConvert)));
>                 if (result != null) {
>                     return result;
>                 }
>     
>                 // 返回数据对象
>                 return ResultBody.ok(list);
>             }
>             // 返回方法不存在提示
>             return ResultBody.build(ErrorCode.NOT_FOUND);
>         }
>     
>         /**
>          * 转换 Dto 对象
>          *
>          * @param mapper
>          * @param function
>          */
>         private <T> ResultBody<?> convertDto(BaseMapper<T> mapper, Function<QueryUtil.IConvert, ResultBody<?>> function) {
>             Map<String, QueryUtil.IConvert> convertBeans = applicationContext.getBeansOfType(QueryUtil.IConvert.class);
>             for (QueryUtil.IConvert convert : convertBeans.values()) {
>                 Method convertMethod = convert.getClass().getMethods()[0];
>                 Class<?> poType = convertMethod.getParameterTypes()[0];
>                 if (poType.isAssignableFrom(QueryUtil.getMapperEntityClass(mapper))) {
>                     // 返回转换Dto后的结果
>                     return function.apply(convert);
>                 }
>             }
>             return null;
>         }
>     
>         /**
>          * 获取 BaseMapper<T> 实例
>          *
>          * @param mapperBeanName
>          * @param <T>
>          * @return
>          */
>         private <T> BaseMapper<T> getBaseMapper(String mapperBeanName) {
>             try {
>                 if (!mapperBeanName.endsWith("Mapper")) mapperBeanName += "Mapper";
>                 return applicationContext.getBean(mapperBeanName, BaseMapper.class);
>             } catch (Exception ignored) {
>             }
>             return null;
>         }
>     }
> ```
> 
> ### 公共JumperFeignClient 使用方式
>  + 动态 FeignClient 客户端  使用方式
>    
>> ```java
>>      @RestController
>>      public class Controller {
>>          @Resource
>>          private JumperFeign jumperFeign;
>>      
>>          @PostMapping("/query/data")
>>          public TResult<IPage<DataDTO>> query(@RequestBody QueryUtil.QueryDto<DataDTO> query) {
>>              TResult<IPage<DataDTO>> result = jumperFeign.getClient("serverId").queryPage("MapperBeanName",query);
>>              return callBackLogDTOS;
>>          }
>>      }
>> ```  
> + 动态创建 FeignClient 实现代码
>> ```java
>>     /**
>>      * 动态创建FeignClient 工具类
>>      */
>>     @Component
>>     public class FeignClientUtils implements ApplicationContextAware {
>>     
>>         private static ApplicationContext applicationContext = null;
>>         private static final Map<String, Object> BEAN_CACHE = new ConcurrentHashMap<>();
>>     
>>         @Override
>>         public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
>>             if (FeignClientUtils.applicationContext == null) {
>>                 FeignClientUtils.applicationContext = applicationContext;
>>             }
>>         }
>>     
>>         public static <T> T build(String serverName, String url, Class<T> targetClass) {
>>             return buildClient(serverName, url, targetClass, null);
>>         }
>>     
>>         public static <T> T build(String serverName, Class<T> targetClass) {
>>             return buildClient(serverName, null, targetClass, null);
>>         }
>>     
>>         public static <T> T build(String serverName, String url, Class<T> targetClass, Class<? extends T> fallback) {
>>             return buildClient(serverName, url, targetClass, fallback);
>>         }
>>     
>>         public static <T> T build(String serverName, Class<T> targetClass, Class<? extends T> fallback) {
>>             return buildClient(serverName, null, targetClass, fallback);
>>         }
>>     
>>         @SuppressWarnings("unchecked")
>>         private static <T> T buildClient(String serverName, String url, Class<T> targetClass, Class<? extends T> fallback) {
>>             T t = (T) BEAN_CACHE.get(serverName);
>>             if (Objects.isNull(t)) {
>>                 FeignClientBuilder.Builder<T> builder = new FeignClientBuilder(applicationContext).forType(targetClass, serverName).url(url).fallback(fallback);
>>                 t = builder.build();
>>                 BEAN_CACHE.put(serverName, t);
>>             }
>>             return t;
>>         }
>>     }
>> ```
> + 动态创建 JumperFeignClient 客户端 实现代码
>> ```java
>>     public class JumperFeign {
>>     
>>         /**
>>          * 获取 JumperFeignClient
>>          *
>>          * @param serverName 服务名称（ServerId）
>>          * @return
>>          */
>>         public JumperFeignClient getClient(String serverName) {
>>             return FeignClientUtils.build(serverName, JumperFeignClient.class, JumperFeignClientImpl.class);
>>         }
>>     
>>         /**
>>          * 获取 JumperFeignClient
>>          *
>>          * @param serverName 服务名称（ServerId）
>>          * @return
>>          */
>>         public JumperFeignClient getClient(String serverName,String url) {
>>             return FeignClientUtils.build(serverName, url, JumperFeignClient.class, JumperFeignClientImpl.class);
>>         }
>>     
>>     
>>         public interface JumperFeignClient {
>>             @PostMapping("/jumper/page/{mapperBeanName}")
>>             <T> TResult<IPage<T>> queryPage(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto<?> dto);
>>     
>>             @PostMapping("/jumper/list/{mapperBeanName}")
>>             <T> TResult<List<T>> queryList(@PathVariable(value = "mapperBeanName") String mapperBeanName, @RequestBody(required = false) QueryUtil.QueryDto<?> dto);
>>         }
>>     
>>         public static class JumperFeignClientImpl implements JumperFeignClient {
>>             @Override
>>             public <T> TResult<IPage<T>> queryPage(String mapperBeanName, QueryUtil.QueryDto<?> dto) {
>>                 return TResult.build(0, "服务不可用");
>>             }
>>     
>>             @Override
>>             public <T> TResult<List<T>> queryList(String mapperBeanName, QueryUtil.QueryDto<?> dto) {
>>                 return TResult.build(0, "服务不可用");
>>             }
>>         }
>>     }
>> ```

## 运行时动态生成 BaseMapper 接口并注入容器
> + 通过自定义Sql语句（不支持sql中有子查询）动态生成 继承自BaseMapper的 interface.class 文件以及 Po内部类。
> + 提供 动态BaseMapper 在Spring 容器的注入、销毁和重载方法
> + 动态生成的 BaseMapper 可直接使用公共查询接口进行查询
>
> ### 自动生成的 class文件
>> 生成过程如下：
>>> 1. 将 sql 语句解析成字符串表述的 XXXBaseMapper.java 源码文本字符串。
>>> 2. 将 XXXBaseMapper.java 源码文本字符串 通过 JavaCompiler 编译成.class文件。
>>> 3. 将编译后的.class文件写出的指定的项目目录下，并加载到JVM。
>>> 4. 将 得到的 Class<?> 接口 加载到 SqlSessionTemplate.Configuration 的 Mapper 列表中。
>>> 5. 根据加载的 Class<?> 接口 构建BaseMapper 默认实现对象，并加载到 Spring 容器。
>>> 6. 此时就可以通过上面介绍的 JumperQueryController 公共查询接口进行相关查询操作了。
>
> ### Sql生成的 BaseMapper.java 源码
>> + 生成的源码文件不会文件保存，但会在日志中打印。
>> + Sql中不需要携带 where 条件，携带也将会被忽略，所有的查询条件都需要在使用时由使用者指定。
>> + Sql 中不可以使用 select * 查询，因为需要根据指定的参数构建Po对象。可以指定别名，不指定将自动驼峰。
>> + Sql 生成过程示例：
>>> ```sql
>>>     select uc.name,uc.password,c.customer_code,c.nick_name,c.card_type,c.card_id 
>>>     from user_credentials uc left join customer c on uc.customer_code=c.customer_code
>>> ```
>>> ```java
>>>    package jumper.db.mapper;
>>>    import com.baomidou.mybatisplus.annotation.TableField;
>>>    import com.baomidou.mybatisplus.annotation.TableName;
>>>    import org.apache.ibatis.annotations.Mapper;
>>>    import com.baomidou.mybatisplus.core.mapper.BaseMapper;
>>>    import lombok.Data;
>>>    import java.io.Serializable;
>>>    public interface UserInfoMapper extends BaseMapper<UserInfoMapper.UserInfo> {
>>>        @Data
>>>        @TableName(value = " user_credentials uc left join customer c on uc.customer_code=c.customer_code ")
>>>        public static class UserInfo implements Serializable{
>>>            @TableField("uc.name")
>>>            private Object name;
>>>            @TableField("uc.password")
>>>            private Object password;
>>>            @TableField("c.customer_code")
>>>            private Object customerCode;
>>>            @TableField("c.nick_name")
>>>            private Object nickName;
>>>            @TableField("c.card_type")
>>>            private Object cardType;
>>>            @TableField("c.card_id")
>>>            private Object cardId;
>>>    
>>>        }
>>>    }
>>> ```
>
>### 自定义 Sql 编译 BaseMapper 源码构建过程
>> 继承 SqlCompileConfiguration Sql 编译配置类，重写父类方法实现构建过程介入。
>> ```java
>>  /**
>>   * 参与 Sql 编译 BaseMapper java源码过程，自定义构建规则。重写父类方法实现
>>   */
>>  @Configuration
>>  public class MySqlCompileConfiguration extends SqlCompileConfiguration {
>>  }
>> ```
>
> ### 自定义 SQL BaseMapper 启动时自动注入
>> + 向容器中注入 SqlCompileSupplier 接口实例。
>> + 在服务启动后会调用 SqlCompileSupplier接口的 getSql() 方法进行自动构建 BaseMapper 并注入容器。
>> + 示例：
>>> ```java
>>>     @Configuration
>>>     public class SqlCompile implements SqlCompileSupplier {
>>>         /**
>>>          * 需要编译的 Sql 列表
>>>          * key: beanName  value: Sql语句
>>>          *
>>>          * @return
>>>          */
>>>         @Override
>>>         public Map<String, String> getSql() {
>>>             Map<String,String> sqlMap = new HashMap<>();
>>>             
>>>             sqlMap.put("UserInfo","select uc.name,uc.password,c.customer_code,c.nick_name,c.card_type,c.card_id from user_credentials uc left join customer c on uc.customer_code=c.customer_code");
>>>             
>>>             return sqlMap;
>>>         }
>>>     }
>>> ```
>
> ### 运行时 动态BaseMapper 构建销毁操作
>> 以下接口已集成，可以参考在项目中自定义改造。接口默认不可用 需要在yml配置中添加开启参数
>> 接口示例：
>>> ```java
>>>     @Api(tags = "动态加载BaseMapper")
>>>     @RequestMapping("/jumperSqlCompile")
>>>     public class SqlCompileBaseMapperController {
>>>     
>>>         @Resource
>>>         private SqlCompileBaseMapper sqlCompileBaseMapper;
>>>     
>>>         @ApiOperation(value = "Sql动态构建BaseMapper")
>>>         @GetMapping("/registry")
>>>         @ResponseBody
>>>         public TResult<Boolean> registryMapper(@RequestParam(value = "className") String className, @RequestParam(value = "sql") String sql) {
>>> 
>>>             return TResult.ok(sqlCompileBaseMapper.registryBaseMapper(className, sql));
>>>         }
>>>     
>>>         @ApiOperation(value = "重置Sql动态构建BaseMapper")
>>>         @GetMapping("/reset")
>>>         @ResponseBody
>>>         public TResult<Boolean> resetMapper(@RequestParam(value = "className") String className, @RequestParam(value = "sql") String sql) {
>>> 
>>>             return TResult.ok(sqlCompileBaseMapper.resetBaseMapper(className, sql));
>>>         }
>>>     
>>>         @ApiOperation(value = "卸载BaseMapper")
>>>         @GetMapping("/destroy")
>>>         @ResponseBody
>>>         public TResult<Boolean> destroyMapper(@RequestParam(value = "className") String className) {
>>> 
>>>             if (!className.endsWith("Mapper")) {
>>>                 className += "Mapper";
>>>             }
>>>             return TResult.ok(sqlCompileBaseMapper.destroyBaseMapper(className));
>>>         }
>>>     }
>>> ```
>
>### 配置参数信息
>> ```json
>>  {
>>    "properties": [
>>      {
>>        "name": "jumper.enabled",
>>        "type": "java.lang.Boolean",
>>        "description": "开启 Jumper 支持."
>>      },
>>      {
>>        "name": "jumper.SqlCompile.controller",
>>        "type": "java.lang.Boolean",
>>        "description": "开启 Sql 编译注入 BaseMapper 接口."
>>      },{
>>        "name": "jumper.package_mapper",
>>        "type": "java.lang.String",
>>        "description": "保存生成的BaseMapper class文件的包地址"
>>      },{
>>        "name": "jumper.DbType",
>>        "type": "java.lang.String",
>>        "description": "数据库类型 默认Mysql"
>>      }
>>    ]
>>  }
>> ```
>
>## 注意
>> ### 不配置 jumper.package_mapper 参数时
>>> 不指定编译后的 class文件存储包路径，则默认使用 jumper.db.mapper，保存在项目下。
>>> 使用 jumper.db.mapper 包路径时，注意Sql日志打印问题，如果需要打印，则需要设置此路径的日志级别。
>>> 结构如下：
>>> ```text
>>>  ├── classes
>>>     ├── com  项目代码包
>>>     ├── jumper
>>>         ├── db
>>>             ├── mapper
>>>                 ├── UserInfoMapper.class
>>> ```
>
>## 快速集成
>> ### 环境需求
>>> 由于项目依赖与 Spring Boot、Spring Mvc、MyBatis-Plus。   
>>> 并且 未杜绝使用中版本冲突项目未打包相关依赖，
>>> 所以项目中需要使用这自行引入相关Jar
>
>> ### 依赖配置
>>> ```xml
>>>     <dependencies>
>>>         <!-- Spring核心依赖 -->
>>>         <dependency>
>>>             <groupId>org.springframework.boot</groupId>
>>>             <artifactId>spring-boot-starter</artifactId>
>>>         </dependency>
>>> 
>>>         <!-- WEB 服务依赖 -->
>>>         <dependency>
>>>             <groupId>org.springframework.boot</groupId>
>>>             <artifactId>spring-boot-starter-web</artifactI
>>>         </dependency>
>>> 
>>>         <!-- Mybatis-plus包 -->
>>>         <dependency>
>>>             <groupId>com.baomidou</groupId>
>>>             <artifactId>mybatis-plus-boot-starter</artifac
>>>             <version>3.4.1</version>
>>>         </dependency>
>>> 
>>>         <!-- 微服务跨越查询(本功能集成jar)-->
>>>         <dependency>
>>>             <groupId>io.github.ck-open</groupId>
>>>             <artifactId>jumper-starter</artifactId>
>>>             <version>1.0.1</version>
>>>         </dependency>
>>> 
>>>         <!-- MySql依赖包 -->
>>>         <dependency>
>>>             <groupId>mysql</groupId>
>>>             <artifactId>mysql-connector-java</artifactId>
>>>             <version>8.0.30</version>
>>>         </dependency>
>>>     </dependencies>
>>> ```
>
>> ### 功能开启yml配置
>>> ```yaml
>>>     jumper:
>>>       enabled: true  # 启用跨服务数据查询功能
>>>       package_mapper: com.ck.db.po  # 根据Sql 动态提供查询时指定生成的.class存储路径
>>>       SqlCompile:
>>>         controller: true  # 开启 Sql 编译并加载 BaseMapper.class Http操作接口支持
>>> ```
>
>### Sql 动态编译 Http 接口示例
>> + 编译接口
>>> ```text
>>>     127.0.0.1:8080/jumperSqlCompile/registry?className=userInfo&sql=select uc.name,uc.password,c.nick_name,c.customer_code,c.card_type,c.card_id,c.address from customer c left join user_credentials uc on c.customer_code=uc.customer_code
>>> ```
>> + 重新编译接口
>>> ```text
>>>     127.0.0.1:8080/jumperSqlCompile/reset?className=userInfo&sql=select uc.name,uc.password,c.nick_name,c.customer_code,c.card_type,c.card_id,c.address from customer c left join user_credentials uc on c.customer_code=uc.customer_code
>>> ``` 
>> + 卸载接口
>>> ```text
>>>     127.0.0.1:8080/jumperSqlCompile/destroy?className=userInfo
>>> ```
>>
>> + 查询编译的接口
>>> 具体操作方式参考上面 公共查询接口实现 介绍。  
>>> 两个接口：分页查询、列表查询。
>>> ```text
>>>     - 请求方式：POST
>>>     - 条件传参：Content-Type = application/json
>>>     - 127.0.0.1:8080/jumper/page/userInfo
>>>     - 127.0.0.1:8080/jumper/list/userInfo
>>> ```


