package com.ck.utils;

import java.util.Properties;

/**
 * java 环境
 *
 * @author cyk
 * @since 2021-06-06
 */
public final class RuntimeConfig {
    /**
     * Java的运行环境版本
     */
    private String javaVersion;
    /**
     * Java的运行环境供应商
     */
    private String javaVendor;
    /**
     * Java供应商的URL
     */
    private String javaVendorUrl;
    /**
     * Java的安装路径
     */
    private String javaHome;

    /**
     * Java的虚拟机规范版本
     */
    private String javaVmVSpecificationVersion;
    /**
     * Java的虚拟机规范供应商
     */
    private String javaVmVSpecificationVendor;
    /**
     * Java的虚拟机规范名称
     */
    private String javaVmVSpecificationName;
    /**
     * Java的虚拟机实现版本
     */
    private String javaVmVersion;
    /**
     * Java的虚拟机实现供应商
     */
    private String javaVmVendor;
    /**
     * Java的虚拟机实现名称
     */
    private String javaVmName;
    /**
     * Java运行时环境规范版本
     */
    private String javaSpecificationVersion;
    /**
     * Java运行时环境规范供应商
     */
    private String javaSpecificationVendor;
    /**
     * Java运行时环境规范名称
     */
    private String javaSpecificationName;
    /**
     * Java的类格式版本号
     */
    private String javaClassVersion;
    /**
     * Java的类路径
     */
    private String javaClassPath;
    /**
     * 加载库时搜索的路径列表
     */
    private String javaLibraryPath;
    /**
     * 默认的临时文件路径
     */
    private String javaIoTmpdir;
    /**
     * 一个或多个扩展目录的路径
     */
    private String javaExtDirs;
    /**
     * 操作系统的名称
     */
    private String osName;
    /**
     * 操作系统的构架
     */
    private String osArch;
    /**
     * 操作系统的版本
     */
    private String osVersion;
    /**
     * 用户的主目录
     */
    private String userHome;
    /**
     * 用户的当前工作目录
     */
    private String userDir;
    /**
     * 文件分隔符
     */
    private String fileSeparator;
    /**
     * 路径分隔符
     * 在 unix 系统中是＂／＂
     */
    private String pathSeparator;
    /**
     * 行分隔符
     * 在 unix 系统中是＂:＂
     */
    private String lineSeparator;
    /**
     * 用户的账户名称
     * 在 unix 系统中是＂/n＂
     */
    private String userName;


    RuntimeConfig() {
        Properties props = System.getProperties();

        this.javaVersion = props.getProperty("java.version");
        this.javaVendor = props.getProperty("java.vendor");
        this.javaVendorUrl = props.getProperty("java.vendor.url");
        this.javaHome = props.getProperty("java.home");
        this.javaVmVSpecificationVersion = props.getProperty("java.vm.specification.version");
        this.javaVmVSpecificationVendor = props.getProperty("java.vm.specification.vendor");
        this.javaVmVSpecificationName = props.getProperty("java.vm.specification.name");
        this.javaVmVersion = props.getProperty("java.vm.version");
        this.javaVmVendor = props.getProperty("java.vm.vendor");
        this.javaVmName = props.getProperty("java.vm.name");
        this.javaSpecificationVersion = props.getProperty("java.specification.version");
        this.javaSpecificationVendor = props.getProperty("java.specification.vender");
        this.javaSpecificationName = props.getProperty("java.specification.name");
        this.javaClassVersion = props.getProperty("java.class.version");
        this.javaClassPath = props.getProperty("java.class.path");
        this.javaLibraryPath = props.getProperty("java.library.path");
        this.javaIoTmpdir = props.getProperty("java.io.tmpdir");
        this.javaExtDirs = props.getProperty("java.ext.dirs");
        this.osName = props.getProperty("os.name");
        this.osArch = props.getProperty("os.arch");
        this.osVersion = props.getProperty("os.version");
        this.userHome = props.getProperty("user.home");
        this.userDir = props.getProperty("user.dir");
        this.fileSeparator = props.getProperty("file.separator");
        this.pathSeparator = props.getProperty("path.separator");
        this.lineSeparator = props.getProperty("line.separator");
        this.userName = props.getProperty("user.name");

        if (this.osName.toLowerCase().startsWith("unix")) {

        }
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public String getJavaVendorUrl() {
        return javaVendorUrl;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getJavaVmVSpecificationVersion() {
        return javaVmVSpecificationVersion;
    }

    public String getJavaVmVSpecificationVendor() {
        return javaVmVSpecificationVendor;
    }

    public String getJavaVmVSpecificationName() {
        return javaVmVSpecificationName;
    }

    public String getJavaVmVersion() {
        return javaVmVersion;
    }

    public String getJavaVmVendor() {
        return javaVmVendor;
    }

    public String getJavaVmName() {
        return javaVmName;
    }

    public String getJavaSpecificationVersion() {
        return javaSpecificationVersion;
    }

    public String getJavaSpecificationVendor() {
        return javaSpecificationVendor;
    }

    public String getJavaSpecificationName() {
        return javaSpecificationName;
    }

    public String getJavaClassVersion() {
        return javaClassVersion;
    }

    public String getJavaClassPath() {
        return javaClassPath;
    }

    public String getJavaLibraryPath() {
        return javaLibraryPath;
    }

    public String getJavaIoTmpdir() {
        return javaIoTmpdir;
    }

    public String getJavaExtDirs() {
        return javaExtDirs;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getUserHome() {
        return userHome;
    }

    public String getUserDir() {
        return userDir;
    }

    public String getFileSeparator() {
        return fileSeparator;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {

        StringBuffer str = new StringBuffer();
        str.append("Java的运行环境版本：" + this.javaVersion + lineSeparator);
        str.append("Java的运行环境供应商：" + this.javaVendor + lineSeparator);
        str.append("Java供应商的URL：" + javaVendorUrl + lineSeparator);
        str.append("Java的安装路径：" + javaHome + lineSeparator);
        str.append("Java的虚拟机规范版本：" + javaVmVSpecificationVersion + lineSeparator);
        str.append("Java的虚拟机规范供应商：" + javaVmVSpecificationVendor + lineSeparator);
        str.append("Java的虚拟机规范名称：" + javaVmVSpecificationName + lineSeparator);
        str.append("Java的虚拟机实现版本：" + javaVmVersion + lineSeparator);
        str.append("Java的虚拟机实现供应商：" + javaVmVendor + lineSeparator);
        str.append("Java的虚拟机实现名称：" + javaVmName + lineSeparator);
        str.append("Java运行时环境规范版本：" + javaSpecificationVersion + lineSeparator);
        str.append("Java运行时环境规范供应商：" + javaSpecificationVendor + lineSeparator);
        str.append("Java运行时环境规范名称：" + javaSpecificationName + lineSeparator);
        str.append("Java的类格式版本号：" + javaClassVersion + lineSeparator);
        str.append("Java的类路径：" + javaClassPath + lineSeparator);
        str.append("加载库时搜索的路径列表：" + javaLibraryPath + lineSeparator);
        str.append("默认的临时文件路径：" + javaIoTmpdir + lineSeparator);
        str.append("一个或多个扩展目录的路径：" + javaExtDirs + lineSeparator);
        str.append("操作系统的名称：" + osName + lineSeparator);
        str.append("操作系统的构架：" + osArch + lineSeparator);
        str.append("操作系统的版本：" + osVersion + lineSeparator);
        str.append("用户的主目录：" + userHome + lineSeparator);
        str.append("用户的当前工作目录：" + userDir + lineSeparator);
        str.append("文件分隔符：" + fileSeparator + lineSeparator);
        str.append("路径分隔符：" + pathSeparator + lineSeparator);
        str.append("用户的账户名称：" + userName + lineSeparator);
//        str.append("行分隔符：" + lineSeparator);
        return str.toString();
    }
}