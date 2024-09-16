package cap.team3.what.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class History {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private List<String> keywords;
    private LocalDateTime time;

    public History() {}
}
