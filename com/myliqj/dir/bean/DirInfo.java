package com.myliqj.dir.bean;

public class DirInfo {
	// DirDel    : src_id, dir_id, dir_file_count, dir_file_size, dir_sub_count, dir_name
	Long src_id;
	Long dir_id;
	Long parent_dir_id=-1L;
	Long dir_file_count=0L;
	Long dir_file_size=0L;
	Long dir_sub_count=0L;
	String dir_name = "";
	
	Long dir_sub_all_count=0L;  // xx1  这三项值，等于windows 文件夹属性页中的：      包含  xx2 个文件， xx1 个文件夹， 大小 (xx3 字节)
	Long dir_sub_file_count=0L; // xx2
	Long dir_sub_file_size=0L;  // xx3
	
	int level = 0;
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public DirInfo(Long src_id,Long dir_id,Long parent_dir_id,Long dir_file_count,Long dir_file_size,Long dir_sub_count,String dir_name){
		this.src_id = src_id;
		this.dir_id = dir_id;
		this.parent_dir_id = parent_dir_id;
		this.dir_file_count = dir_file_count;
		this.dir_file_size = dir_file_size;
		this.dir_sub_count = dir_sub_count;
		this.dir_name = dir_name;
	}
	
	public String to1String(){
		return dir_name;
	}
	@Override
	public String toString(){
		return String.format("%d, %d, %d, %d, %d, %d, %d, %s, %d, %d, %d"
				,level, src_id,dir_id,parent_dir_id,dir_file_count,dir_file_size,dir_sub_count,dir_name,dir_sub_all_count,dir_sub_file_count,dir_sub_file_size);
	}

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

	public Long getDir_file_count() {
		return dir_file_count;
	}

	public void setDir_file_count(Long dir_file_count) {
		this.dir_file_count = dir_file_count;
	}

	public Long getDir_file_size() {
		return dir_file_size;
	}

	public void setDir_file_size(Long dir_file_size) {
		this.dir_file_size = dir_file_size;
	}

	public Long getDir_sub_count() {
		return dir_sub_count;
	}

	public void setDir_sub_count(Long dir_sub_count) {
		this.dir_sub_count = dir_sub_count;
	}

	public String getDir_name() {
		return dir_name;
	}

	public void setDir_name(String dir_name) {
		this.dir_name = dir_name;
	}

	public Long getDir_sub_all_count() {
		return dir_sub_all_count;
	}

	public void setDir_sub_all_count(Long dir_sub_all_count) {
		this.dir_sub_all_count = dir_sub_all_count;
	}

	public Long getDir_sub_file_count() {
		return dir_sub_file_count;
	}

	public void setDir_sub_file_count(Long dir_sub_file_count) {
		this.dir_sub_file_count = dir_sub_file_count;
	}

	public Long getDir_sub_file_size() {
		return dir_sub_file_size;
	}

	public void setDir_sub_file_size(Long dir_sub_file_size) {
		this.dir_sub_file_size = dir_sub_file_size;
	}

	public Long getParent_dir_id() {
		return parent_dir_id;
	}

	public void setParent_dir_id(Long parent_dir_id) {
		this.parent_dir_id = parent_dir_id;
	}
	
	 
}
