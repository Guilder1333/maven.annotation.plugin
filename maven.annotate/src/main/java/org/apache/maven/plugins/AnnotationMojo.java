package org.apache.maven.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "annotation", defaultPhase = LifecyclePhase.COMPILE)
public class AnnotationMojo extends AbstractMojo
{
    /**
     * @parameter required="true"
     */
    @org.apache.maven.plugins.annotations.Parameter(required = true)
    private AnnotationTarget[] targets;

    /**
     * @parameter default-value="${project.build.directory}"
     */
    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project.build.directory}")
    private String classesDir;

    private final Map<String, List<AnnotationTarget>> targetsMap = new HashMap<String, List<AnnotationTarget>>();

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        final String directory = classesDir + "/classes";
        ClassPool classPool = new ClassPool();
        try {
            classPool.appendClassPath(directory);
        } catch (NotFoundException e) {
            throw new MojoFailureException("Path to classes was not found.", e);
        }

        if (targets == null) {
            throw new MojoExecutionException("Targets configuration parameter must be set.");
        }

        for(AnnotationTarget target : targets) {
            List<AnnotationTarget> list = targetsMap.get(target.getClassName());
            if (list == null) {
                list = new ArrayList<AnnotationTarget>();
                targetsMap.put(target.getClassName(), list);
            }
            list.add(target);
        }

        for (List<AnnotationTarget> list : targetsMap.values()) {
            boolean hasChanges = false;
            CtClass cc = null;
            for(AnnotationTarget target : list) {
                if (target.getClassName() == null || target.getClassName().length() == 0) {
                    getLog().warn("Class skipped because has empty name.");
                    continue;
                }
                if (target.getAnnotation() == null || target.getAnnotation().length() == 0) {
                    getLog().warn("Class '" + target.getClassName() + "' skipped because annotation class name is empty.");
                    continue;
                }

                if (cc == null) {
                    try {
                        cc = classPool.get(target.getClassName());
                    } catch (NotFoundException e) {
                        getLog().error("Failed to load class '" + target.getClassName() + "' for annotation.", e);
                        continue;
                    }
                }

                final ClassFile classFile = cc.getClassFile();
                ConstPool constPool = classFile.getConstPool();
                AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
                final boolean addAttribute = attribute == null;

                if (target.isRemove()) {
                    boolean removed = false;
                    if (!addAttribute) {
                        Annotation[] annotations = attribute.getAnnotations();
                        for (int i = 0; i < annotations.length; i++) {
                            if (annotations[i].getTypeName().equals(target.getAnnotation())) {
                                Annotation[] newAnnotations = new Annotation[annotations.length - 1];
                                System.arraycopy(annotations, 0, newAnnotations, 0, i);
                                System.arraycopy(annotations, i + 1, newAnnotations, i, annotations.length - i - 1);
                                attribute.setAnnotations(newAnnotations);
                                removed = true;
                                hasChanges = true;
                                break;
                            }
                        }
                    }
                    if (!removed) {
                        getLog().warn("Remove failed. Annotation '" + target.getAnnotation() + "' does not exists in class '" + target.getClassName() + "'.");
                    }
                } else {
                    if (addAttribute) {
                        attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    }

                    Annotation annotation = new Annotation(target.getAnnotation(), constPool);
                    if (target.getParameters() != null) {
                        for (Parameter entry : target.getParameters()) {
                            final MemberValue memberValue = convertValue(entry.getValue(), constPool);
                            if (memberValue != null) {
                                annotation.addMemberValue(entry.getName(), memberValue);
                            }
                        }
                    }

                    attribute.addAnnotation(annotation);
                    if (addAttribute) {
                        classFile.addAttribute(attribute);
                    }
                    hasChanges = true;
                }
            }
            if (hasChanges) {
                try {
                    cc.getClassFile().compact();
                    cc.writeFile(directory);
                    getLog().info("Annotation modified successfully for class '" + cc.getName() + "'.");
                } catch (IOException e) {
                    getLog().error("Failed to save class '" + cc.getName() + "' File write error.", e);
                } catch (CannotCompileException e) {
                    getLog().error("Failed to save class '" + cc.getName() + "' Class compilation exception.", e);
                }
            }
        }
    }

    @Nullable
    private static MemberValue convertValue(@Nonnull ParameterValue value, @Nonnull ConstPool constPool) throws MojoFailureException {
        if (value.getType() == ParameterValue.Type.ANNOTATION) {
            final AnnotationValue annotationValue = value.getAnnotation();
            if (annotationValue != null) {
                Annotation annotation = new Annotation(annotationValue.getAnnotation(), constPool);
                if (annotationValue.getParameters() != null) {
                    for (Parameter parameter : annotationValue.getParameters()) {
                        annotation.addMemberValue(parameter.getName(), convertValue(parameter.getValue(), constPool));
                    }
                }
                return new AnnotationMemberValue(annotation, constPool);
            }
        } else if (value.getType() == ParameterValue.Type.ARRAY) {
            final ParameterValue[] array = value.getValues();
            if (array != null) {
                MemberValue[] memberValues = new MemberValue[array.length];
                for (int i = 0; i < array.length; i++) {
                    ParameterValue item = array[i];
                    memberValues[i] = convertValue(item, constPool);
                }
                ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constPool);
                arrayMemberValue.setValue(memberValues);
                return arrayMemberValue;
            }
        } else if (value.getValue() != null) {
            return convertValue(value.getValue(), value.getType(), constPool);
        }
        return null;
    }

    @Nonnull
    private static MemberValue convertValue(@Nonnull String value, @Nonnull ParameterValue.Type type, @Nonnull ConstPool constPool) throws MojoFailureException {
        switch (type) {
            case BOOLEAN:
                return new BooleanMemberValue(Boolean.parseBoolean(value), constPool);
            case BYTE:
                return new ByteMemberValue(Byte.parseByte(value), constPool);
            case CHAR:
                return new CharMemberValue(value.charAt(0), constPool);
            case CLASS:
                return new ClassMemberValue(value, constPool);
            case DOUBLE:
                return new DoubleMemberValue(Double.parseDouble(value), constPool);
            case ENUM: {
                final EnumMemberValue enumMemberValue = new EnumMemberValue(constPool);
                int index = value.lastIndexOf('.');
                if (index <= 0) {
                    throw new MojoFailureException("Enum value must contain full name of enum-class and value separated by '.'");
                }
                enumMemberValue.setType(value.substring(0, index));
                enumMemberValue.setValue(value.substring(index + 1));
                return enumMemberValue;
            }
            case FLOAT:
                return new FloatMemberValue(Float.parseFloat(value), constPool);
            case INTEGER:
                return new IntegerMemberValue(Integer.parseInt(value), constPool);
            case LONG:
                return new LongMemberValue(Long.parseLong(value), constPool);
            case SHORT:
                return new ShortMemberValue(Short.parseShort(value), constPool);
            case STRING:
                return new StringMemberValue(value, constPool);
        }
        throw new MojoFailureException("Unexpected value type '" + type + "'.");
    }
}
