package dk.via.sep3.drivia.tier3.location.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "location")
public class Location{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;

    // JPA needs an empty constructor
    protected Location(){
        }

        public Location(String name, String address) {
            this.name = name;
            this.address = address;
        }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
