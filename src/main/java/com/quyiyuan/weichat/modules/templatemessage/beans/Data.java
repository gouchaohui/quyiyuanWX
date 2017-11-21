package com.quyiyuan.weichat.modules.templatemessage.beans;
public class Data {
	private BaseValue first;
	private BaseValue keyword1;
	private BaseValue keyword2;
	private BaseValue keyword3;
	private BaseValue keyword4;
	private BaseValue keyword5;
	private BaseValue keyword6;
	private BaseValue remark;
	public Data(){
	}
	public Data(BaseValue first, BaseValue remark){
		this.first = first;
		this.remark = remark;
	}
	public BaseValue getFirst(){
		return first;
	}
	public void setFirst(BaseValue first){
		this.first =first;
	}
	public BaseValue getKeyword1(){
		return keyword1;
	}
	public void setKeyword1(BaseValue keyword1){
		this.keyword1 = keyword1;
	}
	public BaseValue getKeyword2(){
		return keyword2;
	}
	public void setKeyword2(BaseValue keyword2){
		this.keyword2 = keyword2;
	}
	public BaseValue getKeyword3(){
		return keyword3;
	}
	public void setKeyword3(BaseValue keyword3){
		this.keyword3 = keyword3;
	}
	public BaseValue getKeyword4(){
		return keyword4;
	}
	public void setKeyword4(BaseValue keyword4){
		this.keyword4 = keyword4;
	}
	public BaseValue getKeyword5(){
		return keyword5;
	}
	public void setKeyword5(BaseValue keyword5){
		this.keyword5 = keyword5;
	}
	public BaseValue getKeyword6(){
		return keyword6;
	}
	public void setKeyword6(BaseValue keyword6){
		this.keyword6 = keyword6;
	}
	public BaseValue getRemark(){
		return remark;
	}
	public void setRemark(BaseValue remark){
		this.remark = remark;
	}

}
