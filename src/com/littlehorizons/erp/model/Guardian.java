package com.littlehorizons.erp.model;

/**
 * Parent or legal tutor responsible for one or more students (table acudientes).
 */
public class Guardian extends Person {

    private String relationship;
    private String primaryPhone;
    private String secondaryPhone;
    private String occupation;
    private boolean financiallyResponsible = true;

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean isFinanciallyResponsible() {
        return financiallyResponsible;
    }

    public void setFinanciallyResponsible(boolean financiallyResponsible) {
        this.financiallyResponsible = financiallyResponsible;
    }
}
