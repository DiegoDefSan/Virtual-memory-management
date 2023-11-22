package configuration;

public class Configuration {
    public static final int PRIMARY_MEMORY_SIZE = 32; // 2^5
    public static final int SECONDARY_MEMORY_SIZE = 64; // 2^6
    public static final int PAGE_SIZE = 8; // 2^3
    public static final int FRAME_SIZE = 8; // 2^3
    public static final int LOGICAL_ADDRESS_BITS = 5;

    public static final int LOGICAL_ADDRESS_SIZE = (int) Math.pow(2, LOGICAL_ADDRESS_BITS);
    public static final int NUMBER_OF_FRAMES_PRIMARY_MEMORY = PRIMARY_MEMORY_SIZE / FRAME_SIZE;
    public static final int NUMBER_OF_FRAMES_SECONDARY_MEMORY = SECONDARY_MEMORY_SIZE / FRAME_SIZE;
    public static final int BITS_PAGE_NUMBER = 2;
    public static final int BITS_OFFSET = 3;
}
