export class ReviewModel {

    personEmail: string;
    personFirstName: string;
    date: Date;
    rating: number;
    reviewDescription: string;
    id?: number;

    constructor ( personEmail: string, personFirstName: string, date: Date, rating: number, reviewDescription: string, id?: number) {

        this.personEmail = personEmail;
        this.personFirstName = personFirstName;
        this.date = date;
        this.rating = rating;
        this.reviewDescription = reviewDescription;
        this.id = id;
    }
    
}