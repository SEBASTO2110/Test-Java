package com.littlehorizons.erp.model;

/**
 * Row of the competition ranking: how many 1st, 2nd and 3rd places a student has.
 */
public class StudentPodiumRanking {
    private int studentId;
    private String studentName;
    private long firstPlaces;
    private long secondPlaces;
    private long thirdPlaces;

    public StudentPodiumRanking(int studentId, String studentName, long firstPlaces, long secondPlaces, long thirdPlaces) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.firstPlaces = firstPlaces;
        this.secondPlaces = secondPlaces;
        this.thirdPlaces = thirdPlaces;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public long getFirstPlaces() {
        return firstPlaces;
    }

    public long getSecondPlaces() {
        return secondPlaces;
    }

    public long getThirdPlaces() {
        return thirdPlaces;
    }

    public long getTotalPodiums() {
        return firstPlaces + secondPlaces + thirdPlaces;
    }

    /**
     * Score used to sort the ranking: gold = 3, silver = 2, bronze = 1.
     */
    public long getScore() {
        return firstPlaces * 3 + secondPlaces * 2 + thirdPlaces;
    }
}
