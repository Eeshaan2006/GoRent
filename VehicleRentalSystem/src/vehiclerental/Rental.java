package vehiclerental;

import java.time.LocalDateTime;

/** A single rental record (also used to print the receipt). */
public class Rental {
    private static int counter = 1000;

    private final int id;
    private final Vehicle vehicle;
    private final String customer;
    private final String phone;
    private final int days;
    private final double base;
    private final double amount;
    private final LocalDateTime rentedAt;
    private LocalDateTime returnedAt;

    public Rental(Vehicle vehicle, String customer, String phone, int days) {
        this.id = ++counter;
        this.vehicle = vehicle;
        this.customer = customer;
        this.phone = phone;
        this.days = days;
        this.base = vehicle.getRatePerDay() * days;
        this.amount = vehicle.calculateRent(days);
        this.rentedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public String getCustomer() { return customer; }
    public String getPhone() { return phone; }
    public int getDays() { return days; }
    public double getBase() { return base; }
    public double getAmount() { return amount; }
    public LocalDateTime getRentedAt() { return rentedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public boolean isActive() { return returnedAt == null; }
    public void markReturned() { this.returnedAt = LocalDateTime.now(); }
}
