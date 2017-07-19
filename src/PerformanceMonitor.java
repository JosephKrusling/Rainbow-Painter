/**
 * Created by josep on 7/7/2017.
 */
public class PerformanceMonitor {

    private long startTime;
    private long lastCheckpoint;

    public PerformanceMonitor() {
    }

    public void start() {
        startTime = System.currentTimeMillis();
        lastCheckpoint = startTime;
    }

    public long checkPoint() {
        long thisCheckpoint = System.currentTimeMillis();
        long deltaTime = thisCheckpoint - lastCheckpoint;
        lastCheckpoint = thisCheckpoint;
        return deltaTime;
    }

    public long stop() {
        return System.currentTimeMillis() - startTime;
    }
}
