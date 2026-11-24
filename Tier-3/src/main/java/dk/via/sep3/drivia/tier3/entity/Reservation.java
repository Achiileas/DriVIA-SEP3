package dk.via.sep3.drivia.tier3.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table( name = "reservation" )
public class Reservation{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn( name = "customer_id" )
    private Customer customer;

    @ManyToOne
    @JoinColumn( name = "vehicle_id" )
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn( name = "location_id" )
    private Location location;

    private LocalDateTime pickupDate;
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private int totalPrice;

    // JPA needs an empty constructor
    protected Reservation(){
    }

    public Reservation( Customer customer, Vehicle vehicle, LocalDateTime pickupDate, LocalDateTime returnDate, int totalPrice ){
        this.customer = customer;
        this.vehicle = vehicle;
        this.location = vehicle.getLocation();
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getCustomerId() {
        return customer.getId();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle( Vehicle vehicle ){
        this.vehicle = vehicle;
        this.location = vehicle.getLocation();
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDateTime pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
