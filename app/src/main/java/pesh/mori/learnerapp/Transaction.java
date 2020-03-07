package pesh.mori.learnerapp;

public class Transaction {
    private String refNumber,item,timestamp,amount,tokensPurchased,tokensValue,paymentMethod;

    public Transaction() {
    }

    public Transaction(String refNumber, String item, String timestamp, String amount, String tokensPurchased, String tokensValue, String paymentMethod) {
        this.refNumber = refNumber;
        this.item = item;
        this.timestamp = timestamp;
        this.amount = amount;
        this.tokensPurchased = tokensPurchased;
        this.tokensValue = tokensValue;
        this.paymentMethod = paymentMethod;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTokensPurchased() {
        return tokensPurchased;
    }

    public void setTokensPurchased(String tokensPurchased) {
        this.tokensPurchased = tokensPurchased;
    }

    public String getTokensValue() {
        return tokensValue;
    }

    public void setTokensValue(String tokensValue) {
        this.tokensValue = tokensValue;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
