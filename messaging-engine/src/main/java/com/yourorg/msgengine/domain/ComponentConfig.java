package com.yourorg.msgengine.domain;

public class ComponentConfig {
    private String componentId;
    private String componentName;
    private String componentType;
    private String configScope;
    private int configVersion;
    private String configState;
    private String configPayload;
    private String dependsOn;
    private String description;

    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }

    public String getConfigScope() { return configScope; }
    public void setConfigScope(String configScope) { this.configScope = configScope; }

    public int getConfigVersion() { return configVersion; }
    public void setConfigVersion(int configVersion) { this.configVersion = configVersion; }

    public String getConfigState() { return configState; }
    public void setConfigState(String configState) { this.configState = configState; }

    public String getConfigPayload() { return configPayload; }
    public void setConfigPayload(String configPayload) { this.configPayload = configPayload; }

    public String getDependsOn() { return dependsOn; }
    public void setDependsOn(String dependsOn) { this.dependsOn = dependsOn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
