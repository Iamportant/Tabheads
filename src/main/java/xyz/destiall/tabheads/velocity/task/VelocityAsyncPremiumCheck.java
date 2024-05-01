package xyz.destiall.tabheads.velocity.task;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import xyz.destiall.tabheads.core.JoinTask;
import xyz.destiall.tabheads.velocity.TabheadsVelocity;
import xyz.destiall.tabheads.velocity.auth.VelocityLoginSource;
import xyz.destiall.tabheads.velocity.session.VelocityLoginSession;

import java.util.UUID;

public class VelocityAsyncPremiumCheck extends JoinTask<VelocityLoginSource> implements Runnable {
    private final PreLoginEvent preLoginEvent;
    private final String username;

    public VelocityAsyncPremiumCheck(PreLoginEvent preLoginEvent, String username) {
        this.preLoginEvent = preLoginEvent;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            super.onLogin(username, new VelocityLoginSource(preLoginEvent));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestPremiumLogin(VelocityLoginSource source, UUID offline, String username) {
        TabheadsVelocity.INSTANCE.putSession(source.getConnection().getConnection().getRemoteAddress(), new VelocityLoginSession(username, offline));
        try {
            source.enableOnlinemode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startCrackedSession(VelocityLoginSource source, String username) {
        TabheadsVelocity.INSTANCE.putSession(source.getConnection().getConnection().getRemoteAddress(), new VelocityLoginSession(username));
    }
}
