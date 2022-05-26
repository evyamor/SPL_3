package bgu.spl.net.srv;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class Course { // Class to Generate a bgu.spl.net.srv.Course Object
    private int courseNum;      // retains the id number of the specified course
    private String courseName;  // retains the name of the specified course
    private List<Integer> KdamCoursesList;      // holds "Kdam" related course id numbers list
    private int numOfMaxStudents;
    private int seatsAvailable;
    public ConcurrentHashMap<String, User> registeredToCourse;
    private int index;

    public Course(int courseNum, String courseName, List<Integer> KdamCoursesList, int numOfMaxStudents,int index) {
        this.courseNum = courseNum;
        this.courseName = courseName;
        this.KdamCoursesList = KdamCoursesList;
        this.numOfMaxStudents = numOfMaxStudents;
        seatsAvailable = numOfMaxStudents;
        registeredToCourse = new ConcurrentHashMap<>();
        this.index=index;
    }

    //getters and setters
    public int getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(int courseNum) {
        this.courseNum = courseNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public List<Integer> getKdamCoursesList() {
        return KdamCoursesList;
    }

    public void setKdamCoursesList(List<Integer> kdamCoursesList) {
        KdamCoursesList = kdamCoursesList;
    }

    public int getNumOfMaxStudents() {
        return numOfMaxStudents;
    }

    public void setNumOfMaxStudents(int numOfMaxStudents) {
        this.numOfMaxStudents = numOfMaxStudents;
    }

    public boolean seatAvailable() {
        return seatsAvailable > 0;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public boolean registerStudentToCourse(User u) {
        if (seatAvailable()) {
            registeredToCourse.put(u.getUserName(), u);
            seatsAvailable--;
            return true;
        } else return false;
    }

    public boolean unRegisterStudentToCourse(User u) {

        String studName = u.getUserName();
        if (registeredToCourse.containsKey(studName)) {
            registeredToCourse.remove(studName);
            seatsAvailable++;
            return true;
        } else return false;
    }

    public String studentRegToCourseToString() {
        SortedSet<String> keySet = new TreeSet<>(registeredToCourse.keySet());
        String toReturn = "[";
        for (String u : keySet)
            toReturn = toReturn + u + ",";
        if (toReturn.length() > 1)
            toReturn = toReturn.substring(0, toReturn.length() - 1);//to remove last","
        return toReturn + "]";
    }

    public boolean studentIsRegistered(String user) {
        return registeredToCourse.containsKey(user);
    }

    public int getIndex() {
        return index;
    }
}
