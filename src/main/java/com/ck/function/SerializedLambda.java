package com.ck.function;


import java.io.Serializable;

/**
     * 这个类是从 {@link java.lang.invoke.SerializedLambda} 里面 copy 过来的，
     * 字段信息完全一样
     * <p>负责将一个支持序列的 Function 序列化为 SerializedLambda</p>
     *
     * @author HCL
     * @since 2018/05/10
     */
    @SuppressWarnings("unused")
    public class SerializedLambda implements Serializable {

        private static final long serialVersionUID = 8025925345765570181L;

        private Class<?> capturingClass;
        private String functionalInterfaceClass;
        private String functionalInterfaceMethodName;
        private String functionalInterfaceMethodSignature;
        private String implClass;
        private String implMethodName;
        private String implMethodSignature;
        private int implMethodKind;
        private String instantiatedMethodType;
        private Object[] capturedArgs;

    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    public void setCapturingClass(Class<?> capturingClass) {
        this.capturingClass = capturingClass;
    }

    public String getFunctionalInterfaceClass() {
        return functionalInterfaceClass;
    }

    public void setFunctionalInterfaceClass(String functionalInterfaceClass) {
        this.functionalInterfaceClass = functionalInterfaceClass;
    }

    public String getFunctionalInterfaceMethodName() {
        return functionalInterfaceMethodName;
    }

    public void setFunctionalInterfaceMethodName(String functionalInterfaceMethodName) {
        this.functionalInterfaceMethodName = functionalInterfaceMethodName;
    }

    public String getFunctionalInterfaceMethodSignature() {
        return functionalInterfaceMethodSignature;
    }

    public void setFunctionalInterfaceMethodSignature(String functionalInterfaceMethodSignature) {
        this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public String getImplMethodName() {
        return implMethodName;
    }

    public void setImplMethodName(String implMethodName) {
        this.implMethodName = implMethodName;
    }

    public String getImplMethodSignature() {
        return implMethodSignature;
    }

    public void setImplMethodSignature(String implMethodSignature) {
        this.implMethodSignature = implMethodSignature;
    }

    public int getImplMethodKind() {
        return implMethodKind;
    }

    public void setImplMethodKind(int implMethodKind) {
        this.implMethodKind = implMethodKind;
    }

    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    public void setInstantiatedMethodType(String instantiatedMethodType) {
        this.instantiatedMethodType = instantiatedMethodType;
    }

    public Object[] getCapturedArgs() {
        return capturedArgs;
    }

    public void setCapturedArgs(Object[] capturedArgs) {
        this.capturedArgs = capturedArgs;
    }
}