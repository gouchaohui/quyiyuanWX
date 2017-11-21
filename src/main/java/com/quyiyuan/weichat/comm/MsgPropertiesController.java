package com.quyiyuan.weichat.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;


@Controller
public class MsgPropertiesController extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected static Logger logger = Logger.getLogger(MessageUtil.class.getClass());
	public  static Map<String,String> map = null;
	@PostConstruct
	public void init() {
		logger.info("init properties start");
		try {
			map = fileToMap(map);
			logger.info("读取message.propertise文件:"+map);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	 /**
     * 根据java标准properties文件读取信息，并赋值为一个 HashMap<String,String>
     * @param path
     * @param map
     * @return Map
     * @throws Exception
     */
    public final  Map<String,String> fileToMap(Map<String,String> map) throws Exception{
        if(map == null){
            map = new HashMap<String,String>();
        }
        InputStream isr = null;
        Reader r = null;
        try {
        	isr = MsgPropertiesController.class.getClassLoader().getResourceAsStream("message.properties");
            r = new InputStreamReader(isr, "UTF-8");
            Properties props = new Properties();
            props.load(r);

            //得到props为<Object,Object>，再次将props<Object,Object>转为Map<String,String>
            Set<Entry<Object, Object>> entrySet = props.entrySet();
            for (Entry<Object, Object> entry : entrySet) {	
                    map.put(((String) entry.getKey()), ((String) entry.getValue()));
            }
            return map;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                	logger.error(e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (Exception e2) {
                	logger.error(e2);
                }
            }
        }
    }
    
	

	/**
	 * <pre>
	 * 任务： 
	 * 描述：将输入的properties配置文件转为Map 
	 * 作者：屈剑飞
	 * 时间：2015年1月22日下午4:20:34
	 * @param map 将配置文件要存放的map
	 * @param fileName 要读取的配置文件的文件名
	 * @return 将保存了配置文件内容的map返回
	 * returnType：Map
	 * </pre>
	*/
	public static Map initProperties(String fileName,Map map){
		if(map == null){
			map = new HashMap();
		}
		//创建一个字符流输入流用来读取hospitalLevel.properties文件
		InputStream input = null;
		//创建一个Reader的字符输入流
		Reader r = null;
		try {
			input = MsgPropertiesController.class.getClassLoader().getResourceAsStream("/"+fileName);
			r = new InputStreamReader(input,"UTF-8");
			
			Properties prop = new Properties();
			prop.load(r);
			
			map = prop;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//关闭规则，先关闭最外层的流(即父类的流)
			if(r != null){
				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					if(input != null){
						try {
							input.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return map;
	}

}
