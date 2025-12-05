export class DiscussionModel {
    
    title: string;
    question: string;
    id?: number;
    personEmail?: string;
    personFirstName?: string;
    personLastName?: string;
    adminEmail?: string;
    response?: string;
    closed?: boolean;

    constructor(title: string, question: string, id?: number, personEmail?: string, personFirstName?: string, 
        personLastName?: string, adminEmail?: string, response?: string, closed?: boolean) {

        this.id = id;
        this.personEmail = personEmail;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.title = title;
        this.question = question;
        this.adminEmail = adminEmail;
        this.response = response;
        this.closed = closed;
    }
}