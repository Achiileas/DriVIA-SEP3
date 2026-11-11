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
import java.time.LocalDate;

@Entity
@Table(name = "vehicle")
public class Vehicle{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String registrationNo;
    private String model;
    private LocalDate productionDate;
    private int horsepower;
    private String category;
    private String gearbox;
    private int pricePerDay;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @ManyToOne
    @JoinColumn( name = "location_id" )
    private Location location;

    // JPA needs an empty constructor
    protected Vehicle(){
    }

    public Vehicle( String registrationNo, String model, LocalDate productionDate,
        int horsepower, String category, String gearbox, int pricePerDay, Location location ){

        this.registrationNo = registrationNo;
        this.model = model;
        this.productionDate = productionDate;
        this.horsepower = horsepower;
        this.category = category;
        this.gearbox = gearbox;
        this.pricePerDay = pricePerDay;
        this.location = location;
        this.status = VehicleStatus.AVAILABLE;
    }

    // Only these statuses can get new reservations
    public boolean canBeReserved() {
        return status == VehicleStatus.AVAILABLE || status == VehicleStatus.RENTED;
    }

    public int getId() {
        return id;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public int getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(int horsepower) {
        this.horsepower = horsepower;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGearbox() {
        return gearbox;
    }

    public void setGearbox(String gearbox) {
        this.gearbox = gearbox;
    }

    public int getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(int pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}