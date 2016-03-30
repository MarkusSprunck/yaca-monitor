package com.sw_engineering_candies.yaca;

public class Node {

    private String packageName;

    private String className;

    private String methodName;

    private boolean newItem;

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

    @Override
    public String toString() {
	return "Entry [" + packageName + ", " + className + ", " + methodName + "]";
    }

    public void setMethodName(final String methodName) {
	// Assertions - start
	assert methodName != null : "Null is not allowed";
	assert !methodName.isEmpty() : "Empty is not allowed";
	// Assertions - end
	this.methodName = methodName;
    }

    public void setClassName(final String className) {
	// Assertions - start
	assert className != null : "Null is not allowed";
	assert !className.isEmpty() : "Empty is not allowed";
	// Assertions - end
	this.className = className;
    }

    public void setPackageName(final String packageName) {
	// Assertions - start
	assert packageName != null : "Null is not allowed";
	assert !packageName.isEmpty() : "Empty is not allowed";
	// Assertions - end
	this.packageName = packageName;
    }
}
