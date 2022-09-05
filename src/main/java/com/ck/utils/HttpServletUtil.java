package com.ck.utils;

import com.ck.http_client.NetUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HttpServlet 工具类
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class HttpServletUtil {
    private static final Logger log = Logger.getLogger(HttpServletUtil.class.getName());

    public static void forward(HttpServletRequest request, HttpServletResponse response, String forwardURL) {
        int timeOut = 1000 * 60 * 3;
        forward(request, response, forwardURL, timeOut, timeOut, null);
    }


    /**
     * 路由转发
     *
     * @param request     请求对象
     * @param response    响应对象
     * @param forwardURL  要路由的目的地
     * @param timeout     路由请求超时
     * @param readTimeout 结果响应超时
     */
    public static void forward(HttpServletRequest request, HttpServletResponse response, String forwardURL, int timeout, int readTimeout, Map<String, List<String>> parameterMap) {
        // 获取客户端请求参数
        String parameter = NetUtil.getParameter(request.getParameterMap(), parameterMap);
        // 创建请求对象
        HttpURLConnection conn = forwardGetHttpURLConnection(request, forwardURL + parameter, timeout, readTimeout);
        // 建立链接并转发报文内容
        forwardSendContent(request, conn);
        // 获取响应结果并转发到客户端
        forwardResponse(response, conn);
    }

    /**
     * 获取待转发请求对象
     *
     * @param request     请求对象
     * @param forwardURL  要路由的目的地
     * @param timeout     路由请求超时
     * @param readTimeout 结果响应超时
     * @return
     */
    public static HttpURLConnection forwardGetHttpURLConnection(HttpServletRequest request, String forwardURL, int timeout, int readTimeout) {
        // 请求转发
        HttpURLConnection conn = null;
        try {
            URL url = new URL(forwardURL);
            conn = (HttpURLConnection) url.openConnection();

            // 请求方式
            conn.setRequestMethod(request.getMethod());

            // 设置一个指定的超时值(以毫秒为单位)，用于打开到此URLConnection引用的资源的通信链接。
            conn.setConnectTimeout(timeout);
            // 将读取超时设置为指定超时(以毫秒为单位)。非零值指定连接到资源时从输入流读取时的超时。如果超时在可用数据读取之前过期。
            conn.setReadTimeout(readTimeout);

            // 设置输入、输出流是否开启
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 是否使用缓存数据
            conn.setUseCaches(false);

            // 设置自定义请求头
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String key = headers.nextElement();
                conn.setRequestProperty(key, request.getHeader(key));
            }

            log.info("路由待转发对象组建完成！" + request.getRequestURL() + "  To " + forwardURL);
        } catch (UnsupportedEncodingException e) {
            log.warning("网络连接获取失败！ URL参数编码失败:" + forwardURL + "   ==> " + e.getMessage());
        } catch (MalformedURLException e) {
            log.warning("网络连接获取失败！ URL格式错误:" + forwardURL + "   ==> " + e.getMessage());
        } catch (IOException e) {
            log.warning("网络连接获取失败！URL:" + forwardURL + "   I/O异常:" + e.getMessage());
        } catch (Exception e) {
            log.warning("网络连接获取失败！ URL:" + forwardURL + "   ==>" + e.getMessage());
        }
        return conn;
    }

    /**
     * 转发请求内容
     *
     * @param request 请求对象
     * @param conn    带转发对象
     * @return
     */
    public static HttpURLConnection forwardSendContent(HttpServletRequest request, HttpURLConnection conn) {
        String forwardURL = null;
        try {
            forwardURL = "http://" + conn.getURL().getHost() + ":" + conn.getURL().getPort() + conn.getURL().getPath();
            conn.connect();  // 发送请求
            OutputStream os = conn.getOutputStream();
            InputStream is = request.getInputStream();
            NetUtil.io(is, os);
            NetUtil.closeIO(is, os);

            log.info("路由请求内容转发完成！" + request.getRequestURL() + "  To " + forwardURL);
        } catch (UnsupportedEncodingException e) {
            log.warning("网络连接获取失败！ URL参数编码失败:" + forwardURL + "   ==> " + e.getMessage());
        } catch (MalformedURLException e) {
            log.warning("网络连接获取失败！ URL格式错误:" + forwardURL + "   ==> " + e.getMessage());
        } catch (IOException e) {
            log.warning("网络连接获取失败！URL:" + forwardURL + "   I/O异常:" + e.getMessage());
        } catch (Exception e) {
            log.warning("网络连接获取失败！ URL:" + forwardURL + "   ==>" + e.getMessage());
        }
        return conn;
    }

    /**
     * 响应客户端，转发返回值
     *
     * @param response 客户端响应对象
     * @param conn     带转发对象
     * @return
     */
    public static void forwardResponse(HttpServletResponse response, HttpURLConnection conn) {
        String forwardURL = null;
        try {
            forwardURL = "http://" + conn.getURL().getHost() + ":" + conn.getURL().getPort() + conn.getURL().getPath();
            // 响应结果转发
            response.setStatus(conn.getResponseCode());
            for (String key : conn.getHeaderFields().keySet()) {
                if (conn.getHeaderFields().get(key) != null) {
                    for (String val : conn.getHeaderFields().get(key)) {
                        response.setHeader(key, val);
                    }
                }
            }

            response.setContentType(conn.getContentType());
            response.setContentLength(conn.getContentLength());
            response.setCharacterEncoding(conn.getContentEncoding());

            OutputStream os = null;
            InputStream is = null;
            os = response.getOutputStream();
            if (200 == conn.getResponseCode()) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            NetUtil.io(is, os);
            NetUtil.closeIO(is, os);

            log.info("路由转发完成！" + forwardURL);
        } catch (UnsupportedEncodingException e) {
            log.warning("网络连接获取失败！ URL参数编码失败:" + forwardURL + "   ==> " + e.getMessage());
        } catch (MalformedURLException e) {
            log.warning("网络连接获取失败！ URL格式错误:" + forwardURL + "   ==> " + e.getMessage());
        } catch (IOException e) {
            log.warning("网络连接获取失败！URL:" + forwardURL + "   I/O异常:" + e.getMessage());
        } catch (Exception e) {
            log.warning("网络连接获取失败！ URL:" + forwardURL + "   ==>" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
    }

    /**
     * 通过HttpServletRequest返回IP地址
     *
     * @param request HttpservletRequest
     * @return ip string
     * @throws Exception
     */
    public static String getIpAddr(HttpServletRequest request) throws Exception {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-client-Ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP CLIENT IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP X FORWARDED FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 将Workbook 文件数组数据下载输出<br>
     * ByteArrayOutputStream.toByteArray();
     *
     * @param request
     * @param response
     * @param data
     * @return
     */
    public static void downFile(HttpServletRequest request, HttpServletResponse response, ByteArrayOutputStream data, String fileName) {
        try {
            byte[] bytes = data.toByteArray();
            setResponseHeaders(fileName, bytes.length, request, response);
            io(bytes, response.getOutputStream());
        } catch (Exception e) {
            log.log(Level.WARNING,"文件下载异常",e);
        }
    }


    /**
     * 将文件下载输出
     *
     * @param request
     * @param response
     * @param file
     * @return
     */
    public static boolean downFile(HttpServletRequest request, HttpServletResponse response, File file) {
        if (file.exists()) {
            setResponseHeaders(file.getName(), file.length(), request, response);
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;

            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                os = response.getOutputStream();

                return io(bis, os);
            } catch (IOException e) {
                log.log(Level.WARNING,"文件下载异常",e);
            } finally {
                try {
                    bis.close();
                    fis.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 设置响应头
     *
     * @param fileName 文件名
     * @param length   文件长度
     * @param request
     * @param response
     */
    public static void setResponseHeaders(String fileName, long length, HttpServletRequest request, HttpServletResponse response) {
//        response.reset();
        String excelName = restFileNameByBrowser(fileName, request);
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=" + excelName);

        setContentTypeByteFileType(fileName, response);

        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Length", "" + length);
    }

    /**
     * 根据不同浏览器设置文件名编码规则
     *
     * @param request
     * @param fileName
     * @return
     */
    public static String restFileNameByBrowser(String fileName, HttpServletRequest request) {
        try {
            fileName = fileName == null || "".equals(fileName.trim()) ? "download" : fileName;
            String userAgent = request.getHeader("User-Agent");

            if (userAgent.toLowerCase().indexOf("firefox") > 0) {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1"); // firefox浏览器
            } else if (userAgent.toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "UTF-8");// IE浏览器
            } else if (userAgent.toUpperCase().indexOf("CHROME") > 0) {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");// 谷歌
            } else {
                fileName = URLEncoder.encode(fileName, "UTF-8");// IE浏览器
            }
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("文档导出文件名称生成失败！" + e.getMessage());
        }
    }

    /**
     * 设置文件介质类型 ContentType
     *
     * @param fileName
     * @param response
     */
    public static void setContentTypeByteFileType(String fileName, HttpServletResponse response) {
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlt") || fileName.endsWith(".xla")) {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
        } else if (fileName.endsWith(".xlsx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        } else if (fileName.endsWith(".xltx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.template;charset=utf-8");
        } else if (fileName.endsWith(".xlsm")) {
            response.setContentType("application/vnd.ms-excel.sheet.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".xltm")) {
            response.setContentType("application/vnd.ms-excel.template.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".xlam")) {
            response.setContentType("application/vnd.ms-excel.addin.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".xlsb")) {
            response.setContentType("application/vnd.ms-excel.sheet.binary.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".dot")) {
            response.setContentType("application/msword;charset=utf-8");
        } else if (fileName.endsWith(".docx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document;charset=utf-8");
        } else if (fileName.endsWith(".dotx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.template;charset=utf-8");
        } else if (fileName.endsWith(".docm")) {
            response.setContentType("application/vnd.ms-word.document.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".dotm")) {
            response.setContentType("application/vnd.ms-word.template.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pot") || fileName.endsWith(".pps") || fileName.endsWith(".ppa")) {
            response.setContentType("application/vnd.ms-powerpoint;charset=utf-8");
        } else if (fileName.endsWith(".pptx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation;charset=utf-8");
        } else if (fileName.endsWith(".potx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.template;charset=utf-8");
        } else if (fileName.endsWith(".ppsx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.slideshow;charset=utf-8");
        } else if (fileName.endsWith(".ppam")) {
            response.setContentType("application/vnd.ms-powerpoint.addin.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".pptm")) {
            response.setContentType("application/vnd.ms-powerpoint.presentation.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".potm")) {
            response.setContentType("application/vnd.ms-powerpoint.presentation.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".zip")) {
            response.setContentType("application/zip;charset=utf-8");
        } else if (fileName.endsWith(".ppsm")) {
            response.setContentType("application/vnd.ms-powerpoint.slideshow.macroEnabled.12;charset=utf-8");
        } else if (fileName.endsWith(".tar")) {
            response.setContentType("application/x-tar;charset=utf-8");
        }
    }

    /**
     * 数据传输
     *
     * @param inputStream
     * @param outputStream
     */
    private static boolean io(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while (len != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
                len = inputStream.read(buffer);
            }
            return true;
        } catch (IOException e) {
            log.log(Level.WARNING, "文件数据传输失败！", e);
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 数据传输
     *
     * @param excelBytes
     * @param output
     */
    private static boolean io(byte[] excelBytes, OutputStream output) {
        try {
            if (excelBytes != null) {
                int len = 1024;
                for (int i = 0; i < excelBytes.length; i = i + len) {
                    if (i + len > excelBytes.length)
                        len = excelBytes.length - i;
                    output.write(excelBytes, i, len);
                    output.flush();
                }
            }
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "文件数据传输失败！", e);
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (Exception e2) {
            }
        }
        return false;
    }
}
