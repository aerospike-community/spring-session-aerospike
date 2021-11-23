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
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.aerospike.springframework.session.aerospike.AerospikeIndexedSessionRepository.PrincipalNameResolver;

import org.springframework.session.Session;

/**
 * Session object providing additional information about the datetime of expiration.
 *
 * @author Jeff Boone
 * @author Michael Zhang
 * @since 2.0
 */
public class AerospikeSession implements Session {

    static PrincipalNameResolver PRINCIPAL_NAME_RESOLVER = new PrincipalNameResolver();

    private String id;
    private long createdInMillis = System.currentTimeMillis();
    private long accessedInMillis;
    private long intervalInSeconds;
    private String principal;
    private Map<String, Object> attrs = new HashMap<>();
    private Date expireAt;

    public AerospikeSession() {
        this(AerospikeIndexedSessionRepository.DEFAULT_INACTIVE_INTERVAL);
    }

    public AerospikeSession(int maxInactiveIntervalInSeconds) {
        this(UUID.randomUUID().toString(), maxInactiveIntervalInSeconds);
    }

    public AerospikeSession(String id, int maxInactiveIntervalInSeconds) {
        this.id = id;
        this.intervalInSeconds = maxInactiveIntervalInSeconds;
        setLastAccessedTime(Instant.ofEpochMilli(this.createdInMillis));
    }

    public AerospikeSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        this.id = session.getId();
        this.attrs = new HashMap<>(
                session.getAttributeNames().size());
        for (String attrName : session.getAttributeNames()) {
            Object attrValue = session.getAttribute(attrName);
            this.attrs.put(attrName, attrValue);
        }
        this.accessedInMillis = session.getLastAccessedTime().toEpochMilli();
        this.createdInMillis = session.getCreationTime().toEpochMilli();
        this.intervalInSeconds = session.getMaxInactiveInterval().getSeconds();
        this.setPrincipal(PRINCIPAL_NAME_RESOLVER.resolvePrincipal(session));
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String changeSessionId() {
        String newId = UUID.randomUUID().toString();
        this.id = newId;
        return newId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attributeName) {
        return (T) this.attrs.get(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
        return attrs.keySet();
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributeValue == null) {
            removeAttribute(attributeName);
        } else {
            this.attrs.put(attributeName, attributeValue);
        }
    }

    @Override
    public void removeAttribute(String attributeName) {
        this.attrs.remove(attributeName);
    }

    @Override
    public Instant getCreationTime() {
        return Instant.ofEpochMilli(this.createdInMillis);
    }

    public void setCreationTime(long created) {
        this.createdInMillis = created;
    }

    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {
        this.accessedInMillis = lastAccessedTime.toEpochMilli();
        this.expireAt = Date.from(lastAccessedTime.plus(Duration.ofSeconds(this.intervalInSeconds)));
    }

    @Override
    public Instant getLastAccessedTime() {
        return Instant.ofEpochMilli(this.accessedInMillis);
    }

    @Override
    public void setMaxInactiveInterval(Duration duration) {
        this.intervalInSeconds = duration.getSeconds();
    }

    @Override
    public Duration getMaxInactiveInterval() {
        return Duration.ofSeconds(this.intervalInSeconds);
    }

    @Override
    public boolean isExpired() {
        return this.intervalInSeconds >= 0 && new Date().after(this.expireAt);
    }

    public Date getExpireAt() {
        return this.expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AerospikeSession that = (AerospikeSession) o;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
