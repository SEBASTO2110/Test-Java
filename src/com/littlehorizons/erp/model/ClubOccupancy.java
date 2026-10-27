package com.littlehorizons.erp.model;

/**
 * Row of the club occupancy report.
 */
public class ClubOccupancy {
    private String clubName;
    private String supervisorName;
    private int activeMembers;
    private int maxCapacity;

    public ClubOccupancy(String clubName, String supervisorName, int activeMembers, int maxCapacity) {
        this.clubName = clubName;
        this.supervisorName = supervisorName;
        this.activeMembers = activeMembers;
        this.maxCapacity = maxCapacity;
    }

    public String getClubName() {
        return clubName;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public int getActiveMembers() {
        return activeMembers;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public double getOccupancyPercentage() {
        if (maxCapacity == 0) {
            return 0;
        }
        return activeMembers * 100.0 / maxCapacity;
    }
}
