import { BookModel } from "./BookModel";

export class CheckoutModel {

    bookDTO: BookModel;
    daysLeft: number

    constructor(bookDTO: BookModel, daysLeft: number) {
        this.bookDTO = bookDTO;
        this.daysLeft = daysLeft;
    }
}