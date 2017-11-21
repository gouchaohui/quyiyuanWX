package com.quyiyuan.weichat.modules.openthird.dao.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kyee.nextframework.core.base.dao.helper.NamedParamsBuilder;
import com.kyee.nextframework.core.base.dao.internal.impl.JdbcBaseDao;
import com.quyiyuan.weichat.modules.openthird.beans.AuthInfoPO;
import com.quyiyuan.weichat.modules.openthird.dao.IOpenThirdDao;
import com.quyiyuan.weichat.modules.templatemessage.domain.TemplateId;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("openThirdDao")
public class OpenThirdDao extends JdbcBaseDao<TemplateId, String> implements IOpenThirdDao {

    @Override
    public void updateComponentTokenOrTicket(String keyComponent,String componentValue) {
        NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
        paramBuilder.put("TYPE",keyComponent);
        paramBuilder.put("VALUE",componentValue);
        String sqlStr =
                "UPDATE weixin_thirdparty_info SET VALUE=:VALUE,UPDATE_TIME=NOW() WHERE TYPE=:TYPE";
        updateByNamedParamsSql(
                sqlStr.toString(), paramBuilder);
    }

    @Override
    public Map getComponentTokenFromTable(String keyComponentVerifyTicket) {
        NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
        paramBuilder.put("TYPE",keyComponentVerifyTicket);
        String sqlStr = " SELECT VALUE AS :TYPE, " +
                " CASE WHEN VALUE <> '-1' THEN " +
                "  (CASE WHEN (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(update_time)) > 7000 THEN 1 ELSE 0 END) " +
                " ELSE 1  END IsInvalid FROM weixin_thirdparty_info " +
                " WHERE TYPE = :TYPE ";
        Map<String,Object> data=findOneByNamedParamsSqlForEntryValue(sqlStr.toString(),paramBuilder);
        return data;
    }

    @Override
    public int getAuthorizerApp(String authorizer_appid) {
        NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
        paramBuilder.put("AUTHORIZER_APPID",authorizer_appid);
        String sqlStr = "SELECT COUNT(*) count FROM weixin_thirdparty_accesstoken_info WHERE authorizer_appid=:AUTHORIZER_APPID";
        Map<String,Object> data=findOneByNamedParamsSqlForEntryValue(sqlStr.toString(),paramBuilder);
        return Integer.parseInt(data.get("count")+"");
    }

    @Override
    public void updateAuthorizerRefreshToken(AuthInfoPO authInfoPO) {
        NamedParamsBuilder paramBuilder=castAuthInfoPO2NamedParamsBuilder(authInfoPO);
        StringBuffer sql = new StringBuffer();
        sql.append(" UPDATE weixin_thirdparty_accesstoken_info             ");
        sql.append(" SET authorizer_access_token=:AUTHORIZER_ACCESS_TOKEN, ");
        sql.append(" authorizer_access_token_updateTime=NOW(),             ");
        sql.append(" authorizer_refresh_token=:AUTHORIZER_REFRESH_TOKEN,   ");
        sql.append(" authorizer_refresh_token_updateTime=NOW()             ");
        if(null!=authInfoPO.getFunc_info())
            sql.append(" ,func_info=:FUNC_INFO                             ");
        sql.append(" WHERE authorizer_appid=:AUTHORIZER_APPID              ");
        updateByNamedParamsSql(sql.toString(), paramBuilder);
    }

    @Override
    public void insertAuthorizerRefreshToken(AuthInfoPO authInfoPO) {
        NamedParamsBuilder paramBuilder =castAuthInfoPO2NamedParamsBuilder(authInfoPO);
        StringBuffer sql = new StringBuffer();
        sql.append(" INSERT INTO weixin_thirdparty_accesstoken_info (   ");
        sql.append(" authorizer_appid,                                  ");
        sql.append(" authorizer_access_token,                           ");
        sql.append(" authorizer_access_token_updateTime,                ");
        sql.append(" authorizer_refresh_token,                          ");
        sql.append(" authorizer_refresh_token_updateTime,               ");
        sql.append(" func_info)                                         ");
        sql.append(" VALUES                                             ");
        sql.append(" (:AUTHORIZER_APPID,                                ");
        sql.append(" :AUTHORIZER_ACCESS_TOKEN,                          ");
        sql.append(" NOW(),                                             ");
        sql.append(" :AUTHORIZER_REFRESH_TOKEN,                         ");
        sql.append(" NOW(),                                             ");
        sql.append(" :FUNC_INFO )                                       ");
        saveByNamedParamsSqlAndGetKey(sql.toString(),"id",paramBuilder);

    }
    public NamedParamsBuilder castAuthInfoPO2NamedParamsBuilder(AuthInfoPO authInfoPO){
        NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
        paramBuilder.put("AUTHORIZER_APPID",authInfoPO.getAuthorizer_appid());
        paramBuilder.put("AUTHORIZER_ACCESS_TOKEN",authInfoPO.getAuthorizer_access_token());
        paramBuilder.put("EXPIRES_IN",authInfoPO.getExpires_in());
        paramBuilder.put("AUTHORIZER_REFRESH_TOKEN",authInfoPO.getAuthorizer_refresh_token());
        if(null!=authInfoPO.getFunc_info())
            paramBuilder.put("FUNC_INFO", JSONArray.fromObject(authInfoPO.getFunc_info()).toString() );
        return paramBuilder;
    }

    @Override
    public Map getAuthorizerAccessTokenInfo(String authorizerAppId) {
        NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
        paramBuilder.put("AUTHORIZER_APPID",authorizerAppId);
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT authorizer_appid,                 ");
        sql.append(" authorizer_access_token,                 ");
        sql.append(" authorizer_refresh_token,                ");
        sql.append(" (CASE WHEN (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(authorizer_access_token_updateTime)) > 7000 THEN 1 ELSE 0 END) IsInvalid ");
        sql.append(" FROM weixin_thirdparty_accesstoken_info  ");
        sql.append(" WHERE authorizer_appid=:AUTHORIZER_APPID ");
        sql.append(" AND flag=1                               ");
        Map<String,Object> data=findOneByNamedParamsSqlForEntryValue(sql.toString(),paramBuilder);
        return data;
    }

    @Override
    public List<Map<String,Object>>getAuthorizerAccessTokenInfoAll() {
        NamedParamsBuilder paramBuilder = NamedParamsBuilder.getInstance();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT authorizer_appid,                 ");
        sql.append(" authorizer_access_token,                 ");
        sql.append(" authorizer_refresh_token,                ");
        sql.append(" (CASE WHEN (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(authorizer_access_token_updateTime)) > 7000 THEN 1 ELSE 0 END) IsInvalid ");
        sql.append(" FROM weixin_thirdparty_accesstoken_info  ");
        List<Map<String,Object>> data=findByNamedParamsSqlForMapValue(sql.toString(),paramBuilder);
        return data;
    }
}
