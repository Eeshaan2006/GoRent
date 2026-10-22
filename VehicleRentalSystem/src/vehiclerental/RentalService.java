package vehiclerental;

import java.util.ArrayList;
import java.util.List;

/** All business rules live here. Data is kept in memory. */
public class RentalService {
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();

    public RentalService() {
        vehicles.add(new Car("TS09AB1234", "Maruti Swift", 1800));
        vehicles.add(new Car("TS10CD5678", "Hyundai Creta", 2800));
        vehicles.add(new Car("AP28EF9012", "Toyota Innova Crysta", 3500));
        vehicles.add(new Car("TS07GH3456", "Honda City", 2400));
        vehicles.add(new Car("KA01JK7788", "Tata Nexon EV", 3000));
        vehicles.add(new Bike("TS08MN1122", "Royal Enfield Classic 350", 900));
        vehicles.add(new Bike("TS11PQ3344", "Honda Activa 6G", 500));
        vehicles.add(new Bike("AP39RS5566", "Yamaha R15", 800));
        vehicles.add(new Bike("TS12TU7788", "KTM Duke 200", 850));
        vehicles.add(new Truck("TS13VW9900", "Tata Ace", 4500));
        vehicles.add(new Truck("AP16XY2211", "Mahindra Bolero Pickup", 5200));
        vehicles.add(new Truck("TS14ZA4433", "Eicher Pro 2049", 6000));
    }

    public synchronized List<Vehicle> getVehicles() { return new ArrayList<>(vehicles); }
    public synchronized List<Rental> getRentals() { return new ArrayList<>(rentals); }

    private Vehicle find(String regNo) {
        for (Vehicle v : vehicles) {
            if (v.getRegNo().equalsIgnoreCase(regNo)) return v;
        }
        return null;
    }

    public synchronized Vehicle addVehicle(String regNo, String model, String type, double rate) {
        regNo = regNo == null ? "" : regNo.trim().toUpperCase();
        model = model == null ? "" : model.trim();
        if (regNo.isEmpty() || model.isEmpty()) throw new IllegalArgumentException("Registration number and model are required.");
        if (rate <= 0) throw new IllegalArgumentException("Rental rate must be greater than zero.");
        if (find(regNo) != null) throw new IllegalArgumentException("A vehicle with this registration number already exists.");
        Vehicle v;
        switch (type == null ? "" : type) {
            case "Car":   v = new Car(regNo, model, rate); break;
            case "Bike":  v = new Bike(regNo, model, rate); break;
            case "Truck": v = new Truck(regNo, model, rate); break;
            default: throw new IllegalArgumentException("Type must be Car, Bike or Truck.");
        }
        vehicles.add(v);
        return v;
    }

    public synchronized Vehicle getVehicle(String regNo) {
        Vehicle v = find(regNo);
        if (v == null) throw new IllegalArgumentException("Vehicle not found.");
        return v;
    }

    public synchronized Rental rent(String regNo, String customer, String phone, int days) {
        Vehicle v = getVehicle(regNo);
        customer = customer == null ? "" : customer.trim();
        phone = phone == null ? "" : phone.trim();
        if (customer.isEmpty()) throw new IllegalArgumentException("Customer name is required.");
        if (!phone.matches("\\d{10}")) throw new IllegalArgumentException("Enter a valid 10-digit phone number.");
        if (days < 1 || days > 90) throw new IllegalArgumentException("Days must be between 1 and 90.");
        if (!v.isAvailable()) throw new IllegalStateException("Sorry, this vehicle is already rented.");
        v.setAvailable(false);
        Rental r = new Rental(v, customer, phone, days);
        rentals.add(0, r);
        return r;
    }

    public synchronized Rental returnVehicle(String regNo) {
        Vehicle v = getVehicle(regNo);
        for (Rental r : rentals) {
            if (r.isActive() && r.getVehicle() == v) {
                r.markReturned();
                v.setAvailable(true);
                return r;
            }
        }
        throw new IllegalStateException("This vehicle is not currently rented.");
    }
}
