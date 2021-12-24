package com.web.jwtauth.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(targetEntity = Book.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "book_id")
    private Book book;

    @Column
    private Long quantity;

    public CartItem(Book book, Long quantity) {
        this.book = book;
        this.quantity = quantity;
    }
}
