package dk.via.sep3.drivia.tier3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

// Supertype from the GR diagram. Customer and Employee each get their own table,
// joined to this one by the same id (JOINED inheritance).
// "user" is a reserved word in PostgreSQL, so the table is called "users".
@Entity
@Table( name = "users" )
@Inheritance( strategy = InheritanceType.JOINED )
public abstract class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column( unique = true )
    private String email;

    // Plain text for now
    private String password;

    // JPA needs an empty constructor
    protected User(){
    }

    protected User( String name, String email, String password ){
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Each subtype says which role it is
    public abstract UserRole getRole();

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
