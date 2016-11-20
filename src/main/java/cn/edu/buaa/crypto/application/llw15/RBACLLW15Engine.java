package cn.edu.buaa.crypto.application.llw15;

import cn.edu.buaa.crypto.algebra.genparams.AsymmetricKeySerPair;
import cn.edu.buaa.crypto.algebra.genparams.PairingKeyEncapsulationSerPair;
import cn.edu.buaa.crypto.algebra.serparams.AsymmetricKeySerParameter;
import cn.edu.buaa.crypto.algebra.serparams.PairingCipherSerParameter;
import cn.edu.buaa.crypto.application.llw15.generators.*;
import cn.edu.buaa.crypto.application.llw15.genparams.*;
import cn.edu.buaa.crypto.application.llw15.serparams.*;
import it.unisa.dia.gas.jpbc.PairingParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 * Created by Weiran Liu on 16/5/17.
 *
 * Liu-Liu-Wu EHR role-based access control engine.
 */
public class RBACLLW15Engine {
    //Scheme name, used for exceptions
    public static final String SCHEME_NAME = "Liu-Liu-Wu-15 EHR Role-Based Access Control scheme";

    private static RBACLLW15Engine engine;

    public static RBACLLW15Engine getInstance() {
        if (engine == null) {
            engine = new RBACLLW15Engine();
        }
        return engine;
    }

    private RBACLLW15Engine() {

    }

    /**
     * Setup algorithm
     * @param pairingParameters the base pairing groups.
     * @param maxRoleNumber maximal number of atom roles
     * @return public key / master secret key pairs
     */
    public AsymmetricKeySerPair Setup(PairingParameters pairingParameters, int maxRoleNumber) {
        RBACLLW15KeyPairGenerator keyPairGenerator = new RBACLLW15KeyPairGenerator();
        keyPairGenerator.init(new RBACLLW15KeyPairGenerationParameter(pairingParameters, maxRoleNumber));

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Intermediate parameter generation lagorithm
     * @param publicKey public key
     * @return intermediate parameters
     */
    public PairingCipherSerParameter IntermediateGen(AsymmetricKeySerParameter publicKey) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)) {
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
        RBACLLW15IntermediateGenerator intermediateGenerator = new RBACLLW15IntermediateGenerator();
        intermediateGenerator.init(new RBACLLW15IntermediateGenParameter(publicKey));
        return intermediateGenerator.generateIntermadiateParameters();
    }

    /**
     * Patient access credential generation algorithm
     * @param publicKey public key
     * @param masterKey master secret key
     * @param id patient's id
     * @return access credential for the patient associated with id
     */
    public AsymmetricKeySerParameter ACGenP(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter masterKey, String id) {
        isValidACGenParameters(publicKey, masterKey);
        RBACLLW15AccessCredentialPGenerator secretKeyGenerator = new RBACLLW15AccessCredentialPGenerator();
        secretKeyGenerator.init(new RBACLLW15AccessCredentialPGenParameter(
                publicKey, masterKey, id));

        return secretKeyGenerator.generateKey();
    }

    /**
     * Patient access credential generation algorithm using intermediate parameters
     * @param publicKey public key
     * @param masterKey master secret key
     * @param intermediateParameters intermediate parameters
     * @param id patient's id
     * @return access credential for the patient associated with id
     */
    public AsymmetricKeySerParameter ACGenP(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter masterKey,
                                   PairingCipherSerParameter intermediateParameters, String id) {
        isValidACGenParameters(publicKey, masterKey);
        RBACLLW15AccessCredentialPGenerator secretKeyGenerator = new RBACLLW15AccessCredentialPGenerator();
        secretKeyGenerator.init(new RBACLLW15AccessCredentialPGenParameter(
                publicKey, masterKey, intermediateParameters, id));

        return secretKeyGenerator.generateKey();
    }

    /**
     * Medical staff access credential generation algorithm
     * @param publicKey public key
     * @param masterKey master secret key
     * @param roles role vectors
     * @param time valid time
     * @return access credential for the medical staff associated with roles
     */
    public AsymmetricKeySerParameter ACGenM(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter masterKey, String[] roles, String time) {
        isValidACGenParameters(publicKey, masterKey);
        RBACLLW15AccessCredentialMGenerator secretKeyGenerator = new RBACLLW15AccessCredentialMGenerator();
        secretKeyGenerator.init(new RBACLLW15AccessCredentialMGenParameter(
                publicKey, masterKey, roles, time));

        return secretKeyGenerator.generateKey();
    }

    /**
     * Medical staff access credential generation algorithm using intermediate parameters
     * @param publicKey public key
     * @param masterKey master secret key
     * @param intermediateParameters intermediate parameters
     * @param roles role vectors
     * @param time valid time
     * @return access credential for the medical staff associated with roles
     */
    public AsymmetricKeySerParameter ACGenM(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter masterKey,
                                   PairingCipherSerParameter intermediateParameters, String[] roles, String time) {
        isValidACGenParameters(publicKey, masterKey);
        RBACLLW15AccessCredentialMGenerator secretKeyGenerator = new RBACLLW15AccessCredentialMGenerator();
        secretKeyGenerator.init(new RBACLLW15AccessCredentialMGenParameter(
                publicKey, masterKey, intermediateParameters, roles, time));

        return secretKeyGenerator.generateKey();
    }

    private void isValidACGenParameters(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter masterKey) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
        if (!(masterKey instanceof RBACLLW15MasterSecretKeySerParameter)) {
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + masterKey.getClass().getName() + ", require"
                            + RBACLLW15MasterSecretKeySerParameter.class.getName());
        }
    }

    /**
     * Medical staff access credential delegation algorithm
     * @param publicKey public key
     * @param accessCredentialM parent medical staff access credential
     * @param index delegated role index
     * @param role delegated role
     * @return access credential for the medical staff associated with delegated role
     */
    public AsymmetricKeySerParameter ACDeleM(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter accessCredentialM, int index, String role) {
        isValidACDeleMParameters(publicKey, accessCredentialM);
        RBACLLW15AccessCredentialMGenerator secretKeyGenerator = new RBACLLW15AccessCredentialMGenerator();
        secretKeyGenerator.init(new RBACLLW15AccessCredentialMDeleParameter(
                publicKey, accessCredentialM, index, role));

        return secretKeyGenerator.generateKey();
    }

    /**
     * Medical staff access credential delegation algorithm using intermediate parameters
     * @param publicKey public key
     * @param accessCredentialM parent medical staff access credential
     * @param intermediateParameters intermediate parameters
     * @param index delegated role index
     * @param role delegated role
     * @return access credential for the medical staff associated with delegated role
     */
    public AsymmetricKeySerParameter ACDeleM(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter accessCredentialM,
                                    PairingCipherSerParameter intermediateParameters, int index, String role) {
        isValidACDeleMParameters(publicKey, accessCredentialM);
        RBACLLW15AccessCredentialMGenerator secretKeyGenerator = new RBACLLW15AccessCredentialMGenerator();
        secretKeyGenerator.init(new RBACLLW15AccessCredentialMDeleParameter(
                publicKey, accessCredentialM, intermediateParameters, index, role));

        return secretKeyGenerator.generateKey();
    }

    private void isValidACDeleMParameters(AsymmetricKeySerParameter publicKey, AsymmetricKeySerParameter accessCredentialM) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
        if (!(accessCredentialM instanceof RBACLLW15AccessCredentialMSerParameter)) {
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + accessCredentialM.getClass().getName() + ", require"
                            + RBACLLW15AccessCredentialMSerParameter.class.getName());
        }
    }

    /**
     * key encapsulation algorithm
     * @param publicKey public key
     * @param id patient's id
     * @param roles associated roles
     * @param time valid time
     * @return ciphertext / sesscion key pair
     */
    public PairingKeyEncapsulationSerPair EHREnc(AsymmetricKeySerParameter publicKey, String id, String[] roles, String time) {
        isValidKeyEncapsulationParameters(publicKey);
        RBACLLW15EncapsulationPairGenerator keyEncapsulationPairGenerator = new RBACLLW15EncapsulationPairGenerator();
        keyEncapsulationPairGenerator.init(new RBACLLW15EncapsulationGenParameter(
                publicKey, id, roles, time));

        return keyEncapsulationPairGenerator.generateEncryptionPair();
    }

    /**
     * key encapsulation algorithm using intermediate parameters
     * @param publicKey public key
     * @param intermediateParameters intermediate parameters
     * @param id patient's id
     * @param roles associated roles
     * @param time valid time
     * @return ciphertext / session key pair
     */
    public PairingKeyEncapsulationSerPair EHREnc(AsymmetricKeySerParameter publicKey, PairingCipherSerParameter intermediateParameters,
                                                 String id, String[] roles, String time) {
        isValidKeyEncapsulationParameters(publicKey);
        RBACLLW15EncapsulationPairGenerator keyEncapsulationPairGenerator = new RBACLLW15EncapsulationPairGenerator();
        keyEncapsulationPairGenerator.init(new RBACLLW15EncapsulationGenParameter(
                publicKey, intermediateParameters, id, roles, time));

        return keyEncapsulationPairGenerator.generateEncryptionPair();
    }

    private void isValidKeyEncapsulationParameters(AsymmetricKeySerParameter publicKey) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
    }

    /**
     * EHR encapsulation audit algorithm
     * @param publicKey public key
     * @param id patient's id
     * @param roles associated roles
     * @param time valid time
     * @param encapsulation ciphertext
     * @return true if valid, false if invalid
     */
    public boolean EHRAudit(AsymmetricKeySerParameter publicKey, String id, String[] roles, String time, PairingCipherSerParameter encapsulation) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
        if (!(encapsulation instanceof RBACLLW15EncapsulationSerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + encapsulation.getClass().getName() + ", require "
                            + RBACLLW15EncapsulationSerParameter.class.getName());
        }
        RBACLLW15EncapsulationAudit encapsulationAudit = new RBACLLW15EncapsulationAudit();
        encapsulationAudit.init(new RBACLLW15EncapsulationAuditParameter(
                publicKey, id, roles, time, encapsulation));
        return encapsulationAudit.audit();
    }

    /**
     * EHR medical staff decapsulation algorithm without encapsulation audit procedure
     * @param publicKey public key
     * @param id patient's id
     * @param roles associated roles
     * @param time valid time
     * @param encapsulation ciphertext
     * @param accessCredentialM medical staff access credential
     * @return decapsulated session key
     */
    public byte[] EHRDecM (
            AsymmetricKeySerParameter publicKey, String id, String[] roles, String time,
            PairingCipherSerParameter encapsulation, AsymmetricKeySerParameter accessCredentialM) throws InvalidCipherTextException {
        isValidAccessCredentialMDecapsulationParameters(publicKey, encapsulation, accessCredentialM);
        RBACLLW15DecapsulationMGenerator keyDecapsulationGenerator = new RBACLLW15DecapsulationMGenerator();
        keyDecapsulationGenerator.init(new RBACLLW15DecapsulationMParameter(
                publicKey, accessCredentialM, id, roles, time, encapsulation));
        return keyDecapsulationGenerator.recoverKey();
    }

    /**
     * EHR medical staff decapsulation algorithm with encapsulation audit procedure
     * @param publicKey public key
     * @param id patient's id
     * @param roles associated roles
     * @param time valid time
     * @param encapsulation ciphertext
     * @param accessCredentialM medical staff access credential
     * @return decapsulated session key
     * @throws InvalidCipherTextException if EHR encapsulation is invalid
     */
    public byte[] EHRDecMWithAudit (
            AsymmetricKeySerParameter publicKey, String id, String[] roles, String time,
            PairingCipherSerParameter encapsulation, AsymmetricKeySerParameter accessCredentialM) throws InvalidCipherTextException {
        isValidAccessCredentialMDecapsulationParameters(publicKey, encapsulation, accessCredentialM);
        if (!EHRAudit(publicKey, id, roles, time, encapsulation)) {
            throw new InvalidCipherTextException("Encapsulation is invalid due to EHRAudit");
        }
        return EHRDecM(publicKey, id, roles, time, encapsulation, accessCredentialM);
    }

    private void isValidAccessCredentialMDecapsulationParameters(
            AsymmetricKeySerParameter publicKey, PairingCipherSerParameter encapsulation, AsymmetricKeySerParameter accessCredentialM) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
        if (!(accessCredentialM instanceof RBACLLW15AccessCredentialMSerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + accessCredentialM.getClass().getName() + ", require "
                            + RBACLLW15AccessCredentialMSerParameter.class.getName());
        }
        if (!(encapsulation instanceof RBACLLW15EncapsulationSerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + encapsulation.getClass().getName() + ", require "
                            + RBACLLW15EncapsulationSerParameter.class.getName());
        }
    }

    /**
     * EHR patient decapsulation algorithm without encapsulation audit procedure
     * @param publicKey public key
     * @param id patient's id
     * @param roles assocciated roles
     * @param time valid time
     * @param encapsulation ciphertext
     * @param accessCredentialP patient access credential
     * @return decapsulated session key
     */
    public byte[] EHRDecP (
            AsymmetricKeySerParameter publicKey, String id, String[] roles, String time,
            PairingCipherSerParameter encapsulation, AsymmetricKeySerParameter accessCredentialP) throws InvalidCipherTextException {
        isValidAccessCredentialPDecapsulationParameters(publicKey, encapsulation, accessCredentialP);
        RBACLLW15DecapsulationPGenerator keyDecapsulationGenerator = new RBACLLW15DecapsulationPGenerator();
        keyDecapsulationGenerator.init(new RBACLLW15DecapsulationPParameter(
                publicKey, accessCredentialP, id, roles, time, encapsulation));
        return keyDecapsulationGenerator.recoverKey();
    }

    /**
     * EHR patient decapsulation algorithm with encapsulation audit procedure
     * @param publicKey public key
     * @param id patient's id
     * @param roles assocciated roles
     * @param time valid time
     * @param encapsulation ciphertext
     * @param accessCredentialP patient access credential
     * @return decapsulated session key
     */
    public byte[] EHRDecPWithAudit (
            AsymmetricKeySerParameter publicKey, String id, String[] roles, String time,
            PairingCipherSerParameter encapsulation, AsymmetricKeySerParameter accessCredentialP) throws InvalidCipherTextException {
        isValidAccessCredentialPDecapsulationParameters(publicKey, encapsulation, accessCredentialP);
        if (!EHRAudit(publicKey, id, roles, time, encapsulation)) {
            throw new InvalidCipherTextException("Encapsulation is invalid due to EHRAudit");
        }
        return EHRDecP(publicKey, id, roles, time, encapsulation, accessCredentialP);
    }

    private void isValidAccessCredentialPDecapsulationParameters(
            AsymmetricKeySerParameter publicKey, PairingCipherSerParameter encapsulation, AsymmetricKeySerParameter accessCredentialP) {
        if (!(publicKey instanceof RBACLLW15PublicKeySerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + publicKey.getClass().getName() + ", require "
                            + RBACLLW15PublicKeySerParameter.class.getName());
        }
        if (!(accessCredentialP instanceof RBACLLW15AccessCredentialPSerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + accessCredentialP.getClass().getName() + ", require "
                            + RBACLLW15AccessCredentialMSerParameter.class.getName());
        }
        if (!(encapsulation instanceof RBACLLW15EncapsulationSerParameter)){
            throw new IllegalArgumentException
                    ("Invalid CipherParameter Instance of " + SCHEME_NAME  + ", find "
                            + encapsulation.getClass().getName() + ", require "
                            + RBACLLW15EncapsulationSerParameter.class.getName());
        }
    }
}
