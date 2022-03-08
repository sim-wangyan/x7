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
package io.xream.rey.fallback.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sim
 */
public class FallbackCounter {

    private static Map<String,Counter> counterMap = new ConcurrentHashMap<>();
    private static MeterRegistry registry;

    public FallbackCounter(MeterRegistry r) {
        registry = r;
    }

    public static void increment(String className,
                                 String methodName,
                                 String uri,
                                 String exceptionName) {
        String key = className + methodName;
        Counter counter = counterMap.get(key);
        if (counter == null) {
            counter = Counter
                    .builder("rey_fallback_count")
                    .baseUnit("seconds")
                    .tag("class",className)
                    .tag("method",methodName)
                    .tag("uri",uri)
                    .tag("exception",exceptionName)
                    .description("Rey fallback count base on seconds")
                    .register(registry);
            counter.increment();
            counterMap.put(key,counter);
        }else {
            counter.increment();
        }
    }

}
