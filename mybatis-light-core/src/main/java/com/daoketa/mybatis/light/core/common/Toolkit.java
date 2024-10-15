package com.daoketa.mybatis.light.core.common;

import com.daoketa.mybatis.light.core.Column;
import com.daoketa.mybatis.light.core.MybatisLightException;
import com.daoketa.mybatis.light.core.Table;
import com.daoketa.mybatis.light.core.provider.RootSqlNodeProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wangcy 2024/10/12 16:07
 */
@Slf4j
public class Toolkit {

	public static MappedStatement createMappedStatement(Ctx ctx, List<RootSqlNodeProvider> providerList) throws MybatisLightException {
		return new MappedStatement.Builder(ctx.configuration, ctx.qualifiedId, getSqlSource(ctx, providerList), getSqlCommandType(ctx.qualifiedId))
				.resultMaps(Collections.singletonList(getResultMap(ctx)))
				.build();
	}
	
	private static SqlSource getSqlSource(Ctx ctx, List<RootSqlNodeProvider> providerList) throws MybatisLightException {
		return new DynamicSqlSource(ctx.configuration, getRootSqlNode(ctx, providerList));
	}
	
	private static SqlNode getRootSqlNode(Ctx ctx, List<RootSqlNodeProvider> providerList) throws MybatisLightException {
		for(RootSqlNodeProvider provider : providerList) {
			if(provider.supports(ctx.qualifiedId)) {
				return provider.create(ctx);
			}
		}
		throw new MybatisLightException(String.format("none RootSqlNodeProvider found for %s", ctx.qualifiedId));
	}
	
	private static ResultMap getResultMap(Ctx ctx) {
		String resultMapName = ctx.domainClass.getName() + "MybatisLightResultMap_" + System.currentTimeMillis();
		return new ResultMap.Builder(ctx.configuration, resultMapName, ctx.domainClass, getResultMappingList(ctx)).build();
	}
	
	private static List<ResultMapping> getResultMappingList(Ctx ctx) {
		List<ResultMapping> resultMappingList = new ArrayList<>();
		for(Field field : ctx.fields) {
			Column column = field.getAnnotation(Column.class);
			if(column != null) {
				resultMappingList.add(new ResultMapping.Builder(ctx.configuration, field.getName(), getColumnName(column, field), field.getType()).build());
			}
		}
		return resultMappingList;
	}
	
	public static String getColumnName(Column column, Field field) {
		return getOrDefault(column.value(), field.getName());
	}
	
	public static String getDomainProperty(Field field) {
		return field.getName();
	}
	
	public static String getTableName(Class<?> domainClass) {
		Table table = domainClass.getAnnotation(Table.class);
		return table == null || isEmpty(table.value()) ? domainClass.getName().toLowerCase() : table.value();
	}

	private static String getOrDefault(String value, String defaultValue) {
		return isNotEmpty(value) ? value : defaultValue;
	}

	private static String getMethodName(String qualifiedId) throws MybatisLightException {
		String[] arr = qualifiedId.split("\\.");
		try {
			return arr[arr.length - 1];
		} catch (Exception e) {
			throw new MybatisLightException(e);
		}
	}

	public static boolean matches(String qualifiedId, String methodName) throws MybatisLightException {
		return equals(getMethodName(qualifiedId), methodName);
	}

	public static SqlCommandType getSqlCommandType(String qualifiedId) throws MybatisLightException {
		String methodName = getMethodName(qualifiedId);
		for(SqlCommandType type : SqlCommandType.values()) {
			if(methodName.toLowerCase().indexOf(type.name().toLowerCase()) == 0) {
				return type;
			}
		}
		throw new MybatisLightException("cannot find SqlCommandType from " + qualifiedId);
	}

	private static boolean isEmpty(String str) {
		return str == null || "".equals(str);
	}

	private static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	
	private static boolean equals(String a, String b) {
		return isNotEmpty(a) && a.equals(b);
	}
}
