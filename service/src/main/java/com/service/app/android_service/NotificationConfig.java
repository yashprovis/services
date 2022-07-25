package com.service.app.android_service;

import java.io.Serializable;

public class NotificationConfig implements Serializable {

    private CharSequence contentTitle;
    private Integer smallIcon;

    public CharSequence getContentTitle() {
        return contentTitle;
    }

    public Integer getSmallIcon() {
        return smallIcon;
    }

    public static class Builder {

        private CharSequence contentTitle;
        private int smallIcon;

        public Builder setContentTitle(CharSequence contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder setSmallIcon(int smallIcon) {
            this.smallIcon = smallIcon;
            return this;
        }

        public NotificationConfig build() {
            NotificationConfig notificationConfig = new NotificationConfig();

            notificationConfig.contentTitle = contentTitle;
            notificationConfig.smallIcon = smallIcon;

            return notificationConfig;
        }

    }

}
