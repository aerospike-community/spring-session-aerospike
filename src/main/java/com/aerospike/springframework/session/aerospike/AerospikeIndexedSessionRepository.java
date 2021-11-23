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

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.data.aerospike.core.AerospikeOperations;
import org.springframework.data.aerospike.repository.query.Criteria;
import org.springframework.data.aerospike.repository.query.Query;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.session.FindByIndexNameSessionRepository;
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
public class AerospikeIndexedSessionRepository
        implements FindByIndexNameSessionRepository<AerospikeSession> {

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
    private String setName = DEFAULT_SET_NAME;
    private int maxInactiveIntervalInSeconds = DEFAULT_INACTIVE_INTERVAL;

    public AerospikeIndexedSessionRepository(AerospikeOperations aerospikeOperations) {
        this.aerospikeOperations = aerospikeOperations;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public void setMaxInactiveIntervalInSeconds(Integer maxInactiveIntervalInSeconds) {
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    }

    @PostConstruct
    public void ensureIndexesAreCreated() {
        try {
            aerospikeOperations.createIndex(AerospikeSession.class, PRINCIPAL_INDEX, BIN_NAME_PRINCIPAL, IndexType.STRING);
        } catch (Exception e) {

        }
    }

    @Override
    public AerospikeSession createSession() {
        AerospikeSession aerospikeSession = new AerospikeSession();
        aerospikeSession.setMaxInactiveInterval(Duration.ofSeconds(this.maxInactiveIntervalInSeconds));
        return aerospikeSession;
    }

    @Override
    public void save(AerospikeSession session) {
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.expiration = this.maxInactiveIntervalInSeconds;
        session.setMaxInactiveInterval(Duration.ofSeconds(this.maxInactiveIntervalInSeconds));
        aerospikeOperations.persist(session, writePolicy);
    }

    @Override
    public AerospikeSession findById(String id) {
        return aerospikeOperations.findById(id, AerospikeSession.class);
    }

    @Override
    public void deleteById(String id) {
        aerospikeOperations.delete(id, AerospikeSession.class);
    }

    @Override
    public Map<String, AerospikeSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {
        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Collections.emptyMap();
        }

        Query query = new Query(Criteria.where(AerospikeSession.class.getSimpleName()).is(indexValue, BIN_NAME_PRINCIPAL));

        return aerospikeOperations
                .find(query, AerospikeSession.class)
                .collect(Collectors.toMap(AerospikeSession::getId, session -> session));
    }

    /**
     * Principal name resolver helper class.
     */
    static class PrincipalNameResolver {
        private final SpelExpressionParser parser = new SpelExpressionParser();

        public String resolvePrincipal(Session session) {
            String principalName = session.getAttribute(PRINCIPAL_NAME_INDEX_NAME);
            if (principalName != null) {
                return principalName;
            }
            Object authentication = session.getAttribute(SPRING_SECURITY_CONTEXT);
            if (authentication != null) {
                Expression expression = this.parser.parseExpression("authentication?.name");
                return expression.getValue(authentication, String.class);
            }
            return null;
        }
    }
}
