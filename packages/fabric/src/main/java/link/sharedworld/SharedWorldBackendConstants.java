package link.sharedworld;

public final class SharedWorldBackendConstants {
    /**
     * 0.4.6: the self-hosted backend (lane D). Releases up to 0.4.5 carry the
     * Cloudflare worker address, which keeps serving them as a forwarder to
     * this host; the worker also stays the download relay for everyone.
     */
    public static final String DEFAULT_BASE_URL = "https://peerworld-server.onrender.com";
    public static final String BACKEND_URL_SYSTEM_PROPERTY = "sharedworld.backendUrl";
    public static final String PEERWORLD_BACKEND_URL_SYSTEM_PROPERTY = "peerworld.backendUrl";

    private SharedWorldBackendConstants() {
    }
}
