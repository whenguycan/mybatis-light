package com.daoketa.mybatis.light.core;

import java.util.List;

/**
 * @author wangcy 2024/9/29 16:21
 */
public interface BaseMapper<T, ID> {

	List<T> selectList(T t);
	
}
