package org.apache.maven.plugins;

import java.io.Serializable;

/**
 * Created by agriboyedov on 7/5/2016.
 */
public class ParameterValue implements Serializable {
    /**
     * Parameter value type.
     */
    public Type type;
    /**
     * Simple value.
     */
    public String value;
    /**
     * Annotation value.
     */
    public AnnotationValue annotation;
    /**
     * Array value.
     */
    public ParameterValue[] values;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ParameterValue[] getValues() {
        return values;
    }

    public void setValues(ParameterValue[] values) {
        this.values = values;
    }

    public AnnotationValue getAnnotation() {
        return annotation;
    }

    public void setAnnotation(AnnotationValue annotation) {
        this.annotation = annotation;
    }

    public void set(ParameterValue other) {
        this.type = other.type;
        this.value = other.value;
        this.annotation = other.annotation;
        this.values = other.values;
    }

    public enum Type {
        ANNOTATION,
        ARRAY,
        BOOLEAN,
        BYTE,
        CHAR,
        CLASS,
        DOUBLE,
        ENUM,
        FLOAT,
        INTEGER,
        LONG,
        SHORT,
        STRING
    }
}
