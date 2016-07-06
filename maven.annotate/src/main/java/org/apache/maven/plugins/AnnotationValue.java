package org.apache.maven.plugins;

import java.io.Serializable;

/**
 * Created by agriboyedov on 7/6/2016.
 */
public class AnnotationValue implements Serializable {
    public String annotation;
    public Parameter[] parameters;

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    public void set(AnnotationValue other) {
        this.annotation = other.annotation;
        this.parameters = other.parameters;
    }
}
