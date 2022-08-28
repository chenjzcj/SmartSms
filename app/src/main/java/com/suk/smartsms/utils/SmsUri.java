package com.suk.smartsms.utils;

import android.net.Uri;

/**
 * @author Administrator
 */
public class SmsUri {
    public final static Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");

    public final static Uri SMS_URI = Uri.parse("content://sms");
    public final static Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
    public final static Uri SMS_OUTBOX_URI = Uri.parse("content://sms/outbox");
    public final static Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
    public final static Uri SMS_DRAFT_URI = Uri.parse("content://sms/draft");

    public final static Uri GROUP_URI = Uri.parse("content://com.itheima.groupprovider/groups");
    public final static Uri GROUP_ID_URI = Uri.parse("content://com.itheima.groupprovider/groups/#");
    public final static Uri THREAD_GROUP_URI = Uri.parse("content://com.itheima.groupprovider/thread_group");
}
