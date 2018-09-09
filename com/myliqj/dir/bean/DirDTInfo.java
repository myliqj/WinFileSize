package com.myliqj.dir.bean;

public class DirDTInfo {
  // DriDtDel  : src_id, parent_dir_id, dir_time, dir_name
	Long src_id;
	Long parent_dir_id;
	String dir_time;
	String dir_name;
	
	public DirDTInfo(Long src_id,Long parent_dir_id,String dir_time,String dir_name){
		this.src_id = src_id;
		this.parent_dir_id = parent_dir_id; 
		this.dir_time = dir_time; 
		this.dir_name = dir_name;
	}
	@Override
	public String toString(){
		return dir_time + " " + dir_name;
	}

	public Long getSrc_id() {
		return src_id;
	}

	public void setSrc_id(Long src_id) {
		this.src_id = src_id;
	}

	public Long getParent_dir_id() {
		return parent_dir_id;
	}

	public void setParent_dir_id(Long parent_dir_id) {
		this.parent_dir_id = parent_dir_id;
	}

	public String getDir_time() {
		return dir_time;
	}

	public void setDir_time(String dir_time) {
		this.dir_time = dir_time;
	}

	public String getDir_name() {
		return dir_name;
	}

	public void setDir_name(String dir_name) {
		this.dir_name = dir_name;
	}
	
	 
}
