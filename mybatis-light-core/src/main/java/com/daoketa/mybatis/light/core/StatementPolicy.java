package com.daoketa.mybatis.light.core;

/**
 * @author wangcy 2024/9/29 15:57
 */
public enum StatementPolicy {

	strict, // exception when both xml and default exists
	cover, // if xml exists then use xml otherwise use default
	
}
