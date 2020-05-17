package io.quarkus.test.junit.mockito.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Qualifier;

import org.mockito.Mockito;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.test.junit.callback.QuarkusTestBeforeAllCallback;
import io.quarkus.test.junit.mockito.InjectSpy;

public class CreateMockitoSpiesCallback implements QuarkusTestBeforeAllCallback {

    @Override
    public void beforeAll(Object testInstance) {
        Class<?> current = testInstance.getClass();
        while (current.getSuperclass() != null) {
            for (Field field : current.getDeclaredFields()) {
                InjectSpy injectSpyAnnotation = field.getAnnotation(InjectSpy.class);
                if (injectSpyAnnotation != null) {
                    Object beanInstance = getBeanInstance(testInstance, field);
                    Object spy = createSpyAndSetTestField(testInstance, field, beanInstance);
                    MockitoMocksTracker.track(testInstance, spy, beanInstance);
                }
            }
            current = current.getSuperclass();
        }
    }

    private Object createSpyAndSetTestField(Object testInstance, Field field, Object beanInstance) {
        Object spy = Mockito.spy(beanInstance);
        field.setAccessible(true);
        try {
            field.set(testInstance, spy);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return spy;
    }

    private Object getBeanInstance(Object testInstance, Field field) {
        Class<?> fieldClass = field.getType();
        InstanceHandle<?> instance = Arc.container().instance(fieldClass, getQualifiers(field));
        if (!instance.isAvailable()) {
            throw new IllegalStateException("Invalid use of @InjectSpy - could not determine bean of type: "
                    + fieldClass + ". Offending field is " + field.getName() + " of test class "
                    + testInstance.getClass());
        }
        return instance.get();
    }

    private Annotation[] getQualifiers(Field fieldToSpy) {
        List<Annotation> qualifiers = new ArrayList<>();
        Annotation[] fieldAnnotations = fieldToSpy.getDeclaredAnnotations();
        for (Annotation fieldAnnotation : fieldAnnotations) {
            for (Annotation annotationOfFieldAnnotation : fieldAnnotation.annotationType().getAnnotations()) {
                if (annotationOfFieldAnnotation.annotationType().equals(Qualifier.class)) {
                    qualifiers.add(fieldAnnotation);
                    break;
                }
            }
        }
        return qualifiers.toArray(new Annotation[0]);
    }
}
