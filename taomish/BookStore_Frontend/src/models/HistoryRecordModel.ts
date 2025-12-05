import { BookModel } from "./BookModel";

export class HistoryRecordModel {

    id: number;
    bookDTO: BookModel;
    checkoutDate: Date;
    returnDate: Date;

    constructor(id: number, bookDTO: BookModel, checkoutDate: Date, returnDate: Date) {
        this.id = id;
        this.bookDTO = bookDTO;
        this.checkoutDate = checkoutDate;
        this.returnDate = returnDate;
    }
}