package xyz.destiall.tabheads.bukkit.tasks;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey;
import com.github.games647.craftapi.model.auth.Verification;
import com.github.games647.craftapi.resolver.MojangResolver;
import org.bukkit.entity.Player;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.bukkit.auth.ClientPublicKey;
import xyz.destiall.tabheads.bukkit.listener.ProtocolListener;
import xyz.destiall.tabheads.bukkit.session.BukkitLoginSession;
import xyz.destiall.tabheads.core.EncryptionUtil;
import xyz.destiall.tabheads.core.Tabheads;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.comphenix.protocol.PacketType.Login.Client.START;
import static com.comphenix.protocol.PacketType.Login.Server.DISCONNECT;
import static xyz.destiall.tabheads.bukkit.listener.ProtocolListener.V1_19;

public class VerifyResponseTask implements Runnable {
    private static final String ENCRYPTION_CLASS_NAME = "MinecraftEncryption";
    private static final Class<?> ENCRYPTION_CLASS;

    private static Method encryptMethod;
    private static Method cipherMethod;

    static {
        ENCRYPTION_CLASS = MinecraftReflection.getMinecraftClass("util." + ENCRYPTION_CLASS_NAME, ENCRYPTION_CLASS_NAME);
    }

    private final TabheadsBukkit plugin;
    private final PacketEvent packetEvent;
    private final KeyPair serverKey;
    private final Player player;
    private final byte[] sharedSecret;
    private final InetSocketAddress address;
    private final String username;

    public VerifyResponseTask(TabheadsBukkit plugin, PacketEvent packetEvent, Player player, String username, byte[] sharedSecret, KeyPair keyPair) {
        this.plugin = plugin;
        this.packetEvent = packetEvent;
        this.player = player;
        this.address = player.getAddress();
        this.username = username;
        this.sharedSecret = Arrays.copyOf(sharedSecret, sharedSecret.length);
        this.serverKey = keyPair;
    }

    @Override
    public void run() {
        try {
            BukkitLoginSession session = plugin.getSession(address);
            if (session == null) {
                disconnect(address + " tried to send encryption response in an invalid state");
            } else {
                verifyResponse(session);
            }
        } finally {
            synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
                packetEvent.setCancelled(true);
            }
            TabheadsBukkit.PROTOCOL.getAsynchronousManager().signalPacketTransmission(packetEvent);
        }
    }

    private void verifyResponse(BukkitLoginSession session) {
        PrivateKey privateKey = serverKey.getPrivate();
        SecretKey loginKey;
        try {
            loginKey = EncryptionUtil.decryptSharedKey(privateKey, sharedSecret);
        } catch (GeneralSecurityException securityEx) {
            disconnect("Cannot decrypt received contents", securityEx);
            return;
        }
        String serverId = EncryptionUtil.getServerIdHashString("", loginKey, serverKey.getPublic());
        String requestedUsername = session.getRequestUsername();
        try {
            if (!enableEncryption(loginKey) || !checkVerifyToken(session)) {
                return;
            }
        } catch (Exception ex) {
            disconnect("Cannot decrypt received contents", ex);
            return;
        }
        InetSocketAddress socketAddress = address;
        try {
            MojangResolver resolver = Tabheads.get().getResolver();
            InetAddress address = socketAddress.getAddress();
            Optional<Verification> response = resolver.hasJoined(requestedUsername, serverId, address);
            if (response.isPresent()) {
                Verification verification = response.get();
                if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                    Tabheads.get().getTabLogger().info(requestedUsername + " has a verified premium account");
                }
                String realUsername = verification.getName();
                if (realUsername == null) {
                    disconnect("Username field null for " + requestedUsername);
                    return;
                }
                receiveFakeStartPacket(realUsername, session.getClientPublicKey());
            } else {
                disconnect(session.getRequestUsername() + " (" + socketAddress + ") tried to log in with an invalid session [" + serverId + "]");
            }
        } catch (IOException ioEx) {
            disconnect("Failed to connect to session server", ioEx);
        }
    }

    private boolean checkVerifyToken(BukkitLoginSession session) throws GeneralSecurityException {
        byte[] requestVerify = session.getVerifyToken();
        byte[] responseVerify = packetEvent.getPacket().getByteArrays().read(MinecraftVersion.getCurrentVersion().isAtLeast(V1_19) ? 0 : 1);
        if (!Arrays.equals(requestVerify, EncryptionUtil.decrypt(serverKey.getPrivate(), responseVerify))) {
            disconnect(session.getRequestUsername() + " (" + address + ") tried to login with an invalid verify token [" + Arrays.toString(requestVerify) + "/" + Arrays.toString(responseVerify) + "]");
            return false;
        }
        return true;
    }

    private Object getNetworkManager() throws IllegalAccessException, ClassNotFoundException {
        Object injectorContainer = TemporaryPlayerFactory.getInjectorFromPlayer(player);
        Class<?> injectorClass = Class.forName("com.comphenix.protocol.injector.netty.Injector");
        Object rawInjector = FuzzyReflection.getFieldValue(injectorContainer, injectorClass, true);
        Field field = FuzzyReflection.fromClass(injectorClass).getFieldByName("networkManager");
        field.setAccessible(true);
        return field.get(rawInjector);
    }

    private boolean enableEncryption(SecretKey loginKey) throws IllegalArgumentException {
        if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
            Tabheads.get().getTabLogger().info("Enabling encryption for " + username);
        }
        if (encryptMethod == null) {
            Class<?> networkManagerClass = MinecraftReflection.getNetworkManagerClass();

            try {
                // Try to get the old (pre MC 1.16.4) encryption method
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", SecretKey.class);
            } catch (IllegalArgumentException exception) {
                // Get the new encryption method
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", Cipher.class, Cipher.class);

                // Get the needed Cipher helper method (used to generate ciphers from login key)
                cipherMethod = FuzzyReflection.fromClass(ENCRYPTION_CLASS)
                        .getMethodByParameters("a", int.class, Key.class);
            }
        }

        try {
            Object networkManager = this.getNetworkManager();

            // If cipherMethod is null - use old encryption (pre MC 1.16.4), otherwise use the new cipher one
            if (cipherMethod == null) {
                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, loginKey);
            } else {
                // Create ciphers from login key
                Object decryptionCipher = cipherMethod.invoke(null, Cipher.DECRYPT_MODE, loginKey);
                Object encryptionCipher = cipherMethod.invoke(null, Cipher.ENCRYPT_MODE, loginKey);

                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, decryptionCipher, encryptionCipher);
            }
        } catch (Exception ex) {
            disconnect("Couldn't enable encryption");
            return false;
        }
        return true;
    }

    private void disconnect(String logMessage, Exception... ex) {
        if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
            Tabheads.get().getTabLogger().warning(logMessage);
            if (ex != null && ex.length > 0) {
                for (Exception e : ex) {
                    e.printStackTrace();
                }
            }
        }
        kickPlayer();
    }

    private void kickPlayer() {
        PacketContainer kickPacket = new PacketContainer(DISCONNECT);
        kickPacket.getChatComponents().write(0, WrappedChatComponent.fromText("Disconnected"));
        TabheadsBukkit.PROTOCOL.sendServerPacket(player, kickPacket);
        player.kickPlayer("Disconnect");
    }

    private void receiveFakeStartPacket(String username, ClientPublicKey clientKey) {
        //see StartPacketListener for packet information
        PacketContainer startPacket = new PacketContainer(START);

        if (MinecraftVersion.getCurrentVersion().isAtLeast(new MinecraftVersion(1, 19, 0))) {
            startPacket.getStrings().write(0, username);

            EquivalentConverter<WrappedProfilePublicKey.WrappedProfileKeyData> converter = BukkitConverters.getWrappedPublicKeyDataConverter();
            Optional<WrappedProfilePublicKey.WrappedProfileKeyData> wrappedKey = Optional.ofNullable(clientKey).map(key ->
                    new WrappedProfilePublicKey.WrappedProfileKeyData(clientKey.expiry, clientKey.key, clientKey.signature)
            );

            startPacket.getOptionals(converter).write(0, wrappedKey);
        } else {
            //uuid is ignored by the packet definition
            WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), username);
            startPacket.getGameProfiles().write(0, fakeProfile);
        }

        //we don't want to handle our own packets so ignore filters
        startPacket.setMeta(ProtocolListener.SOURCE_META_KEY, plugin.getName());
        ProtocolLibrary.getProtocolManager().receiveClientPacket(player, startPacket, true);
    }
}
