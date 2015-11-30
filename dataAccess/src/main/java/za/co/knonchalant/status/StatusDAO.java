package za.co.knonchalant.status;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by evan on 15/02/22.
 */
@Stateless
public class StatusDAO {

    private static final Logger LOG = Logger.getLogger(StatusDAO.class.getName());

    @PersistenceContext
    EntityManager em;

    public int saveStatuses(List<Status> statusList) {
        int count = 0;
        for (Status status : statusList) {
            if (status.getText() != null) {
                try {
                    em.persist(status);
                    count++;
                } catch (Exception ex) {
                    LOG.info("Failed while adding length " + status.getText().length() + " (" + status.getText() + ")");
                    break;
                }
            }
        }
        return count;
    }

    public Status findMostRecentStatus(String userId) {
        Query query = em.createQuery("Select u From Status u Where u.userId = :userId order by u.date desc");
        query.setParameter("userId", userId);
        List<Status> result = query.setMaxResults(1).getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public List<Status> findByUser(String userId) {
        Query query = em.createQuery("Select u From Status u Where u.userId = :userId order by u.date desc");
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public StatusUser findUserByUserId(String userId) {
        Query query = em.createQuery("Select u From StatusUser u Where u.facebookUserId = :userId");
        query.setParameter("userId", userId);

        StatusUser statusUser;
        try {
            statusUser = (StatusUser) query.getSingleResult();
        } catch (NoResultException ex) {
            statusUser = new StatusUser(userId, 0);
        }

        return statusUser;
    }

    public void updateUser(StatusUser user) {
        em.merge(user);
    }

    public void saveStatus(Status status) {
        if (status.getText() != null) {
            em.persist(status);
        }
    }

    public void savePostedStatus(String userId, String status) {
        em.persist(new PostedStatus(userId, status));
    }

    public boolean userIsOptedIn(String userId) {
        Query query = em.createQuery("Select u From StatusUser u Where u.facebookUserId = :userId and u.optInShare = true");
        query.setParameter("userId", userId);

        try {
            query.getSingleResult();
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

    public boolean existsForUser(String userId, String status) {
        PostedStatus postedStatus = new PostedStatus(userId, status);
        Query query = em.createQuery("Select u From PostedStatus u Where u.userId = :userId and u.base64 = :base64");
        query.setParameter("userId", userId);
        query.setParameter("base64", postedStatus.getBase64());
        List resultList = query.getResultList();

        Query queryStatus = em.createQuery("Select u From Status u Where u.userId = :userId and u.base64 = :base64");
        queryStatus.setParameter("userId", userId);
        queryStatus.setParameter("base64", postedStatus.getBase64());
        List resultListStatus = queryStatus.getResultList();

        return !resultList.isEmpty() || !resultListStatus.isEmpty();
    }

    public List<StatusUser> findOptedInFriends(List<String> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return new ArrayList<>();
        }

        TypedQuery<StatusUser> query = em.createQuery("Select u From StatusUser u Where u.facebookUserId in :userIds and u.optInShare = true", StatusUser.class);
        query.setParameter("userIds", friendIds);
        return query.getResultList();
    }

    public List<Status> findByUsers(List<String> friends) {
        if (friends == null || friends.isEmpty()) {
            return new ArrayList<>();
        }

        TypedQuery<Status> query = em.createQuery("Select u From Status u Where u.userId in :friends order by u.date desc", Status.class);
        query.setParameter("friends", friends);
        return query.getResultList();
    }
}
