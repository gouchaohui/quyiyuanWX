package com.quyiyuan.weichat.modules.templatemessage.dao.impl;

import org.springframework.stereotype.Repository;

import com.kyee.nextframework.core.base.dao.helper.NamedParamsBuilder;
import com.kyee.nextframework.core.base.dao.internal.impl.JdbcBaseDao;
import com.quyiyuan.weichat.modules.templatemessage.dao.ITemplateIdDao;
import com.quyiyuan.weichat.modules.templatemessage.domain.TemplateId;

@Repository("templateIdDao")
public class TemplateIdDao extends JdbcBaseDao<TemplateId, String> implements ITemplateIdDao{

	@Override
	public TemplateId findTemplateIdByName(String templateName) {
		NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
		paramBuilder.put("TEMPLATE_NAME",templateName);
		String sqlStr = "SELECT TEMPLATE_ID FROM weixin_templateId where TEMPLATE_NAME=:TEMPLATE_NAME";	
		TemplateId templateId = findOneByNamedParamsSqlForObjectValue(
				sqlStr.toString(), paramBuilder, TemplateId.class);
		return templateId;
	}

}
