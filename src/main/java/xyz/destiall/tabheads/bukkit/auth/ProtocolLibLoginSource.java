package xyz.destiall.tabheads.bukkit.auth;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.entity.Player;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.core.EncryptionUtil;
import xyz.destiall.tabheads.core.LoginSource;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

import static com.comphenix.protocol.PacketType.Login.Server.ENCRYPTION_BEGIN;

public class ProtocolLibLoginSource implements LoginSource {
    private final Player player;
    private final Random random;
    private final PublicKey publicKey;
    private byte[] verifyToken;

    public ProtocolLibLoginSource(Player player, Random random, PublicKey publicKey) {
        this.player = player;
        this.random = random;
        this.publicKey = publicKey;
    }

    @Override
    public void enableOnlinemode() {
        verifyToken = EncryptionUtil.generateVerifyToken(random);
        PacketContainer newPacket = new PacketContainer(ENCRYPTION_BEGIN);
        newPacket.getStrings().write(0, "");
        StructureModifier<PublicKey> keyModifier = newPacket.getSpecificModifier(PublicKey.class);
        int verifyField = 0;
        if (keyModifier.getFields().isEmpty()) {
            newPacket.getByteArrays().write(0, publicKey.getEncoded());
            verifyField++;
        } else {
            keyModifier.write(0, publicKey);
        }
        newPacket.getByteArrays().write(verifyField, verifyToken);
        TabheadsBukkit.PROTOCOL.sendServerPacket(player, newPacket);
    }

    public byte[] getVerifyToken() {
        return verifyToken.clone();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "player=" + player +
                ", random=" + random +
                ", verifyToken=" + Arrays.toString(verifyToken) +
                '}';
    }
}
