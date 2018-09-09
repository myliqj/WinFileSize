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
//				throw new Exception("�봫�����:dstPathӦ��ΪĿ¼!");
//			} catch (Exception e) {
//				System.out.println(e.toString());
//			}
//		}
		File srcFile = new File(srcPath);
		copy(srcFile, dstFile);
	}
	
	private void copy(File srcFile, File dstFile) {
		//����srcFileΪFile��Directory.
		File[] srcFileChildren = null;
		if(srcFile.isDirectory()) {
			srcFileChildren = srcFile.listFiles();
		} else {
			srcFileChildren = new File[1];
			srcFileChildren[0] = srcFile;
		}
		for(File file : srcFileChildren) {
			//������������ļ���Ŀ¼���Ҳ�����,��Ҫ��Ŀ��Ŀ¼���д���,Ȼ����еݹ�.
			if(file.isDirectory()) {
				File dstFileChildrenDir = null;
				try {
					dstFileChildrenDir = new File(dstFile.getAbsoluteFile() + "/" + file.getName());
					if(!dstFileChildrenDir.exists()) {
						dstFileChildrenDir.mkdir();
					}
					copy(file, dstFileChildrenDir);
				} catch (Exception e) {
					System.out.println("����Ŀ¼" + dstFileChildrenDir.getAbsolutePath() + "��������!");
				}
				//������������ļ����ļ����Ҳ�����,��Ҫ��Ŀ��Ŀ¼���д���,Ȼ����и���.
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
						  System.out.println("�Ѵ��ڣ����滻������! file=" + file.getAbsolutePath());
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
					System.out.println("����" + file.getAbsolutePath() + "�ļ���������!");
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
						
						// ��������޸ĺ���ļ� ʱ��
						if ((getIsSetLastTime()) && (srcTime!=0)) {
							File f2=new File(dstFileChildrenFile.getAbsoluteFile().toString());
							if (!(f2.setLastModified(srcTime))){
								System.out.println("     �޷��޸� ���ʱ��!");
							}
						}
						
					} catch (IOException e) {
						System.out.println("�ر�����������!");
					}
				} 
			}
		}
	}
}