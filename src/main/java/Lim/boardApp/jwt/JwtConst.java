package Lim.boardApp.jwt;

public abstract class JwtConst {
    public static Long ACCESS_TOKEN_EXPIRED_TIME = 1000 * 60 * 30L; //30분
    public static Long REFRESH_TOKEN_EXPIRED_TIME = 1000 * 60 * 300L; //300분
}
