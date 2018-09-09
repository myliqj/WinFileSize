package com.myliqj.dir;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author IluckySi
 * @since20150505
 */
public class CopyFileUtil {

	private int buffer;
	
	private String srcPath;
	
	private String dstPath;
	
	private boolean isSetLastTime;
	private boolean isReplaceExistsFile;

	public boolean getIsSetLastTime() {
		return isSetLastTime;
	}

	public void setIsSetLastTime(boolean isSetLastTime) {
		this.isSetLastTime = isSetLastTime;
	}

	public boolean IsReplaceExistsFile() {
		return isReplaceExistsFile;
	}

	public void setIsReplaceExistsFile(boolean isReplaceExistsFile) {
		this.isReplaceExistsFile = isReplaceExistsFile;
	}

	public int getBuffer() {
		return buffer;
	}
	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public void setDstPath(String dstPath) {
		this.dstPath = dstPath;
	}

	public void copyFile() {
		File dstFile = new File(dstPath);
//		if(!dstFile.isDirectory()) {
//			try {
//				throw new Exception("请传入参数:dstPath应该为目录!");
//			} catch (Exception e) {
//				System.out.println(e.toString());
//			}
//		}
		File srcFile = new File(srcPath);
		copy(srcFile, dstFile);
	}
	
	private void copy(File srcFile, File dstFile) {
		//适配srcFile为File和Directory.
		File[] srcFileChildren = null;
		if(srcFile.isDirectory()) {
			srcFileChildren = srcFile.listFiles();
		} else {
			srcFileChildren = new File[1];
			srcFileChildren[0] = srcFile;
		}
		for(File file : srcFileChildren) {
			//如果遍历到的文件是目录并且不存在,需要在目的目录进行创建,然后进行递归.
			if(file.isDirectory()) {
				File dstFileChildrenDir = null;
				try {
					dstFileChildrenDir = new File(dstFile.getAbsoluteFile() + "/" + file.getName());
					if(!dstFileChildrenDir.exists()) {
						dstFileChildrenDir.mkdir();
					}
					copy(file, dstFileChildrenDir);
				} catch (Exception e) {
					System.out.println("创建目录" + dstFileChildrenDir.getAbsolutePath() + "发生问题!");
				}
				//如果遍历到的文件是文件并且不存在,需要在目的目录进行创建,然后进行复制.
			} else {
				Long srcTime = 0L;
				File dstFileChildrenFile = null;
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				FileOutputStream fos = null;
				BufferedOutputStream bos = null;
				try {
					
					if (dstFile.isDirectory()){
						dstFileChildrenFile = new File(dstFile + "/" + file.getName());						
					}else{
						dstFileChildrenFile = dstFile;
					}
					
					
					if(!dstFileChildrenFile.exists()) {
						file.createNewFile();
					}else{
						// exists
						if (!isReplaceExistsFile){
						  System.out.println("已存在，不替换，跟过! file=" + file.getAbsolutePath());
						  continue;
						}
					}
					
					if (getIsSetLastTime() )
						srcTime = file.lastModified();
					
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					fos = new FileOutputStream(dstFileChildrenFile);
					bos = new BufferedOutputStream(fos);
					byte[] bytes = new byte[buffer];
					int length = 0;
					while ((length = bis.read(bytes, 0, buffer)) != -1) {
						bos.write(bytes, 0, length);
						bos.flush();
					}
				} catch (Exception e) {
					System.out.println("拷贝" + file.getAbsolutePath() + "文件发生问题!");
				} finally {
					try {
						if(bos != null) {
							bos.close();
							bos = null;
						}
						if(fos != null) {
							fos.close();
							fos = null;
						}
						if(bis != null) {
							bis.close();
							bis = null;
						}
						if(fis != null) {
							fis.close();
							fis = null;
						}
						
						// 最后设置修改后的文件 时间
						if ((getIsSetLastTime()) && (srcTime!=0)) {
							File f2=new File(dstFileChildrenFile.getAbsoluteFile().toString());
							if (!(f2.setLastModified(srcTime))){
								System.out.println("     无法修改 最后时间!");
							}
						}
						
					} catch (IOException e) {
						System.out.println("关闭流发生问题!");
					}
				} 
			}
		}
	}
}