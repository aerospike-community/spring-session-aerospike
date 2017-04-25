
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
package com.aerospike.springframework.session.aerospike;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.data.aerospike.core.AerospikeOperations;
import org.springframework.data.aerospike.repository.query.Criteria;
import org.springframework.data.aerospike.repository.query.Query;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.session.ExpiringSession;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;

import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.IndexType;


/**
 * Implementation of a SessionRepository which uses an Aerospike database
 * to store sessions.
 *
 * @author Jeff Boone
 * @author Michael Zhang
 * @since 2.0
 */
public class AerospikeOperationsSessionRepository 
	implements FindByIndexNameSessionRepository<ExpiringSession> {
 
 	static PrincipalNameResolver PRINCIPAL_NAME_RESOLVER = new PrincipalNameResolver();

	AerospikeOperations aerospikeOperations;

	private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

	/**
	 * The default time period in seconds in which a session will expire.
	 */
	public static final int DEFAULT_INACTIVE_INTERVAL = 1800;

	/**
	 * the default namespace for storing session.
	 */
	public static final String DEFAULT_NAMESPACE = "session_store";
	
	/**
	 * the default set name for storing session.
	 */
	public static final String DEFAULT_SET_NAME = "sessions";
	
	/**
	 * the bin name for the session data.
	 */
	private static final String BIN_NAME = "data";
	private static final String BIN_NAME_PRINCIPAL = "principal";
	private static final String PRINCIPAL_INDEX = "principal_index";
	
	private String namespace = DEFAULT_NAMESPACE;
	private String setname = DEFAULT_SET_NAME;
	private int	maxInactiveIntervalInSeconds = DEFAULT_INACTIVE_INTERVAL;
	
	public AerospikeOperationsSessionRepository(AerospikeOperations aerospikeOperations) {
		this.aerospikeOperations = aerospikeOperations;
	}
	
	@PostConstruct
	public void ensureIndexesAreCreated() {
		this.aerospikeOperations.createIndex(AerospikeExpiringSession.class, PRINCIPAL_INDEX, BIN_NAME_PRINCIPAL, IndexType.STRING);
	}

	public ExpiringSession createSession() {
		return new MapSession();
	}

	public void save(ExpiringSession session) {
		WritePolicy writePolicy = new WritePolicy();
		writePolicy.expiration = this.maxInactiveIntervalInSeconds;
		AerospikeExpiringSession aSession = new AerospikeExpiringSession(session);
		aSession.setMaxInactiveIntervalInSeconds(this.maxInactiveIntervalInSeconds);
		this.aerospikeOperations.save(session.getId(), aSession, writePolicy);
	}

	public ExpiringSession getSession(String id) {
		AerospikeExpiringSession session = this.aerospikeOperations.findOne(id, AerospikeExpiringSession.class);
		if (session == null) return null;

		return new MapSession(session);
	}

	public void delete(String id) {
		this.aerospikeOperations.delete(id, AerospikeExpiringSession.class);
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setSetName(String setname) {
		this.setname = setname;
	}

	public void setMaxInactiveIntervalInSeconds(Integer maxInactiveIntervalInSeconds) {
		this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
	}

	public Map<String, ExpiringSession> findByIndexNameAndIndexValue(String indexName, 
			String indexValue) {
		if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
			return Collections.emptyMap();
		}

		HashMap<String, ExpiringSession> result = new HashMap<String, ExpiringSession>();

		Query<?> query = new Query<Object>(
				Criteria.where(AerospikeExpiringSession.class.getSimpleName()).is(indexValue, BIN_NAME_PRINCIPAL));

		Iterable<AerospikeExpiringSession> it = this.aerospikeOperations.find( query, AerospikeExpiringSession.class);
		for (AerospikeExpiringSession aesw : it) {
			result.put(aesw.getId(), new MapSession(aesw));
		}

		return result;
	}

	/**
	 * Principal name resolver helper class.
	 */
	static class PrincipalNameResolver {
		private SpelExpressionParser parser = new SpelExpressionParser();

		public String resolvePrincipal(Session session) {
			String principalName = session.getAttribute(PRINCIPAL_NAME_INDEX_NAME);
			if (principalName != null) {
				return principalName;
			}
			Object authentication = session.getAttribute(SPRING_SECURITY_CONTEXT);
			if (authentication != null) {
				Expression expression = this.parser
						.parseExpression("authentication?.name");
				return expression.getValue(authentication, String.class);
			}
			return null;
		}
	}

}