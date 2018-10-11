package com.myliqj.dir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.myliqj.dir.bean.DirDTInfo;
import com.myliqj.dir.bean.DirInfo;
import com.myliqj.dir.bean.FileInfo;

public class GetDirInfo {
	
	
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
	
	public static Map getDirInfo(String path,Long topSize) throws Exception{
		String cmd = "cmd /c dir /-c /t:w /s \"C:\\Users\\Administrator\\AppData\\Roaming\\youku\\config\"";
		cmd = "cmd /c dir /-c /t:w /s D:\\java_run\\Quartz";
		cmd = "cmd /c dir /-c /t:w /s D:\\db2tool";
		cmd = "cmd /c dir /-c /t:w /s \"C:\\Program Files (x86)\"";
		cmd = "cmd /c dir /-c /t:w /s \"C:\\Windows\"";
		if (path==null) return null;
		path = path.replaceAll("/", "\\");
		if (!path.endsWith("\\")) path += "\\";
		cmd = "cmd /c dir /-c /t:w /s \""+path+"\"";
		System.out.println(cmd);
		Process pro = Runtime.getRuntime().exec(cmd);
		
		boolean isDeleteTempFile = true;
		String tempFileName=FileUtils.createTempFile("dir_temp",".txt",null);
		System.out.println("tempFileName=" + tempFileName);
		
		PrintStream ps = new PrintStream( new FileOutputStream(tempFileName,true),true, "GBK" );		
		StreamGobbler errorGobbler = new StreamGobbler(pro.getErrorStream(), "GBK", "ERR", ps);
		StreamGobbler outputGobbler = new StreamGobbler(pro.getInputStream(), "GBK", "OUT", ps);		
		
		//StreamGobbler errorGobbler = new StreamGobbler(pro.getErrorStream(), "GBK", "ERR", System.err);
		//StreamGobbler outputGobbler = new StreamGobbler(pro.getInputStream(), "GBK", "OUT", System.out);
		long start = System.nanoTime(); //System.currentTimeMillis();
		
		errorGobbler.start();
		outputGobbler.start();
		//pro.waitFor();
		int exitVal = pro.waitFor();
		long ys = System.nanoTime() - start;
		ps.close();
		
		File tmpFile = new File(tempFileName);
		System.out.println("使用dir命令获取目录文件信息到临时文件。 ExitValue=" + exitVal
				+ " 用时=" + (ys/1000000)+" ms "
				+ " 文件大小="+StringHelper.readableFileSize(tmpFile.length()) + "\n");
		
		// output
		
		List<DirInfo> dirInfo = new ArrayList<DirInfo>();
		List<DirDTInfo> dirDTInfo = null; // new ArrayList<DirDTInfo>();
		List<FileInfo> fileInfo = null; // new ArrayList<FileInfo>(); 
		List<FileInfo> fileInfo_TopSize = null;
		if (topSize>0) fileInfo_TopSize = new ArrayList<FileInfo>();
		// dirTodel(Long inFileNo,File inFile,String outDir,boolean isOnlyOutputLoad
		//,boolean isOutputFile,int i_SkipRow
		//,List<DirInfo> dirInfo,List<DirDTInfo> dirDTInfo
		//,List<FileInfo> fileInfo)
		
		start = System.nanoTime();
		dirTodel(-1L,tmpFile,null,false,false,0,dirInfo,dirDTInfo,fileInfo,topSize,fileInfo_TopSize);
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
				String curName = dir.getDir_name().substring(dir0_len+1);
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
			
			map = GetDirInfo.getDirInfo(path,topsize);

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
        		//if ("20170924_221712_k470p_750g_04_f_dir_all_file_info.txt".equals(file.getName())){
        		////	dirTodel(inFileNo,file,outDir,false);
        		//}
        		
        		// load from xx of del messages load.msg insert into xx NONRECOVERABLE
        		// output-load
        		//dirTodel(inFileNo,file,outDir,true);
        		
        		
        	}
        }
	}
	public static void dirTodel(Long inFileNo,File inFile,String outDir,boolean isOnlyOutputLoad
			,boolean isOutputFile,int i_SkipRow
			,List<DirInfo> dirInfo,List<DirDTInfo> dirDTInfo
			,List<FileInfo> fileInfo
			,Long topSize                    // 返回指定大小的文件，字节，<=0 表示不返回
			,List<FileInfo> fileInfo_TopSize // 指定大小的文件列表
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
			return ;
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
		boolean timeHastt = dtFormat.indexOf("tt")>0;
		if (timeHastt) dtFormat = dtFormat.replace("tt", " a");
		System.out.println("current datetime format:" + dtFormat);
		java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat(dtFormat);
		// 2010-03-25-00.00.00
		java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		
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
		int skipJUNCTION = 0; int skipSYMLINK = 0;
		int rows = 0; int len =0 ; int FileCount = 0;
		
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
				
				if (p_curDirNo>1) // 从1开始，首次的不要保存，   ---表示整个所有，parent_dir_id=-1，其它时候 parent_dir为不正确的值，要另外经过处理才行
					// src_id, dir_id,p_parent_dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name 
					if (isOutputFile) {
						pw_dir.println(inFileNo + "," + p_curDirNo+ "," + p_parent_dir_id+ "," + p_DirFileCount +"," + p_dirSize + "," + p_DirSubCount + ",\"" + curPath.replace("\"", "\"\"") + "\"");
					}else{
						// bean
						if (dirInfo!=null) dirInfo.add(new DirInfo(inFileNo , p_curDirNo, p_parent_dir_id, p_DirFileCount, p_dirSize , p_DirSubCount , curPath ));
					}
				curPath = line.substring(0, len-3).trim();
				if (curPath.charAt(curPath.length()-1) !='\\'){
					curPath += "\\";
				}
				
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
				if (line.contains("<JUNCTION>")){
					skipJUNCTION ++;
					continue;
				}else if (line.contains("<SYMLINK>")){
					skipSYMLINK ++;
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
						continue;
					} 
					continue;
				}else if (line.startsWith("          ")){
					skipSummary ++;
					continue;
				} else {
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
						+" (err="+errCount+",empty="+skipEmpty +",summary="+skipSummary+",subdir="+ skipSubDir 
						+ ",symlink="+skipSYMLINK +",junction="+ skipJUNCTION+ ") ... ");
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
				+" (err="+errCount+",empty="+skipEmpty +",summary="+skipSummary+",subdir="+ skipSubDir 
				+ ",symlink="+skipSYMLINK +",junction="+ skipJUNCTION+ ") ... end");
		
		if (isOutputFile){
			pw.flush(); pw.close();
			pw_dir.flush(); pw_dir.close();
			pw_dir_dt.flush(); pw_dir_dt.close();
		}
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main2(String[] args) throws Exception {
		// TODO Auto-generated method stub
		 
		String inFile = "20170924_221040_k470p_750g_02_d_dir_all_file_info.txt";
		String filename = "h:\\" + inFile ;
		String inFileNo = "1";
		
		String outfile = "h:/"+ "import_" + inFile;
		String outdir = "h:/"+ "import_dir_" + inFile;
		String outdir_dt = "h:/"+ "import_dir_dt_" + inFile;
		
		
		System.out.println("start ... file=" + inFileNo + "  " + inFile );
		
		
		//Vector<String> f = FileUtils.getFileLines(filename);
		
		FileWriter fw = new FileWriter(outfile,false);
		PrintWriter pw=new PrintWriter(fw); 

		FileWriter fw_dir = new FileWriter(outdir,false);
		PrintWriter pw_dir=new PrintWriter(fw_dir); 
		
		FileWriter fw_dir_dt = new FileWriter(outdir_dt,false);
		PrintWriter pw_dir_dt=new PrintWriter(outdir_dt); 
		
		java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd  HH:mm");
		// 2010-03-25-00.00.00
		java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		 
		String curPath = ""; int curDirNo = 0; String dir_dt_out = "";
		int p_curDirNo = 0; long p_dirSize = 0;  // 上一次序号与大小(从文件汇总)
		int p_parent_dir_id = -1;
		int p_DirFileCount = 0; int p_DirSubCount = 0;
		String dt = ""; String dt_out = ""; 
		String size = ""; String file=""; String ext = "";
		int errCount = 0; int skipEmpty = 0; int skipSummary = 0; int skipSubDir = 0;
		int skipJUNCTION = 0; int skipSYMLINK = 0;
		int rows = 0; int len =0 ; int FileCount = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		while ((line = br.readLine()) != null){ 
				 
		//for (String line :f){
			rows ++;  
			//if (rows>200) break;  // test
			if (rows<6) {
				// 跳过最前面的5行
				skipSummary ++;
				continue;
			}
			
			len = line.length();
			if (line.trim().length()==0) {
				skipEmpty ++;
				continue;
			}
//					if (line.startsWith("out_file=")){
//						skipSammay ++;
//						continue;
//					}
//					if (line.startsWith("start ----")){
//						skipSammay ++;
//						continue;
//					}
			if (line.startsWith("end ----")){
				skipSummary ++;
				continue;
			}
			
			boolean isDir = line.endsWith("的目录");
			if (isDir){
				
				if (p_curDirNo>0)
					pw_dir.println(inFileNo + "," + p_curDirNo+ "," +p_parent_dir_id+ "," + p_DirFileCount +"," + p_dirSize + "," + p_DirSubCount + ",\"" + curPath.replace("\"", "\"\"") + "\"");
	    		    // src_id, dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name 
				curDirNo ++;
				curPath = line.substring(0, len-3).trim();
				if (curPath.charAt(curPath.length()-1) !='\\'){
					curPath += "\\";
				}
				
				
				p_dirSize = 0;
				p_DirFileCount = 0;
				p_DirSubCount = 0;
				p_parent_dir_id = p_curDirNo;
				p_curDirNo = curDirNo;
				//System.out.println("dir: " + curPath);
				//pw_dir.println(inFileNo + "," + curDirNo + ",\"" + curPath.replace("\"", "\"\"") + "\"");
				continue;
			}
			
			//if (curPath.trim().length()==0) continue;
			
			if (len>17){ 
				if (line.contains("<JUNCTION>")){
					skipJUNCTION ++;
					continue;
				}else if (line.contains("<SYMLINK>")){
					skipSYMLINK ++;
					continue;
				}else if (line.contains("<DIR>")){
					// 当前是目录中的目录，不处理
					skipSubDir ++;
					p_DirSubCount ++; 
					try{ 
						dir_dt_out = outputFormat.format(inputFormat.parse(line.substring(0,17)));
						
						int start = line.indexOf("<DIR>") + 5;
						while (start<len && (line.charAt(start) ==' ')) start++;
						file = line.substring(start);
						
						if (!".".equals(file) && !"..".equals(file))
							pw_dir_dt.println(inFileNo + "," + curDirNo +",\"" + dir_dt_out+"\",\"" + file.replace("\"", "\"\"") + "\"");//字符串末尾不需要换行符
							// src_id, parent_dir_id, dir_time, dir_name 
					}catch (Exception e) {
						e.printStackTrace();
						errCount ++;
						continue;
					} 
					continue;
				}else if (line.startsWith("          ")){
					skipSummary ++;
					continue;
				} else {
					dt = line.substring(0,17);
					try{ 
						dt_out = outputFormat.format(inputFormat.parse(dt));
					}catch (Exception e) {
						e.printStackTrace();
						errCount ++;
						continue;
					}
					
					// 找第一个数字
					int i = 17;
					while (i<len && !(line.charAt(i)>='0' && line.charAt(i)<='9')) i++;
					int start=i;
					// 找下一个空格
					while (i<len && !(line.charAt(i) ==' ')) i++;
					
					try{
						if (i>start){
							size = line.substring(start, i);
						}
						file = line.substring(i+1); 
						FileCount ++;
						
						p_DirFileCount ++;
						p_dirSize += Long.valueOf(size.trim());
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
					}
					
					pw.println(inFileNo + "," + curDirNo +",\"" + dt_out + "\"," + size 
							+ ",\"" + file.replace("\"", "\"\"") + "\""
							+ ",\"" + ext.replace("\"", "\"\"") + "\"");//字符串末尾不需要换行符
					// src_id, dir_id, file_time,file_size, file_name, file_ext 
				}
				
			}else{
				skipEmpty ++;
			}
			if (rows % 2000 == 0){
				pw.flush();
				pw_dir.flush();
				System.out.println("Dir:" + curDirNo + " , File:" + FileCount + ",  Read-Rows:" + rows 
						+" (err="+errCount+",empty="+skipEmpty +",summary="+skipSummary+",subdir="+ skipSubDir 
						+ ",symlink="+skipSYMLINK +",junction="+ skipJUNCTION+ ") ... ");
			} 
		}
		br.close();
		if (p_curDirNo>0)  
			pw_dir.println(inFileNo + "," + p_curDirNo+ "," + p_DirFileCount +"," + p_dirSize + "," + p_DirSubCount + ",\"" + curPath.replace("\"", "\"\"") + "\"");
    		// src_id, dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name 

		System.out.println("Dir:" + curDirNo + " , File:" + FileCount + ",  Read-Rows:" + rows 
				+" (err="+errCount+",empty="+skipEmpty +",summary="+skipSummary+",subdir="+ skipSubDir 
				+ ",symlink="+skipSYMLINK +",junction="+ skipJUNCTION+ ") ... end");
		pw.flush();
		pw.close();
		fw.close();
		pw_dir.flush();
		pw_dir.close();
		fw_dir.close();
		pw_dir_dt.flush();
		pw_dir_dt.close();
		fw_dir_dt.close();
		
		
	}

}
