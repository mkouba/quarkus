/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkus.arc.processor;

import io.quarkus.arc.GenericArrayTypeImpl;
import io.quarkus.arc.ParameterizedTypeImpl;
import io.quarkus.arc.TypeVariableImpl;
import io.quarkus.arc.WildcardTypeImpl;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType.Primitive;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;

/**
 *
 * @author Martin Kouba
 */
final class Types {

    private static final Type OBJECT_TYPE = Type.create(DotNames.OBJECT, Kind.CLASS);

    private Types() {
    }

    static ResultHandle getTypeHandle(BytecodeCreator creator, Type type) {
        if (Kind.CLASS.equals(type.kind())) {
            return creator.loadClass(type.asClassType().name().toString());
        } else if (Kind.TYPE_VARIABLE.equals(type.kind())) {
            // E.g. T -> new TypeVariableImpl("T")
            TypeVariable typeVariable = type.asTypeVariable();
            ResultHandle boundsHandle;
            List<Type> bounds = typeVariable.bounds();
            if (bounds.isEmpty()) {
                boundsHandle = creator.newArray(java.lang.reflect.Type.class, creator.load(0));
            } else {
                boundsHandle = creator.newArray(java.lang.reflect.Type.class, creator.load(bounds.size()));
                for (int i = 0; i < bounds.size(); i++) {
                    creator.writeArrayValue(boundsHandle, i, getTypeHandle(creator, bounds.get(i)));
                }
            }
            return creator.newInstance(
                    MethodDescriptor.ofConstructor(TypeVariableImpl.class, String.class, java.lang.reflect.Type[].class),
                    creator.load(typeVariable.identifier()), boundsHandle);

        } else if (Kind.PARAMETERIZED_TYPE.equals(type.kind())) {
            // E.g. List<String> -> new ParameterizedTypeImpl(List.class, String.class)
            ParameterizedType parameterizedType = type.asParameterizedType();

            List<Type> arguments = parameterizedType.arguments();
            ResultHandle typeArgsHandle = creator.newArray(java.lang.reflect.Type.class, creator.load(arguments.size()));
            for (int i = 0; i < arguments.size(); i++) {
                creator.writeArrayValue(typeArgsHandle, i, getTypeHandle(creator, arguments.get(i)));
            }
            return creator.newInstance(
                    MethodDescriptor.ofConstructor(ParameterizedTypeImpl.class, java.lang.reflect.Type.class,
                            java.lang.reflect.Type[].class),
                    creator.loadClass(parameterizedType.name().toString()), typeArgsHandle);

        } else if (Kind.ARRAY.equals(type.kind())) {
            Type componentType = type.asArrayType().component();
            // E.g. String[] -> new GenericArrayTypeImpl(String.class)
            return creator.newInstance(MethodDescriptor.ofConstructor(GenericArrayTypeImpl.class, java.lang.reflect.Type.class),
                    getTypeHandle(creator, componentType));

        } else if (Kind.WILDCARD_TYPE.equals(type.kind())) {
            // E.g. ? extends Number -> WildcardTypeImpl.withUpperBound(Number.class)
            WildcardType wildcardType = type.asWildcardType();

            if (wildcardType.superBound() == null) {
                return creator.invokeStaticMethod(
                        MethodDescriptor.ofMethod(WildcardTypeImpl.class, "withUpperBound",
                                java.lang.reflect.WildcardType.class, java.lang.reflect.Type.class),
                        getTypeHandle(creator, wildcardType.extendsBound()));
            } else {
                return creator.invokeStaticMethod(
                        MethodDescriptor.ofMethod(WildcardTypeImpl.class, "withLowerBound",
                                java.lang.reflect.WildcardType.class, java.lang.reflect.Type.class),
                        getTypeHandle(creator, wildcardType.superBound()));
            }
        } else if (Kind.PRIMITIVE.equals(type.kind())) {
            switch (type.asPrimitiveType().primitive()) {
                case INT:
                    return creator.loadClass(int.class);
                case LONG:
                    return creator.loadClass(long.class);
                case BOOLEAN:
                    return creator.loadClass(boolean.class);
                case BYTE:
                    return creator.loadClass(byte.class);
                case CHAR:
                    return creator.loadClass(char.class);
                case DOUBLE:
                    return creator.loadClass(double.class);
                case FLOAT:
                    return creator.loadClass(float.class);
                case SHORT:
                    return creator.loadClass(short.class);
                default:
                    throw new IllegalArgumentException("Unsupported primitive type: " + type);
            }
        } else {
            throw new IllegalArgumentException("Unsupported bean type: " + type.kind() + ", " + type);
        }
    }

    static Type getProviderType(ClassInfo classInfo) {
        List<TypeVariable> typeParameters = classInfo.typeParameters();
        if (!typeParameters.isEmpty()) {
            return ParameterizedType.create(classInfo.name(), typeParameters.toArray(new Type[] {}), null);
        } else {
            return Type.create(classInfo.name(), Kind.CLASS);
        }
    }

    static Set<Type> getProducerMethodTypeClosure(MethodInfo producerMethod, BeanDeployment beanDeployment) {
        Set<Type> types;
        Type returnType = producerMethod.returnType();
        if (returnType.kind() == Kind.PRIMITIVE || returnType.kind() == Kind.ARRAY) {
            types = new HashSet<>();
            types.add(returnType);
            types.add(OBJECT_TYPE);
            return types;
        } else {
            ClassInfo returnTypeClassInfo = beanDeployment.getIndex().getClassByName(returnType.name());
            if (returnTypeClassInfo == null) {
                throw new IllegalArgumentException(
                        "Producer method return type not found in index: " + producerMethod.returnType().name());
            }
            if (Kind.CLASS.equals(returnType.kind())) {
                types = getTypeClosure(returnTypeClassInfo, Collections.emptyMap(), beanDeployment, null);
            } else if (Kind.PARAMETERIZED_TYPE.equals(returnType.kind())) {
                types = getTypeClosure(returnTypeClassInfo,
                        buildResolvedMap(returnType.asParameterizedType().arguments(), returnTypeClassInfo.typeParameters(),
                                Collections.emptyMap()),
                        beanDeployment, null);
            } else {
                throw new IllegalArgumentException("Unsupported return type");
            }
        }
        return restrictBeanTypes(types, beanDeployment.getAnnotations(producerMethod));
    }

    static Set<Type> getProducerFieldTypeClosure(FieldInfo producerField, BeanDeployment beanDeployment) {
        Set<Type> types;
        Type fieldType = producerField.type();
        if (fieldType.kind() == Kind.PRIMITIVE || fieldType.kind() == Kind.ARRAY) {
            types = new HashSet<>();
            types.add(fieldType);
            types.add(OBJECT_TYPE);
        } else {
            ClassInfo fieldClassInfo = beanDeployment.getIndex().getClassByName(producerField.type().name());
            if (fieldClassInfo == null) {
                throw new IllegalArgumentException("Producer field type not found in index: " + producerField.type().name());
            }
            if (Kind.CLASS.equals(fieldType.kind())) {
                types = getTypeClosure(fieldClassInfo, Collections.emptyMap(), beanDeployment, null);
            } else if (Kind.PARAMETERIZED_TYPE.equals(fieldType.kind())) {
                types = getTypeClosure(fieldClassInfo,
                        buildResolvedMap(fieldType.asParameterizedType().arguments(), fieldClassInfo.typeParameters(),
                                Collections.emptyMap()),
                        beanDeployment, null);
            } else {
                throw new IllegalArgumentException("Unsupported return type");
            }
        }
        return restrictBeanTypes(types, beanDeployment.getAnnotations(producerField));
    }

    static Set<Type> getClassBeanTypeClosure(ClassInfo classInfo, Map<TypeVariable, Type> resolvedTypeParameters,
            BeanDeployment beanDeployment) {
        return restrictBeanTypes(getTypeClosure(classInfo, resolvedTypeParameters, beanDeployment, null),
                beanDeployment.getAnnotations(classInfo));
    }

    static Set<Type> getTypeClosure(ClassInfo classInfo, Map<TypeVariable, Type> resolvedTypeParameters,
            BeanDeployment beanDeployment, BiConsumer<ClassInfo, Map<TypeVariable, Type>> resolvedTypeVariablesConsumer) {
        Set<Type> types = new HashSet<>();
        List<TypeVariable> typeParameters = classInfo.typeParameters();
        if (!typeParameters.isEmpty()) {
            // Canonical ParameterizedType with unresolved type variables
            Type[] typeParams = new Type[typeParameters.size()];
            for (int i = 0; i < typeParameters.size(); i++) {
                TypeVariable paramType = typeParameters.get(i);
                Type resolvedType = resolvedTypeParameters.get(paramType);
                if (resolvedType == null) {
                    resolvedType = paramType.bounds().get(0);
                }
                typeParams[i] = resolvedType;
            }
            if (resolvedTypeVariablesConsumer != null) {
                Map<TypeVariable, Type> resolved = new HashMap<>();
                for (int i = 0; i < typeParameters.size(); i++) {
                    resolved.put(typeParameters.get(i), typeParams[i]);
                }
                resolvedTypeVariablesConsumer.accept(classInfo, resolved);
            }
            types.add(ParameterizedType.create(classInfo.name(), typeParams, null));
        } else {
            types.add(Type.create(classInfo.name(), Kind.CLASS));
        }
        // Interfaces
        for (Type interfaceType : classInfo.interfaceTypes()) {
            ClassInfo interfaceClassInfo = beanDeployment.getIndex().getClassByName(interfaceType.name());
            if (interfaceClassInfo != null) {
                Map<TypeVariable, Type> resolved = Collections.emptyMap();
                if (Kind.PARAMETERIZED_TYPE.equals(interfaceType.kind())) {
                    resolved = buildResolvedMap(interfaceType.asParameterizedType().arguments(),
                            interfaceClassInfo.typeParameters(), resolvedTypeParameters);
                }
                types.addAll(getTypeClosure(interfaceClassInfo, resolved, beanDeployment, resolvedTypeVariablesConsumer));
            }
        }
        // Superclass
        if (classInfo.superClassType() != null) {
            ClassInfo superClassInfo = beanDeployment.getIndex().getClassByName(classInfo.superName());
            if (superClassInfo != null) {
                Map<TypeVariable, Type> resolved = Collections.emptyMap();
                if (Kind.PARAMETERIZED_TYPE.equals(classInfo.superClassType().kind())) {
                    resolved = buildResolvedMap(classInfo.superClassType().asParameterizedType().arguments(),
                            superClassInfo.typeParameters(),
                            resolvedTypeParameters);
                }
                types.addAll(getTypeClosure(superClassInfo, resolved, beanDeployment, resolvedTypeVariablesConsumer));
            }
        }
        return types;
    }

    static Set<Type> restrictBeanTypes(Set<Type> types, Collection<AnnotationInstance> annotations) {
        AnnotationInstance typed = annotations.stream().filter(a -> a.name().equals(DotNames.TYPED))
                .findFirst().orElse(null);
        if (typed != null) {
            AnnotationValue typedValue = typed.value();
            if (typedValue == null) {
                types.clear();
                types.add(OBJECT_TYPE);
            } else {
                Set<DotName> typedClasses = new HashSet<>();
                for (Type type : typedValue.asClassArray()) {
                    typedClasses.add(type.name());
                }
                for (Iterator<Type> iterator = types.iterator(); iterator.hasNext();) {
                    if (!typedClasses.contains(iterator.next().name())) {
                        iterator.remove();
                    }
                }
            }
        }
        return types;
    }

    static Map<TypeVariable, Type> buildResolvedMap(List<Type> resolvedTypeVariables, List<TypeVariable> typeVariables,
            Map<TypeVariable, Type> resolvedTypeParameters) {
        Map<TypeVariable, Type> resolvedMap = new HashMap<>();
        for (int i = 0; i < resolvedTypeVariables.size(); i++) {
            Type resolvedTypeVariable = resolvedTypeVariables.get(i);
            Type resolvedTypeParam = Kind.TYPE_VARIABLE.equals(resolvedTypeVariable.kind())
                    ? resolvedTypeParameters.getOrDefault(resolvedTypeVariable, resolvedTypeVariable)
                    : resolvedTypeVariable;
            resolvedMap.put(typeVariables.get(i), resolvedTypeParam);
        }
        return resolvedMap;
    }

    static String getPackageName(String className) {
        className = className.replace('/', '.');
        return className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "";
    }

    static String getSimpleName(String className) {
        return className.contains(".") ? className.substring(className.lastIndexOf(".") + 1, className.length()) : className;
    }

    static Type box(Type type) {
        if (type.kind() == Kind.PRIMITIVE) {
            return box(type.asPrimitiveType().primitive());
        }
        return type;
    }

    static Type box(Primitive primitive) {
        switch (primitive) {
            case BOOLEAN:
                return Type.create(DotNames.BOOLEAN, Kind.CLASS);
            case DOUBLE:
                return Type.create(DotNames.DOUBLE, Kind.CLASS);
            case FLOAT:
                return Type.create(DotNames.FLOAT, Kind.CLASS);
            case LONG:
                return Type.create(DotNames.LONG, Kind.CLASS);
            case INT:
                return Type.create(DotNames.INTEGER, Kind.CLASS);
            case BYTE:
                return Type.create(DotNames.BYTE, Kind.CLASS);
            case CHAR:
                return Type.create(DotNames.CHARACTER, Kind.CLASS);
            case SHORT:
                return Type.create(DotNames.SHORT, Kind.CLASS);
            default:
                throw new IllegalArgumentException("Unsupported primitive: " + primitive);
        }
    }

}
