package Lim.boardApp.domain.customer.repository;

import Lim.boardApp.domain.customer.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public Optional<Customer> findByLoginId(String loginId);

    public Optional<Customer> findByKakaoId(Long kakaoId);

    public Optional<Customer> findByEmail(String email);

    public Optional<Customer> findByName(String name);
}
