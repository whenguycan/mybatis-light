package com.daoketa.mybatis.light.core.provider;

import com.daoketa.mybatis.light.core.Cnd;
import com.daoketa.mybatis.light.core.Column;
import com.daoketa.mybatis.light.core.MybatisLightException;
import com.daoketa.mybatis.light.core.common.Ctx;
import com.daoketa.mybatis.light.core.common.Toolkit;
import lombok.AllArgsConstructor;
import org.apache.ibatis.scripting.xmltags.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author wangcy 2024/10/14 14:55
 */
@AllArgsConstructor
public abstract class AbstractRootSqlNodeProvider implements RootSqlNodeProvider {

	final String methodName;

	@Override
	public boolean supports(String qualifiedId) throws MybatisLightException {
		return Toolkit.matches(qualifiedId, methodName);
	}
	
	protected SqlNode getFullColumnSqlNode(Ctx ctx) {
		return new TrimSqlNode(ctx.getConfiguration(), new MixedSqlNode(
				Arrays.stream(ctx.getFields())
						.filter(f -> f.isAnnotationPresent(Column.class))
						.map(f -> {
							String columnName = Toolkit.getColumnName(f.getAnnotation(Column.class), f);
							return new TextSqlNode(String.format(" %s,", columnName));
						})
						.collect(Collectors.toList())
		), null, null, null, ",");
	}
	
	protected SqlNode getWhereSqlNodeForSelect(Ctx ctx) {
		return new WhereSqlNode(ctx.getConfiguration(), new MixedSqlNode(
				Arrays.stream(ctx.getFields())
						.filter(f -> f.isAnnotationPresent(Column.class))
						.map(f -> {
							Column column = f.getAnnotation(Column.class);
							String columnName = Toolkit.getColumnName(column, f);
							String domainProperty = Toolkit.getDomainProperty(f);
							String template = column.cnd() == Cnd.like ? "%s LIKE CONCAT('%%', #{%s}, '%%')" : " %s = #{%s}";
							String sql = String.format(template, columnName, domainProperty);
							return new IfSqlNode(new TextSqlNode(sql), String.format("%s != null", domainProperty));
						})
						.collect(Collectors.toList())
		));
	}
}
