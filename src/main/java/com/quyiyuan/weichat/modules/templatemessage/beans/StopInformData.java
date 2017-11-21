package com.quyiyuan.weichat.modules.templatemessage.beans;

public class StopInformData extends Data {
	private BaseValue hospitalname;
	private BaseValue deptname;
	private BaseValue doctorname;
	private BaseValue planstarttime;
	
	public BaseValue getHospitalname() {
		return hospitalname;
	}
	public void setHospitalname(BaseValue hospitalname) {
		this.hospitalname = hospitalname;
	}
	public BaseValue getDeptname() {
		return deptname;
	}
	public void setDeptname(BaseValue deptname) {
		this.deptname = deptname;
	}
	public BaseValue getDoctorname() {
		return doctorname;
	}
	public void setDoctorname(BaseValue doctorname) {
		this.doctorname = doctorname;
	}
	public BaseValue getPlanstarttime() {
		return planstarttime;
	}
	public void setPlanstarttime(BaseValue planstarttime) {
		this.planstarttime = planstarttime;
	}
}
