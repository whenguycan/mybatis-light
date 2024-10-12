package com.daoketa.mybatis.light.autoconfigure;

import com.daoketa.mybatis.light.core.MappedStatementResolver;
import com.daoketa.mybatis.light.core.MybatisLightProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author wangcy 2024/9/29 15:26
 */
@Configuration
@ConditionalOnClass({MybatisLightAutoConfiguration.class, SqlSessionFactory.class})
@AutoConfigureAfter({MybatisAutoConfiguration.class})
@EnableConfigurationProperties({MybatisLightProperties.class})
public class MybatisLightAutoConfiguration implements InitializingBean {

	@Autowired
	List<SqlSessionFactory> sqlSessionFactoryList;
	@Autowired
	MybatisLightProperties mybatisLightProperties;

	@Override
	public void afterPropertiesSet() throws Exception {
		if(sqlSessionFactoryList == null || sqlSessionFactoryList.isEmpty()) {
			throw new IllegalArgumentException("org.apache.ibatis.session.SqlSessionFactory not found");
		}
		MappedStatementResolver cpsr = MappedStatementResolver.builder()
				.mybatisLightProperties(mybatisLightProperties)
				.build();
		for(SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
			cpsr.resolve(sqlSessionFactory.getConfiguration());
		}
	}
}
