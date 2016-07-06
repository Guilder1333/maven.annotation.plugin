package org.apache.maven.plugins;

import java.io.Serializable;

/**
 * Created by agriboyedov on 7/5/2016.
 */
public class AnnotationTarget implements Serializable {
    public String className;

    public String annotation;

    public Parameter[] parameters;

    public boolean remove;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    public void set(AnnotationTarget other) {
        this.className = other.className;
        this.annotation = other.annotation;
        this.parameters = other.parameters;
        this.remove = other.remove;
    }
}
