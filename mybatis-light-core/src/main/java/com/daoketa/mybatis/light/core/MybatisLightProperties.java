package com.daoketa.mybatis.light.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wangcy 2024/9/29 16:00
 */
@Data
@ConfigurationProperties(prefix = "mybatis-light")
public class MybatisLightProperties {

	/**
	 * MappedStatement cover policy
	 */
	StatementPolicy statementPolicy = StatementPolicy.strict; // exception on duplicate

}
