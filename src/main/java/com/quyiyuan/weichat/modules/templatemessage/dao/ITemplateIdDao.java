package com.quyiyuan.weichat.modules.templatemessage.dao;

import com.kyee.nextframework.core.base.dao.internal.IJdbcBaseDao;
import com.quyiyuan.weichat.modules.templatemessage.domain.TemplateId;

public interface ITemplateIdDao extends IJdbcBaseDao<TemplateId,String>  {
	TemplateId findTemplateIdByName(String templateName);

}
