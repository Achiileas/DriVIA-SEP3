package dk.via.sep3.drivia.tier3.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// Employees are added to the database directly, they cannot register themselves
@Entity
@Table( name = "employee" )
public class Employee extends User{

    private String employeeNo;

    // JPA needs an empty constructor
    protected Employee(){
    }

    public Employee( String name, String email, String password, String employeeNo ){
        super(name, email, password);
        this.employeeNo = employeeNo;
    }

    @Override
    public UserRole getRole() {
        return UserRole.EMPLOYEE;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }
}
