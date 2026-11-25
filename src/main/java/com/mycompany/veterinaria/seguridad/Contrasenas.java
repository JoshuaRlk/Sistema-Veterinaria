package com.mycompany.veterinaria.seguridad;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Cifrado de contraseñas con PBKDF2 (incluido en el JDK, no requiere librerías).
 * Formato guardado en usuario.contrasena_hash:  pbkdf2$iteraciones$salBase64$hashBase64
 * Nunca se guarda la contraseña en texto plano.
 */
public final class Contrasenas {

    private static final int ITERACIONES = 65536;
    private static final int BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Contrasenas() {
    }

    public static String cifrar(String contrasena) {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        return "pbkdf2$" + ITERACIONES + "$" + b64(sal) + "$" + b64(derivar(contrasena, sal, ITERACIONES));
    }

    public static boolean coincide(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        String[] p = guardado.split("\\$");
        if (p.length != 4 || !"pbkdf2".equals(p[0])) {
            return false; // por ejemplo 'pendiente_bcrypt': usuario sin contraseña configurada
        }
        try {
            byte[] sal = Base64.getDecoder().decode(p[2]);
            byte[] esperado = Base64.getDecoder().decode(p[3]);
            byte[] calculado = derivar(contrasena, sal, Integer.parseInt(p[1]));
            return java.security.MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derivar(String contrasena, byte[] sal, int iteraciones) {
        try {
            KeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", ex);
        }
    }

    private static String b64(byte[] datos) {
        return Base64.getEncoder().encodeToString(datos);
    }
}
