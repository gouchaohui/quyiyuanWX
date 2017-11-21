package com.quyiyuan.weichat.modules.openthird.dao;

import com.kyee.nextframework.core.base.dao.internal.IJdbcBaseDao;
import com.quyiyuan.weichat.modules.openthird.beans.AuthInfoPO;
import com.quyiyuan.weichat.modules.templatemessage.domain.TemplateId;

import java.util.List;
import java.util.Map;

public interface IOpenThirdDao extends IJdbcBaseDao<TemplateId,String>  {

    void updateComponentTokenOrTicket(String keyComponent, String componentValue);

    Map getComponentTokenFromTable(String keyComponentVerifyTicket);

    int getAuthorizerApp(String authorizer_appid);

    void updateAuthorizerRefreshToken(AuthInfoPO authInfoPO);

    void insertAuthorizerRefreshToken(AuthInfoPO authInfoPO);

    Map getAuthorizerAccessTokenInfo(String authorizerAppId);

    List<Map<String, Object>> getAuthorizerAccessTokenInfoAll();
}
