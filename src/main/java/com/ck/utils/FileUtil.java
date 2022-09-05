package com.ck.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 文件操作类
 *
 * @author cyk
 * @since 2020-01-01
 */
public final class FileUtil {
    private static final Logger log = Logger.getLogger(FileUtil.class.getName());

    public static void main(String[] args) {
        String path = "E:\\test\\2699.zip";
        System.out.println(zipUpFile("E:\\test\\2699"));
        System.out.println(zipUnpack(path));;
    }

    /**
     * 新建目录
     *
     * @param folderPath String 如 c:/fqf
     * @return boolean
     */
    public static boolean newFolder(String folderPath) {
        try {
            File myFilePath = new File(folderPath);
            if (!myFilePath.exists()) {
                return myFilePath.mkdir();
            }
        } catch (Exception e) {
            log.warning(String.format("新建目录操作出错:%s  %s", folderPath, e.getMessage()));
        }
        return false;
    }


    /**
     * 新建文件
     *
     * @param filePathAndName String 文件路径及名称 如c:/fqf.txt
     * @param fileContent     String 文件内容
     * @return boolean
     */
    public static boolean newFile(String filePathAndName, String fileContent) {
        try {
            File myFilePath = new File(filePathAndName);
            if (!myFilePath.getParentFile().exists()) {
                myFilePath.getParentFile().mkdirs();
            }
            if (!myFilePath.exists()) {
                myFilePath.createNewFile();
            }
            FileWriter resultFile = new FileWriter(myFilePath);
            PrintWriter myFile = new PrintWriter(resultFile);
            myFile.println(fileContent);
            resultFile.close();
            return true;
        } catch (IOException e) {
            log.warning(String.format("新建文件操作出错:%s  %s", filePathAndName, e.getMessage()));
        }
        return false;
    }

    /**
     * 删除文件
     *
     * @param filePathAndName String 文件路径及名称 如c:/fqf.txt
     * @return boolean
     */
    public static boolean delFile(String filePathAndName) {
        try {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            File myDelFile = new File(filePath);
            return myDelFile.delete();

        } catch (Exception e) {
            log.warning(String.format("删除文件操作出错:%s  %s", filePathAndName, e.getMessage()));
        }
        return false;
    }

    /**
     * 删除文件夹
     *
     * @return boolean
     */
    public static boolean delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            File myFilePath = new File(folderPath);
            return myFilePath.delete(); // 删除空文件夹

        } catch (Exception e) {
            log.warning(String.format("删除文件夹操作出错:%s  %s", folderPath, e.getMessage()));
        }
        return false;
    }

    /**
     * 删除文件夹里面的所有文件
     *
     * @param path String 文件夹路径 如 c:/fqf
     */
    public static boolean delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + File.separator + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + File.separator + tempList[i]);// 再删除空文件夹
            }
        }
        return true;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
            return true;
        } catch (Exception e) {
            log.warning(String.format("复制单个文件操作出错:%s 到 %s  %s", oldPath, newPath, e.getMessage()));
        }
        return false;
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static boolean copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + File.separator + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(oldPath + File.separator + file[i], newPath + File.separator + file[i]);
                }
            }
            return true;
        } catch (Exception e) {
            log.warning(String.format("复制整个文件夹内容操作出错:%s 到 %s  %s", oldPath, newPath, e.getMessage()));
        }
        return false;
    }

    /**
     * 将文件保存到本地    保存位置在项目路径+自定义路径
     *
     * @param data        接收的文件数据 MultipartFile.getBytes()
     * @param contentType 文件类型 MultipartFile.getContentType()
     * @param fileName    文件名 MultipartFile.getOriginalFilename()
     * @param filePath    自定义保存位置
     * @return "uuid+文件名";  返回null 则表示操作失败
     */
    public static String saveMultipartFile(byte[] data, String contentType, String fileName, String filePath) {
        //保存的文件名  uuid-维修id
        String name = UUID.randomUUID().toString().replace("-", "") + fileName;
        if (!saveFile(data, filePath, name)) {
            log.info("fileName-->" + fileName + "  getContentType-->" + contentType + "  filePath-->" + filePath);
            return null;
        }
        return name;
    }


    /**
     * 将文件保存到本地
     *
     * @param file     要保存的文件
     * @param filePath 保存的地址(不存在则创建)
     * @param fileName 文件名
     * @throws Exception
     */
    public static boolean saveFile(byte[] file, String filePath, String fileName) {
        try {
            File targetFile = new File(filePath);
            // 判断文件是否存在，不存在则创建
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(filePath + fileName);
            out.write(file);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            log.warning(String.format("文件保存到本地操作出错:%s   %s   %s", filePath, fileName, e.getMessage()));
        }
        return false;
    }

    /**
     * 将文件拷贝到指定路径下
     *
     * @param file 要保存的文件
     * @param path 保存路径及文件名
     */
    public static boolean copy(File file, String path) {
        try {
            @SuppressWarnings("resource")
            InputStream is = new FileInputStream(file);
            @SuppressWarnings("resource")
            OutputStream os = new FileOutputStream(path);

            long fileSize = file.length();
            long index = 0;

            byte[] data = new byte[1024 * 1024 * 50];
            int len = -1;
            while ((len = is.read(data)) != -1) {
                os.write(data, 0, len);
                fileSize -= len;
                index += len / 1024 / 1024;
                log.info("==> 已复制：" + index + " MB    剩余：" + (fileSize / 1024 / 1024) + " MB    进度：" + (fileSize / (file.length()) * 100) + "% ");
            }
            return true;
        } catch (IOException e) {
            log.warning(String.format("文件拷贝操作出错:%s   %s   %s", file, path, e.getMessage()));
        }
        return false;
    }


    /**
     * 复制文件
     *
     * @param file       要拷贝的文件
     * @param targetPath 将拷贝的文件粘贴到的路径及文件名
     */
    public static boolean copyFile(File file, File targetPath) {
        if (!file.exists()) {
            log.info("复制的文件不存在！");
            return false;
        }
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        log.info("读取到文件准备复制...");
        RandomAccessFile raf = null;
        RandomAccessFile target = null;
        try {
            // 获取源文件
            raf = new RandomAccessFile(file, "r");
            // 获取目标文件
            target = new RandomAccessFile(targetPath, "rw");
            // 创建读取器
            byte[] data = new byte[1024 * 10];
            // 记录读取的长度
            int len = -1;
            // 循环读取
            while ((len = raf.read(data)) != -1) {
                // 将读取到的数据写出到目标文件
                int index = (int) target.length();
                target.write(data, index, len);
            }
            log.info("复制完毕！");
            return true;
        } catch (IOException e) {
            log.warning(String.format("文件拷贝操作出错:%s   %s   %s", file, targetPath, e.getMessage()));
        } finally {
            // 关闭资源
            try {
                raf.close();
                target.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 删除多级目录
     */
    public static boolean deleteFile(File file) {
        // 判断文件是否有下级目录
        if (file.isDirectory()) {
            // 获取子集目录
            File[] files = file.listFiles();
            // 遍历每个子集文件
            for (File f : files) {
                // 每个子集文件递归调用该方法
                deleteFile(f);
            }
        }
        // 删除文件
        return file.delete();
    }

    /**
     * 读取文件到字符串
     *
     * @param file
     * @return
     */
    public static String readFileToString(File file) {
        return readFileToString(file, "UTF-8");
    }

    /**
     * 读取文件到字符串
     *
     * @param file    目标文件
     * @param charset 字符集
     * @return
     */
    public static String readFileToString(File file, String charset) {
        String result = "";
        try {
            FileReader fileReader = new FileReader(file);
            Reader reader = new InputStreamReader(new FileInputStream(file), charset);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            result = sb.toString();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            log.warning(String.format("读取文件到字符串操作出错:%s   %s   %s", file, charset, e.getMessage()));
        }
        return null;
    }


    /**
     * 读取Text文件文本
     *
     * @param filePathAndName
     * @return
     */
    public static List<String> readerTextFile(String filePathAndName) {
        File file = new File(filePathAndName);
        if (file.isFile()) {
            List<String> text = new ArrayList<>();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempStr;
                while ((tempStr = reader.readLine()) != null) {
                    text.add(tempStr);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return text;
        }
        return null;
    }


    public static boolean writerTextFile(String filePathAndName, String date) {
        return writerTextFile(filePathAndName, date, false);
    }

    public static boolean writerTextFile(String filePathAndName, String date, boolean append) {
        return writerTextFile(filePathAndName, date, append, null);
    }

    /**
     * 将文本写出到文件，文件不存在则创建
     *
     * @param filePathAndName 文件储存路径及文件名
     * @param date            需要写入到文件的内容
     * @param append          是否写入到文件末尾，false将覆盖文件
     * @param charsetName     写入文件的字符集，null则为系统默认
     * @return
     */
    public static boolean writerTextFile(String filePathAndName, String date, boolean append, String charsetName) {
        try {
            String filePath = filePathAndName;
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter printWriter = null;
            if (charsetName == null || "".equals(charsetName.trim())) {
                printWriter = new PrintWriter(new FileWriter(filePathAndName, append));

            } else {
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charsetName)));
            }
            printWriter.println(date);
            printWriter.flush();
            printWriter.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 文档导出到本地文件
     *
     * @param data     文档二进制
     * @param filePath 本地路径
     * @param fileName 文件名称
     * @throws IOException
     */
    public static void download(ByteArrayOutputStream data, String filePath, String fileName) throws IOException {

        if (fileName == null || "".equalsIgnoreCase(fileName.trim())) {
            fileName = "download";
        }

        File file = getFile(filePath, fileName);

        FileOutputStream out = new FileOutputStream(file);
        io(data.toByteArray(), out);
    }

    /**
     * 获取路径下指定文件，不存在则创建
     *
     * @param filePath
     * @param fileName
     * @return
     * @throws IOException
     */
    public static File getFile(String filePath, String fileName) throws IOException {
        getFilePath(filePath);
        File file = new File(filePath + File.separator+ fileName);
        if (!file.exists()) {
            file.createNewFile();//创建文件
        }
        return file;
    }

    /**
     * 获取路径文件夹,不存在则创建
     *
     * @param filePath
     * @return
     */
    public static File getFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();//创建文件夹
        }
        return file;
    }



    /**
     * 压缩文件夹.zip
     *
     * @param filePath
     */
    private static String zipUpFile(String filePath) {
        String resourcePath = "";
        InputStream inputStream = null;
        ZipOutputStream zipOutputStream = null;
        OutputStream outputStream = null;
        try {
            //需要压缩的文件夹
            File file = new File(filePath);
            String dirName = file.getName();
            String fileParentPath = file.getParent();
            resourcePath = fileParentPath + File.separator + dirName + ".zip";

            //需要生成的压缩包名称和生成路径
            outputStream = new FileOutputStream(resourcePath);
            zipOutputStream = new ZipOutputStream(outputStream);

            //获取目录结构
            Map<String, String> map = new HashMap<>();
            map = zipUpFileCatalog(file, map);

            //通过key遍历map
            Set<String> keySet = map.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                //key(当是空文件夹的时候key为目录，当文件夹有文件的时候key为文件名)
                String fileName = iterator.next();
                //value(当是空文件夹的时候value为""，当文件夹有文件的时候value为目录)
                String path = map.get(fileName);
                if (path.equals("")) {
                    //空文件夹
                    //这里获取从压缩包开始的路径   \Bypass\Logs>>>>>>2020-09-12.txt  \Bypass\Music
                    String[] basePath = fileName.split(dirName);
                    String parent = basePath[1];
                    //压入压缩包流文件的存放路径  \Bypass\Music
                    zipOutputStream.putNextEntry(new ZipEntry(parent + File.separator));
                } else {
                    //正常文件
                    //文件转输入流
                    inputStream = new FileInputStream(path + File.separator + fileName);
                    //这里获取从压缩包开始的路径   \Bypass\Logs>>>>>>2020-09-12.txt  \Bypass>>>>>>使用须知.txt
                    String[] basePath = path.split(dirName);
                    String parent = basePath[1];
                    zipOutputStream.putNextEntry(new ZipEntry(parent + File.separator + fileName));
                }
                //写文件
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(bytes)) != -1) {
                    zipOutputStream.write(bytes, 0, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resourcePath;
    }

    /**
     * 解压缩.zip
     *
     * @param zipPath    压缩文件地址
     * @return
     */
    public static String zipUnpack(String zipPath) {
        return zipUnpack(zipPath,null,null);
    }

    /**
     * 解压缩.zip
     *
     * @param zipPath    压缩文件地址
     * @param unpackPath  解压文件储存地址
     * @return
     */
    public static String zipUnpack(String zipPath,String unpackPath,Charset charset) {
        String resourcePath = "";
        InputStream bi = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            if (charset == null){
                charset = Charset.forName("GBK");
            }

            //对中文名字进行了处理
            ZipFile zipFile = new ZipFile(zipPath, charset);
            //压缩包的路径及名字，不包含后缀  .zip
            String zipName = zipFile.getName().substring(0, zipFile.getName().indexOf("."));

            if (unpackPath!=null && !"".equals(unpackPath.trim())){
                zipName = unpackPath+File.separator + zipName.substring(zipName.lastIndexOf(File.separator));
            }

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
//				String resourcePath = zipFileParentPath+"\\"+zipName+"\\";
                resourcePath = zipName + File.separator;
                if (entry.isDirectory()) {
                    //空文件夹，直接创建  压缩包路径+压缩包名字+空文件夹路径
                    File file = new File(resourcePath + entry);
                    file.mkdirs();
                } else {
                    //获取文件在压缩包内的路径
                    String entryPath = entry.getName().substring(0, entry.getName().lastIndexOf(File.separator));

                    //为存放的路径创建文件夹
                    File file = new File(resourcePath + entryPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    outputStream = new FileOutputStream(resourcePath + entry.getName());
                }
                // 写文件
                bi = zipFile.getInputStream(entry);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = bi.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bi != null) {
                    bi.close();
                    bi.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resourcePath;
    }


    /**
     * 创建要压缩的文件目录结构
     * @param file, 需要压缩的文件
     *              map 存放目录结构
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @throws
     * @Description: 使用递归的方式向map中存入目录结构
     */
    private static Map<String, String> zipUpFileCatalog(File file, Map<String, String> map) {
        File[] files = file.listFiles();
        //如果是空文件夹的时候使用路径作为key
        if (files.length == 0) {
            map.put(file.getAbsolutePath(), "");
        }
        for (File file1 : files) {
            if (file1.isDirectory()) {
                //递归
                zipUpFileCatalog(file1, map);
            }
            if (file1.isFile()) {
                //文件作为key，路径作为value
                map.put(file1.getName(), file1.getParent());
            }
        }
        return map;
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
