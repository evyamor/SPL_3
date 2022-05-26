package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class User {

    private final String userName;
    private final String password;
    private boolean isLogged;
    private final ConcurrentHashMap<Integer,Course> myCourses;
    private boolean admin;


    public User(String userName, String password) {
        this.userName=userName;
        this.password=password;
        this.isLogged=false;
        myCourses=new ConcurrentHashMap<>();
        admin=false;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAdmin() { // returns if the user is an admin user or not
        return admin;
    }
    public void setAdmin() {
        admin=true;
    }


    public void registerToCourse(Course c){
        myCourses.put(c.getCourseNum(),c);
    }
    public void unRegisterToCourse(Course c){
        myCourses.remove(c.getCourseNum(),c);
    }
    public boolean isRegisteredToCourse(int course){
        return myCourses.containsKey(course);
    }

    public String getPassword() {
        return password;
    }
    public ConcurrentHashMap<Integer,Course> getCourses(){
        return myCourses;
    }
    public boolean isLogged() {return isLogged;}

    public void setLogged(boolean logged) {isLogged = logged;}
}
