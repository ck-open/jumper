package com.ck.utils;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * 设备操作工具类
 *
 * @author cyk
 * @since 2021-06-06
 */
public final class SystemUtil {
    private static Logger log = Logger.getLogger(SystemUtil.class.getName());
    /**
     * 运行环境
     */
    private static RuntimeConfig runtimeConfig;

    /**
     * 本机桌面关联应用程序类
     */
    private static Desktop desktop;


    public static void main(String[] args) {
//        System.out.println(calc());
//        mail();

        setStrClipboard("测试剪切板");
        setStrClipboard("测试剪切板4");
        System.out.println(getStrClipboard());

//        setImageClipboard(ImageUtil.getImage("C:\\Users\\cyk\\Desktop\\车险理赔\\车险事故\\机动车证件\\IMG_20210310_204100.jpg"));
    }

    /**
     * 获取Java运行环境配置信息
     *
     * @return
     */
    public static RuntimeConfig getRuntimeConfig() {
        if (runtimeConfig == null) {
            runtimeConfig = new RuntimeConfig();
        }
        return runtimeConfig;
    }


    /**
     * 启动计算器
     *
     * @return
     */
    public static boolean calc() {
        return exec("calc.exe");
    }

    /**
     * 执行系统程序
     *
     * @param command
     * @return
     */
    public static boolean exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            if (process.waitFor() == 0) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            log.warning(String.format("exec Error:%s", e.getMessage()));
        }
        return false;
    }

    /**
     * 判断当前平台是否支持Desktop类
     *
     * @return
     */
    public static boolean isDesktopSupported() {
        return Desktop.isDesktopSupported();
    }

    /**
     * 判断当前平台是否支持Open操作
     *
     * @return
     */
    public static boolean isOpen() {
        return getDesktop().isSupported(Desktop.Action.OPEN);
    }

    /**
     * 判断当前平台是否支持Edit操作
     *
     * @return
     */
    public static boolean isEdit() {
        return getDesktop().isSupported(Desktop.Action.EDIT);
    }

    /**
     * 判断当前平台是否支持Print操作
     *
     * @return
     */
    public static boolean isPrint() {
        return getDesktop().isSupported(Desktop.Action.PRINT);
    }

    /**
     * 判断当前平台是否支持Mail操作
     *
     * @return
     */
    public static boolean isMail() {
        return getDesktop().isSupported(Desktop.Action.MAIL);
    }

    /**
     * 判断当前平台是否支持Browse操作
     *
     * @return
     */
    public static boolean isBrowse() {
        return getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    /**
     * 获取与当前系统平台关联的Desktop对象
     *
     * @return
     */
    public static Desktop getDesktop() {
        if (desktop == null) {
            desktop = Desktop.getDesktop();
        }
        return desktop;
    }

    /**
     * 启动系统默认浏览器来访问指定URL
     *
     * @param url
     */
    public static void browse(String url) {
        try {
            getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            log.warning(String.format("Browse boot Error:%s", e.getMessage()));
        }
    }

    /**
     * 启动关联应用程序来打开文件
     *
     * @param file
     */
    public static void open(File file) {
        try {
            getDesktop().open(file);
        } catch (IOException e) {
            log.warning(String.format("Open File Error:%s", e.getMessage()));
        }
    }

    /**
     * 启动关联编辑器应用程序并打开用于编辑的文件
     *
     * @param file
     */
    public static void edit(File file) {
        try {
            getDesktop().edit(file);
        } catch (IOException e) {
            log.warning(String.format("Edit File Error:%s", e.getMessage()));
        }
    }

    /**
     * 使用关联应用程序的打印命令，用本机桌面打印设备来打印文件
     *
     * @param file
     */
    public static void print(File file) {
        try {
            getDesktop().print(file);
        } catch (IOException e) {
            log.warning(String.format("Print File Error:%s", e.getMessage()));
        }
    }

    /**
     * 启动默认邮件客户端
     */
    public static void mail() {
        try {
            getDesktop().mail();
        } catch (IOException e) {
            log.warning(String.format("Open Mail.java Error:%s", e.getMessage()));
        }
    }

    /**
     * 启动默认邮件客户端，填充由mailtoURI指定的消息字段
     *
     * @param mailtoURI
     */
    public static void mail(String mailtoURI) {
        try {
            getDesktop().mail(URI.create(mailtoURI));
        } catch (IOException e) {
            log.warning(String.format("Open MailToURI Error:%s", e.getMessage()));
        }
    }

    /**
     * 获取系统剪切板
     *
     * @return
     */
    public static Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * 设置文本到系统剪切板
     *
     * @param str
     */
    public static void setStrClipboard(String str) {
        // 构建String数据类型
        StringSelection selection = new StringSelection(str);
        // 添加文本到系统剪切板
        getClipboard().setContents(selection, null);
    }

    /**
     * 从剪切板中获取文本数据
     *
     * @return
     */
    public static String getStrClipboard() {
        try {
            // 从系统剪切板中获取数据
            Transferable content = getClipboard().getContents(null);
            // 判断是否为文本类型
            if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                // 从数据中获取文本值
                return (String) content.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            log.warning(String.format("从剪切板获取文本异常：%s", e.getMessage()));
        }
        return null;
    }

    /**
     * 设置图片到系统剪切板
     *
     * @param image
     */
    public static void setImageClipboard(Image image) {
        if (image != null) {
            getClipboard().setContents(new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{DataFlavor.imageFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.imageFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    return image;
                }
            }, null);
        }
    }

    /**
     * 从剪切板中获取图片数据
     *
     * @return
     */
    public static Image getImageClipboard() {
        Transferable transferable = getClipboard().getContents(null);
        try {
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            log.warning(String.format("从剪切板获取图片异常：%s", e.getMessage()));
        }
        return null;
    }


    /**
     * 获取屏幕截屏
     *
     * @return
     */
    public static BufferedImage screenShot() {
        // 截取整个屏幕
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        // 通过截取的屏幕构造矩形对象
        Rectangle rec = new Rectangle(dimension);

        // 缓冲图像  创建包含从屏幕读取的像素的图像。此图像不包括鼠标光标。
        return RobotUtil.getRobot().createScreenCapture(rec);
    }
}
