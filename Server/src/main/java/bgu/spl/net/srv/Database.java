package bgu.spl.net.srv;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the bgu.spl.net.srv.Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
    private HashMap<Integer, Course> KdamCoursesList = new HashMap<>();    // holds "Kdam" related course id numbers list
    private final ConcurrentHashMap<Integer, Course> coursesHashMap = new ConcurrentHashMap<>(); // retains all courses in database
    private final ConcurrentHashMap<String, User> usersHashMap = new ConcurrentHashMap<>();  //retains all users by id and pw that have registered
    private final AtomicInteger numOfUsersLoggedIn = new AtomicInteger();
    private final Object registerLock = new Object();


    //to prevent user from creating new bgu.spl.net.srv.Database
    private Database() {
        initialize("Courses.txt");
    }

    // a wrapper for singleton instance
    private static class DataBaseWrapper {
        private final static Database instance = new Database();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return DataBaseWrapper.instance;
    }


    /**
     * loades the courses from the file path specified
     * into the bgu.spl.net.srv.Database, returns true if successful.
     */
    boolean initialize(String coursesFilePath) {
        try {
            Scanner scanner = new Scanner(new FileReader(coursesFilePath));
            int index = 0;
            while (scanner.hasNextLine()) {
                String[] tokens = scanner.nextLine().split("\\|");
                int courseNum = Integer.parseInt(tokens[0]); // first token retains course number
                String courseName = tokens[1]; // second token retains the name of the course, already in string
                int numOfMaxStudents = Integer.parseInt(tokens[3]); //   fourth token retains the maximum number of students allowed in the course
                //cutting the " [ ] " from the string
                tokens[2] = tokens[2].substring(1, tokens[2].length() - 1); // TODO CHECK IF PARSER IS CORRECT WITHIN NEEDED TERMS
                List<Integer> currCourseKdams = new LinkedList<>();
                if (tokens[2].length() > 0) {
                    String[] KdamTokens = tokens[2].split(",");

                    for (int j = 0; j < KdamTokens.length; j++) {
                        currCourseKdams.add(Integer.parseInt(KdamTokens[j])); // adds "Kdam Courses" to the list
                    }
                }
                Course courseToAdd = new Course(courseNum, courseName, currCourseKdams, numOfMaxStudents,index); // creates course with local variables
                coursesHashMap.putIfAbsent(courseNum, courseToAdd); // adds the course to the hash map
                index++;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean addUserToDatabase(String name, String pw) { // adding users to the database when a client registers to the server
        boolean register = true;
        synchronized (registerLock) {
            if (usersHashMap.containsKey(name)) {
                register = false;
            } else {
                usersHashMap.put(name, new User(name, pw));
            }
            registerLock.notifyAll();
        }
        return register;
    }

    public Course getCourse(int courseNum) { //may produce null ptr
        return coursesHashMap.get(courseNum);
    }

    public boolean isCourseExist(int courseNum) { // checks if course exist in data base
        return coursesHashMap.containsKey(courseNum);
    }

    public String getPw(String user) {  //may produce null ptr
        return usersHashMap.get(user).getPassword();
    }

    public boolean isUserExist(String user) { // checks if user exist in data base
        return usersHashMap.containsKey(user);
    }

    public User getUserByName(String name) { // search user in database by user name
        return usersHashMap.get(name);
    }

    public void setAdmin(String name) {  // sets user as admin
        getUserByName(name).setAdmin();
    }

    public boolean logIn(String user, String password) { // do login for a user and increase the num of logged in users.
        boolean logged = false;
        if (isUserExist(user) && getPw(user).equals(password)) {
            synchronized (getUserByName(user)) {
                if (!getUserByName(user).isLogged()) {
                    getUserByName(user).setLogged(true);
                    numOfUsersLoggedIn.compareAndSet(numOfUsersLoggedIn.get(), numOfUsersLoggedIn.get() + 1);
                    logged = true;
                    //      numOfUsersLoggedIn++;

                }
                getUserByName(user).notifyAll();
            }
        }
        return logged;
    }

    public boolean logout(String user) { // do log out for a user and decrease the num of logged in users.
        boolean loggedOut = false;
        synchronized (getUserByName(user)) {
            if (numOfUsersLoggedIn.get() > 0 & getUserByName(user).isLogged()) {
                getUserByName(user).setLogged(false);
                numOfUsersLoggedIn.compareAndSet(numOfUsersLoggedIn.get(), numOfUsersLoggedIn.get() - 1);
                loggedOut = true;
            }
            getUserByName(user).notifyAll();
        }
        return loggedOut;
    }

    public boolean checkKdam(String user, int course) { // checks if the user has all kdam courses for this course.
        boolean kdamCheck = true;
        synchronized (getUserByName(user)) {
            User u = getUserByName(user);
            if (u.isLogged()) {

                synchronized (getCourse(course)) {
                    List<Integer> courseKdam = getCourse(course).getKdamCoursesList();
                    ConcurrentHashMap<Integer, Course> userCourses = u.getCourses();
                    for (Integer i : courseKdam) {
                        if (!userCourses.containsKey(i)) {
                            kdamCheck = false;
                        }
                    }
                    getCourse(course).notifyAll();
                }

            } else kdamCheck = false;
            getUserByName(user).notifyAll();

        }
        return kdamCheck;
    }


    public boolean registerUserToCourse(String user, Integer courseNum) {
        boolean registered = false;
        synchronized (getUserByName(user)) {
            if (getUserByName(user).isLogged() & !getUserByName(user).isAdmin()) {
                User u = getUserByName(user);
                synchronized (getCourse(courseNum)) {
                    if (!u.isRegisteredToCourse(courseNum)) {
                        if (getCourse(courseNum).seatAvailable() & getCourse(courseNum).registerStudentToCourse(u)) {
                            u.registerToCourse(getCourse(courseNum));
                            registered = true;
                        }

                    }
                    getCourse(courseNum).notifyAll();
                }

            }
            getUserByName(user).notifyAll();
        }
        return registered;
    }

    public boolean unRegisterUserToCourse(String user, Integer courseNum) {
        boolean unRegistered = false;
        synchronized (getUserByName(user)) {
            User u = getUserByName(user);
            synchronized (getCourse(courseNum)) {
                if (getCourse(courseNum).unRegisterStudentToCourse(u)) {
                    u.unRegisterToCourse(getCourse(courseNum));
                    unRegistered = true;
                }
                getCourse(courseNum).notifyAll();
            }
            getUserByName(user).notifyAll();
        }
        return unRegistered;

    }

    public String studentStat(String name) {
        String toReturn = "";
        toReturn = "Student: " + name + "\n" + "Courses: " + coursesToString(name);
        return toReturn;
    }


    public String kdamToString(int courseNum) {
        List<Integer> courseKdam = getCourse(courseNum).getKdamCoursesList();
        courseKdam.sort(Comparator.comparingInt(course-> coursesHashMap.get(course).getIndex()));
        String toReturn = "[";
        for (Integer i : courseKdam)
            toReturn = toReturn + i + ",";
        if (toReturn.length() > 1)
            toReturn = toReturn.substring(0, toReturn.length() - 1);//to remove last","
        return toReturn + "]";
    }

    public String coursesToString(String userName) {
        String toReturn = "";
        synchronized (getUserByName(userName)) {
            ConcurrentHashMap<Integer, Course> myCourses = getUserByName(userName).getCourses();
            List<Integer> l1 = new ArrayList<>(coursesHashMap.keySet());
            l1.sort(Comparator.comparingInt(course-> coursesHashMap.get(course).getIndex()));
            toReturn = "[";
            for (Integer i : l1) {
                if (myCourses.containsKey(i))
                    toReturn = toReturn + i + ",";
            }
            if (toReturn.length() > 1)
                toReturn = toReturn.substring(0, toReturn.length() - 1);//to remove last","
            toReturn = toReturn + "]";
            getUserByName(userName).notifyAll();
        }
        return toReturn;
    }

    public String courseStatus(int courseNum) {
        String toReturn = "";
        synchronized (getCourse(courseNum)) {
            Course c = getCourse(courseNum);

            toReturn = "Course: " + "(" + c.getCourseNum() + ")" + " " + c.getCourseName() + "\n";
            toReturn = toReturn + "Seats Available: " + c.getSeatsAvailable() + "/" + c.getNumOfMaxStudents() + "\n";
            toReturn = toReturn + "Students Registered: " + c.studentRegToCourseToString();
            getCourse(courseNum).notifyAll();
        }
        return toReturn;
    }

    public boolean isRegisteredToCourse(String name, int courseNum) {
        boolean registered = false;
        synchronized (getUserByName(name)) {
            if (getUserByName(name).isRegisteredToCourse(courseNum))
                registered = true;
            getUserByName(name).notifyAll();
        }
        return registered;
    }

    //need to build a function that says if the user have all the kdam.
}
