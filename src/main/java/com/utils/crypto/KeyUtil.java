package com.utils.crypto;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import com.utils.core.lang.Assert;
import com.utils.core.util.*;
import com.utils.crypto.asymmetric.AsymmetricAlgorithm;
import com.utils.crypto.symmetric.SymmetricAlgorithm;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.*;

/**
 * 密钥工具类
 * 
 * @author looly, Gsealy
 * @since 4.4.1
 */
public class KeyUtil {

	/** Java密钥库(Java Key Store，JKS)KEY_STORE */
	public static final String KEY_STORE = "JKS";
	public static final String X509 = "X.509";

	/**
	 * 默认密钥字节数
	 * 
	 * <pre>
	 * RSA/DSA
	 * Default Keysize 1024
	 * Keysize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
	 * </pre>
	 */
	public static final int DEFAULT_KEY_SIZE = 1024;

	/**
	 * SM2默认曲线
	 * 
	 * <pre>
	 * Default SM2 curve
	 * </pre>
	 */
	public static final String SM2_DEFAULT_CURVE = "sm2p256v1";

	/**
	 * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
	 * 
	 * @param algorithm 算法，支持PBE算法
	 * @return {@link SecretKey}
	 */
	public static SecretKey generateKey(String algorithm) {
		return generateKey(algorithm, -1);
	}

	/**
	 * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
	 * 
	 * @param algorithm 算法，支持PBE算法
	 * @param keySize 密钥长度
	 * @return {@link SecretKey}
	 * @since 3.1.2
	 */
	public static SecretKey generateKey(String algorithm, int keySize) {
		algorithm = getMainAlgorithm(algorithm);
		
		final KeyGenerator keyGenerator = getKeyGenerator(algorithm);
		if (keySize > 0) {
			keyGenerator.init(keySize);
		} else if (SymmetricAlgorithm.AES.getValue().equals(algorithm)) {
			// 对于AES的密钥，除非指定，否则强制使用128位
			keyGenerator.init(128);
		}
		return keyGenerator.generateKey();
	}

	/**
	 * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
	 * 
	 * @param algorithm 算法
	 * @param key 密钥，如果为{@code null} 自动生成随机密钥
	 * @return {@link SecretKey}
	 */
	public static SecretKey generateKey(String algorithm, byte[] key) {
		Assert.notBlank(algorithm, "Algorithm is blank!");
		SecretKey secretKey = null;
		if (algorithm.startsWith("PBE")) {
			// PBE密钥
			secretKey = generatePBEKey(algorithm, (null == key) ? null : StrUtil.str(key, CharsetUtil.CHARSET_UTF_8).toCharArray());
		} else if (algorithm.startsWith("DES")) {
			// DES密钥
			secretKey = generateDESKey(algorithm, key);
		} else {
			// 其它算法密钥
			secretKey = (null == key) ? generateKey(algorithm) : new SecretKeySpec(key, algorithm);
		}
		return secretKey;
	}

	/**
	 * 生成 {@link SecretKey}
	 * 
	 * @param algorithm DES算法，包括DES、DESede等
	 * @param key 密钥
	 * @return {@link SecretKey}
	 */
	public static SecretKey generateDESKey(String algorithm, byte[] key) {
		if (StrUtil.isBlank(algorithm) || false == algorithm.startsWith("DES")) {
			throw new CryptoException("Algorithm [{}] is not a DES algorithm!");
		}

		SecretKey secretKey = null;
		if (null == key) {
			secretKey = generateKey(algorithm);
		} else {
			KeySpec keySpec;
			try {
				if (algorithm.startsWith("DESede")) {
					// DESede兼容
					keySpec = new DESedeKeySpec(key);
				} else {
					keySpec = new DESKeySpec(key);
				}
			} catch (InvalidKeyException e) {
				throw new CryptoException(e);
			}
			secretKey = generateKey(algorithm, keySpec);
		}
		return secretKey;
	}

	/**
	 * 生成PBE {@link SecretKey}
	 * 
	 * @param algorithm PBE算法，包括：PBEWithMD5AndDES、PBEWithSHA1AndDESede、PBEWithSHA1AndRC2_40等
	 * @param key 密钥
	 * @return {@link SecretKey}
	 */
	public static SecretKey generatePBEKey(String algorithm, char[] key) {
		if (StrUtil.isBlank(algorithm) || false == algorithm.startsWith("PBE")) {
			throw new CryptoException("Algorithm [{}] is not a PBE algorithm!");
		}

		if (null == key) {
			key = RandomUtil.randomString(32).toCharArray();
		}
		PBEKeySpec keySpec = new PBEKeySpec(key);
		return generateKey(algorithm, keySpec);
	}

	/**
	 * 生成 {@link SecretKey}，仅用于对称加密和摘要算法
	 * 
	 * @param algorithm 算法
	 * @param keySpec {@link KeySpec}
	 * @return {@link SecretKey}
	 */
	public static SecretKey generateKey(String algorithm, KeySpec keySpec) {
		final SecretKeyFactory keyFactory = getSecretKeyFactory(algorithm);
		try {
			return keyFactory.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 生成RSA私钥，仅用于非对称加密<br>
	 * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法<br>
	 * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
	 * 
	 * @param key 密钥，必须为DER编码存储
	 * @return RSA私钥 {@link PrivateKey}
	 * @since 4.5.2
	 */
	public static PrivateKey generateRSAPrivateKey(byte[] key) {
		return generatePrivateKey(AsymmetricAlgorithm.RSA.getValue(), key);
	}

	/**
	 * 生成私钥，仅用于非对称加密<br>
	 * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法<br>
	 * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
	 * 
	 * @param algorithm 算法
	 * @param key 密钥，必须为DER编码存储
	 * @return 私钥 {@link PrivateKey}
	 */
	public static PrivateKey generatePrivateKey(String algorithm, byte[] key) {
		if (null == key) {
			return null;
		}
		return generatePrivateKey(algorithm, new PKCS8EncodedKeySpec(key));
	}

	/**
	 * 生成私钥，仅用于非对称加密<br>
	 * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
	 * 
	 * @param algorithm 算法
	 * @param keySpec {@link KeySpec}
	 * @return 私钥 {@link PrivateKey}
	 * @since 3.1.1
	 */
	public static PrivateKey generatePrivateKey(String algorithm, KeySpec keySpec) {
		if (null == keySpec) {
			return null;
		}
		algorithm = getAlgorithmAfterWith(algorithm);
		try {
			return getKeyFactory(algorithm).generatePrivate(keySpec);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 生成私钥，仅用于非对称加密
	 * 
	 * @param keyStore {@link KeyStore}
	 * @param alias 别名
	 * @param password 密码
	 * @return 私钥 {@link PrivateKey}
	 */
	public static PrivateKey generatePrivateKey(KeyStore keyStore, String alias, char[] password) {
		try {
			return (PrivateKey) keyStore.getKey(alias, password);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 生成RSA公钥，仅用于非对称加密<br>
	 * 采用X509证书规范<br>
	 * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
	 * 
	 * @param key 密钥，必须为DER编码存储
	 * @return 公钥 {@link PublicKey}
	 * @since 4.5.2
	 */
	public static PublicKey generateRSAPublicKey(byte[] key) {
		return generatePublicKey(AsymmetricAlgorithm.RSA.getValue(), key);
	}

	/**
	 * 生成公钥，仅用于非对称加密<br>
	 * 采用X509证书规范<br>
	 * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
	 * 
	 * @param algorithm 算法
	 * @param key 密钥，必须为DER编码存储
	 * @return 公钥 {@link PublicKey}
	 */
	public static PublicKey generatePublicKey(String algorithm, byte[] key) {
		if (null == key) {
			return null;
		}
		return generatePublicKey(algorithm, new X509EncodedKeySpec(key));
	}

	/**
	 * 生成公钥，仅用于非对称加密<br>
	 * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
	 * 
	 * @param algorithm 算法
	 * @param keySpec {@link KeySpec}
	 * @return 公钥 {@link PublicKey}
	 * @since 3.1.1
	 */
	public static PublicKey generatePublicKey(String algorithm, KeySpec keySpec) {
		if (null == keySpec) {
			return null;
		}
		algorithm = getAlgorithmAfterWith(algorithm);
		try {
			return getKeyFactory(algorithm).generatePublic(keySpec);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 生成用于非对称加密的公钥和私钥，仅用于非对称加密<br>
	 * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
	 * 
	 * @param algorithm 非对称加密算法
	 * @return {@link KeyPair}
	 */
	public static KeyPair generateKeyPair(String algorithm) {
		return generateKeyPair(algorithm, DEFAULT_KEY_SIZE);
	}

	/**
	 * 生成用于非对称加密的公钥和私钥<br>
	 * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
	 * 
	 * @param algorithm 非对称加密算法
	 * @param keySize 密钥模（modulus ）长度
	 * @return {@link KeyPair}
	 */
	public static KeyPair generateKeyPair(String algorithm, int keySize) {
		return generateKeyPair(algorithm, keySize, null);
	}

	/**
	 * 生成用于非对称加密的公钥和私钥<br>
	 * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
	 * 
	 * @param algorithm 非对称加密算法
	 * @param keySize 密钥模（modulus ）长度
	 * @param seed 种子
	 * @return {@link KeyPair}
	 */
	public static KeyPair generateKeyPair(String algorithm, int keySize, byte[] seed) {
		// SM2算法需要单独定义其曲线生成
		if ("SM2".equalsIgnoreCase(algorithm)) {
			final ECGenParameterSpec sm2p256v1 = new ECGenParameterSpec(SM2_DEFAULT_CURVE);
			return generateKeyPair(algorithm, keySize, seed, sm2p256v1);
		}

		return generateKeyPair(algorithm, keySize, seed, (AlgorithmParameterSpec[]) null);
	}

	/**
	 * 生成用于非对称加密的公钥和私钥<br>
	 * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
	 * 
	 * @param algorithm 非对称加密算法
	 * @param params {@link AlgorithmParameterSpec}
	 * @return {@link KeyPair}
	 * @since 4.3.3
	 */
	public static KeyPair generateKeyPair(String algorithm, AlgorithmParameterSpec params) {
		return generateKeyPair(algorithm, null, params);
	}

	/**
	 * 生成用于非对称加密的公钥和私钥<br>
	 * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
	 * 
	 * @param algorithm 非对称加密算法
	 * @param param {@link AlgorithmParameterSpec}
	 * @param seed 种子
	 * @return {@link KeyPair}
	 * @since 4.3.3
	 */
	public static KeyPair generateKeyPair(String algorithm, byte[] seed, AlgorithmParameterSpec param) {
		return generateKeyPair(algorithm, DEFAULT_KEY_SIZE, seed, param);
	}

	/**
	 * 生成用于非对称加密的公钥和私钥<br>
	 * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
	 * 
	 * @param algorithm 非对称加密算法
	 * @param keySize 密钥模（modulus ）长度
	 * @param seed 种子
	 * @param params {@link AlgorithmParameterSpec}
	 * @return {@link KeyPair}
	 * @since 4.3.3
	 */
	public static KeyPair generateKeyPair(String algorithm, int keySize, byte[] seed, AlgorithmParameterSpec... params) {
		algorithm = getAlgorithmAfterWith(algorithm);
		final KeyPairGenerator keyPairGen = getKeyPairGenerator(algorithm);

		// 密钥模（modulus ）长度初始化定义
		if (keySize > 0) {
			// key长度适配修正
			if ("EC".equalsIgnoreCase(algorithm) && keySize > 256) {
				// 对于EC算法，密钥长度有限制，在此使用默认256
				keySize = 256;
			}
			if (null != seed) {
				keyPairGen.initialize(keySize, new SecureRandom(seed));
			} else {
				keyPairGen.initialize(keySize);
			}
		}

		// 自定义初始化参数
		if (ArrayUtil.isNotEmpty(params)) {
			for (AlgorithmParameterSpec param : params) {
				if (null == param) {
					continue;
				}
				try {
					if (null != seed) {
						keyPairGen.initialize(param, new SecureRandom(seed));
					} else {
						keyPairGen.initialize(param);
					}
				} catch (InvalidAlgorithmParameterException e) {
					throw new CryptoException(e);
				}
			}
		}
		return keyPairGen.generateKeyPair();
	}

	/**
	 * 获取{@link KeyPairGenerator}
	 * 
	 * @param algorithm 非对称加密算法
	 * @return {@link KeyPairGenerator}
	 * @since 4.4.3
	 */
	public static KeyPairGenerator getKeyPairGenerator(String algorithm) {
		final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();

		KeyPairGenerator keyPairGen;
		try {
			keyPairGen = (null == provider) //
					? KeyPairGenerator.getInstance(getMainAlgorithm(algorithm)) //
					: KeyPairGenerator.getInstance(getMainAlgorithm(algorithm), provider);//
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		return keyPairGen;
	}

	/**
	 * 获取{@link KeyFactory}
	 * 
	 * @param algorithm 非对称加密算法
	 * @return {@link KeyFactory}
	 * @since 4.4.4
	 */
	public static KeyFactory getKeyFactory(String algorithm) {
		final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();

		KeyFactory keyFactory;
		try {
			keyFactory = (null == provider) //
					? KeyFactory.getInstance(getMainAlgorithm(algorithm)) //
					: KeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		return keyFactory;
	}

	/**
	 * 获取{@link SecretKeyFactory}
	 * 
	 * @param algorithm 对称加密算法
	 * @return {@link KeyFactory}
	 * @since 4.5.2
	 */
	public static SecretKeyFactory getSecretKeyFactory(String algorithm) {
		final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();

		SecretKeyFactory keyFactory;
		try {
			keyFactory = (null == provider) //
					? SecretKeyFactory.getInstance(getMainAlgorithm(algorithm)) //
					: SecretKeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		return keyFactory;
	}

	/**
	 * 获取{@link KeyGenerator}
	 * 
	 * @param algorithm 对称加密算法
	 * @return {@link KeyGenerator}
	 * @since 4.5.2
	 */
	public static KeyGenerator getKeyGenerator(String algorithm) {
		final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();

		KeyGenerator generator;
		try {
			generator = (null == provider) //
					? KeyGenerator.getInstance(getMainAlgorithm(algorithm)) //
					: KeyGenerator.getInstance(getMainAlgorithm(algorithm), provider);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		return generator;
	}

	/**
	 * 获取主体算法名，例如RSA/ECB/PKCS1Padding的主体算法是RSA
	 * 
	 * @return 主体算法名
	 * @since 4.5.2
	 */
	public static String getMainAlgorithm(String algorithm) {
		final int slashIndex = algorithm.indexOf(CharUtil.SLASH);
		if (slashIndex > 0) {
			return algorithm.substring(0, slashIndex);
		}
		return algorithm;
	}

	/**
	 * 获取用于密钥生成的算法<br>
	 * 获取XXXwithXXX算法的后半部分算法，如果为ECDSA或SM2，返回算法为EC
	 * 
	 * @param algorithm XXXwithXXX算法
	 * @return 算法
	 */
	public static String getAlgorithmAfterWith(String algorithm) {
		Assert.notNull(algorithm, "algorithm must be not null !");
		int indexOfWith = StrUtil.lastIndexOfIgnoreCase(algorithm, "with");
		if (indexOfWith > 0) {
			algorithm = StrUtil.subSuf(algorithm, indexOfWith + "with".length());
		}
		if ("ECDSA".equalsIgnoreCase(algorithm) || "SM2".equalsIgnoreCase(algorithm)) {
			algorithm = "EC";
		}
		return algorithm;
	}

	/**
	 * 读取密钥库(Java Key Store，JKS) KeyStore文件<br>
	 * KeyStore文件用于数字证书的密钥对保存<br>
	 * see: http://snowolf.iteye.com/blog/391931
	 * 
	 * @param in {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @param password 密码
	 * @return {@link KeyStore}
	 */
	public static KeyStore readJKSKeyStore(InputStream in, char[] password) {
		return readKeyStore(KEY_STORE, in, password);
	}

	/**
	 * 读取KeyStore文件<br>
	 * KeyStore文件用于数字证书的密钥对保存<br>
	 * see: http://snowolf.iteye.com/blog/391931
	 * 
	 * @param type 类型
	 * @param in {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @param password 密码
	 * @return {@link KeyStore}
	 */
	public static KeyStore readKeyStore(String type, InputStream in, char[] password) {
		KeyStore keyStore = null;
		try {
			keyStore = KeyStore.getInstance(type);
			keyStore.load(in, password);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
		return keyStore;
	}

	/**
	 * 从KeyStore中获取私钥公钥
	 * 
	 * @param type 类型
	 * @param in {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @param password 密码
	 * @param alias 别名
	 * @return {@link KeyPair}
	 * @since 4.4.1
	 */
	public static KeyPair getKeyPair(String type, InputStream in, char[] password, String alias) {
		final KeyStore keyStore = readKeyStore(type, in, password);
		return getKeyPair(keyStore, password, alias);
	}

	/**
	 * 从KeyStore中获取私钥公钥
	 * 
	 * @param keyStore {@link KeyStore}
	 * @param password 密码
	 * @param alias 别名
	 * @return {@link KeyPair}
	 * @since 4.4.1
	 */
	public static KeyPair getKeyPair(KeyStore keyStore, char[] password, String alias) {
		PublicKey publicKey;
		PrivateKey privateKey;
		try {
			publicKey = keyStore.getCertificate(alias).getPublicKey();
			privateKey = (PrivateKey) keyStore.getKey(alias, password);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * 读取X.509 Certification文件<br>
	 * Certification为证书文件<br>
	 * see: http://snowolf.iteye.com/blog/391931
	 * 
	 * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @param password 密码
	 * @param alias 别名
	 * @return {@link KeyStore}
	 * @since 4.4.1
	 */
	public static Certificate readX509Certificate(InputStream in, char[] password, String alias) {
		return readCertificate(X509, in, password, alias);
	}

	/**
	 * 读取X.509 Certification文件中的公钥<br>
	 * Certification为证书文件<br>
	 * see: https://www.cnblogs.com/yinliang/p/10115519.html
	 * 
	 * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @return {@link KeyStore}
	 * @since 4.5.2
	 */
	public static PublicKey readPublicKeyFromCert(InputStream in) {
		final Certificate certificate = readX509Certificate(in);
		if (null != certificate) {
			return certificate.getPublicKey();
		}
		return null;
	}

	/**
	 * 读取X.509 Certification文件<br>
	 * Certification为证书文件<br>
	 * see: http://snowolf.iteye.com/blog/391931
	 * 
	 * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @return {@link KeyStore}
	 * @since 4.4.1
	 */
	public static Certificate readX509Certificate(InputStream in) {
		return readCertificate(X509, in);
	}

	/**
	 * 读取Certification文件<br>
	 * Certification为证书文件<br>
	 * see: http://snowolf.iteye.com/blog/391931
	 * 
	 * @param type 类型，例如X.509
	 * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @param password 密码
	 * @param alias 别名
	 * @return {@link KeyStore}
	 * @since 4.4.1
	 */
	public static Certificate readCertificate(String type, InputStream in, char[] password, String alias) {
		final KeyStore keyStore = readKeyStore(type, in, password);
		try {
			return keyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 读取Certification文件<br>
	 * Certification为证书文件<br>
	 * see: http://snowolf.iteye.com/blog/391931
	 * 
	 * @param type 类型，例如X.509
	 * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(java.io.File)} 读取
	 * @return {@link Certificate}
	 */
	public static Certificate readCertificate(String type, InputStream in) {
		try {
			return getCertificateFactory(type).generateCertificate(in);
		} catch (CertificateException e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 获得 Certification
	 * 
	 * @param keyStore {@link KeyStore}
	 * @param alias 别名
	 * @return {@link Certificate}
	 */
	public static Certificate getCertificate(KeyStore keyStore, String alias) {
		try {
			return keyStore.getCertificate(alias);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 获取{@link CertificateFactory}
	 * 
	 * @param type 类型，例如X.509
	 * @return {@link KeyPairGenerator}
	 * @since 4.5.0
	 */
	public static CertificateFactory getCertificateFactory(String type) {
		final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();

		CertificateFactory factory;
		try {
			factory = (null == provider) ? CertificateFactory.getInstance(type) : CertificateFactory.getInstance(type, provider);
		} catch (CertificateException e) {
			throw new CryptoException(e);
		}
		return factory;
	}

	/**
	 * 编码压缩EC公钥（基于BouncyCastle）<br>
	 * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
	 * 
	 * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
	 * @return 压缩得到的X
	 * @since 4.4.4
	 */
	public static byte[] encodeECPublicKey(PublicKey publicKey) {
		return BCUtil.encodeECPublicKey(publicKey);
	}

	/**
	 * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）<br>
	 * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
	 * 
	 * @param encode 压缩公钥
	 * @param curveName EC曲线名
	 * @since 4.4.4
	 */
	public static PublicKey decodeECPoint(String encode, String curveName) {
		return BCUtil.decodeECPoint(encode, curveName);
	}

	/**
	 * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）<br>
	 * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
	 * 
	 * @param encodeByte 压缩公钥
	 * @param curveName EC曲线名
	 * @since 4.4.4
	 */
	public static PublicKey decodeECPoint(byte[] encodeByte, String curveName) {
		return BCUtil.decodeECPoint(encodeByte, curveName);
	}
}
