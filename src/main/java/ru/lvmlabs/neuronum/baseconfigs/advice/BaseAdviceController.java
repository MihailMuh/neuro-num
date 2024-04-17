package ru.lvmlabs.neuronum.baseconfigs.advice;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseAdviceController {
    protected Map<String, Object> extractErrorAttributes(BindException exception) {
        Map<String, Object> errorAttributes = getBaseErrorAttributes();
        errorAttributes.put("details", exception
                .getBindingResult()
                .getFieldErrors()
                .parallelStream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList());

        return errorAttributes;
    }

    protected Map<String, Object> wrapException(Exception exception) {
        Map<String, Object> errorAttributes = getBaseErrorAttributes();
        errorAttributes.put("details", exception.getMessage());
        return errorAttributes;
    }

    protected Map<String, Object> getBaseErrorAttributes() {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", LocalDate.now());
        errorAttributes.put("message", "Can't process request!");
        return errorAttributes;
    }
}
