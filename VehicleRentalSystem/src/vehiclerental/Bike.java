package vehiclerental;

/** Bike: 10% discount when rented for 7 days or more. */
public class Bike extends Vehicle {
    public Bike(String regNo, String model, double ratePerDay) {
        super(regNo, model, ratePerDay);
    }

    @Override public String getType() { return "Bike"; }

    @Override
    public double calculateRent(int days) {
        double rent = baseRent(days);
        if (days >= 7) rent *= 0.90;
        return round2(rent);
    }

    @Override public String getChargeNote() { return "10% off for 7+ days"; }
}
