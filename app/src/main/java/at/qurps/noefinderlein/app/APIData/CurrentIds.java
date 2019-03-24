package at.qurps.noefinderlein.app.APIData;

public class CurrentIds {
    private int changeid;
    private int daysChngId;
    private int daysChangeCount;

    public int getChangeId() {
        return changeid;
    }

    public void setChangeId(int changeId) {
        this.changeid = changeId;
    }

    public int getDaysChangeId() {
        return daysChngId;
    }

    public void setDaysChangeId(int daysChangeId) {
        this.daysChngId = daysChangeId;
    }

    public int getDaysChangeCount() {
        return daysChangeCount;
    }

    public void setDaysChangeCount(int daysChangeCount) {
        this.daysChangeCount = daysChangeCount;
    }
}
