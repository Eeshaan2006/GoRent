package vehiclerental;

/** Base class for every rentable vehicle (Inheritance). */
public abstract class Vehicle {
    private final String regNo;
    private final String model;
    private final double ratePerDay;
    private boolean available = true;

    protected Vehicle(String regNo, String model, double ratePerDay) {
        this.regNo = regNo;
        this.model = model;
        this.ratePerDay = ratePerDay;
    }

    public String getRegNo() { return regNo; }
    public String getModel() { return model; }
    public double getRatePerDay() { return ratePerDay; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    /** Vehicle type name, e.g. Car, Bike, Truck. */
    public abstract String getType();

    /** Each subclass calculates the rent in its own way (Polymorphism). */
    public abstract double calculateRent(int days);

    /** Short text explaining the extra charge / discount of this vehicle type. */
    public abstract String getChargeNote();

    protected double baseRent(int days) {
        return ratePerDay * days;
    }

    protected static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
