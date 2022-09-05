package com.ck.http_client.netenum;

/**
 * 网络连接请求内容类型
 *
 * @author cyk
 * @since 2020-01-01
 */
public enum ContentTypeEnum {
    Text_Html("text/html", "HTML格式"),
    Text_Plain("text/;plain", "纯文本格式"),
    Text_Xml("text/xml", "XML格式"),
    Text_Gif("image/gif", "gif图片格式"),
    Text_Jpeg("image/jpeg", "jpg图片格式"),
    Text_Png("image/png", "png图片格式"),

    Application_Xhtml_Xml("application/xhtml+xml", "XHTML格式"),
    Application_Xml("application/xml", "XML数据格式"),
    Application_Atom_Xml("application/atom+xml", " Atom XML聚合格式"),
    Application_Json("application/json", "JSON数据格式"),
    Application_Pdf("application/pdf", "pdf格式"),
    Application_MSword("application/msword", "Word文档格式"),
    Application_Octet_Stream("application/octet-stream", "二进制流数据（如常见的文件下载）"),
    //  <form encType=””>中默认的encType，form表单数据被编码为key/value格式发送到服务器（表单默认的提交数据的格式）
    Application_X_WWW_Form_Urlencoded("application/x-www-form-urlencoded", "表单默认的提交数据的格式"),
    // 媒体格式是上传文件之时使用的需要在表单中进行文件上传时，就需要使用该格式
    Multipart_Form_Data("multipart/form-data", "媒体格式");

    /**
     * 值
     */
    private String type;
    /**
     * 描述
     */
    private String describe;

    ContentTypeEnum(String v, String d) {
        this.type = v;
        this.describe = d;
    }

    public String getDescribe() {
        return this.describe;
    }

    public String getType() {
        return this.type;
    }


}
