package com.service.app.logging.notification;

public class NotificatorFactory {

    private static Notificator notificator = new DefaultNotificator();

    private NotificatorFactory() {
    }

    public static void setCustomNotificator(Notificator notificator) {
        NotificatorFactory.notificator = notificator;
    }

    public static Notificator getNotificator() {
        return notificator;
    }

}
