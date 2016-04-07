package com.sw_engineering_candies.yaca;

public class Node {
    
    /**
     * Attributes
     */
    private String packageName;
    
    private String className;
    
    private String methodName;
    
    private boolean newItem;
    
    /**
     * Methods
     */
    public boolean isNewItem() {
        return newItem;
    }
    
    public void setNewItem(boolean newItem) {
        this.newItem = newItem;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(final String methodName) {
        this.methodName = methodName;
    }
    
    public void setClassName(final String className) {
        this.className = className;
    }
    
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }
    
    @Override
    public String toString() {
        return "Entry [" + packageName + ", " + className + ", " + methodName + "]";
    }
    
}
