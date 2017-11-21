Ext.application({
    name: 'QueInfo',
    useLoadMask: true,
    viewport: {
        autoMaximize: true // 该属性可以设置页面自动最大化（隐藏地址栏）
    },

    launch: function () {
    		var queInfo = Ext.create('Ext.Panel',{
	    		//html:'this is my panel'
	    		layout:'vbox',
	    		items:[
		    		   {
			    			xtype:'panel',
			    			//flex:1,
			    			html:'问题1：  点击”附近医院”后，当前地理位置信息显示不正确？',
			    			//style:'background-color: #5E99CC;'
		    		   },
		    		   {
		    			   xtype:'panel',
			    			//flex:2,
			    			html:[' 回答：<br/> 1.Android:确定开启手机GPS，并确定微信有获取GPS的权限。查看该服务号信息，确定该服务号的提供位置信息设置为打开。<br/>',
			    				  '2.IOS:设置-->微信-->位置  确定微信允许访问位置信息。查看该服务号信息，确定该服务号的提供位置信息设置为打开。'].join(""),
			    			//style:'background-color: #759E60;'
		    		   }
		    	]
    		})
    		
    		Ext.Viewport.add(queInfo);
    }
});
    		