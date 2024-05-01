package xyz.destiall.tabheads.bungee.task;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import xyz.destiall.tabheads.bungee.TabheadsBungee;
import xyz.destiall.tabheads.bungee.auth.BungeeLoginSource;
import xyz.destiall.tabheads.bungee.session.BungeeLoginSession;
import xyz.destiall.tabheads.core.JoinTask;

import java.util.UUID;

public class BungeeAsyncPremiumCheck extends JoinTask<BungeeLoginSource> implements Runnable {
    private final PreLoginEvent preLoginEvent;
    private final String username;
    private final PendingConnection connection;

    public BungeeAsyncPremiumCheck(PreLoginEvent preLoginEvent, PendingConnection connection, String username) {
        this.preLoginEvent = preLoginEvent;
        this.connection = connection;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            super.onLogin(username, new BungeeLoginSource(connection));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            preLoginEvent.completeIntent(TabheadsBungee.INSTANCE);
        }
    }

    @Override
    public void requestPremiumLogin(BungeeLoginSource source, UUID offline, String username) {
        TabheadsBungee.INSTANCE.putSession(source.getConnection(), new BungeeLoginSession(username, offline));
        try {
            source.enableOnlinemode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startCrackedSession(BungeeLoginSource source, String username) {
        TabheadsBungee.INSTANCE.putSession(source.getConnection(), new BungeeLoginSession(username));
    }
}
