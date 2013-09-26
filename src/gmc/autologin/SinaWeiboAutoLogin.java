/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gmc.autologin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import gmc.config.Config;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pok
 */
public class SinaWeiboAutoLogin extends Thread {

    private String rsakv;
    private long servertime;
    private String nonce;
    private String pcid;
    private String pubkey;
    private int retcode;
    private String sp;
    private String su;
    private Config conf;

    public SinaWeiboAutoLogin(Config conf) {
        this.conf = conf;
    }
    

    
    
    
    /**
     * 使用HttpClient4实现自动微博登陆
     *
     * @param username 登录账号
     * @param password 登录密码
     * @return cookie 登陆后返回的cookie
     */
    public String getLoginCookie(String username, String password) throws IOException, JSONException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, NoSuchPaddingException, InvalidKeyException {
        String cookie="";
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
        getParam(client);
        sp = rsaCrypt(pubkey, "10001", password, servertime, nonce);
        su = encodeUserName(username);
        String url = login(client);
        if (url.equals("-1")) {
            System.out.println("登录失败！");
            return "";
        } else {
            HttpGet getMethod = new HttpGet(url);
            HttpResponse response = client.execute(getMethod);
            for (Cookie c : client.getCookieStore().getCookies()) {
                String s = c.toString();
                String name = s.substring(s.indexOf("name:") + 5, s.indexOf("][value")).trim();
                String value = s.substring(s.indexOf("value:") + 6, s.indexOf("][domain"));
                s = name + "=" + value + ";";
                cookie += s;
            }
        }
        client=null;
        return cookie;
    }

    private void getParam(DefaultHttpClient client) throws IOException, JSONException {
        String preloginurl = "http://login.sina.com.cn/sso/prelogin.php?"
                + "entry=sso&"
                + "callback=sinaSSOController.preloginCallBack&su=dW5kZWZpbmVk&"
                + "rsakt=mod&"
                + "client=ssologin.js(v1.4.2)&"
                + "_=" + getCurrentTime();
        HttpGet get = new HttpGet(preloginurl);
        HttpResponse response = client.execute(get);
        String getResp = EntityUtils.toString(response.getEntity());
        int firstLeftBracket = getResp.indexOf("(");
        int lastRightBracket = getResp.lastIndexOf(")");
        String jsonStr = getResp.substring(firstLeftBracket + 1, lastRightBracket);
        JSONObject jsonInfo = new JSONObject(jsonStr);
        nonce = jsonInfo.getString("nonce");
        pcid = jsonInfo.getString("pcid");
        pubkey = jsonInfo.getString("pubkey");
        retcode = jsonInfo.getInt("retcode");
        rsakv = jsonInfo.getString("rsakv");
        servertime = jsonInfo.getLong("servertime");
    }

    private String getCurrentTime() {
        return String.valueOf(new Date().getTime() / 1000);
    }

    private String rsaCrypt(String modeHex, String exponentHex, String password, long serverTime, String nonce) throws IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, UnsupportedEncodingException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        BigInteger m = new BigInteger(modeHex, 16); /* public exponent */
        BigInteger e = new BigInteger(exponentHex, 16); /* modulus */
        RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
        RSAPublicKey pub = (RSAPublicKey) factory.generatePublic(spec);
        Cipher enc = Cipher.getInstance("RSA");
        enc.init(Cipher.ENCRYPT_MODE, pub);
        String confusrPassword = serverTime + "\t" + nonce + "\n" + password;
        byte[] encryptedContentKey = enc.doFinal(confusrPassword.getBytes("GB2312"));
        return new String(Hex.encodeHex(encryptedContentKey));
    }

    private String encodeUserName(String username) {
        username = username.replaceFirst("@", "%40");
        return Base64.encodeBase64String(username.getBytes());
    }

    private String login(DefaultHttpClient client) throws UnsupportedEncodingException, IOException {
        HttpPost post = new HttpPost("http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.2)");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("entry", "weibo"));
        nvps.add(new BasicNameValuePair("gateway", "1"));
        nvps.add(new BasicNameValuePair("from", ""));
        nvps.add(new BasicNameValuePair("savestate", "7"));
        nvps.add(new BasicNameValuePair("useticket", "1"));
        nvps.add(new BasicNameValuePair("ssosimplelogin", "1"));
        nvps.add(new BasicNameValuePair("vsnf", "1"));
        nvps.add(new BasicNameValuePair("su", su));
        nvps.add(new BasicNameValuePair("service", "miniblog"));
        nvps.add(new BasicNameValuePair("servertime", servertime + ""));
        nvps.add(new BasicNameValuePair("nonce", nonce));
        nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
        nvps.add(new BasicNameValuePair("rsakv", rsakv));
        nvps.add(new BasicNameValuePair("sp", sp));
        nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
        nvps.add(new BasicNameValuePair("prelt", "115"));
        nvps.add(new BasicNameValuePair("returntype", "META"));
        nvps.add(new BasicNameValuePair("url", "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
        post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        HttpResponse response = client.execute(post);
        String entity = EntityUtils.toString(response.getEntity());
        String url = "";
        try {
            url = entity.substring(entity.indexOf("http://weibo.com/ajaxlogin.php?"), entity.indexOf("code=0") + 6);
        } catch (Exception e) {
            return "-1";
        }
        return url;
    }


    public void pushCookie() throws IOException, JSONException, IllegalBlockSizeException, IllegalBlockSizeException, IllegalBlockSizeException, BadPaddingException, BadPaddingException, BadPaddingException, NoSuchAlgorithmException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, NoSuchPaddingException, InterruptedException {
        while (true) {
            DB db = Mongo.connect(new DBAddress(conf.getCookieHost(), conf.getCookieDBName()));
            db.requestStart();
            DBCollection coll = db.getCollection(conf.getCookieCollectionName());
            DBCollection uidColl = db.getCollection(conf.getCookie_idCollectionName());
            DBCursor cur = uidColl.find(new BasicDBObject("status", "available"));
            ArrayList<DBObject> loginList=new ArrayList<DBObject>();
            while (cur.hasNext()) {
                Thread.sleep((long) (1000 * 10 * Math.random()));
                DBObject obj = cur.next();
                String cookie = getLoginCookie(obj.get("uid").toString(), obj.get("pwd").toString());
                Thread.sleep(1000 * 10);
                if (!cookie.isEmpty() && cookie != null && !cookie.equals("")) {
                    Date date = new Date();
                    DBObject inObj = new BasicDBObject();
                    inObj.put("cookie", cookie);
                    inObj.put("date", date.toString());
                    inObj.put("status", "available");
                    loginList.add(inObj);
                } else {
                    continue;
                }
            }
            if (loginList.size() > 0) {
                DBObject queryObj = new BasicDBObject("status", "available");
                DBObject updateObj = new BasicDBObject("$set", new BasicDBObject("status", "disable"));
                coll.update(queryObj, updateObj, false, true);
                for (DBObject o : loginList) {
                    coll.insert(o);
                }
                cur.close();
                db.requestDone();
                db.getMongo().close();
                System.out.println(Class.class.getName()+"\tstart sleeping...");
                Thread.sleep(1000 * 60 * 60 * 12);
            } else {
                cur.close();
                db.requestDone();
                db.getMongo().close();
                System.out.println(this.getClass().getName()+"\tcatch exception:run again after 1 min");
                Thread.sleep((long)(1000 * 60 * Math.random()));
            }
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            pushCookie();
        } catch (IOException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SinaWeiboAutoLogin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IOException, JSONException
    {
    	SinaWeiboAutoLogin login = new SinaWeiboAutoLogin(null);
    	System.out.println(login.getLoginCookie("mitcn@163.com", "245412401"));
    	System.out.print("hello");
    }
    
}
