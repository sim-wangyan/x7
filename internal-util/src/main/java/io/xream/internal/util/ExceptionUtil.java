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
package io.xream.internal.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionUtil {

    private ExceptionUtil() {
    }

    private static boolean startWithJava(String str) {
        if (str.startsWith("java")) {
            return str.startsWith("java.lang.reflect")
                    || str.startsWith("java.lang.Thread")
                    || str.startsWith("javax");
        }
        return false;
    }

    private static boolean startWithIO(String str) {
        if (str.startsWith("io")) {
            return str.startsWith("io.github.resili")
                    || str.startsWith("io.vavr")
                    || str.startsWith("io.undertow")
                    || str.startsWith("io.xream.x7")
                    || str.startsWith("io.xream.rey")
                    || str.startsWith("io.opentracing");
        }
        return false;
    }

    private static boolean startWithOrg(String str) {
        if (str.startsWith("org")) {
            return (str.startsWith("org.spring")
                    && !(
                            str.startsWith("org.springframework.util.Assert")
                            || str.startsWith("org.springframework.jdbc")
                        )
                    )
                    || str.startsWith("org.jboss");
        }
        return false;
    }

    public static String getStack(Throwable e) {
        String msg = "";
        StackTraceElement[] eleArr = e.getStackTrace();
        if (eleArr == null || eleArr.length == 0)
            return msg;

        int i = 11;
        for (StackTraceElement ele : eleArr) {
            String str = ele.toString();
            boolean notAppend = str.startsWith("sun.reflect")
                    || startWithJava(str)
                    || startWithOrg(str)
                    || startWithIO(str)
                    || str.startsWith("com.sun");
            if (
                    !notAppend
            ) {
                msg += str;
                msg += " ";
                if (--i == 0)
                    break;
            }
        }

        return msg;
    }

    public static String getMessage(Throwable e) {
        String msg = e.getMessage();
        msg += " ";
        StackTraceElement[] eleArr = e.getStackTrace();
        if (eleArr == null || eleArr.length == 0)
            return msg;

        int i = 11;
        for (StackTraceElement ele : eleArr) {
            String str = ele.toString();
            boolean notAppend = str.startsWith("sun.reflect")
                    || startWithJava(str)
                    || startWithOrg(str)
                    || startWithIO(str)
                    || str.startsWith("com.sun");
            if (
                    !notAppend
            ) {
                msg += str;
                msg += " ";
                if (--i == 0)
                    break;
            }
        }

        return msg;
    }

    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }
}
