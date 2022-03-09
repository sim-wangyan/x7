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
package io.xream.rey.spring.beanconfiguration;

import io.xream.rey.fallback.FallbackParsed;
import io.xream.rey.fallback.FallbackParser;
import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

/**
 * @author Sim
 */
public class FallbackPostProcessor implements BeanPostProcessor {

    private Advice advice;

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (!bean.getClass().getName().contains("io.rey"))
            return bean;

        for (FallbackParsed parsed : FallbackParser.all()) {
            if (parsed.getTargetClass() == bean.getClass()
                    || parsed.getTargetClass().isAssignableFrom(bean.getClass())
            ) {

                Method[] fallbackMethodArr = parsed.getFallback().getClass().getDeclaredMethods();
                int length = fallbackMethodArr.length;
                if (length == 0)
                    continue;
                String[] nameArr = new String[fallbackMethodArr.length];
                for (int i = 0; i < length; i++) {
                    nameArr[i] = fallbackMethodArr[i].getName();
                }

                NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
                advisor.setMappedNames(nameArr);
                advisor.setAdvice(advice);

                ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
                proxyFactoryBean.addAdvisor(advisor);
                proxyFactoryBean.setTarget(bean);
                proxyFactoryBean.setAutodetectInterfaces(true);
                proxyFactoryBean.setProxyTargetClass(false);
                return proxyFactoryBean.getObject();
            }
        }

        return bean;
    }
}
