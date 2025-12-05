export class RegistrationModel {

    firstName: string;
    lastName: string;
    dateOfBirth: Date;
    email: string;
    password: string;

    constructor (firstName: string, lastName: string, dateOfBirth: Date, email: string, password: string) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
    }
    
}