package com.quyiyuan.weichat.modules.templatemessage.beans;

public class AppointSuccessData extends Data {
	private BaseValue hospitalname;
	private BaseValue deptname;
	private BaseValue doctorname;
	private BaseValue planstarttime;
	
	public AppointSuccessData() {
	}
	public BaseValue getHospitalName(){
		return hospitalname;
	}
	public void setHospitalName(BaseValue hospitalname){
		this.hospitalname = hospitalname;
	}
	public BaseValue getDeptName(){
		return deptname;
	}
	public void setDeptName(BaseValue deptname){
		this.deptname = deptname;
	}
	public BaseValue getDoctorName(){
		return doctorname;
	}
	public void setDoctorName(BaseValue doctorname){
		this.doctorname = doctorname;
	}
	public BaseValue getPlanStartTime(){
		return planstarttime;
	}
	public void setPlanStartTime(BaseValue planstarttime){
		this.planstarttime =planstarttime;
	}

}
