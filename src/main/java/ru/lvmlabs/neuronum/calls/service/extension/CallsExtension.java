package ru.lvmlabs.neuronum.calls.service.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CallsExtension {
    String[] administratorNames();

    String[] doctorSpecialties();
}
