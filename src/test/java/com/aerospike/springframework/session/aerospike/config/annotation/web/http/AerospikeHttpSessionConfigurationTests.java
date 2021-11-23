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

import static org.assertj.core.api.Assertions.assertThat;

import com.aerospike.client.Host;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.aerospike.config.AbstractAerospikeDataConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.aerospike.springframework.session.aerospike.AerospikeIndexedSessionRepository;

import java.util.Collection;
import java.util.Collections;

/**
 * Tests for {@link AerospikeHttpSessionConfiguration}.
 *
 * @author Jeff Boone
 * @author Michael Zhang
 */
public class AerospikeHttpSessionConfigurationTests {

    private static final int MAX_INACTIVE_INTERVAL_IN_SECONDS = 800;
    private static final String TEST_NAMESPACE = "session_test";

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @After
    public void after() {
        this.context.close();
    }

    @Test
    public void customMaxInactiveIntervalInSeconds() {
        registerAndRefresh(CustomMaxInactiveIntervalInSecondsConfiguration.class);

        AerospikeIndexedSessionRepository repository = this.context
                .getBean(AerospikeIndexedSessionRepository.class);
        assertThat(repository).isNotNull();
        assertThat(ReflectionTestUtils.getField(repository, "maxInactiveIntervalInSeconds"))
                .isEqualTo(MAX_INACTIVE_INTERVAL_IN_SECONDS);
    }

    @Test
    public void customNamespace() {
        registerAndRefresh(CustomNamespaceConfiguration.class);

        AerospikeIndexedSessionRepository repository = this.context
                .getBean(AerospikeIndexedSessionRepository.class);
        assertThat(repository).isNotNull();
        assertThat(ReflectionTestUtils.getField(repository, "namespace"))
                .isEqualTo(TEST_NAMESPACE);
    }

    private void registerAndRefresh(Class<?>... annotatedClasses) {
        this.context.register(annotatedClasses);
        this.context.refresh();
    }

    static class BaseConfiguration extends AbstractAerospikeDataConfiguration {
        @Override
        protected Collection<Host> getHosts() {
            return Collections.singleton(new Host("localhost", 3000));
        }

        @Override
        protected String nameSpace() {
            return "test";
        }
    }

    @Configuration
    @EnableAerospikeHttpSession(maxInactiveIntervalInSeconds = MAX_INACTIVE_INTERVAL_IN_SECONDS)
    static class CustomMaxInactiveIntervalInSecondsConfiguration extends BaseConfiguration {

    }

    @Configuration
    @EnableAerospikeHttpSession(namespace = TEST_NAMESPACE)
    static class CustomNamespaceConfiguration extends BaseConfiguration {

    }
}
