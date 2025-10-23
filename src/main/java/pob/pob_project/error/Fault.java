package pob.pob_project.error;

public class Fault {
    private ErrorType type;
    private boolean active;
    private long startTime;
    private  long endTime;

    public Fault(ErrorType type) {
        this.type = type;
        this.active = true;
        this.startTime = System.currentTimeMillis();
    }

    public void deactivate() {
        this.active = false;
        this.endTime = System.currentTimeMillis();
    }

    public ErrorType getType() { return type; }
    public boolean isActive() { return active; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
}
