package dk.via.sep3.drivia.tier3.vehicle.entity;

public enum VehicleStatus {
    AVAILABLE,   // at the location, can be rented
    RENTED,      // a customer has it right now
    UNAVAILABLE, // temporarily out of use, cannot be reserved
    REMOVED      // no longer in the fleet, hidden from customers
}

