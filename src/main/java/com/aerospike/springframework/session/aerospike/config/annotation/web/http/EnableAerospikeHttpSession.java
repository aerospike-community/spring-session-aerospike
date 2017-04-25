/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aerospike.springframework.session.aerospike.config.annotation.web.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.aerospike.springframework.session.aerospike.AerospikeOperationsSessionRepository;

/**
 * Add this annotation to a {@code @Configuration} class to expose the
 * SessionRepositoryFilter as a bean named "springSessionRepositoryFilter" and backed by
 * an Aerospike database. 
 *   Use {@code namespace} to change default name of the namespce used to store sessions ("session_store").
 *   Use {@code maxInactiveIntervalInSeconds} to change the default session timeout (1800 seconds).
 * <pre>
 * <code>
 * {@literal @EnableAerospikeHttpSession}
 * public class AerospikeHttpSessionConfig {
 *
 *	   {@literal @Value("${spring.aerospike.hostname}")}
 *	   private String hostName;
 *
 *	   {@literal @Value("${spring.aerospike.port}")}
 *	   private int port;
 *
 *	   {@literal @Value("${spring.aerospike.namespace}")}
 *	   private String namespace;
 *
 *	   {@literal @Bean}
 *	    public AerospikeTemplate aerospikeTemplate() {
 *		   AerospikeClient aerospikeClient;
 *		   aerospikeClient = new AerospikeClient(hostName, port);
 *		   return new AerospikeTemplate(aerospikeClient, namespace);
 *	   }
 * }
 * </code> </pre>
 *
 * @author Jeff Boone
 * @author Michael Zhang
 * @since 2.0.0
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
@Documented
@Import(AerospikeHttpSessionConfiguration.class)
@Configuration
public @interface EnableAerospikeHttpSession {
	int maxInactiveIntervalInSeconds() 
		default AerospikeOperationsSessionRepository.DEFAULT_INACTIVE_INTERVAL;
	String namespace() default AerospikeOperationsSessionRepository.DEFAULT_NAMESPACE;
}

