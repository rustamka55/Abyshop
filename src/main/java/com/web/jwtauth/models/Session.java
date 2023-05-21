package com.web.jwtauth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private @NotBlank String time;

    @Column
    private @NotBlank String duration;
    @Column
    private @NotBlank String date;
    @Column
    private @NotBlank String room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public Session(String time, String duration, String date, String room) {
        this.time = time;
        this.duration = duration;
        this.date = date;
        this.room = room;
    }

    public Session(String time, String duration, String date, String room,User user) {
        this.time = time;
        this.duration = duration;
        this.date = date;
        this.room = room;
        this.user = user;
    }
}
