package org.apache.maven.plugins;

import java.io.Serializable;

/**
 * Created by agriboyedov on 7/6/2016.
 */
public class Parameter implements Serializable {
    /**
     * Annotation parameter name.
     */
    public String name;
    /**
     * Annotation parameter value.
     */
    public ParameterValue value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterValue getValue() {
        return value;
    }

    public void setValue(ParameterValue value) {
        this.value = value;
    }

    public void set(Parameter other) {
        this.name = other.name;
        this.value = other.value;
    }
}
