package com.wdcloud.data.neo4j;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

/**
 * @author bigd
 */
@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration
@EnableTransactionManagement
@EntityScan("com.wdcloud.data.neo4j.domain")
@Import(RepositoryRestMvcConfiguration.class)
public class SampleMovieApplication {

	private  static final  String  REQUIRED = "PROPAGATION_REQUIRED,-Throwable";

	private  static final  String REQUIRED_READONLY = "PROPAGATION_REQUIRED,-Throwable,readOnly";

	public static void main(String[] args) {
		SpringApplication.run(SampleMovieApplication.class, args);
	}


	@Bean(name = "transactionInterceptor")
	public TransactionInterceptor transactionInterceptor(
			PlatformTransactionManager platformTransactionManager) {
		TransactionInterceptor transactionInterceptor = new TransactionInterceptor();

		/**
		 * 事物管理器
		 */
		transactionInterceptor.setTransactionManager(platformTransactionManager);
		Properties transactionAttributes = new Properties();
		/**
		 * 新增
		 */
		transactionAttributes.setProperty("insert*",REQUIRED);
		transactionAttributes.setProperty("create*",REQUIRED);
		transactionAttributes.setProperty("save*",REQUIRED);

		/**
		 * 修改
		 */
		transactionAttributes.setProperty("update*",REQUIRED);
		/**
		 * 删除
		 */
		transactionAttributes.setProperty("delete*",REQUIRED);
		transactionAttributes.setProperty("remove*",REQUIRED);
		/**
		 * 查询
		 */
		transactionAttributes.setProperty("select*",REQUIRED_READONLY);
		transactionAttributes.setProperty("obtain*",REQUIRED_READONLY);

		transactionInterceptor.setTransactionAttributes(transactionAttributes);
		return transactionInterceptor;
	}

	/**
	 * 代理到ServiceImpl的Bean
	 */
	@Bean
	public BeanNameAutoProxyCreator transactionAutoProxy() {
		BeanNameAutoProxyCreator transactionAutoProxy = new BeanNameAutoProxyCreator();
		transactionAutoProxy.setProxyTargetClass(true);
		transactionAutoProxy.setBeanNames("*ServiceImpl");
		transactionAutoProxy.setInterceptorNames("transactionInterceptor");
		return transactionAutoProxy;
	}

	@Value("${spring.data.neo4j.uri}")
	private String neo4jUrl;

	@Value("${spring.data.neo4j.username}")
	private String userName;

	@Value("${spring.data.neo4j.password}")
	private String passWord;
}

@Configuration
@PropertySource(value = "/conf/graph.properties")
class PropertiesConfiguration {
}
