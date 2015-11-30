package za.co.knonchalant.status;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by evan on 15/02/25.
 */
@Entity
public class StatusUser {
    private int userId;
    private String facebookUserId;
    private String authToken;

    private String firstName, lastName;

    private boolean optInShare = false;

    private int offset;

    public StatusUser(String facebookUserId, int offset) {
        this.facebookUserId = facebookUserId;
        this.offset = offset;
    }

    public StatusUser() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isOptInShare() {
        return optInShare;
    }

    public void setOptInShare(boolean optInShare) {
        this.optInShare = optInShare;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
