package com.myliqj.dir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import testWin.TestMain;

import com.myliqj.dir.bean.DirDTInfo;
import com.myliqj.dir.bean.DirInfo;
import com.myliqj.dir.bean.FileInfo;

public class GetDirInfo {
	public static String INPUT_DEFAULT_DATETIME = "yyyy-MM-dd HH:mm"; 
	public static String OUTPUT_DEFAULT_DATETIME = "yyyy-MM-dd-HH.mm.ss"; 
	
	public static String getSystemDateTimeFormat(){
		String dateFmt = "yyyy-MM-dd";
		String timeFmt = "HH:mm";
		try {
			dateFmt = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Control Panel\\International","sShortDate") ;
			timeFmt = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Control Panel\\International","sShortTime") ;//sTimeFormat
		} catch (Exception e) { 
			dateFmt = "yyyy-MM-dd";
			timeFmt = "HH:mm";
			e.printStackTrace();
		}
		return dateFmt + " " + timeFmt;
	}
	
	public static Map getDirInfo(String path,Long topSize,Map options) throws Exception{
//		String cmd = "cmd /c dir /-c /t:w /s \"C:\\Users\\Administrator\\AppData\\Roaming\\youku\\config\"";
//		cmd = "cmd /c dir /-c /t:w /s D:\\java_run\\Quartz";
//		cmd = "cmd /c dir /-c /t:w /s D:\\db2tool";
//		cmd = "cmd /c dir /-c /t:w /s \"C:\\Program Files (x86)\"";
//		cmd = "cmd /c dir /-c /t:w /s \"C:\\Windows\"";
		if (path==null) return null;
		String cmd = null;
		
		// 是否过虑部份文件而已 ，可以在路径上带上模糊查询，即有 * 符号。   dir /s d:\*.txt
		boolean isPart = path.contains("*");
		
		File pathFile = null;
		if (!isPart){
			pathFile = new File(path);
			if (!pathFile.exists()){
				TestMain.showMessage("路径或文件["+path+"]不存在，请重新录入！");
				return null;
			}
		}
		
		long start,ys;
		String tempFileName= null;
		File tmpFile = null;
		boolean isDeleteTempFile = false;
		
		if (isPart || (pathFile!=null && pathFile.isDirectory() )) {
			path = path.replaceAll("/", "\\");
			if (!isPart && !path.endsWith("\\")) path += "\\";
			
			/*
			  dir 命令选项
			  /a   所有文件，包括隐藏文件等
			  /-c  在文件大小中显示千位数分隔符。这是默认值。用 /-C 来 禁用分隔符显示。
			  /t:w 控制显示或用来分类的时间字符域。 C 创建时间,A 上次访问时间,W 上次写入的时间
			  /s   显示指定目录和所有子目录中的文件。
			*/
			cmd = "cmd /c dir /a /-c /t:w /s \""+path+"\"";
			System.out.println(cmd);
			Process pro = Runtime.getRuntime().exec(cmd);
			
			isDeleteTempFile = true;
			tempFileName=FileUtils.createTempFile("dir_temp",".txt",null);
			System.out.println("tempFileName=" + tempFileName);
			
			PrintStream ps = new PrintStream( new FileOutputStream(tempFileName,true),true, "GBK" );		
			StreamGobbler errorGobbler = new StreamGobbler(pro.getErrorStream(), "GBK", "ERR", ps);
			StreamGobbler outputGobbler = new StreamGobbler(pro.getInputStream(), "GBK", "OUT", ps);		
			
			//StreamGobbler errorGobbler = new StreamGobbler(pro.getErrorStream(), "GBK", "ERR", System.err);
			//StreamGobbler outputGobbler = new StreamGobbler(pro.getInputStream(), "GBK", "OUT", System.out);
			start = System.nanoTime(); //System.currentTimeMillis();
			
			errorGobbler.start();
			outputGobbler.start();
			//pro.waitFor();
			int exitVal = pro.waitFor();
			ys = System.nanoTime() - start;
			ps.close();
			
			tmpFile = new File(tempFileName);
			System.out.println("使用dir命令获取目录文件信息到临时文件。 ExitValue=" + exitVal
					+ " 用时=" + (ys/1000000)+" ms "
					+ " 文件大小="+StringHelper.readableFileSize(tmpFile.length()) + "\n");
		}else{
			tmpFile = pathFile;
			System.out.println("直接读取文件：" + path);
		}
		 
		// output
		List<DirInfo> dirInfo = new ArrayList<DirInfo>();
		List<DirDTInfo> dirDTInfo = null; // new ArrayList<DirDTInfo>();
		List<FileInfo> fileInfo = null; // new ArrayList<FileInfo>(); 
		List<FileInfo> fileInfo_TopSize = null;
		if (topSize>0) fileInfo_TopSize = new ArrayList<FileInfo>();

		start = System.nanoTime();
		dirTodel(-1L,tmpFile,null,false,false,0,dirInfo,dirDTInfo,fileInfo,topSize,fileInfo_TopSize,options);
		ys = System.nanoTime() - start;		
		if (isDeleteTempFile){
			// remove temp file
			tmpFile.deleteOnExit(); 
			System.out.println("deleteOnExit file:" + tempFileName
					+" "+StringHelper.readableFileSize(tmpFile.length()) );
		}

		// set-sub-all 
		int curLevel = 0; // 层数，相对首个的    / 数量
		DirInfo dir0 = null; int dir0_len = 0;
		if (dirInfo.size()>0) {
			dir0 = dirInfo.get(0);
			//System.out.println("s-dir0: " + dir0.getDir_name());
			dir0_len = dir0.getDir_name().length();
		}
		long topsl = fileInfo_TopSize==null?0:fileInfo_TopSize.size();
		System.out.println("从临时文件处理到对象，用时=" + (ys/1000000)+" ms 总目录数量="+dirInfo.size()
				+(topSize>0?" 达到指定大小("+StringHelper.readableFileSize(topSize)+"字节)的文件数量="+topsl:"")+"\n");
		
		start = System.nanoTime();
		int allCount = dirInfo.size(); long all_file_size = 0L; long for_sl = 0;
		for (int i=0; i<allCount;i++){
			//System.out.println(dir);
			for_sl++;
			
			DirInfo dir = dirInfo.get(i);
			dir.setDir_sub_file_count( dir.getDir_file_count() ); // 先包含自己
			dir.setDir_sub_file_size( dir.getDir_file_size() ); // 先包含自己
			
			if (i>0){
				String curName = dir.getDir_name();
				try {					
					curName = dir.getDir_name().substring(dir0_len+1);
				} catch (Exception e) {}
				dir.setLevel( StringHelper.appearNumber(curName, "\\") );
			}
			
			for (int j=i+1; j<allCount; j++){
				for_sl++;
				// 向下找到不是名称开头的为止，因为数组已是排序了的
				DirInfo dir2 = dirInfo.get(j);
				if (dir2.getDir_name().startsWith(dir.getDir_name())){
					
					dir2.setParent_dir_id(dir.getDir_id()); // 根据顺序，设置上级
					
					// 子文件夹数,不包含自己，每个文件夹+1
					dir.setDir_sub_all_count( dir.getDir_sub_all_count() + 1);
					
					// 总文件数，累计
					dir.setDir_sub_file_count( dir.getDir_sub_file_count() + dir2.getDir_file_count() );
					
					// 总文件大小, 累计
					dir.setDir_sub_file_size( dir.getDir_sub_file_size() + dir2.getDir_file_size() );
					
					
				}else{
					break;
				}
			} 
			
		}
		
		// output
//		for (DirInfo dir : dirInfo){
//			System.out.println(dir); 
//		}
		
		// sort
		Collections.sort(dirInfo, new Comparator<DirInfo>(){ 
            public int compare(DirInfo o1, DirInfo o2) {
                 //按照 包括子文件夹最占空间 降序排列 
            	if (o2.getDir_sub_file_size().longValue()>o1.getDir_sub_file_size().longValue()){
            		return 1;
            	}else if (o2.getDir_sub_file_size().longValue()==o1.getDir_sub_file_size().longValue()){
            		return 0;
            	}
                return -1; 
            } 
         });
		if (fileInfo_TopSize!=null){ 
			Collections.sort(fileInfo_TopSize, new Comparator<FileInfo>(){ 
	            public int compare(FileInfo o1, FileInfo o2) {
	                 //按照 文件 最占空间 降序排列 
	            	if (o2.getFile_size().longValue()>o1.getFile_size().longValue()){
	            		return 1;
	            	}else if (o2.getFile_size().longValue()<o1.getFile_size().longValue()){
	            		return -1;
	            	}
	                return 0; 
	            } 
	         });
		}
		

		// output
		System.out.println(dir0
				+ " , " + StringHelper.readableFileSize(dir0.getDir_sub_file_size())
				+ " , " + String.format("%.2f", dir0.getDir_sub_file_size().floatValue()/dir0.getDir_sub_file_size().floatValue()*100) +"%");
		System.out.println("================== 1 ===============");
		long v_dir_id = 0L;
		int out_cnt = 0;
		for (DirInfo dir : dirInfo){
			if (dir.getLevel() == 1){
				if (v_dir_id!=0L){
					v_dir_id = dir.getDir_id().longValue();
				}

			    out_cnt ++;
				if (out_cnt>30){
					System.out.println(" ... ");
					break;
				}
				System.out.println(dir 
						+ " , " + StringHelper.readableFileSize(dir.getDir_sub_file_size())
						+ " , " + String.format("%.2f", dir.getDir_sub_file_size().floatValue()/dir0.getDir_sub_file_size().floatValue()*100) +"%"); 
			}
		}

//		System.out.println("\n================== 2 ===============");
//		out_cnt = 0;
//		for (DirInfo dir : dirInfo){
//			if (dir.getLevel() == 2 && dir.getDir_id().longValue() == v_dir_id){
//				out_cnt ++ ;
//				if (out_cnt>50) break;
//				System.out.println(dir 
//						+ " , " + StringHelper.readableFileSize(dir.getDir_sub_file_size())
//						+ " , " + String.format("%.2f", dir.getDir_sub_file_size().floatValue()/dir0.getDir_sub_file_size().floatValue()*100) +"%"); 
//			}
//		}
		
		//System.out.println(Arrays.asList(dirInfo));
		//System.out.println(dirDTInfo);
		//System.out.println(fileInfo);
		
//		if (dirInfo.size()>0) {
//			dir0 = dirInfo.get(0);
//			System.out.println("root-dir0: " + dir0.getDir_name());
//		}
		dir0 = dirInfo.get(0);
		ys = System.nanoTime() - start;	
		System.out.println("处理目录对象汇总、排序，用时：" + (ys/1000000)+" ms 汇总总循环次数="+for_sl
				+" 总目录="+dir0.getDir_sub_all_count()+" 总文件="+dir0.getDir_sub_file_count()+"\n");

		if (1==1) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("dir", dirInfo);
			map.put("file_topsize", fileInfo_TopSize);
			return map;
		}
		
		return null;
//		BufferedReader br = new BufferedReader( new InputStreamReader(pro.getInputStream(),Charset.forName("GBK"))  );
//		String line = null;
//		while ((line = br.readLine()) != null){
//			System.out.println(line);
//		}
//		br.close();
//
//		br = new BufferedReader( new InputStreamReader(pro.getErrorStream(),Charset.forName("GBK"))  );
//		while ((line = br.readLine()) != null){
//			System.out.println(line);
//		} 
//		br.close();
		
//		BufferedInputStream br = new BufferedInputStream(pro.getInputStream());
//        BufferedInputStream br2 = new BufferedInputStream(pro.getErrorStream());
//        
//        int ch;
//        System.out.println("Input Stream:");
//        while((ch = br.read())!= -1){
//            System.out.print((char)ch);
//        } 
//        System.out.println("Error Stream:");
//        while((ch = br2.read())!= -1){
//            System.out.print((char)ch);
//        } 
       
	}
	
	public static void mainrr(String[] args) {
		
		Map<String, String> envs = System.getenv();		
		for (String name: envs.keySet()) {			
			System.out.println(name + " ---> " + envs.get(name));		
		}				
		System.out.println(System.getenv("JAVA_HOME"));
		
		
		
		Properties sysProps = System.getProperties(); // 获得属性集合		
		System.out.println(System.getProperty("os.name")); // 直接获取指定的系统属性		
		System.out.println(sysProps); // 在属性集合中获取指定属性				for (Object key: sysProps.keySet()) { // 程序中遍历			String name = (String)key;			System.out.println(sysProps.getProperty(name));		}
		Set<String> s=sysProps.stringPropertyNames();
        for (String x:s) {
            System.out.println(x+" :"+sysProps.get(x));
        }
        
        Locale currentLocale = Locale.getDefault();
        System.out.println(currentLocale);
        System.out.println((new Date()).toLocaleString());
        DateFormat formatter = DateFormat.getDateTimeInstance();
        //formatter.format(date)
        System.out.println(Locale.getDefault(Locale.Category.FORMAT));
        
        Locale loc = currentLocale;
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), loc);
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        
        SimpleDateFormat sdf = new SimpleDateFormat();
        System.out.println(sdf.toPattern());
        
        DateFormat df = DateFormat.getDateTimeInstance(2, 2);
        System.out.println("current-date:" + df.format(new Date()));
        
        SimpleDateFormat aa = new SimpleDateFormat();
//        System.out.println(Preferences.userRoot()); 
//        Preferences re = Preferences.userRoot();
//        try {
//			System.out.println(re.nodeExists("HKEY_CURRENT_USER\\Control Panel\\International"));
//		} catch (BackingStoreException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        
        String dateFmt = null;
        String timeFmt = null;
			try {
				dateFmt = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Control Panel\\International","sShortDate") ;
				timeFmt = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Control Panel\\International","sShortTime") ;
//				Map<String,String> map = WinRegistry.readStringValues(WinRegistry.HKEY_CURRENT_USER, "Control Panel\\International");
//				System.out.println("sShortDate=" + map.get("sShortDate"));
//				System.out.println("sShortTime=" + map.get("sShortTime"));
			} catch (IllegalArgumentException | IllegalAccessException
					| InvocationTargetException e1) {
				e1.printStackTrace();
			}
		System.out.println("DateTime:" + dateFmt + " "+ timeFmt);
        
        //AbstractPreferences wp = WindowsPreferencesFactory.
        // WinRegistry.java
        // HKEY_CURRENT_USER\Control Panel\International
        
        
		String st=getSystemDateTimeFormat();
		System.out.println("curr-sys-set:" + st);
		java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat(st);
		// 2010-03-25-00.00.00
		java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		
//        SimpleDateFormat sdf_out = (SimpleDateFormat) DateFormat.getDateTimeInstance();
//        SimpleDateFormat sdf_in = sdf_out;
        System.out.println();
        String out = null;
		try {
			out = outputFormat.format(inputFormat.parse("2017-11-12 04:32"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("===" + out);
        
        //System.out.println(LocaleProviderAdapter.getResourceBundleBased().getDateFormatProvider().toString());
        
//        System.out.println(LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(locale)              
//        .getDateTimePattern(3, 3, calendar));
	}
	public static void main(String[] args) throws Exception{
		if (true){

			// 2018-10-21  18:13    <SYMLINKD>     md [E:\Python27]
			// 2018-10-21  18:15    <SYMLINKD>     mj [E:\Python27]
			String line = "<SYMLINKD> maad [E:\\Python27]";
			int len = line.length();
			int start = line.indexOf("<SYMLINKD>") + 10;
			while (start<len && (line.charAt(start) ==' ')) start++; // 跳过空格
			int end = line.indexOf('[', start+1);
			System.out.println("start="+start + " ,end="+end);
			String dir = line.substring(start,end).trim();
			System.out.println(dir);
			return;
		}
		
		
//		GetDirInfo.getDirInfo("C:\\Windows\\temp\\",1*1024L);
//		String a="2018-09-02  下午 10:32";
//		System.out.println(a.length());
		
		//yyyy-MM-dd a h:mm
//		java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd  a h:mm");
//		String d = "2018-10-11  下午 11:54";
////		System.out.println(f.format(new Date()));
//		System.out.println(f.parse(d));
//		
//		if (1==1){return ;};
//		
//		String path = "d:\\java_run\\";
		String path = "C:\\Windows\\temp\\";

		long topsize = 50L*1024*1024; // 50mb 为基础
		topsize = 500*1024L;
		
		Map map = null;	
		//PrintStream cacheStream = null;
		//PrintStream oldStream = System.out;
		try{ 
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			cacheStream = new PrintStream(baos);
//			System.setOut(cacheStream);
			
			map = GetDirInfo.getDirInfo(path,topsize,null);

			//System.out.println("\n"+baos.toString()); 
		
			if (map==null) return;
			
			List<DirInfo> dirInfo = (List<DirInfo>)map.get("dir");
			
			List<FileInfo> fis = (List<FileInfo>) map.get("file_topsize");
			if (fis!=null){
				// 返回值 已排序，倒序
				// 显示 
				StringBuilder sb = new StringBuilder();
				for (FileInfo fi : fis) {
					String len = StringHelper.readableFileSize(fi.getFile_size());
					sb.append(fi.getFile_time()).append(" ");
					sb.append((len.length()<10)?StringHelper.repeat(" ", 10-len.length()):"")
					  .append(len).append(" ").append(fi.getFile_name()).append("\n");
				}
				System.out.println(sb.toString()); 
			}
		}finally{
			//System.setOut(oldStream);
		}
		
	}
	public static void maina(String[] args) throws Exception{
		//Map<String, PropertyDescriptor> m=BeanUtil.getFieldNamePropertyDescriptorMap(DirInfo.class);
		//System.out.println(m);
//		PropertyDescriptor[] aa = BeanUtil.getPropertyDescriptors(DirInfo.class);
//		System.out.println(Arrays.asList(aa));
	}
	
	public static void main22(String[] args) throws Exception{
		
		String line= "12672-06-01  09:43             30208 深圳社保滞纳金项目会议纪要（2012-03-23）.doc";
		String dt = line.substring(0,17); String dt_y ="";
		int i = dt.indexOf('-');
			if (i>0){
				dt_y = dt.substring(0,i);
				if (Integer.valueOf(dt_y)>2050){
					dt = "2050"+line.substring(i,i+17-4);
				}
			}
		System.out.println(dt_y + " dt="+dt);
		
		
		if (1==1) return;
		
		
		String rootDir = "H:\\__dir\\20170924_myliqj_all__dir_history_all\\20170924\\";
		String outDir = rootDir + "del\\";
		
		File dir = new File(rootDir);

		File[] filelist = dir.listFiles();
		int inFileNo = 0;
        for (File file : filelist) {
        	if (file.getName().startsWith("20170924")){
        		inFileNo++;
        		System.out.println(inFileNo+","+file.getName());
        		
        	}
        }
	}
	public static Map dirTodel(Long inFileNo,File inFile,String outDir,boolean isOnlyOutputLoad
			,boolean isOutputFile,int i_SkipRow
			,List<DirInfo> dirInfo,List<DirDTInfo> dirDTInfo
			,List<FileInfo> fileInfo
			,Long topSize                    // 返回指定大小的文件，字节，<=0 表示不返回
			,List<FileInfo> fileInfo_TopSize // 指定大小的文件列表
			,Map<String,String> options
			) throws Exception{
		String inFileName = inFile.getName();
		int dot = inFileName.lastIndexOf('.');
		String fileName = inFileName.substring(0,dot);//-1 不用减1的
		
		Charset readCharset = Charset.forName("GBK");		
		Charset writeCharset = Charset.forName("GBK");
		
		/*
		  三个文件对应的结构：
		  DirDel    : src_id, dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name
		  DriDtDel  : src_id, parent_dir_id, dir_time, dir_name
		  fileDel   : src_id, dir_id, file_time,file_size, file_name, file_ext
		
		   其中 src_id bigint 指文件所属id，从1开始
		    dir_id bigint 指目录所属id,从1开始，从读文件顺序开始
		    dir_file_count bigint 指对应目录的文件数据，不包括子目录
		    dir_file_size bigint 指对应目录的文件大小合计（字节），不包括子目录
		    dir_sub_count bigint 指对应目录的子目录数量，不包括下级子目录       ***注意，包括了 . 和 .. 二个目录，所以此值至少>=2 
		    dir_name C2000  指目录名称
		    
		    parent_dir_id bigint 指当前目录的上级目录ID号
		    dir_time timestmap 指目录的最后修改时间  yyyy-MM-dd-HH.mm.ss
		    
		    file_time timestmap 指文件最后修改时间 yyyy-MM-dd-HH.mm.ss
		    file_size bigint 指文件大小（字节）
		    file_name C2000 指文件名，不含路径
		    file_ext C100 指文件扩展名，不带小数点。
		  
		  create table t_im_src_id_rel(src_id bigint,driver_name varchar(100));
		  create table t_im_dir(src_id bigint, dir_id bigint, dir_file_count bigint, dir_file_size bigint, dir_sub_count bigint, dir_name varchar(2000));
		  create table t_im_dir_dt(src_id bigint, parent_dir_id bigint, dir_time timestamp, dir_name varchar(2000));
		  create table t_im_file(src_id bigint, dir_id bigint, file_time timestamp,file_size bigint, file_name varchar(2000), file_ext varchar(100));
		  
		  其中 t_im_src_id_rel 手工导入  src_id 与文件名的关系，才能找到文件所属盘
		  其它三个表使用 load 导数。
		  load from xx of del messages load.msg insert into xx NONRECOVERABLE
		  
		*/

		if (isOutputFile) {
			PrintWriter pw1_src_id=new PrintWriter(new OutputStreamWriter(new FileOutputStream(outDir + "src_id_sql.txt",true), writeCharset));
			pw1_src_id.println("insert into t_im_src_id_rel(src_id,driver_name) select "+inFileNo+",'" + inFileName + "' from sysibm.sysdummy1;" );
			pw1_src_id.flush();
			pw1_src_id.close();
			
			PrintWriter pw1_load=new PrintWriter(new OutputStreamWriter(new FileOutputStream(outDir + "load_cmd.txt",true), writeCharset));
			pw1_load.println("load from imdir_" + fileName + ".del of del messages load.msg insert into t_im_dir NONRECOVERABLE;");
			pw1_load.println("load from imdirdt_" + fileName + ".del of del messages load.msg insert into t_im_dir_dt NONRECOVERABLE;");
			pw1_load.println("load from imfile_" + fileName + ".del of del messages load.msg insert into t_im_file NONRECOVERABLE;"); 
		    pw1_load.flush();
		    pw1_load.close();
		}
		
		if (isOnlyOutputLoad){
			return null;
		}

		String delFileNameOfFile = outDir + "imfile_" + fileName + ".del";       // 导出文件对应的 del
		String delFileNameOfDir = outDir + "imdir_" + fileName + ".del";         // 导出目录对应的del
		String delFileNameOfDirDt = outDir + "imdirdt_" + fileName + ".del";     // 导出目录对应的时间del

		PrintWriter pw=null ,pw_dir=null ,pw_dir_dt = null;
		if (isOutputFile) {
			pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(delFileNameOfFile), writeCharset)); 
			pw_dir=new PrintWriter(new OutputStreamWriter(new FileOutputStream(delFileNameOfDir), writeCharset)); 
			pw_dir_dt=new PrintWriter(new OutputStreamWriter(new FileOutputStream(delFileNameOfDirDt), writeCharset)); 
		}	
		 	
		String dtFormat = getSystemDateTimeFormat();
		if (options!=null && StringHelper.isNotEmpty( options.get("setDateTimeFormat") )){
			dtFormat = options.get("setDateTimeFormat");
		}
		int dateParseErrorCountMax = 5000; // 如果日期时间解释错误多少个则退出处理
		if (options!=null && StringHelper.isNotEmpty( options.get("setDateParseErrorCountMax") )){ 
			try {				
				dateParseErrorCountMax = Integer.valueOf( options.get("setDateParseErrorCountMax") );
			} catch (Exception e) {}
		}
		boolean isSkipSYMLINKD = true;
		if (options!=null && StringHelper.isNotEmpty( options.get("setSkipSYMLINKD") )){ 
			try {				
				isSkipSYMLINKD = "YES".equalsIgnoreCase(options.get("setSkipSYMLINKD"));
			} catch (Exception e) {}
		}
				
		boolean timeHastt = dtFormat.indexOf("tt")>0;
		if (timeHastt) dtFormat = dtFormat.replace("tt", " a");
		System.out.println("current datetime format:" + dtFormat);
		java.text.SimpleDateFormat inputFormat = null;
		try {
			inputFormat = new java.text.SimpleDateFormat(dtFormat);
		} catch (Exception e) {
			e.printStackTrace();
			inputFormat = new java.text.SimpleDateFormat(INPUT_DEFAULT_DATETIME); // 解析不正确，使用回默认值
		} 
		// 2010-03-25-00.00.00
		java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat(OUTPUT_DEFAULT_DATETIME);
		
		char c_date_sep = '-';
		if(dtFormat!=null && dtFormat.indexOf(c_date_sep)==-1){
			for (int i = 0; i < dtFormat.length(); i++) {
				char c = dtFormat.charAt(i);
				if (!(c==' ' || ( c>='0' || c<='9'))) {
					// 找到第一个非空格和数字，判断为日期分隔符
					c_date_sep = c;
				}
			}
		}
		 
		String curPath = ""; long curDirNo = 1; String dir_dt_out = "";
		long p_curDirNo = 1; long p_dirSize = 0;  // 上一次序号与大小(从文件汇总)
		long p_parent_dir_id = -1;
		long p_DirFileCount = 0; long p_DirSubCount = 0;
		String dt = ""; String dt_out = ""; 
		String size = ""; String file=""; String ext = "";
		int errCount = 0; int skipEmpty = 0; int skipSummary = 0; int skipSubDir = 0;
		int skipJUNCTION = 0; int skipSYMLINK = 0; int errDirTooLongCount = 0;
		int rows = 0; int len =0 ; int FileCount = 0;
		int skipSYMLINKD = 0;
		int dateParseErrorCount = 0;
		
		Set<String> skipSD = null;
		if (isSkipSYMLINKD) skipSD = new LinkedHashSet<String>(); // 跳过 <SYMLINKD> 和 <JUNCTION>
		// <JUNCTION> 是由 mklink /j 创建的,例如 mklink /d /h /j md_j E:\Python27\
		// <SYMLINKD> 是由 mklink /d 创建的,例如 mklink /d mj E:\Python27
		
		BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(inFile), readCharset)  );
		String line = "";
		while ((line = br.readLine()) != null){ 
			rows ++;  
			if (rows<=i_SkipRow) {
				// 跳过最前面的5行
				skipSummary ++;
				continue;
			}
			
			len = line.length();
			if (line.trim().length()==0) {
				skipEmpty ++;
				continue;
			}
			if (line.startsWith("end ----") || line.startsWith("     所列文件总数")
					|| line.startsWith(" 驱动器 ") || line.startsWith(" 卷的序列号是")){
				skipSummary ++;
				continue;
			}
			
			boolean isDir = line.endsWith("的目录");
			if (isDir){
				
				// 检查是否链接目录，如果是，则跳过不处理
				String cur = line.substring(0, len-3).trim();
				if (cur.charAt(cur.length()-1) !='\\'){
					cur += "\\";
				}
				if (skipSYMLINKD>0 && isSkipSYMLINKD){
					// 检查相应所有子目录，如果有则全部跳过
					boolean isFind = false;
					for (String str : skipSD) {
						isFind = cur.startsWith(str); // 最后字符都有 \ ，所以应该不会只匹配一半
						if (isFind){
							break;
						}
					}
					if (isFind){
						continue;
					}
				}
				
				
				if (p_curDirNo>1) {// 从1开始，首次的不要保存，   ---表示整个所有，parent_dir_id=-1，其它时候 parent_dir为不正确的值，要另外经过处理才行
					// src_id, dir_id,p_parent_dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name 
					if (isOutputFile) {
						pw_dir.println(inFileNo + "," + p_curDirNo+ "," + p_parent_dir_id+ "," + p_DirFileCount +"," + p_dirSize + "," + p_DirSubCount + ",\"" + curPath.replace("\"", "\"\"") + "\"");
					}else{
						// bean
						if (dirInfo!=null) dirInfo.add(new DirInfo(inFileNo , p_curDirNo, p_parent_dir_id, p_DirFileCount, p_dirSize , p_DirSubCount , curPath ));
					}
				}
				curPath = cur;
				
				curDirNo ++;
				
				p_dirSize = 0;
				p_DirFileCount = 0;
				p_DirSubCount = 0;
				p_parent_dir_id = p_curDirNo;
				p_curDirNo = curDirNo;
				//pw_dir.println(inFileNo + "," + curDirNo + ",\"" + curPath.replace("\"", "\"\"") + "\"");
				continue;
			}
			
			//if (curPath.trim().length()==0) continue;
			
			if (len>17){ 
				if (line.contains("<SYMLINK>")){
					skipSYMLINK ++;
					continue;
				}else if (line.contains("<SYMLINKD>") || line.contains("<JUNCTION>")){
					int start = 0;
					if (line.contains("<SYMLINKD>")){
						skipSYMLINKD ++;
						if (isSkipSYMLINKD) start = line.indexOf("<SYMLINKD>") + 10;
					}else if (line.contains("<JUNCTION>")){
						skipJUNCTION ++;
						if (isSkipSYMLINKD) start = line.indexOf("<JUNCTION>") + 10;
					}
					// 2018-10-21  18:18    <JUNCTION>     md_j [E:\Python27]
					// 2018-10-21  18:13    <SYMLINKD>     md [E:\Python27]
					if (isSkipSYMLINKD) {
						while (start<len && (line.charAt(start) ==' ')) start++; // 跳过空格
						int end = line.indexOf('[', start+1);
						String dir = line.substring(start,end).trim();
						skipSD.add(curPath+dir+"\\");
					}
					continue;
				}else if (line.contains("<DIR>")){
					// 当前是目录中的目录，不处理
					skipSubDir ++;
					p_DirSubCount ++; 
					try{ 						
						if (!timeHastt){
							dir_dt_out = outputFormat.format(inputFormat.parse(line.substring(0,17)));
						}else{
							// 日期时间有包括上午/下午
							dir_dt_out = outputFormat.format(inputFormat.parse(line.substring(0,20)));
							//String s_dt = line.substring(0,17+5);
							//dir_dt_out = outputFormat.format(inputFormat.parse(
							//		s_dt.replace("上午 ", "am").replace("下午", "pm").substring(0,20) ));							
						}
						
						int start = line.indexOf("<DIR>") + 5;
						while (start<len && (line.charAt(start) ==' ')) start++;
						file = line.substring(start);
						
						if (!".".equals(file) && !"..".equals(file))
							// src_id, parent_dir_id, dir_time, dir_name 
							if (isOutputFile) {
								pw_dir_dt.println(inFileNo + "," + curDirNo +",\"" + dir_dt_out+"\",\"" + file.replace("\"", "\"\"") + "\"");//字符串末尾不需要换行符
							}else{
								// bean
								if (dirDTInfo!=null) dirDTInfo.add(new DirDTInfo(inFileNo , curDirNo, dir_dt_out, file ));
							}
					}catch (Exception e) {
						e.printStackTrace();
						errCount ++;
						dateParseErrorCount++;
						if (dateParseErrorCountMax>0 && dateParseErrorCount>=dateParseErrorCountMax){
							System.err.println("dateParseErrorCount=" + dateParseErrorCount);
							break;
						}
						continue;
					} 
					continue;
				}else if (line.startsWith("          ")){
					skipSummary ++;
					continue;
				} else {
					
					// 可能会有  : "目录名称 ..... 过长。" 的报错，处理一下。
					if (line.startsWith("目录名称")){
						if (line.endsWith("过长。")){
							errDirTooLongCount++;
							continue;
						}
					}
					
					
					// 时间有超前的，需要处理一下，例子 27672-06-01-09.04.00
					// 2018-05-06  下午 02:58    <DIR>          Program Files (x86)
					// 2015-07-07  下午 01:41               304 SSSE32.ini
					dt = line.substring(0,17);	
					if (timeHastt){
						// 日期时间有包括上午/下午
						dt = line.substring(0,20);
						//dt = dt.replace("上午", "am").replace("下午", "pm").substring(0,20);							
					}
					try{ 
						String dt_y ="";
						int i = dt.indexOf(c_date_sep);
						if (i>0){
							dt_y = dt.substring(0,i);
							if (Integer.valueOf(dt_y)>2050){
								dt = "2050"+line.substring(i,i+17-4);
							}
						}
						dt_out = outputFormat.format(inputFormat.parse(dt));
						
					}catch (Exception e) {
						e.printStackTrace();
						errCount ++;
						dateParseErrorCount++;
						if (dateParseErrorCountMax>0 && dateParseErrorCount>=dateParseErrorCountMax){
							System.err.println("dateParseErrorCount=" + dateParseErrorCount);
							break;
						}
						continue;
					}
					
					// 找第一个数字
					int i = 17;
					if (timeHastt) i+=5;
					while (i<len && !(line.charAt(i)>='0' && line.charAt(i)<='9')) i++;
					int start=i;
					// 找下一个空格
					while (i<len && !(line.charAt(i) ==' ')) i++;
					
					long curFileSize = 0;
					try{
						size = "0";
						if (i>start){
							size = line.substring(start, i);
							try {
								curFileSize = Long.valueOf(size.trim());
							} catch (Exception e) {}
						}
						file = line.substring(i+1); 
						FileCount ++;
						
						p_DirFileCount ++;
						p_dirSize += curFileSize;
					} catch (Exception e) {
						e.printStackTrace();
						errCount ++;
						continue;
					}
					//System.out.println("  " + dt + " " + size + " " + file);
					
					ext = "";
					start = file.lastIndexOf(".");
					if (start>0){
						ext = file.substring(start+1);
						if (ext.length()>50) ext = ext.substring(0,50);
					}
					
					// src_id, dir_id, file_time,file_size, file_name, file_ext 
					if (isOutputFile){
						pw.println(inFileNo + "," + curDirNo +",\"" + dt_out + "\"," + size 
								+ ",\"" + file.replace("\"", "\"\"") + "\""
								+ ",\"" + ext.replace("\"", "\"\"") + "\"");//字符串末尾不需要换行符
					}else{
						
						if (fileInfo!=null){
							FileInfo fi = new FileInfo(inFileNo , curDirNo, dt_out, curFileSize , file , ext );
							fileInfo.add(fi);
						}
						if (topSize>0){
							if (curFileSize>=topSize && fileInfo_TopSize!=null){
								FileInfo fi = new FileInfo(inFileNo , curDirNo, dt_out, curFileSize , curPath+file , ext );
								fileInfo_TopSize.add(fi);
							}
						}
						
					}
				}
				
			}else{
				skipEmpty ++;
			}
			if (rows % 5000 == 0){
				if (isOutputFile){
					pw.flush();pw_dir.flush();pw_dir_dt.flush();
				} 
				System.out.println("Dir:" + curDirNo + " , File:" + FileCount + ",  Read-Rows:" + rows 
						+" (err="+errCount+",errDTL="+errDirTooLongCount+",empty="+skipEmpty +",summary="+skipSummary+",subdir="+ skipSubDir 
						+ ",symlink="+skipSYMLINK+ ",symlinkd="+skipSYMLINKD +",junction="+ skipJUNCTION+ ") ... ");
			} 
		}
		br.close();
		
		if (p_curDirNo>0)  
			// src_id, dir_id, p_parent_dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name 
			if (isOutputFile) {
				pw_dir.println(inFileNo + "," + p_curDirNo+ "," + p_parent_dir_id+ "," + p_DirFileCount +"," + p_dirSize + "," + p_DirSubCount + ",\"" + curPath.replace("\"", "\"\"") + "\"");
			}else{
				// bean
				if (dirInfo!=null) dirInfo.add(new DirInfo(inFileNo , p_curDirNo, p_parent_dir_id, p_DirFileCount, p_dirSize , p_DirSubCount , curPath ));
			}

		System.out.println("Dir:" + curDirNo + " , File:" + FileCount + ",  Read-Rows:" + rows 
				+" (err="+errCount+",errDTL="+errDirTooLongCount+",empty="+skipEmpty +",summary="+skipSummary+",subdir="+ skipSubDir 
				+ ",symlink="+skipSYMLINK + ",symlinkd="+skipSYMLINKD +",junction="+ skipJUNCTION+ ") ... end");
		
		if (isSkipSYMLINKD && skipSYMLINKD+skipJUNCTION>0) {
			System.out.println(" <SYMLINKD>或<JUNCTION> 目录:");
			for (String str : skipSD) {
				System.out.println("    " + str);
			}
		}
		System.out.println();
		
		if (isOutputFile){
			pw.flush(); pw.close();
			pw_dir.flush(); pw_dir.close();
			pw_dir_dt.flush(); pw_dir_dt.close();
		}
		return null;
	}
}
