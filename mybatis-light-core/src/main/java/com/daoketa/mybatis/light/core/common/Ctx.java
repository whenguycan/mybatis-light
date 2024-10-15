package com.daoketa.mybatis.light.core.common;

import com.daoketa.mybatis.light.core.MybatisLightException;
import com.daoketa.mybatis.light.core.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;

/**
 * @author wangcy 2024/10/14 13:30
 */
@Getter
public class Ctx {

	@AllArgsConstructor
	public static class Builder {
		Configuration configuration;
		String qualifiedId;
		Class<?> domainClass;
		public Ctx build() throws MybatisLightException {
			Ctx ctx = new Ctx();
			ctx.configuration = configuration;
			ctx.qualifiedId = qualifiedId;
			ctx.domainClass = domainClass;
			ctx.sqlCommandType = Toolkit.getSqlCommandType(qualifiedId);
			ctx.tableName = Toolkit.getTableName(domainClass);
			ctx.fields = domainClass.getDeclaredFields();
			return ctx;
		}
	}
	
	Configuration configuration;
	String qualifiedId;
	Class<?> domainClass;
	
	SqlCommandType sqlCommandType;
	String tableName;
	Field[] fields;
	
}
