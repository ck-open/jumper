package com.ck.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.*;
import java.util.logging.Logger;

/**
 * 图片操作类
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class ImageUtil {
    private static final Logger log = Logger.getLogger(ImageUtil.class.getName());

    /**
     * 默认格式
     */
    private static final String Format_Name = "JPEG";

    /**
     * 获取Image数据对象
     *
     * @param imagePath
     * @return
     */
    public static BufferedImage readImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile != null && imageFile.exists())
            return readImage(imageFile);
        return null;
    }

    /**
     * 获取Image数据对象
     *
     * @param imageFile
     * @return
     */
    public static BufferedImage readImage(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            log.warning(String.format("getImage Reader Error:%s", e.getMessage()));
        }
        return null;
    }

    /**
     * 写出图片到文件
     *
     * @param image      图像对象
     * @param imageFile  目标文件
     * @param formatName 图片格式 如：jpg
     */
    public static void writeImage(BufferedImage image, File imageFile, String formatName) {
        try {
            ImageIO.write(image, formatName, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将图像数组读取成BufferedImage
     *
     * @param imageBytes 图像byte[]
     * @return BufferedImage
     */
    public static BufferedImage getBytesToBufferedImage(byte[] imageBytes) {
        try {
            // 将图像数组读取成BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bais);
        } catch (IOException e) {
            log.warning(String.format("getBytesToBufferedImage Error：%s", e.getMessage()));
        }
        return null;
    }

    /**
     * 将图像数组读取成ImageIcon
     *
     * @param imageBytes 图像byte[]
     * @return ImageIcon
     */
    public static ImageIcon getBytesToImageIcon(byte[] imageBytes) {
        return new ImageIcon(imageBytes);
    }

    /**
     * 将BufferedImage图像转换成ImageIcon
     *
     * @param image 图像BufferedImage
     * @return ImageIcon
     */
    public static ImageIcon getBufferedImageToImageIcon(BufferedImage image) {
        return new ImageIcon(image);
    }

    /**
     * 将ImageIcon图像转换成BufferedImage
     *
     * @param imageIocn 图像ImageIcon
     * @return BufferedImage
     */
    public static BufferedImage getImageIconToBufferedImage(ImageIcon imageIocn) {
        // 把imageIocn转为image格式、然后设置图片宽高为当前窗口宽高
        return (BufferedImage) imageIocn.getImage().getScaledInstance(imageIocn.getIconWidth(),
                imageIocn.getIconHeight(), Image.SCALE_SMOOTH);
    }

    /**
     * 将BufferedImage图像转换成JPEGImage二进制文件
     *
     * @param images 图像BufferedImage
     * @return
     */
    public static byte[] getBufferedImageToByte(BufferedImage images) {
        try {
            // 将BufferedImage压缩为二进制图片文件
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 将image按照指定的图片格式 转换成byte[] 并传输给baos
            ImageIO.write(images, Format_Name, baos);
            // 转换成byte数组
            return baos.toByteArray();
        } catch (IOException e) {
            log.warning(String.format("getBufferedImageToByte Error：%s", e.getMessage()));
        }
        return null;
    }

    /**
     * 按比例压缩图片
     *
     * @param image  要压缩的图片
     * @param Width  指定压缩后的图形宽度
     * @param Height 指定压缩后的图像高度
     * @return byte[] JPEGImage文件二进制数组
     */
    public static byte[] getImageBytes(BufferedImage image, int Width, int Height) {
        return getBufferedImageToByte(setImageRedraw(image, Width, Height));
    }

    /**
     * 按比例重绘图片像素
     *
     * @param image  要重绘的图片
     * @param Width  指定重绘后的图形宽度
     * @param Height 指定重绘后的图像高度
     * @return 重绘后的BufferedImage
     */
    public static BufferedImage setImageRedraw(BufferedImage image, int Width, int Height) {
        // 将BufferedImage向上造型Image
        Image src = image;

        // 构造一个类型为预定义图像类型之一的 BufferedImage
        BufferedImage tag = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);

        /*
         * 绘制图像 getScaledInstance表示创建此图像的缩放版本，返回一个新的缩放版本Image,按指定的width,height呈现图像。
         * Java提供了四种图像缩放算法： image.SCALE_SMOOTH //平滑优先 image.SCALE_FAST//速度优先
         * image.SCALE_AREA_AVERAGING //区域均值 image.SCALE_REPLICATE //像素复制型缩放
         * image.SCALE_DEFAULT //默认缩放模式
         */
        tag.getGraphics().drawImage(src.getScaledInstance(Width, Height, Image.SCALE_SMOOTH), 0, 0, null);
        return tag;
    }

    /**
     * 按照固定宽高原图压缩
     *
     * @param img    图片文件
     * @param width  目标 宽度
     * @param height 目标 高度
     * @param out    目标图片输出流
     */
    public static void thumbnail(File img, int width, int height, OutputStream out) {
        try {
            BufferedImage BI = ImageIO.read(img);
            Image image = BI.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            imageIoWriter(image, width, height, out);
        } catch (IOException e) {
            log.warning(String.format("thumbnail Error：%s", e.getMessage()));
        }
    }

    /**
     * 图像输出到流
     *
     * @param image
     * @param width
     * @param height
     * @param out
     */
    public static void imageIoWriter(Image image, int width, int height, OutputStream out) {
        try {
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.setColor(Color.RED);
            g.drawImage(image, 0, 0, null); // 绘制处理后的图
            g.dispose();
            ImageIO.write(tag, Format_Name, out);
        } catch (IOException e) {
            log.warning(String.format("imageIoWriter Error：%s", e.getMessage()));
        }
    }

    /**
     * 按照宽高裁剪
     *
     * @param srcImageFile 图片文件
     * @param destWidth    宽
     * @param destHeight   高
     * @param out          结果输出流
     */
    public static void cut_w_h(File srcImageFile, int destWidth, int destHeight, OutputStream out) {
        cut_w_h(srcImageFile, 0, 0, destWidth, destHeight, out);
    }

    /**
     * 按照宽高裁剪
     *
     * @param imageFile  图片文件
     * @param destWidth  宽
     * @param destHeight 高
     * @param out        结果输出流
     * @param x          截取起始横向坐标
     * @param y          截取纵向坐标
     */
    public static void cut_w_h(File imageFile, int x, int y, int destWidth, int destHeight, OutputStream out) {
        try {
            Image img;
            ImageFilter cropFilter;
            // 读取源图像
            BufferedImage bi = ImageIO.read(imageFile);
            int srcWidth = bi.getWidth(); // 源图宽度
            int srcHeight = bi.getHeight(); // 源图高度

            if (srcWidth >= destWidth && srcHeight >= destHeight) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);

                cropFilter = new CropImageFilter(x, y, destWidth, destHeight);
                img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));

                imageIoWriter(img, destWidth, destHeight, out);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 比对两个Image是否相同<br>
     * 根据图片的像素RGB颜色值比较，存在一定误差。
     *
     * @param oldImage 旧图
     * @param newImage 新图
     * @return
     */
    public static boolean contrastPixelRGB(BufferedImage oldImage, BufferedImage newImage) {
        if (oldImage == null || newImage == null)
            return false;
        // 截取色号编码前三位数字用
        String sb = null;
        // 记录不同色号连续出现次数
        int conut = 0;

        for (int i = 0; i < oldImage.getWidth(); i += 10) {
            for (int j = 0; j < oldImage.getHeight(); j += 3) {
                // 比较两张图同一位置色号,并取出前三个数字相比较，不够三个则全部取出
                int oldPixel = oldImage.getRGB(i, j);
                sb = String.valueOf(Math.abs(oldPixel));
                oldPixel = Integer.parseInt(sb.substring(0, sb.length() >= 3 ? 3 : sb.length()));
                int newPixel = newImage.getRGB(i, j);
                sb = String.valueOf(Math.abs(newPixel));
                newPixel = Integer.parseInt(sb.substring(0, sb.length() >= 3 ? 3 : sb.length()));

                // 比较差大于限定值，则认为该像素点不相同，不行同计数器+1，否则计数器-1直至为0。计数器大于限定值则认为两张图片不同
                int pixel = Math.abs(oldPixel - newPixel);
                if (pixel > 300)
                    conut++;
                else if (conut >= 0)
                    conut--;
                if (conut >= 5)
                    return false;
            }
        }
        return true;
    }


    /**
     * 颜色分量转换RGB值
     *
     * @param alpha
     * @param red
     * @param green
     * @param blue
     * @return
     */
    public static int colorToRGB(int alpha, int red, int green, int blue) {
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;
        return newPixel;
    }

    /**
     * 灰度化
     *
     * @param status
     * @param image
     * @return
     */
    public static BufferedImage grayImage(int status, BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, image.getType());
//        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = image.getRGB(i, j);
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                int gray = 0;
                if (status == 1) {
                    gray = r >= g && r >= b ? r : Math.max(g, b);  // 最大值法灰度化
                } else if (status == 2) {
                    gray = r <= g && r <= b ? r : Math.min(g, b); // 最小值法灰度化
                } else if (status == 3) {
                    gray = (r + g + b) / 3;  // 均值法灰度化
                } else if (status == 4) {
                    gray = (int) (0.3 * r + 0.59 * g + 0.11 * b); // 加权法灰度化
                }
                grayImage.setRGB(i, j, colorToRGB(0, gray, gray, gray));
            }
        }
        return grayImage;
    }


    /**
     * 图像二值化
     * @param image
     * @return
     */
    public static BufferedImage binaryImage(BufferedImage image) {
        BufferedImage binaryImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        double sw = 160;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int color = image.getRGB(i, j);
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                int avg = (r + g + b) / 3;
                if (avg <= sw) {
                    int max = new Color(0, 0, 0).getRGB();
                    binaryImage.setRGB(i, j, max);
                } else {
                    int min = new Color(255, 255, 255).getRGB();
                    binaryImage.setRGB(i, j, min);
                }
            }
        }
        return binaryImage;
    }

    /**
     * 合成图片并添加文字
     *
     * @param tempImage
     *            模版图片
     * @param mergedImage
     *            叠加图片
     * @param left
     *            叠加图片左边距
     * @param top
     *            叠加图片上边距
     * @param width
     *            叠加图片宽带
     * @param height
     *            叠加图片高度
     * @param str
     *            文字
     * @param font
     *            文字字体
     * @param fontLeft
     *            文字左边距
     * @param fonTop
     *            文字上边距
     * @param outputfile
     *            最终合成文件
     * @throws IOException
     */
    public static void merged(File tempImage, File mergedImage, int left,
                              int top, int width, int height, String str, Font font,
                              int fontLeft, int fonTop, File outputfile) throws IOException {
        // 加载模版图片
        BufferedImage imageLocal = ImageIO.read(tempImage);
        // 加载叠加图片
        BufferedImage imageCode = ImageIO.read(mergedImage);
        Graphics2D g = imageLocal.createGraphics();
        // 在模板上添加叠加图片(地址,左边距,上边距,图片宽度,图片高度,未知)
        g.drawImage(imageCode, left, top, width, height, null);
        // 添加文本说明
        if (str != null) {
            // 设置文本样式
            g.setFont(font);
            g.setColor(Color.RED);
            g.drawString(str, fontLeft, fonTop);
        }
        // 完成模板修改
        g.dispose();
        ImageIO.write(imageLocal, "png", outputfile);
    }

    public static void main(String[] args) {
        File file = new File("E:\\222.jpg");
//        String imagePath = "D:\\图片\\工厂1.jpg";
        String imagePath = "E:\\IMG_20210312_163701.jpg";
        BufferedImage image = readImage(imagePath);
        writeImage(binaryImage(grayImage(1, image)), file, "jpg");
//        writeImage(binaryImage(image), file, "jpg");
    }
}
