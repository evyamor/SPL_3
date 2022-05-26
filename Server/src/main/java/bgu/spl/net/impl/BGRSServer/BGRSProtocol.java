package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.BGRSServer.Messages.*;
import bgu.spl.net.srv.Database;
import bgu.spl.net.srv.User;

public class BGRSProtocol implements MessagingProtocol<Message> {
    User user = null;
    private boolean shouldTerminate = false;
    private final Database database=Database.getInstance();

    public BGRSProtocol() {
    }

    @Override
    public Message process(Message msg) {
        short opCode = msg.getOpcode();
        String userName;
        String password;

        switch (opCode) {
            //-------------------------------------admin register----------------------------------------------------------------------------
            case 1: {
                userName = ((ADMINREG) msg).getUsername();
                password = ((ADMINREG) msg).getPassword();
                if (user==null&database.addUserToDatabase(userName, password)) {
                    database.setAdmin(userName);
                    return new ACK(opCode);

                } else return new ERROR(opCode);
            }
//-------------------------------------student register----------------------------------------------------------------------------
            case 2: {
                userName = ((STUDENTREG) msg).getUsername();
                password = ((STUDENTREG) msg).getPassword();
                if (user==null&database.addUserToDatabase(userName, password)) {
                    return new ACK(opCode);
                } else return new ERROR(opCode);
            }
            //-------------------------------------LOGIN----------------------------------------------------------------------------
            case 3: {
                userName = ((LOGIN) msg).getUsername();
                password = ((LOGIN) msg).getPassword();
                if (database.logIn(userName, password)) {
                    user = database.getUserByName(userName);
                    return new ACK(opCode);
                } else return new ERROR(opCode);

            }
            //-------------------------------------LOGOUT----------------------------------------------------------------------------
            case 4: {
                if (user != null && database.isUserExist(user.getUserName())) {
                    if (database.logout(user.getUserName())) {
                        shouldTerminate = true;
                        return new ACK(opCode);
                    } else return new ERROR(opCode);
                } else return new ERROR(opCode);
            }
            //-------------------------------------courser register----------------------------------------------------------------------------
            case 5: {
                int courseNumber = ((COURSEREG) msg).getCourseNumber();
                if (user != null && database.isCourseExist(courseNumber) && database.checkKdam(user.getUserName(), courseNumber)) {
                    if (database.registerUserToCourse(user.getUserName(), courseNumber))
                        return new ACK(opCode);
                    else return new ERROR(opCode);
                } else return new ERROR(opCode);
            }

            //-------------------------------------KDAMCHECK----------------------------------------------------------------------------
            case 6: {
                int course = ((KDAMCHECK) msg).getCourseNumber();
                if (user != null && user.isLogged() & database.isCourseExist(course)) {
                    ACK ack = new ACK(opCode);
                    ack.setOptional(database.kdamToString(course));
                    return ack;
                } else return new ERROR(opCode);
            }
            //-------------------------------------course status----------------------------------------------------------------------------
            case 7: {
                if (user != null && user.isLogged() & user.isAdmin()) {
                    int courseNum = ((COURSESTAT) msg).getCourseNumber();
                    ACK ack = new ACK(opCode);
                    if (database.isCourseExist(courseNum)) {
                        ack.setOptional((database.courseStatus(courseNum)));
                        return ack;
                    }
                } else return new ERROR(opCode);
            }
            //-------------------------------------student status--------------
            case 8: {
                String userNm = ((STUDENTSTAT) msg).getUserName();
                if (user != null && database.isUserExist(user.getUserName()) & user.isLogged() & user.isAdmin() & database.isUserExist(userNm)) {
                    ACK ack = new ACK(opCode);
                    if (!database.getUserByName(userNm).isAdmin()) {
                        ack.setOptional(database.studentStat(userNm));
                        return ack;
                    }else return new ERROR(opCode);
                } else return new ERROR(opCode);
            }
            //-------------------------------------is registered to a course----------------------------------------------------------------------------
            case 9: {
                int courseN = ((ISREGISTERED) msg).getCourseNumber();
                ACK ack = new ACK(opCode);
                if (user != null&&user.isLogged()) {
                    if (database.isRegisteredToCourse(user.getUserName(),courseN)) {
                        ack.setOptional("REGISTERED");
                    } else ack.setOptional("NOT REGISTERED");
                    return ack;
                }
                return new ERROR(opCode);
            }
            //-------------------------------------un register from course----------------------------------------------------------------------------
            case 10: {
                int courseN = ((UNREGISTER) msg).getCourseNumber();
                if (user != null && !user.isAdmin()& database.unRegisterUserToCourse(user.getUserName(), courseN)) {
                    return new ACK(opCode);
                } else return new ERROR(opCode);
            }

            //-------------------------------------My courses----------------------------------------------------------------------------
            case 11: {
                if (user != null && user.isLogged()&!user.isAdmin()) {
                    ACK ack = new ACK(opCode);
                    ack.setOptional(database.coursesToString(user.getUserName()));
                    return ack;
                } else return new ERROR(opCode);
            }
        }
        return null;
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
