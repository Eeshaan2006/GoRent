package vehiclerental;

/** Car: 10% insurance is added to the rent. */
public class Car extends Vehicle {
    public Car(String regNo, String model, double ratePerDay) {
        super(regNo, model, ratePerDay);
    }

    @Override public String getType() { return "Car"; }

    @Override
    public double calculateRent(int days) {
        return round2(baseRent(days) * 1.10);
    }

    @Override public String getChargeNote() { return "Includes 10% insurance"; }
}
