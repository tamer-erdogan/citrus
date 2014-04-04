/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.endpoint.resolver;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointComponent;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.*;

/**
 * Default endpoint resolver implementation uses registered endpoint components in Spring application context to resolve endpoint
 * from given endpoint uri.
 *
 * @author Christoph Deppisch
 */
public class DefaultEndpointResolver implements EndpointResolver {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultEndpointResolver.class);

    @Autowired(required = false)
    private Map<String, EndpointComponent> endpointComponents = new HashMap<String, EndpointComponent>();

    /** Default Citrus endpoint components from classpath resource properties */
    private Properties endpointComponentProperties;

    /** Spring application context */
    private ApplicationContext applicationContext;

    /**
     * Default constructor.
     */
    public DefaultEndpointResolver() {
        loadEndpointComponentProperties();
    }

    /**
     * Optional constructor using special application context instance.
     * @param applicationContext
     */
    public DefaultEndpointResolver(ApplicationContext applicationContext) {
        this();
        this.applicationContext = applicationContext;
        this.endpointComponents = applicationContext.getBeansOfType(EndpointComponent.class);
    }

    @Override
    public Endpoint resolve(String endpointUri) {
        if (endpointUri.indexOf(":") < 0) {
            return applicationContext.getBean(endpointUri, Endpoint.class);
        }

        StringTokenizer tok = new StringTokenizer(endpointUri, ":");
        if (tok.countTokens() < 2) {
            throw new CitrusRuntimeException(String.format("Invalid endpoint uri '%s'", endpointUri));
        }

        String componentName = tok.nextToken();
        EndpointComponent component = endpointComponents.get(componentName);

        if (component == null) {
            // try to get component from default Citrus modules
            component = resolveDefaultComponent(componentName);
        }

        if (component == null) {
            throw new CitrusRuntimeException(String.format("Unable to resolve endpoint component with name '%s'", componentName));
        }

        return component.createEndpoint(endpointUri);
    }

    private EndpointComponent resolveDefaultComponent(String componentName) {
        String endpointComponentClassName = endpointComponentProperties.getProperty(componentName);

        try {
            if (endpointComponentClassName != null) {
                Class<EndpointComponent> endpointComponentClass = (Class<EndpointComponent>) Class.forName(endpointComponentClassName);
                EndpointComponent endpointComponent = endpointComponentClass.newInstance();

                endpointComponent.setName(componentName);
                endpointComponent.setApplicationContext(applicationContext);
                return endpointComponent;
            }
        } catch (ClassNotFoundException e) {
            log.warn(String.format("Unable to find default Citrus endpoint component '%s' in classpath", endpointComponentClassName), e);
        } catch (InstantiationException e) {
            log.warn(String.format("Unable to instantiate Citrus endpoint component '%s'", endpointComponentClassName), e);
        } catch (IllegalAccessException e) {
            log.warn(String.format("Unable to access Citrus endpoint component '%s'", endpointComponentClassName), e);
        }

        return null;
    }

    /**
     * Loads property file from classpath holding default endpoint component definitions in Citrus.
     */
    private void loadEndpointComponentProperties() {
        try {
            endpointComponentProperties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("com/consol/citrus/endpoint/endpoint.components"));
        } catch (IOException e) {
            log.warn("Unable to laod default endpoint components from resource '%s'", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
