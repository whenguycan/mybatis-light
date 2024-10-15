package com.daoketa.mybatis.light.core.provider;

import com.daoketa.mybatis.light.core.common.Ctx;
import org.apache.ibatis.scripting.xmltags.*;

import java.util.Arrays;

/**
 * @author wangcy 2024/10/14 14:33
 */
public class SelectListRootSqlNodeProvider extends AbstractRootSqlNodeProvider implements RootSqlNodeProvider {

	public SelectListRootSqlNodeProvider() {
		super(METHOD_NAME_SELECT_LIST);
	}

	@Override
	public SqlNode create(Ctx ctx) {
		return new MixedSqlNode(Arrays.asList(
				new TextSqlNode(" SELECT"),
				getFullColumnSqlNode(ctx),
				new TextSqlNode(" FROM " + ctx.getTableName()),
				getWhereSqlNodeForSelect(ctx)
		));
	}
}
