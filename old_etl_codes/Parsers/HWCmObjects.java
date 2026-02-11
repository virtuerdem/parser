
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

public class HWCmObjects {
    
    private boolean actStatus;
    private String objectID;
    private String objectName;
    private String objectParentID;
    private int objectType;
    private String dataDate;
    private String topNEID;
    private String tempDescription;

    public boolean isActStatus() {
        return actStatus;
    }

    public String getTempDescription() {
        return tempDescription;
    }

    public void setTempDescription(String tempDescription) {
        this.tempDescription = tempDescription;
    }

    public void setActStatus(boolean actStatus) {
        this.actStatus = actStatus;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectParentID() {
        return objectParentID;
    }

    public void setObjectParentID(String objectParentID) {
        this.objectParentID = objectParentID;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public String getTopNEID() {
        return topNEID;
    }

    public void setTopNEID(String topNEID) {
        this.topNEID = topNEID;
    }
}
