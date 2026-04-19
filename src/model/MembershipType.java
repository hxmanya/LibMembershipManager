package model;

public enum MembershipType {
    BASIC("Basic", 30, 1),
    STANDARD("Standard", 60, 3),
    PREMIUM("Premium", 100, 6);

    private final String label;
    private final double annualFee;
    private final int bookBorrowLimit;

    MembershipType(String label, double annualFee, int bookBorrowLimit) {
        this.label = label;
        this.annualFee = annualFee;
        this.bookBorrowLimit = bookBorrowLimit;
    }

    public String getLabel()         { return label; }
    public double getAnnualFee()     { return annualFee; }
    public int getBookBorrowLimit()  { return bookBorrowLimit; }

    @Override
    public String toString() { return label; }
}
