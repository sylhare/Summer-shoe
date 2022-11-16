package sun.flower.cache;

public class MemcachedClientConfiguration {
    protected final String name;

    protected final String address;

    protected final int expiration;

    public MemcachedClientConfiguration(String name, String address, int expiration) {
        this.name = name;
        this.address = address;
        this.expiration = expiration;
    }
}
