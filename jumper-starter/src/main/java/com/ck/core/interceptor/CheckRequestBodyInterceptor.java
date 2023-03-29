package com.ck.core.interceptor;//package com.com.ck.core.interceptor;
//
//import com.com.ck.check_bean.CheckResult;
//import com.com.ck.check_bean.CheckValueUtil;
//import com.com.ck.check_bean.annotation.CheckFlag;
//import com.com.ck.exception.CheckException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.MethodParameter;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpInputMessage;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.util.ObjectUtils;
//import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
//
//import java.lang.reflect.Type;
//import java.util.List;
//import java.util.Objects;
//
//
//@Slf4j
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class CheckRequestBodyInterceptor extends RequestBodyAdviceAdapter {
//
//    /**
//     * 拦截器是否启用
//     * Invoked first to determine if this interceptor applies.
//     *
//     * @param methodParameter the method parameter
//     * @param type            the target type, not necessarily the same as the method
//     *                        parameter type, e.g. for {@code HttpEntity<String>}.
//     * @param aClass          the selected converter type
//     * @return whether this interceptor should be invoked or not
//     */
//    @Override
//    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
////        String simpleName = methodParameter.getContainingClass().getSimpleName();
////        if (simpleName.equals("通过包含类来确定")) {
////            return false;
////        }
//        return true;
//    }
//
//    /**
//     * 在读取和转换请求体之前被调用。
//     * Invoked second before the request body is read and converted.
//     *
//     * @param inputMessage  the request
//     * @param parameter     the target method parameter
//     * @param targetType    the target type, not necessarily the same as the method
//     *                      parameter type, e.g. for {@code HttpEntity<String>}.
//     * @param converterType the converter used to deserialize the body
//     * @return the input request or a new instance (never {@code null})
//     */
//    @Override
//    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
//                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
////        String simpleName = parameter.getContainingClass().getSimpleName();
////        if (simpleName.equals(Object.class.getSimpleName())){
////            return inputMessage;
////        }
//        return inputMessage;
//    }
//
//    /**
//     * 在读取和转换请求体之后被调用。
//     * Invoked third (and last) after the request body is converted to an Object.
//     *
//     * @param body          set to the converter Object before the first advice is called
//     * @param inputMessage  the request
//     * @param parameter     the target method parameter
//     * @param targetType    the target type, not necessarily the same as the method
//     *                      parameter type, e.g. for {@code HttpEntity<String>}.
//     * @param converterType the converter used to deserialize the body
//     * @return the same body or a new instance
//     */
//    @Override
//    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
//                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//        List<CheckResult> results = null;
//        try {
//
//            CheckFlag flag = Objects.requireNonNull(parameter.getMethod()).getDeclaredAnnotation(CheckFlag.class);
//            results = CheckValueUtil.checkBeanFieldIsNotNull(body, flag == null || "".equalsIgnoreCase(flag.value().trim()) ? null : flag.value());
//        } catch (Exception e) {
//            log.error(String.format("Web 接口[%s]报文体非空规则校验异常", parameter.getExecutable()), e);
//            throw new CheckException("报文体检查异常请联系系统管理员");
//        }
//        if (!ObjectUtils.isEmpty(results)) {
//            throw new CheckException("报文体错误：" + results.get(0).getMessage());
//        }
//        return body;
//    }
//}
