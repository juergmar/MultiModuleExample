package de.ma.mme.customerA;

import jakarta.persistence.*;

@Entity(name = "Customer")
@Table(name = "customer")
public class CustomerEntityV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Additional field for Customer A
    private String additionalInfo;

    public CustomerEntityV2() { }

    public CustomerEntityV2(String name, String additionalInfo) {
        this.name = name;
        this.additionalInfo = additionalInfo;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
}
