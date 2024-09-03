package cn.eroad.device.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author:suncheng
 * @Data:2023/2/7
 * @code:
 */
public class JjwtUtil {

    /**
     * JWT的唯一身份标识
     */
    public static final String JWT_ID = "eroad-Zdsc!";

    /**
     * 秘钥
     */
    public static final String JWT_SECRET = "eroad-Zdsc!123";

    /**
     * Redis 存储用户或应用 Token 信息标识
     */
    public static final String redisCode = "AC_API_ID_";

    /**
     * Redis 存储判断用户是否为超级管理员标识
     */
    public static final String redisJudgeCode = "AC_JUDGE_ID_";

    /**
     * Token 存储用户或应用 Id 信息标识
     */
    public static final String tokenLoginCode = "ac_api_id";

    /**
     * Token 存储用户或应用 Id 信息标识
     */
    public static final String tokenJudgeCode = "ac_judge_id";

    /**
     * 超级管理员信息标识
     */
    public static final String adminCode = "ROLE_ADMIN";

    /**
     * 过期时间，单位毫秒
     */
    public static final Long EXPIRE_TIME = 12 * 60 * 60 * 1000L;  //设置过期时间为12个小时

    /**
     * 用户信息
     */
    public static final String USER_MESSAGE = "userMessage";


    //创建Token
    public static String createJwt(Map<String, Object> map) {


        //添加头部信息
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");


        //添加载荷信息
        Map<String, Object> claims = new HashMap<>();

        for (String key : map.keySet()) {
            claims.put(key, map.get(key));
        }

        //生成JWT的时间
        long nowTime = System.currentTimeMillis();
        Date issuedAt = new Date(nowTime);

        JwtBuilder builder = Jwts.builder()
                .setHeader(header) // 设置头部信息
                .setClaims(claims)
                .setId(JWT_ID) // jti(JWT ID)：jwt的唯一身份标识
                .setIssuedAt(issuedAt) // iat(issuedAt)：jwt的签发时间
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, JWT_SECRET);

        //设置过期时间
        if (EXPIRE_TIME > 0) {
            long exp = nowTime + EXPIRE_TIME;
            builder.setExpiration(new Date(exp));
        }

        return builder.compact();
    }

    //解析Token
    public static Claims parseJwt(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody();
    }

    /**
     * @return java.lang.String
     * @Description getTenantCodeFromToken:从token中获取租户信息
     * @Param [token]
     **/
    public static String getTenantCodeFromToken(String token) {
        Claims claims = JjwtUtil.parseJwt(token);
        if (claims.get("userMessage") != null) {
            LinkedHashMap<String, String> userMessage = (LinkedHashMap<String, String>) claims.get("userMessage");
            return userMessage.get("tenantCode");
        } else {
            return null;
        }
    }

    /**
     * @return java.lang.String
     * @Description getTenantCodeFromTokenPrefix:获取命名前缀，tanantName_
     * @Param [token]
     **/
    public static String getTenantCodeFromTokenPrefix(String token) {
        Claims claims = JjwtUtil.parseJwt(token);
        if (claims.get("userMessage") != null) {
            LinkedHashMap<String, String> userMessage = (LinkedHashMap<String, String>) claims.get("userMessage");
            return userMessage.get("tenantCode") + "_";
        } else {
            return null;
        }
    }
}

