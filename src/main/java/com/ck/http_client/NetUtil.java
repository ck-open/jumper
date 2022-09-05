package com.ck.http_client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 网络连接控制类
 *
 * @author cyk
 * @since 2020-01-01
 */
public class NetUtil {

    private static Logger log = Logger.getLogger(NetUtil.class.getName());

    /**
     * 创建网络连接并发送请求
     * GET、POST、HEAD、OPTIONS、PUT、DELETE、TRACE
     *
     * @param urlPort       请求地址http://localhost:port/user/...
     * @param requestMethod 请求方式：GET/POST 参数为null则默认GET
     * @param parameter     URL 地址栏传参列表
     * @param heads         自定义请求头
     * @return HttpURLConnection连接对象
     */
    public static HttpURLConnection getConnection(String urlPort, String requestMethod, Map<String, List<String>> parameter, Map<String, String> heads, int timeout, int readTimeout) {
        if (!urlPort.startsWith("http://") && !urlPort.startsWith("https://"))
            urlPort = "http://" + urlPort;

        // 根据请求地址创建Connection连接
        HttpURLConnection conn = null;
        try {
            // 像url 追加参数
            urlPort += getParameter(null, parameter);

            // 创建URL obiect
            URL url;
            url = new URL(urlPort);
            conn = (HttpURLConnection) url.openConnection();
            // 请求方式
            if (requestMethod == null || "".equals(requestMethod.trim())) {
                log.warning("网络连接获取失败！ URL参数编码失败:" + urlPort + "   ==> 请求方式错误：" + requestMethod);
            }
            conn.setRequestMethod(requestMethod);

            // 设置一个指定的超时值(以毫秒为单位)，用于打开到此URLConnection引用的资源的通信链接。
            conn.setConnectTimeout(timeout);
            // 将读取超时设置为指定超时(以毫秒为单位)。非零值指定连接到资源时从输入流读取时的超时。如果超时在可用数据读取之前过期。
            conn.setReadTimeout(readTimeout);

            // 设置输入、输出流是否开启
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 是否使用缓存数据
            conn.setUseCaches(false);

            // 设置连接和字符集
            conn.setRequestProperty("Connection", "Keep-Alive");

            // 设置自定义请求头
            if (heads != null && !heads.isEmpty()) {
                for (String key : heads.keySet()) {
                    conn.setRequestProperty(key, heads.get(key));
                }
            }

            // 返回此连接
            return conn;
        } catch (UnsupportedEncodingException e) {
            log.warning("网络连接获取失败！ URL参数编码失败:" + urlPort + "   ==> " + e.getMessage());
        } catch (MalformedURLException e) {
            log.warning("网络连接获取失败！ URL格式错误:" + urlPort + "   ==> " + e.getMessage());
        } catch (IOException e) {
            log.warning("网络连接获取失败！URL:" + urlPort + "   I/O异常:" + e.getMessage());
        } catch (Exception e) {
            log.warning("网络连接获取失败！ URL:" + urlPort + "   ==>" + e.getMessage());
        } finally {
            close(conn);
        }
        return null;
    }


    public static HttpURLConnection getConnection(String urlPort, String requestMethod, Map<String, List<String>> parameter, Map<String, String> heads) {
        return getConnection(urlPort, requestMethod, parameter, heads, 60000, 60000);
    }

    public static HttpURLConnection getConnectionPost(String urlPort, Map<String, List<String>> parameter, Map<String, String> heads) {
        return getConnection(urlPort, "POST", parameter, heads, 60000, 60000);
    }

    public static HttpURLConnection getConnectionGet(String urlPort, Map<String, List<String>> parameter, Map<String, String> heads) {
        return getConnection(urlPort, "GET", parameter, heads, 60000, 60000);
    }


    public static boolean sendContent(HttpURLConnection conn, String data) {
        return sendContent(conn, data, "UTF-8");
    }

    /**
     * 向接口发送Body参数
     *
     * @param conn HttpURLConnection连接对象
     * @param data 要发送的数据字符串
     */
    public static boolean sendContent(HttpURLConnection conn, String data, String charset) {
        try {
            // 指定正文内容长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
            conn.setRequestProperty("Charset", charset);

            // 传输请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write(data.getBytes(charset));
            // 刷新缓存输出流
            out.flush();
            // 关闭释放此输出流
            out.close();
            // 断开请求
            conn.disconnect();
            return true;
        } catch (IOException e) {
            log.warning("网络数据发送失败！URL:" + conn.getURL() + "   I/O异常:" + e.getMessage());
        }
        return false;
    }

    /**
     * 解读服务端响应的数据<br>
     * 解码字符集：UTF-8
     *
     * @param conn HttpURLConnection连接对象
     * @return NetResult
     */
    public static NetResult getNetResult(HttpURLConnection conn) {
        NetResult netResult = new NetResult();

        try {
            // 响应结果转发
            netResult.setResponseCode(conn.getResponseCode());
            netResult.setContentType(conn.getContentType());
            netResult.setContentLength(conn.getContentLength());
            netResult.setContentEncoding(conn.getContentEncoding());
            netResult.setResponseMessage(conn.getResponseMessage());
            netResult.setHeads(conn.getHeaderFields());

            // 读取数据  将内容读取内存中
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream is = null;
            if (200 == conn.getResponseCode()) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            if (is != null) {
                io(is, os);
                closeIO(is, os);
            }

            if (netResult.getContentEncoding() == null || "".equals(netResult.getContentEncoding().trim())) {
                netResult.setContentEncoding("UTF-8");
            }
            netResult.setContent(os.toString(netResult.getContentEncoding()));

        } catch (IOException e) {
            log.warning("响应数据解读失败！URL:" + conn.getURL() + "   I/O异常:" + e.getMessage());
        }
        return netResult;
    }

    /**
     * 获取请求参数列表
     *
     * @param parameters 请求对象
     * @return
     */
    public static String getParameter(Map<String, String[]> parameters, Map<String, List<String>> parameterMap) {
        String parameterStr = "";

        StringBuffer parameter = new StringBuffer();
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> {
                if (parameterMap == null || !parameterMap.containsKey(k)) {
                    if (v != null && v.length > 0) {
                        for (String item : v) {
                            parameter.append("&").append(urlEncoder(k)).append("=").append(urlEncoder(item));
                        }
                    }
                }
            });
        }
        if (parameterMap != null && !parameterMap.isEmpty()) {
            parameterMap.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    for (String item : v) {
                        parameter.append("&").append(urlEncoder(k)).append("=").append(urlEncoder(item));
                    }
                }
            });
        }

        if (parameter.length() > 0) {
//                parameterStr += "?" + URLEncoder.encode(parameter.toString().substring(1), StandardCharsets.ISO_8859_1.toString());
            parameterStr += "?" + parameter.toString().substring(1);
        }

        return parameterStr;
    }

    public static String urlEncoder(String url) {
        return urlEncoder(url, StandardCharsets.UTF_8.toString());
    }

    /**
     * 地址转码
     * @param url  地址
     * @param charset  字符集
     * @return
     */
    public static String urlEncoder(String url, String charset) {
        try {
            return URLEncoder.encode(url, charset);
        } catch (UnsupportedEncodingException e) {
            log.info("URLEncoder 转码异常");
        }
        return url;
    }


    public static void io(InputStream is, OutputStream os) throws IOException {
        io(is, os, 10);
    }

    /**
     * 输入输出流对接
     *
     * @param is    输入流
     * @param os    输出流
     * @param speed 传输速率M
     * @throws IOException
     */
    public static void io(InputStream is, OutputStream os, double speed) throws IOException {
        if (speed <= 0) {
            speed = 1;
        }
        int len = -1;
        byte[] by = new byte[(int) (1024 * speed)];
        while ((len = is.read(by)) != -1) {
            os.write(by, 0, len);
        }
        os.flush();
    }

    /**
     * 关闭数据流
     *
     * @param is
     * @param os
     */
    public static void closeIO(InputStream is, OutputStream os) {
        try {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.flush();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     *
     * @param conn
     */
    public static void close(HttpURLConnection conn) {
        if (conn == null) {
            return;
        }
        // 断开连接
        conn.disconnect();
        conn = null;
    }

    /**
     * 通过IP地址获取MAC地址
     *
     * @param ip string, 127.0.0.1格式
     * @return mac string
     * @throws Exception
     */
    public static String getMAC(String ip) {
        String line = "";
        String macAddress = "";
        final String MAC_ADDRESS_PREFIX = "MAC Address =";
        final String LOOPBACK_ADDRESS = "127.0.0.1";

        try {
            // 如果为127.0.0.1,则获取本机MAC地址。
            if (LOOPBACK_ADDRESS.equals(ip)) {
                InetAddress inetAddress = InetAddress.getLocalHost();
                //貌似此方法器 DK1.6
                byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
                //下面代码是把 mac 地址拼接成String
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    if (i != 0) {
                        sb.append("-");
                    }
                    //mac[i] & OxFF 是为了把byte转化为正整数
                    String s = Integer.toHexString(mac[i] & 0xFF);
                    sb.append(s.length() == 1 ? 0 + s : s);
                }
                //把字符串所有小写字母改为大写成为正规的mac地址并返回
                macAddress = sb.toString().trim().toUpperCase();
                return macAddress;
            }

            //获职非本 IP的MAC地血

            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line != null) {
                    int index = line.indexOf(MAC_ADDRESS_PREFIX);
                    if (index != -1) {
                        macAddress = line.substring(index + MAC_ADDRESS_PREFIX.length()).trim().toUpperCase();
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return macAddress;
    }


    /**
     * 获取本机IP
     *
     * @return
     */
    public static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
        }
        return "-";
    }


    /**
     * 获取IP链路
     *
     * @return
     */
    public static List<InetAddress> getLocalIPLike() {
        List<InetAddress> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address) { // IPV4
                        ipList.add(inetAddress);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }

    /**
     * 获取本地外网IP
     *
     * @return
     */
    public static String getOuterIp() {
        String localip = null;// 本地IP
        String netip = null; // 外网IP
        for (InetAddress ip : getLocalIPLike()) {
            if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {// 外网IP
                netip = ip.getHostAddress();
            } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                    && !ip.getHostAddress().contains(":")) {// 内网IP
                localip = ip.getHostAddress();
            }
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    public static void main(String[] args) {
        System.out.println(getOuterIp());
    }


    /**
     * 访问结果对象
     * RESTFul风格
     *
     * @author cyk
     * @since 2020-01-01
     */
    public static class NetResult {
        /**
         * 响应状态码
         */
        private Integer responseCode;
        /**
         * 失败信息
         */
        private String responseMessage;
        /**
         * 响应头
         */
        private Map<String, List<String>> heads;
        /**
         * 响应体类型
         */
        private String contentType;
        /**
         * 响应体大小
         */
        private int contentLength;
        /**
         * 响应体字符集
         */
        private String contentEncoding;
        /**
         * 响应体正文
         */
        private String Content;

        public NetResult() {

        }

        public NetResult(String Content, Integer responseCode, Map<String, List<String>> heads) {
            this.responseCode = responseCode;
            this.Content = Content;
            this.heads = heads;
        }

        public Integer getResponseCode() {
            return responseCode;
        }

        public NetResult setResponseCode(Integer responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        public String getContent() {
            return this.Content;
        }

        public NetResult setContent(String content) {
            this.Content = content;
            return this;
        }

        public Map<String, List<String>> getHeads() {
            return heads;
        }

        public NetResult setHeads(Map<String, List<String>> heads) {
            this.heads = heads;
            return this;
        }

        public String getResponseMessage() {
            return responseMessage;
        }

        public NetResult setResponseMessage(String responseMessage) {
            this.responseMessage = responseMessage;
            return this;
        }

        public String getContentType() {
            return contentType;
        }

        public NetResult setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public int getContentLength() {
            return contentLength;
        }

        public NetResult setContentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public String getContentEncoding() {
            return contentEncoding;
        }

        public NetResult setContentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
        }
    }
}
