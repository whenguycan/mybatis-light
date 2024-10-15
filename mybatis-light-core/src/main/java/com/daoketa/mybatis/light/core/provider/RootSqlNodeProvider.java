package com.daoketa.mybatis.light.core.provider;

import com.daoketa.mybatis.light.core.MybatisLightException;
import com.daoketa.mybatis.light.core.common.Ctx;
import org.apache.ibatis.scripting.xmltags.SqlNode;

/**
 * @author wangcy 2024/10/14 14:24
 */
public interface RootSqlNodeProvider {
	
	String METHOD_NAME_SELECT_ONE = "selectOne";
	String METHOD_NAME_SELECT_LIST = "selectList";
	String METHOD_NAME_INSERT = "insert";
	String METHOD_NAME_UPDATE = "update";
	String METHOD_NAME_DELETE = "delete";

	default boolean supports(String qualifiedId) throws MybatisLightException {
		return false;
	}
	
	SqlNode create(Ctx ctx);
	
}
