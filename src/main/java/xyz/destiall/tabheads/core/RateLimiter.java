package xyz.destiall.tabheads.core;

public class RateLimiter {

    private final long[] requests;
    private final long expireTime;
    private int position;

    public RateLimiter(int maxLimit, long expireTime) {
        this.requests = new long[maxLimit];
        this.expireTime = expireTime;
    }

    public boolean tryAcquire() {
        long now = System.nanoTime() / 1_000_000;
        long toBeExpired = now - expireTime;
        synchronized (this) {
            long oldest = requests[position];
            if (oldest < toBeExpired) {
                requests[position] = now;
                position = (position + 1) % requests.length;
                return true;
            }
            return false;
        }
    }
}
