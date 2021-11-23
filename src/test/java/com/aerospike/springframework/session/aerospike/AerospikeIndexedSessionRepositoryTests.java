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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.aerospike.core.AerospikeOperations;

import java.time.Duration;

/**
 * Tests for {@link AerospikeIndexedSessionRepository}.
 *
 * @author Jeff Boone
 * @author Michael Zhang
 */
public class AerospikeIndexedSessionRepositoryTests {

    @Mock
    AerospikeOperations aerospikeOperations;

    AerospikeIndexedSessionRepository repository;

    @Before
    public void setUp() {
        this.repository = new AerospikeIndexedSessionRepository(aerospikeOperations);
    }

    @Test
    public void shouldCreateSession() {
        AerospikeSession session = repository.createSession();

        assertThat(session.getId()).isNotEmpty();
        assertThat(session.getMaxInactiveInterval())
                .isEqualTo(Duration.ofSeconds(AerospikeIndexedSessionRepository.DEFAULT_INACTIVE_INTERVAL));
    }
}
