package de.ma.mme.customerA;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerRepositoryV2 repository;

    public CustomerController(CustomerRepositoryV2 repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CustomerEntityV2> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public CustomerEntityV2 create(@RequestBody CustomerEntityV2 customer) {
        return repository.save(customer);
    }
}
