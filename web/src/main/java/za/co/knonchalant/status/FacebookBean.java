package za.co.knonchalant.status;

import facebook4j.*;
import org.joda.time.DateTime;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by evan on 15/02/22.
 */
@ManagedBean
@SessionScoped
public class FacebookBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(FacebookBean.class.getName());
    private static final int GIVE_UP = 2;

    @EJB
    StatusDAO statusDAO;

    List<Status> statuses;
    List<Friend> friendList;
    List<SelectedWrapper<StatusUser>> optedInFriends;

    private DateTime retrievedFriends;

    private ERetrievalStatus retrievalStatus = ERetrievalStatus.PENDING;

    public int getSelectedFriendCount() {
        return SelectedWrapper.getSelected(optedInFriends).size();
    }

    public List<SelectedWrapper<StatusUser>> getOptedInFriends() {
        DateTime now = new DateTime();
        now = now.minusMinutes(5);

        if (optedInFriends == null || optedInFriends.isEmpty() || now.isAfter(retrievedFriends)) {
            retrievedFriends = new DateTime();
            List<Friend> friendsFromFacebook = getFriendsFromFacebook();
            List<String> friendIds = getIds(friendsFromFacebook);
            optedInFriends = SelectedWrapper.makeWrapper(statusDAO.findOptedInFriends(friendIds));
        }
        return optedInFriends;
    }

    private List<String> getIds(List<Friend> friendsFromFacebook) {
        ArrayList<String> ids = new ArrayList<>();
        for (Friend friend : friendsFromFacebook) {
            ids.add(friend.getId());
        }
        return ids;
    }

    public String getFacebookId() {
        Facebook facebook = retrieveSessionObject();
        try {
            return facebook.getId();
        } catch (FacebookException e) {
            return null;
        }
    }

    private List<Friend> getFriendsFromFacebook() {
        if (friendList == null) {
            Facebook facebook = retrieveSessionObject();
            try {
                ResponseList<Friend> friends = facebook.friends().getFriends();
                Paging<Friend> paging = friends.getPaging();
                ResponseList<Friend> pagedFriends = facebook.fetchNext(paging);
                while (pagedFriends != null && !pagedFriends.isEmpty()) {
                    friends.addAll(pagedFriends);
                    pagedFriends = facebook.fetchNext(paging);
                }
                friendList = friends;
            } catch (FacebookException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Could not retrieve friends :("));
                friendList = new ArrayList<>();
            }

        }
        return friendList;
    }

    public Facebook retrieveSessionObject() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        Facebook facebook = (Facebook) session.getAttribute("facebook");

        if (facebook == null) {
            String contextPath = session.getServletContext().getContextPath();
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + "/signin");
            } catch (IOException e) {
                // couldn't redirect
            }
        }
        return facebook;
    }

    public void refreshRecentStatuses() {
        Facebook facebook = retrieveSessionObject();

        String userId = getUserId(facebook);

        updateUserInformation(facebook, userId);

        Status recentStatus = statusDAO.findMostRecentStatus(userId);

        if (recentStatus == null) {
            return;
        }

        Reading reading = setupReading(0);
        try {
            StatusUser user = statusDAO.findUserByUserId(userId);
            int offset = user.getOffset();
            ResponseList<Post> postStatuses = facebook.getStatuses(reading);
            List<Status> newStatuses = convertToStatusList(postStatuses, userId);
            for (Status status : newStatuses) {
                if (status.getDate().after(recentStatus.getDate())) {
                    if (status.getText() != null) {
                        if (!statusDAO.existsForUser(userId, status.getText())) {
                            statusDAO.saveStatus(status);
                            statuses.add(0, status);
                        }
                    }
                    offset++;
                } else {
                    break;
                }
            }
            user.setOffset(offset);
            statusDAO.updateUser(user);
        } catch (FacebookException e) {
            LOG.info("Could not refresh statuses: " + e.toString());
        }
    }

    private void updateUserInformation(Facebook facebook, String userId) {
        StatusUser user = statusDAO.findUserByUserId(userId);
        if (user.getFirstName() == null || user.getLastName() == null) {
            User me = getUser(facebook);
            if (me == null) {
                return;
            }

            user.setFirstName(me.getFirstName());
            user.setLastName(me.getLastName());
        }

        user.setAuthToken(facebook.getOAuthAccessToken().getToken());

        statusDAO.updateUser(user);
    }

    private User getUser(Facebook facebook) {
        try {
            return facebook.getMe();
        } catch (FacebookException e) {
            LOG.warning("Could not retrieve Facebook user.");
            return null;
        }
    }

    private String getUserId(Facebook facebook) {
        try {
            return facebook.getId();
        } catch (FacebookException e) {
            return null;
        }
    }

    public void loadMoreStatuses() {
        Facebook facebook = retrieveSessionObject();
        refreshRecentStatuses();
        try {
            String userId = facebook.getId();
            List<Status> newStatuses = retrieveAndPersistStatuses(facebook, userId, 200);
            statuses.addAll(newStatuses);
        } catch (FacebookException e) {
            e.printStackTrace();
            retrievalStatus = ERetrievalStatus.FACEBOOK_ERROR;
        }
    }

    public List<Status> getStatuses() {
        if (statuses == null || statuses.isEmpty()) {
            try {
                statuses = getPosts();
                retrievalStatus = ERetrievalStatus.RETRIEVED;
            } catch (FacebookException e) {
                retrievalStatus = ERetrievalStatus.FACEBOOK_ERROR;
            }
        }
        return statuses;
    }

    private List<Status> getPosts() throws FacebookException {
        Facebook facebook = retrieveSessionObject();
        if (facebook == null) {
            return new ArrayList<>();
        }
        String userId = facebook.getId();

        List<Status> statuses = statusDAO.findByUser(userId);

        if (statuses.isEmpty()) {
            return retrieveAndPersistStatuses(facebook, userId, 500);
        }

        LOG.info("Retrieved statuses from DB, " + statuses.size());
        return statuses;
    }

    private List<Status> retrieveAndPersistStatuses(Facebook facebook, String userId, int targetPosts) {
        List<Post> posts = retrieveStatusesFromFacebook(facebook, targetPosts, userId);

        List<Status> statusList = clean(convertToStatusList(posts, userId));

        int count = statusDAO.saveStatuses(statusList);

        LOG.info("Persisted " + count + " statuses.");

        return statusList;
    }

    private List<Status> clean(List<Status> statuses) {
        ArrayList<Status> result = new ArrayList<>();
        for (Status status : statuses) {
            if (status.getText() != null) {
                result.add(status);
            }
        }
        return result;
    }


    private List<Status> convertToStatusList(List<Post> posts, String userId) {
        ArrayList<Status> result = new ArrayList<>();
        for (Post post : posts) {
            result.add(new Status(userId, post.getUpdatedTime(), post.getMessage()));
        }
        return result;
    }

    private List<Post> retrieveStatusesFromFacebook(Facebook facebook, int targetPosts, String userId) {
        StatusUser user = statusDAO.findUserByUserId(userId);
        int offset = user.getOffset();

        try {
            List<Post> statuses = null;
            int noNewStatuses = 0;
            while (noNewStatuses < GIVE_UP && (statuses == null || statuses.size() < targetPosts)) {

                Reading reading = setupReading(offset);
                List<Post> cleaned = removeGenerated(facebook.getStatuses(reading), userId);

                if (statuses == null) {
                    statuses = cleaned;
                    offset += statuses.size();
                } else {
                    int prevCount = statuses.size();
                    statuses.addAll(cleaned);
                    if (prevCount == statuses.size()) {
                        noNewStatuses++;
                    } else {
                        noNewStatuses = 0;
                        offset += statuses.size() - prevCount;
                    }
                }

                if (statuses.size() >= targetPosts) {
                    LOG.info("Reached target.");
                }
            }

            user.setOffset(offset);
            statusDAO.updateUser(user);
            return statuses;
        } catch (FacebookException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<Post> removeGenerated(ResponseList<Post> statuses, String userId) {
        ArrayList<Post> result = new ArrayList<>();
        for (Post post : statuses) {
            if (post.getMessage() != null && !statusDAO.existsForUser(userId, post.getMessage())) {
                result.add(post);
            }
        }
        return result;
    }


    private Reading setupReading(int count) {
        Reading reading = new Reading();
        reading.limit(100);
        reading.offset(count);
        reading.fields("id", "message");
        return reading;
    }

    public void postStatus(String status) {
        Facebook facebook = retrieveSessionObject();
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            facebook.postStatusMessage(status);

            ctx.addMessage(null, new FacesMessage("Posted status."));

            statusDAO.savePostedStatus(getUserId(facebook), status);

        } catch (FacebookException e) {
            ctx.addMessage(null, new FacesMessage("Could not post status."));
            try {
                String path = ctx.getExternalContext().getRequestContextPath();
                ctx.getExternalContext().redirect(path + "/signin?scope=publish_actions");
            } catch (IOException e1) {
                ctx.addMessage(null, new FacesMessage("Could not redirect for permissions."));
            }
        }
    }

    private String getTags(List<StatusUser> selectedFriends) {
        if (selectedFriends == null || selectedFriends.isEmpty()) {
            return "";
        }

        String result = selectedFriends.get(0).getFacebookUserId();
        for (int i = 1; i < selectedFriends.size(); i++) {
            result += "," + selectedFriends.get(i).getFacebookUserId();
        }
        return result;
    }

    public void resetFriends() {

        optedInFriends = null;
    }
}
