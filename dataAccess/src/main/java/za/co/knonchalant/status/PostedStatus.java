package za.co.knonchalant.status;

import javax.persistence.*;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by evan on 15/02/25.
 */
@Entity
public class PostedStatus {
    private static final int TEXT_LENGTH=3000;

    private int postedId;
    private String userId;

    private String text;

    public PostedStatus() {
    }

    public PostedStatus(String userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public int getPostedId() {
        return postedId;
    }

    public void setPostedId(int postedId) {
        this.postedId = postedId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
