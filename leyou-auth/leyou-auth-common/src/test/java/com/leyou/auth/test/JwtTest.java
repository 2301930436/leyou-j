package com.leyou.auth.test;

import com.leyou.common.entity.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "D:\\JetBrains\\ideaIU-2021.1\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\JetBrains\\ideaIU-2021.1\\tmp\\rsa\\rsa.pri";

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
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTY2ODU3ODMwMH0.TCHHnXsM1W6Q9BeDDSVTJV50JG5kDveDmc76xPt2XmtPJykUJZ8Nv4Mjp7LMfjLtFt76_Az8fJ7JCjVEWXZt-YoIRG6RAsMvmxDUc4REDeYGevERDj9nptjoH8eMF7DeIF62NZ6pqGcRc_aYxPrj6QAW8cvooMe1HjqAeWWXAT4";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}