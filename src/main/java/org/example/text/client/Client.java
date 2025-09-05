package org.example.text.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD}) // 可以应用在类和方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
public @interface Client {
    String address();
    String name();
}