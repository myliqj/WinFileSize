package com.myliqj.dir.bean;

public class FileInfo {
    // fileDel   : src_id, dir_id, file_time,file_size, file_name, file_ext
	Long src_id;
	Long dir_id;
	String file_time;
	Long file_size;
	String file_name;
	String file_ext;
	
	public FileInfo(Long src_id,Long dir_id,String file_time,Long file_size,String file_name,String file_ext){
		this.src_id = src_id;
		this.dir_id = dir_id; 
		this.file_time = file_time;
		this.file_size = file_size;
		this.file_name = file_name;
		this.file_ext = file_ext;
	}
	
	@Override
	public String toString(){
		return file_name;
	}
//	public String output(){
//		return String.format("", );
//	}
	
	public Long getSrc_id() {
		return src_id;
	}

	public void setSrc_id(Long src_id) {
		this.src_id = src_id;
	}

	public Long getDir_id() {
		return dir_id;
	}

	public void setDir_id(Long dir_id) {
		this.dir_id = dir_id;
	}

	public String getFile_time() {
		return file_time;
	}

	public void setFile_time(String file_time) {
		this.file_time = file_time;
	}

	public Long getFile_size() {
		return file_size;
	}

	public void setFile_size(Long file_size) {
		this.file_size = file_size;
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getFile_ext() {
		return file_ext;
	}

	public void setFile_ext(String file_ext) {
		this.file_ext = file_ext;
	}
	
	 
	
}
