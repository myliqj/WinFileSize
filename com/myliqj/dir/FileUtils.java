/*
 * cn.edu.hfut.dmic.webcollector.util
 */
package com.myliqj.dir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 *
 * @author hu
 */
public class FileUtils {

	public static Vector<String> getFileLines(String file) throws Exception{
		Vector<String> items = new Vector<String>();
		String line; 		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		while ((line = br.readLine()) != null){ 
				items.add(line); 
		}
		br.close();
		return items;
	}
	
	/**
	* 创建单个文件
	* @param destFileName 文件名
	* @return 创建成功返回true，否则返回false
	*/
	public static boolean CreateFile(String destFileName) {
		File file = new File(destFileName);

		if (file.exists()) {
			System.out.println("创建单个文件" + destFileName + "失败，目标文件已存在！");
			return false;
		}

		if (destFileName.endsWith(File.separator)) {
			System.out.println("创建单个文件" + destFileName + "失败，目标不能是目录！");
			return false;
		}

		if (!file.getParentFile().exists()) {
			System.out.println("目标文件所在路径不存在，准备创建。。。");
			if (!file.getParentFile().mkdirs()) {
				System.out.println("创建目录文件所在的目录失败！");
				return false;
			}
		}
		// 创建目标文件
		try {
			if (file.createNewFile()) {
				System.out.println("创建单个文件" + destFileName + "成功！");
				return true;
			} else {
				System.out.println("创建单个文件" + destFileName + "失败！");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("创建单个文件" + destFileName + "失败！");
			return false;
		}
	}
	/**
	* 创建目录
	* @param destDirName 目标目录名
	* @return 目录创建成功返回true，否则返回false
	*/
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			System.out.println("创建目录" + destDirName + "失败，目标目录已存在！");
			return false;
		}
		if (!destDirName.endsWith(File.separator))
			destDirName = destDirName + File.separator;
		// 创建单个目录
		if (dir.mkdirs()) {
			System.out.println("创建目录" + destDirName + "成功！");
			return true;
		} else {
			System.out.println("创建目录" + destDirName + "成功！");
			return false;
		}
	}
	/**
	* 创建临时文件
	* @param prefix 临时文件的前缀
	* @param suffix 临时文件的后缀
	* @param dirName 临时文件所在的目录，如果输入null，则在用户的文档目录下创建临时文件
	* @return 临时文件创建成功返回抽象路径名的规范路径名字符串，否则返回null
	*/
	public static String createTempFile(String prefix, String suffix,
			String dirName) {
		File tempFile = null;
		try {
			if (dirName == null) {
				// 在默认文件夹下创建临时文件
				tempFile = File.createTempFile(prefix, suffix);
				return tempFile.getCanonicalPath();
			} else {
				File dir = new File(dirName);
				// 如果临时文件所在目录不存在，首先创建
				if (!dir.exists()) {
					if (!createDir(dirName)) {
						System.out.println("创建临时文件失败，不能创建临时文件所在目录！");
						return null;
					}
				}
				tempFile = File.createTempFile(prefix, suffix, dir);
				return tempFile.getCanonicalPath();
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("创建临时文件失败" + e.getMessage());
			return null;
		}
	}
	
    public static void deleteDir(File dir) {
        File[] filelist = dir.listFiles();
        for (File file : filelist) {
            if (file.isFile()) {
                file.delete();
            } else {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    public static void copy(File origin, File newfile) throws FileNotFoundException, IOException {
        if (!newfile.getParentFile().exists()) {
            newfile.getParentFile().mkdirs();
        }
        FileInputStream fis = new FileInputStream(origin);
        FileOutputStream fos = new FileOutputStream(newfile);
        byte[] buf = new byte[2048];
        int read;
        while ((read = fis.read(buf)) != -1) {
            fos.write(buf, 0, read);
        }
        fis.close();
        fos.close();
    }

    public static void writeFile(String fileName, String contentStr, String charset) throws FileNotFoundException, IOException {
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content);
        fos.close();
    }

    public static void writeFile(File file, String contentStr, String charset) throws FileNotFoundException, IOException {
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(String fileName, String contentStr, String charset) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(File file, String contentStr, String charset) throws FileNotFoundException, IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        byte[] content = contentStr.getBytes(charset);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFile(String fileName, byte[] content) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content);
        fos.close();
    }

    public static void writeFile(File file, byte[] content) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(String fileName, byte[] content) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static void writeFileWithParent(File file, byte[] content) throws FileNotFoundException, IOException {

        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }

    public static byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[2048];
        int read;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((read = fis.read(buf)) != -1) {
            bos.write(buf, 0, read);
        }

        fis.close();
        return bos.toByteArray();
    }

    public static byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        return readFile(file);
    }

    public static String readFile(File file, String charset) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[2048];
        int read;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((read = fis.read(buf)) != -1) {
            bos.write(buf, 0, read);
        }

        fis.close();
        return new String(bos.toByteArray(), charset);
    }

    public static String readFile(String fileName, String charset) throws Exception {
        File file = new File(fileName);
        return readFile(file, charset);
    }

	/**
	 * 关闭
	 * @param closeable 被关闭的对象
	 */
	public static void close(Closeable closeable) {
		if (closeable == null) return;
		try {
			closeable.close();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 关闭
	 * @param closeable 被关闭的对象
	 * @since 1.7
	 */
	public static void close(AutoCloseable closeable) {
		if (closeable == null) return;
		try {
			closeable.close();
		} catch (Exception e) {
		}
	}
	

    public static boolean fileDelete(String filepath)
    {
        boolean flag1 = false;
        java.io.File file = new java.io.File(filepath);
        
        flag1 = file.delete();
        return flag1;
    }
    
    // 挑选符合条件的文件迭代删除
    public static boolean getlistfilename(String path, GregorianCalendar deletetime)
    {
        
        java.io.File f = new java.io.File(path);
        
        if (f.exists())
        {
            if (f.isFile())
                return f.delete();
            else if (f.isDirectory())
            {
                File[] files = f.listFiles();
                
                for (int i = 0; i < files.length; i++)
                {
                    
                    if (files[i].lastModified() < deletetime.getTimeInMillis())
                    {
                        
                        if (!deleteFile(files[i]))
                            return false;
                    }
                }
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
    
    // 迭代删除目录下的文件
    public static boolean deleteFile(File f)
    {
        if (f.exists())
        {
            if (f.isFile())
                return f.delete();
            else if (f.isDirectory())
            {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++)
                {
                    if (!deleteFile(files[i]))
                        return false;
                }
                
                // return f.delete(); 连带目录一起删除
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
    
    // 判断文件是否存在
//    public static boolean getlistfilename(String path, String time)
//    {
//        
//        java.io.File f = new java.io.File(path);
//        
//        if (f.exists())
//        {
//            if (f.isFile())
//                return true; //f.delete();
//            else if (f.isDirectory())
//            {
//                File[] files = f.listFiles();
//                
//                for (int i = 0; i < files.length; i++)
//                {
//                    // System.out.println((files[i].getName()).substring(10,18));
//                    // System.out.println(time);
//                    **if (((files[i].getName()).substring(10, 18)).equals(time))
//                        return true;
//                }
//                return false;
//            }
//            else
//                return false;
//        }
//        else
//            return false;
//    }
    
    public static boolean isExists_file(String filepath)
    {
        boolean flag1 = false;
        java.io.File file = new java.io.File(filepath);
        if (file.exists())
        {
            flag1 = true;
        }
        return flag1;
    }

	public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
 
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

	public static String getConfig(String fileName) {

        StringBuilder sb = new StringBuilder();
        
        InputStream in = null;
        try {
            
            // 获取当前jar包所在路径
            String path = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toString();
            int begin = path.indexOf(":")+1;
            int end = path.lastIndexOf("/")+1;
            String userDir = path.substring(begin,end);
            
            File confInDir = new File(userDir + File.separator + fileName);
            URL confInClassPath = FileUtils.class.getResource("/" + fileName);
            
            // 普通配置文件获取
            if(confInDir.exists()){
                System.out.println("Load Config File:"+confInDir);
                in = new FileInputStream(confInDir);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                String line = null;
                while((line=reader.readLine())!=null){
                    sb.append(line).append('\n');
                }
                reader.close();
            }else if(null != confInClassPath){
                // web 服务配置文件获取class目录下
                in = FileUtils.class.getResourceAsStream("/" + fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                String line = null;
                while((line=reader.readLine())!=null){
                    sb.append(line).append('\n');
                }
                reader.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != in){
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }
	
}
