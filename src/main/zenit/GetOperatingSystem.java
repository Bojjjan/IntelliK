package main.zenit;

public class GetOperatingSystem {

    /**
     * Enum representing supported operating systems.
     */
    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX
    }

    /**
     * Detects the operating system on which the application is running.
     *
     * @author Philip Boyde
     * @return The detected operating system as an instance of the OperatingSystem enum.
     *         Returns OperatingSystem.WINDOWS if the OS is Windows,
     *         OperatingSystem.MAC if the OS is macOS,
     *         OperatingSystem.LINUX if the OS is a Unix or Linux variant,
     *  @throws UnsupportedOperationException if the OS is not supported.
     */
    protected static OperatingSystem detectOS(){
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;

        } else if (osName.contains("mac")) {
            return OperatingSystem.MAC;

        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OperatingSystem.LINUX;

        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + osName);
        }
    }
}
