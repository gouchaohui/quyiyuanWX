	/*
	*
	* 公交线路方法
	*
	*/
	function takeBus_to(){
		var over = map.getOverlays();
		for(var i in over){
			var x = over[i];
			try{
				var title = x.getTitle();
				if((/[\u4e00-\u9fa5]+/).test(title)){   
					map.removeOverlay(over[i]);
				  }
			}catch(e){
				console.log(e);
				map.removeOverlay(over[i]);
			}
			
		}
		//var transit = new BMap.TransitRoute(map, {renderOptions: {map: map}});
		//var transit = new BMap.TransitRoute(map, {renderOptions: {map: map, panel: "r-result",autoViewport:true},onSearchComplete:function(result){

		var transit = new BMap.TransitRoute(map, {renderOptions: {map: map, panel: "r-result"}});
		transit.search(point, point01);
		transit.setSearchCompleteCallback(function(data){
			console.log(transit.getStatus());
		});
		$("#r-result").css("display","block");
		$("#allmap").css("height","50%");
	}
	
	/*
	*
	* 驾车线路方法
	*
	*/
	function driving_to(){
		var over = map.getOverlays();
		for(var i in over){
			var x = over[i];
			try{
				var title = x.getTitle();
				if((/[\u4e00-\u9fa5]+/).test(title)){   
					map.removeOverlay(over[i]);
				  }   
			}catch(e){
				console.log(e);
				map.removeOverlay(over[i]);
			}
			
		}
		//获得路线
		//var driving = new BMap.DrivingRoute(map, {renderOptions:{map: map, autoViewport: true}});
		var driving = new BMap.DrivingRoute(map, {renderOptions: {map: map, panel: "r-result", autoViewport: true}});
		driving.search(point, point01);
		$("#r-result").css("display","block");
		$("#allmap").css("height","50%");
		//
		
	}
	
	/*
	*
	* 步行线路方法
	*
	*/
	function walking_to(){
		//map.clearOverlays(); 
		
		var over = map.getOverlays();
		for(var i in over){
			var x = over[i];
			try{
				var title = x.getTitle();
				if((/[\u4e00-\u9fa5]+/).test(title)){   
					map.removeOverlay(over[i]);
				  }  
			}catch(e){
				console.log(e);
				map.removeOverlay(over[i]);
			}
			
		}
		
		//var walking = new BMap.WalkingRoute(map, {renderOptions:{map: map, autoViewport: true}});
		var walking = new BMap.WalkingRoute(map, {renderOptions: {map: map, panel: "r-result", autoViewport: true}});
		walking.search(point, point01);
		$("#r-result").css("display","block");
		$("#allmap").css("height","50%");
		
	}
	
	function closeResult(){
		$("#allmap").css("height","100%");
		$("#r-result").css("display","none");
	}
	
	/*
	*
	*  添加附近医院的标注
	*
	*/
	
	function addMarker01(point){
		var marker = new BMap.Marker(point);
		
		marker.addEventListener("click", function(e){
			//searchInfoWindow.open(marker);
			//marker.openInfoWindow(infoWindow);
			point01 = point;
			marker.openInfoWindow( new BMap.InfoWindow("<div class='openWindow'>"+
			"<div style='width:220px;'><div style='float: left; width:40px;'><b>名称:</b></div><div style='float: left;width:180px'>"+point.name+"</div></div>"+
			"<div style='width:220px;'><div style='float: left;width:40px;'><b>地址:</b></div><div style='float: left;width:180px;'>"+point.address+"</div></div>"+
			"<div style='float:left;padding:9px 0px 0px 0px; width:61px'><b>到这里去:</b></div>"+
			"<div id='divbtn' class='divbtn' onclick='takeBus_to();'>公交</div>"+
			"<div id='divbtn' class='divbtn' onclick='driving_to();'>驾车</div>"+
			"<div id='divbtn' class='divbtn' onclick='walking_to();'>步行</div>"+
			"<div class='menudiv' style=''>  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>"+
			"<div class='menubtn' onclick='sendDirect(1)' style='background-image:url(\"images/map_1.png\");'>首页</div>"+
			"<div class='menubtn' onclick='sendDirect(1)' style='background-image:url(\"images/map_2.png\");'>预约</div>"+
			"<div class='menubtn' onclick='sendDirect(1)' style='background-image:url(\"images/map_3.png\");'>挂号</div>"+
			"<div class='menubtn' onclick='sendDirect(1)' style='background-image:url(\"images/map_4.png\");'>账单</div>"+
			"</div>",{enableMessage: false}));//去掉右上角的手机图标
			
		})
	  map.addOverlay(marker);
		
	}
	
	
	/*
	*
	*  添加我的位置的标注
	*
	*/
	function addMarker(point, index){   
		  	// 创建图标对象   
		   	var myIcon = new BMap.Icon("images/mylocation.png", new BMap.Size(30, 30), {   
		    	//offset: new BMap.Size(10, 25),                  // 指定定位位置   
		    	//imageOffset: new BMap.Size(0, 0 - index * 25)   // 设置图片偏移   
			});  
		   	myIcon.setImageSize(new BMap.Size(30, 30));
		   	var marker = new BMap.Marker(point, {icon: myIcon});   
		  	map.addOverlay(marker);  
		  	// marker.setAnimation(BMAP_ANIMATION_BOUNCE); //跳动的动画
		   	var label = new BMap.Label("我的位置",{offset:new BMap.Size(-15,-17)});
			marker.setLabel(label);
	} 
	
	
	function getHospitalData(){
		/*
		*
		*获取数据
		*/
		$.ajax({
			type:"POST",
			url:"http://weixin.quyiyuan.com/publicservice/hospital",
			data:"lat="+lat+"&lng="+lgn,
			success:function(data){
				console.log(data);
				var dataJson = eval('(' + data + ')'); 
				hospitals = dataJson.hospitals;
				showHospitals = dataJson.hospitals;
				console.log(hospitals);
				for(var i in hospitals){
					var point02 = new BMap.Point(hospitals[i].LONGITUDE,hospitals[i].LATITUDE);
					//console.log(point02);
					point02.name = hospitals[i].HOSPITAL_NAME;
					point02.address = hospitals[i].MAILING_ADDRESS;
					addMarker01(point02);

			   }
			},
			error:function(data){
				console.log(data);
			}
		});
	}
	
	
	//筛选等级
	
	/*
	*
	*选择医院等级
	*
	*/
	function selectLevel(value,which){
		level = value;
		showHospitals = [];
		if(hospitals){
			//if(nature==-1){
				//if(scope==-1){
					if(value==-1){
						showHospitals = hospitals;
					}else if(value==0){
						for(var i in hospitals){
							if(hospitals[i].HOSPITAL_LEVEL=="三级特等"){
								showHospitals.push(hospitals[i]);
							}
						}
					}else if(value==1){
						for(var i in hospitals){
							if(hospitals[i].HOSPITAL_LEVEL=="三级甲等" || hospitals[i].HOSPITAL_LEVEL=="三甲"){
								showHospitals.push(hospitals[i]);
							}
						}
					}else if(value==2){
						for(var i in hospitals){
							if(hospitals[i].HOSPITAL_LEVEL == "二级甲等" || hospitals[i].HOSPITAL_LEVEL=="二甲"){
								showHospitals.push(hospitals[i]);
							}
						}
					}else if(value ==5){
						for(var i in hospitals){
							if(hospitals[i].HOSPITAL_LEVEL !="三级甲等" && hospitals[i].HOSPITAL_LEVEL !="三甲" && 
									hospitals[i].HOSPITAL_LEVEL !="二级甲等" && hospitals[i].HOSPITAL_LEVEL !="二甲"){
								showHospitals.push(hospitals[i]);
							}
						}
					}
				//}else{
					
				//}
			//}
					
			
		}
		if(which==1){
			showLevelOnMap(value);
		}else{
			showList();
			$("#dropdown01").find("ul").css("display","none");
			toggle01Show = true;
		}
		
	}
//将筛选结果显示到地图上
function showLevelOnMap(value){

	
	//清除现在地图上的maker
	map.clearOverlays();
	//将我的位置显示出来
	addMarker(point,12);
	//将筛选结果显示出来
	for(var i in showHospitals){
		var point02 = new BMap.Point(showHospitals[i].LONGITUDE,showHospitals[i].LATITUDE);
		point02.name = showHospitals[i].HOSPITAL_NAME;
		point02.address = showHospitals[i].MAILING_ADDRESS;
		addMarker01(point02);
    }
	
	
	var showText = "医院等级";
	switch(value){
		case -1:
			showText = "全&nbsp;部";
		break;
		case 0:
			showText = "三级特等";
		break;
		case 1:
			showText = "三级甲等";
		break;
		case 2:
			showText = "二级甲等";
		break;
		case 3:
			showText = "一级甲等";
		break;
		case 4:
			showText = "三级乙等";
		break;
		default:
			showText = "其&nbsp;他";
		break;
	}
	
	$("#levelname").html(showText);
}
	
/*
*
*选择医院性质
*
*/
function selectNature(value,which){
	nature = value;
	if(which==1){
		showNatureOnMap(value)
	}else{
		showList();
		$("#dropdown02").find("ul").css("display","none");
		toggle02Show = true;
	}
	
	
	
}

function showNatureOnMap(value){
	var showText = "医院性质";
	switch(value){
		case -1:
			showText = "全&nbsp;部";
		break;
		case 0:
			showText = "公立医院";
		break;
		case 1:
			showText = "私立医院";
		break;
		default:
			showText = "其&nbsp;他";
		break;
	}
	
	$("#natureName").html(showText);
}

/*
*
*选择诊疗范围
*
*/
function selectScope(value,which){
	scope = value;
	if(which==1){
		showScopeOnMap(value)
	}else{
		showList();
		$("#dropdown01").find("ul").css("display","none");
		$("#dropdown02").find("ul").css("display","none");
		$("#dropdown03").find("ul").css("display","none");
		toggle01Show = true;
		toggle02Show = true;
		toggle03Show = true;
	}
	
}

function showScopeOnMap(value){
	var showText = "诊疗范围";
	switch(value){
		case -1:
			showText = "全&nbsp;部";
		break;
		case 0:
			showText = "综合医院";
		break;
		case 1:
			showText = "专科医院";
		break;
		default:
			showText = "其&nbsp;他";
		break;
	}
	
	$("#scopeName").html(showText);
}


function sendDirect(to){
	if(to==1){
		location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx09cbf2186670fddc&redirect_uri=http%3a%2f%2fopen.quyiyuan.com%2fpublicservice%2flogin&response_type=code&scope=snsapi_base&state=index#wechat_redirect";
	}
}



function levelSelect(value){
	$("#toggle01").attr("checked",false);
	
	//document.getElementById("toggle01").checked =  false;
	
}

function natureSelect(){
	$("#toggle02").attr("checked",false);
	
}

function scopeSelect(){
	$("#toggle03").attr("checked",false);
	
}

function showList(){
	var listdata="";
	
	for(var i in showHospitals){
		var starCount = "";
		if(showHospitals[i].HOSPITAL_GRADE == 10 ){
			starCount = "<div style='color:#FF9435;line-height:23px;font-size:15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"&nbsp;&nbsp;&nbsp;10分</div>";
		}else if(showHospitals[i].HOSPITAL_GRADE == 8 ){
			starCount = "<div style='color:#FF9435;line-height:23px;font-size:15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"&nbsp;&nbsp;&nbsp;8.0分</div>";
		}else if(showHospitals[i].HOSPITAL_GRADE == 6 ){
			starCount = "<div style='color:#FF9435;line-height:23px;font-size:15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"&nbsp;&nbsp;&nbsp;6.0分</div>";
		}else if(showHospitals[i].HOSPITAL_GRADE == 4 ){
			starCount = "<div style='color:#FF9435;line-height:23px;font-size:15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"&nbsp;&nbsp;&nbsp;4.0分</div>";
		}else if(showHospitals[i].HOSPITAL_GRADE == 2 ){
			starCount = "<div style='color:#FF9435;line-height:23px;font-size:15px'>"+
			"<img src='images/star_1.png' width='15px' height='15px'>"+
			"&nbsp;&nbsp;&nbsp;6.0分</div>";
		}else if(showHospitals[i].HOSPITAL_GRADE == 0 ){
			starCount = "<div style='color:#FF9435;line-height:23px;font-size:15px'>"+
			"&nbsp;&nbsp;&nbsp;6.0分</div>";
		}
		
		commentCount = showHospitals[i].HOSPITAL_COMMENT_COUNT;
		if(commentCount==0){
			commentCount ="暂无评论";
		}else{
			commentCount += "条"; 
		}
		
		hospname = showHospitals[i].HOSPITAL_NAME;
		if(hospname.length>10){
			hospname = hospname.substring(0,10)+"...";
		}
		listdata+="<div class='mui-table-view-cell' name='hosdiv' onclick='sendDirect(1);'>"+
					"	<div class='imgdiv' ><img src='images/"+showHospitals[i].HOSPITAL_IMG+"'class='imgs'></div>"+
					"	<div class='hosname'>"+
					"		<div><b>"+hospname+"</b></div>"+
					"		<div style='color:#BD4543'><b>"+showHospitals[i].HOSPITAL_LEVEL+"</b></div>"+starCount+
					
					"		<div style='line-height:20px;color:gray'>评论数："+commentCount+"</div>"+
					"	</div>"+
					"</div>";
		
	}
	$("#list01").html(listdata);
	
	
}
	