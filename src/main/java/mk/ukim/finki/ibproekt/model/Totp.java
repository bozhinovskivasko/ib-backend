package mk.ukim.finki.ibproekt.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "one_time_pass")
public class Totp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title")
    String totp;

    @Column(name = "date_issued")
    ZonedDateTime dateIssued;

    public Totp(String totp, ZonedDateTime dateIssued) {
        this.totp = totp;
        this.dateIssued = dateIssued;
    }

    public Totp() {
    }

    public String getTotp() {
        return totp;
    }

    public ZonedDateTime getDateIssued() {
        return dateIssued;
    }
}
