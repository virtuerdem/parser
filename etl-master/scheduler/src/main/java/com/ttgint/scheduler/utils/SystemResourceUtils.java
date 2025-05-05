package com.ttgint.scheduler.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SystemResourceUtils {

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem operatingSystem;

    public SystemResourceUtils() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.operatingSystem = systemInfo.getOperatingSystem();
    }

    public double getCpuUsage() {
        CentralProcessor processor = hardware.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        return Math.round(cpuUsage * 100.0) / 100.0;
    }

    public double getMemoryUsage() {
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        double memoryUsage = ((totalMemory - availableMemory) * 100.0) / totalMemory;
        return Math.round(memoryUsage * 100.0) / 100.0;
    }

    public double getSwapUsage() {
        GlobalMemory memory = hardware.getMemory();
        long totalSwap = memory.getVirtualMemory().getSwapTotal();
        long usedSwap = memory.getVirtualMemory().getSwapUsed();
        if (totalSwap == 0) return 0.0;
        double swapUsage = (usedSwap * 100.0) / totalSwap;
        return Math.round(swapUsage * 100.0) / 100.0;
    }

    public double[] getLoadAverages() {
        CentralProcessor processor = hardware.getProcessor();
        double[] loadAverages = processor.getSystemLoadAverage(3);
        for (int i = 0; i < loadAverages.length; i++) {
            if (loadAverages[i] < 0) loadAverages[i] = 0;
        }
        return loadAverages;
    }

    public String getUptime() {
        long seconds = operatingSystem.getSystemUptime();
        Duration duration = Duration.ofSeconds(seconds);
        return String.format("%d gün, %d saat, %d dakika, %d saniye",
                duration.toDays(),
                duration.toHours() % 24,
                duration.toMinutes() % 60,
                duration.getSeconds() % 60);
    }

    public double getDiskUsage() {
        List<OSFileStore> fileStores = operatingSystem.getFileSystem().getFileStores();

        long totalUsed = 0;
        long totalSpace = 0;

        for (OSFileStore fs : fileStores) {
            // Sadece fiziksel diskleri filtrele
            if (!isVirtualFileSystem(fs) && fs.getTotalSpace() > 0) {
                totalUsed += (fs.getTotalSpace() - fs.getFreeSpace());
                totalSpace += fs.getTotalSpace();
            }
        }

        return totalSpace > 0 ? Math.round((totalUsed * 100.0 / totalSpace) * 100.0) / 100.0 : 0.0;
    }

    private boolean isVirtualFileSystem(OSFileStore fs) {
        // Sanal dosya sistemlerini filtrele
        String type = fs.getType().toLowerCase();
        String mount = fs.getMount().toLowerCase();

        return type.contains("tmpfs") ||
                type.contains("ramfs") ||
                type.contains("efivarfs") ||
                mount.contains("/dev/shm") ||
                mount.contains("/run") ||
                mount.contains("/sys/") ||
                mount.contains("/proc") ||
                mount.contains("/var/run") ||
                type.contains("network") ||     // Windows Ağ sürücüleri
                type.startsWith("\\\\") ||      // Windows UNC yolları
                mount.contains("/volumes/") ||  // MAC Harici diskler
                type.contains("autofs");        // MAC Otomatik mount edilenler
    }

}