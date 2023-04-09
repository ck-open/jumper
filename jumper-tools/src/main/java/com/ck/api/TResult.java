package com.ck.api;

import com.alibaba.fastjson.JSONObject;
import com.ck.utils.TimeUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

/**
 * 返回结果信息
 *
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class TResult<T> implements Serializable {
    private static Logger log = Logger.getLogger(TResult.class.getName());

    private Integer status = 1;

    private String message;

    private String time = TimeUtil.parseDateToString_s(new Date());

    private T data;

    public TResult() {
    }

    @Override
    public String toString() {
        return "TResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                ", data=" + data +
                '}';
    }


    public static <T> TResult<T> build(TResultCode code, String message) {
        return build(code.getCode(), message, null);
    }

    public static <T> TResult<T> build(TResultCode code, String message, T data) {
        return build(code.getCode(), message, data);
    }

    public static <T> TResult<T> build(TResultCode code) {
        return build(code.getCode(), code.getMessage(), null);
    }

    public static <T> TResult<T> build(Integer status, String msg) {
        return build(status, msg, null);
    }

    public static <T> TResult<T> build(Integer status, String msg, T data) {
        return new TResult<T>().setStatus(status).setMessage(msg).setData(data);
    }

    public static <T> TResult<T> ok(T data) {
        return new TResult<T>().setStatus(TResultCode.OK.getCode()).setMessage("成功").setData(data);
    }

    /**
     * 将异常信息按照统一报文格式写出
     *
     * @param response
     * @param ex
     */
    public static void writerException(HttpServletResponse response, Throwable ex) {
        TResult resultBody = TResult.resolveException(ex);
        response.setStatus(resultBody.getStatus());
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(JSONObject.toJSONString(resultBody));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.warning("响应异常信息失败  error:" + e.getMessage());
            throw new RuntimeException(e);

        }
    }

    /**
     * 静态解析异常。可以直接调用
     *
     * @param ex
     * @return
     */
    public static TResult resolveException(Throwable ex) {
        String message = ex.getMessage();
        String className = ex.getClass().getSimpleName();

        switch (className) {

            // 账号错误
            case "UsernameNotFoundException":
                return build(3005, "username_not_found");
            case "BadCredentialsException":
                return build(3000, "bad_credentials");
            case "AccountExpiredException":
                return build(3002, "account_expired");
            case "LockedException":
                return build(3004, "account_locked");
            case "DisabledException":
                return build(3001, "account_disabled");
            case "CredentialsExpiredException":
                return build(3003, "credentials_expired");

            // oauth2返回码
            case "InvalidClientException":
                return build(2003, "invalid_client");
            case "UnauthorizedClientException":
                return build(2006, "unauthorized_client");
            case "InsufficientAuthenticationException":
            case "AuthenticationCredentialsNotFoundException":
                return build(2012, "unauthorized");

            case "InvalidGrantException": // 账号错误
                if ("Bad credentials".contains(message)) {
                    return build(3000, "bad_credentials");
                } else if ("User is disabled".contains(message)) {
                    return build(3001, "account_disabled");
                } else if ("User account is locked".contains(message)) {
                    return build(3004, "account_locked");
                } else {
                    return build(3004, "InvalidGrantException");
                }

            case "InvalidScopeException":
                return build(2001, "invalid_scope");
            case "InvalidTokenException":
                return build(2000, "invalid_token");
            case "InvalidRequestException":
                return build(2002, "invalid_request");
            case "RedirectMismatchException":
                return build(2005, "redirect_uri_mismatch");
            case "UnsupportedGrantTypeException":
                return build(2008, "unsupported_grant_type");
            case "UnsupportedResponseTypeException":
                return build(2009, "unsupported_response_type");
            case "UserDeniedAuthorizationException":
                return build(4030, "access_denied");
            case "AccessDeniedException":
                if (Arrays.asList("access_denied_black_limited", "access_denied_white_limited", "access_denied_authority_expired", "access_denied_updating", "access_denied_disabled", "access_denied_not_open")
                        .contains(message)) {
                    return build(4031, message);
                } else {
                    return build(4030, "access_denied");
                }

                // 请求错误
            case "HttpMessageNotReadableException":
            case "TypeMismatchException":
            case "MissingServletRequestParameterException":
                return build(4000, "bad_request(参数结构错误无法解析)");
            case "NoHandlerFoundException":
            case "ResponseStatusException":
                return build(4004, "not_found");
            case "HttpRequestMethodNotSupportedException":
                return build(4005, "method_not_allowed");
            case "HttpMediaTypeNotAcceptableException":
                return build(4006, "media_type_not_acceptable");
            case "MethodArgumentNotValidException":
                return build(1001, "方法参数无效");
            case "IllegalArgumentException":
                return build(1001, "参数错误");
            case "OpenSignatureException":
                if ("too_many_requests".equalsIgnoreCase(message)) {
                    return build(4029, "too_many_requests");
                } else {
                    return build(2013, "signature_denied");
                }
            case "OpenAlertException":
            default:
                return build(1001, "alert");
        }
    }
}
