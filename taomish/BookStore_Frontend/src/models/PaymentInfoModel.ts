export class PaymentInfoModel {

    amount: number;
    currency: string;
    receiptEmail?: string;

    constructor(amount: number, currency: string, receiptEmail?: string) {
        
        this.amount = amount;
        this.currency = currency;
        this.receiptEmail = receiptEmail;
    }
}