package za.co.knonchalant.status;

import javax.persistence.*;
import javax.xml.bind.DatatypeConverter;
import java.util.Date;

/**
 * Created by evan on 15/02/22.
 */
@Entity
public class Status {
    private static final int TEXT_LENGTH=3000;

    private int statusId;
    private String userId;
    private Date date;
    private String text;

    public Status() {

    }

    public Status(String userId, Date date, String text) {
        this.userId = userId;
        this.date = date;
        this.text = text;
        if (this.text != null && this.text.length() > TEXT_LENGTH) {
            this.text = shorten(text);
        }
    }

    private String shorten(String text) {
        String substring = text.substring(0, TEXT_LENGTH);
        int spacePoint = substring.lastIndexOf(" ");
        return substring.substring(0, spacePoint);
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Transient
    public String getText() {
        return text;
    }

    @Column(length = TEXT_LENGTH)
    public String getBase64() {
        return DatatypeConverter.printBase64Binary(text.getBytes());
    }

    public void setBase64(String input) {
        text = new String(DatatypeConverter.parseBase64Binary(input));
    }

    public void setText(String text) {
        this.text = text;
    }
}
