package org.example.text.client;

import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ComponentScan(basePackages = {
        "org.example.text",
        "#{environment.getProperty('app.base-package', 'org.example.default')}"
})
public @interface RegisterClient {
}
