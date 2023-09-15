package mk.ukim.finki.ibproekt.repository;

import mk.ukim.finki.ibproekt.model.Totp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TotpRepository extends JpaRepository<Totp, Long> {
    Totp findByTotp(String totp);
}
