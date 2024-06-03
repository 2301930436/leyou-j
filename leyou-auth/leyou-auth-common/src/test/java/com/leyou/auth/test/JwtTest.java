package com.leyou.auth.test;

import com.leyou.common.entity.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "E:\\Data\\leyou\\leyou-j\\rsa.pub";

    private static final String priKeyPath = "E:\\Data\\leyou\\leyou-j\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    /**
     * 公私钥生成
     * @throws Exception
     */
    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 10);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTcxNzA2NjU3OH0.SPstU8gUdWQbuuXhNBEDClAcuMGiz1eCL-wIUJk4YxHpYRaXZS2EMl_y25oS8Y3DqLsmJOYttmcO6UvUxI5DOQFh-pooRrf9wU2mour4_bLiO01eDVZIeYqUEgm4dh8lynmbwx70s5-xqrIGnNog0NTuKmQjPXRBl2TAR1N3w9Y";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}