package com.iliamalafeev.bookstore.bookstore_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutDTO {

    private BookDTO bookDTO;

    private Integer daysLeft;
}
