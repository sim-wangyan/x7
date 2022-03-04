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
package io.xream.x7.fallback.internal.aop;

import io.xream.x7.fallback.internal.FallbackParsed;
import io.xream.x7.fallback.internal.FallbackParser;
import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

/**
 * @author Sim
 */
public class FallbackProxy implements BeanPostProcessor {

    private Advice advice;

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        for (FallbackParsed parsed : FallbackParser.all()) {
            if (parsed.getTargetClass() == bean.getClass()) {

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
                proxyFactoryBean.setAutodetectInterfaces(false);
                proxyFactoryBean.setProxyTargetClass(true);
                return proxyFactoryBean.getObject();
            }
        }


        return bean;
    }
}
