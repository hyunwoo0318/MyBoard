package Lim.boardApp.ObjectValue;

import java.util.Arrays;
import java.util.List;

public abstract class WhiteList {
    public static List<String> WHITELIST = Arrays.asList("/css/**","/*.ico","/error", "/", "/login", "/logout", "/register");
}
