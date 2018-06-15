package ru.assisttech.sdk.cardreader;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class AssistCard implements Parcelable {

    private String pan;
    private String cardHolder;
    private String expireMonth;
    private String expireYear;

    public AssistCard() {
    }

    private AssistCard(Parcel in) {
        read(in);
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(String month) {
        if ((month != null) && (month.length() == 1))
            expireMonth = "0" + month;
        else
            expireMonth = month;
    }

    public void setExpireMonth(int month) {
        setExpireMonth(String.valueOf(month));
    }

    public String getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(String expireYear) {
        if (expireYear.length() == 2) {
            Calendar calendar = Calendar.getInstance();
            int year = (calendar.get(Calendar.YEAR) / 100) * 100 + Integer.valueOf(expireYear);
            this.expireYear = String.valueOf(year);
        } else {
            this.expireYear = expireYear;
        }
    }

    public void setExpireYear(int year) {
        setExpireYear(String.valueOf(year));
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        write(dest);
    }

    // Parcelable
    public static final Parcelable.Creator<AssistCard> CREATOR = new Parcelable.Creator<AssistCard>() {
        public AssistCard createFromParcel(Parcel in) {
            return new AssistCard(in);
        }

        public AssistCard[] newArray(int size) {
            return new AssistCard[size];
        }
    };

    void read(Parcel in) {
        pan = in.readString();
        cardHolder = in.readString();
        expireMonth = in.readString();
        expireYear = in.readString();
    }

    void write(Parcel out) {
        out.writeString(pan);
        out.writeString(cardHolder);
        out.writeString(expireMonth);
        out.writeString(expireYear);
    }
}
