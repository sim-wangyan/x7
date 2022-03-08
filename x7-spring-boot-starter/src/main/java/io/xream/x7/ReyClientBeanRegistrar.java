/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.x7;


import io.xream.rey.annotation.ReyClient;
import io.xream.rey.internal.BackendDecoration;
import io.xream.rey.internal.ClientBackend;
import io.xream.rey.internal.ClientBackendProxy;
import io.xream.rey.internal.ReyParser;
import io.xream.x7.base.util.ClassFileReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class ReyClientBeanRegistrar implements EnvironmentAware,ImportBeanDefinitionRegistrar, BeanFactoryAware {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        String startClassName = annotationMetadata.getClassName();
        String basePackage = startClassName.substring(0, startClassName.lastIndexOf("."));

        Set<Class<?>> set = ClassFileReader.getClasses(basePackage);

        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableReyClient.class.getName());

        Object isReyTemplateNotRequired = attributes.get("isReyTemplateNotRequired");

        Object obj = attributes.get("basePackages");
        if (obj != null){
            String[] strArr = (String[]) obj;
            for (String str : strArr){
                Set<Class<?>> set1 = ClassFileReader.getClasses(str);
                set.addAll(set1);
            }
        }


        for (Class clz : set) {
            ReyClient annotation = (ReyClient)clz.getAnnotation(ReyClient.class);
            if (annotation == null)
                continue;

            ReyParser.parse(clz,
                    urlPattern -> environment.resolvePlaceholders(urlPattern),
                    fallback -> beanFactory.getBean(fallback));

            String beanName = clz.getName();

            String backend = annotation.circuitBreaker();
            if (backend.equals(" ")){
                backend = null;
            }

            boolean retry = annotation.retry();

            ClientBackend clientBackend = this.beanFactory.getBean(ClientBackend.class);
            Objects.requireNonNull(clientBackend);

            if (!registry.containsBeanDefinition(beanName)) {

                BackendDecoration backendDecoration = new BackendDecoration();
                backendDecoration.setServiceName(clz.getSimpleName());
                backendDecoration.setConfigName(backend);
                backendDecoration.setRetry(retry);

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clz);
                GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                definition.getPropertyValues().add("objectType", clz);
                definition.getPropertyValues().add("backendDecoration", backendDecoration);
                definition.getPropertyValues().add("clientBackend",clientBackend);
                definition.getPropertyValues().add("reyTemplateNotRequired", isReyTemplateNotRequired);
                definition.setBeanClass(ClientBackendProxy.class);
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

                registry.registerBeanDefinition(beanName, definition);

            }
        }
    }


    private Environment environment;
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private BeanFactory beanFactory;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
