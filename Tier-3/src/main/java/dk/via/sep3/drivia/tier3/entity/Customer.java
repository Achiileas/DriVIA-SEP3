package dk.via.sep3.drivia.tier3.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table( name = "customer" )
public class Customer extends User{

    private String phoneNumber;

    // JPA needs an empty constructor
    protected Customer(){
    }

    public Customer( String name, String email, String password, String phoneNumber ){
        super(name, email, password);
        this.phoneNumber = phoneNumber;
    }

    @Override
    public UserRole getRole() {
        return UserRole.CUSTOMER;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
