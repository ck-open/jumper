package com.ck.api;

import com.ck.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum TResultCode implements CodeEnum<Integer, TResultCode,String> {

    OK(1, "成功"),
    OPERATE_OK(1, "操作成功"),
    SAVE_OK(1, "保存成功"),
    UPDATE_OK(1, "更新成功"),
    QUERY_OK(1, "查询成功"),
    DEL_OK(1, "删除成功"),

    // 操作错误
    UNKNOWN_ERROR(0, "未知错误"),
    OPERATE_ERROR(1000, "操作失败"),
    SAVE_ERROR(1001, "保存失败"),
    UPDATE_ERROR(1002, "更新失败"),
    QUERY_ERROR(1003, "查询失败"),
    DEL_ERROR(1004, "删除失败"),

    // 接口错误
    PARAM_FORMAT_ERROR(1101, "参数格式错误"),
    API_ERROR(1102, "接口调用失败"),
    FILE_UPLOAD_ERROR(1103, "文件上传失败"),
    FILE_DOWN_ERROR(1104, "文件下载失败"),
    FILE_TYPE_ERROR(1105, "不能识别的文件类型"),

    // 请求验证错误
    IP_ERROR(1201, "IP验证失败"),
    LOGIN_ERROR(1202, "未登录"),
    AUTH_ERROR(1203, "无权限"),
    ;


    private Integer code;
    private String message;

    TResultCode(Integer code, String message) {
        this.code=code;
        this.message=message;
    }

    /**
     * 获取枚举代码
     *
     * @return
     */
    @Override
    public Integer getCode() {

        return this.code;
    }

    /**
     * 获取枚举名称
     *
     * @return
     */
    @Override
    public String getName() {
        return this.message;
    }

    /**
     * 获取枚举值
     *
     * @return
     */
    @Override
    public String getValue() {
        return this.message;
    }
}
