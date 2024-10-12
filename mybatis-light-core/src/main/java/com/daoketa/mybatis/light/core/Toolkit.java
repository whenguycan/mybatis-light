package com.daoketa.mybatis.light.core;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author wangcy 2024/10/12 16:07
 */
public class Toolkit {

	public static MappedStatement createMappedStatement(Configuration configuration, String qualifiedId, Class<?> domainClass) throws MybatisLightException {
		return new MappedStatement.Builder(configuration, qualifiedId, getSqlSource(configuration, qualifiedId, domainClass), getSqlCommandType(qualifiedId))
				.resultMaps(Collections.singletonList(getResultMap(configuration, domainClass)))
				.build();
	}
	
	private static SqlSource getSqlSource(Configuration configuration, String qualifiedId, Class<?> domainClass) throws MybatisLightException {
		return new DynamicSqlSource(configuration, getRootSqlNode(configuration, qualifiedId, domainClass));
	}
	
	private static SqlNode getRootSqlNode(Configuration configuration, String qualifiedId, Class<?> domainClass) throws MybatisLightException {
		Table table = domainClass.getAnnotation(Table.class);
		SqlCommandType sqlCommandType = getSqlCommandType(qualifiedId);
		List<SqlNode> sqlNodeList = new ArrayList<>();
		if(sqlCommandType == SqlCommandType.SELECT) {
			/* select a, b from table where a = 1 and b = 2 */
			sqlNodeList.add(new TextSqlNode(" SELECT"));
			sqlNodeList.add(getColumnsSqlNode(configuration, domainClass));
			sqlNodeList.add(new TextSqlNode(" FROM " + table.value()));
			sqlNodeList.add(getConditionSqlNode(configuration, qualifiedId, domainClass));
		}else if(sqlCommandType == SqlCommandType.INSERT) {
			/* insert into table (a, b) values (1, 3) */
		}else if(sqlCommandType == SqlCommandType.UPDATE) {
			/* update table set a = 1, b = 2 where a = 1 and b = 2 */
		}else if(sqlCommandType == SqlCommandType.DELETE) {
			/* delete from table where a in (1, 2) */
		}
		return new MixedSqlNode(sqlNodeList);
	}
	
	private static SqlNode getColumnsSqlNode(Configuration configuration, Class<?> domainClass) {
		List<SqlNode> sqlNodeList = new ArrayList<>();
		for(Field field : domainClass.getDeclaredFields()) {
			Column column = field.getAnnotation(Column.class);
			if(column != null) {
				sqlNodeList.add(new TextSqlNode(String.format(" %s,", getOrDefault(column.value(), field.getName()))));
			}
		}
		return new TrimSqlNode(configuration, new MixedSqlNode(sqlNodeList), null, null, null, ",");
	}
	
	private static SqlNode getConditionSqlNode(Configuration configuration, String qualifiedId, Class<?> domainClass) throws MybatisLightException {
		List<SqlNode> sqlNodeList = new ArrayList<>();
		String[] arr = qualifiedId.split("\\.");
		if("selectOne".equals(arr[arr.length - 1])) {
			for(Field field : domainClass.getDeclaredFields()) {
				Id id = field.getAnnotation(Id.class);
				if(id != null) {
					Column column = field.getAnnotation(Column.class);
					if(column != null) {
						sqlNodeList.add(new TextSqlNode(String.format(" %s = #{id}", getOrDefault(column.value(), field.getName()))));
					}
				}
			}
			if(sqlNodeList.isEmpty()) {
				throw new MybatisLightException("ID not found or ID is not Column");
			}
		}else if("selectList".equals(arr[arr.length - 1])) {
			for(Field field : domainClass.getDeclaredFields()) {
				Column column = field.getAnnotation(Column.class);
				if(column != null) {
					Cnd cnd = column.cnd();
					String template = " AND %s = #{%s}"; 
					if(cnd == Cnd.like) {
						template = " AND %s LIKE CONCAT('%%', #{%s}, '%%')";	
					}
					sqlNodeList.add(new IfSqlNode(new TextSqlNode(String.format(template, getOrDefault(column.value(), field.getName()), field.getName())), field.getName() + " != null"));
				}
			}
		}else if("delete".equals(arr[arr.length - 1])) {
			for(Field field : domainClass.getDeclaredFields()) {
				Id id = field.getAnnotation(Id.class);
				if(id != null) {
					Column column = field.getAnnotation(Column.class);
					if(column != null) {
						sqlNodeList.add(new MixedSqlNode(Arrays.asList(
								new TextSqlNode(" %s IN "),
								new ForEachSqlNode(configuration, new TextSqlNode("#{id}"), "ids", "i", "id", "(", ")", ",")
						)));
					}
				}
			}
			if(sqlNodeList.isEmpty()) {
				throw new MybatisLightException("ID not found or ID is not Column");
			}
		}
		return new WhereSqlNode(configuration, new MixedSqlNode(sqlNodeList));
	}
	
	private static ResultMap getResultMap(Configuration configuration, Class<?> domainClass) {
		String resultMapName = domainClass.getName() + "MybatisLightResultMap_" + System.currentTimeMillis();
		return new ResultMap.Builder(configuration, resultMapName, domainClass, getResultMappingList(configuration, domainClass)).build();
	}
	
	private static List<ResultMapping> getResultMappingList(Configuration configuration, Class<?> domainClass) {
		List<ResultMapping> resultMappingList = new ArrayList<>();
		for(Field field : domainClass.getDeclaredFields()) {
			Column column = field.getAnnotation(Column.class);
			if(column != null) {
				resultMappingList.add(new ResultMapping.Builder(configuration, field.getName(), getOrDefault(column.value(), field.getName()), field.getType()).build());
			}
		}
		return resultMappingList;
	}

	private static SqlCommandType getSqlCommandType(String qualifiedId) throws MybatisLightException {
		if(qualifiedId != null) {
			String[] arr = qualifiedId.split("\\.");
			for(SqlCommandType type : SqlCommandType.values()) {
				if(arr[arr.length - 1].toLowerCase().indexOf(type.name().toLowerCase()) == 0) {
					return type;
				}
			}
		}
		throw new MybatisLightException(String.format("MappedStatement [%s] cannot decide type", qualifiedId));
	}
	
	private static String getOrDefault(String value, String defaultValue) {
		return isNotEmpty(value) ? value : defaultValue;
	}
	
	private static boolean isEmpty(String str) {
		return str == null || "".equals(str);
	}
	
	private static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
}
