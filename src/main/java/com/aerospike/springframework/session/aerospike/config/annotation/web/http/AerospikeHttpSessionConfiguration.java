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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.aerospike.core.AerospikeOperations;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;

import com.aerospike.springframework.session.aerospike.AerospikeIndexedSessionRepository;

/**
 * Configuration class registering {@code AerospikeOperationsSessionRepository}
 * bean. To import this configuration use {@link EnableAerospikeHttpSession}
 * annotation.
 *
 * @author Jeff Boone
 * @author Michael Zhang
 * @since 2.0.0
 */
@Configuration
public class AerospikeHttpSessionConfiguration extends SpringHttpSessionConfiguration
        implements ImportAware {

    private Integer maxInactiveIntervalInSeconds;
    private String namespace;

    @Bean
    public AerospikeIndexedSessionRepository aerospikeSessionRepository(AerospikeOperations aerospikeOperations) {
        AerospikeIndexedSessionRepository repository =
                new AerospikeIndexedSessionRepository(aerospikeOperations);
        repository.setMaxInactiveIntervalInSeconds(this.maxInactiveIntervalInSeconds);
        repository.setNamespace(this.namespace);
        return repository;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setMaxInactiveIntervalInSeconds(Integer maxInactiveIntervalInSeconds) {
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importMetadata
                .getAnnotationAttributes(EnableAerospikeHttpSession.class.getName()));
        if (attributes != null) {
            this.maxInactiveIntervalInSeconds = attributes.getNumber("maxInactiveIntervalInSeconds");
            this.namespace = attributes.getString("namespace");
        } else {
            this.maxInactiveIntervalInSeconds = AerospikeIndexedSessionRepository.DEFAULT_INACTIVE_INTERVAL;
            this.namespace = AerospikeIndexedSessionRepository.DEFAULT_NAMESPACE;
        }
    }
}

