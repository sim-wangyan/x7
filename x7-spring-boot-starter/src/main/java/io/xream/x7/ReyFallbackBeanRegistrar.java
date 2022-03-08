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


import io.xream.rey.annotation.Fallback;
import io.xream.rey.fallback.FallbackParser;
import io.xream.x7.base.util.ClassFileReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;


public class ReyFallbackBeanRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        String startClassName = annotationMetadata.getClassName();
        String basePackage = startClassName.substring(0, startClassName.lastIndexOf("."));

        Set<Class<?>> set = ClassFileReader.getClasses(basePackage);
        try {
            Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableFallback.class.getName());
            Object obj = attributes.get("basePackages");
            if (obj != null) {
                String[] strArr = (String[]) obj;
                for (String str : strArr) {
                    Set<Class<?>> set1 = ClassFileReader.getClasses(str);
                    set.addAll(set1);
                }
            }
        }catch (Exception e) {
            return;
        }

        for (Class clz : set) {
            Fallback annotation = (Fallback)clz.getAnnotation(Fallback.class);
            if (annotation == null)
                continue;

            FallbackParser.parse(annotation.ignoreExceptions(), clz,
                    annotation.fallback(), fallback -> beanFactory.getBean(fallback));

        }
    }

    private BeanFactory beanFactory;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


}
