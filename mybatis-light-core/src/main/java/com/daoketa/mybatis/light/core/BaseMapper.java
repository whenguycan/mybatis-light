package com.daoketa.mybatis.light.core;

import java.util.List;

/**
 * @author wangcy 2024/9/29 16:21
 */
public interface BaseMapper<T, ID> {

	T selectOne(ID id);
	
	List<T> selectList(T t);
	
	int insert(T t);
	
	int update(T t);
	
	int delete(ID... ids);
	
}
