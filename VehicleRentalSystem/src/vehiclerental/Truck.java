package vehiclerental;

/** Truck: a flat loading fee is added to the rent. */
public class Truck extends Vehicle {
    public static final double LOADING_FEE = 500;

    public Truck(String regNo, String model, double ratePerDay) {
        super(regNo, model, ratePerDay);
    }

    @Override public String getType() { return "Truck"; }

    @Override
    public double calculateRent(int days) {
        return round2(baseRent(days) + LOADING_FEE);
    }

    @Override public String getChargeNote() { return "Includes flat loading fee of 500"; }
}
