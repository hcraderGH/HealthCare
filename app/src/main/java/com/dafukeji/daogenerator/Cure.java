package com.dafukeji.daogenerator;

import java.util.List;
import com.dafukeji.daogenerator.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "CURE".
 */
public class Cure {

    private Long startTime;
    private Long stopTime;
    private Integer cureType;
    private Long id;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient CureDao myDao;

    private List<Point> points;

    public Cure() {
    }

    public Cure(Long id) {
        this.id = id;
    }

    public Cure(Long startTime, Long stopTime, Integer cureType, Long id) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.cureType = cureType;
        this.id = id;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCureDao() : null;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStopTime() {
        return stopTime;
    }

    public void setStopTime(Long stopTime) {
        this.stopTime = stopTime;
    }

    public Integer getCureType() {
        return cureType;
    }

    public void setCureType(Integer cureType) {
        this.cureType = cureType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<Point> getPoints() {
        if (points == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PointDao targetDao = daoSession.getPointDao();
            List<Point> pointsNew = targetDao._queryCure_Points(id);
            synchronized (this) {
                if(points == null) {
                    points = pointsNew;
                }
            }
        }
        return points;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetPoints() {
        points = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
