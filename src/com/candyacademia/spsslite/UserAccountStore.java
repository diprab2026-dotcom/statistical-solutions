package com.candyacademia.spsslite;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Local account storage using PBKDF2-HMAC-SHA256 salted hashes. */
public final class UserAccountStore {
    private static final int ITERATIONS=120_000,BITS=256;
    private final Path file;private final Properties data=new Properties();private final SecureRandom random=new SecureRandom();
    public UserAccountStore()throws IOException{this(defaultPath());}
    public UserAccountStore(Path file)throws IOException{this.file=file;if(Files.exists(file))try(InputStream in=Files.newInputStream(file)){data.load(in);}}
    public synchronized boolean hasUsers(){return data.stringPropertyNames().stream().anyMatch(k->k.startsWith("u.")&&k.endsWith(".name"));}
    public synchronized boolean exists(String username){return data.containsKey(prefix(username)+"name");}
    public synchronized String displayName(String username){return data.getProperty(prefix(username)+"name",username);}
    public synchronized String securityQuestion(String username){return data.getProperty(prefix(username)+"question");}
    public synchronized void create(String username,char[]password,String question,char[]answer)throws Exception{validateUsername(username);validatePassword(password);if(question==null||question.isBlank())throw new IllegalArgumentException("Choose a security question.");if(answer==null||new String(answer).trim().length()<2)throw new IllegalArgumentException("Enter a recovery answer of at least 2 characters.");if(exists(username))throw new IllegalArgumentException("That username already exists.");String p=prefix(username);byte[]ps=salt(),as=salt();data.setProperty(p+"name",username.trim());data.setProperty(p+"question",question.trim());data.setProperty(p+"passwordSalt",encode(ps));data.setProperty(p+"passwordHash",encode(hash(password,ps)));char[]normalized=normalizeAnswer(answer);data.setProperty(p+"answerSalt",encode(as));data.setProperty(p+"answerHash",encode(hash(normalized,as)));Arrays.fill(normalized,'\0');save();}
    public synchronized boolean authenticate(String username,char[]password)throws Exception{return verify(prefix(username),"password",password);}
    public synchronized boolean verifyRecovery(String username,char[]answer)throws Exception{char[]normalized=normalizeAnswer(answer);try{return verify(prefix(username),"answer",normalized);}finally{Arrays.fill(normalized,'\0');}}
    public synchronized void changePassword(String username,char[]current,char[]replacement)throws Exception{if(!authenticate(username,current))throw new IllegalArgumentException("Current password is incorrect.");setPassword(username,replacement);}
    public synchronized void resetPassword(String username,char[]answer,char[]replacement)throws Exception{if(!exists(username))throw new IllegalArgumentException("Username was not found.");if(!verifyRecovery(username,answer))throw new IllegalArgumentException("Recovery answer is incorrect.");setPassword(username,replacement);}
    private void setPassword(String username,char[]password)throws Exception{validatePassword(password);byte[]salt=salt();String p=prefix(username);data.setProperty(p+"passwordSalt",encode(salt));data.setProperty(p+"passwordHash",encode(hash(password,salt)));save();}
    private boolean verify(String prefix,String kind,char[]secret)throws Exception{String ss=data.getProperty(prefix+kind+"Salt"),hs=data.getProperty(prefix+kind+"Hash");if(ss==null||hs==null)return false;return MessageDigest.isEqual(Base64.getDecoder().decode(hs),hash(secret,Base64.getDecoder().decode(ss)));}
    private void save()throws IOException{Path parent=file.toAbsolutePath().getParent();if(parent!=null)Files.createDirectories(parent);Path temp=Files.createTempFile(parent,"accounts-",".tmp");try(OutputStream out=Files.newOutputStream(temp)){data.store(out,"Statistical Solutions local accounts");}try{Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}}
    private byte[]hash(char[]secret,byte[]salt)throws Exception{PBEKeySpec spec=new PBEKeySpec(secret,salt,ITERATIONS,BITS);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}}
    private byte[]salt(){byte[]s=new byte[16];random.nextBytes(s);return s;}private static String encode(byte[]b){return Base64.getEncoder().encodeToString(b);}
    private static String prefix(String username){String n=username==null?"":username.trim().toLowerCase(Locale.ROOT);return "u."+Base64.getUrlEncoder().withoutPadding().encodeToString(n.getBytes(StandardCharsets.UTF_8))+".";}
    private static void validateUsername(String username){String u=username==null?"":username.trim();if(!u.matches("[A-Za-z0-9._-]{3,32}"))throw new IllegalArgumentException("Username must be 3-32 characters using letters, numbers, dot, underscore or hyphen.");}
    private static void validatePassword(char[]password){if(password==null||password.length<8)throw new IllegalArgumentException("Password must contain at least 8 characters.");boolean letter=false,number=false;for(char c:password){letter|=Character.isLetter(c);number|=Character.isDigit(c);}if(!letter||!number)throw new IllegalArgumentException("Password must include at least one letter and one number.");}
    private static char[]normalizeAnswer(char[]answer){return new String(answer).trim().toLowerCase(Locale.ROOT).toCharArray();}
    private static Path defaultPath(){String appData=System.getenv("APPDATA");return appData!=null&&!appData.isBlank()?Path.of(appData,"Statistical Solutions","accounts.properties"):Path.of(System.getProperty("user.home"),".statistical-solutions","accounts.properties");}
}
