Ext.application({
    name: 'Demo',
    viewport: {
        autoMaximize: true // 该属性可以设置页面自动最大化（隐藏地址栏）
    },
    requires:['Ext.MessageBox'],
    
    launch: function () {
    	
    	/*
    	var ddd =  Ext.create('Ext.Panel',{
    		//html:'this is my panel'
    		layout:'hbox',
    		items:[
	    		   {
		    			xtype:'panel',
		    			flex:1,
		    			html:'left hello1',
		    			style:'background-color: #5E99CC;'
	    		   },
	    		   {
	    			   xtype:'panel',
		    			flex:2,
		    			html:'left hello1',
		    			style:'background-color: #759E60;'
	    		   }
	    	]
    	});
    	*/
    	/*
    	var ddd =  Ext.create('Ext.form.Text',{
    		lable:'name',
    		listeners:{
    			change:function(field,newValue,oldValue){
    				console.log(field);
    				console.log(newValue);
    				console.log(oldValue);
    			}
    		}
    		
    	});
    	
    	console.log(ddd);
    	*/
    	/*
    	 Ext.create("Ext.TabPanel", {
             fullscreen: true,
             items: [
                 {
                     title: 'Home',
                     iconCls: 'home',
                     cls:'home',
                     html: [
                            '<img src="http://staging.sencha.com/img/sencha.png" />',
                            '<h1>欢迎使用sencha touch</h1>',
                            "<p>你好",
                            "to use tabs, lists and forms to create a simple app</p>",
                            '<h4>这是啥</h4>'
                        ].join("")
                 }
             ]
         });
         */
    	/*
    	 Ext.create("Ext.TabPanel", {
             fullscreen: true,
             tabBarPosition: 'bottom',

             items: [
                 {
                     title: 'Home',
                     iconCls: 'home',
                     cls: 'home',
                     html: [
                         '<img width="65%" src="http://staging.sencha.com/img/sencha.png" />',
                         '<h1>Welcome to Sencha Touch</h1>',
                         "<p>You're creating the Getting Started app. This demonstrates how ",
                         "to use tabs, lists and forms to create a simple app</p>",
                         '<h2>Sencha Touch (2.0.0pr1)</h2>'
                     ].join("")
                 },
                 {
                     xtype: 'list',
                     title: 'Blog',
                     iconCls: 'star',

                     itemTpl: '{title}',
                     store: {
                         fields: ['title', 'url'],
                         data: [
                             {title: 'Ext Scheduler 2.0', url: 'ext-scheduler-2-0-upgrading-to-ext-js-4'},
                             {title: 'Previewing Sencha Touch 2', url: 'sencha-touch-2-what-to-expect'},
                             {title: 'Sencha Con 2011', url: 'senchacon-2011-now-packed-with-more-goodness'},
                             {title: 'Documentation in Ext JS 4', url: 'new-ext-js-4-documentation-center'}
                         ]
                     }
                 }
             ]
         }).setActiveItem(0);
    	*/
    	
    	Ext.create("Ext.TabPanel", {
            fullscreen: true,
            tabBarPosition: 'bottom',

            items: [
                {
                    title: 'Home',
                    iconCls: 'home',
                    cls: 'home',
                    html: [
                        '<img width="65%" src="http://staging.sencha.com/img/sencha.png" />',
                        '<h1>Welcome to Sencha Touch</h1>',
                        "<p>You're creating the Getting Started app. This demonstrates how ",
                        "to use tabs, lists and forms to create a simple app</p>",
                        '<h2>Sencha Touch (2.0.0pr1)</h2>'
                    ].join("")
                },
                {
                    xtype: 'list',
                    title: 'Blog',
                    iconCls: 'star',

                    itemTpl: '{title}',
                    store: {
                        fields: ['title', 'url'],
                        data: [
                            {title: 'Ext Scheduler 2.0', url: 'ext-scheduler-2-0-upgrading-to-ext-js-4'},
                            {title: 'Previewing Sencha Touch 2', url: 'sencha-touch-2-what-to-expect'},
                            {title: 'Sencha Con 2011', url: 'senchacon-2011-now-packed-with-more-goodness'},
                            {title: 'Documentation in Ext JS 4', url: 'new-ext-js-4-documentation-center'}
                        ]
                    }
                },
                //this is the new item
                {
                    title: 'Contact',
                    iconCls: 'user',
                    xtype: 'formpanel',
                    url: 'contact.php',
                    layout: 'vbox',

                    items: [
                        {
                            xtype: 'fieldset',
                            title: 'Contact Us',
                            instructions: '(email address is optional)',
                            items: [
                                {
                                    xtype: 'textfield',
                                    label: 'Name'
                                },
                                {
                                    xtype: 'emailfield',
                                    label: 'Email'
                                },
                                {
                                    xtype: 'textareafield',
                                    label: 'Message'
                                }
                            ]
                        },
                        {
                            xtype: 'button',
                            text: 'Send',
                            ui: 'confirm',
                            handler: function() {
                                this.up('formpanel').submit();
                            }
                        }
                    ]
                }
            ]
        }).setActiveItem(2);
    
    	
    	//Ext.Viewport.add(ddd);
    	//console.log("11");
    	/*
    	
        Ext.create("Ext.Panel", {
            fullscreen: true,
            items: [
                {
                    xtype: 'fieldset',
                    margin: 10,
                    title: '文本框与Picker结合实例',
                    items: [
                        {
                            xtype: 'textfield',
                            name: 'aTextField',
                            id: 'aTextField',
                            readOnly: true,         // 把文本框设为只读，禁止输入
                            label: '取值结果',
                            clearIcon: true,
                            listeners: {
                                // 侦听文本框的focus事件，获取到焦点时触发
                                focus: function () {
                                    this.disable();                 // 先禁用文本框，防止系统调出软键盘
                                    Ext.getCmp('aPicker').show();   // 然后显示用来选择内容的Picker
                                }
                            }
                        }
                    ]
                }
            ]
        });

        // 定义一个Picker供文本框联动
        aPicker = Ext.create('Ext.Picker', {
            name: 'aPicker',
            id: 'aPicker',
            hidden: true,
            listeners: {
                // 侦听change事件，Picker的值改变同时也设定文本框的值
                change: function () {
                    Ext.getCmp('aTextField').setValue(aPicker.getValue().question);
                },
                // 侦听hide事件，当Picker消失时将文本框状态恢复为enable
                hide: function () {
                    Ext.getCmp('aTextField').enable();
                }
            },
            slots: [
                {
                    name: 'question',
                    data: [
                        {
                            text: '无',
                            value: ''
                        },
                        {
                            text: '最喜欢的颜色',
                            value: 'color'
                        },
                        {
                            text: '最喜欢的运动',
                            value: 'sport'
                        },
                        {
                            text: '最喜欢的明星',
                            value: 'star'
                        }
                    ]
                }
            ]
        });
*/
        // 前面定义的Picker控件必须显式加入Viewport，否则无法被调用显示
        // Ext.Viewport是Sencha Touch自动创建的一个顶级容器
    	
    	
    	
        //Ext.Viewport.add(aPicker);
    }
});