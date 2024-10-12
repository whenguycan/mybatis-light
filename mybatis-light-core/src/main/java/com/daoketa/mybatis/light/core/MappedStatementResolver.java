package com.daoketa.mybatis.light.core;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author wangcy 2024/9/29 15:46
 */
@Slf4j
@Builder
public class MappedStatementResolver {

	MybatisLightProperties mybatisLightProperties;
	
	public void resolve(Configuration configuration) throws MybatisLightException {
		StatementPolicy statementPolicy = mybatisLightProperties.getStatementPolicy();
		final Set<String> mappedStatementNameSet = new HashSet<>(configuration.getMappedStatementNames());
		for(Class<?> mapperClass : configuration.getMapperRegistry().getMappers()) {
			if(BaseMapper.class.isAssignableFrom(mapperClass)) {
				for(Method baseMapperMethod : BaseMapper.class.getMethods()) {
					boolean generate = false;
					String qualifiedId = mapperClass.getName() + "." + baseMapperMethod.getName();
					if(statementPolicy == StatementPolicy.cover) {
						if(!mappedStatementNameSet.contains(qualifiedId)) {
							generate = true;
						}else {
							log.warn("MappedStatement {} cover default on StatementPolicy.cover", qualifiedId);
						}
					}else if(statementPolicy == StatementPolicy.strict) {
						if(!mappedStatementNameSet.contains(qualifiedId)) {
							generate = true;
						}else {
							String message = String.format("duplicate mappedStatement %s found on StatementPolicy.strict", qualifiedId);
							log.error(message);
							throw new MybatisLightException(message);
						}
					}
					if(generate) {
						Class<?> domainClass = ResolvableType.forType(mapperClass).getInterfaces()[0].getGeneric(0).getRawClass();
						configuration.addMappedStatement(Toolkit.createMappedStatement(configuration, qualifiedId, domainClass));
					}
				}
			}
		}
	}
	
}
