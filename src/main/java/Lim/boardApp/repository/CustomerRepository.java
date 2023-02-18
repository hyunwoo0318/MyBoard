package Lim.boardApp.repository;

import Lim.boardApp.domain.Customer;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public Optional<Customer> findByLoginId(String loginId);

    public Optional<Customer> findByKakaoId(Long kakaoId);
}
